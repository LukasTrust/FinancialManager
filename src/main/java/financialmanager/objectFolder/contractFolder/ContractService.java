package financialmanager.objectFolder.contractFolder;

import financialmanager.Utils.Utils;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    public List<Contract> findByBankAccountId(Long bankAccountId) {
        return contractRepository.findDistinctContractsByBankAccountId(bankAccountId);
    }

    public Contract findByIdAndUsersId(Long id, Long usersId) {
        return contractRepository.findByIdAndUsersId(id, usersId);
    }

    public List<Contract> findByCounterParty(CounterParty counterParty) {
        return contractRepository.findByCounterParty(counterParty);
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

    public void saveAll(List<Contract> contracts) {
        contractRepository.saveAll(contracts);
    }
}
