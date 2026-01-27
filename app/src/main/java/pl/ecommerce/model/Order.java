package pl.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date = LocalDateTime.now();

    // Status: NEW, PAID, SHIPPED itp.
    private String status = "NEW";

    // Dane klienta
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String zipCode;
    private String city;

    // Metoda dostawy
    private String deliveryMethod;
    private BigDecimal deliveryCost;

    private BigDecimal totalAmount; // Łącznie z dostawą

    // Lista produktów w zamówieniu
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
}