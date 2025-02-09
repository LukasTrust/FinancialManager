package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistoryService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.Transaction;
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
    private final ContractHistoryService contractHistoryService;

    public void checkIfTransactionsBelongToContract(List<Transaction> transactions) {
        Optional<Transaction> lastTransaction = transactions.stream().max(Comparator.comparing(Transaction::getDate));

        LocalDate lastTransactionDate = LocalDate.now();

        if (lastTransaction.isPresent()) {
            lastTransactionDate = lastTransaction.get().getDate();
        }

        Long bankAccountId = transactions.getFirst().getBankAccount().getId();
        List<Contract> contracts = contractService.findByBankAccountId(bankAccountId);

        assignTransactionsToExistingContracts(transactions, contracts);

        // Find contracts that have changed
        transactions = checkIfExistingContractsChanged(transactions, contracts);

        // Find new contracts
        List<Contract> newContracts = tryToFindNewContracts(transactions);

        if (newContracts != null && !newContracts.isEmpty()) {
            contracts.addAll(newContracts);
        }

        closeContracts(lastTransactionDate, contracts);

        contractService.saveAll(contracts);
    }

    private List<Transaction> checkIfExistingContractsChanged(List<Transaction> transactionsWithOutContract, List<Contract> contracts) {
        List<Contract> changedContracts = new ArrayList<>();
        List<ContractHistory> contractHistories = new ArrayList<>();
        List<Transaction> newTransactions = new ArrayList<>(transactionsWithOutContract);

        for (Contract contract : contracts) {
            List<Transaction> possibleMatches = new ArrayList<>(transactionsWithOutContract.stream().filter(transaction ->
                    isTransactionValidForContract(transaction, contract, false)).toList());

            if (possibleMatches.size() > 2) {
                possibleMatches.sort(Comparator.comparing(Transaction::getDate));
                Transaction firstTransaction = possibleMatches.getFirst();

                ContractHistory contractHistory = new ContractHistory(contract, firstTransaction.getAmount(), firstTransaction.getDate());
                contractHistories.add(contractHistory);

                contract.setAmount(firstTransaction.getAmount());
                contract.setLastUpdatedAt(firstTransaction.getDate());
                contract.setLastPaymentDate(possibleMatches.getLast().getDate());

                changedContracts.add(contract);

                setContractForTransactions(contract, possibleMatches);

                // Remove transactions that new have a contract
                newTransactions.removeAll(possibleMatches);
            }
        }

        if (!changedContracts.isEmpty()) {
            contractHistoryService.saveAll(contractHistories);
        }

        return newTransactions;
    }

    private void assignTransactionsToExistingContracts(List<Transaction> transactions, List<Contract> contracts) {
        // Group transactions by contract for lookup
        Map<Contract, List<Transaction>> contractTransactionsLookUp = new HashMap<>();
        for (Contract contract : contracts) {
            for (Transaction transaction : transactions) {
                if (isTransactionValidForContract(transaction, contract, true)) {
                    contractTransactionsLookUp.computeIfAbsent(contract, _ -> new ArrayList<>()).add(transaction);
                }
            }
        }

        // Assign transactions and update last payment date
        contractTransactionsLookUp.forEach((contract, transactionList) -> {
            setContractForTransactions(contract, transactionList);
            updateLastPaymentDate(contract, transactionList);
            updateStartDate(contract, transactionList);
        });

        // Remove all transactions that have new a contract
        transactions = new ArrayList<>(transactions);
        Set<Transaction> transactionsToRemove = contractTransactionsLookUp.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()); // Use Set for better performance

        transactions.removeAll(transactionsToRemove);
    }

    private void updateLastPaymentDate(Contract contract, List<Transaction> transactions) {
        Optional<Transaction> lastTransaction = transactions.stream().max(Comparator.comparing(Transaction::getDate));

        lastTransaction.ifPresent(transaction -> {
            if (contract.getLastPaymentDate().isBefore(transaction.getDate())) {
                contract.setLastPaymentDate(transaction.getDate());
            }
        });
    }

    private void updateStartDate(Contract contract, List<Transaction> transactions) {
        Optional<Transaction> firstDate = transactions.stream().min(Comparator.comparing(Transaction::getDate));

        firstDate.ifPresent(transaction -> {
            if (contract.getStartDate().isAfter(transaction.getDate())) {
                contract.setStartDate(transaction.getDate());
            }
        });
    }

    private void closeContracts(LocalDate lastTransactionDate, List<Contract> contracts) {
        for (Contract contract : contracts) {
            LocalDate lastPaymentDate = contract.getLastPaymentDate();

            // Add extra time to be sure the contract has stopped
            LocalDate lastTransactionDateAndExtra = lastTransactionDate.plusMonths(contract.getMonthsBetweenPayments() * 2L);

            if (lastPaymentDate.isAfter(lastTransactionDateAndExtra)) {
                contract.setEndDate(lastPaymentDate);
            }
        }
    }

    private boolean isTransactionValidForContract(Transaction transaction, Contract contract, boolean checkAmount) {
        if (checkAmount) {
            return contract.getAmount().equals(transaction.getAmount()) &&
                    contract.getCounterParty().equals(transaction.getCounterParty()) &&
                    isValidTransactionDate(transaction.getDate(), contract.getStartDate(), contract.getMonthsBetweenPayments());
        }

        return contract.getCounterParty().equals(transaction.getCounterParty()) &&
                isValidTransactionDate(transaction.getDate(), contract.getStartDate(), contract.getMonthsBetweenPayments());
    }

    private boolean isValidTransactionDate(LocalDate transactionDate, LocalDate startDate, int monthsBetweenPayments) {
        long monthsDifference = ChronoUnit.MONTHS.between(startDate, transactionDate);
        return monthsDifference >= 0 && monthsDifference % monthsBetweenPayments == 0;
    }

    private List<Contract> tryToFindNewContracts(List<Transaction> transactionsWithOutContract) {
        if (transactionsWithOutContract.isEmpty()) {
            return null;
        }

        Map<Double, List<Transaction>> groupedByAmount = transactionsWithOutContract.stream()
                .collect(Collectors.groupingBy(Transaction::getAmount));

        List<Contract> potentialContracts = new ArrayList<>();

        groupedByAmount.forEach((_, amountTransactions) -> {
            Map<CounterParty, List<Transaction>> groupedByCounterParty = amountTransactions.stream()
                    .collect(Collectors.groupingBy(Transaction::getCounterParty));

            groupedByCounterParty.forEach((_, possibleMatches) -> {
                if (possibleMatches.size() > 2) {
                    tryToIdentifyPattern(possibleMatches).ifPresent(potentialContracts::add);
                }
            });
        });

        return potentialContracts;
    }

    private Optional<Contract> tryToIdentifyPattern(List<Transaction> transactions) {
        transactions.sort(Comparator.comparing(Transaction::getDate));

        List<Long> intervals = calculateIntervalsBetweenTransactions(transactions);

        if (isConsistentInterval(intervals)) {
            int monthsBetweenPayments = calculateMonthsFromDays(intervals.getFirst());

            Contract newContract = createContractFromTransactions(transactions, monthsBetweenPayments);

            return Optional.of(newContract);
        }

        return Optional.empty();
    }

    private List<Long> calculateIntervalsBetweenTransactions(List<Transaction> transactions) {
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < transactions.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(transactions.get(i - 1).getDate(), transactions.get(i).getDate());
            intervals.add(daysBetween);
        }
        return intervals;
    }

    private boolean isConsistentInterval(List<Long> intervals) {
        // Define a tolerance of ±10 days
        final long TOLERANCE = 10;

        // Calculate the average interval
        double averageInterval = intervals.stream().mapToLong(Long::longValue).average().orElse(0);

        // Check if all intervals are within the tolerance of the average
        return intervals.stream().allMatch(interval -> Math.abs(interval - averageInterval) <= TOLERANCE);
    }

    private int calculateMonthsFromDays(long days) {
        return (int) Math.round((double) days / 30);
    }

    private Contract createContractFromTransactions(List<Transaction> transactions, int monthsBetweenPayments) {
        Transaction firstTransaction = transactions.getFirst();

        Contract newContract = new Contract(firstTransaction.getDate(), transactions.getLast().getDate(), monthsBetweenPayments,
                firstTransaction.getAmount(), firstTransaction.getCounterParty());

       setContractForTransactions(newContract, transactions);

        return newContract;
    }

    private void setContractForTransactions(Contract contract, List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setContract(contract));
    }
}