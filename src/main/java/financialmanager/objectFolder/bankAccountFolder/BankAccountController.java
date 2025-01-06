package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
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

    private final String subDirectory = "login&signup";
    private final UsersService usersService;
    private final BankAccountService bankAccountService;

    @GetMapping("/getBankAccountsOfUser")
    public ResponseEntity<?> getBankAccountsOfUser() {
        Optional<Users> user = usersService.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(
                    AlertType.ERROR,
                    "User not found"
            ));
        }

        List<BankAccount> bankAccounts = bankAccountService.findAllByUsers(user.get());

        return ResponseEntity.ok(bankAccounts);
    }

    @PostMapping(value = "/addBankAccount", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createBankAccount(@RequestBody BankAccount bankAccount) {
        Optional<Users> user = usersService.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(
                    AlertType.ERROR,
                    "User not found"
            ));
        }

        // Set the associated user
        bankAccount.setUsers(user.get());

        try {
            BankAccount savedBankAccount = bankAccountService.save(bankAccount);

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
