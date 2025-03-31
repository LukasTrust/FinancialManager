package financialmanager.objectFolder.contractFolder.contractHistoryFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractHistoryRepository extends JpaRepository<ContractHistory, Long> {
    List<ContractHistory> findByContractIn(List<Contract> contracts);
}