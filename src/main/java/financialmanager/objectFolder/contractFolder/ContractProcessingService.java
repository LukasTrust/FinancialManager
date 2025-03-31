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

    /**
     * Processes and associates transactions with existing contracts if applicable.
     * If no matching contract is found, new contracts are created.
     *
     * @param bankAccount The bank account related to the transactions.
     * @param transactions The list of transactions to be processed.
     */
    public void processAndAssociateTransactions(BankAccount bankAccount, List<Transaction> transactions) {
        log.info("Starting transaction processing for bank account: {}", bankAccount.getId());

        // Create a mutable list and sort transactions by date
        List<Transaction> mutableTransactions = new ArrayList<>(transactions);
        mutableTransactions.sort(Comparator.comparing(Transaction::getDate));

        // Retrieve existing contracts and contract histories for the bank account
        List<Contract> existingContracts = baseContractService.findByBankAccount(bankAccount);
        List<ContractHistory> existingContractHistories = baseContractHistoryService.findByContractIn(existingContracts);

        log.info("Fetched {} existing contracts and {} contract histories.",
                existingContracts.size(), existingContractHistories.size());

        // Map contract histories to corresponding contracts
        Map<Contract, List<ContractHistory>> contractHistoryMap = Utils.mapContractHistoryToContract(existingContracts, existingContractHistories);

        // Attempt to match transactions with existing contracts
        associateTransactionsWithContracts(mutableTransactions, contractHistoryMap);

        // Filter out transactions that were not matched to any contract
        List<Transaction> unmatchedTransactions = mutableTransactions.stream()
                .filter(transaction -> transaction.getContract() == null)
                .toList();

        log.info("{} transactions could not be matched to an existing contract and will be used to create new contracts.",
                unmatchedTransactions.size());

        generateNewContracts(unmatchedTransactions);
    }

    //<editor-fold desc="help methods">

    /**
     * Groups transactions by their amount and filters out groups with fewer than 3 transactions.
     *
     * @param transactions The list of transactions to be grouped.
     * @return A map where keys are transaction amounts, and values are lists of transactions with that amount.
     */
    private Map<Double, List<Transaction>> groupTransactionsByAmount(List<Transaction> transactions) {
        log.info("Grouping {} transactions by amount.", transactions.size());

        Map<Double, List<Transaction>> groupedTransactions = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getAmount))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 2) // Keep only amounts with more than 2 transactions
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("Grouped transactions resulted in {} unique amounts with more than 2 transactions.", groupedTransactions.size());

        return groupedTransactions;
    }

    /**
     * Groups transactions by counterparty, then by transaction amount, filtering out amounts with fewer than 3 transactions.
     *
     * @param transactions The list of transactions to be grouped.
     * @return A nested map where keys are counterparties, and values are maps of transaction amounts to lists of transactions.
     */
    private Map<CounterParty, Map<Double, List<Transaction>>> groupTransactionsByCounterPartyAndAmount(List<Transaction> transactions) {
        log.info("Grouping {} transactions by counterparty and amount.", transactions.size());

        Map<CounterParty, Map<Double, List<Transaction>>> groupedTransactions = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCounterParty))
                .entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), groupTransactionsByAmount(entry.getValue())))
                .filter(entry -> !entry.getValue().isEmpty()) // Remove counterparties with no valid transactions
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.info("Grouped transactions resulted in {} counterparties with valid transactions.", groupedTransactions.size());

        return groupedTransactions;
    }

    /**
     * Matches transactions to a contract based on the contract amount or historical amounts.
     *
     * @param transactions      The list of transactions to match.
     * @param contract          The contract to match transactions against.
     * @param contractHistories The list of contract history records.
     * @return A map where 'true' contains matched transactions and 'false' contains unmatched ones.
     */
    private Map<Boolean, List<Transaction>> matchTransactionsToContractAmount(List<Transaction> transactions,
                                                                              Contract contract,
                                                                              List<ContractHistory> contractHistories) {
        log.info("Matching {} transactions to contract ID: {}", transactions.size(), contract.getId());

        Map<Boolean, List<Transaction>> matchedTransactions = new HashMap<>();

        for (Transaction transaction : transactions) {
            boolean isMatching = transaction.getAmount().equals(contract.getAmount()) ||
                    contractHistories.stream()
                            .anyMatch(ch -> ch.getPreviousAmount().equals(transaction.getAmount()));

            matchedTransactions.computeIfAbsent(isMatching, k -> new ArrayList<>()).add(transaction);
        }

        log.info("Matching complete: {} transactions matched, {} transactions unmatched.",
                matchedTransactions.getOrDefault(true, Collections.emptyList()).size(),
                matchedTransactions.getOrDefault(false, Collections.emptyList()).size());

        return matchedTransactions;
    }

    /**
     * Calculates the most frequent transaction intervals (in months) and groups transactions accordingly.
     *
     * @param transactions The list of transactions to analyze.
     * @return A map where keys are month intervals (1-12), and values are lists of transactions within that interval.
     */
    private Map<Integer, List<Transaction>> findMostFrequentTransactionIntervals(List<Transaction> transactions) {
        log.info("Analyzing transaction frequency for {} transactions.", transactions.size());

        Map<Integer, List<Transaction>> monthFrequencyMap = new HashMap<>();

        for (int monthsBetween = 1; monthsBetween <= 12; monthsBetween++) {
            List<Transaction> matchingTransactions = new ArrayList<>();

            for (int i = 1; i < transactions.size(); i++) {
                LocalDate previousDate = transactions.get(i - 1).getDate();
                LocalDate currentDate = transactions.get(i).getDate();

                if (isDateWithinInterval(previousDate, currentDate, monthsBetween)) {
                    matchingTransactions.add(transactions.get(i - 1));
                    matchingTransactions.add(transactions.get(i));
                }
            }

            if (matchingTransactions.size() > 2) {
                transactions.removeAll(matchingTransactions);
                monthFrequencyMap.put(monthsBetween, new ArrayList<>(new LinkedHashSet<>(matchingTransactions)));
            }
        }

        log.info("Transaction frequency analysis complete: {} intervals identified.", monthFrequencyMap.size());

        return monthFrequencyMap;
    }

    /**
     * Checks if the difference between two dates falls within an allowed range of a given month interval.
     *
     * @param previousDate The earlier date.
     * @param currentDate  The later date.
     * @param monthsBetween The expected interval in months.
     * @return True if the current date falls within the allowed range of the expected interval.
     */
    private boolean isDateWithinInterval(LocalDate previousDate, LocalDate currentDate, int monthsBetween) {
        int allowedDayOffset = 3;

        LocalDate minValidDate = previousDate.plusMonths(monthsBetween).minusDays(allowedDayOffset);
        LocalDate maxValidDate = previousDate.plusMonths(monthsBetween).plusDays(allowedDayOffset);

        return !currentDate.isBefore(minValidDate) && !currentDate.isAfter(maxValidDate);
    }

    /**
     * Identifies candidate transactions that could be linked to an existing contract.
     *
     * @param transactions The list of transactions to analyze.
     * @param contract The contract to match transactions against.
     * @return A list of transactions that may belong to the contract.
     */
    private List<Transaction> findCandidateTransactionsForContract(List<Transaction> transactions, Contract contract) {
        log.info("Finding candidate transactions for contract ID: {}", contract.getId());

        List<Transaction> transactionsWithSameCounterParty = transactions.stream()
                .filter(transaction -> transaction.getCounterParty().equals(contract.getCounterParty()))
                .toList();

        if (transactionsWithSameCounterParty.isEmpty()) {
            log.info("No transactions found for the same counterparty as contract ID: {}", contract.getId());
            return Collections.emptyList();
        }

        LocalDate startDate = contract.getStartDate();
        LocalDate lastPaymentDate = contract.getLastPaymentDate();
        int monthsBetween = contract.getMonthsBetweenPayments();

        Transaction firstTransaction = transactionsWithSameCounterParty.getFirst();
        Transaction lastTransaction = transactionsWithSameCounterParty.getLast();

        List<Transaction> matchingTransactions = new ArrayList<>();

        if (lastTransaction.getDate().isBefore(startDate)) {
            log.info("Processing transactions in reverse before contract start date: {}", startDate);
            processTransactionsInReverse(transactionsWithSameCounterParty, matchingTransactions, startDate, monthsBetween);
        } else if (firstTransaction.getDate().isAfter(lastPaymentDate)) {
            log.info("Processing transactions forward after contract last payment date: {}", lastPaymentDate);
            processTransactionsForward(transactionsWithSameCounterParty, matchingTransactions, lastPaymentDate, monthsBetween);
        } else {
            log.info("Processing transactions within contract date range.");
            processTransactionsWithinRange(transactionsWithSameCounterParty, matchingTransactions, startDate, monthsBetween);
        }

        log.info("Found {} matching transactions for contract ID: {}", matchingTransactions.size(), contract.getId());
        return matchingTransactions;
    }

    /**
     * Processes transactions in reverse order to find matches before the contract's start date.
     *
     * @param transactions The list of transactions sorted by date.
     * @param result The list where matching transactions will be stored.
     * @param startDate The contract's start date.
     * @param monthsBetween The expected months between payments.
     */
    private void processTransactionsInReverse(List<Transaction> transactions, List<Transaction> result, LocalDate startDate, int monthsBetween) {
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction transaction = transactions.get(i);
            if (isDateWithinInterval(transaction.getDate(), startDate, monthsBetween)) {
                result.add(transaction);
                startDate = transaction.getDate();
            }
        }
    }

    /**
     * Processes transactions in forward order to find matches after the last recorded payment.
     *
     * @param transactions The list of transactions sorted by date.
     * @param result The list where matching transactions will be stored.
     * @param lastPaymentDate The last payment date in the contract.
     * @param monthsBetween The expected months between payments.
     */
    private void processTransactionsForward(List<Transaction> transactions, List<Transaction> result, LocalDate lastPaymentDate, int monthsBetween) {
        for (Transaction transaction : transactions) {
            if (isDateWithinInterval(lastPaymentDate, transaction.getDate(), monthsBetween)) {
                result.add(transaction);
                lastPaymentDate = transaction.getDate();
            }
        }
    }

    /**
     * Processes transactions within a specified date range, ensuring they match expected payment intervals.
     *
     * @param transactions The list of transactions to analyze.
     * @param matchedTransactions The list where matching transactions will be stored.
     * @param startDate The starting date of the contract.
     * @param monthsBetween The expected months between payments.
     */
    private void processTransactionsWithinRange(List<Transaction> transactions, List<Transaction> matchedTransactions, LocalDate startDate, int monthsBetween) {
        log.info("Filtering transactions within range from start date: {}", startDate);

        LocalDate currentDate = startDate;
        for (Transaction transaction : transactions) {
            if (isDateWithinInterval(currentDate, transaction.getDate(), monthsBetween)) {
                matchedTransactions.add(transaction);
            }
            currentDate = transaction.getDate();
        }

        log.info("Filtered {} transactions within range.", matchedTransactions.size());
    }

    /**
     * Retrieves the earliest transaction date from a list.
     *
     * @param transactions The list of transactions.
     * @return The earliest transaction date, or today's date if the list is empty.
     */
    private LocalDate findEarliestTransactionDate(List<Transaction> transactions) {
        return transactions.stream()
                .min(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate)
                .orElse(LocalDate.now());
    }

    /**
     * Retrieves the latest transaction date from a list.
     *
     * @param transactions The list of transactions.
     * @return The latest transaction date, or today's date if the list is empty.
     */
    private LocalDate findLatestTransactionDate(List<Transaction> transactions) {
        return transactions.stream()
                .max(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate)
                .orElse(LocalDate.now());
    }

    /**
     * Updates contract details including amount, last payment date, and last updated timestamp.
     *
     * @param contract The contract to update.
     * @param amount The new amount.
     * @param lastPaymentDate The last payment date for the contract.
     * @param changedAt The timestamp when the update was made.
     */
    private void updateContractDetails(Contract contract, Double amount, LocalDate lastPaymentDate, LocalDate changedAt) {
        log.info("Updating contract ID: {} with new amount: {}, last payment date: {}, updated at: {}",
                contract.getId(), amount, lastPaymentDate, changedAt);

        contract.setAmount(amount);
        contract.setLastPaymentDate(lastPaymentDate);
        contract.setLastUpdatedAt(changedAt);

        log.info("Contract ID: {} updated successfully.", contract.getId());
    }

    //</editor-fold>

    //<editor-fold desc="create contract">
    /**
     * Creates new contracts based on transaction data grouped by CounterParty and amount.
     *
     * @param transactions The list of transactions to process.
     */
    private void generateNewContracts(List<Transaction> transactions) {
        log.info("Starting contract creation process for {} transactions.", transactions.size());

        Map<CounterParty, Map<Double, List<Transaction>>> groupedTransactions = groupTransactionsByCounterPartyAndAmount(transactions);

        for (Map.Entry<CounterParty, Map<Double, List<Transaction>>> counterPartyEntry : groupedTransactions.entrySet()) {
            for (Map.Entry<Double, List<Transaction>> amountEntry : counterPartyEntry.getValue().entrySet()) {
                if (!amountEntry.getValue().isEmpty()) {
                    processTransactionGroups(counterPartyEntry.getValue(), amountEntry.getValue());
                }
            }
        }

        log.info("Completed contract creation process.");
    }

    /**
     * Processes grouped transactions by analyzing intervals and merging related transactions.
     *
     * @param transactionsByAmount The map containing transactions grouped by amount.
     * @param transactions The list of transactions to process.
     */
    private void processTransactionGroups(Map<Double, List<Transaction>> transactionsByAmount, List<Transaction> transactions) {
        log.info("Processing transaction groups for {} transactions.", transactions.size());

        Map<Integer, List<Transaction>> transactionIntervals = findMostFrequentTransactionIntervals(transactions);

        for (Map.Entry<Integer, List<Transaction>> intervalEntry : transactionIntervals.entrySet()) {
            List<Transaction> groupedTransactions = intervalEntry.getValue();
            int monthsBetween = intervalEntry.getKey();

            LocalDate earliestDate = findEarliestTransactionDate(groupedTransactions);
            List<Transaction> additionalMatchingTransactions = mergeMatchingTransactions(transactionsByAmount, monthsBetween);

            finalizeAndSaveContract(groupedTransactions, additionalMatchingTransactions, earliestDate, monthsBetween);
        }
    }

    /**
     * Merges matching transactions based on interval frequency.
     *
     * @param transactionsByAmount The map containing transactions grouped by amount.
     * @param monthsBetween The expected interval in months.
     * @return A list of transactions that match the given interval.
     */
    private List<Transaction> mergeMatchingTransactions(Map<Double, List<Transaction>> transactionsByAmount, int monthsBetween) {
        log.info("Merging transactions matching {}-month intervals.", monthsBetween);

        List<Transaction> matchedTransactions = new ArrayList<>();

        for (Map.Entry<Double, List<Transaction>> amountEntry : transactionsByAmount.entrySet()) {
            List<Transaction> transactionList = amountEntry.getValue();
            if (transactionList.isEmpty()) continue;

            Map<Integer, List<Transaction>> transactionIntervals = findMostFrequentTransactionIntervals(transactionList);
            List<Transaction> currentMatches = transactionIntervals.get(monthsBetween);

            if (currentMatches != null) {
                matchedTransactions.addAll(currentMatches);
            }
        }

        log.info("Merged {} transactions for {}-month interval.", matchedTransactions.size(), monthsBetween);
        return matchedTransactions;
    }

    /**
     * Finalizes and saves a new contract based on matched transactions.
     *
     * @param transactions         Transactions associated with the new contract.
     * @param potentialTransactions Additional transactions that might belong to the contract.
     * @param contractStartDate    The start date of the contract.
     * @param paymentIntervalMonths The payment interval in months.
     */
    private void finalizeAndSaveContract(List<Transaction> transactions, List<Transaction> potentialTransactions,
                                         LocalDate contractStartDate, int paymentIntervalMonths) {
        LocalDate lastPaymentDate = findLatestTransactionDate(transactions);
        Transaction firstTransaction = transactions.getFirst();

        Contract newContract = new Contract(
                contractStartDate, lastPaymentDate, paymentIntervalMonths,
                firstTransaction.getAmount(), firstTransaction.getCounterParty(), firstTransaction.getBankAccount()
        );

        log.info("Creating new contract for CounterParty: {}, Amount: {}, Interval: {} months",
                firstTransaction.getCounterParty(), firstTransaction.getAmount(), paymentIntervalMonths);

        baseContractService.save(newContract);

        // Update contract with potential transactions
        List<Transaction> updatedTransactions = updateExistingContract(potentialTransactions, newContract, new ArrayList<>());
        transactions.addAll(updatedTransactions);

        baseContractService.saveAsync(newContract);
        baseTransactionService.setContractAsync(newContract, transactions, false);

        log.info("Contract finalized and saved successfully.");
    }

    //</editor-fold>

    //<editor-fold desc="update contract">
    /**
     * Updates an existing contract with transactions that match its criteria.
     *
     * @param transactions     List of candidate transactions.
     * @param contract         The contract being updated.
     * @param contractHistories List to store contract history changes.
     * @return List of transactions that belong to this contract.
     */
    private List<Transaction> updateExistingContract(List<Transaction> transactions, Contract contract,
                                                     List<ContractHistory> contractHistories) {
        Map<Double, List<Transaction>> transactionsByAmount = groupTransactionsByAmount(transactions);
        List<Transaction> matchingTransactions = new ArrayList<>();

        for (Map.Entry<Double, List<Transaction>> entry : transactionsByAmount.entrySet()) {
            List<Transaction> transactionList = entry.getValue();
            Double amount = entry.getKey();
            matchingTransactions.addAll(transactionList);

            LocalDate earliestTransactionDate = findEarliestTransactionDate(transactionList);
            ContractHistory contractHistory;

            if (earliestTransactionDate.isBefore(contract.getStartDate())) {
                log.info("Updating contract start date from {} to {}", contract.getStartDate(), earliestTransactionDate);
                contract.setStartDate(earliestTransactionDate);

                contractHistory = new ContractHistory(contract, contract.getAmount(), amount, earliestTransactionDate);
                contractHistories.add(contractHistory);
            } else {
                LocalDate latestTransactionDate = findLatestTransactionDate(transactionList);

                contractHistory = contractHistories.isEmpty()
                        ? handleNewContractWithoutHistory(contract, amount, earliestTransactionDate, latestTransactionDate)
                        : handleContractWithExistingHistory(contract, contractHistories, amount, earliestTransactionDate, latestTransactionDate);

                contractHistories.add(contractHistory);
            }

            baseContractHistoryService.saveAsync(contractHistory);
        }

        return matchingTransactions;
    }

    /**
     * Handles contract updates when no prior history exists.
     */
    private ContractHistory handleNewContractWithoutHistory(Contract contract, Double amount,
                                                            LocalDate changedAt, LocalDate lastPaymentDate) {
        updateContractDetails(contract, amount, lastPaymentDate, changedAt);
        return new ContractHistory(contract, amount, contract.getAmount(), changedAt);
    }

    /**
     * Handles contract updates when historical records already exist.
     */
    private ContractHistory handleContractWithExistingHistory(Contract contract, List<ContractHistory> contractHistories,
                                                              Double amount, LocalDate changedAt, LocalDate lastPaymentDate) {

        ContractHistory earliestHistory = contractHistories.stream()
                .min(Comparator.comparing(ContractHistory::getChangedAt))
                .orElseThrow();
        ContractHistory latestHistory = contractHistories.stream()
                .max(Comparator.comparing(ContractHistory::getChangedAt))
                .orElseThrow();

        if (changedAt.isBefore(earliestHistory.getChangedAt())) {
            log.info("Adding history record before existing contract history.");
            return new ContractHistory(contract, earliestHistory.getPreviousAmount(), amount, changedAt);
        }

        updateContractDetails(contract, amount, lastPaymentDate, changedAt);
        return new ContractHistory(contract, amount, latestHistory.getNewAmount(), changedAt);
    }
    //</editor-fold>

    //<editor-fold desc="add to existing contract">
    /**
     * Associates transactions with existing contracts based on contract history.
     *
     * @param transactions       The list of transactions to be processed.
     * @param contractHistoryMap A map containing contracts and their associated histories.
     */
    private void associateTransactionsWithContracts(List<Transaction> transactions,
                                                    Map<Contract, List<ContractHistory>> contractHistoryMap) {
        for (Map.Entry<Contract, List<ContractHistory>> entry : contractHistoryMap.entrySet()) {
            Contract contract = entry.getKey();

            List<ContractHistory> contractHistories = new ArrayList<>(entry.getValue());

            log.info("Processing contract ID: {} with {} history records.",
                    contract.getId(), contractHistories.size());

            // Match transactions to the contract using its history
            matchTransactionsToExistingContract(transactions, contract, contractHistories);

            // Update the entry with the modified contract history
            entry.setValue(contractHistories);
        }

        log.info("Transaction association with contracts completed.");
    }

    /**
     * Matches a list of transactions to an existing contract and updates it accordingly.
     *
     * @param transactions      The list of available transactions.
     * @param contract          The existing contract to match transactions against.
     * @param contractHistories The contract's history for tracking changes.
     */
    private void matchTransactionsToExistingContract(List<Transaction> transactions, Contract contract,
                                                     List<ContractHistory> contractHistories) {
        List<Transaction> candidateTransactions = findCandidateTransactionsForContract(transactions, contract);

        if (candidateTransactions.isEmpty()) {
            log.info("No candidate transactions found for contract with CounterParty: {}", contract.getCounterParty());
            return;
        }

        // Partition transactions into matched and unmatched based on amount comparison
        Map<Boolean, List<Transaction>> partitionedTransactions = matchTransactionsToContractAmount(candidateTransactions, contract, contractHistories);

        List<Transaction> matchedTransactions = partitionedTransactions.getOrDefault(true, new ArrayList<>());
        List<Transaction> unmatchedTransactions = partitionedTransactions.getOrDefault(false, new ArrayList<>());

        if (!unmatchedTransactions.isEmpty()) {
            log.info("Updating contract {} with {} unmatched transactions.", contract.getId(), unmatchedTransactions.size());
            matchedTransactions.addAll(updateExistingContract(unmatchedTransactions, contract, contractHistories));
        }

        // Update contract dates based on the transactions
        updateContractDates(contract, matchedTransactions);

        if (!matchedTransactions.isEmpty()) {
            baseTransactionService.setContractAsync(contract, matchedTransactions, false);
            log.info("Successfully matched {} transactions to contract {}.", matchedTransactions.size(), contract.getId());
        }
    }

    /**
     * Updates contract start and last payment dates based on provided transactions.
     *
     * @param contract     The contract to update.
     * @param transactions The transactions affecting the contract.
     */
    private void updateContractDates(Contract contract, List<Transaction> transactions) {
        if (transactions.isEmpty()) return;

        LocalDate earliestDate = findEarliestTransactionDate(transactions);
        LocalDate latestDate = findLatestTransactionDate(transactions);

        if (earliestDate.isBefore(contract.getStartDate())) {
            log.info("Updating contract start date from {} to {}", contract.getStartDate(), earliestDate);
            contract.setStartDate(earliestDate);
        }

        if (latestDate.isAfter(contract.getLastPaymentDate())) {
            log.info("Updating contract last payment date from {} to {}", contract.getLastPaymentDate(), latestDate);
            contract.setLastPaymentDate(latestDate);
        }
    }
    //</editor-fold>
}