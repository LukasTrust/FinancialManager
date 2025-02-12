package financialmanager.controller;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
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
        Users user = usersService.getCurrentUser();

        List<BankAccount> bankAccounts = bankAccountService.findAllByUsers(user);

        return ResponseEntity.ok(bankAccounts);
    }
}
