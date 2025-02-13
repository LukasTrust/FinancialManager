package financialmanager.objectFolder.usersFolder;

import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.ResponseService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@AllArgsConstructor
public class UsersController {

    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final ResponseService responseService;

    private final Pattern passwordPattern =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    private final Pattern emailPattern =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createUser(@RequestBody Users user) {
        try {
            // Validate email
            String email = user.getEmail();
            if (!isValidEmail(email)) {
                return responseService.createResponse(HttpStatus.BAD_REQUEST, "invalidEmail", AlertType.WARNING);
            }

            // Validate password
            String password = user.getPassword();
//            String passwordValidationMessage = validatePassword(password);
//            if (passwordValidationMessage != null) {
//                return responseService.createResponse(HttpStatus.BAD_REQUEST, passwordValidationMessage, AlertType.WARNING);
//            }

            // Encode password
            user.setPassword(passwordEncoder.encode(password));

            // Save user
            usersService.save(user);

            // Success response
            return responseService.createResponse(HttpStatus.CREATED, "signUp", AlertType.SUCCESS);

        } catch (DataIntegrityViolationException e) {
            return responseService.createResponse(HttpStatus.CONFLICT, "userExists", AlertType.ERROR);
        } catch (Exception e) {
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "generic", AlertType.ERROR);
        }
    }

    // Utility method for password validation
    public String validatePassword(String password) {
        if (password.length() < 8) {
            return "passwordLength";
        }
        if (!isStrongPassword(password)) {
            return "error_passwordStrength";
        }
        return null; // Indicates password is valid
    }

    private boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }

    private boolean isStrongPassword(String password) {
        return passwordPattern.matcher(password).matches();
    }
}
