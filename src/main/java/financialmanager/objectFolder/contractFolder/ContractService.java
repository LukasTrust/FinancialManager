package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.BaseContractHistoryService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.BaseTransactionService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;

@Service
@AllArgsConstructor
public class ContractService {

    private final BaseContractService baseContractService;
    private final BaseContractHistoryService baseContractHistoryService;
    private final BaseTransactionService baseTransactionService;

    private final ResultService resultService;
    private final ResponseService responseService;

    private Map<Contract, List<Transaction>> findTransactionsByContract(List<Contract> contracts) {
        Map<Contract, List<Transaction>> contractTransactionMap = new HashMap<>();

        List<Transaction> transactions = baseTransactionService.findByContractIn(contracts);

        for (Contract contract : contracts) {
            List<Transaction> transactionsOfContract = transactions.stream().filter(t -> t.getContract().equals(contract)).toList();
            contractTransactionMap.put(contract, transactionsOfContract);
        }

        return contractTransactionMap;
    }

    //<editor-fold desc="find functions">
    public ResponseEntity<?> findContractsForBankAccountAsResponse(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        return ResponseEntity.ok(baseContractService.findByBankAccount(bankAccountResult.getValue()));
    }

    public ResponseEntity<?> findContractDisplaysForBankAccount(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Contract> contracts = baseContractService.findByBankAccount(bankAccountResult.getValue());
        Map<Contract, List<Transaction>> contractTransactionMap = findTransactionsByContract(contracts);

        List<ContractDisplay> contractDisplays = new ArrayList<>();

        for (Map.Entry<Contract, List<Transaction>> contractTransactions : contractTransactionMap.entrySet()) {
            Contract contract = contractTransactions.getKey();
            List<ContractHistory> contractHistories = baseContractHistoryService.findByContract(contract);

            List<Transaction> transactions = contractTransactions.getValue();
            Integer transactionCount = transactions.size();
            Double totalAmount = transactions.stream().map(Transaction::getAmount).reduce(0.0, Double::sum);

            ContractDisplay contractDisplay = new ContractDisplay(contract, contractHistories, transactionCount, totalAmount);
            contractDisplays.add(contractDisplay);
        }

        return ResponseEntity.ok(contractDisplays);
    }
    //</editor-fold>

    //<editor-fold desc="update functions">
    public ResponseEntity<Response> updateContractField(Long bankAccountId, Long contractId, Map<String, String> requestBody,
                                                        BiConsumer<Contract, String> fieldUpdater) {
        String newValue = requestBody.get("newValue");

        Result<Contract, ResponseEntity<Response>> contractResult = resultService.findContractByIdAndBankAccountId(contractId, bankAccountId);

        if (contractResult.isErr()) {
            return contractResult.getError();
        }

        Contract contract = contractResult.getValue();
        fieldUpdater.accept(contract, newValue);

        baseContractService.save(contract);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Response> updateContractVisibility(Long bankAccountId, List<Long> contractIds, boolean hide) {
        Result<List<Contract>, ResponseEntity<Response>> contractResult = resultService.findContractsByIdInAndBankAccountId(bankAccountId, contractIds);

        if (contractResult.isErr()) {
            return contractResult.getError();
        }

        List<Contract> contracts = contractResult.getValue();
        Map<Contract, List<Transaction>> contractTransactionMap = findTransactionsByContract(contracts);

        int transactionCount = 0;

        for (Map.Entry<Contract, List<Transaction>> contractTransactions : contractTransactionMap.entrySet()) {
            Contract contract = contractTransactions.getKey();
            List<Transaction> transactions = contractTransactions.getValue();

            baseTransactionService.setHidden(hide, transactions);
            contract.setHidden(hide);

            baseContractService.save(contract);
            transactionCount += transactions.size();
        }

        List<String> placeHolder = new ArrayList<>();
        placeHolder.add(String.valueOf(contractIds.size()));
        placeHolder.add(String.valueOf(transactionCount));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "contractsHidden" : "contractsUnhidden", AlertType.SUCCESS, placeHolder);
    }

    public ResponseEntity<Response> mergeContracts(Long bankAccountId, Long headerId, List<Long> contractIds) {
        Result<Contract, ResponseEntity<Response>> headerContractResult = resultService.findContractByIdAndBankAccountId(headerId, bankAccountId);

        if (headerContractResult.isErr()) {
            return headerContractResult.getError();
        }

        Result<List<Contract>, ResponseEntity<Response>> contractsResult = resultService.findContractsByIdInAndBankAccountId(bankAccountId, contractIds);

        if (contractsResult.isErr()) {
            return contractsResult.getError();
        }

        Contract headerContract = headerContractResult.getValue();
        List<Contract> contracts = contractsResult.getValue().stream().filter(contract ->
                contract.getBankAccount().equals(headerContract.getBankAccount())
                        && contract.getCounterParty().equals(headerContract.getCounterParty()
                )).toList();

        Map<Contract, List<Transaction>> contractTransactionMap = findTransactionsByContract(contracts);

        int transactionCount = 0;

        for (Map.Entry<Contract, List<Transaction>> contractTransactions : contractTransactionMap.entrySet()) {
            Contract contract = contractTransactions.getKey();
            List<Transaction> transactions = contractTransactions.getValue();

            baseTransactionService.setContract(contract, transactions);

            transactionCount += transactions.size();
        }

        baseContractService.deleteAll(contracts);

        List<String> placeHolder = new ArrayList<>();
        placeHolder.add(String.valueOf(contracts.size()));
        placeHolder.add(String.valueOf(transactionCount));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "mergeContracts", AlertType.INFO, placeHolder);
    }
    //</editor-fold>

    public ResponseEntity<Response> deleteContracts(Long bankAccountId, List<Long> contractIds) {
        Result<List<Contract>, ResponseEntity<Response>> contractsResult = resultService.findContractsByIdInAndBankAccountId(bankAccountId, contractIds);

        if (contractsResult.isErr()) {
            return contractsResult.getError();
        }

        List<Contract> contracts = contractsResult.getValue();
        Map<Contract, List<Transaction>> contractTransactionMap = findTransactionsByContract(contracts);

        int transactionCount = 0;

        for (Map.Entry<Contract, List<Transaction>> contractTransactions : contractTransactionMap.entrySet()) {
            List<Transaction> transactions = contractTransactions.getValue();

            baseTransactionService.setContract(null, transactions);
            transactionCount += transactions.size();
        }

        List<String> placeHolder = new ArrayList<>();
        placeHolder.add(String.valueOf(contracts.size()));
        placeHolder.add(String.valueOf(transactionCount));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "deleteContracts", AlertType.INFO, placeHolder);
    }
}
