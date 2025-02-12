package financialmanager.objectFolder.responseFolder;

import financialmanager.locale.LocaleService;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ResponseService {

    private final LocaleService localeService;
    private final UsersService usersService;

    private ResponseEntity<Response> buildResponse(HttpStatus status, String subDirectory, String key, AlertType alertType, Object data, List<String> placeholders) {
        String message = (placeholders == null || placeholders.isEmpty())
                ? localeService.getMessage(subDirectory, key, usersService.getCurrentUser())
                : localeService.getMessageWithPlaceHolder(subDirectory, key, usersService.getCurrentUser(), placeholders);

        return ResponseEntity.status(status).body(new Response(alertType, message, data));
    }

    public ResponseEntity<Response> createResponse(HttpStatus status, String subDirectory, String key, AlertType alertType) {
        return buildResponse(status, subDirectory, key, alertType, null, null);
    }

    public ResponseEntity<Response> createResponseWithPlaceHolders(HttpStatus status, String subDirectory, String key, AlertType alertType, List<String> placeholders) {
        return buildResponse(status, subDirectory, key, alertType, null, placeholders);
    }

    public ResponseEntity<Response> createResponseWithData(HttpStatus status, String subDirectory, String key, AlertType alertType, Object data) {
        return buildResponse(status, subDirectory, key, alertType, data, null);
    }

    public ResponseEntity<Response> createResponseWithDataAndPlaceHolders(HttpStatus status, String subDirectory, String key, AlertType alertType, Object data, List<String> placeholders) {
        return buildResponse(status, subDirectory, key, alertType, data, placeholders);
    }
}
