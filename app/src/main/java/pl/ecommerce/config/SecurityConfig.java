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
                .csrf(csrf -> csrf.disable()) // Wyłączone dla ułatwienia testów API
                .authorizeHttpRequests(auth -> auth
                        // 1. ENDPOINTY PUBLICZNE
                        .requestMatchers(
                                "/",                // Strona główna
                                "/api/**",          // REST API
                                "/product/**",      // Szczegóły produktu
                                "/cart/**",         // Koszyk
                                "/order/**",        // Składanie zamówienia
                                "/order/**",        // Proces zamawiania
                                "/css/**",          // Style
                                "/js/**",           // Skrypty
                                "/uploads/**",      // Zdjęcia produktów
                                "/login"            // Strona logowania
                        ).permitAll()

                        // 2. ENDPOINTY SPECYFICZNE (Tylko dla Administratora)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 3. RESZTA CHRONIONA
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .defaultSuccessUrl("/admin", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}