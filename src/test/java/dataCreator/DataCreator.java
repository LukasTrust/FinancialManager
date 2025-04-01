package dataCreator;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.mock;

public class DataCreator {

    private final Users users;
    private final Faker faker;
    private final String COUNTER_PARTY_NAME;

    public DataCreator(String COUNTER_PARTY_NAME, Users users) {
        this.COUNTER_PARTY_NAME = COUNTER_PARTY_NAME;
        this.users = users;
        faker = new Faker();
    }

    public BankAccount createBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setName(faker.name().firstName());
        bankAccount.setUsers(users);

        return bankAccount;
    }

    public Contract createContract(BankAccount bankAccount) {
        Contract contract = createContract(faker.name().firstName(), createCounterParty(users, faker.company().name()),
                50.0, 1, 1);
        contract.setBankAccount(bankAccount);

        return contract;
    }

    public Contract createContract(String name, CounterParty counterParty, double amount) {
        return createContract(name, counterParty, amount, 1, 1);
    }

    public Contract createContract(String name, CounterParty counterParty, double amount, int monthsBetween, int lastPaymentOffset) {
        Contract contract = new Contract();
        contract.setName(name);
        contract.setCounterParty(counterParty);
        contract.setAmount(amount);
        contract.setStartDate(LocalDate.now());
        contract.setLastPaymentDate(LocalDate.now().plusMonths(lastPaymentOffset + 1));
        contract.setMonthsBetweenPayments(monthsBetween);

        return contract;
    }

    public ContractHistory createContractHistory(Contract contract, double newAmount, double oldAmount) {
        return new ContractHistory(contract, newAmount, oldAmount, LocalDate.now().plusMonths(5));
    }

    public Transaction createTransaction(double amount, CounterParty counterParty, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCounterParty(counterParty);
        transaction.setDate(date);
        return transaction;
    }

    public List<Transaction> createRandomTransactions() {
        return IntStream.range(0, 10)
                .mapToObj(i -> createTransaction(faker.random().nextDouble(),
                        new CounterParty(users, faker.company().name()), LocalDate.now().plusDays(i)))
                .toList();
    }

    public List<Transaction> createTransactionsForContract(CounterParty counterParty, double amount, int count, int startMonth) {
        return createTransactionsForContract(counterParty, amount, count, startMonth, 1);
    }

    public List<Transaction> createTransactionsForContract(CounterParty counterParty, double amount, int count, int startMonth, int monthsBetween) {
        return IntStream.range(0, count)
                .mapToObj(i -> createTransaction(amount, counterParty, LocalDate.now().plusMonths(startMonth + (long) i * monthsBetween)))
                .toList();
    }

    public CounterParty createCounterParty(Users users, String name) {
        return new CounterParty(users, name);
    }

    public List<CounterParty> createCounterParties(int counterPartyAmount) {
        return IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> createCounterParty(users, COUNTER_PARTY_NAME + " " + i))
                .toList();
    }

}
