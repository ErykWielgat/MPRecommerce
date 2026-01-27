/*
 * Globalny komponent konfiguracji (ControllerAdvice), który udostępnia dane (np. licznik koszyka) we wszystkich widokach aplikacji automatycznie.
 */
package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.service.CartService;

@ControllerAdvice // Oznacza, że logika tej klasy jest aplikowana globalnie do wszystkich kontrolerów (np. ładowanie wspólnych danych do menu).
@RequiredArgsConstructor
public class GlobalControlerAdvice {

    private final CartService cartService;

    // Metoda uruchamiana przed każdym żądaniem; oblicza ilość produktów w koszyku i dodaje ją do modelu każdej strony jako "cartCount".
    @ModelAttribute("cartCount")
    public int getCartCount() {
        return cartService.getCartItems().stream()
                .mapToInt(CartItem::getQuantity) // Sumuje liczbę sztuk wszystkich pozycji w koszyku.
                .sum();
    }
}