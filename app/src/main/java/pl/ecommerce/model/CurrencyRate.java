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

    private String currencyCode;

    private Double rate;

    private LocalDate fetchDate;
}