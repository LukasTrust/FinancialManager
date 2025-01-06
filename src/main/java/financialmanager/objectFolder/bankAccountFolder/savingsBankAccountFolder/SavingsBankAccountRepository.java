package financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsBankAccountRepository extends JpaRepository<SavingsBankAccount, Long> {
}