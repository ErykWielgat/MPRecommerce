package pl.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ecommerce.model.CurrencyRate;

public interface CurrencyRepository extends JpaRepository<CurrencyRate, Long> {
    // Puste w środku - Spring Data zrobi resztę automatycznie
}