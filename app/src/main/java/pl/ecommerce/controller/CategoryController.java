/*
 * Kontroler REST API odpowiedzialny za zarządzanie kategoriami produktów (tworzenie i pobieranie) w formacie JSON.
 */
package pl.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ecommerce.dto.CategoryDto;
import pl.ecommerce.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Tworzy nową kategorię w systemie na podstawie przesłanych danych JSON.
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(categoryDto));
    }

    // Pobiera pełną listę wszystkich kategorii dostępnych w sklepie.
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}