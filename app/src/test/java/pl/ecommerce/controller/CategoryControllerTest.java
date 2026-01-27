package pl.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.config.SecurityConfig;
import pl.ecommerce.dto.CategoryDto;
import pl.ecommerce.service.*;
import pl.ecommerce.dao.ProductJdbcDao;
import pl.ecommerce.nbp.NbpClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CategoryService categoryService;
    @MockitoBean private CartService cartService;
    @MockitoBean private CurrencyService currencyService;
    @MockitoBean private ProductService productService;
    @MockitoBean private ImageService imageService;
    @MockitoBean private ProductJdbcDao productJdbcDao;
    @MockitoBean private NbpClient nbpClient;

    @Test
    @WithMockUser
    void shouldGetAllCategories() throws Exception {
        // given
        CategoryDto cat1 = new CategoryDto();
        cat1.setName("Elektronika");

        CategoryDto cat2 = new CategoryDto();
        cat2.setName("Książki");

        when(categoryService.getAllCategories()).thenReturn(List.of(cat1, cat2));

        // when & then
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Elektronika"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCategory() throws Exception {
        // given
        CategoryDto inputDto = new CategoryDto();
        inputDto.setName("Nowa Kategoria");

        CategoryDto returnedDto = new CategoryDto();
        returnedDto.setId(1L);
        returnedDto.setName("Nowa Kategoria");

        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(returnedDto);

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nowa Kategoria"));
    }
}