package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@AllArgsConstructor
public abstract class FileParser implements IFileParser {

    protected final BankAccountService bankAccountService;
    protected final UsersService usersService;
    protected final BufferedReader bufferedReader;
    private final String data = "date";
    private final String amount = "amount";
    private final String amountAfterTransaction = "amountAfterTransaction";
    private final String counterParty = "counterParty";

    public List<Transaction> createTransactionsFromData(MultipartFile file, Long bankId) {
        Users user = usersService.getCurrentUser();

        if (!bankAccountService.checkIfBankAccountBelongsToUser(bankId, user)) {
            return null;
        }

        String[] header = getNextLineOfData(file);
        if (header == null) {
            return null;
        }

        Map<String, List<String>> searchStrings = getSearchStrings(bankId);

        if (searchStrings == null) {
            return null;
        }

        Map<String, Integer> columnsInData = findColumnsInData(header, searchStrings);

        if (columnsInData.size() != 4){
            return null;
        }

        // Extract indices for each required column
        int dateIndex = columnsInData.getOrDefault(data, -1);
        int amountIndex = columnsInData.getOrDefault(amount, -1);
        int amountAfterTransactionIndex = columnsInData.getOrDefault(amountAfterTransaction, -1);
        int counterPartyIndex = columnsInData.getOrDefault(counterParty, -1);

        if (dateIndex == -1 || amountIndex == -1 || amountAfterTransactionIndex == -1 || counterPartyIndex == -1) {
            return null; // One or more columns are missing
        }

        List<Transaction> transactions = new ArrayList<>();

        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

        String[] line;
        while ((line = getNextLineOfData(file)) != null) {
            try {
                // Parse the required fields
                String dateStr = line[dateIndex];
                String amountStr = line[amountIndex];
                String amountAfterStr = line[amountAfterTransactionIndex];
                String counterPartyName = line[counterPartyIndex];

                // Convert values as needed
                LocalDate date = LocalDate.parse(dateStr, formatters);
                Double amount =  (Double) format.parse(amountStr);
                Double amountAfterTransaction = (Double) format.parse(amountAfterStr);

                // Create and add the Transaction object

            } catch (Exception e) {
                // Handle parsing errors (e.g., log and skip invalid lines)
                System.err.println("Error parsing line: " + Arrays.toString(line) + " - " + e.getMessage());
            }
        }

        return transactions;
    }

    public abstract String[] getNextLineOfData(MultipartFile file);

    private Map<String, Integer> findColumnsInData(String[] header, Map<String, List<String>>  listOfSearchStrings) {
        Map<String, Integer> columnsInData = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : listOfSearchStrings.entrySet()) {
            for (String value : entry.getValue()) {
                for (int column = 0; column < header.length; column++) {
                    if (header[column].equals(value)) {
                        columnsInData.put(entry.getKey(), column);
                    }
                }
            }
        }

        return columnsInData;
    }

    private Map<String, List<String>> getSearchStrings(Long bankId) {
        Users user = usersService.getCurrentUser();

        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankId, user);
        if (bankAccountOptional.isEmpty()) {
            return null;
        }

        BankAccount bankAccount = bankAccountOptional.get();
        List<String> counterPartySearchStrings = bankAccount.getCounterPartySearchStrings();
        List<String> amountSearchStrings = bankAccount.getAmountSearchStrings();
        List<String> amountAfterTransactionSearchStrings = bankAccount.getAmountInBankAfterSearchStrings();
        List<String> dateSearchStrings = bankAccount.getDateSearchStrings();

        Map<String, List<String>> searchStrings = new HashMap<>();
        if (!counterPartySearchStrings.isEmpty()) {
            searchStrings.put(counterParty, counterPartySearchStrings);
        }
        if (!amountSearchStrings.isEmpty()) {
            searchStrings.put(amount, amountSearchStrings);

        }
        if (!amountAfterTransactionSearchStrings.isEmpty()) {
            searchStrings.put(amountAfterTransaction, amountAfterTransactionSearchStrings);

        }
        if (!dateSearchStrings.isEmpty()) {
            searchStrings.put(data, dateSearchStrings);

        }

        return searchStrings;
    }
}