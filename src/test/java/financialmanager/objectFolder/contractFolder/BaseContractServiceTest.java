package financialmanager.objectFolder.contractFolder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BaseContractServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    BaseContractService baseContractService;

    @BeforeEach
    void setup() {
        baseContractService = new BaseContractService(mock(ContractRepository.class));
    }

    @BeforeAll
    static void setUp() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    //<editor-fold desc="help methods">

    private

    //</editor-fold>
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