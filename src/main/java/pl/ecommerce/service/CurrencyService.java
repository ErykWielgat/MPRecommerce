package pl.ecommerce.service;

import lombok.RequiredArgsConstructor;
import pl.ecommerce.nbp.NbpClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ecommerce.model.CurrencyRate;
import pl.ecommerce.nbp.NbpRateDto;
import pl.ecommerce.repository.CurrencyRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final NbpClient nbpClient; // Wstrzykujemy klienta z modułu obok
    @Transactional
    public void updateRates() {
        // 1. Używamy modułu zewnętrznego do pobrania danych
        System.out.println("Łączenie z NBP...");
        List<NbpRateDto> ratesFromInternet = nbpClient.getOfficialRates();

        if (ratesFromInternet.isEmpty()) {
            System.out.println("NBP nie odpowiedział lub brak danych.");
            return;
        }

        // 2. Czyścimy stare kursy w bazie (żeby nie dublować)
        currencyRepository.deleteAll();

        // 3. Przepisujemy DTO (z modułu) na naszą Encję (do bazy)
        for (NbpRateDto dto : ratesFromInternet) {
            CurrencyRate rate = new CurrencyRate();
            rate.setCurrencyCode(dto.getCode());
            rate.setRate(dto.getRate());
            rate.setFetchDate(LocalDate.now());

            currencyRepository.save(rate);
        }
        System.out.println("Zapisano " + ratesFromInternet.size() + " kursów walut.");
    }

    public List<CurrencyRate> getAllRates() {
        return currencyRepository.findAll();
    }
}