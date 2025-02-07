package financialmanager.objectFolder.usersFolder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class UsersServiceTest {

    private UsersService usersService;
    private UsersRepository usersRepository;
    private Optional<Users> usersOptional;
    private Users user;
    private final String email = "test@test.com";

    @BeforeEach
    void setup() {
        usersRepository = mock(UsersRepository.class);
        usersService = new UsersService(usersRepository);

        user = mock(Users.class);
        when(user.getEmail()).thenReturn(email);
        when(user.getId()).thenReturn(1L);
        when(user.getPassword()).thenReturn("password");
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Smith");

        usersOptional = Optional.of(user);
    }

    @Test
    void loadUserByUsername_null(){
        assertThrows(UsernameNotFoundException.class,
                () -> usersService.loadUserByUsername(null));
    }

    @Test
    void loadUserByUsername_userFound(){
        when(usersRepository.findByEmail(email)).thenReturn(usersOptional);

        UserDetails userDetails = usersService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assert(userDetails.getUsername().equals(email));
        assert(userDetails.getPassword().equals(usersOptional.get().getPassword()));
    }

    @Test
    void loadUserByUsername_noUserFound(){
        assertThrows(UsernameNotFoundException.class,
                () -> usersService.loadUserByUsername(email));
    }

    @Test
    void save_null(){
        usersService.save(null);
    }
}