package financialmanager.objectFolder.chartFolder;

import financialmanager.Utils.Utils;
import financialmanager.locale.LocaleService;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChartService {

    private final TransactionService transactionService;
    private final BankAccountService bankAccountService;
    private final UsersService usersService;
    private final LocaleService localeService;
    private static final String SUB_DIRECTORY = "chartMessages";
    private static final Logger log = LoggerFactory.getLogger(ChartService.class);

    public ChartData getTransactionDate(List<Long> bankAccountIds, LocalDate startDate, LocalDate endDate) {
        LocalDate[] dates = Utils.normalizeDateRange(startDate, endDate);
        LocalDate start = dates[0];
        LocalDate end = dates[1];

        String title = buildTitle(start, end);

        List<ChartSeries> chartSeries = bankAccountIds.stream()
                .map(bankAccountId -> getChartSeries(usersService.getCurrentUser(), bankAccountId, start, end))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new ChartData(title, chartSeries);
    }

    private String buildTitle(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return startDate + " - " + endDate;
        } else if (startDate != null) {
            return startDate + " -           ";
        } else if (endDate != null) {
            return "           - " + endDate;
        } else {
            return "";
        }
    }

    private Optional<ChartSeries> getChartSeries(Users currentUser, Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        Optional<BankAccount> bankAccountOptional = bankAccountService.findByIdAndUsers(bankAccountId, currentUser);
        Optional<ChartSeries> chartSeriesOptional;

        if (bankAccountOptional.isPresent()) {
            BankAccount bankAccount = bankAccountOptional.get();
            List<Transaction> transactions = transactionService.findByBankAccountIdBetweenDates(bankAccount.getId(),
                    startDate, endDate);

            transactions.sort(Comparator.comparing(Transaction::getDate));

            List<DataPoint> dataPoints = getDataPoints(currentUser, transactions);

            ChartSeries chartSeries = new ChartSeries(bankAccount.getName(), dataPoints);
            chartSeriesOptional = Optional.of(chartSeries);
        } else {
            log.warn("User {} does not own the bank account {}", currentUser, bankAccountId);
            chartSeriesOptional = Optional.empty();
        }

        return chartSeriesOptional;
    }

    private List<DataPoint> getDataPoints(Users currentUser, List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> createDataPoint(transaction, currentUser))
                .collect(Collectors.toList());
    }

    private DataPoint createDataPoint(Transaction transaction, Users currentUser) {
        double difference = Utils.getDifferenceInTransaction(transaction);

        PointStyle pointStyle = difference == 0 ? PointStyle.NORMAL
                : difference > 0 ? PointStyle.BAD
                : PointStyle.GOOD;

        String info = (pointStyle == PointStyle.NORMAL) ? null
                : localeService.getMessageWithPlaceHolder(
                SUB_DIRECTORY,
                pointStyle == PointStyle.BAD ? "lessAmountAfter" : "moreAmountAfter",
                currentUser,
                List.of(String.valueOf(difference))
        );

        return new DataPoint(transaction.getAmountInBankAfter(), transaction.getDate(), info, pointStyle);
    }
}
