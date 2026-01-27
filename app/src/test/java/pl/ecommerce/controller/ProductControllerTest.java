package pl.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// ZMIANA IMPORTU:
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private ProductService productService;
    @MockitoBean private ProductJdbcDao productJdbcDao;
    @MockitoBean private CartService cartService;
    @MockitoBean private CurrencyService currencyService;

    // --- 1. TEST PAGINACJI ---
    @Test
    @WithMockUser
    void shouldGetAllProductsPaged() throws Exception {
        // given
        ProductDto dto = new ProductDto();
        dto.setName("RestTest");
        dto.setPrice(BigDecimal.valueOf(100));

        when(productService.searchProducts(
                any(), // name
                any(), // categoryId
                any(), // minPrice
                any(), // maxPrice
                any(Pageable.class) // pageable
        )).thenReturn(new PageImpl<>(List.of(dto)));

        // when & then
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("RestTest"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldCreateProduct() throws Exception {
        ProductDto input = new ProductDto();
        input.setName("New One");
        input.setPrice(BigDecimal.valueOf(50));
        input.setCategoryId(1L);

        when(productService.createProduct(any())).thenReturn(input);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated());
    }
}