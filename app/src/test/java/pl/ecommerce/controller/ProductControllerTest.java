package pl.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.config.SecurityConfig;
import pl.ecommerce.dao.ProductJdbcDao;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;
import pl.ecommerce.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class) // Ładujemy tylko warstwę WWW dla tego kontrolera
@Import(SecurityConfig.class) // Importujemy naszą konfigurację bezpieczeństwa
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc; // Narzędzie do "strzelania" w endpointy

    @MockBean
    private ProductService productService; // Mockujemy serwis (logikę już przetestowaliśmy wyżej)

    @MockBean
    private ProductJdbcDao productJdbcDao; // Musimy zmockować też to, bo kontroler tego używa

    @MockBean
    private CartService cartService; // SecurityConfig/SessionListener może tego wymagać

    @MockBean
    private CurrencyService currencyService; // SecurityConfig/SessionListener może tego wymagać

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser // Symulujemy zalogowanego użytkownika (lub gościa, jeśli API publiczne)
    void shouldGetAllProducts() throws Exception {
        // given
        ProductDto dto = new ProductDto();
        dto.setName("RestTest");
        dto.setPrice(BigDecimal.valueOf(100));
        when(productService.getAllProducts()).thenReturn(List.of(dto));

        // when & then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("RestTest"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateProduct() throws Exception {
        // given
        ProductDto input = new ProductDto();
        input.setName("New One");
        input.setPrice(BigDecimal.valueOf(50));
        input.setCategoryId(1L);

        when(productService.createProduct(any())).thenReturn(input);

        // when & then
        mockMvc.perform(post("/api/v1/products"));
    }
}
