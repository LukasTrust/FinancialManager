package financialmanager.objectFolder.contractFolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Contract findByIdAndUsersId(Long id, Long usersId);

    @Query("SELECT DISTINCT t.contract FROM Transaction t WHERE t.bankAccount.id = :bank_account_Id")
    List<Contract> findDistinctContractsByBankAccountId(@Param("bank_account_Id") Long bankAccountId);
}
