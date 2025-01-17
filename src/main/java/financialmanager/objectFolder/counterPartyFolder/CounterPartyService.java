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
    private final Map<Long, List<CounterParty>> counterPartyMap = new HashMap<>();

    public void saveAll(List<CounterParty> counterParties) {
        counterPartyRepository.saveAll(counterParties);
    }

    public void addToMap(Long bankAccountId, CounterParty counterParty) {
        counterPartyMap.computeIfAbsent(bankAccountId, k -> new ArrayList<>()).add(counterParty);
    }

    public void addListToMap(Long bankAccountId, List<CounterParty> counterParties) {
        counterPartyMap.computeIfAbsent(bankAccountId, k -> new ArrayList<>()).addAll(counterParties);
    }

    public List<CounterParty> findAllCounterPartiesOfBankAccount(Long bankAccountId) {
        return counterPartyMap.computeIfAbsent(bankAccountId, transactionService::findDistinctCounterPartiesByBankAccountId);
    }

    public void createOrUpdateCounterParty(List<Transaction> transactions) {
        // Get the bank account ID from the first transaction
        Long bankAccountId = transactions.getFirst().getBankAccount().getId();
        List<CounterParty> existingCounterParties = findAllCounterPartiesOfBankAccount(bankAccountId);

        // Create a map for quick lookup of existing counterparties by search strings
        Map<String, CounterParty> counterPartyMap = new HashMap<>();
        for (CounterParty existingCounterParty : existingCounterParties) {
            for (String searchString : existingCounterParty.getCounterPartySearchStrings()) {
                counterPartyMap.put(searchString, existingCounterParty);
            }
        }

        // List to hold new counterparties
        List<CounterParty> newCounterParties = new ArrayList<>();

        // Process each transaction
        for (Transaction transaction : transactions) {
            CounterParty transactionCounterParty = transaction.getCounterParty();
            CounterParty matchedCounterParty = counterPartyMap.get(transactionCounterParty.getName());

            if (matchedCounterParty != null) {
                // Assign existing counterparty to the transaction
                transaction.setCounterParty(matchedCounterParty);
            } else {
                // Add the new counterparty to the list
                counterPartyMap.put(transactionCounterParty.getName(), transactionCounterParty);
                newCounterParties.add(transactionCounterParty);
            }
        }

        // Add the new counterparties to the map
        addListToMap(bankAccountId, newCounterParties);
        saveAll(newCounterParties);
    }
}
