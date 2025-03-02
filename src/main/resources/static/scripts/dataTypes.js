var AlertType;
(function (AlertType) {
    AlertType["SUCCESS"] = "SUCCESS";
    AlertType["WARNING"] = "WARNING";
    AlertType["ERROR"] = "ERROR";
    AlertType["INFO"] = "INFO";
})(AlertType || (AlertType = {}));
class PointStyle {
    constructor(pointBorderColor, pointBackgroundColor, pointBorderWidth) {
        this.pointBorderColor = pointBorderColor;
        this.pointBackgroundColor = pointBackgroundColor;
        this.pointBorderWidth = pointBorderWidth;
    }
}
PointStyle.NORMAL = new PointStyle("#000000", "#FFFFFF", 2);
PointStyle.GOOD = new PointStyle("#008000", "#00FF00", 3);
PointStyle.BAD = new PointStyle("#FF0000", "#FFCCCC", 3);
class DataPoint {
    constructor(value, date, info, style) {
        this.value = value;
        this.date = date;
        this.info = info;
        this.style = style;
    }
}
class ChartSeries {
    constructor(name, dataPoints) {
        this.name = name;
        this.dataPoints = dataPoints;
    }
}
class ChartData {
    constructor(title, seriesList) {
        this.title = title;
        this.seriesList = seriesList;
    }
}
//# sourceMappingURL=dataTypes.js.map