package financialmanager.controller;

import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("transactions/{bankAccountId}/data")
public class TransactionsController {

    private final TransactionService transactionService;

    @GetMapping("")
    public ResponseEntity<?> getTransactionsForBankAccount(@PathVariable Long bankAccountId) {
        return transactionService.getTransactionsForBankAccount(bankAccountId);
    }

    @PostMapping("/hide")
    public ResponseEntity<Response> hideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return transactionService.updateTransactionVisibility(bankAccountId, transactionIds, true);
    }

    @PostMapping("/unHide")
    public ResponseEntity<Response> unHideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return transactionService.updateTransactionVisibility(bankAccountId, transactionIds, false);
    }
}
