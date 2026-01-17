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
import pl.ecommerce.service.CartService;
import pl.ecommerce.service.CurrencyService;


import java.math.BigDecimal;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final CurrencyService currencyService;

    // 1. Wyświetl formularz
    @GetMapping("/checkout")
    public String showCheckoutForm(Model model,
                                   @RequestParam(required = false, defaultValue = "PLN") String currency) {

        // Jeśli koszyk pusty -> wypad do sklepu
        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/";
        }

        // Używamy metody pomocniczej (definicja na dole pliku!)
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

        // WALIDACJA: Jeśli są błędy w formularzu
        if (bindingResult.hasErrors()) {
            // Musimy znowu przeliczyć kwoty, żeby formularz wyświetlił ceny, a nie puste pola
            prepareCheckoutModel(model, currencyCode);
            return "checkout";
        }

        // --- LICZENIE OSTATECZNEJ KWOTY (Zanim wyczyścimy koszyk) ---
        BigDecimal totalPln = cartService.getTotalSum();

        // Koszt dostawy w PLN
        BigDecimal deliveryCostPln = BigDecimal.ZERO;
        if ("PACZKOMAT".equals(orderDto.getDeliveryMethod())) {
            deliveryCostPln = new BigDecimal("10.00");
        } else if ("KURIER".equals(orderDto.getDeliveryMethod())) {
            deliveryCostPln = new BigDecimal("20.00");
        }

        // Suma całkowita w PLN
        BigDecimal totalWithDeliveryPln = totalPln.add(deliveryCostPln);

        // Przelicz na wybraną walutę
        BigDecimal finalAmount = currencyService.calculatePriceInCurrency(totalWithDeliveryPln, currencyCode);

        // Przekazujemy te dane do strony z podziękowaniem (FlashAttributes przetrwają przekierowanie)
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

    // --- TO JEST TA METODA, KTÓREJ BRAKOWAŁO ---
    // Służy do tego, żeby nie pisać tego samego kodu 2 razy (w GET i w POST przy błędzie)
    private void prepareCheckoutModel(Model model, String currency) {
        BigDecimal totalPln = cartService.getTotalSum();
        BigDecimal deliveryPaczkomatPln = new BigDecimal("10.00");
        BigDecimal deliveryKurierPln = new BigDecimal("20.00");

        // Przeliczamy wszystko na wybraną walutę i wrzucamy do modelu
        model.addAttribute("cartSum", currencyService.calculatePriceInCurrency(totalPln, currency));
        model.addAttribute("costPaczkomat", currencyService.calculatePriceInCurrency(deliveryPaczkomatPln, currency));
        model.addAttribute("costKurier", currencyService.calculatePriceInCurrency(deliveryKurierPln, currency));
        model.addAttribute("currency", currency);
    }
}