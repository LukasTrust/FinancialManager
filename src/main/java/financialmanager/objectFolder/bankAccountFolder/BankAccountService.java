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

    public BankAccount save(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public List<BankAccount> findAllByUsers(Users users){
        return bankAccountRepository.findAllByUsers(users);
    }

    public Optional<BankAccount> findByIdAndUsers(Long id, Users users){
        return bankAccountRepository.findByIdAndUsers(id, users);
    }

    public Result<BankAccount, ResponseEntity<Response>> findById(Long bankAccountId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(currentUserResponse.getError().getBody()));
        }

        Users currentUser = currentUserResponse.getValue();

        Optional<BankAccount> bankAccountResponse = bankAccountRepository.findByIdAndUsers(bankAccountId, currentUser);

        if (bankAccountResponse.isPresent())  {
            return new Ok<>(bankAccountResponse.get());
        }

        return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "bankNotFound", AlertType.ERROR));
    }
}
