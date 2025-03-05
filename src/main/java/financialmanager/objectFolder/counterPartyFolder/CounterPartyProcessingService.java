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

            Integer transactionCount = transactions.size();
            Integer numberOfContracts = contracts.size();
            Double totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();

            CounterPartyDisplay counterPartyDisplay = new CounterPartyDisplay(counterParty, transactionCount, numberOfContracts, totalAmount);
            counterPartyDisplays.add(counterPartyDisplay);
        }

        return counterPartyDisplays;
    }

    public CounterParty changeCounterPartyOfTransactions(Users currentUser, String counterPartyName) {
        List<Transaction> transactions = transactionService.findByOriginalCounterParty(counterPartyName);

        if (transactions.isEmpty()) {
            return null;
        }

        CounterParty counterParty = new CounterParty(currentUser, counterPartyName);
        setCounterParty(counterParty, transactions);

        transactionService.saveAll(transactions);

        return counterParty;
    }

    public void setCounterCounterParties(Users currentUser, List<Transaction> transactions) {
        List<CounterParty> existingCounterParties = counterPartyService.findByUsers(currentUser);
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

            if (counterParty != null) {
                // If existing counterparty found, assign it to transactions
                setCounterParty(counterParty, counterPartyTransactions);
            } else {
                // If not found, create a new one
                counterParty = new CounterParty(currentUser, counterPartyName);
                setCounterParty(counterParty, counterPartyTransactions);
                newCounterParties.add(counterParty);
            }
        }

        // Save new counterparties if needed
        if (!newCounterParties.isEmpty()) {
            counterPartyService.saveAll(newCounterParties);
        }
    }

    private void setCounterParty(CounterParty counterParty, List<Transaction> transactions) {
        transactions.forEach(transaction -> {transaction.setCounterParty(counterParty);});
    }
}
