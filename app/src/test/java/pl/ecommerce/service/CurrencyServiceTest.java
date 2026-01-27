package pl.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ecommerce.model.CurrencyRate;
import pl.ecommerce.nbp.NbpClient;
import pl.ecommerce.nbp.NbpRateDto;
import pl.ecommerce.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private NbpClient nbpClient;

    @InjectMocks
    private CurrencyService currencyService;

    // --- TESTY PRZELICZANIA CENY (calculatePriceInCurrency) ---

    @Test
    void shouldCalculatePriceInForeignCurrency() {
        // given
        CurrencyRate rate = new CurrencyRate();
        rate.setCurrencyCode("USD");
        rate.setRate(4.00);

        when(currencyRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(rate));

        // when
        // 100 PLN / 4.00 = 25 USD
        BigDecimal result = currencyService.calculatePriceInCurrency(new BigDecimal("100.00"), "USD");

        // then
        assertEquals(new BigDecimal("25.00"), result);
    }

    @Test
    void shouldReturnOriginalPriceForPLN() {
        // given - brak mockowania, bo PLN powinno wyjść od razu

        // when
        BigDecimal result = currencyService.calculatePriceInCurrency(new BigDecimal("123.00"), "PLN");

        // then
        assertEquals(new BigDecimal("123.00"), result);
    }

    @Test
    void shouldReturnOriginalPriceWhenRateNotFound() {
        // given
        // Symulujemy brak waluty w bazie (zwraca Optional.empty -> getRate zwraca 0 -> calculate zwraca input)
        when(currencyRepository.findByCurrencyCode("XYZ")).thenReturn(Optional.empty());

        // when
        BigDecimal result = currencyService.calculatePriceInCurrency(new BigDecimal("100.00"), "XYZ");

        // then
        // Metoda powinna zwrócić 100.00, zamiast rzucić błąd lub dzielić przez zero
        assertEquals(new BigDecimal("100.00"), result);
    }

    // --- TESTY AKTUALIZACJI KURSÓW (checkAndRefreshRates) ---

    @Test
    void shouldUpdateRatesWhenDatabaseIsEmpty() {
        // given
        // 1. Symulujemy, że baza jest pusta (więc brakuje USD, EUR itd.)
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        // 2. Symulujemy, że klient NBP zwraca nowe kursy
        NbpRateDto nbpRate = new NbpRateDto();
        nbpRate.setCode("USD");
        nbpRate.setRate(4.50);
        when(nbpClient.getOfficialRates()).thenReturn(List.of(nbpRate));

        // when
        currencyService.checkAndRefreshRates();

        // then
        // Sprawdzamy czy wywołano czyszczenie bazy
        verify(currencyRepository).deleteAll();
        // Sprawdzamy czy zapisano nowy kurs
        verify(currencyRepository).save(any(CurrencyRate.class));
    }

    @Test
    void shouldNotUpdateRatesWhenDataIsUpToDate() {
        // given
        // Symulujemy, że mamy w bazie wszystkie potrzebne waluty z dzisiejszą datą
        LocalDate today = LocalDate.now();
        List<CurrencyRate> rates = List.of(
                createRate("USD", today),
                createRate("EUR", today),
                createRate("GBP", today),
                createRate("CHF", today)
        );

        when(currencyRepository.findAll()).thenReturn(rates);

        // when
        currencyService.checkAndRefreshRates();

        // then
        verify(nbpClient, never()).getOfficialRates();
        verify(currencyRepository, never()).deleteAll();
    }

    // Metoda pomocnicza do tworzenia obiektów w teście
    private CurrencyRate createRate(String code, LocalDate date) {
        CurrencyRate r = new CurrencyRate();
        r.setCurrencyCode(code);
        r.setFetchDate(date);
        r.setRate(1.0);
        return r;
    }
}