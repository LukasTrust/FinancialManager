package src.fakeDataCreator;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
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

    public static Users createFakeUsers(Long id) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password();

        Users mockUsers = mock(Users.class);
        when(mockUsers.getId()).thenReturn(id);
        when(mockUsers.getFirstName()).thenReturn(firstName);
        when(mockUsers.getLastName()).thenReturn(lastName);
        when(mockUsers.getEmail()).thenReturn(email);
        when(mockUsers.getPassword()).thenReturn(password);

        return mockUsers;
    }

    public static BankAccount createFakeBankAccount(Long id, Users users, boolean isSavingsAccount, boolean withStringLists) {
        BankAccount mockBankAccount;

        if (isSavingsAccount) {
            SavingsBankAccount mockSavingsAccount = mock(SavingsBankAccount.class);
            Double interestRate = faker.number().randomDouble(2, 0, 5);
            when(mockSavingsAccount.getInterestRate()).thenReturn(interestRate);

            if (withStringLists) {
                when(mockSavingsAccount.getInterestRateSearchStrings()).thenReturn(getRandomListString(faker.number().randomDigit()));
            }

            mockBankAccount = mockSavingsAccount;
        }
        else {
            mockBankAccount = mock(BankAccount.class);
        }

        String name = faker.lorem().word();
        String description = faker.lorem().sentence(5);

        when(mockBankAccount.getId()).thenReturn(id);
        when(mockBankAccount.getUsers()).thenReturn(users);
        when(mockBankAccount.getName()).thenReturn(name);
        when(mockBankAccount.getDescription()).thenReturn(description);

        if (withStringLists) {
            when(mockBankAccount.getAmountInBankAfterSearchStrings()).thenReturn(getRandomListString(faker.number().randomDigit()));
            when(mockBankAccount.getCounterPartySearchStrings()).thenReturn(getRandomListString(faker.number().randomDigit()));
            when(mockBankAccount.getDateSearchStrings()).thenReturn(getRandomListString(faker.number().randomDigit()));
            when(mockBankAccount.getAmountSearchStrings()).thenReturn(getRandomListString(faker.number().randomDigit()));
        }

        return mockBankAccount;
    }

    public static List<Transaction> getFakeTransactions(BankAccount bankAccount, int numberOfTransactions) {
        List<Transaction> transactions = new ArrayList<>();

        for (long i = 0L; i < numberOfTransactions; i++) {
            transactions.add(getFakeTransaction(i, bankAccount, i%2 == 0));
        }

        return transactions;
    }

    public static Transaction getFakeTransaction(Long id, BankAccount bankAccount, boolean matchingAfter) {
        Transaction mockTransaction = mock(Transaction.class);
        double amount = faker.number().randomDouble(2, 10, 10000);
        double amountBefore = faker.number().randomDouble(2, 1000, 50000);
        double amountAfter = matchingAfter ? amountBefore + amount : faker.number().randomDouble(2, 1000, 50000);
        LocalDate date = getRandomDate();
        String originalCounterParty = faker.lorem().word();

        when(mockTransaction.getId()).thenReturn(id);
        when(mockTransaction.getBankAccount()).thenReturn(bankAccount);
        when(mockTransaction.getAmount()).thenReturn(amount);
        when(mockTransaction.getAmountInBankBefore()).thenReturn(amountBefore);
        when(mockTransaction.getAmountInBankAfter()).thenReturn(amountAfter);
        when(mockTransaction.getDate()).thenReturn(date);
        when(mockTransaction.getOriginalCounterParty()).thenReturn(originalCounterParty);

        return mockTransaction;
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
