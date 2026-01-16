package pl.ecommerce.nbp;

import lombok.Data;

@Data
public class NbpRateDto {
    private String code; // np. "USD"
    private Double rate; // np. 4.05
}
