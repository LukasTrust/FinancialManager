package financialmanager.Utils.fileParser;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@AllArgsConstructor
public class FileParserFactory {

    private static final Logger log = LoggerFactory.getLogger(FileParserFactory.class);

    public IFileParser getFileParser(MultipartFile file) {
        if (file == null) {
            log.error("File is null");
            throw new IllegalArgumentException("File is null");
        }

        String contentType = file.getContentType();
        log.info("File content type is {}", contentType);

        if (contentType == null) {
            log.error("File content type is null");
            throw new IllegalArgumentException("File content type is null");
        }

        BufferedReader bufferedReader;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        } catch (Exception e) {
            log.error("Error initializing file parser", e);
            throw new RuntimeException("Error initializing file parser");
        }

        return switch (contentType) {
            case "application/vnd.ms-excel", "text/csv" -> new CsvFileParser(bufferedReader, file.getOriginalFilename());
            default -> throw new IllegalArgumentException("Unsupported file type: " + contentType);
        };
    }
}
