package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.ecommerce.service.CategoryService;
import pl.ecommerce.service.ProductService;

@Controller // UWAGA: Tu jest zwykły Controller, a nie RestController!
@RequiredArgsConstructor
public class ViewController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // To obsłuży wejście na stronę główną: http://localhost:8080/
    @GetMapping("/")
    public String home(Model model,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) Long categoryId,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) java.math.BigDecimal minPrice,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) java.math.BigDecimal maxPrice,
                       @org.springframework.web.bind.annotation.RequestParam(required = false) String name,
                       @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "name") String sort,
                       @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "asc") String dir) {

        // 1. Pobieramy przefiltrowane produkty
        var products = productService.filterProducts(categoryId, minPrice, maxPrice, name, sort, dir);
        model.addAttribute("products", products);

        // 2. Pobieramy listę kategorii do paska bocznego
        model.addAttribute("categories", categoryService.getAllCategories());

        // 3. Przekazujemy obecne filtry z powrotem do widoku (żeby formularz pamiętał co wpisaliśmy)
        model.addAttribute("paramCategoryId", categoryId);
        model.addAttribute("paramMinPrice", minPrice);
        model.addAttribute("paramMaxPrice", maxPrice);
        model.addAttribute("paramName", name);
        model.addAttribute("paramSort", sort);
        model.addAttribute("paramDir", dir);

        return "index";
    }
    // 1. Wyświetlanie strony szczegółów
    @GetMapping("/product/{id}")
    public String productDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        pl.ecommerce.model.Product product = productService.getProductEntity(id);
        model.addAttribute("product", product);

        // Obliczanie średniej oceny
        double averageRating = product.getReviews().stream()
                .mapToInt(pl.ecommerce.model.Review::getRating)
                .average()
                .orElse(0.0);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));

        return "product-details";
    }

    // 2. Obsługa formularza dodawania opinii
    @org.springframework.web.bind.annotation.PostMapping("/product/{id}/review")
    public String addReview(@org.springframework.web.bind.annotation.PathVariable Long id,
                            @org.springframework.web.bind.annotation.RequestParam String author,
                            @org.springframework.web.bind.annotation.RequestParam String content,
                            @org.springframework.web.bind.annotation.RequestParam int rating) {

        productService.addReview(id, author, content, rating);

        // Po dodaniu przekieruj z powrotem na stronę produktu
        return "redirect:/product/" + id;
    }
}