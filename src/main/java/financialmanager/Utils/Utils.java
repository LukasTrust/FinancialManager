package financialmanager.Utils;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;
import financialmanager.objectFolder.transactionFolder.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<Contract, List<ContractHistory>> mapContractHistoryToContract(List<Contract> contracts, List<ContractHistory> contractHistories) {
        Map<Contract, List<ContractHistory>> contractHistoryMap = new HashMap<>();

        for (Contract contract : contracts) {
            List<ContractHistory> contractHistoriesOfContract = contractHistories.stream().filter(contractHistory ->
                    contractHistory.getContract() == contract).toList();

            contractHistoryMap.put(contract, contractHistoriesOfContract);
        }

        return contractHistoryMap;
    }
}
