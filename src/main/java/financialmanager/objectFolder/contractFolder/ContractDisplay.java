package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.contractFolder.contractHistoryFolder.ContractHistory;

import java.util.List;

public record ContractDisplay(Contract contract, List<ContractHistory> contractHistories, Integer transactionCount, Double totalAmount) {
}
