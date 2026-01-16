package pl.ecommerce.currencyclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Używamy "excludeName" i podajemy pełną nazwę w cudzysłowie.
// Dzięki temu nie musimy robić importu, który Ci nie działa.
@SpringBootApplication(excludeName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
public class CurrencyClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyClientApplication.class, args);
    }
}