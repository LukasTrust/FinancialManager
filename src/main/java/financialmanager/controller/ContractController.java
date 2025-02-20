package financialmanager.controller;

import financialmanager.objectFolder.contractFolder.ContractService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/contracts/{bankAccountId}/data")
public class ContractController {

    private final ContractService contractService;

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
