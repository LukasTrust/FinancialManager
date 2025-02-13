package financialmanager.controller;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("transactions")
public class TransactionsController {

    private final TransactionService transactionService;
    private final BankAccountService bankAccountService;
    private final UsersService usersService;
    private final ResponseService responseService;

    @GetMapping("/{bankAccountId}/data")
    public ResponseEntity<?> getTransactionsForBankAccount(@PathVariable Long bankAccountId) {
        Users currentUser = usersService.getCurrentUser();
        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, currentUser);

        if (bankAccountOptional.isPresent()) {
            return ResponseEntity.ok(transactionService.findByBankAccountId(bankAccountId));
        }

        return responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR);
    }
}
