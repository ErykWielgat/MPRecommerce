/*
 * Interfejs repozytorium (Data Access Layer) dla produktów: umożliwia zaawansowane wyszukiwanie z filtrowaniem dynamicznym bezpośrednio w bazie danych.
 */
package pl.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Metoda zwracająca pełną listę wyników (bez podziału na strony)
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:name IS NULL OR lower(p.name) LIKE lower(concat('%', :name, '%')))")
    List<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("name") String name,
            Sort sort);


    // Metoda zwracająca wyniki porcjowane (strona po stronie)
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:name IS NULL OR lower(p.name) LIKE lower(concat('%', :name, '%')))")
    Page<Product> searchProducts(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("name") String name,
            Pageable pageable);
}