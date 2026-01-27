package pl.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.ecommerce.dto.OrderDto;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;
import pl.ecommerce.service.ProductService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@SessionAttributes("orderDto")
public class OrderController {

    private final CartService cartService;
    private final CurrencyService currencyService;
    private final ProductService productService;

    // KROK 1: Wyświetl formularz danych
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model,
                                   @RequestParam(required = false, defaultValue = "PLN") String currency) {

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/";
        }

        // Jeśli w sesji nie ma jeszcze orderDto, tworzymy nowe
        if (!model.containsAttribute("orderDto")) {
            model.addAttribute("orderDto", new OrderDto());
        }

        prepareCheckoutModel(model, currency);
        return "checkout";
    }

    // KROK 2: Walidacja i przejście do Podsumowania
    @PostMapping("/summary")
    public String showSummary(@Valid @ModelAttribute OrderDto orderDto,
                              BindingResult bindingResult,
                              @RequestParam(defaultValue = "PLN") String currency,
                              Model model) {

        // 1. Walidacja danych wpisanych w checkout
        if (bindingResult.hasErrors()) {
            prepareCheckoutModel(model, currency);
            return "checkout";
        }

        // 2. Obliczenia kosztów dla podsumowania
        BigDecimal cartTotal = cartService.getTotalSum();
        BigDecimal deliveryCost = BigDecimal.ZERO;

        if ("PACZKOMAT".equals(orderDto.getDeliveryMethod())) {
            deliveryCost = new BigDecimal("10.00");
        } else if ("KURIER".equals(orderDto.getDeliveryMethod())) {
            deliveryCost = new BigDecimal("20.00");
        }

        // 3. Przeliczanie na walutę (żeby wyświetlić w podsumowaniu)
        BigDecimal totalWithDelivery = cartTotal.add(deliveryCost);

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartSum", currencyService.calculatePriceInCurrency(cartTotal, currency));
        model.addAttribute("deliveryCost", currencyService.calculatePriceInCurrency(deliveryCost, currency));
        model.addAttribute("finalTotal", currencyService.calculatePriceInCurrency(totalWithDelivery, currency));
        model.addAttribute("currency", currency);

        return "summary"; // Idziemy do pliku summary.html
    }

    // KROK 3: Finalne zatwierdzenie i zapis (ZMODYFIKOWANA)
    @PostMapping("/submit")
    public String submitOrder(@ModelAttribute OrderDto orderDto,
                              SessionStatus sessionStatus,
                              @RequestParam(defaultValue = "PLN") String currency,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        // --- Sprawdzamy dostępność towaru (Dopiero teraz, przy ostatecznym kliknięciu) ---
        try {
            for (CartItem item : cartService.getCartItems()) {
                // WAŻNE: Używamy item.getQuantity(), żeby zdjąć tyle ile kupuje, a nie zawsze 1
                productService.decreaseStock(item.getProductId(), item.getQuantity());
            }
        } catch (RuntimeException e) {
            // Jeśli brak towaru -> Wracamy do checkoutu z błędem
            model.addAttribute("stockError", e.getMessage());
            prepareCheckoutModel(model, currency);
            return "checkout";
        }

        // Obliczenie kwoty do "płatności" (dla widoku potwierdzenia)
        BigDecimal totalPln = cartService.getTotalSum();
        BigDecimal deliveryCost = "KURIER".equals(orderDto.getDeliveryMethod()) ? new BigDecimal("20.00") : new BigDecimal("10.00");
        BigDecimal finalAmount = currencyService.calculatePriceInCurrency(totalPln.add(deliveryCost), currency);

        redirectAttributes.addFlashAttribute("paidAmount", finalAmount);
        redirectAttributes.addFlashAttribute("paidCurrency", currency);

        // Zapis do bazy
        cartService.saveOrder(orderDto);

        // Wyczyszczenie tymczasowego formularza z sesji
        sessionStatus.setComplete();

        return "redirect:/order/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "order-confirmation";
    }

    // Metoda pomocnicza
    private void prepareCheckoutModel(Model model, String currency) {
        BigDecimal totalPln = cartService.getTotalSum();
        BigDecimal deliveryPaczkomatPln = new BigDecimal("10.00");
        BigDecimal deliveryKurierPln = new BigDecimal("20.00");

        model.addAttribute("cartSum", currencyService.calculatePriceInCurrency(totalPln, currency));
        model.addAttribute("costPaczkomat", currencyService.calculatePriceInCurrency(deliveryPaczkomatPln, currency));
        model.addAttribute("costKurier", currencyService.calculatePriceInCurrency(deliveryKurierPln, currency));
        model.addAttribute("currency", currency);
    }
}