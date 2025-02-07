package financialmanager.objectFolder.transactionFolder;

import financialmanager.Utils.fileParser.DataColumns;
import financialmanager.Utils.fileParser.IFileParser;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.categoryFolder.CategoryProcessingService;
import financialmanager.objectFolder.contractFolder.ContractProcessingService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyProcessingService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionProcessingService {

    private final BankAccountService bankAccountService;
    private final UsersService usersService;
    private final TransactionService transactionService;
    private final ResponseService responseService;
    private final CounterPartyProcessingService counterPartyProcessingService;
    private final ContractProcessingService contractProcessingService;
    private final CategoryProcessingService categoryProcessingService;
    private static final String SUB_DIRECTORY = "bankAccountMessages";

    public ResponseEntity<Response> createTransactionsFromData(IFileParser fileParser, Long bankAccountId) {
        Users currentUser = usersService.getCurrentUser();

        try {
            String[] header = fileParser.getNextLineOfData();
            if (header == null) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_headerNotFound", fileParser.getFileName(), HttpStatus.NOT_FOUND);
            }

            Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, currentUser);

            if (bankAccountOptional.isEmpty()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_bankNotFound", null, HttpStatus.NOT_FOUND);
            }

            BankAccount bankAccount = bankAccountOptional.get();

            DataColumns dataColumns = findColumnsInData(header, bankAccount);
            if (!dataColumns.checkIfAllAreFound()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_notFourColumnsFound", null, HttpStatus.NOT_FOUND);
            }

            List<Transaction> newTransactions = parseTransactions(fileParser, bankAccount, dataColumns);
            if (newTransactions.isEmpty()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_noValidTransactions", null, HttpStatus.BAD_REQUEST);
            }

            List<Transaction> transactions = transactionService.findByBankAccountId(bankAccountId);

            newTransactions = checkIfTransactionsAlreadyExist(newTransactions, transactions);
            if (newTransactions.isEmpty()) {
                return responseService.createResponse(SUB_DIRECTORY, "info_noNewTransactionsFound", AlertType.INFO);
            }

            // Create or set counterparties for the new transactions
            counterPartyProcessingService.setCounterCounterParties(currentUser, newTransactions);

            // Now add the existing transactions
            transactions.addAll(newTransactions);

            categoryProcessingService.addTransactionsToCategories(currentUser, transactions);

            // Filter transactions that have a contract
            transactions = getTransactionsWithoutContract(transactions);

            contractProcessingService.checkIfTransactionsBelongToContract(transactions);

            transactionService.saveAll(transactions);
            return responseService.createResponse("SUB_DIRECTORY", "success_filesProcessed", AlertType.SUCCESS);

        } catch (Exception e) {
            return responseService.createErrorResponse(SUB_DIRECTORY, "error_generic", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Transaction> getTransactionsWithoutContract(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getContract() == null)
                .toList();
    }

    private DataColumns findColumnsInData(String[] header, BankAccount bankAccount) {
        int counterPartyColumn = 0, amountColumn = 0, amountAfterTransactionColumn = 0, dateColumn = 0;

        for (int i = 0; i < header.length; i++) {
            String headerLine = header[i];
            if (bankAccount.getCounterPartySearchStrings().contains(headerLine)) {
                counterPartyColumn = i;
            }
            if (bankAccount.getAmountSearchStrings().contains(headerLine)) {
                amountColumn = i;
            }
            if (bankAccount.getDateSearchStrings().contains(headerLine)) {
                dateColumn = i;
            }
            if (bankAccount.getAmountInBankAfterSearchStrings().contains(headerLine)) {
                amountAfterTransactionColumn = i;
            }
        }

        return new DataColumns(counterPartyColumn, amountColumn, amountAfterTransactionColumn, dateColumn);
    }

    private List<Transaction> parseTransactions(IFileParser fileParser, BankAccount bankAccount, DataColumns columns) {
        List<Transaction> newTransactions = new ArrayList<>();

        List<String[]> lines = fileParser.readAllLines();

        // Determine the direction of the lines
        boolean directionOfLine = findDirectionOfLines(lines, columns);

        // If the direction is bottom-to-top, reverse the lines
        if (!directionOfLine) {
            Collections.reverse(lines);
        }

        // Iterate over the lines in the correct direction
        for (String[] line : lines) {
            try {
                // Create and add a transaction for each line
                Transaction transaction = createTransactionFromLine(line, columns, bankAccount, newTransactions);
                newTransactions.add(transaction);
            } catch (Exception e) {
                System.err.println("Error parsing line: " + Arrays.toString(line) + " - " + e.getMessage());
            }
        }

        return newTransactions;
    }

    private List<Transaction> checkIfTransactionsAlreadyExist(List<Transaction> newTransactions,
                                                              List<Transaction> existingTransactions) {
        return newTransactions.stream()
                .filter(transaction -> !existingTransactions.contains(transaction))
                .collect(Collectors.toList());
    }

    private boolean findDirectionOfLines(List<String[]> lines, DataColumns columns) {
        int dataColumn = columns.dateColumn();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // Check if there are at least two lines to compare
        if (lines.size() < 2) {
            return true; // No need to determine direction with less than 2 rows
        }

        // Iterate over the lines and compare the current line with the next line
        for (int currentRow = 0; currentRow < lines.size() - 1; currentRow++) {
            String[] currentLine = lines.get(currentRow);
            String[] nextLine = lines.get(currentRow + 1);

            // Parse the dates from the current and next line
            String date1 = currentLine[dataColumn];
            String date2 = nextLine[dataColumn];

            // Parse the dates into LocalDate objects
            LocalDate localDate1 = LocalDate.parse(date1, formatter);
            LocalDate localDate2 = LocalDate.parse(date2, formatter);

            // Check if the dates are not equal
            if (!localDate1.isEqual(localDate2)) {
                // If the current date is after the next date, we know the order is descending
                return localDate1.isBefore(localDate2); // If date1 is before date2, return true (ascending), otherwise false (descending)
            }
        }

        // If no different dates are found, assume the list is in ascending order
        return true;
    }

    private Transaction createTransactionFromLine(String[] line, DataColumns columns, BankAccount bankAccount,
                                                 List<Transaction> newTransactions) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

        LocalDate date = LocalDate.parse(line[columns.dateColumn()], formatter);
        Double amount = numberFormat.parse(line[columns.amountColumn()]).doubleValue();
        Double amountAfterTransaction = numberFormat.parse(line[columns.amountAfterTransactionColumn()]).doubleValue();
        String counterPartyName = line[columns.counterPartyColumn()];

        Double amountBeforeTransaction = findAmountBeforeDate(date, newTransactions);

        if (amountBeforeTransaction == 0.0) {
            amountBeforeTransaction = amountAfterTransaction - amount;
        }

        return new Transaction(bankAccount, counterPartyName, date, amount, amountAfterTransaction, amountBeforeTransaction);
    }

    private Double findAmountBeforeDate(LocalDate date, List<Transaction> newTransactions) {
        Optional<Transaction> transactionBefore = newTransactions.stream()
                .filter(transaction -> transaction.getDate().isBefore(date))
                .max(Comparator.comparing(Transaction::getDate));

        if (transactionBefore.isPresent()) {
            return transactionBefore.get().getAmountInBankAfter();
        }

        return 0.0;
    }
}
