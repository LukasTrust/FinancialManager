package financialmanager.Utils;

import src.fakeDataCreator.FakeObjectCreator;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class UtilsTest {

    static BankAccount bankAccount;

    @BeforeAll
    static void setUpBeforeClass() {
        Users users = FakeObjectCreator.createFakeUsers(1L);
        bankAccount = FakeObjectCreator.createFakeBankAccount(1L, users, false, false);
    }

    @Test
    void getDifferenceInTransaction_noDifference() {
        // Arrange
        Transaction transaction = FakeObjectCreator.getFakeTransaction(1L, bankAccount, true);

        // Act
        double result = Utils.getDifferenceInTransaction(transaction);

        // Assert
        assertThat(result).isEqualTo(0);
    }

    @Test
    void getDifferenceInTransaction_hasDifference() {
        // Arrange
        Transaction transaction = FakeObjectCreator.getFakeTransaction(1L, bankAccount, false);

        // Act
        double result = Utils.getDifferenceInTransaction(transaction);

        // Assert
        assertThat(result).isNotEqualTo(0);
    }

    @Test
    void getRightDateRange_bothNull() {
        // Arrange
        // Act
        LocalDate[] result = Utils.getRightDateRange(null, null);

        // Assert
        assertThat(result[0]).isEqualTo(LocalDate.MIN);
        assertThat(result[1]).isEqualTo(LocalDate.MAX);
    }

    @Test
    void getRightDateRange_firstNull() {
        // Arrange
        LocalDate endDate = LocalDate.now();

        // Act
        LocalDate[] result = Utils.getRightDateRange(null, endDate);

        // Assert
        assertThat(result[0]).isEqualTo(LocalDate.MIN);
        assertThat(result[1]).isEqualTo(endDate);
    }

    @Test
    void getRightDateRange_secondNull() {
        // Arrange
        LocalDate startDate = LocalDate.now();

        // Act
        LocalDate[] result = Utils.getRightDateRange(startDate, null);

        // Assert
        assertThat(result[0]).isEqualTo(startDate);
        assertThat(result[1]).isEqualTo(LocalDate.MAX);
    }

    @Test
    void getRightDateRange_noneAreNull() {
        // Arrange
        LocalDate date = LocalDate.now();

        // Act
        LocalDate[] result = Utils.getRightDateRange(date, date);

        // Assert
        assertThat(result[0]).isEqualTo(date);
        assertThat(result[1]).isEqualTo(date);
    }

    @Test
    void normalizeDateRange_firstIsNull() {
        // Arrange
        LocalDate date = LocalDate.now();

        // Act
        LocalDate[] result = Utils.normalizeDateRange(null, date);

        // Assert
        assertThat(result[0]).isNull();
        assertThat(result[1]).isEqualTo(date);
    }

    @Test
    void normalizeDateRange_secondIsNull() {
        // Arrange
        LocalDate date = LocalDate.now();

        // Act
        LocalDate[] result = Utils.normalizeDateRange(date, null);

        // Assert
        assertThat(result[0]).isEqualTo(date);
        assertThat(result[1]).isNull();
    }

    @Test
    void normalizeDateRange_noneAreNullNoSwitch() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Act
        LocalDate[] result = Utils.normalizeDateRange(startDate, endDate);

        // Assert
        assertThat(result[0]).isEqualTo(startDate);
        assertThat(result[1]).isEqualTo(endDate);
    }

    @Test
    void normalizeDateRange_noneAreNullSwitch() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(10);

        // Act
        LocalDate[] result = Utils.normalizeDateRange(startDate, endDate);

        // Assert
        assertThat(result[0]).isEqualTo(endDate);
        assertThat(result[1]).isEqualTo(startDate);
    }

    @Test
    void getFirstTransaction() {
        // Arrange
        List<Transaction> transactions = FakeObjectCreator.getFakeTransactions(bankAccount, 10);
        Transaction transaction = transactions.get(4);
        when(transaction.getDate()).thenReturn(LocalDate.MIN);

        // Act
        Transaction result = Utils.getFirstTransaction(transactions);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(LocalDate.MIN);
        assertThat(result).isEqualTo(transaction);
    }

    @Test
    void getLastTransaction() {
        // Arrange
        List<Transaction> transactions = FakeObjectCreator.getFakeTransactions(bankAccount, 10);
        Transaction transaction = transactions.get(6);
        when(transaction.getDate()).thenReturn(LocalDate.MAX);

        // Act
        Transaction result = Utils.getLastTransaction(transactions);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo(LocalDate.MAX);
        assertThat(result).isEqualTo(transaction);
    }
}