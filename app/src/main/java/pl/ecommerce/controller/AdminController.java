package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.model.Product;
import pl.ecommerce.repository.ProductRepository;
import pl.ecommerce.repository.ReviewRepository;
import pl.ecommerce.service.CategoryService;
import pl.ecommerce.service.ImageService;
import pl.ecommerce.service.ProductService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final ProductRepository productRepository; // Do usuwania/edycji bezpośredniej
    private final CategoryService categoryService;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    // USUNIĘTO: CurrencyService - admin nie musi już tym zarządzać ręcznie

    // 1. Dashboard (Lista produktów)
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/dashboard";
    }

    // 2. Formularz dodawania produktu
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    // 3. Zapisywanie produktu (z plikiem!)
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute ProductDto productDto,
                              @RequestParam("imageFile") MultipartFile imageFile) {

        // Jeśli przesłano plik, zapisz go i ustaw URL
        if (!imageFile.isEmpty()) {
            String imageUrl = imageService.saveImage(imageFile);
            productDto.setImageUrl(imageUrl);
        }

        productService.createProduct(productDto);
        return "redirect:/admin";
    }

    // 4. Usuwanie produktu
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/admin";
    }

    // 5. Moderacja Opinii (Lista wszystkich opinii)
    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAll());
        return "admin/reviews";
    }

    // 6. Usuwanie Opinii (Moderacja)
    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/reviews";
    }

    // --- EDYCJA PRODUKTU ---
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        // 1. Pobieramy encję
        Product product = productService.getProductEntity(id);

        // 2. Mapujemy ręcznie na DTO, żeby formularz się wypełnił
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategory().getId());
        dto.setImageUrl(product.getImageUrl());

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getAllCategories());

        // Używamy tego samego formularza co do dodawania
        return "admin/product-form";
    }

    // --- SZCZEGÓŁY DLA ADMINA (Z USUWANIEM OPINII) ---
    @GetMapping("/product/{id}")
    public String adminProductDetails(@PathVariable Long id, Model model) {
        Product product = productService.getProductEntity(id);
        model.addAttribute("product", product);
        return "admin/product-details"; // Nowy widok specjalnie dla admina
    }

    // Metoda do usuwania opinii z poziomu widoku produktu
    @GetMapping("/product/{productId}/delete-review/{reviewId}")
    public String deleteReviewFromProduct(@PathVariable Long productId, @PathVariable Long reviewId) {
        reviewRepository.deleteById(reviewId);
        // Po usunięciu wracamy na stronę TEGO SAMEGO produktu
        return "redirect:/admin/product/" + productId;
    }

    // USUNIĘTO: Metody currencies (show i update)
}