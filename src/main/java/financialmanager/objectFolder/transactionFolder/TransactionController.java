package financialmanager.objectFolder.transactionFolder;

import financialmanager.Utils.fileParser.FileParserFactory;
import financialmanager.Utils.fileParser.IFileParser;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class TransactionController {

    private final FileParserFactory fileParserFactory;
    private final TransactionProcessingService transactionProcessingService;

    @PostMapping(value = "/bankAccountOverview/{bankAccountId}/upload/data")
    @ResponseBody
    public ResponseEntity<?> uploadDataForTransactions(
            @PathVariable Long bankAccountId,
            @RequestParam("files") MultipartFile[] files) {
        try {
            for (MultipartFile file : files) {
                IFileParser fileParser = fileParserFactory.getFileParser(file);
                var test = transactionProcessingService.createTransactionsFromData(fileParser, bankAccountId);
            }
            return ResponseEntity.ok("Files processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing files: " + e.getMessage());
        }
    }
}
