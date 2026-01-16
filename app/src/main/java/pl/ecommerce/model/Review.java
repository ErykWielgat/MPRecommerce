package pl.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorName; // Np. "Anonim" lub imię usera

    @Column(nullable = false)
    private String content; // Treść opinii

    @Column(nullable = false)
    private int rating; // Ocena 1-5

    // Relacja: Wiele opinii do jednego produktu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}