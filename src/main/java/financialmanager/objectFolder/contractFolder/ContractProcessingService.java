package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ContractProcessingService {

    private final ContractService contractService;
    private final TransactionService transactionService;

    public void checkIfTransactionsBelongToContract(List<Transaction> transactions) {
        Long bankAccountId = transactions.getFirst().getBankAccount().getId();
        List<Contract> contracts = contractService.findByBankAccountId(bankAccountId);

        // Add transactions without contracts found in the database
        transactions.addAll(transactionService.findByBankAccountIdAndNoContract(bankAccountId));

        assignTransactionsToExistingContracts(transactions, contracts);

        List<Transaction> transactionsWithoutContract = getTransactionsWithoutContract(transactions);

        contracts.addAll(tryToFindNewContracts(transactionsWithoutContract));
        contractService.saveAll(contracts);
    }

    private void checkIfExistingContractsChanged(List<Transaction> transactionsWithOutContract, List<Contract> contracts) {
        for (Contract contract : contracts) {
            List<Transaction> possibleMatches = transactionsWithOutContract.stream().filter(
                    transaction -> isTransactionValidForContract(transaction, contract, false)).toList();

            if (possibleMatches.size() > 2) {
                
            }
        }
    }

    private void assignTransactionsToExistingContracts(List<Transaction> transactions, List<Contract> contracts) {
        for (Contract contract : contracts) {
            transactions.stream()
                    .filter(transaction -> isTransactionValidForContract(transaction, contract, true))
                    .forEach(transaction -> {
                        transaction.setContract(contract);
                        if (contract.getLastPaymentDate().isBefore(transaction.getDate()))
                            contract.setLastPaymentDate(transaction.getDate());
                    });
        }
    }

    private List<Transaction> getTransactionsWithoutContract(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getContract() == null)
                .toList();
    }

    private boolean isTransactionValidForContract(Transaction transaction, Contract contract, boolean checkAmount) {
        if (checkAmount) {
            return contract.getAmount().equals(transaction.getAmount())
                    && contract.getContractSearchStrings().contains(transaction.getOriginalCounterParty())
                    && isValidTransactionDate(transaction.getDate(), contract.getStartDate(), contract.getMonthsBetweenPayments());
        }

        return contract.getContractSearchStrings().contains(transaction.getOriginalCounterParty())
                && isValidTransactionDate(transaction.getDate(), contract.getStartDate(), contract.getMonthsBetweenPayments());
    }

    private boolean isValidTransactionDate(LocalDate transactionDate, LocalDate startDate, int monthsBetweenPayments) {
        long monthsDifference = ChronoUnit.MONTHS.between(startDate, transactionDate);
        return monthsDifference >= 0 && monthsDifference % monthsBetweenPayments == 0;
    }

    private List<Contract> tryToFindNewContracts(List<Transaction> transactionsWithOutContract) {
        if (transactionsWithOutContract == null || transactionsWithOutContract.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Double, List<Transaction>> groupedByAmount = transactionsWithOutContract.stream()
                .collect(Collectors.groupingBy(Transaction::getAmount));

        List<Contract> potentialContracts = new ArrayList<>();

        groupedByAmount.forEach((amount, amountTransactions) -> {
            Map<String, List<Transaction>> groupedByCounterparty = amountTransactions.stream()
                    .collect(Collectors.groupingBy(Transaction::getOriginalCounterParty));

            groupedByCounterparty.forEach((counterparty, counterpartyTransactions) -> {
                if (counterpartyTransactions.size() > 2) {
                    tryToIdentifyPattern(counterpartyTransactions, amount)
                            .ifPresent(potentialContracts::add);
                }
            });
        });

        return potentialContracts;
    }

    private Optional<Contract> tryToIdentifyPattern(List<Transaction> transactions, Double amount) {
        transactions.sort(Comparator.comparing(Transaction::getDate));

        List<Long> intervals = calculateIntervalsBetweenTransactions(transactions);

        if (isConsistentInterval(intervals)) {
            int monthsBetweenPayments = calculateMonthsFromDays(intervals.getFirst());

            Contract newContract = createContractFromTransactions(transactions, amount, monthsBetweenPayments);

            return Optional.of(newContract);
        }

        return Optional.empty();
    }

    private List<Long> calculateIntervalsBetweenTransactions(List<Transaction> transactions) {
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < transactions.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(
                    transactions.get(i - 1).getDate(),
                    transactions.get(i).getDate()
            );
            intervals.add(daysBetween);
        }
        return intervals;
    }

    private boolean isConsistentInterval(List<Long> intervals) {
        // Define a tolerance of ±10 days
        final long TOLERANCE = 10;

        // Calculate the average interval
        double averageInterval = intervals.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        // Check if all intervals are within the tolerance of the average
        return intervals.stream()
                .allMatch(interval -> Math.abs(interval - averageInterval) <= TOLERANCE);
    }

    private int calculateMonthsFromDays(long days) {
        return (int) Math.round((double) days / 30);
    }

    private Contract createContractFromTransactions(List<Transaction> transactions, Double amount, int monthsBetweenPayments) {
        Transaction firstTransaction = transactions.getFirst();

        Contract newContract = new Contract(
                firstTransaction.getOriginalCounterParty(),
                firstTransaction.getDate(),
                transactions.getLast().getDate(),
                monthsBetweenPayments,
                amount
        );

        transactions.forEach(transaction -> transaction.setContract(newContract));

        return newContract;
    }
}
