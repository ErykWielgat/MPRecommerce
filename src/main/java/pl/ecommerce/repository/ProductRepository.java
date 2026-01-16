package pl.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Zwykłe szukanie po kategorii (po nazwie pola w klasie Category)
    List<Product> findByCategoryName(String categoryName);

    // 2. Obsługa Paginacji (Pageable) - wymagane w projekcie
    // Pozwala pobrać np. "stronę 2, po 10 produktów"
    Page<Product> findAll(Pageable pageable);

    // Szukanie po kategorii z paginacją
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // 3. Custom Query (Własne zapytanie JPQL) - wymagane w projekcie
    // JPQL to taki SQL, ale operujący na obiektach (p to Product)
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findProductsInPriceRange(@Param("minPrice") BigDecimal min, @Param("maxPrice") BigDecimal max);
}