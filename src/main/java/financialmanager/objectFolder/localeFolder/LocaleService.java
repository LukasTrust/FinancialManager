package financialmanager.objectFolder.localeFolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import financialmanager.objectFolder.usersFolder.BaseUsersService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@AllArgsConstructor
public class LocaleService {

    private final Map<Long, Locale> locales = new HashMap<>();
    private final ObjectMapper objectMapper;
    private final BaseUsersService baseUsersService;

    public Locale getCurrentLocale() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Optional<Users> usersOptional = baseUsersService.findByEmail(username);

        if (usersOptional.isEmpty()) {
            return Locale.ENGLISH;
        }

        Users currentUser = usersOptional.get();
        Long userId = currentUser.getId();

        if (locales.containsKey(userId)) {
            return locales.get(userId);
        }

        return Locale.ENGLISH;
    }

    public void setCurrentLocale(Locale locale, Long userId) {
        if (userId == null) {
            return;
        }

        if (locales.containsKey(userId)) {
            locales.replace(userId, locale);
        } else {
            locales.put(userId, locale);
        }
    }

    // Method to fetch a specific message by key
    public String getMessage(String subDirectory, String key) {
        try {
            Map<String, String> messages = loadMessages("serverSide", subDirectory, null);
            return messages.getOrDefault(key, key);
        } catch (IOException e) {
            return key; // Return the key itself as a fallback
        }
    }

    public String getMessage(String key) {
        try {
            Map<String, String> messages = loadMessages("serverSide", "info", null);
            return messages.getOrDefault(key, key);
        } catch (IOException e) {
            return key; // Return the key itself as a fallback
        }
    }

    public String getMessageWithPlaceHolder(String subDirectory, String key, List<String> placeHolders) {
        try {
            Map<String, String> messages = loadMessages("serverSide", subDirectory, null);

            String message = messages.getOrDefault(key, key);

            for (String placeholder : placeHolders) {
                message = message.replaceFirst("\\{}", placeholder);
            }
            return message;
        } catch (IOException e) {
            return key; // Return the key itself as a fallback
        }
    }

    public String getMessageWithPlaceHolder(String key, List<String> placeHolders) {
        try {
            Map<String, String> messages = loadMessages("serverSide", "info", null);

            String message = messages.getOrDefault(key, key);

            for (String placeholder : placeHolders) {
                message = message.replaceFirst("\\{}", placeholder);
            }
            return message;
        } catch (IOException e) {
            return key; // Return the key itself as a fallback
        }
    }

    // Shared method to load JSON content
    public Map<String, String> loadMessages(String mainDirectory, String subDirectory, Locale locale) throws IOException {
        String filePath = "localization/" + mainDirectory + "/" + subDirectory + "/messages_";
        filePath += locale != null ? locale.getLanguage() : getCurrentLocale();
        filePath += ".json";
        Resource resource = new ClassPathResource(filePath);

        if (!resource.exists()) {
            throw new IOException("Localization file not found");
        }

        File file = resource.getFile();
        return objectMapper.readValue(file, new TypeReference<>() {});
    }
}
