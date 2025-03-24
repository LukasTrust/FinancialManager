package financialmanager.objectFolder.contractFolder.contractHistoryFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class BaseContractHistoryService {

    private final ContractHistoryRepository contractHistoryRepository;

    @Getter
    private static CompletableFuture<Void> lastDeleteFuture;

    public List<ContractHistory> findByContract(Contract contract) {
        return contractHistoryRepository.findByContract(contract);
    }

    public List<ContractHistory> findByContractIn(List<Contract> contracts) {
        return contractHistoryRepository.findByContractIn(contracts);
    }

    public void saveAll(List<ContractHistory> contractHistoryList) {
        contractHistoryRepository.saveAll(contractHistoryList);
    }

    @Async
    public void save(ContractHistory contractHistory) {
        contractHistoryRepository.save(contractHistory);
    }

    public void deleteAll(List<ContractHistory> contractHistoryList) {
        lastDeleteFuture = CompletableFuture.runAsync(() -> contractHistoryRepository.deleteAll(contractHistoryList));
    }
}
