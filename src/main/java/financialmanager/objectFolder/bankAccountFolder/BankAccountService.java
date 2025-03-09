package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Ok;
import financialmanager.Utils.Result.Result;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BankAccountService {

    private final UsersService usersService;
    private final BankAccountRepository bankAccountRepository;
    private final ResponseService responseService;

    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    public BankAccount save(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public List<BankAccount> findAllByUsers(Users users) {
        return bankAccountRepository.findAllByUsers(users);
    }

    public Result<BankAccount, ResponseEntity<Response>> findById(Long bankAccountId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(currentUserResponse.getError().getBody()));
        }

        Users currentUser = currentUserResponse.getValue();

        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findByIdAndUsers(bankAccountId, currentUser);

        if (bankAccountOptional.isPresent()) {
            return new Ok<>(bankAccountOptional.get());
        }

        log.warn("User {} does not own the bank account {}", currentUser, bankAccountId);
        return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR));
    }

    public ResponseEntity<Response> createBankAccount(BankAccount bankAccount) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        // Set the associated currentUser
        bankAccount.setUsers(currentUser);

        try {
            BankAccount savedBankAccount = save(bankAccount);

            return responseService.createResponseWithData(HttpStatus.CREATED, "bankAccountCreated",
                    AlertType.SUCCESS, savedBankAccount);
        } catch (Exception e) {
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "failedCreateBankAccount", AlertType.ERROR);
        }
    }
}
