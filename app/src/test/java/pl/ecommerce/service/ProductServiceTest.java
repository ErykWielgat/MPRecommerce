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
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setPrice(BigDecimal.TEN);

        when(productRepository.findAll()).thenReturn(List.of(p));

        // when
        List<ProductDto> result = productService.getAllProducts();

        // then
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
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