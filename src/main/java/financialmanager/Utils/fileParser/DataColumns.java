package financialmanager.Utils.fileParser;

public record DataColumns(Integer counterPartyColumn, Integer amountColumn, Integer amountAfterTransactionColumn, Integer dateColumn) {

    public boolean checkIfAllAreFound() {
        return counterPartyColumn != null && amountColumn != null && amountAfterTransactionColumn != null && dateColumn != null;
    }
}
