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
    public ResponseEntity<String> createUser(@RequestBody Users user) {
        try {
            // Validate email
            String email = user.getEmail();
            if (!isValidEmail(email)) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("warning_invalidEmail") ;
            }

            // Validate password
            String password = user.getPassword();
//            String passwordValidationMessage = validatePassword(password);
//            if (passwordValidationMessage != null) {
//                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(passwordValidationMessage);
//            }

            // Encode password
            user.setPassword(passwordEncoder.encode(password));

            // Save user
            usersService.save(user);

            // Success response
            return ResponseEntity.status(HttpStatus.OK).body("success_signUp");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("error_userExists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error_generic");
        }
    }

    // Utility method for password validation
    public String validatePassword(String password) {
        if (password.length() < 8) {
            return "warning_passwordLength";
        }
        if (!isStrongPassword(password)) {
            return "warning_passwordStrength";
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
