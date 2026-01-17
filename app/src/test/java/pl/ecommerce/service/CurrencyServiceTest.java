package pl.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ecommerce.model.CurrencyRate;
import pl.ecommerce.nbp.NbpClient;
import pl.ecommerce.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private NbpClient nbpClient; // Mockujemy klienta NBP

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void shouldCalculatePriceInForeignCurrency() {
        // given
        // Kurs: 1 USD = 4.00 PLN
        CurrencyRate rate = new CurrencyRate();
        rate.setCurrencyCode("USD");
        rate.setRate(4.00);

        when(currencyRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(rate));

        // when
        // Cena 100 PLN / 4.00 = 25 USD
        BigDecimal priceInUsd = currencyService.calculatePriceInCurrency(new BigDecimal("100.00"), "USD");

        // then
        assertEquals(new BigDecimal("25.00"), priceInUsd);
    }

    @Test
    void shouldReturnSamePriceForPLN() {
        // when
        BigDecimal price = currencyService.calculatePriceInCurrency(new BigDecimal("123.00"), "PLN");

        // then
        assertEquals(new BigDecimal("123.00"), price);
    }
}