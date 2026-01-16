package pl.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.service.ProductService;

import java.util.List;

@RestController // 1. Mówi Springowi: To tutaj wpadają zapytania HTTP
@RequestMapping("/api/v1/products") // 2. Adres bazowy dla wszystkich metod w tym pliku
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET http://localhost:8080/api/v1/products
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET http://localhost:8080/api/v1/products/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // POST http://localhost:8080/api/v1/products
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        // @Valid uruchamia sprawdzanie (czy cena > 0, czy nazwa niepusta)
        // @RequestBody bierze JSON-a z żądania i zamienia na obiekt Java
        ProductDto createdProduct = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
}