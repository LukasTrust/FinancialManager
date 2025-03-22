package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.contractFolder.BaseContractService;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CounterPartyService {

    //<editor-fold desc="properties">
    private final BaseCounterPartyService baseCounterPartyService;
    private final BaseTransactionService baseTransactionService;
    private final BaseContractService baseContractService;

    private final ResponseService responseService;
    private final ResultService resultService;

    private static final Logger log = LoggerFactory.getLogger(CounterPartyService.class);
    //</editor-fold>

    //<editor-fold desc="find functions">
    public ResponseEntity<?> findCounterPartyDisplaysAsResponse() {
        Result<Users, ResponseEntity<Response>> currentUserResult = resultService.getCurrentUser();

        if (currentUserResult.isErr())
            return currentUserResult.getError();

        List<CounterPartyDisplay> counterPartyDisplays = new ArrayList<>();

        List<CounterParty> counterParties = baseCounterPartyService.findByUsers(currentUserResult.getValue());

        for (CounterParty counterParty : counterParties) {
            CounterPartyDisplay counterPartyDisplay = createCounterPartyDisplay(counterParty);
            counterPartyDisplays.add(counterPartyDisplay);
        }

        return ResponseEntity.ok(counterPartyDisplays);
    }

    private CounterPartyDisplay createCounterPartyDisplay(CounterParty counterParty) {
        List<Transaction> transactions = baseTransactionService.findByCounterParty(counterParty);
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
        Result<CounterParty, ResponseEntity<Response>> headerResult = resultService.findCounterPartyById(headerId);
        if (headerResult.isErr()) {
            log.error("Error loading the header counter party, id: {}", headerId);
            return headerResult.getError();
        }

        Result<List<CounterParty>, ResponseEntity<Response>> counterPartiesResult = resultService.findCounterPartiesByIdInAndUsers(counterPartyIds);
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
        List<Transaction> transactions = baseTransactionService.findByCounterPartyIn(mergingCounterParties);

        int contractCount = setCounterParty(targetCounterParty, transactions);

        // Merge unique search strings
        Set<String> mergedSearchStrings = mergingCounterParties.stream()
                .flatMap(cp -> cp.getCounterPartySearchStrings().stream())
                .collect(Collectors.toSet());

        mergedSearchStrings.addAll(targetCounterParty.getCounterPartySearchStrings());
        targetCounterParty.setCounterPartySearchStrings(new ArrayList<>(mergedSearchStrings));

        baseCounterPartyService.deleteAll(mergingCounterParties);

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
        Result<List<CounterParty>, ResponseEntity<Response>> counterPartiesResult = resultService.findCounterPartiesByIdInAndUsers(counterPartyIds);
        if (counterPartiesResult.isErr())
            return counterPartiesResult.getError();

        List<CounterParty> counterParties = counterPartiesResult.getValue();

        List<String> updatedData = updateVisibilityForCounterParties(counterParties, hide);
        return responseService.createResponseWithPlaceHolders(
                HttpStatus.OK, hide ? "counterPartiesHidden" : "counterPartiesUnHidden",
                AlertType.SUCCESS, updatedData
        );
    }

    private List<String> updateVisibilityForCounterParties(List<CounterParty> counterParties, boolean hide) {
        List<Transaction> transactions = baseTransactionService.findByCounterPartyIn(counterParties);
        List<Contract> contracts = getContractsFromTransactions(transactions);

        baseTransactionService.setHidden(hide, transactions);
        baseContractService.setHidden(hide, contracts);
        baseCounterPartyService.setHidden(hide, counterParties);

        return List.of(
                String.valueOf(counterParties.size()),
                String.valueOf(transactions.size()),
                String.valueOf(contracts.size())
        );
    }
    //</editor-fold>

    //<editor-fold desc="change searchString functions">
    public ResponseEntity<Response> addSearchStringToCounterParty(Long counterPartyId, String searchString) {
        if (baseCounterPartyService.existsByCounterPartySearchStringsContaining(searchString)) {
            log.warn("Counter parties contains search string {}", searchString);
            return responseService.createResponse(HttpStatus.CONFLICT, "counterPartySearchStringAlreadyInCounterParty", AlertType.ERROR);
        }

        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = resultService.findCounterPartyById(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        counterParty.getCounterPartySearchStrings().add(searchString);
        baseCounterPartyService.save(counterParty);

        return responseService.createResponse(HttpStatus.OK, "addedSearchStringToCounterParty", AlertType.SUCCESS);
    }

    public ResponseEntity<Response> removeSearchStringFromCounterParty(Long counterPartyId, String searchString) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = resultService.findCounterPartyById(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        List<String> searchStrings = counterParty.getCounterPartySearchStrings();

        if (!searchStrings.contains(searchString)) {
            log.error("Counter parties contains search string {}", searchString);
            return responseService.createResponse(
                    HttpStatus.BAD_REQUEST, "searchStringNotInCounterParty", AlertType.ERROR);
        }

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

        List<CounterPartyDisplay> counterPartyDisplays = new ArrayList<>();

        if (!checkIfOtherSearchStringsAreInUse(searchStrings)) {
            // No other search strings are in use, reset list and add back searchString
            searchStrings = new ArrayList<>();
            searchStrings.add(searchString);

            counterParty.setCounterPartySearchStrings(searchStrings);

            // Ensure the updated counterparty is added first
            CounterPartyDisplay updatedDisplay = createCounterPartyDisplay(counterParty);
            counterPartyDisplays.add(updatedDisplay);
        } else {
            // Updated counterparty should be first
            counterParty.setCounterPartySearchStrings(searchStrings);

            CounterParty splitCounterParty = createNewCounterPartyForTransactions(counterParty.getUsers(), searchString);

            CounterPartyDisplay updatedDisplay = createCounterPartyDisplay(counterParty);
            counterPartyDisplays.add(updatedDisplay);

            // Create and add the split counterparty second
            if (splitCounterParty != null) {
                CounterPartyDisplay splitDisplay = createCounterPartyDisplay(splitCounterParty);
                counterPartyDisplays.add(splitDisplay);
            }
        }

        baseCounterPartyService.save(counterParty);

        return responseService.createResponseWithData(HttpStatus.OK, "removedSearchStringFromCounterParty", AlertType.SUCCESS, counterPartyDisplays);
    }
    //</editor-fold>

    //<editor-fold desc="edit counterParty">
    public ResponseEntity<Response> updateCounterPartyField(Long counterPartyId, Map<String, String> requestBody,
                                                            BiConsumer<CounterParty, String> fieldUpdater) {
        String newValue = requestBody.get("newValue");

        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = resultService.findCounterPartyById(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        fieldUpdater.accept(counterParty, newValue);

        baseCounterPartyService.save(counterParty);

        return ResponseEntity.ok().build();
    }
    //</editor-fold>

    //<editor-fold desc="edit transactions">
    public void setCounterPartyForNewTransactions(Users currentUser, List<Transaction> transactions) {
        List<CounterParty> existingCounterParties = baseCounterPartyService.findByUsers(currentUser);
        Map<String, CounterParty> counterPartyLookup = new HashMap<>();

        // Build lookup map
        for (CounterParty counterParty : existingCounterParties) {
            for (String searchString : counterParty.getCounterPartySearchStrings()) {
                counterPartyLookup.putIfAbsent(searchString, counterParty);
            }
        }

        List<CounterParty> newCounterParties = new ArrayList<>();

        // Group transactions by counterparty name
        Map<String, List<Transaction>> transactionsByCounterParty = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::getOriginalCounterParty));

        for (Map.Entry<String, List<Transaction>> entry : transactionsByCounterParty.entrySet()) {
            String counterPartyName = entry.getKey();
            List<Transaction> counterPartyTransactions = entry.getValue();

            // Use computeIfAbsent to avoid extra containsKey calls
            CounterParty counterParty = counterPartyLookup.computeIfAbsent(counterPartyName, name -> {
                CounterParty newCP = new CounterParty(currentUser, name);
                newCounterParties.add(newCP);
                return newCP;
            });

            baseTransactionService.setCounterParty(counterParty, counterPartyTransactions, false);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // Save new counterparties only once
        if (!newCounterParties.isEmpty()) {
            baseCounterPartyService.saveAll(newCounterParties);
        }
        stopWatch.stop();
        log.info("{} for saveAll", stopWatch.getTotalTimeMillis());
    }

    private boolean checkIfOtherSearchStringsAreInUse(List<String> searchStrings) {
        for (String searchString : searchStrings) {
            if (!baseTransactionService.findByOriginalCounterParty(searchString).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private CounterParty createNewCounterPartyForTransactions(Users currentUser, String originalCounterParty) {
        List<Transaction> transactions = baseTransactionService.findByOriginalCounterParty(originalCounterParty);
        if (transactions.isEmpty()) return null;

        CounterParty splitCounterParty = new CounterParty(currentUser, originalCounterParty);
        baseCounterPartyService.save(splitCounterParty);
        setCounterParty(splitCounterParty, transactions);

        return splitCounterParty;
    }

    private int setCounterParty(CounterParty counterParty, List<Transaction> transactions) {
        if (transactions.isEmpty()) return 0;

        List<Contract> uniqueContracts = getContractsFromTransactions(transactions);

        baseTransactionService.setCounterParty(counterParty, transactions, true);
        baseContractService.setCounterParty(counterParty, uniqueContracts, true);

        return uniqueContracts.size();
    }
    //</editor-fold>
    //</editor-fold>
}