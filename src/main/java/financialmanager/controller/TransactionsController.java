package financialmanager.controller;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("transactions/{bankAccountId}/data")
public class TransactionsController {

    private final TransactionService transactionService;
    private final BankAccountService bankAccountService;
    private final UsersService usersService;
    private final ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<?> getTransactionsForBankAccount(@PathVariable Long bankAccountId) {
        Users currentUser = usersService.getCurrentUser();
        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, currentUser);

        if (bankAccountOptional.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR);
        }

        return ResponseEntity.ok(transactionService.findByBankAccountId(bankAccountId));
    }

    @PostMapping("/hideTransactions")
    public ResponseEntity<?> hideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> ids) {
        return updateTransactionVisibility(bankAccountId, ids, true);
    }

    @PostMapping("/unHideTransactions")
    public ResponseEntity<?> unHideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> ids) {
        return updateTransactionVisibility(bankAccountId, ids, false);
    }

    private ResponseEntity<?> updateTransactionVisibility(Long bankAccountId, List<Long> ids, boolean hide) {
        Users currentUser = usersService.getCurrentUser();

        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, currentUser);
        if (bankAccountOptional.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR);
        }

        BankAccount bankAccount = bankAccountOptional.get();
        List<Transaction> transactions = transactionService.findAllByListOfId(ids).stream()
                .filter(transaction -> transaction.getBankAccount().equals(bankAccount) &&
                        transaction.isHidden() != hide)
                .toList();

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.CONFLICT, "noTransactionsUpdated", AlertType.INFO);
        }

        transactions.forEach(transaction -> transaction.setHidden(hide));
        transactionService.saveAll(transactions);

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "transactionsHidden" : "transactionsUnhidden",
                AlertType.SUCCESS, List.of(String.valueOf(ids.size())));
    }
}
