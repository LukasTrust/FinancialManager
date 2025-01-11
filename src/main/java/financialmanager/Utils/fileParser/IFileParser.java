package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.transactionFolder.Transaction;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileParser {
    List<Transaction> createTransactionsFromData(MultipartFile file, Long bankId);
}
