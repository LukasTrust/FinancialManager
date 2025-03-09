package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final BankAccountService bankAccountService;

    public List<Contract> findByBankAccountId(Long bankAccountId) {
        return contractRepository.findDistinctContractsByBankAccountId(bankAccountId);
    }

    public Contract findByIdAndUsersId(Long id, Long usersId) {
        return contractRepository.findByIdAndUsersId(id, usersId);
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
}
