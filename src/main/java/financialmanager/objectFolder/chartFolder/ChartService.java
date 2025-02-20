package financialmanager.objectFolder.chartFolder;

import financialmanager.Utils.Result.Result;
import financialmanager.Utils.Utils;
import financialmanager.locale.LocaleService;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChartService {

    private final TransactionService transactionService;
    private final BankAccountService bankAccountService;
    private final LocaleService localeService;

    public ChartData getTransactionDate(List<Long> bankAccountIds, LocalDate startDate, LocalDate endDate) {
        LocalDate[] dates = Utils.normalizeDateRange(startDate, endDate);
        LocalDate start = dates[0];
        LocalDate end = dates[1];

        String title = buildTitle(start, end);

        List<ChartSeries> chartSeries = bankAccountIds.stream()
                .map(bankAccountId -> getChartSeries(bankAccountId, start, end))
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

    private Optional<ChartSeries> getChartSeries(Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        Optional<ChartSeries> chartSeriesOptional = Optional.empty();

        Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

        if (bankAccountResponse.isOk()) {
            BankAccount bankAccount = bankAccountResponse.getValue();
            List<Transaction> transactions = new ArrayList<>(transactionService.findByBankAccountIdBetweenDates(
                    bankAccount.getId(), startDate, endDate));

            transactions.sort(Comparator.comparing(financialmanager.objectFolder.transactionFolder.Transaction::getDate));

            List<DataPoint> dataPoints = getDataPoints(transactions);

            ChartSeries chartSeries = new ChartSeries(bankAccount.getName(), dataPoints);
            chartSeriesOptional = Optional.of(chartSeries);
        }

        return chartSeriesOptional;
    }

    private List<DataPoint> getDataPoints(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::createDataPoint)
                .collect(Collectors.toList());
    }

    private DataPoint createDataPoint(Transaction transaction) {
        double difference = Utils.getDifferenceInTransaction(transaction);

        PointStyle pointStyle = difference == 0 ? PointStyle.NORMAL
                : difference > 0 ? PointStyle.BAD
                : PointStyle.GOOD;

        String info = (pointStyle == PointStyle.NORMAL) ? null
                : localeService.getMessageWithPlaceHolder(
                pointStyle == PointStyle.BAD ? "lessAmountAfter" : "moreAmountAfter",
                List.of(String.valueOf(difference))
        );

        return new DataPoint(transaction.getAmountInBankAfter(), transaction.getDate(), info, pointStyle);
    }
}
