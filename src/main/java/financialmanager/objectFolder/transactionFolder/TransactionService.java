package financialmanager.objectFolder.transactionFolder;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByBankAccountId(Long bankAccountId) {
        return transactionRepository.findByBankAccountId(bankAccountId);
    }

    public List<Transaction> findByBankAccountIdAndNoContract(Long bankAccountId) {
        return transactionRepository.findByBankAccountIdAndContractIsNull(bankAccountId);
    }

    public void saveAll(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    public List<CounterParty> findDistinctCounterPartiesByBankAccountId(Long accountId) {
        return transactionRepository.findDistinctCounterPartiesByBankAccountId(accountId);
    }

    public List<Contract> findDistinctContractsByBankAccountId(Long accountId) {
        return transactionRepository.findDistinctContractsByBankAccountId(accountId);
    }
}
