package financialmanager.config;

import financialmanager.objectFolder.usersFolder.BaseUsersService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final BaseUsersService baseUsersService;

    @Bean
    public UserDetailsService userDetailsService() {
        return baseUsersService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(baseUsersService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .logout((logout) -> logout.logoutSuccessUrl("/login"))
                .formLogin(
                        httpForm -> httpForm
                                .loginPage("/login")
                                .failureUrl("/login?error=true") // Redirect with error parameter
                                .defaultSuccessUrl("/", true)
                                .permitAll())
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/signup", "/css/**", "/scripts/**", "/images/**", "/localization/**").permitAll();

                    registry.anyRequest().authenticated();
                })
                .build();
    }
}
