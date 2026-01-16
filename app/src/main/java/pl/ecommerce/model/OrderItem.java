package pl.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId; // Zapisujemy ID, żeby wiedzieć co to było
    private String productName; // Zapisujemy nazwę (gdyby produkt usunięto ze sklepu, w zamówieniu musi zostać nazwa)
    private BigDecimal price; // Cena W MOMENCIE ZAKUPU (ważne!)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}