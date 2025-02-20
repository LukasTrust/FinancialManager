package financialmanager.controller;

import financialmanager.Utils.Result.Result;
import financialmanager.Utils.ValidationService;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.responseFolder.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/contracts/{bankAccountId}/data")
public class ContractController {

    private final ContractService contractService;
    private final ValidationService validationService;

//    @GetMapping("")
//    public ResponseEntity<?> getContractsForBankAccount(@PathVariable Long bankAccountId) {
//        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = validationService.getBankAccountOrErrorResponse(bankAccountId);
//
//        if (bankAccountResponse.isErr()) {
//            return bankAccountResponse.getError();
//        }
//
//        return ResponseEntity.ok(contractService.findByBankAccountId(bankAccountId));
//
//    }
}
