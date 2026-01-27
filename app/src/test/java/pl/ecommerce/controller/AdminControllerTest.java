package pl.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.config.SecurityConfig;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.service.*;
import pl.ecommerce.dao.ProductJdbcDao;
import pl.ecommerce.nbp.NbpClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- ZALEŻNOŚCI BEZPOŚREDNIE KONTROLERA  ---
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private ImageService imageService;

    // --- ZALEŻNOŚCI POBOCZNE  ---
    @MockitoBean private CartService cartService;
    @MockitoBean private CurrencyService currencyService;
    @MockitoBean private ProductJdbcDao productJdbcDao;
    @MockitoBean private NbpClient nbpClient;


    // --- 1. TESTY DOSTĘPU ---

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldShowDashboardToAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldDenyAccessToNonAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    // --- 2. TESTY AKCJI ---

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldSaveProductWithImage() throws Exception {
        // given
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "test.jpg", "image/jpeg", "content".getBytes()
        );

        when(imageService.saveImage(any())).thenReturn("/img/test.jpg");

        // when & then
        mockMvc.perform(multipart("/admin/products/save")
                        .file(imageFile)
                        .param("name", "Nowy Produkt")
                        .param("price", "100.00")
                        .param("stock", "10")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        // Weryfikacja: Sprawdzamy czy wywołano SERWIS
        verify(productService).createProduct(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldDeleteProduct() throws Exception {
        // when
        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        // then
        verify(productService).deleteProduct(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldShowEditForm() throws Exception {
        // given
        Product p = new Product();
        p.setId(1L);
        p.setName("Stary");
        p.setStock(5);
        p.setCategory(new Category());

        // Mockujemy metodę, która pobiera encję (używaną w edycji)
        when(productService.getProductEntity(1L)).thenReturn(p);

        // when & then
        mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"));
    }
}