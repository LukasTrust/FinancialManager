package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseTransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction findById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<Transaction> findByBankAccountId(Long bankAccountId) {
        return transactionRepository.findByBankAccountId(bankAccountId);
    }

    public List<Transaction> findByIdInAndBankAccountId(List<Long> ids, Long bankAccountId) {
        return transactionRepository.findByIdInAndBankAccountId(ids, bankAccountId);
    }

    public List<Transaction> findByCounterParty(CounterParty counterParty) {
        return transactionRepository.findByCounterParty(counterParty);
    }

    public List<Transaction> findByCounterPartyIn(List<CounterParty> counterParties) {
        return transactionRepository.findByCounterPartyIn(counterParties);
    }

    public List<Transaction> findByOriginalCounterParty(String originalCounterParty) {
        return transactionRepository.findByOriginalCounterParty(originalCounterParty);
    }

    public List<Transaction> findByContract(Contract contract) {
        return transactionRepository.findByContract(contract);
    }

    public void saveAll(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
