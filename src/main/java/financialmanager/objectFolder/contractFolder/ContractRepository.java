package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByBankAccount(BankAccount bankAccount);

    Contract findByIdAndBankAccountId(Long id, Long bankAccountId);

    List<Contract> findByIdInAndBankAccountId(List<Long> ids, Long bankAccountId);
}
