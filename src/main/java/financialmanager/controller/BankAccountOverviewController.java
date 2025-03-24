package financialmanager.controller;

import financialmanager.objectFolder.chartFolder.ChartData;
import financialmanager.objectFolder.chartFolder.ChartService;
import financialmanager.objectFolder.keyFigureFolder.KeyFigure;
import financialmanager.objectFolder.keyFigureFolder.KeyFigureService;
import financialmanager.objectFolder.transactionFolder.TransactionProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("bankAccountOverview")
public class BankAccountOverviewController {

    private final ChartService chartService;
    private final KeyFigureService keyFigureService;
    private final TransactionProcessingService transactionProcessingService;

    @GetMapping("/{bankAccountId}/data/keyFigures")
    public ResponseEntity<List<KeyFigure>> getKeyFigures(
            @PathVariable("bankAccountId") Long bankAccountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(keyFigureService.getKeyFiguresOfBankAccounts(Collections.singletonList(bankAccountId), startDate, endDate));
    }

    @GetMapping("/{bankAccountId}/data/lineChart")
    public ResponseEntity<ChartData> getLineChart(
            @PathVariable("bankAccountId") Long bankAccountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(chartService.getTransactionDate(Collections.singletonList(bankAccountId), startDate, endDate));
    }

    @PostMapping("/{bankAccountId}/data/upload")
    @ResponseBody
    public ResponseEntity<?> uploadDataForTransactions(@PathVariable Long bankAccountId, @RequestParam("files") MultipartFile[] files) {
        return transactionProcessingService.uploadDataForTransactions(bankAccountId, files);
    }

    @PostMapping("/{bankAccountId}/data/deleteData")
    public ResponseEntity<?> deleteData(@PathVariable Long bankAccountId) {
        return transactionProcessingService.deleteData(bankAccountId);
    }
}
