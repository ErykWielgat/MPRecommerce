package pl.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Wyłączamy CSRF dla ułatwienia na start (formularze POST będą działać bez tokena)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Tylko zalogowany z rolą ADMIN wejdzie tutaj
                        .anyRequest().permitAll() // Wszystko inne (sklep, koszyk, css) jest dostępne dla każdego
                )
                .formLogin(login -> login
                        .defaultSuccessUrl("/admin", true) // Gdzie przekierować po udanym logowaniu
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // Gdzie po wylogowaniu
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Tworzymy testowego admina w pamięci aplikacji
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin67")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}