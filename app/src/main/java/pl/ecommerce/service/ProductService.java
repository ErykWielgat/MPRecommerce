package pl.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.exception.ResourceNotFoundException;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.model.Review;
import pl.ecommerce.repository.CategoryRepository;
import pl.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final pl.ecommerce.repository.ReviewRepository reviewRepository;

    private final pl.ecommerce.dao.ProductJdbcDao productJdbcDao;

    // Pobieranie wszystkich produktów
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll() // 1. Pobierz encje z bazy
                .stream()
                .map(this::mapToDto) // 2. Przerób każdą encję na DTO
                .collect(Collectors.toList());
    }

    // Pobieranie pojedynczego produktu
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono produktu o id: " + id));
        return mapToDto(product);
    }

    public Page<ProductDto> getAllProductsPaged(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    // Tworzenie produktu
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product;

        // 1. Tworzenie lub edycja produktu
        if (productDto.getId() != null) {
            product = productRepository.findById(productDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produkt nie istnieje"));
        } else {
            product = new Product();
        }

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock() != null ? productDto.getStock() : 0);

        if (productDto.getImageUrl() != null && !productDto.getImageUrl().isEmpty()) {
            product.setImageUrl(productDto.getImageUrl());
        }

        // ---  Obsługa Kategorii ---
        Category category;

        // A. Czy użytkownik wpisał nazwę nowej kategorii?
        if (productDto.getNewCategoryName() != null && !productDto.getNewCategoryName().trim().isEmpty()) {
            String newName = productDto.getNewCategoryName().trim();

            // Sprawdź czy taka już istnieje, żeby nie dublować
            category = categoryRepository.findByName(newName)
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(newName);
                        return categoryRepository.save(newCat);
                    });

        } else {
            // B. Nie wpisał nowej, więc bierzemy z listy (ID)
            if (productDto.getCategoryId() == null) {
                throw new RuntimeException("Musisz wybrać kategorię z listy lub wpisać nową nazwę!");
            }
            category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wybrana kategoria nie istnieje"));
        }

        product.setCategory(category);


        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());
        dto.setStock(product.getStock());
        return dto;
    }

    //Rollback
    @Transactional
    public void decreaseStock(Long productId, int quantityToDecrease) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie znaleziony"));

        if (product.getStock() < quantityToDecrease) {
            throw new RuntimeException("Niewystarczająca ilość produktu " + product.getName() + " w magazynie!");
        }

        product.setStock(product.getStock() - quantityToDecrease);
        productRepository.save(product);
    }

    // Metoda do dodawania opinii
    @Transactional
    public void addReview(Long productId, String author, String content, int rating) {
        // 1. Pobierz produkt
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie istnieje"));

        // 2. Stwórz i zapisz nową opinię
        Review review = new Review();
        review.setAuthorName(author);
        review.setContent(content);
        review.setRating(rating);
        review.setProduct(product);
        reviewRepository.save(review);
       // 3. Odśwież listę opinii produktu
        product.getReviews().add(review);

        // 4. Policz nową średnią
        double newAverage = product.getReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 5. Zapisz nową średnią w Produkcie
        product.setAverageRating(newAverage);
        productRepository.save(product);
    }

    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono produktu"));
    }

    public List<ProductDto> filterProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String name, String sortField, String sortDir) {
        // 1. Ustalanie kierunku sortowania
        org.springframework.data.domain.Sort.Direction direction =
                "desc".equalsIgnoreCase(sortDir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;

        // 2. Ustalanie po czym sortujemy
        String actualSortField = (sortField != null && !sortField.isEmpty()) ? sortField : "name";

        // 3. Wywołanie "inteligentnego zapytania" z repozytorium
        List<Product> products = productRepository.searchProducts(
                categoryId, minPrice, maxPrice, name,
                org.springframework.data.domain.Sort.by(direction, actualSortField)
        );

        // 4. Zamiana na DTO
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nie można usunąć. Produkt o id " + id + " nie istnieje.");
        }
        productRepository.deleteById(id);
    }

    //  Pobieranie wszystkich opinii
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    //  Usuwanie opinii
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);

    }

    // 1. Metoda searchProducts zwracająca Page (
    public Page<ProductDto> searchProducts(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
       return productRepository.searchProducts(categoryId, minPrice, maxPrice, name, pageable)
                .map(this::mapToDto);
    }

    // 2. Metoda raportowa (delegacja do JDBC)
    public List<pl.ecommerce.dto.ProductPriceSummary> getExpensiveProductsReport(BigDecimal minPrice) {
        return productJdbcDao.findProductsMoreExpensiveThan(minPrice);
    }

    // 3. Metoda aktualizacji cen (delegacja do JDBC)
    @Transactional
    public int bulkUpdatePrices(Long categoryId, BigDecimal amount) {
        return productJdbcDao.updatePriceByCategory(categoryId, amount);
    }
}