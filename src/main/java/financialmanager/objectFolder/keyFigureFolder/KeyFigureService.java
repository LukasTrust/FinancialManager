package financialmanager.objectFolder.keyFigureFolder;

import financialmanager.Utils.Result.Result;
import financialmanager.Utils.Utils;
import financialmanager.locale.LocaleService;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.contractFolder.ContractService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.transactionFolder.TransactionService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final ContractService contractService;
    private final BankAccountService bankAccountService;
    private final LocaleService localeService;

    private static final Logger log = LoggerFactory.getLogger(KeyFigureService.class);

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
            Result<BankAccount, ResponseEntity<Response>> bankAccountResponse = bankAccountService.findById(bankAccountId);

            if (bankAccountResponse.isErr()) {
                continue;
            }

            List<Transaction> bankAccountTransactions = transactionService.findByBankAccountIdBetweenDates(bankAccountId, start, end);
            allTransactions.addAll(bankAccountTransactions);

            if (bankAccountResponse.getValue() instanceof SavingsBankAccount) {
                discrepancy += getDiscrepancy(bankAccountTransactions);
                isSavingsAccount = true;
            } else {
                List<Contract> contracts = contractService.findByBankAccountIdBetweenDates(bankAccountId, start, end);
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