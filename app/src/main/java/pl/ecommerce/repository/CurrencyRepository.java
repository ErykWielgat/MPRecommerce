package pl.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ecommerce.model.CurrencyRate;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<CurrencyRate, Long> {

    // Spring Data automatycznie wygeneruje zapytanie SQL: SELECT * FROM ... WHERE currency_code = ?
    Optional<CurrencyRate> findByCurrencyCode(String currencyCode);
}