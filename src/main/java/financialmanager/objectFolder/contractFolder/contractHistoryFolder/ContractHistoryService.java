package financialmanager.objectFolder.contractFolder.contractHistoryFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ContractHistoryService {

    private final ContractHistoryRepository contractHistoryRepository;

    public List<ContractHistory> getContractHistoryForContract(Contract contract) {
        return contractHistoryRepository.findByContract(contract);
    }

    public void saveAll(List<ContractHistory> contractHistoryList) {
        contractHistoryRepository.saveAll(contractHistoryList);
    }
}
