package financialmanager.controller;

import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("transactions/{bankAccountId}/data")
public class TransactionsController {

    private final TransactionService transactionService;
    private final BankAccountService bankAccountService;
    private final ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<?> getTransactionsForBankAccount(@PathVariable Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isErr()) {
            return bankAccountResponse.getError();
        }

        List<Transaction> transactions = transactionService.findByBankAccountId(bankAccountId)
                .stream()
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.reverseOrder()))
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/hideTransactions")
    public ResponseEntity<?> hideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return updateTransactionVisibility(bankAccountId, transactionIds, true);
    }

    @PostMapping("/unHideTransactions")
    public ResponseEntity<?> unHideTransactions(@PathVariable Long bankAccountId, @RequestBody List<Long> transactionIds) {
        return updateTransactionVisibility(bankAccountId, transactionIds, false);
    }

    private ResponseEntity<?> updateTransactionVisibility(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isErr()) {
            return bankAccountResponse.getError();
        }

        BankAccount bankAccount = bankAccountResponse.getValue();
        List<Transaction> transactions = transactionService.findByIdInAndBankAccountId(transactionIds, bankAccountId).stream()
                .filter(transaction -> transaction.isHidden() != hide)
                .toList();

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.CONFLICT, "noTransactionsUpdated", AlertType.INFO);
        }

        transactions.forEach(transaction -> transaction.setHidden(hide));
        transactionService.saveAll(transactions);

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "transactionsHidden" : "transactionsUnhidden",
                AlertType.SUCCESS, List.of(String.valueOf(transactionIds.size())));
    }
}
