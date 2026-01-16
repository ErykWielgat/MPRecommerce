package pl.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.ecommerce.dto.OrderDto;
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final CurrencyService currencyService; // <--- 1. Wstrzykujemy

    // 1. Wyświetl formularz
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model,
                                   @RequestParam(required = false, defaultValue = "PLN") String currency) {

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/";
        }

        // Przygotuj dane do modelu (waluta, koszty)
        prepareCheckoutModel(model, currency);

        model.addAttribute("orderDto", new OrderDto());
        return "checkout";
    }

    // 2. Przetwórz zamówienie
    @PostMapping("/submit")
    public String submitOrder(@Valid @ModelAttribute OrderDto orderDto,
                              BindingResult bindingResult,
                              @RequestParam(defaultValue = "PLN") String currencyCode, // Pobieramy walutę z ukrytego pola
                              Model model) {

        // Jeśli są błędy, musimy ZNOWU przeliczyć kwoty, żeby formularz się nie rozsypał
        if (bindingResult.hasErrors()) {
            prepareCheckoutModel(model, currencyCode); // Używamy metody pomocniczej
            return "checkout";
        }

        // Zapisz zamówienie (możesz tu też przekazać currencyCode do serwisu, jeśli chcesz zapisać walutę w bazie)
        cartService.saveOrder(orderDto);

        return "redirect:/order/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "order-confirmation";
    }

    // --- METODA POMOCNICZA (żeby nie kopiować kodu) ---
    private void prepareCheckoutModel(Model model, String currency) {
        // 1. Pobierz sumę w PLN
        BigDecimal totalPln = cartService.getTotalSum();

        // 2. Zdefiniuj koszty dostawy w PLN
        BigDecimal deliveryPaczkomatPln = new BigDecimal("10.00");
        BigDecimal deliveryKurierPln = new BigDecimal("20.00");

        // 3. Przelicz na wybraną walutę
        model.addAttribute("cartSum", currencyService.calculatePriceInCurrency(totalPln, currency));
        model.addAttribute("costPaczkomat", currencyService.calculatePriceInCurrency(deliveryPaczkomatPln, currency));
        model.addAttribute("costKurier", currencyService.calculatePriceInCurrency(deliveryKurierPln, currency));

        // 4. Przekaż kod waluty do widoku
        model.addAttribute("currency", currency);
    }
}