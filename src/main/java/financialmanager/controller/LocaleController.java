package financialmanager.controller;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Ok;
import financialmanager.Utils.Result.Result;
import financialmanager.locale.LocaleService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
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
    private ResponseService responseService;
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
    @GetMapping("/clientSide/{subDirectory}/messages/{locale}")
    public ResponseEntity<?> getLocalization(@PathVariable String subDirectory, @PathVariable(required = false) Locale locale) {
        if (locale.getLanguage().equals("undefined")) {
            locale = null;
        }

        Users user = usersService.getCurrentUser();
        try {
            Map<String, String> messages = localeService.loadMessages("clientSide", subDirectory, user, locale);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages);
        } catch (IOException e) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "localizationNotFound", AlertType.ERROR);
        }
    }
}
