package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.controller.LocaleController;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class BankAccountController {

    private final String subDirectory = "addBankAccount";
    private final UsersService usersService;
    private final BankAccountService bankAccountService;
    private final LocaleController localeController;

    @GetMapping("/getBankAccountsOfUser")
    public ResponseEntity<?> getBankAccountsOfUser() {
        Users user = usersService.getCurrentUser();

        List<BankAccount> bankAccounts = bankAccountService.findAllByUsers(user);

        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/bankAccountOverview/{bankAccountId}/data")
    public ResponseEntity<?> getBankAccountById(@PathVariable Long bankAccountId) {
        Users user = usersService.getCurrentUser();

        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, user);
        if (bankAccountOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    AlertType.ERROR,
                    localeController.getMessage(subDirectory, "error_bankNotFound", user)
            ));
        }

        BankAccount bankAccount = bankAccountOptional.get();

        return ResponseEntity.ok(bankAccount);
    }

    @PostMapping(value = "/addBankAccount", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createBankAccount(@RequestBody BankAccount bankAccount) {
        Users user = usersService.getCurrentUser();

        // Set the associated user
        bankAccount.setUsers(user);

        try {
            BankAccount savedBankAccount = bankAccountService.save(bankAccount);

            return ResponseEntity.ok(new Response(
                    AlertType.SUCCESS,
                    localeController.getMessage(subDirectory, "success_bankAccountCreated", user),
                    savedBankAccount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    AlertType.ERROR,
                    localeController.getMessage(subDirectory, "error_failedCreateBankAccount", user)
            ));
        }
    }
}
