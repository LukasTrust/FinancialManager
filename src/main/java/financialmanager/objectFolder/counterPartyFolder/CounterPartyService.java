package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.resultFolder.Err;
import financialmanager.objectFolder.resultFolder.Ok;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.contractFolder.Contract;
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

    //<editor-fold desc="properties">
    private final CounterPartyRepository counterPartyRepository;
    private final ResponseService responseService;
    private final UsersService usersService;
    private final TransactionService transactionService;

    private static final Logger log = LoggerFactory.getLogger(CounterPartyService.class);
    //</editor-fold>

    //<editor-fold desc="repository functions">
    public void saveAll(List<CounterParty> counterParties) {
        counterPartyRepository.saveAll(counterParties);
    }

    public void save(CounterParty counterParty) {
        counterPartyRepository.save(counterParty);
    }

    public List<CounterParty> findByUsers(Users user) {
        return counterPartyRepository.findByUsers(user);
    }

    public void deleteAll(List<CounterParty> counterParties) {
        counterPartyRepository.deleteAll(counterParties);
    }

    public boolean existsByCounterPartySearchStringsContaining(String searchString) {
        return counterPartyRepository.existsByCounterPartySearchStringsContaining(searchString);
    }
    //</editor-fold>

    //<editor-fold desc="find functions">
    public Result<List<CounterParty>, ResponseEntity<Response>> findByIdInAndUsers(List<Long> counterPartyIds, Users user) {
        List<CounterParty> counterParties = counterPartyRepository.findByIdInAndUsers(counterPartyIds, user);

        if (counterParties.isEmpty()) {
            log.warn("Counter parties not found");
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "counterPartiesNotFound", AlertType.ERROR));
        }

        return new Ok<>(counterParties);
    }

    public Result<CounterParty, ResponseEntity<Response>> findByIdAndUsers(Long counterPartyId, Users currentUser) {
        Optional<CounterParty> counterPartyOpt = counterPartyRepository.findByIdAndUsers(counterPartyId, currentUser);

        if (counterPartyOpt.isPresent()) {
            CounterParty counterParty = counterPartyOpt.get();
            return new Ok<>(counterParty);
        } else {
            log.warn("User {} does not own the bank account {}", currentUser, counterPartyId);
            ResponseEntity<Response> errorResponse = responseService.createResponse(
                    HttpStatus.NOT_FOUND, "counterPartyNotFound", AlertType.ERROR
            );
            return new Err<>(errorResponse);
        }
    }

    //</editor-fold>

    //<editor-fold desc="get functions">
    public ResponseEntity<?> getCounterPartyDisplays() {
        return usersService.getCurrentUser()
                .map(this::createCounterPartyDisplay)
                .map(counterPartyDisplays -> ResponseEntity.ok((Object) counterPartyDisplays))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(usersService.getCurrentUser().getError()));
    }

    public Result<CounterParty, ResponseEntity<Response>> getCounterParty(Long counterPartyId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(currentUserResponse.getError());
        }

        Users currentUser = currentUserResponse.getValue();

        return findByIdAndUsers(counterPartyId, currentUser);
    }

    private List<CounterPartyDisplay> createCounterPartyDisplay(Users users) {
        List<CounterPartyDisplay> counterPartyDisplays = new ArrayList<>();

        List<CounterParty> counterParties = findByUsers(users);

        for (CounterParty counterParty : counterParties) {
            CounterPartyDisplay counterPartyDisplay = createCounterPartyDisplay(counterParty);
            counterPartyDisplays.add(counterPartyDisplay);
        }

        return counterPartyDisplays;
    }

    private CounterPartyDisplay createCounterPartyDisplay(CounterParty counterParty) {
        List<Transaction> transactions = transactionService.findByCounterParty(counterParty);
        List<Contract> contracts = getContractsFromTransactions(transactions);

        Integer transactionCount = transactions.size();
        Integer numberOfContracts = contracts.size();
        Double totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();

        return new CounterPartyDisplay(counterParty, transactionCount, numberOfContracts, totalAmount);
    }

    private List<Contract> getContractsFromTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getContract)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .stream()
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="merge functions">
    public ResponseEntity<Response> mergeCounterParties(Long headerId, List<Long> counterPartyIds) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();
        if (currentUserResponse.isErr()) return currentUserResponse.getError();

        Users currentUser = currentUserResponse.getValue();

        Result<CounterParty, ResponseEntity<Response>> headerResult = findByIdAndUsers(headerId, currentUser);
        if (headerResult.isErr()) {
            log.error("Error loading the header counter party, id: {}", headerId);
            return headerResult.getError();
        }

        Result<List<CounterParty>, ResponseEntity<Response>> counterPartiesResult = findByIdInAndUsers(counterPartyIds, currentUser);
        if (counterPartiesResult.isErr()) {
            log.error("Error loading the counter parties, ids: {}", counterPartyIds);
            return counterPartiesResult.getError();
        }

        CounterParty headerCounterParty = headerResult.getValue();
        List<String> updatedData = mergeCounterPartiesIntoHeader(counterPartiesResult.getValue(), headerCounterParty);

        CounterPartyDisplay counterPartyDisplay = createCounterPartyDisplay(headerCounterParty);

        return responseService.createResponseWithDataAndPlaceHolders(HttpStatus.OK, "counterPartiesMerged", AlertType.SUCCESS, counterPartyDisplay, updatedData);
    }

    private List<String> mergeCounterPartiesIntoHeader(List<CounterParty> mergingCounterParties, CounterParty targetCounterParty) {
        List<Transaction> transactions = transactionService.findByCounterPartyIn(mergingCounterParties);

        int contractCount = setCounterParty(targetCounterParty, transactions);
        transactionService.saveAll(transactions);

        // Merge unique search strings
        Set<String> mergedSearchStrings = mergingCounterParties.stream()
                .flatMap(cp -> cp.getCounterPartySearchStrings().stream())
                .collect(Collectors.toSet());

        mergedSearchStrings.addAll(targetCounterParty.getCounterPartySearchStrings());
        targetCounterParty.setCounterPartySearchStrings(new ArrayList<>(mergedSearchStrings));

        deleteAll(mergingCounterParties);

        return List.of(
                String.valueOf(mergingCounterParties.size()),
                String.valueOf(transactions.size()),
                String.valueOf(contractCount)
        );
    }
    //</editor-fold>

    //<editor-fold desc="update functions">
    //<editor-fold desc="visibility functions">
    public ResponseEntity<Response> updateCounterPartyVisibility(List<Long> counterPartyIds, boolean hide) {
        return usersService.getCurrentUser()
                .flatMap(user -> findByIdInAndUsers(counterPartyIds, user))
                .map(counterParties -> {
                    List<String> updatedData = updateVisibilityForCounterParties(counterParties, hide);
                    return responseService.createResponseWithPlaceHolders(
                            HttpStatus.OK, hide ? "counterPartiesHidden" : "counterPartiesUnHidden",
                            AlertType.SUCCESS, updatedData
                    );
                })
                .orElseGet(usersService.getCurrentUser()::getError);
    }

    private List<String> updateVisibilityForCounterParties(List<CounterParty> counterParties, boolean isHidden) {
        List<Transaction> transactions = transactionService.findByCounterPartyIn(counterParties);
        List<Contract> contracts = getContractsFromTransactions(transactions);

        transactions.forEach(transaction -> transaction.setHidden(isHidden));
        contracts.forEach(contract -> contract.setHidden(isHidden));
        counterParties.forEach(counterParty -> counterParty.setHidden(isHidden));

        transactionService.saveAll(transactions);

        return List.of(
                String.valueOf(counterParties.size()),
                String.valueOf(transactions.size()),
                String.valueOf(contracts.size())
        );
    }
    //</editor-fold>

    //<editor-fold desc="change searchString functions">
    public ResponseEntity<Response> addSearchStringToCounterParty(Long counterPartyId, String searchString) {
        if (existsByCounterPartySearchStringsContaining(searchString)) {
            log.warn("Counter parties contains search string {}", searchString);
            return responseService.createResponse(HttpStatus.CONFLICT, "counterPartySearchStringAlreadyInCounterParty", AlertType.ERROR);
        }

        return getCounterParty(counterPartyId)
                .map(counterParty -> {
                    counterParty.getCounterPartySearchStrings().add(searchString);
                    save(counterParty);
                    return responseService.createResponse(HttpStatus.OK, "addedSearchStringToCounterParty", AlertType.SUCCESS);
                })
                .getError();
    }

    public ResponseEntity<Response> removeSearchStringFromCounterParty(Long counterPartyId, String searchString) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = getCounterParty(counterPartyId);

        if (counterPartyResult.isErr()){
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        List<String> searchStrings = counterParty.getCounterPartySearchStrings();

        if (searchStrings.size() == 1) {
            log.warn("Search string list size is 1, can not remove search string {} from id: {}", searchString, counterPartyId);
            return responseService.createResponse(
                    HttpStatus.BAD_REQUEST, "searchStringCanNotBeRemovedFromCounterParty", AlertType.WARNING);
        }

        if (!searchStrings.remove(searchString)) {
            log.error("Error while removing search string {} from id: {}", searchString, counterPartyId);
            return responseService.createResponseWithPlaceHolders(
                    HttpStatus.NOT_FOUND, "searchStringNotFoundInCounterParty", AlertType.ERROR, List.of(searchString));
        }

        CounterParty splitCounterParty = changeCounterPartyOfTransactions(counterParty.getUsers(), searchString);
        CounterPartyDisplay counterPartyDisplay = createCounterPartyDisplay(splitCounterParty);

        return responseService.createResponseWithData(HttpStatus.OK, "removedSearchStringFromCounterParty", AlertType.SUCCESS, counterPartyDisplay);
    }
    //</editor-fold>

    //<editor-fold desc="edit counterParty">
    public ResponseEntity<Response> updateCounterPartyField(Long counterPartyId, String newValue,
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
    //</editor-fold>

    //<editor-fold desc="edit transactions">
    public void setCounterPartyForNewTransactions(Users currentUser, List<Transaction> transactions) {
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

    private CounterParty changeCounterPartyOfTransactions(Users currentUser, String counterPartyName) {
        List<Transaction> transactions = transactionService.findByOriginalCounterParty(counterPartyName);
        if (transactions.isEmpty()) return null;

        CounterParty counterParty = new CounterParty(currentUser, counterPartyName);
        setCounterParty(counterParty, transactions);

        save(counterParty);

        transactionService.saveAll(transactions);

        return counterParty;
    }

    private int setCounterParty(CounterParty counterParty, List<Transaction> transactions) {
        if (transactions.isEmpty()) return 0;

        transactions.forEach(transaction -> transaction.setCounterParty(counterParty));

        List<Contract> uniqueContracts = getContractsFromTransactions(transactions);
        uniqueContracts.forEach(contract -> contract.setCounterParty(counterParty));

        return uniqueContracts.size();
    }
    //</editor-fold>
    //</editor-fold>
}