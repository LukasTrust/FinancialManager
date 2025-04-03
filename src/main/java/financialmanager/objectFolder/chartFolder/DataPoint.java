package financialmanager.objectFolder.chartFolder;

import java.time.LocalDate;

public record DataPoint(Double value, Double amount, String counterPartyName, String info, LocalDate date, PointStyle style) {}