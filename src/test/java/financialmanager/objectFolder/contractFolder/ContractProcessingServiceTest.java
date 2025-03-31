package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.BaseContractHistoryService;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionRepository;
import financialmanager.objectFolder.usersFolder.Users;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ContractProcessingServiceTest {

    private ContractProcessingService contractProcessingService;
    private BaseContractService baseContractService;
    private BaseContractHistoryService baseContractHistoryService;
    private BankAccount bankAccount;
    private Users users;
    private Faker faker;

    @BeforeEach
    void setup() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        baseContractService = mock(BaseContractService.class);
        baseContractHistoryService = mock(BaseContractHistoryService.class);
        BaseTransactionService baseTransactionService = new BaseTransactionService(transactionRepository);

        bankAccount = mock(BankAccount.class);
        users = mock(Users.class);
        faker = new Faker();

        contractProcessingService = new ContractProcessingService(baseContractService, baseContractHistoryService, baseTransactionService);
    }

    @AfterEach
    void tearDown() {
        verify(baseContractService, atMost(1)).findByBankAccount(any());
        verify(baseContractHistoryService, atMost(1)).findByContractIn(anyList());
    }

    //<editor-fold desc="help methods">
    private List<Transaction> createRandomTransactions() {
        return IntStream.range(0, 10)
                .mapToObj(i -> createTransaction(faker.random().nextDouble(), createCounterParty(faker.company().name()), LocalDate.now().plusDays(i)))
                .toList();
    }

    private Transaction createTransaction(double amount, CounterParty counterParty, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCounterParty(counterParty);
        transaction.setDate(date);
        return transaction;
    }

    private Contract createContract(String name, CounterParty counterParty, double amount) {
        return createContract(name, counterParty, amount, 1, 1);
    }

    private Contract createContract(String name, CounterParty counterParty, double amount, int monthsBetween, int lastPaymentOffset) {
        Contract contract = new Contract();
        contract.setName(name);
        contract.setCounterParty(counterParty);
        contract.setAmount(amount);
        contract.setStartDate(LocalDate.now());
        contract.setLastPaymentDate(LocalDate.now().plusMonths(lastPaymentOffset + 1));
        contract.setMonthsBetweenPayments(monthsBetween);

        return contract;
    }

    private ContractHistory createContractHistory(Contract contract, double newAmount, double oldAmount) {
        return new ContractHistory(contract, newAmount, oldAmount, LocalDate.now().plusMonths(5));
    }

    private List<Transaction> createTransactionsForContract(CounterParty counterParty, double amount, int count, int startMonth) {
        return createTransactionsForContract(counterParty, amount, count, startMonth, 1);
    }

    private List<Transaction> createTransactionsForContract(CounterParty counterParty, double amount, int count, int startMonth, int monthsBetween) {
        return IntStream.range(0, count)
                .mapToObj(i -> createTransaction(amount, counterParty, LocalDate.now().plusMonths(startMonth + (long) i * monthsBetween)))
                .toList();
    }

    private CounterParty createCounterParty(String name) {
        return new CounterParty(users, name);
    }

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
    void checkIfTransactionsBelongToContract_noExistingContracts_oneTransaction() {
        List<Transaction> transactions = createTransactionsForContract(new CounterParty(), 50.0, 1, 0);
        Transaction transaction = transactions.getFirst();

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_noContractToFind() {
        List<Transaction> transactionsWithoutContract = createRandomTransactions();

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactionsWithoutContract);

        verifySaveCalls(0, 0);
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 9",
            "90.0, 5"
    })
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFind(
            double startAmount, int count) {

        CounterParty counterPartyForContract = new CounterParty(users, "Contract Counter Party");

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, startAmount, count, 0);

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, counterPartyForContract, startAmount));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @Test
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindNoExtra() {
        double startAmount = 50.0;
        int count = 5;

        CounterParty counterPartyForContract = new CounterParty(users, "Contract Counter Party");

        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, startAmount, count, 0);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactionsWithContract);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, counterPartyForContract, startAmount));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 9",
            "90.0, 5"
    })
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindWithDayDifference(
            double startAmount, int count) {

        CounterParty counterPartyForContract = new CounterParty(users, "Contract Counter Party");

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, startAmount, count, 0);
        transactionsWithContract.get(1).setDate(transactionsWithContract.get(1).getDate().plusDays(1));
        transactionsWithContract.get(2).setDate(transactionsWithContract.get(2).getDate().minusDays(1));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

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
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFind_notEnoughDayMatchingDates(
            double startAmount, int count) {

        CounterParty counterPartyForContract = new CounterParty(users, "Contract Counter Party");

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, startAmount, count, 0);
        transactionsWithContract.getLast().setDate(LocalDate.now().plusDays(10));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(0, 0);
        transactionsWithContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3",
            "50.0, 2",
            "90.0, 1"
    })
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFind_notEnoughMonthMatchingDates(
            double startAmount, int count) {

        CounterParty counterPartyForContract = new CounterParty(users, "Contract Counter Party");

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, startAmount, count, 0);
        transactionsWithContract.getLast().setDate(LocalDate.now().plusMonths(10));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(0, 0);
        transactionsWithContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3, 3",
            "50.0, 8, 1",
            "90.0, 4, 2"
    })
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_multipleContractToFind(
            double startAmount, int count, int counterPartyAmount) {

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();

        List<CounterParty> counterParties = IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> createCounterParty("Contract Counter Party " + i))
                .toList();

        List<Transaction> transactionsWithContract = counterParties.stream()
                .flatMap(counterParty -> {
                    double transactionAmount = startAmount + ((counterParties.indexOf(counterParty) + 1) * 10);
                    return createTransactionsForContract(counterParty, transactionAmount, count, 0).stream();
                })
                .toList();

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

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
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindWithHistoryBefore(
            double oldAmount, double newAmount, int oldCount, int newCount) {

        CounterParty counterPartyForContract = createCounterParty("Contract Counter Party");

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = new ArrayList<>();
        transactionsWithContract.addAll(createTransactionsForContract(counterPartyForContract, oldAmount, oldCount, 0));
        transactionsWithContract.addAll(createTransactionsForContract(counterPartyForContract, newAmount, newCount, 3));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

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
    void checkIfTransactionsBelongToContract_noExistingContracts_multipleTransactions_oneContractToFindWithHistoryNotEnough(
            double oldAmount, double newAmount, int oldCount, int newCount) {

        CounterParty counterPartyForContract = createCounterParty("Contract Counter Party");

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, oldAmount, oldCount, 0);
        List<Transaction> transactionsForHistory = createTransactionsForContract(counterPartyForContract, newAmount, newCount, oldCount);
        transactionsForHistory.getLast().setDate(transactionsForHistory.getLast().getDate().plusDays(4));

        allTransactions.addAll(transactionsForHistory);
        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction,
                transaction.getContract().getCounterParty(), oldAmount));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }
    //</editor-fold>

    //<editor-fold desc="existing contracts tests">

    @Test
    void checkIfTransactionsBelongToContract_existingContracts_oneTransaction_doesNotBelongToContract() {
        double amount = 50.0;

        List<Transaction> transactions = createTransactionsForContract(new CounterParty(), amount, 1, 0);
        Transaction transaction = transactions.getFirst();
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);
        Contract contract = createContract(name, counterPartyForContract, amount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void checkIfTransactionsBelongToContract_existingContracts_oneTransaction_doesBelongToContract() {
        double amount = 50.0;
        int monthsBetween = 2;
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> transactions = createTransactionsForContract(counterPartyForContract, amount, 1, monthsBetween);
        Transaction transaction = transactions.getFirst();

        Contract contract = createContract(name, counterPartyForContract, amount, monthsBetween, 3);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(1, 0);
        assertTransactionContract(transaction, counterPartyForContract, amount, monthsBetween);
        assertThat(transaction.getContract().getLastPaymentDate()).isEqualTo(transaction.getDate());
    }

    @Test
    void checkIfTransactionsBelongToContract_existingContracts_oneTransaction_toLateForContract() {
        double amount = 50.0;
        int monthsBetween = 2;
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> transactions = createTransactionsForContract(counterPartyForContract, amount, 1, 9);
        Transaction transaction = transactions.getFirst();

        Contract contract = createContract(name, counterPartyForContract, amount, monthsBetween, 3);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @ParameterizedTest
    @CsvSource({
            "60.0, 3, 1",
            "50.0, 7, 3",
            "90.0, 4, 6"
    })
    void checkIfTransactionsBelongToContract_existingContracts_multipleTransaction_doesBelongToContract(double contractAmount, int count, int monthsBetween) {
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, contractAmount, count, monthsBetween, monthsBetween);

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        Contract contract = createContract(name, counterPartyForContract, contractAmount, monthsBetween, count);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(2, 0);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, counterPartyForContract, contractAmount, monthsBetween));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    @Test
    void checkIfTransactionsBelongToContract_MultipleContracts_multipleTransactions() {
        int counterPartyAmount = 5;
        int transactionCount = 3;
        double baseAmount = 50.0;
        double amountIncrement = 10.0;

        // Create counterparties
        List<CounterParty> counterParties = IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> createCounterParty("Contract Counter Party " + i))
                .toList();

        // Create transactions (some with contracts, some without)
        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();

        List<Transaction> transactionsWithContract = IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> {
                    CounterParty counterParty = counterParties.get(i);
                    double transactionAmount = baseAmount + (i * amountIncrement);
                    return createTransactionsForContract(counterParty, transactionAmount, transactionCount, 1);
                })
                .flatMap(List::stream)
                .toList();

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        // Ensure contracts match the corresponding transaction amount
        List<Contract> contracts = IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> createContract("Contract Counter Party " + i, counterParties.get(i), baseAmount + (i * amountIncrement)))
                .toList();

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(contracts);

        // Process transactions
        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

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
    void checkIfTransactionsBelongToContract_existingContracts_multipleTransaction_createNewContract(
            int offset) {
        double amount = 50.0;
        int count = 3;
        int monthsBetween = 3;
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, amount, count,
                0, monthsBetween);
        transactionsWithContract.forEach(transaction -> transaction.setDate(transaction.getDate().plusMonths(offset)));

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);

        Contract contract = createContract(name, counterPartyForContract, amount + 10, monthsBetween - 1, count);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(2, 1);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, transaction.getContract().getCounterParty(), amount, monthsBetween));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }
    //</editor-fold>
    //<editor-fold desc="with contract history">

    @Test
    void checkIfTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesNotBelongToHistory() {
        double amount = 50.0;

        List<Transaction> transactions = createTransactionsForContract(new CounterParty(), amount, 1, 0);
        Transaction transaction = transactions.getFirst();

        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        Contract contract = createContract(name, counterPartyForContract, amount);
        ContractHistory contractHistory = createContractHistory(contract, amount + 10, amount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void checkIfTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesNotBelongToHistorySameCounterParty() {
        double amount = 50.0;

        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> transactions = createTransactionsForContract(counterPartyForContract, amount + 20, 1, 0);
        Transaction transaction = transactions.getFirst();

        Contract contract = createContract(name, counterPartyForContract, amount);
        ContractHistory contractHistory = createContractHistory(contract, amount + 10, amount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertNull(transaction.getContract());
    }

    @Test
    void checkIfTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesBelongToHistory() {
        double oldAmount = 50.0;
        double newAmount = oldAmount + 10;
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> transactions = createTransactionsForContract(counterPartyForContract, oldAmount, 1, 1);
        Transaction transaction = transactions.getFirst();

        Contract contract = createContract(name, counterPartyForContract, newAmount);
        ContractHistory contractHistory = createContractHistory(contract, newAmount, oldAmount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, transactions);

        verifySaveCalls(0, 0);
        assertTransactionContract(transaction, counterPartyForContract, newAmount);
    }

    @Test
    void checkIfTransactionsBelongToContract_existingContractsAndHistory_oneTransaction_doesBelongToContractCreateHistory() {
        double oldAmount = 50.0;
        double currentAmount = oldAmount + 10;
        double newAmount = currentAmount + 10;
        int monthsBetween = 1;
        String name = "Contract Counter Party";

        CounterParty counterPartyForContract = createCounterParty(name);

        List<Transaction> allTransactions = new ArrayList<>();
        List<Transaction> transactionsWithoutContract = createRandomTransactions();
        List<Transaction> transactionsWithContract = createTransactionsForContract(counterPartyForContract, newAmount, 10, 7);

        allTransactions.addAll(transactionsWithContract);
        allTransactions.addAll(transactionsWithoutContract);


        Contract contract = createContract(name, counterPartyForContract, currentAmount, monthsBetween, 5);
        ContractHistory contractHistory = createContractHistory(contract, currentAmount, oldAmount);

        when(baseContractService.findByBankAccount(bankAccount)).thenReturn(List.of(contract));
        when(baseContractHistoryService.findByContractIn(List.of(contract))).thenReturn(List.of(contractHistory));

        contractProcessingService.checkIfTransactionsBelongToContract(bankAccount, allTransactions);

        verifySaveCalls(1, 1);
        transactionsWithContract.forEach(transaction -> assertTransactionContract(transaction, transaction.getContract().getCounterParty(), newAmount, monthsBetween));
        transactionsWithoutContract.forEach(transaction -> assertNull(transaction.getContract()));
    }

    //</editor-fold>
}