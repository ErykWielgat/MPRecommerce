package pl.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.exception.ResourceNotFoundException;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.CategoryRepository;
import pl.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Używamy Mockito zamiast całego Springa (szybkie testy)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository; // Atrapa repozytorium

    @Mock
    private CategoryRepository categoryRepository; // Atrapa repo kategorii

    @InjectMocks
    private ProductService productService; // To testujemy

    @Test
    void shouldReturnAllProductsAsDto() {
        // given
        // 1. Tworzymy Kategorię (żeby nie było null)
        Category cat = new Category();
        cat.setId(99L);
        cat.setName("Testowa Kat");

        // 2. Tworzymy Produkt i przypisujemy mu kategorię
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setPrice(BigDecimal.TEN);
        p.setCategory(cat); // <--- TO NAPRAWIA NPE

        when(productRepository.findAll()).thenReturn(List.of(p));

        // when
        List<ProductDto> result = productService.getAllProducts();

        // then
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals(99L, result.get(0).getCategoryId()); // Sprawdzamy czy ID kategorii przeszło
    }
    @Test
    void shouldThrowExceptionWhenCategoryNotFoundDuringCreation() {
        // given
        ProductDto dto = new ProductDto();
        dto.setCategoryId(999L); // Nieistniejąca kategoria

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        // Zakładam, że rzucasz RuntimeException lub inną, gdy kategoria nie istnieje
        assertThrows(RuntimeException.class, () -> productService.createProduct(dto));
    }

    @Test
    void shouldHandleUpdateProduct() {
        // Test edycji produktu (jeśli masz metodę updateProduct)
        // To często pomijana logika, która ma dużo if-ów
    }

    @Test
    void shouldCreateProduct() {
        // given
        ProductDto dto = new ProductDto();
        dto.setName("Nowy");
        dto.setPrice(BigDecimal.ONE);
        dto.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(10L); // Symulujemy, że baza nadała ID
            return saved;
        });

        // when
        ProductDto result = productService.createProduct(dto);

        // then
        assertNotNull(result.getId());
        assertEquals(10L, result.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }
}