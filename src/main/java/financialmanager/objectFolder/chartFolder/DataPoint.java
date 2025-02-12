package financialmanager.objectFolder.chartFolder;

import java.time.LocalDate;

public record DataPoint(Double value, LocalDate date, String info, PointStyle style) {}