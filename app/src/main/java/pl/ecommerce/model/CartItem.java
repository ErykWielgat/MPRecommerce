package pl.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Long productId;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;

    // Metoda pomocnicza do liczenia wartości pozycji (cena * ilość)
    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}