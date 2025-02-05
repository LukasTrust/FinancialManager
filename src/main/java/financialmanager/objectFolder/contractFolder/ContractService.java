package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.transactionFolder.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final TransactionRepository transactionRepository;

    public List<Contract> findByBankAccountId(Long bankAccountId) {
        return transactionRepository.findDistinctContractsByBankAccountId(bankAccountId);
    }

    public void saveAll(List<Contract> contracts) {
        contractRepository.saveAll(contracts);
    }
}
