package financialmanager.controller;

import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersRepository;
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
public class SignupController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

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
                        "Please enter a valid email address"
                ));
            }

            // Validate password
            String password = user.getPassword();
//            String passwordValidationMessage = validatePassword(password);
//            if (passwordValidationMessage != null) {
//                return ResponseEntity.badRequest().body(new Response(
//                        AlertType.WARNING,
//                        passwordValidationMessage
//                ));
//            }

            // Encode password
            user.setPassword(passwordEncoder.encode(password));

            // Save user
            Users savedUser = usersRepository.save(user);

            // Success response
            return ResponseEntity.ok(new Response(
                    AlertType.SUCCESS,
                    "Signup successful!",
                    savedUser
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response(
                    AlertType.ERROR,
                    "User with the same email already exists"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(
                    AlertType.ERROR,
                    "An unexpected error occurred. Please try again"
            ));
        }
    }

    // Utility method for password validation
    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!isStrongPassword(password)) {
            return "Password must contain a mix of upper and lower case letters, numbers, and special characters";
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
