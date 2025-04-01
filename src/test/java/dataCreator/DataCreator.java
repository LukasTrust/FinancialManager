package dataCreator;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class DataCreator {

    private List<Transaction> createRandomTransactions() {
        return IntStream.range(0, 10)
                .mapToObj(i -> createTransaction(faker.random().nextDouble(),
                        new CounterParty(users, faker.company().name()), LocalDate.now().plusDays(i)))
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

    private List<CounterParty> createCounterParties(int counterPartyAmount) {
        return IntStream.range(0, counterPartyAmount)
                .mapToObj(i -> new CounterParty(users, COUNTER_PARTY_NAME + " " + i))
                .toList();
    }

}
