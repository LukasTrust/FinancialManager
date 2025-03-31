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

    public void checkIfTransactionsBelongToContract(BankAccount bankAccount, List<Transaction> transactions) {
        List<Transaction> mutableTransactions = new ArrayList<>(transactions);

        mutableTransactions.sort(Comparator.comparing(Transaction::getDate));

        List<Contract> existingContract = baseContractService.findByBankAccount(bankAccount);
        List<ContractHistory> existingContractHistories = baseContractHistoryService.findByContractIn(existingContract);

        Map<Contract, List<ContractHistory>> contractHistoryMap = Utils.mapContractHistoryToContract(existingContract, existingContractHistories);
        matchTransactionsToExistingContract(mutableTransactions, contractHistoryMap);

        mutableTransactions = mutableTransactions.stream().filter(transaction -> transaction.getContract() == null).toList();

        createNewContracts(mutableTransactions);
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

    private Map<Boolean, List<Transaction>> matchTransactionToContractAmount(List<Transaction> transactions, Contract contract,
                                                                             List<ContractHistory> contractHistories) {

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
        Map<Integer, List<Transaction>> monthFrequencyMap = new HashMap<>();

        for (int monthsBetween = 1; monthsBetween <= 12; monthsBetween++) {
            List<Transaction> matchingTransactions = new ArrayList<>();

            for (int i = 1; i < transactions.size(); i++) {
                LocalDate previousDate = transactions.get(i - 1).getDate();
                LocalDate currentDate = transactions.get(i).getDate();

                if (seeIfDateIsInRange(previousDate, currentDate, monthsBetween)) {
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

    private boolean seeIfDateIsInRange(LocalDate previousDate, LocalDate currentDate, int monthsBetween) {
        int allowedDayOffset = 3;

        LocalDate minValidDate = previousDate.plusMonths(monthsBetween).minusDays(allowedDayOffset);
        LocalDate maxValidDate = previousDate.plusMonths(monthsBetween).plusDays(allowedDayOffset);

        return !currentDate.isBefore(minValidDate) && !currentDate.isAfter(maxValidDate);
    }

    private List<Transaction> getCandidatesForExistingContract(List<Transaction> transactions, Contract contract) {
        List<Transaction> transactionsWithSameCounterParty = new ArrayList<>(
                transactions.stream().filter(transaction -> transaction.getCounterParty().equals(contract.getCounterParty())).toList());

        if (transactionsWithSameCounterParty.isEmpty())
            return transactionsWithSameCounterParty;

        LocalDate startDate = contract.getStartDate();
        LocalDate lastPaymentDate = contract.getLastPaymentDate();
        int monthsBetween = contract.getMonthsBetweenPayments();

        Transaction firstTransaction = transactionsWithSameCounterParty.getFirst();
        Transaction lastTransaction = transactionsWithSameCounterParty.getLast();

        List<Transaction> matchingTransactions = new ArrayList<>();

        if (lastTransaction.getDate().isBefore(startDate)) {
            for(int current = transactionsWithSameCounterParty.size() - 1; current >= 0; current--) {
               Transaction currentTransaction = transactionsWithSameCounterParty.get(current);

               if (seeIfDateIsInRange(currentTransaction.getDate(), startDate, monthsBetween)) {
                   matchingTransactions.add(currentTransaction);
                   startDate = currentTransaction.getDate();
               }
            }
        }
        else if (firstTransaction.getDate().isAfter(lastPaymentDate)) {
            for (Transaction currentTransaction : transactionsWithSameCounterParty) {
                if (seeIfDateIsInRange(lastPaymentDate, currentTransaction.getDate(), monthsBetween)) {
                    matchingTransactions.add(currentTransaction);
                    lastPaymentDate = currentTransaction.getDate();
                }
            }
        }
        else {
            LocalDate currentDate = startDate;
            for (Transaction currentTransaction : transactionsWithSameCounterParty) {
                if (seeIfDateIsInRange(currentDate, currentTransaction.getDate(), monthsBetween)) {
                    matchingTransactions.add(currentTransaction);
                }
                currentDate = currentTransaction.getDate();
            }
        }

        return matchingTransactions;
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
                    processMainEntry(mainEntry.getValue(), subEntry.getValue());
            }
        }
    }

    private void processMainEntry(Map<Double, List<Transaction>> amountMap, List<Transaction> transactions) {
        Map<Integer, List<Transaction>> monthsBetweenMap = calculateMostFrequentMonthsBetween(transactions);

        for (Map.Entry<Integer, List<Transaction>> entry : monthsBetweenMap.entrySet()) {
            List<Transaction> monthTransactions = entry.getValue();
            int monthsBetween = entry.getKey();

            if (monthTransactions.size() < 3)
                continue;

            LocalDate firstDate = getEarliestTransactionDate(monthTransactions);
            List<Transaction> transactionsThatAlsoMatch = removeAndMergeMatchingSubEntries(amountMap, monthsBetween);

            finalizeContract(monthTransactions, transactionsThatAlsoMatch, firstDate, monthsBetween);
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

    private void finalizeContract(List<Transaction> transactions, List<Transaction> possibleTransactions, LocalDate firstDate, int monthsBetween) {
        LocalDate lastPaymentDate = getLatestTransactionDate(transactions);

        Transaction firstTransaction = transactions.getFirst();
        Contract contract = new Contract(
                firstDate, lastPaymentDate,
                monthsBetween,
                firstTransaction.getAmount(), firstTransaction.getCounterParty(), firstTransaction.getBankAccount()
        );

        baseContractService.save(contract);
        transactions.addAll(updateExistingContract(possibleTransactions, contract, new ArrayList<>()));
        baseContractService.saveAsync(contract);
        baseTransactionService.setContractAsync(contract, transactions, false);
    }

    //</editor-fold>

    //<editor-fold desc="update contract">
    private List<Transaction> updateExistingContract(List<Transaction> transactions, Contract contract,
                                                     List<ContractHistory> contractHistories) {
        Map<Double, List<Transaction>> transactionsByAmount = mapTransactionByAmount(transactions);
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

            baseContractHistoryService.saveAsync(contractHistory);
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

        if (candidateTransactions.isEmpty())
            return;

        // Partition transactions into matched and unmatched
        Map<Boolean, List<Transaction>> partitionedTransactions = matchTransactionToContractAmount(candidateTransactions, contract, contractHistories);

        List<Transaction> matchedTransactions = partitionedTransactions.get(true);
        List<Transaction> unmatchedTransactions = partitionedTransactions.get(false);

        if (matchedTransactions == null)
            matchedTransactions = new ArrayList<>();

        if (unmatchedTransactions != null)
            matchedTransactions.addAll(updateExistingContract(unmatchedTransactions, contract, contractHistories));

        matchedTransactions.stream()
                .max(Comparator.comparing(Transaction::getDate))
                .ifPresent(transaction -> contract.setLastPaymentDate(transaction.getDate()));

        if (!matchedTransactions.isEmpty()) {
            baseTransactionService.setContractAsync(contract, matchedTransactions, false);
        }
    }
    //</editor-fold>
}