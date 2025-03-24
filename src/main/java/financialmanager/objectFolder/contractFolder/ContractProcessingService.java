package financialmanager.objectFolder.contractFolder;

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
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ContractProcessingService {

    private final BaseContractService baseContractService;
    private final BaseContractHistoryService baseContractHistoryService;
    private final BaseTransactionService baseTransactionService;
    private final ContractService contractService;

    private static final Logger log = LoggerFactory.getLogger(ContractProcessingService.class);

    public void checkIfTransactionsBelongToContract(BankAccount bankAccount, List<Transaction> transactionsWithoutContracts) {
        List<Contract> existingContract = baseContractService.findByBankAccount(bankAccount);
        List<ContractHistory> existingContractHistories = baseContractHistoryService.findByContractIn(existingContract);

        Map<Contract, List<ContractHistory>> contractHistoryMap = mapContractHistoryToContract(existingContract, existingContractHistories);
        transactionsWithoutContracts = matchTransactionsToExistingContract(transactionsWithoutContracts, contractHistoryMap);

        existingContractHistories = baseContractHistoryService.findByContractIn(existingContract);
        contractHistoryMap = mapContractHistoryToContract(existingContract, existingContractHistories);

        createNewContracts(transactionsWithoutContracts, contractHistoryMap);
    }

    //<editor-fold desc="help methods">
    private Map<Contract, List<ContractHistory>> mapContractHistoryToContract(List<Contract> contracts, List<ContractHistory> contractHistories) {
        Map<Contract, List<ContractHistory>> contractHistoryMap = new HashMap<>();

        for (Contract contract : contracts) {
            List<ContractHistory> contractHistoriesOfContract = contractHistories.stream().filter(contractHistory ->
                    contractHistory.getContract() == contract).toList();

            contractHistoryMap.put(contract, contractHistoriesOfContract);
        }

        return contractHistoryMap;
    }

    private List<Transaction> filterTransactionsThatBelongToExistingContract(List<Transaction> transactions, Contract contract) {
        return transactions.stream()
                .filter(transaction -> doesTransactionBelongToContract(transaction, contract))
                .sorted(Comparator.comparing(Transaction::getDate))
                .toList();
    }

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

    private LocalDate getEarliestTransactionDate(List<Transaction> transactions) {
        return transactions.stream().min(Comparator.comparing(Transaction::getDate)).map(Transaction::getDate).orElse(LocalDate.now());
    }

    private LocalDate getLatestTransactionDate(List<Transaction> transactions) {
        return transactions.stream().max(Comparator.comparing(Transaction::getDate)).map(Transaction::getDate).orElse(LocalDate.now());
    }

    //<editor-fold desc="transaction compare methods">
    private boolean doesTransactionBelongToContract(Transaction transaction, Contract contract) {
        return hasSameCounterParty(transaction, contract.getCounterParty()) &&
                hasValidTransactionDate(transaction, contract.getStartDate(), contract.getMonthsBetweenPayments());
    }

    private boolean hasSameCounterParty(Transaction transaction, CounterParty counterParty) {
        return counterParty.equals(transaction.getCounterParty());
    }

    private boolean hasSameAmount(Transaction transaction, Double amount) {
        return transaction.getAmount().compareTo(amount) == 0;
    }

    private boolean hasSameDateDay(LocalDate transactionDate, LocalDate date) {
        return transactionDate.isEqual(date) ||
                transactionDate.isEqual(date.minusDays(1)) ||
                transactionDate.isEqual(date.plusDays(1));
    }

    private boolean hasSameDateDifference(LocalDate transactionDate, LocalDate date, int monthsBetweenPayments) {
        int monthsElapsed = (transactionDate.getYear() - date.getYear()) * 12 +
                (transactionDate.getMonthValue() - date.getMonthValue());

        return monthsElapsed % monthsBetweenPayments == 0;
    }

    private int calculateMonthsDifference(LocalDate startDate, LocalDate endDate) {
        int monthsBetween = (endDate.getYear() - startDate.getYear()) * 12 + (endDate.getMonthValue() - startDate.getMonthValue());
        return Math.abs(monthsBetween);
    }

    private boolean hasValidTransactionDate(Transaction transaction, LocalDate date, int monthsBetweenPayments) {
        LocalDate transactionDate = transaction.getDate();

        // Check if transaction date is within ±1 day of the start date
        // Validate if the month difference is a multiple of `monthsBetween`
        return hasSameDateDay(transactionDate, date) && hasSameDateDifference(transactionDate, date, monthsBetweenPayments);
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="create contract">
    private void createNewContracts(List<Transaction> transactions) {
        Map<CounterParty, Map<Double, List<Transaction>>> counterPartyMapMap = mapTransactionByAmountAndCounterParty(transactions);

        for (Map.Entry<CounterParty, Map<Double, List<Transaction>>> mainEntry : counterPartyMapMap.entrySet()) {
            CounterParty counterParty = mainEntry.getKey();
            
        }
    }

    private List<Contract> handleAmountMap(Map<Double, List<Transaction>> amountMap) {

    }

    //</editor-fold>

    //<editor-fold desc="update contract">
    private List<Transaction> updateExistingContract(List<Transaction> transactions, Contract contract,
                                                     List<ContractHistory> contractHistories) {
        Map<Double, List<Transaction>> transactionsByAmount = mapTransactionByAmount(transactions);

        for (Map.Entry<Double, List<Transaction>> entry : transactionsByAmount.entrySet()) {
            List<Transaction> existingTransactions = entry.getValue();

            ContractHistory contractHistory = null;
            LocalDate earliestTransactionDate = getEarliestTransactionDate(existingTransactions);
            Double amount = entry.getKey();

            if (earliestTransactionDate.isBefore(contract.getStartDate())) {
                contractHistory = handleBeforeCurrentContractStart(contract, amount, earliestTransactionDate);
                contractHistories.add(contractHistory);
            } else {
                LocalDate latestTransactionDate = getLatestTransactionDate(existingTransactions);

                contractHistory = contractHistories.isEmpty()
                        ? handleAfterCurrentContractStartNoHistory(contract, amount, earliestTransactionDate, latestTransactionDate)
                        : handleAfterCurrentContractStartWithHistory(contract, contractHistories, amount, earliestTransactionDate, latestTransactionDate);
                contractHistories.add(contractHistory);
            }

            if (contractHistory != null) {
                baseContractHistoryService.save(contractHistory);
            }
        }

        return transactions.stream().filter(transaction -> transaction.getContract() != null).toList();
    }

    private ContractHistory handleAfterCurrentContractStartNoHistory(Contract contract, Double amount, LocalDate changedAt, LocalDate lastPaymentDate) {
        updateContract(contract, amount, lastPaymentDate, changedAt);

        return new ContractHistory(contract, amount, contract.getAmount(), changedAt);
    }

    private ContractHistory handleBeforeCurrentContractStart(Contract contract, Double previousAmount, LocalDate newStartDate) {
        contract.setStartDate(newStartDate);
        baseContractService.save(contract);

        return new ContractHistory(contract, contract.getAmount(), previousAmount, contract.getStartDate());
    }

    private ContractHistory handleAfterCurrentContractStartWithHistory(
            Contract contract, List<ContractHistory> contractHistories,
            Double amount, LocalDate changedAt, LocalDate lastPaymentDate) {

        if (contractHistories.isEmpty()) {
            log.error("Contract history of contract {} is empty", contract);
            return null;
        }

        ContractHistory earliestHistory = contractHistories.stream()
                .min(Comparator.comparing(ContractHistory::getChangedAt))
                .orElseThrow();
        ContractHistory latestHistory = contractHistories.stream()
                .max(Comparator.comparing(ContractHistory::getChangedAt))
                .orElseThrow();

        if (changedAt.isBefore(earliestHistory.getChangedAt())) {
            // Adding history before the earliest one
            return new ContractHistory(contract, earliestHistory.getPreviousAmount(), amount, changedAt);
        }

        if (changedAt.isAfter(latestHistory.getChangedAt()) || changedAt.isEqual(latestHistory.getChangedAt())) {
            updateContract(contract, amount, lastPaymentDate, changedAt);
        }

        return new ContractHistory(contract, amount, latestHistory.getNewAmount(), changedAt);
    }

    private void updateContract(Contract contract, Double amount, LocalDate lastPaymentDate, LocalDate changedAt) {
        contract.setAmount(amount);
        contract.setLastPaymentDate(lastPaymentDate);
        contract.setLastUpdatedAt(changedAt);
        baseContractService.save(contract);
    }
    //</editor-fold>

    //<editor-fold desc="add to existing contract">
    private List<Transaction> matchTransactionsToExistingContract
    (List<Transaction> transactions, Map<Contract, List<ContractHistory>> contractHistoryMap) {
        for (Map.Entry<Contract, List<ContractHistory>> entry : contractHistoryMap.entrySet()) {
            filterTransactionsThatBelongToTheContract(transactions, entry.getKey(), entry.getValue());
        }

        return transactions.stream()
                .filter(transaction -> transaction.getContract() == null)
                .toList();
    }

    private void filterTransactionsThatBelongToTheContract(List<Transaction> transactions, Contract contract, List<ContractHistory> contractHistories) {
        List<Transaction> candidateTransactions = filterTransactionsThatBelongToExistingContract(transactions, contract);

        if (transactions.isEmpty()) {
            log.error("Transaction list of contract {} is empty", contract);
            return;
        }

        List<Transaction> matchedTransactions = new ArrayList<>();
        List<Transaction> unmatchedTransactions = new ArrayList<>();

        Transaction lastTransaction = transactions.stream().max(Comparator.comparing(Transaction::getDate)).get();

        for (Transaction transaction : candidateTransactions) {
            if (findMatchingTransactions(transaction, contract, contractHistories, matchedTransactions)) {
                lastTransaction = lastTransaction.getDate().isBefore(transaction.getDate()) ? transaction : lastTransaction;
            } else {
                unmatchedTransactions.add(transaction);
            }
        }

        if (!unmatchedTransactions.isEmpty()) {
            matchedTransactions.addAll(updateExistingContract(unmatchedTransactions, contract, contractHistories));
        }

        contract.setLastPaymentDate(lastTransaction.getDate());
        baseTransactionService.setContract(contract, matchedTransactions);
    }

    private boolean findMatchingTransactions(Transaction transaction, Contract contract, List<ContractHistory> contractHistories, List<Transaction> matchedTransactions) {
        if (hasSameAmount(transaction, contract.getAmount())) {
            matchedTransactions.add(transaction);
            return true;
        }
        for (ContractHistory contractHistory : contractHistories) {
            if (hasSameAmount(transaction, contractHistory.getPreviousAmount())) {
                matchedTransactions.add(transaction);
                return true;
            }
        }
        return false;
    }
    //</editor-fold>
}