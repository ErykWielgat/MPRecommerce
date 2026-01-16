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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service // Mówi Springowi: To jest serwis, trzymaj go w pamięci
@RequiredArgsConstructor // To jest kluczowe! Lombok generuje konstruktor dla pól 'final'. To zastępuje @Autowired.
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final pl.ecommerce.repository.ReviewRepository reviewRepository;

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

    // Tworzenie produktu (Modyfikacja danych wymaga @Transactional)
    // Metoda obsługująca zarówno TWORZENIE (id null) jak i EDYCJĘ (id istnieje)
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product;

        // Sprawdzamy, czy to edycja (czy DTO ma ID)
        if (productDto.getId() != null) {
            // EDYCJA: Pobieramy istniejący produkt
            product = productRepository.findById(productDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produkt nie istnieje"));
        } else {
            // NOWY: Tworzymy nowy obiekt
            product = new Product();
        }

        // Aktualizujemy pola
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());

        // Aktualizujemy zdjęcie tylko jeśli przesłano nowy link/plik (żeby nie nadpisać puste)
        if (productDto.getImageUrl() != null && !productDto.getImageUrl().isEmpty()) {
            product.setImageUrl(productDto.getImageUrl());
        }

        // Kategoria
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria nie istnieje"));
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    // Metoda pomocnicza: Encja -> DTO
    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId()); // Wyciągamy ID z obiektu kategorii
        return dto;
    }
    // Metoda do dodawania opinii
    @Transactional
    public void addReview(Long productId, String author, String content, int rating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie istnieje"));

        Review review = new Review();
        review.setAuthorName(author);
        review.setContent(content);
        review.setRating(rating);
        review.setProduct(product);

        reviewRepository.save(review);
    }

    // Metoda pomocnicza: Pobiera "surowy" produkt (encję) dla widoku szczegółów
    // (Wcześniej mieliśmy tylko DTO, ale do widoku Thymeleaf wygodniej czasem wziąć encję,
    // żeby mieć łatwy dostęp do listy opinii, chociaż profesjonalnie powinno się mapować wszystko na DTO)
    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono produktu"));
    }
    public List<ProductDto> filterProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String name, String sortField, String sortDir) {
        // 1. Ustalanie kierunku sortowania (rosnąco/malejąco)
        org.springframework.data.domain.Sort.Direction direction =
                "desc".equalsIgnoreCase(sortDir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;

        // 2. Ustalanie po czym sortujemy (domyślnie po nazwie)
        String actualSortField = (sortField != null && !sortField.isEmpty()) ? sortField : "name";

        // 3. Wywołanie "inteligentnego zapytania" z repozytorium
        List<Product> products = productRepository.searchProducts(
                categoryId, minPrice, maxPrice, name,
                org.springframework.data.domain.Sort.by(direction, actualSortField)
        );

        // 4. Zamiana na DTO
        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}