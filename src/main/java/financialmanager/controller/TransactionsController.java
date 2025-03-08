package financialmanager.controller;

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

    @PostMapping("/hideTransactions")
    public ResponseEntity<?> hideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return transactionService.updateTransactionVisibility(bankAccountId, transactionIds, true);
    }

    @PostMapping("/unHideTransactions")
    public ResponseEntity<?> unHideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return transactionService.updateTransactionVisibility(bankAccountId, transactionIds, false);
    }
}
