package pl.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile; // Ważne do testowania plików
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.config.SecurityConfig;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.ProductRepository;
import pl.ecommerce.repository.ReviewRepository;
import pl.ecommerce.service.*;
import pl.ecommerce.dao.ProductJdbcDao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart; // Do testowania uploadu
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- ZALEŻNOŚCI BEZPOŚREDNIE KONTROLERA ---
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private ImageService imageService;
    @MockitoBean private ProductRepository productRepository;
    @MockitoBean private ReviewRepository reviewRepository;

    // --- ZALEŻNOŚCI POBOCZNE (Wymagane przez Security/Layout) ---
    @MockitoBean private CartService cartService;
    @MockitoBean private CurrencyService currencyService;
    @MockitoBean private ProductJdbcDao productJdbcDao;


    // --- 1. TESTY DOSTĘPU I WIDOKÓW (DASHBOARD) ---

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

    @Test
    void shouldRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    // --- 2. TESTY AKCJI (ZAPIS, EDYCJA, USUWANIE) - Kluczowe dla pokrycia! ---

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
                        .param("categoryId", "1")
                        .with(csrf())) // Token CSRF jest wymagany przy POST!
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).createProduct(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productRepository).deleteById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldShowEditForm() throws Exception {
        // given
        Product p = new Product();
        p.setId(1L);
        p.setName("Stary");
        p.setCategory(new Category()); // Unikamy NPE

        when(productService.getProductEntity(1L)).thenReturn(p);

        // when & then
        mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"));
    }
}