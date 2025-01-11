package financialmanager.Utils.fileParser;

import financialmanager.objectFolder.responseFolder.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IFileParser {
    ResponseEntity<Response> createTransactionsFromData(MultipartFile file, Long bankId);
}
