package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CurrencyService currencyService;

    // Widok koszyka (TERAZ Z WALUTĄ)
    @GetMapping("/cart")
    public String viewCart(Model model,
                           @RequestParam(required = false, defaultValue = "PLN") String currency) {

        // Pobieramy produkty i sumę w PLN (standardowo)
        model.addAttribute("cartItems", cartService.getCartItems());
        BigDecimal totalPln = cartService.getTotalSum();

        // Przeliczamy na wybraną walutę
        BigDecimal finalPrice = currencyService.calculatePriceInCurrency(totalPln, currency);

        // Przekazujemy do HTML
        model.addAttribute("totalSum", totalPln);     // Oryginalna suma PLN (do wyświetlenia w tabelce)
        model.addAttribute("finalPrice", finalPrice); // Suma po przeliczeniu (do zapłaty)
        model.addAttribute("selectedCurrency", currency); // Żeby wiedzieć jaki guzik podświetlić

        return "cart";
    }

    // Dodawanie produktu (bez zmian)
    @GetMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            RedirectAttributes redirectAttributes) {

        cartService.addProductToCart(productId);
        redirectAttributes.addFlashAttribute("successMessage", "Dodano produkt do koszyka!");
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    // Usuwanie produktu (bez zmian)
    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeProduct(productId);
        return "redirect:/cart";
    }

    @org.springframework.web.bind.annotation.PostMapping("/cart/update")
    public String updateCartItem(@RequestParam Long productId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(productId, quantity);
        return "redirect:/cart";
    }
}