package pl.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductPriceSummary {
    private Long id;
    private String name;
    private BigDecimal price;
}