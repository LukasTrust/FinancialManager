package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccountRepository;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class BankAccountController {

    private final String subDirectory = "login&signup";
    private final UsersRepository usersRepository;
    private final BankAccountRepository bankAccountRepository;
    private final SavingsBankAccountRepository savingsBankAccountRepository;

    @PostMapping(value = "/addBankAccount", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createBankAccount(@RequestBody BankAccount bankAccount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Optional<Users> user = usersRepository.findByEmail(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(
                    AlertType.ERROR,
                    "User not found"
            ));
        }

        // Set the associated user
        bankAccount.setUsers(user.get());

        try {
            BankAccount savedBankAccount = null;
            if (bankAccount instanceof SavingsBankAccount savingsBankAccount) {
                savedBankAccount = savingsBankAccountRepository.save(savingsBankAccount);
            } else {
                savedBankAccount = bankAccountRepository.save(bankAccount);
            }

            return ResponseEntity.ok(new Response(
                    AlertType.SUCCESS,
                    "Bank Account created",
                    savedBankAccount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    AlertType.ERROR,
                    "Failed to create Bank Account"
            ));
        }
    }

}
