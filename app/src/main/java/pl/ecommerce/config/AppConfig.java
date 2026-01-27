/*
 * Klasa konfiguracyjna dostarczająca beany pomocnicze, w tym klienta HTTP do komunikacji zewnętrznej.
 */
package pl.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Tworzy klienta umożliwiającego pobieranie danych z zewnętrznych API (np. kursów walut z NBP).
        return new RestTemplate();
    }
}