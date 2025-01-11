package financialmanager.objectFolder.responseFolder;

import financialmanager.locale.LocaleController;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

@Service
@AllArgsConstructor
public class ResponseService {

    private final LocaleController localeController;
    private final UsersService usersService;

    public ResponseEntity<Response> createResponse(String subDirectory, String key, AlertType alertType) {
        String message = localeController.getMessage(subDirectory, key, usersService.getCurrentUser());

        return ResponseEntity.ok(new Response(alertType, message));
    }

    public ResponseEntity<Response> createErrorResponse(String subDirectory, String errorKey, String additionalInfo,
                                                        HttpStatus status) {
        String message = localeController.getMessage(subDirectory, errorKey, usersService.getCurrentUser());
        if (additionalInfo != null) {
            message = message.replace("{}", additionalInfo);
        }
        return ResponseEntity.status(status).body(new Response(AlertType.ERROR, message));
    }

    public ResponseEntity<Response> createErrorResponseWithData(String subDirectory, String errorKey, String additionalInfo,
                                                        HttpStatus status, Object data) {
        String message = localeController.getMessage(subDirectory, errorKey, usersService.getCurrentUser());
        if (additionalInfo != null) {
            message = message.replace("{}", additionalInfo);
        }
        return ResponseEntity.status(status).body(new Response(AlertType.ERROR, message, data));
    }
}
