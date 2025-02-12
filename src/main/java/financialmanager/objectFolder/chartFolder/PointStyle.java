package financialmanager.objectFolder.chartFolder;

public record PointStyle(String pointBorderColor, String pointBackgroundColor, int pointBorderWidth) {
    public static final PointStyle NORMAL = new PointStyle("#000000", "#FFFFFF", 2);
    public static final PointStyle GOOD = new PointStyle("#008000", "#00FF00", 3);
    public static final PointStyle BAD = new PointStyle("#FF0000", "#FFCCCC", 3);
}
