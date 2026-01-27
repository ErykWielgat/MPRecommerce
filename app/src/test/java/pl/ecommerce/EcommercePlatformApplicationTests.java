package pl.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.CategoryRepository;
import pl.ecommerce.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EcommercePlatformApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void fullUserFlowTest() throws Exception {
        // 1. PRZYGOTOWANIE DANYCH
        Category cat = new Category();
        cat.setName("Elektronika Testowa");
        categoryRepository.save(cat);

        Product p = new Product();
        p.setName("Laptop Testowy");
        p.setPrice(new BigDecimal("2500.00"));
        p.setDescription("Super laptop");
        p.setStock(10);
        p.setCategory(cat);
        productRepository.save(p);


        // 2. STRONA GŁÓWNA
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laptop Testowy")));

        // 3. SZCZEGÓŁY
        mockMvc.perform(get("/product/" + p.getId()))
                .andExpect(status().isOk());

        // 4. DODANIE DO KOSZYKA I POBRANIE SESJI!
        var result = mockMvc.perform(get("/cart/add/" + p.getId())
                        .header("Referer", "/"))
                .andExpect(status().is3xxRedirection())
                .andReturn(); // Zwróć wynik

        // Wyciągamy sesję, w której zapisał się koszyk
        var session = result.getRequest().getSession();

        // 5. WIDOK KOSZYKA (Z UŻYCIEM TEJ SAMEJ SESJI)
        mockMvc.perform(get("/cart")
                        .session((org.springframework.mock.web.MockHttpSession) session)) // Przekazujemy sesję dalej
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laptop Testowy"))) // Teraz produkt tu będzie!
                .andExpect(content().string(containsString("2500.00")));

        // 6. CHECKOUT (Z SESJĄ)
        mockMvc.perform(get("/order/checkout")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk());

        // 7. ZŁOŻENIE ZAMÓWIENIA (Z SESJĄ)
        mockMvc.perform(post("/order/submit")
                        .session((org.springframework.mock.web.MockHttpSession) session) // Ważne!
                        .with(csrf())
                        .param("firstName", "Jan")
                        .param("lastName", "Testowy")
                        .param("email", "jan@test.pl")
                        .param("address", "Ulica 1")
                        .param("zipCode", "00-123")
                        .param("city", "Warszawa")
                        .param("deliveryMethod", "PACZKOMAT")
                        .param("currencyCode", "PLN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/confirmation"));
    }

    @Test
    void adminPanelFlowTest() throws Exception {
        // 1. Próba wejścia bez logowania -> Przekierowanie do logowania
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        // 2. Wejście jako ADMIN
        mockMvc.perform(get("/admin")
                        .with(user("admin").password("admin123").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PANEL ADMINA")));
    }
}