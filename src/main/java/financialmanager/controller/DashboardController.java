package financialmanager.controller;

import financialmanager.objectFolder.chartFolder.ChartData;
import financialmanager.objectFolder.chartFolder.ChartService;
import financialmanager.objectFolder.keyFigureFolder.KeyFigure;
import financialmanager.objectFolder.keyFigureFolder.KeyFigureService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/dashboard/data")
public class DashboardController {

    private final ChartService chartService;
    private final KeyFigureService keyFigureService;

    @GetMapping("/keyFigures")
    public ResponseEntity<List<KeyFigure>> getKeyFigures(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(keyFigureService.getKeyFiguresOfUser(startDate, endDate));
    }

    @GetMapping("/lineChart")
    public ResponseEntity<ChartData> getLineChart(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(chartService.getTransactionDateOfUser(startDate, endDate));
    }
}
