package financialmanager.controller;

import financialmanager.objectFolder.bankAccountFolder.BaseBankAccountService;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class SidebarController {

    private final BaseBankAccountService baseBankAccountService;

    private final ResultService resultService;

    @GetMapping("/getBankAccountsOfUser")
    public ResponseEntity<?> getBankAccountsOfUser() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = resultService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        List<BankAccount> bankAccounts = baseBankAccountService.findAllByUsers(currentUser);

        return ResponseEntity.ok(bankAccounts);
    }
}
