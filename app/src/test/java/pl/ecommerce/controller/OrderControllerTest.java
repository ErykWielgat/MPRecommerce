package pl.ecommerce.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.ecommerce.config.SecurityConfig;
import pl.ecommerce.dto.OrderDto;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;
import pl.ecommerce.service.ProductService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private ProductService productService;

    private List<CartItem> sampleCartItems;

    @BeforeEach
    void setUp() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setName("Test Product");
        item.setPrice(new BigDecimal("100.00")); // Precyzja walutowa
        item.setQuantity(2);
        sampleCartItems = List.of(item);

        when(currencyService.calculatePriceInCurrency(any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- KROK 1: GET /checkout ---

    @Test
    @WithMockUser
    void shouldRedirectToHomeIfCartIsEmpty() throws Exception {
        when(cartService.getCartItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/order/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser
    void shouldShowCheckoutFormIfCartIsNotEmpty() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));

        mockMvc.perform(get("/order/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attributeExists("orderDto"))
                .andExpect(model().attributeExists("cartSum"));
    }

    // --- KROK 2: POST /summary ---

    @Test
    @WithMockUser
    void shouldReturnToCheckoutOnValidationError() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));

        mockMvc.perform(post("/order/summary")
                        .with(csrf())
                        .sessionAttr("orderDto", new OrderDto())
                        .param("firstName", "")
                        .param("deliveryMethod", "PACZKOMAT"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser
    void shouldShowSummaryWithPaczkomatCosts() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));

        // POPRAWKA: Używamy new BigDecimal("10.00") zamiast valueOf(10.00)
        // Dzięki temu 'scale' wynosi 2, co zgadza się z kontrolerem
        when(currencyService.calculatePriceInCurrency(eq(new BigDecimal("10.00")), any())).thenReturn(new BigDecimal("10.00"));
        when(currencyService.calculatePriceInCurrency(eq(new BigDecimal("210.00")), any())).thenReturn(new BigDecimal("210.00"));

        mockMvc.perform(post("/order/summary")
                        .with(csrf())
                        .sessionAttr("orderDto", new OrderDto())
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@test.pl")
                        .param("address", "Ulica 1")
                        .param("zipCode", "00-000")
                        .param("city", "Warszawa")
                        .param("deliveryMethod", "PACZKOMAT")
                        .param("currency", "PLN"))
                .andExpect(status().isOk())
                .andExpect(view().name("summary"))
                // POPRAWKA ASERCJI: Oczekujemy dokładnie 10.00 (scale 2)
                .andExpect(model().attribute("deliveryCost", new BigDecimal("10.00")))
                .andExpect(model().attribute("finalTotal", new BigDecimal("210.00")));
    }

    @Test
    @WithMockUser
    void shouldShowSummaryWithKurierCosts() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));

        // POPRAWKA: Używamy stringa "20.00"
        when(currencyService.calculatePriceInCurrency(eq(new BigDecimal("20.00")), any())).thenReturn(new BigDecimal("20.00"));
        when(currencyService.calculatePriceInCurrency(eq(new BigDecimal("220.00")), any())).thenReturn(new BigDecimal("220.00"));

        mockMvc.perform(post("/order/summary")
                        .with(csrf())
                        .sessionAttr("orderDto", new OrderDto())
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski")
                        .param("email", "jan@test.pl")
                        .param("address", "Ulica 1")
                        .param("zipCode", "00-000")
                        .param("city", "Warszawa")
                        .param("deliveryMethod", "KURIER")
                        .param("currency", "PLN"))
                .andExpect(status().isOk())
                .andExpect(view().name("summary"))
                // POPRAWKA ASERCJI: Oczekujemy dokładnie 20.00
                .andExpect(model().attribute("deliveryCost", new BigDecimal("20.00")))
                .andExpect(model().attribute("finalTotal", new BigDecimal("220.00")));
    }

    // --- KROK 3: POST /submit ---

    @Test
    @WithMockUser
    void shouldHandleOutOfStockException() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));

        doThrow(new RuntimeException("Brak towaru")).when(productService).decreaseStock(anyLong(), anyInt());

        mockMvc.perform(post("/order/submit")
                        .with(csrf())
                        .sessionAttr("orderDto", new OrderDto())
                        .param("deliveryMethod", "PACZKOMAT"))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"))
                .andExpect(model().attributeExists("stockError"))
                .andExpect(model().attribute("stockError", "Brak towaru"));
    }

    @Test
    @WithMockUser
    void shouldSubmitOrderSuccessfullyWithKurier() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));
        when(currencyService.calculatePriceInCurrency(any(), anyString())).thenReturn(new BigDecimal("220.00"));

        OrderDto orderDto = new OrderDto();
        orderDto.setDeliveryMethod("KURIER");

        mockMvc.perform(post("/order/submit")
                        .with(csrf())
                        .sessionAttr("orderDto", orderDto)
                        .param("currency", "PLN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/confirmation"))
                .andExpect(flash().attribute("paidAmount", new BigDecimal("220.00")));

        verify(productService).decreaseStock(eq(1L), eq(2));
        verify(cartService).saveOrder(any(OrderDto.class));
    }

    @Test
    @WithMockUser
    void shouldSubmitOrderSuccessfullyWithPaczkomat() throws Exception {
        when(cartService.getCartItems()).thenReturn(sampleCartItems);
        when(cartService.getTotalSum()).thenReturn(new BigDecimal("200.00"));
        when(currencyService.calculatePriceInCurrency(any(), anyString())).thenReturn(new BigDecimal("210.00"));

        OrderDto orderDto = new OrderDto();
        orderDto.setDeliveryMethod("PACZKOMAT");

        mockMvc.perform(post("/order/submit")
                        .with(csrf())
                        .sessionAttr("orderDto", orderDto)
                        .param("currency", "PLN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order/confirmation"))
                .andExpect(flash().attribute("paidAmount", new BigDecimal("210.00")));

        verify(cartService).saveOrder(any(OrderDto.class));
    }

    @Test
    @WithMockUser
    void shouldShowConfirmationPage() throws Exception {
        mockMvc.perform(get("/order/confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-confirmation"));
    }
}