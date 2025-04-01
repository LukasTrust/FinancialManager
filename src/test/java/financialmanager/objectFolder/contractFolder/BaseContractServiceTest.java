package financialmanager.objectFolder.contractFolder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BaseContractServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    BaseContractService baseContractService;

    @BeforeAll
    static void setUp() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void findByBankAccount() {
    }

    @Test
    void findByIdInAndBankAccountId() {
    }

    @Test
    void findByIdAndBankAccountId() {
    }

    @Test
    void saveAll() {
    }

    @Test
    void saveAsync() {
    }

    @Test
    void save() {
    }

    @Test
    void deleteAll() {
    }

    @Test
    void setCounterPartyAsync() {
    }

    @Test
    void setHiddenAsync() {
    }
}