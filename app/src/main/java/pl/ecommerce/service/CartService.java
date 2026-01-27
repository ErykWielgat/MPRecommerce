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
@SessionScope
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
    public void updateQuantity(Long productId, int newQuantity) {
        Optional<CartItem> itemOpt = cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            // Jeśli użytkownik wpisze 0 lub mniej -> usuń produkt
            if (newQuantity <= 0) {
                cartItems.remove(item);
            } else {
                // W przeciwnym razie zaktualizuj ilość
                item.setQuantity(newQuantity);
            }
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
    private final pl.ecommerce.repository.OrderRepository orderRepository;

    // Metoda zapisująca zamówienie
    public void saveOrder(pl.ecommerce.dto.OrderDto orderDto) {
        pl.ecommerce.model.Order order = new pl.ecommerce.model.Order();

        // Przepisanie danych adresowych
        order.setFirstName(orderDto.getFirstName());
        order.setLastName(orderDto.getLastName());
        order.setEmail(orderDto.getEmail());
        order.setAddress(orderDto.getAddress());
        order.setZipCode(orderDto.getZipCode());
        order.setCity(orderDto.getCity());
        order.setDeliveryMethod(orderDto.getDeliveryMethod());

        // Logika kosztów dostawy
        BigDecimal deliveryCost = BigDecimal.ZERO;
        if ("KURIER".equals(orderDto.getDeliveryMethod())) {
            deliveryCost = new BigDecimal("20.00");
        } else if ("PACZKOMAT".equals(orderDto.getDeliveryMethod())) {
            deliveryCost = new BigDecimal("10.00");
        }
        order.setDeliveryCost(deliveryCost);

        // Suma całkowita (produkty + dostawa)
        BigDecimal productsTotal = getTotalSum();
        order.setTotalAmount(productsTotal.add(deliveryCost));

        // Przepisanie produktów z koszyka do encji OrderItem
        List<pl.ecommerce.model.OrderItem> items = new ArrayList<>();
        for (pl.ecommerce.model.CartItem cartItem : cartItems) {
            pl.ecommerce.model.OrderItem orderItem = new pl.ecommerce.model.OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getName());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            items.add(orderItem);
        }
        order.setOrderItems(items);

        // Zapis do bazy
        orderRepository.save(order);

        // WYCZYSZCZENIE KOSZYKA PO ZAKUPIE
        clearCart();

    }
}