package pl.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity // 1. Mówi Hibernate'owi: "To będzie tabela w bazie danych"
@Table(name = "categories") // 2. Nazwa tabeli w bazie to "categories"
@Getter @Setter // 3. Lombok automatycznie generuje gettery i settery (nie musisz ich pisać)
@NoArgsConstructor // 4. Pusty konstruktor (wymagany przez JPA)
@AllArgsConstructor // 5. Konstruktor ze wszystkimi polami
public class Category {

    @Id // 6. To jest klucz główny (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 7. Baza danych sama nada kolejny numer ID (Auto Increment)
    private Long id;

    @Column(nullable = false) // 8. To pole nie może być puste w bazie
    private String name;

    private String description;

    // Relacja: Jedna kategoria -> Wiele produktów
    // mappedBy = "category" oznacza, że właścicielem relacji jest pole 'category' w klasie Product
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;
}