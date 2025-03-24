package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class TransactionService {

    private final BaseTransactionService baseTransactionService;

    private final ResultService resultService;
    private final ResponseService responseService;

    //<editor-fold desc="find functions">
    public List<Transaction> findByBankAccountBetweenDates(BankAccount bankAccount, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = baseTransactionService.findByBankAccount(bankAccount);

        if (startDate == null && endDate == null) {
            return transactions;
        }

        LocalDate[] dates = Utils.getRightDateRange(startDate, endDate);
        LocalDate finalStartDate = dates[0];
        LocalDate finalEndDate = dates[1];

        return transactions.stream()
                .filter(transaction -> !transaction.getDate().isBefore(finalStartDate) && !transaction.getDate().isAfter(finalEndDate))
                .toList();
    }

    public ResponseEntity<?> findTransactionsByBankAccountAsResponse(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        List<Transaction> transactions = baseTransactionService.findByBankAccount(bankAccountResult.getValue())
                .stream()
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.reverseOrder()))
                .toList();

        return ResponseEntity.ok(transactions);
    }
    //</editor-fold>

    //<editor-fold desc="update functions">
    public ResponseEntity<Response> changeContractOfTransactions(Long bankAccountId, List<Long> transactionIds, Long contractId) {
        Contract contract = null;

        if (contractId != null) {
            Result<Contract, ResponseEntity<Response>> contractResult = resultService.findContractByIdAndBankAccountId(bankAccountId, contractId);
            if (contractResult.isErr())
                return contractResult.getError();

            contract = contractResult.getValue();
        }

        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = resultService.findTransactionsByIdInAndBankAccountId(bankAccountId, transactionIds);

        if (transactionResult.isErr())
            return transactionResult.getError();

        baseTransactionService.setContract(contract, transactionResult.getValue());

        return contract != null ?
                responseService.createResponseWithPlaceHolders(HttpStatus.OK, "transactionsAddedContract", AlertType.SUCCESS, List.of(contract.getName())) :
                responseService.createResponse(HttpStatus.OK, "transactionsRemovedContract", AlertType.SUCCESS);
    }

    public ResponseEntity<Response> updateTransactionVisibility(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = resultService.findTransactionsByIdInAndBankAccountIdAndHidden(bankAccountId, transactionIds, hide);

        if (transactionResult.isErr())
            return transactionResult.getError();

        baseTransactionService.setHidden(hide, transactionResult.getValue());

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "transactionsHidden" : "transactionsUnhidden",
                AlertType.SUCCESS, List.of(String.valueOf(transactionIds.size())));
    }
    //</editor-fold>
}
