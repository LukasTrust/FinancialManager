package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
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

    public List<CounterParty> findByUsers(Users user) {
        return counterPartyRepository.findByUsers(user);
    }
}
