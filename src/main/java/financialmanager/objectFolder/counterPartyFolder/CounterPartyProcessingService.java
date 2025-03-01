package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CounterPartyProcessingService {

    private final CounterPartyService counterPartyService;
    private final TransactionService transactionService;
    private final ContractService contractService;

    public List<CounterPartyDisplay> createCounterPartyDisplays(Users users) {
        List<CounterPartyDisplay> counterPartyDisplays = new ArrayList<>();

        List<CounterParty> counterParties = counterPartyService.findByUsers(users);

        for (CounterParty counterParty : counterParties) {
            List<Transaction> transactions = transactionService.findByCounterParty(counterParty);
            List<Contract> contracts = contractService.findByCounterParty(counterParty);

            Double totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();
            Integer numberOfContracts = contracts.size();

            CounterPartyDisplay counterPartyDisplay = new CounterPartyDisplay(counterParty, numberOfContracts, totalAmount);
            counterPartyDisplays.add(counterPartyDisplay);
        }

        return counterPartyDisplays;
    }

    public void setCounterCounterParties(Users users, List<Transaction> transactions) {
        List<CounterParty> existingCounterParties = counterPartyService.findByUsers(users);
        Map<String, CounterParty> counterPartyLookup = new HashMap<>();

        // Populate lookup for quick search
        for (CounterParty counterParty : existingCounterParties) {
            for (String searchString : counterParty.getCounterPartySearchStrings()) {
                counterPartyLookup.put(searchString, counterParty);
            }
        }

        List<CounterParty> newCounterParties = new ArrayList<>();

        // Group transactions by counterparty
        Map<String, List<Transaction>> transactionsByCounterParty = transactions.stream()
                .collect(Collectors.groupingBy(financialmanager.objectFolder.transactionFolder.Transaction::getOriginalCounterParty));

        for (Map.Entry<String, List<Transaction>> entry : transactionsByCounterParty.entrySet()) {
            String counterPartyName = entry.getKey();
            List<Transaction> counterPartyTransactions = entry.getValue();

            CounterParty counterParty = counterPartyLookup.get(counterPartyName);
            CounterParty finalCounterParty;

            if (counterParty != null) {
                // If existing counterparty found, assign it to transactions
                finalCounterParty = counterParty;
                counterPartyTransactions.forEach(transaction -> transaction.setCounterParty(finalCounterParty));
            } else {
                // If not found, create a new one
                counterParty = new CounterParty(users, counterPartyName);
                finalCounterParty = counterParty;
                counterPartyTransactions.forEach(transaction -> transaction.setCounterParty(finalCounterParty));
                newCounterParties.add(counterParty);
            }
        }

        // Save new counterparties if needed
        if (!newCounterParties.isEmpty()) {
            counterPartyService.saveAll(newCounterParties);
        }
    }
}
