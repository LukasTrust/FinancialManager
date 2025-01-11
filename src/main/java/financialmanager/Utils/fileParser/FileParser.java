package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
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
import java.text.NumberFormat;
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

    private static final String SUB_DIRECTORY = "bankAccountMessages";
    private static final String DATA = "date";
    private static final String AMOUNT = "amount";
    private static final String AMOUNT_AFTER_TRANSACTION = "amountAfterTransaction";
    private static final String COUNTER_PARTY = "counterParty";

    protected final BufferedReader bufferedReader;

    @Override
    public ResponseEntity<Response> createTransactionsFromData(MultipartFile file, Long bankAccountId) {
        Users user = usersService.getCurrentUser();

        try {
            String[] header = getNextLineOfData(file);
            if (header == null) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_headerNotFound", file.getName(), HttpStatus.NOT_FOUND);
            }

            Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, user);

            if (bankAccountOptional.isEmpty()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_bankNotFound", null, HttpStatus.NOT_FOUND);
            }

            BankAccount bankAccount = bankAccountOptional.get();

            Map<String, List<String>> searchStrings = getSearchStrings(bankAccount);
            if (searchStrings.size() != 4) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_notAllSearchStringsFound", null, HttpStatus.NOT_FOUND);
            }

            Map<String, Integer> columnsInData = findColumnsInData(header, searchStrings);
            if (!areAllRequiredColumnsPresent(columnsInData)) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_notAllSearchStringsFound", null, HttpStatus.NOT_FOUND);
            }
            
            List<Transaction> newTransactions = parseTransactions(file, bankAccount, columnsInData);
            if (newTransactions.isEmpty()) {
                return responseService.createErrorResponse(SUB_DIRECTORY, "error_noValidTransactions", null, HttpStatus.BAD_REQUEST);
            }

            transactionService.saveAll(newTransactions);
            return responseService.createResponse("SUB_DIRECTORY", "success_filesProcessed",  AlertType.SUCCESS);

        } catch (Exception e) {
            return responseService.createErrorResponse(SUB_DIRECTORY, "error_generic", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Transaction> parseTransactions(MultipartFile file, BankAccount bankAccount, Map<String, Integer> columns) throws IOException {
        List<Transaction> newTransactions = new ArrayList<>();
        List<Transaction> existingTransactions = transactionService.findByBankAccountId(bankAccount.getId());
        List<CounterParty> counterParties = transactionService.findDistinctCounterPartiesByBankAccountId(bankAccount.getId());

        String[] line;
        while ((line = getNextLineOfData(file)) != null) {
            try {
                Transaction transaction = parseTransactionLine(line, columns, bankAccount,
                        counterParties, existingTransactions, newTransactions);
                newTransactions.add(transaction);

            } catch (Exception e) {
                System.err.println("Error parsing line: " + Arrays.toString(line) + " - " + e.getMessage());
            }
        }
        return newTransactions;
    }

    private Transaction parseTransactionLine(String[] line, Map<String, Integer> columns, BankAccount bankAccount,
                                             List<CounterParty> counterParties, List<Transaction> existingTransactions,
                                             List<Transaction> newTransactions) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

        LocalDate date = LocalDate.parse(line[columns.get(DATA)], formatter);
        Double amount = numberFormat.parse(line[columns.get(AMOUNT)]).doubleValue();
        Double amountAfterTransaction = numberFormat.parse(line[columns.get(AMOUNT_AFTER_TRANSACTION)]).doubleValue();
        String counterPartyName = line[columns.get(COUNTER_PARTY)];

        // Find or create the CounterParty
        CounterParty counterParty = counterParties.stream()
                .filter(cp -> cp.getName().equals(counterPartyName))
                .findFirst()
                .orElseGet(() -> {
                    CounterParty newCounterParty = counterPartyService.save(new CounterParty(counterPartyName));
                    counterParties.add(newCounterParty); // Add the newly created CounterParty to the list
                    return newCounterParty;
                });

        Double amountBeforeTransaction = !newTransactions.isEmpty()
                ? newTransactions.getLast().getAmountInBankAfter()
                : (existingTransactions.isEmpty() ? 0.0 : existingTransactions.getLast().getAmountInBankAfter());

        return new Transaction(bankAccount, counterParty, date, amount, amountAfterTransaction, amountBeforeTransaction);
    }

    private boolean areAllRequiredColumnsPresent(Map<String, Integer> columnsInData) {
        return columnsInData.containsKey(DATA) &&
                columnsInData.containsKey(AMOUNT) &&
                columnsInData.containsKey(AMOUNT_AFTER_TRANSACTION) &&
                columnsInData.containsKey(COUNTER_PARTY);
    }

    public abstract String[] getNextLineOfData(MultipartFile file);

    private Map<String, Integer> findColumnsInData(String[] header, Map<String, List<String>> listOfSearchStrings) {
        Map<String, Integer> columnsInData = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : listOfSearchStrings.entrySet()) {
            for (String searchString : entry.getValue()) {
                int index = Arrays.asList(header).indexOf(searchString);
                if (index != -1) {
                    columnsInData.put(entry.getKey(), index);
                }
            }
        }
        return columnsInData;
    }

    private Map<String, List<String>> getSearchStrings(BankAccount bankAccount) {
        Map<String, List<String>> searchStrings = new HashMap<>();
        addIfNotEmpty(searchStrings, COUNTER_PARTY, bankAccount.getCounterPartySearchStrings());
        addIfNotEmpty(searchStrings, AMOUNT, bankAccount.getAmountSearchStrings());
        addIfNotEmpty(searchStrings, AMOUNT_AFTER_TRANSACTION, bankAccount.getAmountInBankAfterSearchStrings());
        addIfNotEmpty(searchStrings, DATA, bankAccount.getDateSearchStrings());
        return searchStrings;
    }

    private void addIfNotEmpty(Map<String, List<String>> map, String key, List<String> values) {
        if (values != null && !values.isEmpty()) {
            map.put(key, values);
        }
    }
}