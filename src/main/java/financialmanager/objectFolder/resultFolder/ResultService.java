package financialmanager.objectFolder.resultFolder;

import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.BaseContractService;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ResultService {

    private final BaseTransactionService baseTransactionService;
    private final BaseContractService baseContractService;

    private final BankAccountService bankAccountService;
    private final ResponseService responseService;

    //<editor-fold desc="Transaction">
    public Result<List<Transaction>, ResponseEntity<Response>> findTransactionsByIdInAndBankAccountId(Long bankAccountId, List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        List<Transaction> transactions = baseTransactionService.findByIdInAndBankAccountId(transactionIds, bankAccountId);

        if (transactions.isEmpty())
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR));


        return new Ok<>(transactions);
    }

    public Result<List<Transaction>, ResponseEntity<Response>> findTransactionsByIdInAndBankAccountIdAndHidden(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = findTransactionsByIdInAndBankAccountId(bankAccountId, transactionIds);

        if (transactionResult.isErr())
            return new Err<>(transactionResult.getError());

        List<Transaction> transactions = transactionResult.getValue().stream()
                .filter(transaction -> transaction.isHidden() != hide)
                .toList();

        if (transactions.isEmpty()) {
            return new Err<>(responseService.createResponse(HttpStatus.CONFLICT, "noTransactionsUpdated", AlertType.INFO));
        }

        return new Ok<>(transactions);
    }
    //</editor-fold>

    //<editor-fold desc="Contract">
    public Result<Contract, ResponseEntity<Response>> findContractByIdAndBankAccountId(Long bankAccountId, Long contractId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        Contract contract = baseContractService.findByIdAndBankAccountId(contractId, bankAccountId);

        if (contract == null)
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "contractNotFound", AlertType.ERROR));

        return new Ok<>(contract);
    }

    public Result<List<Contract>, ResponseEntity<Response>> findContractsByIdInAndBankAccountId(Long bankAccountId, List<Long> contractIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        List<Contract> contracts = baseContractService.findByIdInAndBankAccountId(contractIds, bankAccountId);

        if (contracts.isEmpty())
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "contractsNotFound", AlertType.ERROR));

        return new Ok<>(contracts);
    }

    public List<Contract> findContractsByBankAccountIdBetweenDates(Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        List<Contract> contracts = baseContractService.findByBankAccountId(bankAccountId);

        if (startDate == null && endDate == null) {
            return contracts;
        }

        LocalDate[] dates = Utils.getRightDateRange(startDate, endDate);
        LocalDate finalStartDate = dates[0];
        LocalDate finalEndDate = dates[1];

        // Filter contracts that fall within the date range (inclusive)
        return contracts.stream()
                .filter(contract -> !contract.getStartDate().isBefore(finalStartDate) && (contract.getEndDate() == null
                        || !contract.getEndDate().isAfter(finalEndDate)))
                .toList();
    }
    //</editor-fold>
}
