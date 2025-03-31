package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseTransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByBankAccount(BankAccount bankAccount) {
        return transactionRepository.findByBankAccount(bankAccount);
    }

    public List<Transaction> findByBankAccountAndContractNull(BankAccount bankAccount) {
        return transactionRepository.findByBankAccountAndContractNull(bankAccount);
    }

    public List<Transaction> findByIdInAndBankAccount(List<Long> ids, BankAccount bankAccount) {
        return transactionRepository.findByIdInAndBankAccount(ids, bankAccount);
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

    @Async
    public void setContractAsync(Contract contract, List<Transaction> transactions, boolean instanceSave) {
        transactions.forEach(transaction -> transaction.setContract(contract));
        if (instanceSave) saveAll(transactions);
    }

    @Async
    public void setCounterPartyAsync(CounterParty counterParty, List<Transaction> transactions, boolean instanceSave) {
        transactions.forEach(transaction -> transaction.setCounterParty(counterParty));
        if (instanceSave) saveAll(transactions);
    }

    @Async
    public void setHidden(boolean hide, List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setHidden(hide));
        saveAll(transactions);
    }
}
