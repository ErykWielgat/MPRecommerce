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
}