package pl.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products") // 2. Adres bazowy dla wszystkich metod w tym pliku
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final pl.ecommerce.dao.ProductJdbcDao productJdbcDao;

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        // Uwaga: Musisz dodać metodę getAllProducts(Pageable) w ProductService!
        return ResponseEntity.ok(productService.getAllProductsPaged(pageable));
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
    // Endpoint do testowania JdbcTemplate (SELECT)
    // GET /api/v1/products/expensive?minPrice=1000
    @GetMapping("/expensive")
    public ResponseEntity<List<pl.ecommerce.dto.ProductPriceSummary>> getExpensiveProducts(
            @RequestParam java.math.BigDecimal minPrice) {
        return ResponseEntity.ok(productJdbcDao.findProductsMoreExpensiveThan(minPrice));
    }

    // Endpoint do testowania JdbcTemplate (UPDATE)
    // PUT /api/v1/products/bulk-update?categoryId=1&amount=500
    @PutMapping("/bulk-update")
    public ResponseEntity<String> updatePrices(
            @RequestParam Long categoryId,
            @RequestParam java.math.BigDecimal amount) {
        int updatedRows = productJdbcDao.updatePriceByCategory(categoryId, amount);
        return ResponseEntity.ok("Zaktualizowano cen produktów: " + updatedRows);
    }
    // DELETE http://localhost:8080/api/v1/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        // Zwracamy 204 No Content (sukces, brak treści)
        return ResponseEntity.noContent().build();
    }
}