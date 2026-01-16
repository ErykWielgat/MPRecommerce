package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.ecommerce.service.CategoryService;
import pl.ecommerce.service.CurrencyService;
import pl.ecommerce.service.ProductService;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;
    @GetMapping("/")
    public String home(Model model,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) Long categoryId,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) BigDecimal minPrice,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) BigDecimal maxPrice,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) String name,
                       @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "name") String sort,
                       @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "asc") String dir) {

        // --- Logika produktów (bez zmian) ---
        var products = productService.filterProducts(categoryId, minPrice, maxPrice, name, sort, dir);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("paramCategoryId", categoryId);
        model.addAttribute("paramMinPrice", minPrice);
        model.addAttribute("paramMaxPrice", maxPrice);
        model.addAttribute("paramName", name);
        model.addAttribute("paramSort", sort);
        model.addAttribute("paramDir", dir);

        // --- 2. NOWE: Pobieramy kursy walut ---
        // Używamy metody getRate, którą przed chwilą dodaliśmy do serwisu
        BigDecimal usdRate = currencyService.getRate("USD");
        BigDecimal eurRate = currencyService.getRate("EUR");

        // Przekazujemy do HTML
        model.addAttribute("usdRate", usdRate);
        model.addAttribute("eurRate", eurRate);

        return "index";
    }

    // --- Reszta pliku bez zmian (productDetails, addReview) ---
    @GetMapping("/product/{id}")
    public String productDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        pl.ecommerce.model.Product product = productService.getProductEntity(id);
        model.addAttribute("product", product);

        double averageRating = product.getReviews().stream()
                .mapToInt(pl.ecommerce.model.Review::getRating)
                .average()
                .orElse(0.0);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));

        return "product-details";
    }

    @org.springframework.web.bind.annotation.PostMapping("/product/{id}/review")
    public String addReview(@org.springframework.web.bind.annotation.PathVariable Long id,
                            @org.springframework.web.bind.annotation.RequestParam String author,
                            @org.springframework.web.bind.annotation.RequestParam String content,
                            @org.springframework.web.bind.annotation.RequestParam int rating) {

        productService.addReview(id, author, content, rating);
        return "redirect:/product/" + id;
    }
}