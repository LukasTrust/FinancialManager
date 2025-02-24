package financialmanager.controller;

import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/contracts/{bankAccountId}/data")
public class ContractController {

    private final ContractService contractService;
    private final BankAccountService bankAccountService;
    private final TransactionService transactionService;
    private final ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<?> getContractsForBankAccount(@PathVariable Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isErr()) {
            return bankAccountResponse.getError();
        }

        return ResponseEntity.ok(contractService.findByBankAccountId(bankAccountId));
    }

    @PostMapping("/updateTransaction/{transactionId}")
    public ResponseEntity<?> removeContractFromTransaction(
            @PathVariable Long bankAccountId,
            @PathVariable Long transactionId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isErr()) {
            return bankAccountResponse.getError();
        }

        Transaction transaction = transactionService.findById(transactionId);
        if (transaction == null) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR);
        }

        if (!transaction.getBankAccount().equals(bankAccountResponse.getValue())) {
            return responseService.createResponse(HttpStatus.NOT_ACCEPTABLE, "transactionDoesNotBelongToBankAccount", AlertType.ERROR);
        }

        if (transaction.getContract() == null) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionHasNoContract", AlertType.ERROR);
        }

        transaction.setContract(null);
        transactionService.save(transaction);

        return ResponseEntity.ok().build();
    }
}
