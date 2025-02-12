package financialmanager.controller;

import financialmanager.locale.LocaleService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/localization")
@AllArgsConstructor
public class LocaleController {

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
            Map<String, String> messages = localeService.loadMessages(subDirectory, user, locale);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Localization file not found");
        }
    }
}
