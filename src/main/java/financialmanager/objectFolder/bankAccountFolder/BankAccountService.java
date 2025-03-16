package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BankAccountService {

    private final BaseBankAccountService baseBankAccountService;

    private final ResponseService responseService;
    private final ResultService resultService;

    public ResponseEntity<Response> createBankAccount(BankAccount bankAccount) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = resultService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        // Set the associated currentUser
        bankAccount.setUsers(currentUser);

        try {
            BankAccount savedBankAccount = baseBankAccountService.save(bankAccount);

            return responseService.createResponseWithData(HttpStatus.CREATED, "bankAccountCreated",
                    AlertType.SUCCESS, savedBankAccount);
        } catch (Exception e) {
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "failedCreateBankAccount", AlertType.ERROR);
        }
    }
}
