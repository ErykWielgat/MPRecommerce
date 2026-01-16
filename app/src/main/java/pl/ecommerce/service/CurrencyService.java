package pl.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ecommerce.model.CurrencyRate;
import pl.ecommerce.nbp.NbpClient;
import pl.ecommerce.nbp.NbpRateDto;
import pl.ecommerce.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final NbpClient nbpClient;

    // --- METODA 1: Pobiera kursy z NBP i zapisuje do bazy ---
    @Transactional
    public void updateRates() {
        List<NbpRateDto> rates = nbpClient.getOfficialRates();
        if (rates.isEmpty()) return;

        // Usuwamy stare, żeby nie robić śmietnika
        currencyRepository.deleteAll();

        for (NbpRateDto dto : rates) {
            CurrencyRate rate = new CurrencyRate();
            rate.setCurrencyCode(dto.getCode());
            rate.setRate(dto.getRate());
            rate.setFetchDate(LocalDate.now());
            currencyRepository.save(rate);
        }
    }

    // --- METODA 2: Zwraca jeden konkretny kurs (bezpiecznie) ---
    // Tego Ci brakowało do wyświetlania na stronie głównej!
    public BigDecimal getRate(String currencyCode) {
        return currencyRepository.findByCurrencyCode(currencyCode)
                .map(rate -> BigDecimal.valueOf(rate.getRate())) // Zamiana double na BigDecimal
                .orElse(BigDecimal.ZERO); // Jak nie znajdzie, zwraca 0 (żeby nie było błędu)
    }

    // --- METODA 3: Sprawdza czy trzeba odświeżyć (dla SessionListenera) ---
    @Transactional
    public void checkAndRefreshRates() {
        boolean isUpdatedToday = currencyRepository.findAll().stream()
                .anyMatch(r -> r.getFetchDate().equals(LocalDate.now()));

        if (!isUpdatedToday) {
            updateRates(); // Wywołujemy metodę nr 1
        }
    }
}