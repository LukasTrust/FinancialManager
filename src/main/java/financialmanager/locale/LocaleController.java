package financialmanager.locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class LocaleController {

    private final ObjectMapper objectMapper;
    private LocaleService localeService;
    private final UsersService usersService;

    @PostMapping("/update/locale/{locale}")
    public void updateLocale(@PathVariable Locale locale) {
        Users user = usersService.getCurrentUser();
        if (locale != Locale.ENGLISH && locale != Locale.GERMAN) {
            localeService.setCurrentLocale(Locale.ENGLISH, user.getId());
        } else {
            localeService.setCurrentLocale(locale, user.getId());
        }
    }

    // Endpoint for fetching localization files
    @GetMapping("/{subDirectory}/messages/{locale}")
    public ResponseEntity<?> getLocalization(@PathVariable String subDirectory, @PathVariable(required = false) Locale locale) {
        if (locale.getLanguage().equals("undefined")) {
            locale = null;
        }

        Users user = usersService.getCurrentUser();
        try {
            Map<String, String> messages = loadMessages(subDirectory, user, locale);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Localization file not found");
        }
    }

    // Method to fetch a specific message by key
    public String getMessage(String subDirectory, String key, Users user) {
        try {
            Map<String, String> messages = loadMessages(subDirectory, user, null);
            return messages.getOrDefault(key, key);
        } catch (IOException e) {
            return key; // Return the key itself as a fallback
        }
    }

    // Shared method to load JSON content
    private Map<String, String> loadMessages(String subDirectory, Users user, Locale locale) throws IOException {
        String filePath = "localization/" + subDirectory + "/messages_";
        filePath += locale != null ? locale.getLanguage() : localeService.getCurrentLocale(user.getId());
        filePath += ".json";
        Resource resource = new ClassPathResource(filePath);

        if (!resource.exists()) {
            throw new IOException("Localization file not found");
        }

        File file = resource.getFile();
        return objectMapper.readValue(file, Map.class);
    }
}
