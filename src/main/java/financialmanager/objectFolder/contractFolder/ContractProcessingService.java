package financialmanager.objectFolder.contractFolder;

import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.BaseContractHistoryService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ContractProcessingService {

    private final BaseContractService baseContractService;
    private final BaseContractHistoryService baseContractHistoryService;
    private final BaseTransactionService baseTransactionService;

    private static final Logger log = LoggerFactory.getLogger(ContractProcessingService.class);

    public void checkIfTransactionsBelongToContract(BankAccount bankAccount, List<Transaction> transactionsWithoutContracts) {
        List<Contract> existingContract = baseContractService.findByBankAccount(bankAccount);
        List<ContractHistory> existingContractHistories = baseContractHistoryService.findByContractIn(existingContract);

        Map<Contract, List<ContractHistory>> contractHistoryMap = Utils.mapContractHistoryToContract(existingContract, existingContractHistories);
        matchTransactionsToExistingContract(transactionsWithoutContracts, contractHistoryMap);

        transactionsWithoutContracts = transactionsWithoutContracts.stream().filter(transaction -> transaction.getContract() == null).toList();

        createNewContracts(transactionsWithoutContracts);
    }

    private void matchTransactionsToExistingContract(List<Transaction> transactions,
                                                     Map<Contract, List<ContractHistory>> contractHistoryMap) {
        for (Map.Entry<Contract, List<ContractHistory>> entry : contractHistoryMap.entrySet()) {
            List<ContractHistory> contractHistories = new ArrayList<>(entry.getValue());

            matchTransactionsToExistingContract(transactions, entry.getKey(), contractHistories);

            entry.setValue(contractHistories);
        }
    }

    //<editor-fold desc="help methods">
    private Map<Double, List<Transaction>> mapTransactionByAmount(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getAmount))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 2)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<CounterParty, Map<Double, List<Transaction>>> mapTransactionByAmountAndCounterParty(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCounterParty))
                .entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), mapTransactionByAmount(entry.getValue())))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Boolean, List<Transaction>> matchTransactionToContractAmount(
            List<Transaction> transactions, Contract contract, List<ContractHistory> contractHistories) {

        Map<Boolean, List<Transaction>> matchedAndUnmatchedTransactions = new HashMap<>();

        for (Transaction transaction : transactions) {
            boolean isMatching = transaction.getAmount().equals(contract.getAmount()) ||
                    contractHistories.stream()
                            .anyMatch(ch -> ch.getPreviousAmount().equals(transaction.getAmount()));

            matchedAndUnmatchedTransactions.computeIfAbsent(isMatching, k -> new ArrayList<>()).add(transaction);
        }

        return matchedAndUnmatchedTransactions;
    }

    private Map<Integer, List<Transaction>> calculateMostFrequentMonthsBetween(List<Transaction> transactions) {
        transactions.sort(Comparator.comparing(Transaction::getDate));

        Map<Integer, List<Transaction>> monthFrequencyMap = new HashMap<>();

        int allowedOffset = 3;

        for (int monthsBetween = 1; monthsBetween <= 12; monthsBetween++) {
            List<Transaction> matchingTransactions = new ArrayList<>();

            for (int i = 1; i < transactions.size(); i++) {
                LocalDate previousDate = transactions.get(i - 1).getDate();
                LocalDate currentDate = transactions.get(i).getDate();

                // Adjust date range with ±2-day tolerance
                LocalDate minValidDate = previousDate.plusMonths(monthsBetween).minusDays(allowedOffset);
                LocalDate maxValidDate = previousDate.plusMonths(monthsBetween).plusDays(allowedOffset);

                if (!currentDate.isBefore(minValidDate) && !currentDate.isAfter(maxValidDate)) {
                    matchingTransactions.add(transactions.get(i - 1));
                    matchingTransactions.add(transactions.get(i));
                }
            }

            if (matchingTransactions.size() > 2) {
                transactions.removeAll(matchingTransactions);
                monthFrequencyMap.put(monthsBetween, new ArrayList<>(new LinkedHashSet<>(matchingTransactions)));
            }
        }

        return monthFrequencyMap;
    }

    private List<Transaction> getCandidatesForExistingContract(List<Transaction> transactions, Contract contract) {
        List<Transaction> transactionsWithSameCounterParty = new ArrayList<>(
                transactions.stream().filter(transaction -> transaction.getCounterParty().equals(contract.getCounterParty())).toList());

        LocalDate firstDate = getEarliestTransactionDate(transactionsWithSameCounterParty);
        int monthsBetween = contract.getMonthsBetweenPayments();

        int allowedOffset = 3;
        if (firstDate.isBefore(contract.getStartDate())) {
            LocalDate minValidDate = firstDate.plusMonths(monthsBetween).minusDays(allowedOffset);
            LocalDate maxValidDate = firstDate.plusMonths(monthsBetween).plusDays(allowedOffset);



            if (!currentDate.isBefore(minValidDate) && !currentDate.isAfter(maxValidDate)) {
                matchingTransactions.add(transactions.get(i - 1));
                matchingTransactions.add(transactions.get(i));
            }
        }

        return monthsBetweenMap.get(contract.getMonthsBetweenPayments());
    }

    private LocalDate getEarliestTransactionDate(List<Transaction> transactions) {
        return transactions.stream().min(Comparator.comparing(Transaction::getDate)).map(Transaction::getDate).orElse(LocalDate.now());
    }

    private LocalDate getLatestTransactionDate(List<Transaction> transactions) {
        return transactions.stream().max(Comparator.comparing(Transaction::getDate)).map(Transaction::getDate).orElse(LocalDate.now());
    }

    //</editor-fold>

    //<editor-fold desc="create contract">
    private void createNewContracts(List<Transaction> transactions) {
        Map<CounterParty, Map<Double, List<Transaction>>> counterPartyMapMap = mapTransactionByAmountAndCounterParty(transactions);

        for (Map.Entry<CounterParty, Map<Double, List<Transaction>>> mainEntry : counterPartyMapMap.entrySet()) {
            for (Map.Entry<Double, List<Transaction>> subEntry : mainEntry.getValue().entrySet()) {
                if (!subEntry.getValue().isEmpty())
                    processMainEntry(mainEntry.getValue(), subEntry.getKey(), subEntry.getValue());
            }
        }
    }

    private void processMainEntry(Map<Double, List<Transaction>> amountMap, Double key, List<Transaction> transactions) {
        Map<Integer, List<Transaction>> monthsBetweenMap = calculateMostFrequentMonthsBetween(transactions);

        for (Map.Entry<Integer, List<Transaction>> entry : monthsBetweenMap.entrySet()) {
            List<Transaction> monthTransactions = entry.getValue();
            int monthsBetween = entry.getKey();

            if (monthTransactions.size() < 3)
                continue;

            LocalDate firstDate = getEarliestTransactionDate(monthTransactions);
            List<Transaction> transactionsThatAlsoMatch = removeAndMergeMatchingSubEntries(amountMap, monthsBetween);
            monthTransactions.addAll(transactionsThatAlsoMatch);

            finalizeContract(monthTransactions, firstDate, monthsBetween);
        }
    }

    private List<Transaction> removeAndMergeMatchingSubEntries(Map<Double, List<Transaction>> amountMap, int monthsBetween) {
        List<Transaction> allMatchingTransactions = new ArrayList<>();

        for (Map.Entry<Double, List<Transaction>> subEntry : amountMap.entrySet()) {
            List<Transaction> subTransactions = subEntry.getValue();
            if (subTransactions.isEmpty()) continue;

            Map<Integer, List<Transaction>> monthsBetweenMap = calculateMostFrequentMonthsBetween(subTransactions);

            List<Transaction> currentMatchingTransactions = monthsBetweenMap.get(monthsBetween);

            if (currentMatchingTransactions != null) {
                allMatchingTransactions.addAll(currentMatchingTransactions);
            }
        }

        return allMatchingTransactions;
    }

    private void finalizeContract(List<Transaction> transactions, LocalDate firstDate, int monthsBetween) {
        LocalDate lastPaymentDate = getLatestTransactionDate(transactions);

        Transaction firstTransaction = transactions.getFirst();
        Contract contract = new Contract(
                firstDate, lastPaymentDate,
                monthsBetween,
                firstTransaction.getAmount(), firstTransaction.getCounterParty(), firstTransaction.getBankAccount()
        );

        updateExistingContract(transactions, contract, new ArrayList<>());
        baseTransactionService.setContract(contract, transactions);
        baseContractService.save(contract);
    }

    //</editor-fold>

    //<editor-fold desc="update contract">
    private List<Transaction> updateExistingContract(List<Transaction> transactions, Contract contract,
                                                     List<ContractHistory> contractHistories) {
        Map<Double, List<Transaction>> transactionsByAmount = mapTransactionByAmount(transactions);
        transactionsByAmount.remove(contract.getAmount());

        List<Transaction> transactionsThatBelong = new ArrayList<>();

        for (Map.Entry<Double, List<Transaction>> entry : transactionsByAmount.entrySet()) {
            List<Transaction> existingTransactions = entry.getValue();
            transactionsThatBelong.addAll(existingTransactions);

            ContractHistory contractHistory;
            LocalDate earliestTransactionDate = getEarliestTransactionDate(existingTransactions);
            Double amount = entry.getKey();

            if (earliestTransactionDate.isBefore(contract.getStartDate())) {
                contract.setStartDate(earliestTransactionDate);

                contractHistory = new ContractHistory(contract, contract.getAmount(), amount, contract.getStartDate());
                contractHistories.add(contractHistory);
            } else {
                LocalDate latestTransactionDate = getLatestTransactionDate(existingTransactions);

                contractHistory = contractHistories.isEmpty()
                        ? handleAfterCurrentContractStartNoHistory(contract, amount, earliestTransactionDate, latestTransactionDate)
                        : handleAfterCurrentContractStartWithHistory(contract, contractHistories, amount, earliestTransactionDate, latestTransactionDate);
                contractHistories.add(contractHistory);
            }

            baseContractHistoryService.save(contractHistory);
        }

        return transactionsThatBelong;
    }

    private ContractHistory handleAfterCurrentContractStartNoHistory(Contract contract, Double amount, LocalDate changedAt, LocalDate lastPaymentDate) {
        updateContract(contract, amount, lastPaymentDate, changedAt);

        return new ContractHistory(contract, amount, contract.getAmount(), changedAt);
    }


    private ContractHistory handleAfterCurrentContractStartWithHistory(
            Contract contract, List<ContractHistory> contractHistories,
            Double amount, LocalDate changedAt, LocalDate lastPaymentDate) {

        ContractHistory earliestHistory = contractHistories.stream()
                .min(Comparator.comparing(ContractHistory::getChangedAt))
                .orElseThrow();
        ContractHistory latestHistory = contractHistories.stream()
                .max(Comparator.comparing(ContractHistory::getChangedAt))
                .orElseThrow();

        if (changedAt.isBefore(earliestHistory.getChangedAt()))
            // Adding history before the earliest one
            return new ContractHistory(contract, earliestHistory.getPreviousAmount(), amount, changedAt);

        if (changedAt.isAfter(latestHistory.getChangedAt()) || changedAt.isEqual(latestHistory.getChangedAt()))
            updateContract(contract, amount, lastPaymentDate, changedAt);

        return new ContractHistory(contract, amount, latestHistory.getNewAmount(), changedAt);
    }

    private void updateContract(Contract contract, Double amount, LocalDate lastPaymentDate, LocalDate changedAt) {
        contract.setAmount(amount);
        contract.setLastPaymentDate(lastPaymentDate);
        contract.setLastUpdatedAt(changedAt);
    }
    //</editor-fold>

    //<editor-fold desc="add to existing contract">
    private void matchTransactionsToExistingContract(List<Transaction> transactions, Contract contract, List<ContractHistory> contractHistories) {
        List<Transaction> candidateTransactions = getCandidatesForExistingContract(transactions, contract);

        if (candidateTransactions == null || candidateTransactions.isEmpty())
            return;

        // Partition transactions into matched and unmatched
        Map<Boolean, List<Transaction>> partitionedTransactions = matchTransactionToContractAmount(candidateTransactions, contract, contractHistories);

        List<Transaction> matchedTransactions = partitionedTransactions.get(true);
        List<Transaction> unmatchedTransactions = partitionedTransactions.get(false);

        if (!unmatchedTransactions.isEmpty()) {
            matchedTransactions.addAll(updateExistingContract(unmatchedTransactions, contract, contractHistories));
        }

        matchedTransactions.stream()
                .max(Comparator.comparing(Transaction::getDate))
                .ifPresent(transaction -> contract.setLastPaymentDate(transaction.getDate()));

        if (!matchedTransactions.isEmpty()) {
            baseTransactionService.setContract(contract, matchedTransactions);
        }
    }
    //</editor-fold>
}