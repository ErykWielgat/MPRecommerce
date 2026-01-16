package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.ecommerce.service.ProductService;

@Controller // UWAGA: Tu jest zwykły Controller, a nie RestController!
@RequiredArgsConstructor
public class ViewController {

    private final ProductService productService;

    // To obsłuży wejście na stronę główną: http://localhost:8080/
    @GetMapping("/")
    public String home(Model model) {
        // 1. Pobieramy listę produktów z serwisu (tak jak w REST API)
        var products = productService.getAllProducts();

        // 2. Pakujemy produkty do "worka" o nazwie "model".
        // Dzięki temu HTML będzie je widział pod nazwą "products".
        model.addAttribute("products", products);

        // 3. Zwracamy nazwę pliku HTML (bez .html), który ma się wyświetlić
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