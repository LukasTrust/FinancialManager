package financialmanager.controller;

import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.Contract;
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

import java.util.List;

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

    @PostMapping("/removeContractFromTransaction/{transactionId}")
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

    @PostMapping("/addContractToTransactions/{contractId}")
    public ResponseEntity<?> addContractToTransactions(@PathVariable Long bankAccountId, @PathVariable Long contractId,
                                                       @RequestBody List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isErr()) {
            return bankAccountResponse.getError();
        }

        BankAccount bankAccount = bankAccountResponse.getValue();

        Contract contract = contractService.findByIdAndUsersId(contractId, bankAccount.getUsers().getId());

        if (contract == null) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "contractNotFound", AlertType.ERROR);
        }

        List<Transaction> transactions = transactionService.findByIdInAndBankAccountId(transactionIds, bankAccountId);

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR);
        }

        transactions.forEach(transaction -> transaction.setContract(contract));

        transactionService.saveAll(transactions);

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "transactionsAddedContract", AlertType.SUCCESS,
                List.of(contract.getName()));
    }

    @PostMapping("/removeContractFromTransactions")
    public ResponseEntity<?> removeContractFromTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isErr()) {
            return bankAccountResponse.getError();
        }

        List<Transaction> transactions = transactionService.findByIdInAndBankAccountId(transactionIds, bankAccountId);

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR);
        }

        transactions.forEach(transaction -> transaction.setContract(null));

        transactionService.saveAll(transactions);

        return responseService.createResponse(HttpStatus.OK, "transactionsRemovedContract", AlertType.SUCCESS);
    }
}
