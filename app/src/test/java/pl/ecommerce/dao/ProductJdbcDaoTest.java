package pl.ecommerce.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.ecommerce.dto.ProductPriceSummary;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest // Podnosi tylko bazę danych H2 i JdbcTemplate
@Import(ProductJdbcDao.class) // Importujemy nasze DAO
class ProductJdbcDaoTest {

    @Autowired
    private ProductJdbcDao productJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindExpensiveProducts() {
        // given
        jdbcTemplate.execute("CREATE TABLE categories (id BIGINT PRIMARY KEY, name VARCHAR(255), description VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE products (id BIGINT PRIMARY KEY, name VARCHAR(255), price DECIMAL(10,2), category_id BIGINT, description VARCHAR(255), image_url VARCHAR(255))");

        // 2. Wstawiamy dane
        jdbcTemplate.update("INSERT INTO categories (id, name) VALUES (1, 'TestCat')");
        jdbcTemplate.update("INSERT INTO products (id, name, price, category_id, description) VALUES (1, 'Cheap', 50.00, 1, 'desc')");
        jdbcTemplate.update("INSERT INTO products (id, name, price, category_id, description) VALUES (2, 'Expensive', 150.00, 1, 'desc')");

        // when
        List<ProductPriceSummary> result = productJdbcDao.findProductsMoreExpensiveThan(new BigDecimal("100.00"));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Expensive");
    }
}