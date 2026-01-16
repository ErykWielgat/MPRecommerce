package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import pl.ecommerce.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Widok koszyka
    @GetMapping("/cart")
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalSum", cartService.getTotalSum());
        return "cart";
    }

    // Dodawanie produktu (i powrót na stronę, z której przyszliśmy - to trochę trudniejsze, więc zrobimy proste przekierowanie do koszyka)
    @GetMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        cartService.addProductToCart(productId);
        // Ten komunikat "przeżyje" jedno przekierowanie i wyświetli się na stronie
        redirectAttributes.addFlashAttribute("successMessage", "Dodano produkt do koszyka!");
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    // Usuwanie produktu
    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeProduct(productId);
        return "redirect:/cart";
    }
}