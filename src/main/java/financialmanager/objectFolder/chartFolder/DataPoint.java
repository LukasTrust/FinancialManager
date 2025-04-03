package financialmanager.objectFolder.chartFolder;

import java.time.LocalDate;

public record DataPoint(Double value, String counterPartyName, String info, LocalDate date, PointStyle style) {}