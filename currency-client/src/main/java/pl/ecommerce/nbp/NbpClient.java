package pl.ecommerce.nbp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor // <--- 1. Lombok wygeneruje konstruktor dla pola final
public class NbpClient {

    // 2. Usuwamy "= new RestTemplate()". Teraz Spring wstrzyknie to pole.
    private final RestTemplate restTemplate;

    public List<NbpRateDto> getOfficialRates() {
        String url = "http://api.nbp.pl/api/exchangerates/tables/A?format=json";

        try {
            Object[] response = restTemplate.getForObject(url, Object[].class);

            List<NbpRateDto> resultList = new ArrayList<>();

            if (response != null && response.length > 0) {
                LinkedHashMap table = (LinkedHashMap) response[0];
                List<LinkedHashMap> rates = (List<LinkedHashMap>) table.get("rates");

                for (LinkedHashMap rateData : rates) {
                    NbpRateDto dto = new NbpRateDto();
                    dto.setCode((String) rateData.get("code"));
                    dto.setRate((Double) rateData.get("mid"));
                    resultList.add(dto);
                }
            }
            return resultList;

        } catch (Exception e) {
            System.err.println("Błąd pobierania NBP: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}