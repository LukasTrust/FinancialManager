package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBankAccountId(Long accountId);

    @Query("SELECT DISTINCT t.contract FROM Transaction t WHERE t.bankAccount.id = :bank_account_Id")
    List<Contract> findDistinctContractsByBankAccountId(@Param("bank_account_Id") Long bankAccountId);
}
