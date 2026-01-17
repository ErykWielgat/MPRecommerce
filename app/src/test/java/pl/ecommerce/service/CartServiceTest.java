package pl.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldAddProductToCart() {
        // given
        Product p = new Product();
        p.setId(1L);
        p.setName("Mleko");
        p.setPrice(new BigDecimal("5.00"));
        p.setImageUrl("img.jpg");

        // Uczymy atrapę repozytorium, co ma zwrócić
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        // when
        cartService.addProductToCart(1L);

        // then
        // Zamiast sprawdzać prywatne pole, używamy publicznego gettera
        List<CartItem> items = cartService.getCartItems();
        assertEquals(1, items.size());
        assertEquals("Mleko", items.get(0).getName());
    }

    @Test
    void shouldCalculateTotalSum() {
        // given
        Product p1 = new Product();
        p1.setId(1L);
        p1.setPrice(new BigDecimal("10.00"));

        Product p2 = new Product();
        p2.setId(2L);
        p2.setPrice(new BigDecimal("20.00"));

        // Konfigurujemy mocki dla obu produktów
        when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(p2));

        // Dodajemy produkty "legalnie" przez metodę serwisu
        // Dzięki temu trafią do wewnętrznej listy w cartService
        cartService.addProductToCart(1L); // Wartość: 10.00
        cartService.addProductToCart(2L); // Wartość: 20.00
        cartService.addProductToCart(2L); // Druga sztuka tego samego -> 2 * 20.00 = 40.00

        // when
        BigDecimal total = cartService.getTotalSum();

        // then
        // 10.00 + 40.00 = 50.00
        assertEquals(new BigDecimal("50.00"), total);
    }
}