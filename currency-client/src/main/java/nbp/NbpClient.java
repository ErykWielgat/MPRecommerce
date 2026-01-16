package nbp;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component // Ważne: To oznacza, że Spring zarządza tą klasą (Bean)
public class NbpClient {

    // RestTemplate to narzędzie do wysyłania zapytań HTTP (jak przeglądarka w kodzie)
    private final RestTemplate restTemplate = new RestTemplate();

    public List<NbpRateDto> getOfficialRates() {
        String url = "http://api.nbp.pl/api/exchangerates/tables/A?format=json";

        try {
            // Pobieramy dane z URL (Object[], bo NBP zwraca tablicę JSON)
            Object[] response = restTemplate.getForObject(url, Object[].class);

            List<NbpRateDto> resultList = new ArrayList<>();

            if (response != null && response.length > 0) {
                // Ręczne wyciąganie danych z mapy (LinkedHashMap), żeby nie tworzyć skomplikowanych struktur
                LinkedHashMap table = (LinkedHashMap) response[0];
                List<LinkedHashMap> rates = (List<LinkedHashMap>) table.get("rates");

                for (LinkedHashMap rateData : rates) {
                    NbpRateDto dto = new NbpRateDto();
                    dto.setCode((String) rateData.get("code"));
                    // "mid" to nazwa pola w API NBP oznaczająca średni kurs
                    dto.setRate((Double) rateData.get("mid"));
                    resultList.add(dto);
                }
            }
            return resultList;

        } catch (Exception e) {
            // W razie braku internetu lub błędu API zwracamy pustą listę, żeby nie wywalić całej apki
            System.err.println("Błąd pobierania NBP: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
