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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.ecommerce.dto.OrderDto;
import pl.ecommerce.model.CartItem;
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;
import pl.ecommerce.service.ProductService; // <--- 1. DODANY IMPORT

import java.math.BigDecimal;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final CurrencyService currencyService;
    private final ProductService productService; // <--- 2. WSTRZYKUJEMY SERWIS

    // 1. Wyświetl formularz
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model,
                                   @RequestParam(required = false, defaultValue = "PLN") String currency) {

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/";
        }

        prepareCheckoutModel(model, currency);
        model.addAttribute("orderDto", new OrderDto());
        return "checkout";
    }

    // 2. Przetwórz zamówienie
    @PostMapping("/submit")
    public String submitOrder(@Valid @ModelAttribute OrderDto orderDto,
                              BindingResult bindingResult,
                              @RequestParam(defaultValue = "PLN") String currencyCode,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        // WALIDACJA FORMULARZA
        if (bindingResult.hasErrors()) {
            prepareCheckoutModel(model, currencyCode);
            return "checkout";
        }

        // --- NOWA LOGIKA: Sprawdzamy dostępność towaru ---
        try {
            // Iterujemy po koszyku i zdejmujemy sztuki
            for (CartItem item : cartService.getCartItems()) {
                // Zakładam, że CartItem to 1 sztuka. Jak masz pole quantity, zmień '1' na item.getQuantity()
                productService.decreaseStock(item.getProductId(), 1);
            }
        } catch (RuntimeException e) {
            // JEŚLI BRAK TOWARU:
            // 1. Dodajemy komunikat błędu do widoku
            model.addAttribute("stockError", e.getMessage());
            // 2. Przeliczamy ceny na nowo, żeby widok się nie posypał
            prepareCheckoutModel(model, currencyCode);
            // 3. Wracamy do formularza (nie zapisujemy zamówienia!)
            return "checkout";
        }
        // -----------------------------------------------

        // --- DALSZA CZĘŚĆ TWOJEGO KODU (wykona się tylko jak towar jest dostępny) ---
        BigDecimal totalPln = cartService.getTotalSum();

        BigDecimal deliveryCostPln = BigDecimal.ZERO;
        if ("PACZKOMAT".equals(orderDto.getDeliveryMethod())) {
            deliveryCostPln = new BigDecimal("10.00");
        } else if ("KURIER".equals(orderDto.getDeliveryMethod())) {
            deliveryCostPln = new BigDecimal("20.00");
        }

        BigDecimal totalWithDeliveryPln = totalPln.add(deliveryCostPln);
        BigDecimal finalAmount = currencyService.calculatePriceInCurrency(totalWithDeliveryPln, currencyCode);

        redirectAttributes.addFlashAttribute("paidAmount", finalAmount);
        redirectAttributes.addFlashAttribute("paidCurrency", currencyCode);

        // Zapisujemy zamówienie (to czyści koszyk)
        cartService.saveOrder(orderDto);

        return "redirect:/order/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "order-confirmation";
    }

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