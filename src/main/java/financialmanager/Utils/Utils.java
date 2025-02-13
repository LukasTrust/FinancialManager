package financialmanager.Utils;

import financialmanager.objectFolder.transactionFolder.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class Utils {
    public static double getDifferenceInTransaction(Transaction transaction) {
        double calculatedAmountAfter = roundToTwoDecimals(transaction.getAmount() + transaction.getAmountInBankBefore());
        return roundToTwoDecimals(calculatedAmountAfter - transaction.getAmountInBankAfter());
    }

    public static double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static LocalDate[] getRightDateRange(LocalDate startDate, LocalDate endDate) {
        return new LocalDate[]{(startDate == null) ? LocalDate.MIN : startDate, (endDate == null) ? LocalDate.MAX : endDate};
    }

    public static LocalDate[] normalizeDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return new LocalDate[]{endDate, startDate};
        }
        return new LocalDate[]{startDate, endDate};
    }

    public static Transaction getFirstTransaction(List<Transaction> transactions) {
        return transactions.stream().min(Comparator.comparing(financialmanager.objectFolder.transactionFolder.Transaction::getDate)).orElse(null);
    }

    public static Transaction getLastTransaction(List<Transaction> transactions) {
        return transactions.stream().max(Comparator.comparing(financialmanager.objectFolder.transactionFolder.Transaction::getDate)).orElse(null);
    }
}
