package financialmanager.objectFolder.transactionFolder;

import financialmanager.Utils.fileParser.DataColumns;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByBankAccountId(Long bankAccountId) {
        return transactionRepository.findByBankAccountId(bankAccountId);
    }

    public List<Transaction> findByBankAccountIdAndNoContract(Long bankAccountId) {
        return transactionRepository.findByBankAccountIdAndContractIsNull(bankAccountId);
    }

    public void saveAll(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    public List<CounterParty> findDistinctCounterPartiesByBankAccountId(Long accountId) {
        return transactionRepository.findDistinctCounterPartiesByBankAccountId(accountId);
    }

    public List<Contract> findDistinctContractsByBankAccountId(Long accountId) {
        return transactionRepository.findDistinctContractsByBankAccountId(accountId);
    }

    public List<Transaction> checkIfTransactionsAlreadyExist(List<Transaction> transactions, Long bankAccountId) {
        List<Transaction> existingTransactions = findByBankAccountId(bankAccountId);
        Set<Transaction> existingTransactionSet = new HashSet<>(existingTransactions);

        // Filter out transactions that already exist
        return transactions.stream()
                .filter(transaction -> existingTransactionSet.stream()
                        .noneMatch(transaction::compare))
                .toList();
    }

    public Transaction createTransactionFromLine(String[] line, DataColumns columns, BankAccount bankAccount,
                                                 List<Transaction> newTransactions) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

        LocalDate date = LocalDate.parse(line[columns.dateColumn()], formatter);
        Double amount = numberFormat.parse(line[columns.amountColumn()]).doubleValue();
        Double amountAfterTransaction = numberFormat.parse(line[columns.amountAfterTransactionColumn()]).doubleValue();
        String counterPartyName = line[columns.counterPartyColumn()];

        CounterParty counterParty = new CounterParty(counterPartyName);

        Double amountBeforeTransaction = findAmountBeforeDate(date, bankAccount.getId(), newTransactions);

        if (amountBeforeTransaction == 0.0) {
            amountBeforeTransaction = amountAfterTransaction - amount;
        }

        return new Transaction(bankAccount, counterParty, date, amount, amountAfterTransaction, amountBeforeTransaction);
    }

    private Double findAmountBeforeDate(LocalDate date, Long accountId, List<Transaction> newTransactions) {
        List<Transaction> transactions = findByBankAccountId(accountId);
        newTransactions.addAll(transactions);

        Optional<Transaction> transactionBefore = newTransactions.stream()
                .filter(transaction -> transaction.getDate().isBefore(date))
                .max(Comparator.comparing(Transaction::getDate));

        if (transactionBefore.isPresent()) {
            return transactionBefore.get().getAmountInBankAfter();
        }

        return 0.0;
    }
}
