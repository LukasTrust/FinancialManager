package fakeDataCreator;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeObjectCreator {

    private static final Faker faker = new Faker();

    public Users createUsers() {
        Users users = new Users();
        when(users.getFirstName()).thenReturn(faker.name().firstName());
        when(users.getLastName()).thenReturn(faker.name().lastName());
        when(users.getEmail()).thenReturn(faker.internet().emailAddress());
        when(users.getPassword()).thenReturn(faker.internet().password());
        return users;
    }

    public BankAccount createBankAccount(Users users) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setUsers(users);
        bankAccount.setName(faker.company().name());
        bankAccount.setCurrencySymbol("€");
        return bankAccount;
    }

    public Transaction createTransaction(BankAccount bankAccount, Double amountInBankBefore) {
        LocalDate date = getRandomDate();
        String name = faker.name().firstName();
        Double amount = faker.random().nextDouble();

        Transaction transaction = new Transaction();
        transaction.setBankAccount(bankAccount);
        transaction.setDate(date);
    }

    public static List<String> getRandomListString(int length) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            list.add(faker.lorem().word());
        }

        return list;
    }

    public static LocalDate getRandomDate() {
        return LocalDate.now().minusDays(faker.number().randomDigit());
    }
}
