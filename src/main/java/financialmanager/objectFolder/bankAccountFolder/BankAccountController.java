package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.generalController.LocalizationController;
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
    private final LocalizationController localizationController;

    @GetMapping("/getBankAccountsOfUser")
    public ResponseEntity<?> getBankAccountsOfUser(Locale locale) {
        Optional<Users> userOptional = usersService.getCurrentUser();
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "error_userNotFound", locale)
            ));
        }

        List<BankAccount> bankAccounts = bankAccountService.findAllByUsers(userOptional.get());

        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/bankAccount/{bankAccountId}/data")
    public ResponseEntity<?> getBankAccountById(@PathVariable Long bankAccountId, Locale locale) {
        Optional<Users> userOptional = usersService.getCurrentUser();
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "error_userNotFound", locale)
            ));
        }

        Optional<BankAccount> bankAccountOptional = bankAccountService.findById(bankAccountId);
        if (bankAccountOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "error_bankNotFound", locale)
            ));
        }

        Users users = userOptional.get();
        BankAccount bankAccount = bankAccountOptional.get();

        if (!bankAccount.getUsers().equals(users)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT).body(
                            new Response(
                                    AlertType.ERROR,
                                    localizationController.getMessage(subDirectory, "error_bankDoesNotBelongToUser", locale)
                            ));
        }

        return ResponseEntity.ok(bankAccount);
    }

    @PostMapping(value = "/addBankAccount", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createBankAccount(@RequestBody BankAccount bankAccount, Locale locale) {
        Optional<Users> userOptional = usersService.getCurrentUser();
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "error_userNotFound", locale)
            ));
        }

        // Set the associated user
        bankAccount.setUsers(userOptional.get());

        try {
            BankAccount savedBankAccount = bankAccountService.save(bankAccount);

            return ResponseEntity.ok(new Response(
                    AlertType.SUCCESS,
                    localizationController.getMessage(subDirectory, "success_bankAccountCreated", locale),
                    savedBankAccount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "error_failedCreateBankAccount", locale)
            ));
        }
    }
}
