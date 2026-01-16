package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.service.CartService;

@ControllerAdvice // Magia: To działa dla KAŻDEGO kontrolera w aplikacji
@RequiredArgsConstructor
public class GlobalControlerAdvice {

    private final CartService cartService;

    // Ta metoda uruchamia się przy każdym zapytaniu o stronę HTML
    // i dodaje do modelu zmienną "cartCount"
    @ModelAttribute("cartCount")
    public int getCartCount() {
        return cartService.getCartItems().stream()
                .mapToInt(CartItem::getQuantity) // Sumujemy ilość sztuk (nie tylko ilość pozycji)
                .sum();
    }
}