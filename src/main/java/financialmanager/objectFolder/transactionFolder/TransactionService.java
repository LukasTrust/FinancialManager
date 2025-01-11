package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByBankAccountId(Long accountId) {
        return transactionRepository.findByBankAccountId(accountId);
    }

    public List<Transaction> saveAll(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

    public List<CounterParty> findDistinctCounterPartiesByBankAccountId(Long accountId) {
        return transactionRepository.findDistinctCounterPartiesByBankAccountId(accountId);
    }
}
