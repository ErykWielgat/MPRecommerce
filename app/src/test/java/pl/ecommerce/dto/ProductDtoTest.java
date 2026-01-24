package pl.ecommerce.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ProductDtoTest {

    @Test
    void shouldCorrectlyBuildProductDto() {
        // Symulacja działania Lombokowego Buildera
        Long id = 1L;
        String name = "Testowy Produkt";
        BigDecimal price = new BigDecimal("100.00");

        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPrice(price);

        // Asercje w stylu Hamcrest
        assertThat(dto, hasProperty("id", equalTo(id)));
        assertThat(dto.getName(), startsWith("Testowy"));
        assertThat(dto.getPrice(), greaterThan(BigDecimal.ZERO));

        // Sprawdzenie czy obiekt nie jest nullem
        assertThat(dto, notNullValue());
    }
}