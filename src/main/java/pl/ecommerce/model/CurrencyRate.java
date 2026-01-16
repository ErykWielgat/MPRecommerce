package pl.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "currency_rates")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode; // np. "USD"

    private Double rate;         // np. 4.05

    private LocalDate fetchDate; // Data pobrania kursu
}