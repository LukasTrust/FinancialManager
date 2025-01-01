package financialmanager.contractFolder.contractHistoryFolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractHistoryRepository extends JpaRepository<ContractHistory, Integer> {
}
