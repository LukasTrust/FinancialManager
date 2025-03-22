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

    public List<Transaction> findByContractIn(List<Contract> contracts) {
        return transactionRepository.findByContractIn(contracts);
    }

    public void saveAll(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    public void deleteAll(List<Transaction> transactions) {
        transactionRepository.deleteAll(transactions);
    }

    public void setContract(Contract contract, List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setContract(contract));
        saveAll(transactions);
    }

    public void setCounterParty(CounterParty counterParty, List<Transaction> transactions, boolean instanceSave) {
        transactions.forEach(transaction -> transaction.setCounterParty(counterParty));
        if (instanceSave) saveAll(transactions);
    }

    public void setHidden(boolean hide, List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setHidden(hide));
        saveAll(transactions);
    }
}
