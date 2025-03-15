package financialmanager.objectFolder.transactionFolder;
import financialmanager.objectFolder.contractFolder.ContractDisplay;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
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

    private final TransactionRepository transactionRepository;
    private final BankAccountService bankAccountService;
    private final ResponseService responseService;
    private final ContractService contractService;

    public List<Transaction> findByBankAccountId(Long bankAccountId) {
        return transactionRepository.findByBankAccountId(bankAccountId);
    }

    public List<Transaction> findByIdInAndBankAccountId(List<Long> ids, Long bankAccountId) {
        return transactionRepository.findByIdInAndBankAccountId(ids, bankAccountId);
    }

    public List<Transaction> findByCounterParty(CounterParty counterParty) {
        return transactionRepository.findByCounterParty(counterParty);
    }

    public List<Transaction> findByCounterPartyIn(List<CounterParty> counterParties) {
        return transactionRepository.findByCounterPartyIn(counterParties);
    }

    public List<Transaction> findByOriginalCounterParty(String originalCounterParty) {
        return transactionRepository.findByOriginalCounterParty(originalCounterParty);
    }

    public List<Transaction> findByContract(Contract contract) {
        return transactionRepository.findByContract(contract);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public void saveAll(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public ResponseEntity<Response> addContractToTransactions(Long bankAccountId, Long contractId, List<Long> transactionIds) {
        Result<Contract, ResponseEntity<Response>> contractResult = contractService.findByIdAndBankAccountId(bankAccountId, contractId);

        if (contractResult.isErr()) {
            return contractResult.getError();
        }

        Contract contract = contractResult.getValue();

        List<Transaction> transactions = findByIdInAndBankAccountId(transactionIds, bankAccountId);

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR);
        }

        transactions.forEach(transaction -> transaction.setContract(contract));

        saveAll(transactions);

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "transactionsAddedContract", AlertType.SUCCESS,
                List.of(contract.getName()));
    }

    public ResponseEntity<Response> removeContractFromTransactions(Long bankAccountId, List<Long> transactionIds) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Transaction> transactions = findByIdInAndBankAccountId(transactionIds, bankAccountId);

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "transactionNotFound", AlertType.ERROR);
        }

        transactions.forEach(transaction -> transaction.setContract(null));

        saveAll(transactions);

        return responseService.createResponse(HttpStatus.OK, "transactionsRemovedContract", AlertType.SUCCESS);
    }

    public ResponseEntity<Response> removeContractFromTransaction(Long bankAccountId, Long transactionId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        Transaction transaction = findById(transactionId);
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
        save(transaction);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> getTransactionsForBankAccount(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Transaction> transactions = findByBankAccountId(bankAccountId)
                .stream()
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.reverseOrder()))
                .toList();

        return ResponseEntity.ok(transactions);
    }

    public List<Transaction> findByBankAccountIdBetweenDates(Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = findByBankAccountId(bankAccountId);

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

    public ResponseEntity<Response> updateContractVisibility(Long bankAccountId, List<Long> contractIds, boolean hide) {
        Result<List<Contract>, ResponseEntity<Response>> contractResult = contractService.findByIdInAndBankAccountId(contractIds, bankAccountId);

        if (contractResult.isErr()) {
            return contractResult.getError();
        }

        List<Contract> contracts = contractResult.getValue();
        int transactionCount = 0;

        for (Contract contract : contracts) {
            List<Transaction> transactions = findByContract(contract);
            transactionCount += transactions.size();

            setTransactionVisibility(transactions, hide);
            contract.setHidden(hide);

            contractService.save(contract);
        }

        List<String> data = new ArrayList<>();
        data.add(String.valueOf(contractIds.size()));
        data.add(String.valueOf(transactionCount));

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "contractsHidden" : "contractsUnhidden", AlertType.SUCCESS, data);
    }

    public ResponseEntity<Response> updateTransactionVisibility(Long bankAccountId, List<Long> transactionIds, boolean hide) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Transaction> transactions = findByIdInAndBankAccountId(transactionIds, bankAccountId).stream()
                .filter(transaction -> transaction.isHidden() != hide)
                .toList();

        if (transactions.isEmpty()) {
            return responseService.createResponse(HttpStatus.CONFLICT, "noTransactionsUpdated", AlertType.INFO);
        }

        setTransactionVisibility(transactions, hide);

        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, hide ? "transactionsHidden" : "transactionsUnhidden",
                AlertType.SUCCESS, List.of(String.valueOf(transactionIds.size())));
    }

    private void setTransactionVisibility(List<Transaction> transactions, boolean hide) {
        transactions.forEach(transaction -> transaction.setHidden(hide));
        saveAll(transactions);
    }

    public ResponseEntity<?> getContractDisplaysForBankAccount(Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return bankAccountResult.getError();
        }

        List<Contract> contracts = contractService.findByBankAccountId(bankAccountId);
        List<Transaction> transactions = findByBankAccountId(bankAccountId);
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
}
