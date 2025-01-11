package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.locale.LocaleController;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
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
public class BankAccountController {

    private final String SUB_DIRECTORY = "bankAccountMessages";
    private final UsersService usersService;
    private final BankAccountService bankAccountService;
    private final LocaleController localeController;
    private final ResponseService responseService;

    @GetMapping("/getBankAccountsOfUser")
    public ResponseEntity<?> getBankAccountsOfUser() {
        Users user = usersService.getCurrentUser();

        List<BankAccount> bankAccounts = bankAccountService.findAllByUsers(user);

        return ResponseEntity.ok(bankAccounts);
    }

    @PostMapping(value = "/addBankAccount", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createBankAccount(@RequestBody BankAccount bankAccount) {
        Users user = usersService.getCurrentUser();

        // Set the associated user
        bankAccount.setUsers(user);

        try {
            BankAccount savedBankAccount = bankAccountService.save(bankAccount);

            return responseService.createErrorResponseWithData(SUB_DIRECTORY, "success_bankAccountCreated",
                    "", HttpStatus.CREATED, savedBankAccount);
        } catch (Exception e) {
            return responseService.createErrorResponse(SUB_DIRECTORY, "error_failedCreateBankAccount",
                    "", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/bankAccountOverview/{bankAccountId}/data")
    public ResponseEntity<?> getBankAccountById(@PathVariable Long bankAccountId) {
        Users user = usersService.getCurrentUser();

        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, user);
        if (bankAccountOptional.isEmpty()) {
            return responseService.createResponse(SUB_DIRECTORY, "error_bankNotFound", AlertType.SUCCESS);
        }

        BankAccount bankAccount = bankAccountOptional.get();

        return ResponseEntity.ok(bankAccount);
    }
}
