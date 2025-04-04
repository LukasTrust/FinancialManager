package financialmanager.controller;

import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.chartFolder.ChartData;
import financialmanager.objectFolder.chartFolder.ChartService;
import financialmanager.objectFolder.keyFigureFolder.KeyFigure;
import financialmanager.objectFolder.keyFigureFolder.KeyFigureService;
import financialmanager.objectFolder.transactionFolder.TransactionUploadService;
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
@RequestMapping("bankAccountOverview/{bankAccountId}/data")
public class BankAccountOverviewController {

    private final ChartService chartService;
    private final KeyFigureService keyFigureService;
    private final BankAccountService bankAccountService;
    private final TransactionUploadService transactionUploadService;

    @GetMapping("/keyFigures")
    public ResponseEntity<List<KeyFigure>> getKeyFigures(
            @PathVariable("bankAccountId") Long bankAccountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(keyFigureService.getKeyFiguresOfBankAccounts(Collections.singletonList(bankAccountId), startDate, endDate));
    }

    @GetMapping("/lineChart")
    public ResponseEntity<ChartData> getLineChart(
            @PathVariable("bankAccountId") Long bankAccountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(chartService.getTransactionDateOfBankAccounts(Collections.singletonList(bankAccountId), startDate, endDate));
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadDataForTransactions(@PathVariable Long bankAccountId, @RequestParam("files") MultipartFile[] files) {
        return transactionUploadService.uploadDataForTransactions(bankAccountId, files);
    }

    @PostMapping("/removeSearchString/{listType}/{searchString}")
    public ResponseEntity<?> removeSearchString(@PathVariable Long bankAccountId, @PathVariable String listType, @PathVariable String searchString) {
        return bankAccountService.removeSearchString(bankAccountId, listType, searchString);
    }

    @PostMapping("/addSearchString/{listType}/{searchString}")
    public ResponseEntity<?> addSearchString(@PathVariable Long bankAccountId, @PathVariable String listType, @PathVariable String searchString) {
        return bankAccountService.addSearchString(bankAccountId, listType, searchString);
    }

    @PostMapping("/deleteData")
    public ResponseEntity<?> deleteData(@PathVariable Long bankAccountId) {
        return transactionUploadService.deleteData(bankAccountId);
    }
}
