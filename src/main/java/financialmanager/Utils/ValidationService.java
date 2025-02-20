package financialmanager.Utils;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Ok;
import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ValidationService {

    private final UsersService usersService;
    private final BankAccountService bankAccountService;
    private final ResponseService responseService;

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

//    public Result<BankAccount, ResponseEntity<Response>> getBankAccountOrErrorResponse(Long bankAccountId) {
//        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();
//        if (currentUserResponse.isErr()) {
//            return new Err<>(currentUserResponse.getError());
//        }
//
//        Users currentUser = currentUserResponse.getValue();
//        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, currentUser);
//
//        if (bankAccountOptional.isEmpty()) {
//            log.warn("User {} does not own the bank account {}", currentUser, bankAccountId);
//            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR));
//        }
//
//        return new Ok<>(bankAccountOptional.get());
//    }
}
