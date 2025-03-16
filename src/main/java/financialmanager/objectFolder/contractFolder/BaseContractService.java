package financialmanager.objectFolder.contractFolder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseContractService {

    private ContractRepository contractRepository;

    public List<Contract> findByBankAccountId(Long bankAccountId) {
        return contractRepository.findByBankAccountId(bankAccountId);
    }

    public List<Contract> findByIdInAndBankAccountId(List<Long> ids, Long bankAccountId) {
        return contractRepository.findByIdInAndBankAccountId(ids, bankAccountId);
    }

    public Contract findByIdAndBankAccountId(Long id, Long bankAccountId) {
        return contractRepository.findByIdAndBankAccountId(id, bankAccountId);
    }

    public void saveAll(List<Contract> contracts) {
        contractRepository.saveAll(contracts);
    }

    public void save(Contract contract) {
        contractRepository.save(contract);
    }

    public void deleteAll(List<Contract> contracts) {
        contractRepository.deleteAll(contracts);
    }
}
