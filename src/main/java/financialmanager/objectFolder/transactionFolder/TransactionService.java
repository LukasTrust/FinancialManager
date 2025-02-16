package financialmanager.objectFolder.transactionFolder;
import financialmanager.Utils.Utils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findByBankAccountId(Long bankAccountId) {
        return transactionRepository.findByBankAccountId(bankAccountId);
    }

    public List<Transaction> findAllByListOfId(List<Long> ids) {
        return transactionRepository.findAllById(ids);
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

    public void saveAll(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }
}
