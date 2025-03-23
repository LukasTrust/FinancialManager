package financialmanager.objectFolder.keyFigureFolder;

import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.Utils.Utils;
import financialmanager.objectFolder.localeFolder.LocaleService;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class KeyFigureService {

    private final TransactionService transactionService;
    private final LocaleService localeService;
    private final ResultService resultService;

    public List<KeyFigure> getKeyFiguresOfBankAccounts(List<Long> bankAccountIds, LocalDate startDate, LocalDate endDate) {
        LocalDate[] dates = Utils.normalizeDateRange(startDate, endDate);
        LocalDate start = dates[0];
        LocalDate end = dates[1];

        List<Transaction> allTransactions = new ArrayList<>();

        double average;
        double netGainLoss;
        double discrepancy = 0;
        double contractCostPerMonth = 0;
        boolean isSavingsAccount = false;

        for (Long bankAccountId : bankAccountIds) {
            Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

            if (bankAccountResult.isErr()) {
                continue;
            }

            BankAccount bankAccount = bankAccountResult.getValue();

            List<Transaction> bankAccountTransactions = transactionService.findByBankAccountBetweenDates(bankAccount, start, end);
            allTransactions.addAll(bankAccountTransactions);

            if (bankAccountResult.getValue() instanceof SavingsBankAccount) {
                discrepancy += getDiscrepancy(bankAccountTransactions);
                isSavingsAccount = true;
            } else {
                List<Contract> contracts = resultService.findContractsByBankAccountIdBetweenDates(bankAccount, start, end);
                contractCostPerMonth += getContractCostPerMonth(contracts);
            }
        }

        if (allTransactions.isEmpty()) {
            return createBankAccountKeyFigures(0, 0, isSavingsAccount, 0);
        }

        average = getAverage(allTransactions);
        netGainLoss = getNetGainLoss(allTransactions);
        double lastValue = isSavingsAccount ? Utils.roundToTwoDecimals(discrepancy) : Utils.roundToTwoDecimals(contractCostPerMonth);

        return createBankAccountKeyFigures(
                Utils.roundToTwoDecimals(average),
                Utils.roundToTwoDecimals(netGainLoss),
                isSavingsAccount,
                lastValue);
    }

    private List<KeyFigure> createBankAccountKeyFigures(double average, double netGainLoss,
                                                        boolean isSavingsBankAccount, double lastValue) {
        List<KeyFigure> keyFigures = new ArrayList<>();

        keyFigures.add(createKeyFigure("average", "averageTooltip", average));
        keyFigures.add(createKeyFigure("netGainLoss", "netGainLossTooltip", netGainLoss));
        if (isSavingsBankAccount) {
            keyFigures.add(createKeyFigure("discrepancy", "discrepancyTooltip", lastValue));
        } else {
            keyFigures.add(createKeyFigure("contractCostPerMonth", "contractCostPerMonthTooltip", lastValue));
        }

        return keyFigures;
    }

    private double getAverage(List<Transaction> transactions) {
        double sum = transactions.stream().mapToDouble(financialmanager.objectFolder.transactionFolder.Transaction::getAmount).sum();
        return transactions.isEmpty() ? 0 : sum / transactions.size(); // Avoid division by zero
    }

    private double getNetGainLoss(List<Transaction> transactions) {
        double firstBalance = Optional.ofNullable(Utils.getFirstTransaction(transactions))
                .map(financialmanager.objectFolder.transactionFolder.Transaction::getAmountInBankAfter).orElse(0.0);
        double lastBalance = Optional.ofNullable(Utils.getLastTransaction(transactions))
                .map(financialmanager.objectFolder.transactionFolder.Transaction::getAmountInBankAfter).orElse(0.0);
        return lastBalance - firstBalance;
    }

    private double getContractCostPerMonth(List<Contract> contracts) {
        return contracts.stream().mapToDouble(Contract::getAmount).sum();
    }

    private double getDiscrepancy(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(Utils::getDifferenceInTransaction)
                .sum();
    }

    private KeyFigure createKeyFigure(String key, String tooltipKey, double value) {
        return new KeyFigure(
                localeService.getMessage(key),
                localeService.getMessage(tooltipKey),
                value
        );
    }
}