package financialmanager.objectFolder.transactionFolder;

import financialmanager.Utils.fileParser.FileParserFactory;
import financialmanager.Utils.fileParser.IFileParser;
import financialmanager.objectFolder.responseFolder.Response;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class TransactionController {

    private final FileParserFactory fileParserFactory;
    private final TransactionProcessingService transactionProcessingService;
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping(value = "/bankAccountOverview/{bankAccountId}/upload/data")
    @ResponseBody
    public ResponseEntity<?> uploadDataForTransactions(@PathVariable Long bankAccountId, @RequestParam("files") MultipartFile[] files) {
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<ResponseEntity<Response>>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            futures.add(processFileAsync(file, bankAccountId));
        }

        List<ResponseEntity<Response>> responses = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("Upload transactions took {} ms", duration);

        return ResponseEntity.ok(responses);
    }

    @Async
    protected CompletableFuture<ResponseEntity<Response>> processFileAsync(MultipartFile file, Long bankAccountId) {
        IFileParser fileParser = fileParserFactory.getFileParser(file);
        ResponseEntity<Response> response = transactionProcessingService.createTransactionsFromData(fileParser, bankAccountId);
        return CompletableFuture.completedFuture(response);
    }
}
