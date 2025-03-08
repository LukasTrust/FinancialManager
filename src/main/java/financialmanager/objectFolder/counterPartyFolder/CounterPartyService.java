package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Ok;
import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CounterPartyService {

    private final CounterPartyRepository counterPartyRepository;
    private final ResponseService responseService;
    private final UsersService usersService;
    private final TransactionService transactionService;
    private final ContractService contractService;

    private static final Logger log = LoggerFactory.getLogger(CounterPartyService.class);

    public void saveAll(List<CounterParty> counterParties) {
        counterPartyRepository.saveAll(counterParties);
    }

    public void save(CounterParty counterParty) {
        counterPartyRepository.save(counterParty);
    }

    public List<CounterParty> findByUsers(Users user) {
        return counterPartyRepository.findByUsers(user);
    }

    public List<CounterParty> findByIdInAndUsers(List<Long> counterPartyIds, Users user) {
        return counterPartyRepository.findByIdInAndUsers(counterPartyIds, user);
    }

    public Result<CounterParty, ResponseEntity<Response>> findByIdAndUsers(Long counterPartyId, Users currentUser) {
        Optional<CounterParty> counterPartyOptional = counterPartyRepository.findByIdAndUsers(counterPartyId, currentUser);

        if (counterPartyOptional.isPresent()) {
            return new Ok<>(counterPartyOptional.get());
        }

        log.warn("User {} does not own the bank account {}", currentUser, counterPartyId);

        return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "counterPartyNotFound", AlertType.ERROR));
    }

    public ResponseEntity<?> updateCounterPartyVisibility(List<Long> counterPartyIds, boolean hide) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        List<CounterParty> counterParties = findByIdInAndUsers(counterPartyIds, currentUser);

        if (counterParties.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "counterPartiesNotFound", AlertType.ERROR);
        }

        List<Transaction> transactions = transactionService.findByCounterPartyIn(counterParties);

        transactions.forEach(transaction -> transaction.setHidden(hide));

        counterParties.forEach(counterParty -> counterParty.setHidden(hide));

        transactionService.saveAll(transactions);
        saveAll(counterParties);

        int updatedTransactions = transactions.size();

        List<String> placeHolder = new ArrayList<>();
        placeHolder.add(String.valueOf(counterParties.size()));
        placeHolder.add(String.valueOf(updatedTransactions));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "counterPartiesHidden" : "counterPartiesUnHidden",
                AlertType.SUCCESS, placeHolder);
    }

    public ResponseEntity<?> removeSearchStringFromCounterParty(Long counterPartyId, String searchString) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = getCounterParty(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        List<String> searchStrings = counterParty.getCounterPartySearchStrings();

        // Ensure search string removal is allowed
        if (searchStrings.size() == 1) {
            return responseService.createResponse(
                    HttpStatus.BAD_REQUEST, "searchStringCanNotBeRemovedFromCounterParty", AlertType.WARNING);
        }

        // Attempt to remove the search string
        if (!searchStrings.remove(searchString)) {
            return responseService.createResponseWithPlaceHolders(
                    HttpStatus.NOT_FOUND, "searchStringNotFoundInCounterParty", AlertType.ERROR, List.of(searchString));
        }

        CounterParty splitCounterParty = changeCounterPartyOfTransactions(counterParty.getUsers(), searchString);

        if (splitCounterParty == null) {
            // Return success response without new counterParty
            return responseService.createResponse(
                    HttpStatus.OK, "removedSearchStringFromCounterParty", AlertType.SUCCESS);
        }

        // Return success response with new counterParty
        return responseService.createResponseWithData(
                HttpStatus.OK, "removedSearchStringFromCounterParty", AlertType.SUCCESS, splitCounterParty);
    }

    public ResponseEntity<?> getCounterPartyDisplays() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        List<CounterPartyDisplay> counterPartyDisplays = createCounterPartyDisplays(currentUser);

        return ResponseEntity.ok(counterPartyDisplays);
    }

    public ResponseEntity<?> updateCounterPartyField(Long counterPartyId, String newValue,
                                                      BiConsumer<CounterParty, String> fieldUpdater) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = getCounterParty(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        fieldUpdater.accept(counterParty, newValue);

        save(counterParty);

        return ResponseEntity.ok().build();
    }

    public Result<CounterParty, ResponseEntity<Response>> getCounterParty(Long counterPartyId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(currentUserResponse.getError());
        }

        Users currentUser = currentUserResponse.getValue();

        return findByIdAndUsers(counterPartyId, currentUser);
    }

    private CounterParty changeCounterPartyOfTransactions(Users currentUser, String counterPartyName) {
        List<Transaction> transactions = transactionService.findByOriginalCounterParty(counterPartyName);

        if (transactions.isEmpty()) {
            return null;
        }

        CounterParty counterParty = new CounterParty(currentUser, counterPartyName);
        setCounterParty(counterParty, transactions);

        transactionService.saveAll(transactions);

        return counterParty;
    }

    private void setCounterParty(CounterParty counterParty, List<Transaction> transactions) {
        transactions.forEach(transaction -> {
            transaction.setCounterParty(counterParty);
        });
    }

    private List<CounterPartyDisplay> createCounterPartyDisplays(Users users) {
        List<CounterPartyDisplay> counterPartyDisplays = new ArrayList<>();

        List<CounterParty> counterParties = findByUsers(users);

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

    public void setCounterCounterParties(Users currentUser, List<Transaction> transactions) {
        List<CounterParty> existingCounterParties = findByUsers(currentUser);
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
            saveAll(newCounterParties);
        }
    }
}
