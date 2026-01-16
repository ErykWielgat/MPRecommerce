package pl.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.exception.ResourceNotFoundException;
import pl.ecommerce.model.Category;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.CategoryRepository;
import pl.ecommerce.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service // Mówi Springowi: To jest serwis, trzymaj go w pamięci
@RequiredArgsConstructor // To jest kluczowe! Lombok generuje konstruktor dla pól 'final'. To zastępuje @Autowired.
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        // 1. Szukamy kategorii, do której ma trafić produkt
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria nie istnieje: " + productDto.getCategoryId()));

        // 2. Tworzymy encję na podstawie DTO
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setImageUrl(productDto.getImageUrl());
        product.setCategory(category); // Przypisujemy relację!

        // 3. Zapisujemy w bazie
        Product savedProduct = productRepository.save(product);

        // 4. Zwracamy zapisany obiekt jako DTO
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
}