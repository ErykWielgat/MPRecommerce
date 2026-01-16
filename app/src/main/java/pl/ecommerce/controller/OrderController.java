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
import pl.ecommerce.dto.OrderDto;
import pl.ecommerce.service.CartService;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;

    // 1. Wyświetl formularz
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model) {
        // Jeśli koszyk pusty, przekieruj do sklepu
        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("orderDto", new OrderDto());
        model.addAttribute("cartSum", cartService.getTotalSum());
        return "checkout";
    }

    // 2. Przetwórz zamówienie
    @PostMapping("/submit")
    public String submitOrder(@Valid @ModelAttribute OrderDto orderDto,
                              BindingResult bindingResult,
                              Model model) {
        // Jeśli są błędy walidacji (np. zły kod pocztowy), wróć do formularza
        if (bindingResult.hasErrors()) {
            model.addAttribute("cartSum", cartService.getTotalSum());
            return "checkout";
        }

        // Zapisz zamówienie
        cartService.saveOrder(orderDto);

        // Przekieruj na stronę podziękowania
        return "redirect:/order/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "order-confirmation";
    }
}
