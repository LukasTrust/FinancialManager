package financialmanager.objectFolder.contractFolder;

import financialmanager.Utils.Result.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final BankAccountService bankAccountService;
    private final TransactionService transactionService;
    private final ResponseService responseService;

    public List<Contract> findByBankAccountId(Long bankAccountId) {
        return contractRepository.findDistinctContractsByBankAccountId(bankAccountId);
    }

    public Contract findByIdAndUsersId(Long id, Long usersId) {
        return contractRepository.findByIdAndUsersId(id, usersId);
    }

    public List<Contract> findByCounterParty(CounterParty counterParty) {
        return contractRepository.findByCounterParty(counterParty);
    }

    public void saveAll(List<Contract> contracts) {
        contractRepository.saveAll(contracts);
    }

    public List<Contract> findByBankAccountIdBetweenDates(Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        List<Contract> contracts = findByBankAccountId(bankAccountId);

        if (startDate == null && endDate == null) {
            return contracts;
        }

        LocalDate[] dates = Utils.getRightDateRange(startDate, endDate);
        LocalDate finalStartDate = dates[0];
        LocalDate finalEndDate = dates[1];

        // Filter contracts that fall within the date range (inclusive)
        return contracts.stream()
                .filter(contract -> !contract.getStartDate().isBefore(finalStartDate) && (contract.getEndDate() == null || !contract.getEndDate().isAfter(finalEndDate)))
                .toList();
    }

    public ResponseEntity<?> getContractsForBankAccount(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        return ResponseEntity.ok(findByBankAccountId(bankAccountId));
    }

    public ResponseEntity<?> removeContractFromTransaction(Long bankAccountId, Long transactionId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        Transaction transaction = transactionService.findById(transactionId);
        if (transaction == null) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR);
        }

        if (!transaction.getBankAccount().equals(bankAccountResult.getValue())) {
            return responseService.createResponse(HttpStatus.NOT_ACCEPTABLE, "transactionDoesNotBelongToBankAccount", AlertType.ERROR);
        }

        if (transaction.getContract() == null) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionHasNoContract", AlertType.ERROR);
        }

        transaction.setContract(null);
        transactionService.save(transaction);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> addContractToTransactions(Long bankAccountId, Long contractId, List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        BankAccount bankAccount = bankAccountResult.getValue();

        Contract contract = findByIdAndUsersId(contractId, bankAccount.getUsers().getId());

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

    public ResponseEntity<?> removeContractFromTransactions(Long bankAccountId, List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
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
