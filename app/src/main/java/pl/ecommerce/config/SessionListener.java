package pl.ecommerce.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;
import pl.ecommerce.service.CurrencyService;

@Component
public class SessionListener implements HttpSessionListener {

    private final CurrencyService currencyService;

    // Wstrzykujemy nasz serwis
    public SessionListener(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // To się wykona AUTOMATYCZNIE, gdy wejdzie nowy użytkownik
        System.out.println(">>> Nowa sesja użytkownika: " + se.getSession().getId());

        // Odpalamy sprawdzenie kursów
        currencyService.checkAndRefreshRates();
    }
}