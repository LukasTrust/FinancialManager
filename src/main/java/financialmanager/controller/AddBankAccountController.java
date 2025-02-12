package financialmanager.controller;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class AddBankAccountController {

    private final UsersService usersService;
    private final BankAccountService bankAccountService;
    private final ResponseService responseService;

    @PostMapping(value = "/addBankAccount", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createBankAccount(@RequestBody BankAccount bankAccount) {
        Users currentUser = usersService.getCurrentUser();

        // Set the associated currentUser
        bankAccount.setUsers(currentUser);

        String SUB_DIRECTORY = "addBankAccountMessages";
        try {
            BankAccount savedBankAccount = bankAccountService.save(bankAccount);

            return responseService.createResponseWithData(HttpStatus.CREATED, SUB_DIRECTORY, "success_bankAccountCreated",
                    AlertType.SUCCESS, savedBankAccount);
        } catch (Exception e) {
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, SUB_DIRECTORY,
                    "error_failedCreateBankAccount", AlertType.ERROR);
        }
    }
}
