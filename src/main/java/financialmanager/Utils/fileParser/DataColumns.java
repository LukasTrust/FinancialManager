package financialmanager.Utils.fileParser;

public record DataColumns(int counterPartyColumn, int amountColumn, int amountAfterTransactionColumn, int dateColumn) {

    public boolean checkIfAllAreFound() {
        return counterPartyColumn != 0 && amountColumn != 0 && amountAfterTransactionColumn != 0 && dateColumn != 0;
    }
}
