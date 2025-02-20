package financialmanager.controller;

import financialmanager.Utils.Result.Result;
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
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        // Set the associated currentUser
        bankAccount.setUsers(currentUser);

        try {
            BankAccount savedBankAccount = bankAccountService.save(bankAccount);

            return responseService.createResponseWithData(HttpStatus.CREATED, "bankAccountCreated",
                    AlertType.SUCCESS, savedBankAccount);
        } catch (Exception e) {
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "failedCreateBankAccount", AlertType.ERROR);
        }
    }
}
