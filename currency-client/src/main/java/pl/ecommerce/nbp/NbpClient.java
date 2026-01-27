/*
 * Klient HTTP odpowiedzialny za komunikację z zewnętrznym serwisem NBP (Narodowy Bank Polski).
 * Jego zadaniem jest pobranie aktualnej tabeli kursów średnich walut obcych.
 */
package pl.ecommerce.nbp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NbpClient {

    private final RestTemplate restTemplate;

    // Metoda wysyła żądanie GET do API NBP i ręcznie przetwarza otrzymany JSON na listę naszych obiektów DTO.
    public List<NbpRateDto> getOfficialRates() {
        // Adres URL do tabeli A kursów średnich w formacie JSON.
        String url = "http://api.nbp.pl/api/exchangerates/tables/A?format=json";

        try {
            // Wykonujemy zapytanie HTTP GET
            Object[] response = restTemplate.getForObject(url, Object[].class);

            List<NbpRateDto> resultList = new ArrayList<>();

            // Sprawdzamy, czy otrzymaliśmy jakiekolwiek dane (czy tablica nie jest pusta).
            if (response != null && response.length > 0) {
                // Rzutujemy pierwszy element tablicy na mapę
                LinkedHashMap table = (LinkedHashMap) response[0];

                // Z głównego obiektu wyciągamy listę ukrytą pod kluczem "rates", która zawiera szczegóły poszczególnych walut.
                List<LinkedHashMap> rates = (List<LinkedHashMap>) table.get("rates");

                // Iterujemy po surowych danych mapy i przepisujemy je do naszego czystego obiektu NbpRateDto.
                for (LinkedHashMap rateData : rates) {
                    NbpRateDto dto = new NbpRateDto();
                    dto.setCode((String) rateData.get("code")); // Wyciągamy kod waluty, np. "USD"
                    dto.setRate((Double) rateData.get("mid"));  // Wyciągamy kurs średni, np. 4.25
                    resultList.add(dto);
                }
            }
            return resultList;

        } catch (Exception e) {
            // W bloku catch łapiemy błędy (np. brak internetu, zmiana API)
            System.err.println("Błąd pobierania NBP: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}