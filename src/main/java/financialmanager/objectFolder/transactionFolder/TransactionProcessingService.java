package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.categoryFolder.CategoryService;
import financialmanager.objectFolder.contractFolder.BaseContractService;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractProcessingService;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.BaseContractHistoryService;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.localeFolder.LocaleService;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.Utils.fileParser.DataColumns;
import financialmanager.Utils.fileParser.FileParserFactory;
import financialmanager.Utils.fileParser.IFileParser;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionProcessingService {

    private final BaseTransactionService baseTransactionService;
    private final BaseContractService baseContractService;
    private final BaseContractHistoryService baseContractHistoryService;

    private final ContractProcessingService contractProcessingService;
    private final CategoryService categoryService;
    private final FileParserFactory fileParserFactory;
    private final ResponseService responseService;
    private final ResultService resultService;
    private final LocaleService localeService;

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);
    private final CounterPartyService counterPartyService;

    public ResponseEntity<?> deleteData(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        BankAccount bankAccount = bankAccountResult.getValue();

        try {
            List<Transaction> transactions = baseTransactionService.findByBankAccount(bankAccount);
            List<Contract> contracts = baseContractService.findByBankAccount(bankAccount);
            List<ContractHistory> contractHistories = baseContractHistoryService.findByContractIn(contracts);

            baseTransactionService.deleteAll(transactions);
            baseContractHistoryService.deleteAll(contractHistories);

            baseContractService.deleteAll(contracts);

            return responseService.createResponse(HttpStatus.OK, "deletedData", AlertType.SUCCESS);
        } catch (Exception e) {
            log.error("Error deleting data", e);
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "deletedData", AlertType.ERROR);
        }
    }

    public ResponseEntity<?> uploadDataForTransactions(Long bankAccountId, MultipartFile[] files) {
        List<ResponseEntity<Response>> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            responses.add(processFileAsync(file, bankAccountId));
        }

        return ResponseEntity.ok(responses);
    }

    private ResponseEntity<Response> processFileAsync(MultipartFile file, Long bankAccountId) {
        IFileParser fileParser = fileParserFactory.getFileParser(file);
        return createTransactionsFromData(fileParser, bankAccountId);
    }

    private ResponseEntity<Response> createTransactionsFromData(IFileParser fileParser, Long bankAccountId) {
        String fileName = fileParser.getFileName();
        String[] header;

        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        header = fileParser.getNextLineOfData();

        if (header == null) {
            log.error("{} no header line found", fileName);
            return responseService.createResponseWithPlaceHolders(HttpStatus.NOT_FOUND, "headerNotFound",
                    AlertType.ERROR, Collections.singletonList(fileName));
        }

        BankAccount bankAccount = bankAccountResult.getValue();
        DataColumns dataColumns = findColumnsInData(header, bankAccount);

        if (!dataColumns.checkIfAllAreFound()) {
            log.error("{} could not find the date columns", fileName);
            log.error("Header line: {}", Arrays.toString(header));
            return responseService.createResponse(HttpStatus.NOT_FOUND, "notFourColumnsFound", AlertType.ERROR);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<Transaction> newTransactions = parseTransactions(fileParser, bankAccount, dataColumns);
        stopWatch.stop();
        log.info("{} for parseTransactions", stopWatch.getTotalTimeMillis());

        if (newTransactions.isEmpty()) {
            log.error("{} could not find any transactions", fileName);
            return responseService.createResponse(HttpStatus.BAD_REQUEST, "noValidTransactions", AlertType.ERROR);
        }

        stopWatch.start();
        List<Transaction> existingTransactions = baseTransactionService.findByBankAccountAndContractNull(bankAccount);
        newTransactions = filterNewTransactions(newTransactions, existingTransactions);
        stopWatch.stop();
        log.info("{} for filterNewTransactions", stopWatch.getTotalTimeMillis());

        if (newTransactions.isEmpty()) {
            log.info("{} could not find any transactions", fileName);
            return responseService.createResponseWithPlaceHolders(HttpStatus.NOT_FOUND, "noNewTransactionsFound",
                    AlertType.INFO, Collections.singletonList(fileName));
        }

        processAndSaveTransactions(bankAccount, newTransactions, existingTransactions);

        log.info("{} transactions found", newTransactions.size());
        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "filesProcessed",
                AlertType.SUCCESS, Arrays.asList(fileName, String.valueOf(newTransactions.size())));
    }

    private void processAndSaveTransactions(BankAccount bankAccount, List<Transaction> newTransactions, List<Transaction> existingTransactions) {
        Users currentUser = bankAccount.getUsers();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        counterPartyService.setCounterPartyForNewTransactions(currentUser, newTransactions);
        stopWatch.stop();
        log.info("{} for setCounterPartyForNewTransactions", stopWatch.getTotalTimeMillis());

        stopWatch = new StopWatch();
        stopWatch.start();
        categoryService.addTransactionsToCategories(currentUser, newTransactions);
        stopWatch.stop();
        log.info("{} for addTransactionsToCategories", stopWatch.getTotalTimeMillis());

        newTransactions.addAll(existingTransactions);

        stopWatch = new StopWatch();
        stopWatch.start();
        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, newTransactions);
        stopWatch.stop();
        log.info("{} for checkIfTransactionsBelongToContract", stopWatch.getTotalTimeMillis());

        baseTransactionService.saveAll(newTransactions);
    }

    private DataColumns findColumnsInData(String[] header, BankAccount bankAccount) {
        Integer counterPartyColumn = null, amountColumn = null,
                amountAfterTransactionColumn = null, dateColumn = null;

        for (int i = 0; i < header.length; i++) {
            String headerLine = header[i];
            if (bankAccount.getCounterPartySearchStrings().contains(headerLine)) {
                counterPartyColumn = i;
            } else if (bankAccount.getAmountSearchStrings().contains(headerLine)) {
                amountColumn = i;
            } else if (bankAccount.getDateSearchStrings().contains(headerLine)) {
                dateColumn = i;
            } else if (bankAccount.getAmountInBankAfterSearchStrings().contains(headerLine)) {
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

        Double amountBeforeTransaction = 0.0;

        // Iterate over the lines in the correct direction
        Locale currentLocale = localeService.getCurrentLocale();

        // Define date formatter based on locale
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", currentLocale);

        for (String[] line : lines) {
            // Create and add a transaction for each line
            Transaction transaction = createTransactionFromLine(line, columns, bankAccount, amountBeforeTransaction, currentLocale, formatter);
            if (transaction != null) {
                amountBeforeTransaction = transaction.getAmountInBankAfter();
                newTransactions.add(transaction);
            }
        }

        return newTransactions;
    }

    private List<Transaction> filterNewTransactions(List<Transaction> newTransactions,
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
                                                  Double amountBeforeTransaction, Locale currentLocale, DateTimeFormatter formatter) {
        LocalDate date;
        Double amount;
        Double amountAfterTransaction;
        String counterPartyName;

        try {
            // Parse date
            date = LocalDate.parse(line[columns.dateColumn()], formatter);

            // Parse amount
            amount = parseLocalizedNumber(line[columns.amountColumn()], currentLocale);
            amountAfterTransaction = parseLocalizedNumber(line[columns.amountAfterTransactionColumn()], currentLocale);

            counterPartyName = line[columns.counterPartyColumn()];

            if (amountBeforeTransaction == 0.0)
                amountBeforeTransaction = Math.round((amountAfterTransaction - amount) * 100) / 100.0;

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("Error parsing line: {}", Arrays.toString(line));
            return null;
        }

        return new Transaction(bankAccount, counterPartyName, date, amount, amountAfterTransaction,
                amountBeforeTransaction);
    }

    private static Double parseLocalizedNumber(String numberString, Locale locale) throws ParseException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        char decimalSeparator = symbols.getDecimalSeparator();

        // Normalize decimal separator to the locale-specific one
        numberString = numberString.replace('.', decimalSeparator).replace(',', decimalSeparator);

        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance(locale);
        decimalFormat.setParseBigDecimal(true);

        return decimalFormat.parse(numberString).doubleValue();
    }
}
