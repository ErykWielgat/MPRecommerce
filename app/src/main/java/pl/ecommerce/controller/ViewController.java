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


        String mappedSort = "name";

        if ("rating".equals(sort)) {
            mappedSort = "averageRating"; // Tłumaczymy "rating" na "averageRating"
        } else if ("price".equals(sort)) {
            mappedSort = "price";
        } else {
            mappedSort = "name";
        }

        // --- Logika produktów (Przekazujemy mappedSort zamiast surowego sort) ---
        var products = productService.filterProducts(categoryId, minPrice, maxPrice, name, mappedSort, dir);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());

        // Przekazujemy parametry z powrotem do widoku (żeby formularz pamiętał wybór)
        model.addAttribute("paramCategoryId", categoryId);
        model.addAttribute("paramMinPrice", minPrice);
        model.addAttribute("paramMaxPrice", maxPrice);
        model.addAttribute("paramName", name);
        model.addAttribute("paramSort", sort); // Tu zostawiamy oryginał dla HTML selecta
        model.addAttribute("paramDir", dir);

        // --- 2. Pobieramy 4 waluty ---
        BigDecimal usdRate = currencyService.getRate("USD");
        BigDecimal eurRate = currencyService.getRate("EUR");
        BigDecimal gbpRate = currencyService.getRate("GBP");
        BigDecimal chfRate = currencyService.getRate("CHF");

        model.addAttribute("usdRate", usdRate);
        model.addAttribute("eurRate", eurRate);
        model.addAttribute("gbpRate", gbpRate);
        model.addAttribute("chfRate", chfRate);

        return "index";
    }

    // Wyświetla stronę pojedynczego produktu, obliczając w locie średnią ocenę na podstawie dodanych recenzji.
    @GetMapping("/product/{id}")
    public String productDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        pl.ecommerce.model.Product product = productService.getProductEntity(id);
        model.addAttribute("product", product);

        // Wylicza średnią arytmetyczną z ocen (Stream API), a jeśli brak opinii, zwraca 0.0.
        double averageRating = product.getReviews().stream()
                .mapToInt(pl.ecommerce.model.Review::getRating)
                .average()
                .orElse(0.0);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));

        return "product-details";
    }

    // Odbiera dane z formularza nowej opinii i zleca serwisowi zapisanie jej w bazie danych.
    @org.springframework.web.bind.annotation.PostMapping("/product/{id}/review")
    public String addReview(@org.springframework.web.bind.annotation.PathVariable Long id,
                            @org.springframework.web.bind.annotation.RequestParam String author,
                            @org.springframework.web.bind.annotation.RequestParam String content,
                            @org.springframework.web.bind.annotation.RequestParam int rating) {

        productService.addReview(id, author, content, rating);
        // Po zapisaniu opinii następuje przekierowanie (PRG), aby odświeżyć stronę produktu i pokazać nowy komentarz.
        return "redirect:/product/" + id;
    }
}