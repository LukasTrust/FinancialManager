package financialmanager.controller;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/contracts/{bankAccountId}/data")
public class ContractController {

    private final ContractService contractService;
    private final TransactionService transactionService;

    @GetMapping("/onlyContract")
    public ResponseEntity<?> getContractsForBankAccount(@PathVariable Long bankAccountId) {
        return contractService.getContractsForBankAccount(bankAccountId);
    }

    @GetMapping("")
    public ResponseEntity<?> getContractDisplaysForBankAccount(@PathVariable Long bankAccountId) {
        return transactionService.getContractDisplaysForBankAccount(bankAccountId);
    }

    @PostMapping("/removeContractFromTransaction/{transactionId}")
    public ResponseEntity<Response> removeContractFromTransaction(
            @PathVariable Long bankAccountId,
            @PathVariable Long transactionId) {
        return transactionService.removeContractFromTransaction(bankAccountId, transactionId);
    }

    @PostMapping("/addContractToTransactions/{contractId}")
    public ResponseEntity<Response> addContractToTransactions(@PathVariable Long bankAccountId, @PathVariable Long contractId,
                                                       @RequestBody List<Long> transactionIds) {
        return transactionService.addContractToTransactions(bankAccountId, contractId, transactionIds);
    }

    @PostMapping("/removeContractFromTransactions")
    public ResponseEntity<Response> removeContractFromTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return transactionService.removeContractFromTransactions(bankAccountId, transactionIds);
    }

    @PostMapping("/{contractId}/change/name/{newValue}")
    public ResponseEntity<Response> updateContractName(@PathVariable Long bankAccountId,
                                                       @PathVariable Long contractId,
                                                           @PathVariable String newValue) {
        return contractService.updateContractField(bankAccountId, contractId, newValue, Contract::setName);
    }

    @PostMapping("/{contractId}/change/description/{newValue}")
    public ResponseEntity<Response> updateContractDescription(@PathVariable Long bankAccountId,
                                                              @PathVariable Long contractId,
                                                                  @PathVariable String newValue) {
        return contractService.updateContractField(bankAccountId, contractId, newValue, Contract::setDescription);
    }

    @PostMapping("/hide")
    public ResponseEntity<Response> hideContracts(@PathVariable Long bankAccountId,
                                                       @RequestBody List<Long> contractIds) {
        return transactionService.updateContractVisibility(bankAccountId, contractIds, true);
    }

    @PostMapping("/unHide")
    public ResponseEntity<Response> unHideContracts(@PathVariable Long bankAccountId,
                                                         @RequestBody List<Long> contractIds) {
        return transactionService.updateContractVisibility(bankAccountId, contractIds, false);
    }
}
