package pl.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.config.SecurityConfig;
import pl.ecommerce.repository.ProductRepository; // <--- Import
import pl.ecommerce.repository.ReviewRepository;   // <--- Import
import pl.ecommerce.service.*;
import pl.ecommerce.dao.ProductJdbcDao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- 1. ZALEŻNOŚCI BEZPOŚREDNIE KONTROLERA (Muszą tu być!) ---
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private ImageService imageService;

    // Tych dwóch brakowało i dlatego był błąd:
    @MockitoBean private ProductRepository productRepository;
    @MockitoBean private ReviewRepository reviewRepository;

    // --- 2. ZALEŻNOŚCI POBOCZNE (Wymagane przez Security lub Global Layout) ---
    // SecurityConfig lub SessionListener mogą wymagać CartService
    @MockitoBean private CartService cartService;

    // Layout (header) często wymaga CurrencyService do wyświetlania kursów
    @MockitoBean private CurrencyService currencyService;

    // Czasem wymagane, jeśli Security korzysta z JDBC
    @MockitoBean private ProductJdbcDao productJdbcDao;


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
}