package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class CounterPartyService {

    private final CounterPartyRepository counterPartyRepository;
    private final TransactionService transactionService;

    public void saveAll(List<CounterParty> counterParties) {
        counterPartyRepository.saveAll(counterParties);
    }


    public List<CounterParty> findAllCounterPartiesOfBankAccount(Long bankAccountId) {
        return transactionService.findDistinctCounterPartiesByBankAccountId(bankAccountId);
    }

    public void createOrUpdateCounterParty(List<Transaction> transactions) {

    }
}
