package financialmanager.objectFolder.usersFolder;

import financialmanager.generalController.LocalizationController;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.AlertType;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@AllArgsConstructor
public class UsersController {

    private final String subDirectory = "login&signup";
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final LocalizationController localizationController;

    private final Pattern passwordPattern =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    private final Pattern emailPattern =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Response> createUser(@RequestBody Users user, Locale locale) {
        try {
            // Validate email
            String email = user.getEmail();
            if (!isValidEmail(email)) {
                return ResponseEntity.badRequest().body(new Response(
                        AlertType.WARNING,
                        localizationController.getMessage(subDirectory, "invalidEmail", locale)
                ));
            }

            // Validate password
            String password = user.getPassword();
//            String passwordValidationMessage = validatePassword(password, locale);
//            if (passwordValidationMessage != null) {
//                return ResponseEntity.badRequest().body(new Response(
//                        AlertType.WARNING,
//                        passwordValidationMessage
//                ));
//            }

            // Encode password
            user.setPassword(passwordEncoder.encode(password));

            // Save user
            Users savedUser = usersService.save(user);

            // Success response
            return ResponseEntity.ok(new Response(
                    AlertType.SUCCESS,
                    localizationController.getMessage(subDirectory, "success", locale),
                    savedUser
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "userExists", locale)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    AlertType.ERROR,
                    localizationController.getMessage(subDirectory, "generic", locale)
            ));
        }
    }

    // Utility method for password validation
    public String validatePassword(String password, Locale locale) {
        if (password.length() < 8) {
            return localizationController.getMessage(subDirectory, "passwordLength", locale);
        }
        if (!isStrongPassword(password)) {
            return localizationController.getMessage(subDirectory, "passwordStrength", locale);
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
