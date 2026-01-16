package pl.ecommerce.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pl.ecommerce.dto.ProductPriceSummary;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository // Mówi Springowi: to jest komponent dostępu do danych
@RequiredArgsConstructor
public class ProductJdbcDao {

    private final JdbcTemplate jdbcTemplate; // To jest nasze narzędzie do czystego SQL

    // WYMAGANIE 1: SELECT z query() i RowMapper
    // Pobiera produkty droższe niż podana kwota
    public List<ProductPriceSummary> findProductsMoreExpensiveThan(BigDecimal minPrice) {
        String sql = "SELECT id, name, price FROM products WHERE price > ?";

        // RowMapper tłumaczy wiersz z tabeli SQL na obiekt Java
        RowMapper<ProductPriceSummary> mapper = new RowMapper<ProductPriceSummary>() {
            @Override
            public ProductPriceSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new ProductPriceSummary(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price")
                );
            }
        };

        // Wykonanie zapytania
        return jdbcTemplate.query(sql, mapper, minPrice);
    }

    // WYMAGANIE 2: Operacje UPDATE z update()
    // Masowa podwyżka/obniżka cen o konkretną wartość dla danej kategorii
    public int updatePriceByCategory(Long categoryId, BigDecimal amountToAdd) {
        String sql = "UPDATE products SET price = price + ? WHERE category_id = ?";

        // update() zwraca liczbę zmienionych wierszy
        return jdbcTemplate.update(sql, amountToAdd, categoryId);
    }

    // WYMAGANIE 3: INSERT (Dodatkowe, np. prosty log operacji - zrobimy symulację)
    // Normalnie inserty robimy przez JPA, ale tu pokażemy, że się da
    public void insertSimpleProductAudit(String action) {
        // Zakładamy, że nie mamy tabeli audit, więc to tylko przykład metody
        // String sql = "INSERT INTO audit_logs (action, timestamp) VALUES (?, NOW())";
        // jdbcTemplate.update(sql, action);
    }
}