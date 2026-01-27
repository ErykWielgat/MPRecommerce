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

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 1024)
    private String imageUrl;

    // Relacja: Wiele produktów -> Jedna kategoria
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Review> reviews = new java.util.ArrayList<>();
}