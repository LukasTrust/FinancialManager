package financialmanager.generalController;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/localization")
public class LocalizationController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Shared method to load JSON content
    private Map<String, String> loadMessages(String subDirectory, String locale) throws IOException {
        String filePath = "localization/" + subDirectory + "/messages_" + locale + ".json";
        Resource resource = new ClassPathResource(filePath);

        if (!resource.exists()) {
            throw new IOException("Localization file not found");
        }

        File file = resource.getFile();
        return objectMapper.readValue(file, Map.class);
    }

    // Endpoint for fetching localization files
    @GetMapping("/{subDirectory}/messages_{locale}.json")
    public ResponseEntity<?> getLocalization(@PathVariable String subDirectory, @PathVariable String locale) {
        try {
            Map<String, String> messages = loadMessages(subDirectory, locale);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Localization file not found");
        }
    }

    // Method to fetch a specific message by key
    public String getMessage(String subDirectory, String key, Locale locale) {
        try {
            Map<String, String> messages = loadMessages(subDirectory, locale.getLanguage());
            return messages.getOrDefault(key, key);
        } catch (IOException e) {
            e.printStackTrace();
            return key; // Return the key itself as a fallback
        }
    }
}
