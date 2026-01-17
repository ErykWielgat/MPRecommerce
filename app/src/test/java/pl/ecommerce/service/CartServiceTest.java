package pl.ecommerce.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private CartService cartService;

    private List<CartItem> cartItems;

    @BeforeEach
    void setUp() {
        // Symulujemy zachowanie sesji - trzymamy listę w polu klasy testowej
        cartItems = new ArrayList<>();
        // Kiedy serwis zapyta o "cart", zwróć naszą listę
        lenient().when(session.getAttribute("cart")).thenReturn(cartItems);
    }

    @Test
    void shouldAddProductToCart() {
        // given
        Product p = new Product();
        p.setId(1L);
        p.setName("Mleko");
        p.setPrice(new BigDecimal("5.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        // when
        cartService.addProductToCart(1L);

        // then
        assertEquals(1, cartItems.size());
        assertEquals("Mleko", cartItems.get(0).getName());
        assertEquals(new BigDecimal("5.00"), cartItems.get(0).getPrice());
    }

    @Test
    void shouldCalculateTotalSum() {
        // given
        cartItems.add(new CartItem(1L, "A", new BigDecimal("10.00"), null, 1));
        cartItems.add(new CartItem(2L, "B", new BigDecimal("20.00"), null, 2)); // 2 * 20 = 40

        // when
        BigDecimal total = cartService.getTotalSum();

        // then
        // 10 + 40 = 50
        assertEquals(new BigDecimal("50.00"), total);
    }
}