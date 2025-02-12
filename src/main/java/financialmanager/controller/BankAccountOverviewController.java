package financialmanager.controller;

import financialmanager.Utils.fileParser.FileParserFactory;
import financialmanager.Utils.fileParser.IFileParser;
import financialmanager.objectFolder.chartFolder.ChartData;
import financialmanager.objectFolder.chartFolder.ChartService;
import financialmanager.objectFolder.keyFigureFolder.KeyFigure;
import financialmanager.objectFolder.keyFigureFolder.KeyFigureService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.transactionFolder.TransactionProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class BankAccountOverviewController {

    private final FileParserFactory fileParserFactory;
    private final TransactionProcessingService transactionProcessingService;
    private final ChartService chartService;
    private final KeyFigureService keyFigureService;

    @RequestMapping("bankAccountOverview/{bankAccountId}/data/keyFigures")
    public ResponseEntity<List<KeyFigure>> getKeyFigures(
            @PathVariable("bankAccountId") Long bankAccountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Long> bankAccountIds = new ArrayList<>();
        bankAccountIds.add(bankAccountId);

        return ResponseEntity.ok(keyFigureService.getKeyFiguresOfBankAccounts(bankAccountIds, startDate, endDate));
    }

    @RequestMapping("bankAccountOverview/{bankAccountId}/data/lineChart")
    public ResponseEntity<ChartData> getLineChart(
            @PathVariable("bankAccountId") Long bankAccountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Long> bankAccountIds = new ArrayList<>();
        bankAccountIds.add(bankAccountId);

        return ResponseEntity.ok(chartService.getTransactionDate(bankAccountIds, startDate, endDate));
    }


    @PostMapping(value = "bankAccountOverview/{bankAccountId}/upload/data")
    @ResponseBody
    public ResponseEntity<?> uploadDataForTransactions(@PathVariable Long bankAccountId, @RequestParam("files") MultipartFile[] files) {
        List<CompletableFuture<ResponseEntity<Response>>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            futures.add(processFileAsync(file, bankAccountId));
        }

        List<ResponseEntity<Response>> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());


        return ResponseEntity.ok(responses);
    }

    @Async
    protected CompletableFuture<ResponseEntity<Response>> processFileAsync(MultipartFile file, Long bankAccountId) {
        IFileParser fileParser = fileParserFactory.getFileParser(file);
        ResponseEntity<Response> response = transactionProcessingService.createTransactionsFromData(fileParser, bankAccountId);
        return CompletableFuture.completedFuture(response);
    }
}
