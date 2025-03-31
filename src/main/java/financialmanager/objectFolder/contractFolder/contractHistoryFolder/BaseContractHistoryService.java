package financialmanager.objectFolder.contractFolder.contractHistoryFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseContractHistoryService {

    private final ContractHistoryRepository contractHistoryRepository;

    public List<ContractHistory> findByContractIn(List<Contract> contracts) {
        return contractHistoryRepository.findByContractIn(contracts);
    }

    @Async
    public void saveAsync(ContractHistory contractHistory) {
        contractHistoryRepository.save(contractHistory);
    }

    public void deleteAll(List<ContractHistory> contractHistoryList) {
        contractHistoryRepository.deleteAll(contractHistoryList);
    }
}
