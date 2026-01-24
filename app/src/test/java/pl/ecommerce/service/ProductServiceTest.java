package pl.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.exception.ResourceNotFoundException;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.model.Review;
import pl.ecommerce.repository.CategoryRepository;
import pl.ecommerce.repository.ProductRepository;
import pl.ecommerce.repository.ReviewRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ReviewRepository reviewRepository; // Dodane, bo serwis tego używa

    @InjectMocks
    private ProductService productService;

    // --- 1. TESTY POBIERANIA (GET) ---

    @Test
    void shouldReturnAllProductsAsDto() {
        // given
        Category cat = new Category();
        cat.setId(99L);
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setCategory(cat);

        when(productRepository.findAll()).thenReturn(List.of(p));

        // when
        List<ProductDto> result = productService.getAllProducts();

        // then
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals(99L, result.get(0).getCategoryId());
    }

    @Test
    void shouldGetProductById() {
        // given
        Category cat = new Category();
        cat.setId(1L);
        Product p = new Product();
        p.setId(10L);
        p.setCategory(cat);

        when(productRepository.findById(10L)).thenReturn(Optional.of(p));

        // when
        ProductDto result = productService.getProductById(10L);

        // then
        assertEquals(10L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    // --- 2. TESTY TWORZENIA I EDYCJI (CREATE / UPDATE) - Kluczowe dla Branches! ---

    @Test
    void shouldCreateProductWithNewCategory() {
        // given
        ProductDto dto = new ProductDto();
        dto.setName("Nowy z nową kategorią");
        dto.setPrice(BigDecimal.TEN);
        dto.setNewCategoryName("Super Nowa"); // <--- Testujemy nową funkcję

        when(categoryRepository.findByName("Super Nowa")).thenReturn(Optional.empty()); // Nie ma takiej w bazie
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(55L);
            return c;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ProductDto result = productService.createProduct(dto);

        // then
        verify(categoryRepository).save(any(Category.class)); // Sprawdzamy czy zapisał kategorię
        assertEquals("Nowy z nową kategorią", result.getName());
    }

    @Test
    void shouldUpdateExistingProduct() {
        // given - ID NIE JEST NULL (edycja)
        ProductDto dto = new ProductDto();
        dto.setId(5L); // Istniejące ID
        dto.setName("Zaktualizowana nazwa");
        dto.setPrice(BigDecimal.valueOf(200));
        dto.setCategoryId(2L);
        dto.setImageUrl("nowe_foto.jpg");

        // Stary produkt w bazie
        Product existingProduct = new Product();
        existingProduct.setId(5L);
        existingProduct.setName("Stara nazwa");
        existingProduct.setImageUrl("stare_foto.jpg");

        Category newCategory = new Category();
        newCategory.setId(2L);

        when(productRepository.findById(5L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ProductDto result = productService.createProduct(dto);

        // then
        assertEquals("Zaktualizowana nazwa", result.getName());
        assertEquals("nowe_foto.jpg", result.getImageUrl());
        verify(productRepository).save(existingProduct); // Upewniamy się, że nadpisaliśmy ten sam obiekt
    }

    @Test
    void shouldNotUpdateImageIfUrlIsEmpty() {
        // Pokrycie brancha: if (productDto.getImageUrl() != null && !isEmpty())

        // given
        ProductDto dto = new ProductDto();
        dto.setId(5L);
        dto.setCategoryId(1L);
        dto.setImageUrl(""); // Pusty ciąg znaków!

        Product existingProduct = new Product();
        existingProduct.setId(5L);
        existingProduct.setImageUrl("stare_wazne_foto.jpg"); // To powinno zostać

        when(productRepository.findById(5L)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ProductDto result = productService.createProduct(dto);

        // then
        assertEquals("stare_wazne_foto.jpg", result.getImageUrl()); // Nie powinno się zmienić na puste!
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        // given
        ProductDto dto = new ProductDto();
        dto.setId(999L); // Nie ma takiego

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(dto));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        ProductDto dto = new ProductDto();
        dto.setCategoryId(999L);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.createProduct(dto));
    }

    // --- 3. TESTY OPINII (REVIEWS) ---

    @Test
    void shouldAddReview() {
        // given
        Product p = new Product();
        p.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        // when
        productService.addReview(1L, "Jan", "Super", 5);

        // then
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingReviewToMissingProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> productService.addReview(1L, "Jan", "Opis", 5));
    }

    // --- 4. TESTY FILTROWANIA I SORTOWANIA (Branches: ternary operators) ---

    @Test
    void shouldFilterProductsWithDefaultSort() {
        // given
        Category cat = new Category();
        cat.setId(1L);
        Product p = new Product();
        p.setCategory(cat);

        // Mockujemy wynik wyszukiwania
        when(productRepository.searchProducts(any(), any(), any(), any(), any(Sort.class)))
                .thenReturn(List.of(p));

        // when
        // Sort null, SortDir null -> powinno wejść w domyślne "name" i ASC
        List<ProductDto> result = productService.filterProducts(1L, null, null, null, null, null);

        // then
        assertEquals(1, result.size());
        // Sprawdzamy czy Sort został utworzony poprawnie (ASC, name)
        verify(productRepository).searchProducts(eq(1L), any(), any(), any(),
                eq(Sort.by(Sort.Direction.ASC, "name")));
    }

    @Test
    void shouldFilterProductsWithCustomSort() {
        // given
        when(productRepository.searchProducts(any(), any(), any(), any(), any(Sort.class)))
                .thenReturn(List.of());

        // when
        // Sort "price", SortDir "desc"
        productService.filterProducts(null, null, null, null, "price", "desc");

        // then
        // Sprawdzamy czy Sort to DESC, price
        verify(productRepository).searchProducts(any(), any(), any(), any(),
                eq(Sort.by(Sort.Direction.DESC, "price")));
    }

    // --- 5. TEST ENCJI ---

    @Test
    void shouldGetProductEntity() {
        Product p = new Product();
        p.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Product result = productService.getProductEntity(1L);
        assertEquals(1L, result.getId());
    }
}