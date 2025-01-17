package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@AllArgsConstructor
public abstract class FileParser implements IFileParser {

    private final BankAccountService bankAccountService;
    private final UsersService usersService;
    private final CounterPartyService counterPartyService;
    private final TransactionService transactionService;
    private final ResponseService responseService;
    private final ContractService contractService;

    private static final String SUB_DIRECTORY = "bankAccountMessages";

    protected final BufferedReader bufferedReader;

    public abstract String[] getNextLineOfData();

    public abstract List<String[]> readAllLines();

    @Override
    public ResponseEntity<Response> createTransactionsFromData(MultipartFile file, Long bankAccountId) {
        Users user = usersService.getCurrentUser();

        try {
            String[] header = getNextLineOfData();
            if (header == null) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_headerNotFound", file.getName(), HttpStatus.NOT_FOUND);
            }

            Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, user);

            if (bankAccountOptional.isEmpty()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_bankNotFound", null, HttpStatus.NOT_FOUND);
            }

            BankAccount bankAccount = bankAccountOptional.get();

            DataColumns dataColumns = findColumnsInData(header, bankAccount);
            if (!dataColumns.checkIfAllAreFound()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_notFourColumnsFound", null, HttpStatus.NOT_FOUND);
            }

            List<Transaction> newTransactions = parseTransactions(bankAccount, dataColumns);
            if (newTransactions.isEmpty()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_noValidTransactions", null, HttpStatus.BAD_REQUEST);
            }

            newTransactions = transactionService.checkIfTransactionsAlreadyExist(newTransactions, bankAccountId);
            if (newTransactions.isEmpty()) {
                return responseService.createResponse(SUB_DIRECTORY, "info_noNewTransactionsFound", AlertType.INFO);
            }

            counterPartyService.createOrUpdateCounterParty(newTransactions);
            contractService.checkIfTransactionsBelongToContract(newTransactions);

            transactionService.saveAll(newTransactions);
            return responseService.createResponse("SUB_DIRECTORY", "success_filesProcessed", AlertType.SUCCESS);

        } catch (Exception e) {
            return responseService.createErrorResponse(SUB_DIRECTORY, "error_generic", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    private List<Transaction> parseTransactions(BankAccount bankAccount, DataColumns columns) throws IOException {
        List<Transaction> newTransactions = new ArrayList<>();
        List<String[]> lines = readAllLines();

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
                Transaction transaction = transactionService.createTransactionFromLine(line, columns, bankAccount, newTransactions);
                newTransactions.add(transaction);
            } catch (Exception e) {
                System.err.println("Error parsing line: " + Arrays.toString(line) + " - " + e.getMessage());
            }
        }

        return newTransactions;
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
}