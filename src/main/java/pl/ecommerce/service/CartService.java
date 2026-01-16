package pl.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope // To sprawia, że każdy user ma swój własny koszyk!
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;

    // Lista produktów w koszyku (trzymana w pamięci RAM)
    private final List<CartItem> cartItems = new ArrayList<>();

    public void addProductToCart(Long productId) {
        // Sprawdź, czy produkt już jest w koszyku
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Jeśli jest, zwiększ ilość
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
        } else {
            // Jeśli nie ma, pobierz info z bazy i dodaj
            Product product = productRepository.findById(productId).orElseThrow();
            cartItems.add(new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    1,
                    product.getImageUrl()
            ));
        }
    }

    public void removeProduct(Long productId) {
        cartItems.removeIf(item -> item.getProductId().equals(productId));
    }

    // Metoda do czyszczenia koszyka (np. po zakupie)
    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public BigDecimal getTotalSum() {
        return cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}