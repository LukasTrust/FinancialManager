package financialmanager.configFolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

@Component
public class JsonMessageSource {

    private static final String MESSAGES_DIRECTORY = "src/main/resources/localization/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getMessageWithSubDirectory(String subDirectory, String key, Locale locale) {
        // Construct the filename for the JSON based on the locale
        String fileName = MESSAGES_DIRECTORY + subDirectory + "/messages_" + locale.getLanguage() + ".json";
        try {
            // Load the messages from the JSON file
            Map<String, String> messages = objectMapper.readValue(new File(fileName), Map.class);

            // Retrieve the message for the given code
            String message = messages.get(key);
            if (message != null) {
                return message;
            }
        } catch (IOException e) {
            // Handle missing file or other errors gracefully
            e.printStackTrace();
        }
        // Return a default message if the key is not found
        return key;
    }
}