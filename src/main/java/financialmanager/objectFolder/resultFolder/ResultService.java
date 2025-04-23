package financialmanager.objectFolder.resultFolder;

import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BaseBankAccountService;
import financialmanager.objectFolder.categoryFolder.Category;
import financialmanager.objectFolder.categoryFolder.CategoryService;
import financialmanager.objectFolder.contractFolder.BaseContractService;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.BaseCounterPartyService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.BaseUsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ResultService {

    private final BaseTransactionService baseTransactionService;
    private final BaseContractService baseContractService;
    private final BaseCounterPartyService baseCounterPartyService;
    private final BaseUsersService baseUsersService;
    private final BaseBankAccountService baseBankAccountService;
    private final CategoryService categoryService;

    private final ResponseService responseService;
    
    private static final Logger log = LoggerFactory.getLogger(ResultService.class);

    //<editor-fold desc="Transaction">
    public Result<List<Transaction>, ResponseEntity<Response>> findTransactionsByIdInAndBankAccountId(Long bankAccountId, List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        List<Transaction> transactions = baseTransactionService.findByIdInAndBankAccount(transactionIds, bankAccountResult.getValue());

        if (transactions.isEmpty()) {
            log.warn("No transactions found for id: {}", bankAccountId);
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR));
        }


        return new Ok<>(transactions);
    }

    public Result<List<Transaction>, ResponseEntity<Response>> findTransactionsByIdInAndBankAccountIdAndHidden(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = findTransactionsByIdInAndBankAccountId(bankAccountId, transactionIds);

        if (transactionResult.isErr())
            return new Err<>(transactionResult.getError());

        List<Transaction> transactions = transactionResult.getValue().stream()
                .filter(transaction -> transaction.isHidden() != hide)
                .toList();

        if (transactions.isEmpty()) {
            log.warn("No transactions found to update for id: {}", bankAccountId);
            return new Err<>(responseService.createResponse(HttpStatus.CONFLICT, "noTransactionsUpdated", AlertType.INFO));
        }

        return new Ok<>(transactions);
    }
    //</editor-fold>

    //<editor-fold desc="Contract">
    public Result<Contract, ResponseEntity<Response>> findContractByIdAndBankAccountId(Long bankAccountId, Long contractId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        Contract contract = baseContractService.findByIdAndBankAccountId(contractId, bankAccountId);

        if (contract == null) {
            log.warn("No contract found for id: {}", bankAccountId);
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "contractNotFound", AlertType.ERROR));
        }

        return new Ok<>(contract);
    }

    public Result<List<Contract>, ResponseEntity<Response>> findContractsByIdInAndBankAccountId(Long bankAccountId, List<Long> contractIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        List<Contract> contracts = baseContractService.findByIdInAndBankAccountId(contractIds, bankAccountId);

        if (contracts.isEmpty()) {
            log.warn("No contracts found for id: {}", bankAccountId);
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "contractsNotFound", AlertType.ERROR));
        }

        return new Ok<>(contracts);
    }

    public List<Contract> findContractsByBankAccountIdBetweenDates(BankAccount bankAccount, LocalDate startDate, LocalDate endDate) {
        List<Contract> contracts = baseContractService.findByBankAccount(bankAccount);

        if (startDate == null && endDate == null) {
            return contracts;
        }

        LocalDate[] dates = Utils.getRightDateRange(startDate, endDate);
        LocalDate finalStartDate = dates[0];
        LocalDate finalEndDate = dates[1];

        // Filter contracts that fall within the date range (inclusive)
        return contracts.stream()
                .filter(contract -> !contract.getStartDate().isBefore(finalStartDate) && (contract.getEndDate() == null
                        || !contract.getEndDate().isAfter(finalEndDate)))
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Counter Party">
    public Result<List<CounterParty>, ResponseEntity<Response>> findCounterPartiesByIdInAndUsers(List<Long> counterPartyIds) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(currentUserResponse.getError());
        }

        Users currentUser = currentUserResponse.getValue();

        List<CounterParty> counterParties = baseCounterPartyService.findByIdInAndUsers(counterPartyIds, currentUser);

        if (counterParties.isEmpty()) {
            log.warn("Counter parties not found");
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "counterPartiesNotFound", AlertType.ERROR));
        }

        return new Ok<>(counterParties);
    }

    public Result<CounterParty, ResponseEntity<Response>> findCounterPartyById(Long counterPartyId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(currentUserResponse.getError());
        }

        Users currentUser = currentUserResponse.getValue();

        CounterParty counterParty = baseCounterPartyService.findByIdAndUsers(counterPartyId, currentUser);

        if (counterParty != null) {
            return new Ok<>(counterParty);
        } else {
            log.warn("User {} does not own the counter party {}", currentUser, counterPartyId);
            ResponseEntity<Response> errorResponse = responseService.createResponse(
                    HttpStatus.NOT_FOUND, "counterPartyNotFound", AlertType.ERROR
            );
            return new Err<>(errorResponse);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Users">
    public Result<Users, ResponseEntity<Response>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            Optional<Users> usersOptional = baseUsersService.findByEmail(username);

            if (usersOptional.isPresent()) {
                return new Ok<>(usersOptional.get());
            }

            log.error("Current User not found, email: {}", username);
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(AlertType.ERROR, "User not found", null)));
        } catch (Exception e) {
            log.error("Error while getting current user", e);
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(AlertType.ERROR, "Internal server error", null)));
        }
    }

    //</editor-fold>

    //<editor-fold desc="Bank Account">
    public Result<BankAccount, ResponseEntity<Response>> findBankAccountById(Long bankAccountId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(currentUserResponse.getError().getBody()));
        }

        Users currentUser = currentUserResponse.getValue();

        BankAccount bankAccount = baseBankAccountService.findByIdAndUsers(bankAccountId, currentUser);

        if (bankAccount == null) {
            log.warn("User {} does not own the bank account {}", currentUser, bankAccountId);
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR));
        }

        return new Ok<>(bankAccount);
    }

    public Result<List<BankAccount>, ResponseEntity<Response>> findBankAccountsByUsers() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(currentUserResponse.getError().getBody()));
        }

        Users currentUser = currentUserResponse.getValue();

        List<BankAccount> bankAccounts = baseBankAccountService.findAllByUsers(currentUser);

        return new Ok<>(bankAccounts);
    }

    //</editor-fold>

    //<editor-fold desc="Category">
    public Result<List<Category>, ResponseEntity<Response>> findCategoriesByUsers() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(currentUserResponse.getError().getBody()));
        }

        return new Ok<>(categoryService.findAllByUsers(currentUserResponse.getValue()));
    }


    //</editor-fold>
}
