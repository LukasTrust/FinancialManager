package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CounterPartyProcessingService {

    private CounterPartyService counterPartyService;

    public void setCounterCounterParties(Users users, List<Transaction> transactions) {
        List<CounterParty> existingCounterParties = counterPartyService.findByUsers(users);
        List<CounterParty> newCounterParties = new ArrayList<>();

        for (Transaction transaction : transactions) {
            boolean foundCounterParty = false;

            for (CounterParty counterParty : existingCounterParties) {
                if (counterParty.getCounterPartySearchStrings().contains(transaction.getOriginalCounterParty())) {
                    transaction.setCounterParty(counterParty);
                    foundCounterParty = true;
                }
            }

            if (!foundCounterParty) {
                CounterParty counterParty = new CounterParty(users, transaction.getOriginalCounterParty());
                newCounterParties.add(counterParty);
            }
        }

        if (!newCounterParties.isEmpty()) {
            counterPartyService.saveAll(newCounterParties);
        }
    }
}
