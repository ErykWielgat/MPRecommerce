package pl.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        // 1. Mapowanie parametru z URL na nazwę pola w bazie
        String sortField = "name";
        if ("rating".equals(sort)) {
            sortField = "averageRating";
        } else if ("price".equals(sort)) {
            sortField = "price";
        }

        // 2. Ustalanie kierunku
        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        // 3. Tworzenie Pageable z sortowaniem
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 4. Wywołanie nowej metody w serwisie (obsługującej filtry)
        return ResponseEntity.ok(productService.searchProducts(name, categoryId, minPrice, maxPrice, pageable));
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

    // GET /api/v1/products/expensive?minPrice=1000
    @GetMapping("/expensive")
    public ResponseEntity<List<pl.ecommerce.dto.ProductPriceSummary>> getExpensiveProducts(
            @RequestParam java.math.BigDecimal minPrice) {
        // ZMIANA: Wywołujemy przez Serwis (który w środku używa JDBC), a nie bezpośrednio DAO
        return ResponseEntity.ok(productService.getExpensiveProductsReport(minPrice));
    }

    // PUT /api/v1/products/bulk-update?categoryId=1&amount=500
    @PutMapping("/bulk-update")
    public ResponseEntity<String> updatePrices(
            @RequestParam Long categoryId,
            @RequestParam java.math.BigDecimal amount) {
        int updatedRows = productService.bulkUpdatePrices(categoryId, amount);
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