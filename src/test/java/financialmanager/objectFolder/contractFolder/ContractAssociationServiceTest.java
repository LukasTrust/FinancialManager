package financialmanager.objectFolder.contractFolder;

import dataCreator.DataCreator;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.BaseContractHistoryService;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionRepository;
import financialmanager.objectFolder.usersFolder.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ContractAssociationServiceTest {

    private final String COUNTER_PARTY_NAME = "Contract Counter Party";

    private ContractAssociationService contractAssociationService;
    private BaseContractService baseContractService;
    private BaseContractHistoryService baseContractHistoryService;

    private BankAccount bankAccount;
    private CounterParty counterPartyForContract;
    private DataCreator dataCreator;

    @BeforeEach
    void setup() {
        Users users = mock(Users.class);

        dataCreator = new DataCreator(COUNTER_PARTY_NAME, users);

        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        baseContractService = mock(BaseContractService.class);
        baseContractHistoryService = mock(BaseContractHistoryService.class);
        BaseTransactionService baseTransactionService = new BaseTransactionService(transactionRepository);

        bankAccount = mock(BankAccount.class);
        counterPartyForContract = dataCreator.createCounterParty(users, COUNTER_PARTY_NAME);

        contractAssociationService = new ContractAssociationService(baseContractService, baseContractHistoryService, baseTransactionService);
    }

    @AfterEach
    void tearDown() {
        verify(baseContractService, atMost(1)).findByBankAccount(any());
        verify(baseContractHistoryService, atMost(1)).findByContractIn(anyList());
    }

    //<editor-fold desc="help assert methods">
    private void assertTransactionContract(Transaction transaction, CounterParty expectedCounterParty, double expectedAmount) {
        assertTransactionContract(transaction, expectedCounterParty, expectedAmount, 1);
    }

    private void assertTransactionContract(Transaction transaction, CounterParty expectedCounterParty, double expectedAmount, int monthsBetween) {
        Contract contract = transaction.getContract();

        assertNotNull(contract);
        assertEquals(expectedCounterParty, contract.getCounterParty());
        assertEquals(expectedCounterParty.getName(), contract.getName());
        assertEquals(expectedAmount, contract.getAmount());
        assertEquals(monthsBetween, contract.getMonthsBetweenPayments());
    }

    private void verifySaveCalls(int countContract, int countHistory) {
        verify(baseContractService, atMost(countContract)).saveAsync(any());
        verify(baseContractHistoryService, atMost(countHistory)).saveAsync(any());
    }

    //</editor-fold>

    //<editor-fold desc="no existing contracts tests">
    @Test
    void processAndAssociateTransactions_noExistingContracts_oneTransaction() {
        List<Transaction> transactions = dataCreator.createTransactionsForContract(new CounterParty(), 50.0, 1, 0);
        Transaction transaction = transactions.getFirst();

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_noContractToFind() {
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactionsWithoutContract);

        verifySaveCalls(0, 0);
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 9",
            "90.0, 5"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFind(double startAmount, int count) {
        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, startAmount, count, 0);

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, counterPartyForContract, startAmount));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindNoExtra() {
        double startAmount = 50.0;
        int count = 5;

        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, startAmount, count, 0);

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactionsWithContract);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, counterPartyForContract, startAmount));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 9",
            "90.0, 5"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindWithDayDifference(double startAmount, int count) {
        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, startAmount, count, 0);
        transactionsWithContract.get(1).setDate(transactionsWithContract.get(1).getDate().plusDays(1));
        transactionsWithContract.get(2).setDate(transactionsWithContract.get(2).getDate().minusDays(1));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, counterPartyForContract, startAmount));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 2",
            "90.0, 1"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFind_notEnoughDayMatchingDates(
            double startAmount, int count) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, startAmount, count, 0);
        transactionsWithContract.getLast().setDate(LocalDate.now().plusDays(10));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(0, 0);
        transactionsWithContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 2",
            "90.0, 1"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFind_notEnoughMonthMatchingDates(
            double startAmount, int count) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, startAmount, count, 0);
        transactionsWithContract.getLast().setDate(LocalDate.now().plusMonths(10));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(0, 0);
        transactionsWithContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3, 3",
            "50.0, 8, 1",
            "90.0, 4, 2"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_multipleContractToFind(
            double startAmount, int count, int counterPartyAmount) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();

        List<CounterParty> counterParties = dataCreator.createCounterParties(counterPartyAmount);

        List<Transaction> transactionsWithContract = counterParties.stream()
                .flatMap(counterParty -> {
                    double transactionAmount = startAmount + ((counterParties.indexOf(counterParty) + 1) * 10);
                    return dataCreator.createTransactionsForContract(counterParty, transactionAmount, count, 0).stream();
                })
                .toList();

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(counterPartyAmount*2, 0);
        transactionsWithContract.forEach(transaction -> {
            double expectedAmount = startAmount + ((counterParties.indexOf(transaction.getContract().getCounterParty()) + 1) * 10);
            assertTransactionContract(transaction, transaction.getContract().getCounterParty(), expectedAmount);
        });
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 80.0, 3, 3",
            "50.0, 70.0, 8, 6",
            "90.0, 100.0, 7, 9"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindWithHistoryBefore(
            double oldAmount, double newAmount, int oldCount, int newCount) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = new ArrayList<>();

        transactionsWithContract.addAll(dataCreator.createTransactionsForContract(counterPartyForContract, oldAmount, oldCount, 0));
        transactionsWithContract.addAll(dataCreator.createTransactionsForContract(counterPartyForContract, newAmount, newCount, 3));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 1);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, transaction.getContract().getCounterParty(), newAmount));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 80.0, 3, 3",
            "50.0, 70.0, 8, 1",
            "90.0, 100.0, 4, 2"
    })
    void processAndAssociateTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindWithHistoryNotEnough(
            double oldAmount, double newAmount, int oldCount, int newCount) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, oldAmount, oldCount, 0);
        List<Transaction> transactionsForHistory = dataCreator.createTransactionsForContract(counterPartyForContract, newAmount, newCount, oldCount);
        transactionsForHistory.getLast().setDate(transactionsForHistory.getLast().getDate().plusDays(4));

        allTransactions.addAll(transactionsForHistory);
        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction,
                transaction.getContract().getCounterParty(), oldAmount));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }
    //</editor-fold>

    //<editor-fold desc="existing contracts tests">

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContracts_oneTransaction_doesNot() {
        double amount = 50.0;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(new CounterParty(), amount, 1, 0);
        Transaction transaction = transactions.getFirst();

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContracts_oneTransaction_doesAfter() {
        double amount = 50.0;
        int monthsBetween = 2;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(counterPartyForContract, amount, 1, monthsBetween);
        Transaction transaction = transactions.getFirst();

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount, monthsBetween, 3);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(1, 0);
        assertTransactionContract(transaction, counterPartyForContract, amount, monthsBetween);
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContracts_oneTransaction_doesBefore() {
        double amount = 50.0;
        int monthsBetween = 2;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(counterPartyForContract, amount, 1, monthsBetween);
        Transaction transaction = transactions.getFirst();
        transaction.setDate(LocalDate.now().minusMonths(monthsBetween));

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount, monthsBetween, 3);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(1, 0);
        assertTransactionContract(transaction, counterPartyForContract, amount, monthsBetween);
        assertEquals(contract.getStartDate(), transaction.getDate());
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContracts_oneTransaction_doesBeforeNoMatch() {
        double amount = 50.0;
        int monthsBetween = 2;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(counterPartyForContract, amount, 1, monthsBetween);
        Transaction transaction = transactions.getFirst();
        transaction.setDate(LocalDate.now().minusMonths(monthsBetween + 1));

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount, monthsBetween, 3);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(1, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void processAndAssociateTransactions_oneTransaction_toFarFromLastPayDate() {
        double amount = 50.0;
        int monthsBetween = 2;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(counterPartyForContract, amount, 1, 9);
        Transaction transaction = transactions.getFirst();

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount, monthsBetween, 3);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3, 1",
            "50.0, 7, 3",
            "90.0, 4, 6"
    })
    void processAndAssociateTransactionsBelongToContract_existingContracts_multipleTransaction_does(
            double contractAmount, int count, int monthsBetween) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, contractAmount, count, monthsBetween, monthsBetween);

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, contractAmount, monthsBetween, count);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction ->
                assertTransactionContract(transaction, counterPartyForContract, contractAmount, monthsBetween));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @Test
    void processAndAssociateTransactions_existingContracts_multipleTransaction_nonWillMatch() {
        double contractAmount = 50.0;
        int count = 5;
        int monthsBetween = 2;

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, contractAmount, count, monthsBetween, monthsBetween);
        double counter = 100.0;
        for (Transaction transaction : transactionsWithContract) {
            counter += transaction.getAmount();
            transaction.setAmount(counter);
        }

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, contractAmount, monthsBetween, count);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        allTransactions.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_multipleContracts_multipleTransactions() {
        int counterPartyAmount = 5;
        int transactionCount = 3;
        double baseAmount = 50.0;
        double amountIncrement = 10.0;
        List<CounterParty> counterParties = dataCreator.createCounterParties(counterPartyAmount);

        // Create transactions (some with contracts, some without)
        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> {
                    CounterParty counterParty = counterParties.get(i);
                    double transactionAmount = baseAmount + (i * amountIncrement);
                    return dataCreator.createTransactionsForContract(counterParty, transactionAmount, transactionCount, 1);
                })
                .flatMap(List::stream)
                .toList();

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        // Ensure contracts match the corresponding transaction amount
        List<Contract> contracts = IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> dataCreator.createContract(COUNTER_PARTY_NAME + " " + i, counterParties.get(i), baseAmount + (i * amountIncrement)))
                .toList();

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(contracts);

        // Process transactions
        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        // Validate transactions with contracts
        IntStream.range(0, counterPartyAmount).forEach(i -> {
            CounterParty counterParty = counterParties.get(i);
            double expectedAmount = baseAmount + (i * amountIncrement);
            transactionsWithContract.stream()
                    .filter(transaction -> transaction.getContract().getCounterParty().equals(counterParty))
                    .forEach(transaction -> assertTransactionContract(transaction, counterParty, expectedAmount));
        });

        verifySaveCalls(counterPartyAmount, counterPartyAmount);
        // Validate transactions without contracts
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "-2",
            "0",
            "50"
    })
    void processAndAssociateTransactions_multipleTransactions_matchToContract_afterLastPayDate(int offset) {
        double amount = 50.0;
        int count = 3;
        int monthsBetween = 3;

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, amount, count,
                0, monthsBetween);
        transactionsWithContract.forEach(transaction -> transaction.setDate(transaction.getDate().plusMonths(offset)));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount + 10, monthsBetween - 1, count);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(2, 1);
        transactionsWithContract.forEach(transaction ->
                assertTransactionContract(transaction, transaction.getContract().getCounterParty(), amount, monthsBetween));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }
    //</editor-fold>
    //<editor-fold desc="with contract history">

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesNotBelongToHistory() {
        double amount = 50.0;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(new CounterParty(), amount, 1, 0);
        Transaction transaction = transactions.getFirst();

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount);
        ContractHistory contractHistory = dataCreator.createContractHistory(contract, amount + 10, amount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesNotBelongToHistorySameCounterParty() {
        double amount = 50.0;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(counterPartyForContract, amount + 20, 1, 0);
        Transaction transaction = transactions.getFirst();

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, amount);
        ContractHistory contractHistory = dataCreator.createContractHistory(contract, amount + 10, amount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void processAndAssociateTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesBelongToHistory() {
        double oldAmount = 50.0;
        double newAmount = oldAmount + 10;

        List<Transaction> transactions = dataCreator.createTransactionsForContract(counterPartyForContract, oldAmount, 1, 1);
        Transaction transaction = transactions.getFirst();

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, newAmount);
        ContractHistory contractHistory = dataCreator.createContractHistory(contract, newAmount, oldAmount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractAssociationService.processAndAssociateTransactions(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertTransactionContract(transaction, counterPartyForContract, newAmount);
    }

    @ParameterizedTest
    @CsvSource({
            "50.0, 60.0, 70.0, 70.0, 1, 7",
            "50.0, 60.0, 70.0, 60.0, 1, 1"
    })
    void processAndAssociateTransactions_multipleTransactions_withHistory_BelongTo(double oldAmount, double currentAmount,
                                                                                   double newAmount, double expected, int monthsBetween, int historyMonths) {
        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = dataCreator.createRandomTransactions();
        List<Transaction> transactionsWithContract = dataCreator.createTransactionsForContract(counterPartyForContract, newAmount, 10, historyMonths);

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        Contract contract = dataCreator.createContract(COUNTER_PARTY_NAME, counterPartyForContract, currentAmount, monthsBetween, 5);
        ContractHistory contractHistory = dataCreator.createContractHistory(contract, currentAmount, oldAmount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractAssociationService.processAndAssociateTransactions(bankAccount, allTransactions);

        verifySaveCalls(1, 1);
        transactionsWithContract.forEach(transaction ->
                assertTransactionContract(transaction, transaction.getContract().getCounterParty(), expected, monthsBetween));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }
    //</editor-fold>
}