package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistoryService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.Err;
import financialmanager.objectFolder.resultFolder.Ok;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;

@Service
@AllArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final BankAccountService bankAccountService;
    private final ContractHistoryService contractHistoryService;
    private final ResponseService responseService;

    public List<Contract> findByBankAccountId(Long bankAccountId) {
        return contractRepository.findByBankAccountId(bankAccountId);
    }

    public void saveAll(List<Contract> contracts) {
        contractRepository.saveAll(contracts);
    }

    public void save(Contract contract) {contractRepository.save(contract);}

    public void deleteAll(List<Contract> contracts) {
        contractRepository.deleteAll(contracts);
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

    public List<ContractHistory> getContractHistoryForContract(Contract contract) {
        return contractHistoryService.getContractHistoryForContract(contract);
    }

    public Result<List<Contract>, ResponseEntity<Response>> findByIdInAndBankAccountId(List<Long> contractIds, Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return new Err<>(bankAccountResult.getError());
        }

        List<Contract> contracts = contractRepository.findByIdInAndBankAccountId(contractIds, bankAccountId);

        if (contracts.isEmpty()) {
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "contractsNotFound", AlertType.ERROR));
        }

        return new Ok<>(contracts);
    }

    public Result<Contract, ResponseEntity<Response>> findByIdAndBankAccountId(Long contractId, Long bankAccountId) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = bankAccountService.findById(bankAccountId);

        if (bankAccountResult.isErr()) {
            return new Err<>(bankAccountResult.getError());
        }

        Contract contract = contractRepository.findByIdAndBankAccountId(contractId, bankAccountId);

        if (contract == null) {
            return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "contractNotFound", AlertType.ERROR));
        }

        return new Ok<>(contract);
    }

    public ResponseEntity<Response> updateContractField(Long bankAccountId, Long contractId, Map<String, String> requestBody,
                                                            BiConsumer<Contract, String> fieldUpdater) {
        String newValue = requestBody.get("newValue");

        Result<Contract, ResponseEntity<Response>> contractResult = findByIdAndBankAccountId(contractId, bankAccountId);

        if (contractResult.isErr()) {
            return contractResult.getError();
        }

        Contract contract = contractResult.getValue();
        fieldUpdater.accept(contract, newValue);

        save(contract);

        return ResponseEntity.ok().build();
    }
}
