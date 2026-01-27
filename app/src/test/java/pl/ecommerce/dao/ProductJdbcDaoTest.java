package pl.ecommerce.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import pl.ecommerce.dto.ProductPriceSummary;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(ProductJdbcDao.class)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
class ProductJdbcDaoTest {

    @Autowired
    private ProductJdbcDao productJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindExpensiveProducts() {
        // given
        // ZMIANA: Używamy 'categories' zamiast 'category' (na wszelki wypadek)
        jdbcTemplate.execute("CREATE TABLE categories (id BIGINT PRIMARY KEY, name VARCHAR(255), description VARCHAR(255))");

        // ZMIANA KLUCZOWA: Tworzymy tabelę 'products' (liczba mnoga), bo takiej szuka Twój kod DAO
        jdbcTemplate.execute("CREATE TABLE products (id BIGINT PRIMARY KEY, name VARCHAR(255), price DECIMAL(10,2), category_id BIGINT, description VARCHAR(255), image_url VARCHAR(255), stock INT)");

        // Wstawiamy dane do 'categories' i 'products'
        jdbcTemplate.update("INSERT INTO categories (id, name, description) VALUES (1, 'TestCat', 'Opis')");
        jdbcTemplate.update("INSERT INTO products (id, name, price, category_id, description, stock) VALUES (1, 'Cheap', 50.00, 1, 'desc', 10)");
        jdbcTemplate.update("INSERT INTO products (id, name, price, category_id, description, stock) VALUES (2, 'Expensive', 150.00, 1, 'desc', 5)");

        // when
        List<ProductPriceSummary> result = productJdbcDao.findProductsMoreExpensiveThan(new BigDecimal("100.00"));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Expensive");

        // Sprzątanie po teście
        jdbcTemplate.execute("DROP TABLE products");
        jdbcTemplate.execute("DROP TABLE categories");
    }
}