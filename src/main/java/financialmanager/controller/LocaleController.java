package financialmanager.controller;

import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.localeFolder.LocaleService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.usersFolder.Users;
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

    private final LocaleService localeService;
    private final ResponseService responseService;
    private final ResultService resultService;

    @PostMapping("/update/locale/{locale}")
    public void updateLocale(@PathVariable Locale locale) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = resultService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return;
        }

        Users currentUser = currentUserResponse.getValue();

        if (locale != Locale.ENGLISH && locale != Locale.GERMAN) {
            localeService.setCurrentLocale(Locale.ENGLISH, currentUser.getId());
        } else {
            localeService.setCurrentLocale(locale, currentUser.getId());
        }
    }

    // Endpoint for fetching localization files
    @GetMapping("/clientSide/{subDirectory}/messages/{locale}")
    public ResponseEntity<?> getLocalization(@PathVariable String subDirectory, @PathVariable(required = false) Locale locale) {
        if (locale.getLanguage().equals("undefined")) {
            locale = null;
        }

        try {
            Map<String, String> messages = localeService.loadMessages("clientSide", subDirectory, locale);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(messages);
        } catch (IOException e) {
            return responseService.createResponse(HttpStatus.NOT_FOUND, "localizationNotFound", AlertType.ERROR);
        }
    }
}
