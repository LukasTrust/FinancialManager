package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseContractService {

    private final ContractRepository contractRepository;

    public List<Contract> findByBankAccount(BankAccount bankAccount) {
        return contractRepository.findByBankAccount(bankAccount);
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

    @Async
    public void save(Contract contract) {
        contractRepository.save(contract);
    }

    public void deleteAll(List<Contract> contracts) {
        contractRepository.deleteAll(contracts);
    }

    @Async
    public void setCounterParty(CounterParty counterParty, List<Contract> contracts, boolean instanceSave) {
        contracts.forEach(contract -> contract.setCounterParty(counterParty));
        if (instanceSave) contractRepository.saveAll(contracts);
    }

    @Async
    public void setHidden(boolean hide, List<Contract> contracts) {
        contracts.forEach(contract -> contract.setHidden(hide));
        saveAll(contracts);
    }
}
