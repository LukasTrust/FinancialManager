package financialmanager.objectFolder.usersFolder;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Result;
import financialmanager.Utils.Result.Ok;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UsersService implements UserDetailsService {

    private final UsersRepository usersRepository;
    
    private static final Logger log = LoggerFactory.getLogger(UsersService.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Users> usersOptional = usersRepository.findByEmail(email);
        if (usersOptional.isPresent()) {
            Users user = usersOptional.get();
            return User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .build();
        }
        else{
            log.error("User not found, email: {}", email);
            throw new UsernameNotFoundException(email);
        }
    }

    public void save(Users user) {
        usersRepository.save(user);
    }

    public Result<Users, ResponseEntity<Response>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            Optional<Users> usersOptional = usersRepository.findByEmail(username);

            if (usersOptional.isPresent()) {
                return new Ok<>(usersOptional.get());
            }

            log.error("Current User not found, email: {}", username);
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(AlertType.ERROR, "User not found", null)));
        } catch (Exception e) {
            log.error("Error while getting current user", e);
            return new Err<>(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(AlertType.ERROR, "Internal server error", null)));
        }
    }
}
