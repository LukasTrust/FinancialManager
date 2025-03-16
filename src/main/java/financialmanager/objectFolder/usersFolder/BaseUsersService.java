package financialmanager.objectFolder.usersFolder;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class BaseUsersService implements UserDetailsService {

    private final UsersRepository usersRepository;
    
    private static final Logger log = LoggerFactory.getLogger(BaseUsersService.class);

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

    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }

    public void save(Users user) {
        usersRepository.save(user);
    }
}
