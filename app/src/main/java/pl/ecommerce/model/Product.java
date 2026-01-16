package pl.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Używamy BigDecimal do pieniędzy, bo double ma problemy z precyzją (np. 0.1 + 0.2 != 0.3)
    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl; // Link do zdjęcia lub nazwa pliku

    // Relacja: Wiele produktów -> Jedna kategoria
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false) // 9. W tabeli produktów powstanie kolumna 'category_id'
    private Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Review> reviews = new java.util.ArrayList<>();
}