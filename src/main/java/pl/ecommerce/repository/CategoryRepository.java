package pl.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.ecommerce.model.Category;

// Rozszerzamy JpaRepository<TypEncji, TypKluczaGłównego>
// TypEncji to Category, a klucz (ID) jest typu Long
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Magia Springa: Wystarczy nazwać metodę "findByName",
    // a Spring sam stworzy zapytanie: SELECT * FROM categories WHERE name = ?
    Category findByName(String name);
}