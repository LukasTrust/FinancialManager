package financialmanager.controller;

import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class SidebarController {

    private final UsersService usersService;
    private final BankAccountService bankAccountService;

    @GetMapping("/getBankAccountsOfUser")
    public ResponseEntity<?> getBankAccountsOfUser() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        List<BankAccount> bankAccounts = bankAccountService.findAllByUsers(currentUser);

        return ResponseEntity.ok(bankAccounts);
    }
}
