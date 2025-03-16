package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.contractFolder.ContractDisplay;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.resultFolder.Err;
import financialmanager.objectFolder.resultFolder.Ok;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
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

    private final BankAccountService bankAccountService;
    private final ResponseService responseService;
    private final ContractService contractService;
    private final BaseTransactionService baseTransactionService;

    private Result<List<Transaction>, ResponseEntity<Response>> findByIdInAndBankAccountId(Long bankAccountId, List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr())
            return new Err<>(bankAccountResult.getError());

        List<Transaction> transactions = baseTransactionService.findByIdInAndBankAccountId(transactionIds, bankAccountId);

        if (transactions.isEmpty())
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR));


        return new Ok<>(transactions);
    }

    private Result<List<Transaction>, ResponseEntity<Response>> findByIdInAndBankAccountIdAndHidden(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = findByIdInAndBankAccountId(bankAccountId, transactionIds);

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

    private void setContractForTransactions(Contract contract, List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setContract(contract));
        baseTransactionService.saveAll(transactions);
    }

    private void setHiddenForTransactions(boolean hide, List<Transaction> transactions) {
        transactions.forEach(transaction -> transaction.setHidden(hide));
        baseTransactionService.saveAll(transactions);
    }

    public ResponseEntity<Response> changeContractOfTransactions(Long bankAccountId, List<Long> transactionIds, Long contractId) {
        Contract contract = null;

        if (contractId != null) {
            Result<Contract, ResponseEntity<Response>> contractResult = contractService.findByIdAndBankAccountId(bankAccountId, contractId);
            if (contractResult.isErr())
                return contractResult.getError();

            contract = contractResult.getValue();
        }

        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = findByIdInAndBankAccountId(bankAccountId, transactionIds);
        if (transactionResult.isErr())
            return transactionResult.getError();
        List<Transaction> transactions = transactionResult.getValue();

        setContractForTransactions(contract, transactions);

        return contract != null ?
                responseService.createResponseWithPlaceHolders(HttpStatus.OK, "transactionsAddedContract", AlertType.SUCCESS, List.of(contract.getName())) :
                responseService.createResponse(HttpStatus.OK, "transactionsRemovedContract", AlertType.SUCCESS);
    }

    public ResponseEntity<?> findTransactionsByBankAccountAsResponse(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Transaction> transactions = baseTransactionService.findByBankAccountId(bankAccountId)
                .stream()
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.reverseOrder()))
                .toList();

        return ResponseEntity.ok(transactions);
    }

    public List<Transaction> findByBankAccountIdBetweenDates(Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = baseTransactionService.findByBankAccountId(bankAccountId);

        if (startDate == null && endDate == null) {
            return transactions;
        }

        LocalDate[] dates = Utils.getRightDateRange(startDate, endDate);
        LocalDate finalStartDate = dates[0];
        LocalDate finalEndDate = dates[1];

        // Filter transactions that fall within the date range (inclusive)
        return transactions.stream()
                .filter(transaction -> !transaction.getDate().isBefore(finalStartDate) && !transaction.getDate().isAfter(finalEndDate))
                .toList();
    }

    public ResponseEntity<Response> updateTransactionVisibility(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        Result<List<Transaction>, ResponseEntity<Response>> transactionResult = findByIdInAndBankAccountIdAndHidden(bankAccountId, transactionIds, hide);

        if (transactionResult.isErr())
            return transactionResult.getError();

        setHiddenForTransactions(hide, transactionResult.getValue());

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "transactionsHidden" : "transactionsUnhidden",
                AlertType.SUCCESS, List.of(String.valueOf(transactionIds.size())));
    }

    public ResponseEntity<?> getContractDisplaysForBankAccount(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Contract> contracts = contractService.findByBankAccountId(bankAccountId);
        List<Transaction> transactions = baseTransactionService.findByBankAccountId(bankAccountId);
        List<ContractDisplay> contractDisplays = new ArrayList<>();

        for (Contract contract : contracts) {
            List<Transaction> transactionsOfContract = transactions.stream().filter(transaction -> transaction.getContract() != null && transaction.getContract().equals(contract)).toList();

            List<ContractHistory> contractHistories = contractService.getContractHistoryForContract(contract);
            contractHistories.addAll(contractService.getContractHistoryForContract(contract));
            Integer transactionCount = transactionsOfContract.size();
            Double totalAmount = transactionsOfContract.stream().map(Transaction::getAmount).reduce(0.0, Double::sum);

            ContractDisplay contractDisplay = new ContractDisplay(contract, contractHistories, transactionCount, totalAmount);
            contractDisplays.add(contractDisplay);
        }

        return ResponseEntity.ok(contractDisplays);
    }

    public ResponseEntity<Response> mergeContracts(Long bankAccountId, Long headerId, List<Long> counterPartyIds) {
        Result<Contract, ResponseEntity<Response>> headerContractResult = contractService.findByIdAndBankAccountId(headerId, bankAccountId);

        if (headerContractResult.isErr()) {
            return headerContractResult.getError();
        }

        Result<List<Contract>, ResponseEntity<Response>> contractsResult = contractService.findByIdInAndBankAccountId(counterPartyIds, bankAccountId);

        if (contractsResult.isErr()) {
            return contractsResult.getError();
        }

        Contract headerContract = headerContractResult.getValue();
        List<Contract> contracts = contractsResult.getValue();
        int countOfTransactions = 0;

        for (Contract contract : contracts) {
            if (contract.getBankAccount().equals(headerContract.getBankAccount()) && contract.getCounterParty().equals(headerContract.getCounterParty())) {
                List<Transaction> transactions = baseTransactionService.findByContract(contract);

                transactions.forEach(transaction -> transaction.setContract(headerContract));

                baseTransactionService.saveAll(transactions);
                countOfTransactions += transactions.size();
            }
        }

        contractService.deleteAll(contracts);
        List<String> placeHolder = new ArrayList<>();
        placeHolder.add(String.valueOf(contracts.size()));
        placeHolder.add(String.valueOf(countOfTransactions));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "mergeContracts", AlertType.INFO, placeHolder);
    }

    public ResponseEntity<Response> updateContractVisibility(Long bankAccountId, List<Long> contractIds, boolean hide) {
        Result<List<Contract>, ResponseEntity<Response>> contractResult = contractService.findByIdInAndBankAccountId(contractIds, bankAccountId);

        if (contractResult.isErr()) {
            return contractResult.getError();
        }

        List<Contract> contracts = contractResult.getValue();
        int transactionCount = 0;

        for (Contract contract : contracts) {
            List<Transaction> transactions = baseTransactionService.findByContract(contract);
            transactionCount += transactions.size();

            setHiddenForTransactions(transactions, hide);
            contract.setHidden(hide);

            contractService.save(contract);
        }

        List<String> placeHolder = new ArrayList<>();
        placeHolder.add(String.valueOf(contractIds.size()));
        placeHolder.add(String.valueOf(transactionCount));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "contractsHidden" : "contractsUnhidden", AlertType.SUCCESS, placeHolder);
    }
}
