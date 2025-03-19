package financialmanager.controller;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.responseFolder.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/contracts/{bankAccountId}/data")
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/onlyContract")
    public ResponseEntity<?> getContractsForBankAccount(@PathVariable Long bankAccountId) {
        return contractService.findContractsForBankAccountAsResponse(bankAccountId);
    }

    @GetMapping("")
    public ResponseEntity<?> getContractDisplaysForBankAccount(@PathVariable Long bankAccountId) {
        return contractService.findContractDisplaysForBankAccount(bankAccountId);
    }

    @PostMapping("/{contractId}/change/name")
    public ResponseEntity<Response> updateContractName(@PathVariable Long bankAccountId,
                                                       @PathVariable Long contractId,
                                                       @RequestBody Map<String, String> requestBody) {
        return contractService.updateContractField(bankAccountId, contractId, requestBody, Contract::setName);
    }

    @PostMapping("/{contractId}/change/description")
    public ResponseEntity<Response> updateContractDescription(@PathVariable Long bankAccountId,
                                                              @PathVariable Long contractId,
                                                              @RequestBody Map<String, String> requestBody) {
        return contractService.updateContractField(bankAccountId, contractId, requestBody, Contract::setDescription);
    }

    @PostMapping("/hide")
    public ResponseEntity<Response> hideContracts(@PathVariable Long bankAccountId,
                                                       @RequestBody List<Long> contractIds) {
        return contractService.updateContractVisibility(bankAccountId, contractIds, true);
    }

    @PostMapping("/unHide")
    public ResponseEntity<Response> unHideContracts(@PathVariable Long bankAccountId,
                                                         @RequestBody List<Long> contractIds) {
        return contractService.updateContractVisibility(bankAccountId, contractIds, false);
    }

    @PostMapping("/merge/{headerId}")
    public ResponseEntity<Response> mergeContracts(@PathVariable Long bankAccountId, @PathVariable Long headerId, @RequestBody List<Long> contractIds) {
        return contractService.mergeContracts(bankAccountId, headerId, contractIds);
    }

    @PostMapping("/deleteContracts")
    public ResponseEntity<Response> deleteContracts(@PathVariable Long bankAccountId, @RequestBody List<Long> contractIds) {
        return contractService.deleteContracts(bankAccountId, contractIds);
    }
}
