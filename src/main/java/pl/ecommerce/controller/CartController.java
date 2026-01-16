package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String addToCart(@PathVariable Long productId) {
        cartService.addProductToCart(productId);
        return "redirect:/cart"; // Po dodaniu idziemy od razu do koszyka
    }

    // Usuwanie produktu
    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeProduct(productId);
        return "redirect:/cart";
    }
}