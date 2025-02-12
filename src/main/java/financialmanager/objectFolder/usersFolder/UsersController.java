package financialmanager.objectFolder.usersFolder;

import financialmanager.locale.LocaleService;
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

import java.util.regex.Pattern;

@RestController
@AllArgsConstructor
public class UsersController {

    private final String subDirectory = "login&signup";
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final LocaleService localeService;

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
                return ResponseEntity.badRequest().body(new Response(
                        AlertType.WARNING,
                        localeService.getMessage(subDirectory, "error_invalidEmail", user)
                ));
            }

            // Validate password
            String password = user.getPassword();
//            String passwordValidationMessage = validatePassword(password, user);
//            if (passwordValidationMessage != null) {
//                return ResponseEntity.badRequest().body(new Response(
//                       AlertType.WARNING,
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
                    localeService.getMessage(subDirectory, "success_signUp", user),
                    savedUser
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(
                    AlertType.ERROR,
                    localeService.getMessage(subDirectory, "error_userExists", user)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    AlertType.ERROR,
                    localeService.getMessage(subDirectory, "error_generic", user)
            ));
        }
    }

    // Utility method for password validation
    public String validatePassword(String password, Users user) {
        if (password.length() < 8) {
            return localeService.getMessage(subDirectory, "error_passwordLength", user);
        }
        if (!isStrongPassword(password)) {
            return localeService.getMessage(subDirectory, "error_passwordStrength", user);
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
