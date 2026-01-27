package pl.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ecommerce.dto.ProductDto;
import pl.ecommerce.model.Product;
import pl.ecommerce.service.CategoryService;
import pl.ecommerce.service.ImageService;
import pl.ecommerce.service.ProductService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final ProductService productService;
    private final CategoryService categoryService;
    private final ImageService imageService;

    // 1. Dashboard
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/dashboard";
    }

    // 2. Formularz dodawania
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    // 3. Zapisywanie
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute ProductDto productDto,
                              @RequestParam("imageFile") MultipartFile imageFile) {
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
        productService.deleteProduct(id);
        return "redirect:/admin";
    }

    // 5. Edycja
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductEntity(id);
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategory().getId());
        dto.setImageUrl(product.getImageUrl());

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    // 6. Lista opinii
    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        model.addAttribute("reviews", productService.getAllReviews()); // <--- ZMIANA
        return "admin/reviews";
    }

    // 7. Usuwanie opinii
    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        productService.deleteReview(id); // <--- ZMIANA
        return "redirect:/admin/reviews";
    }

    // 8. Szczegóły produktu
    @GetMapping("/product/{id}")
    public String adminProductDetails(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductEntity(id));
        return "admin/product-details";
    }

    // 9. Usuwanie opinii z widoku produktu
    @GetMapping("/product/{productId}/delete-review/{reviewId}")
    public String deleteReviewFromProduct(@PathVariable Long productId, @PathVariable Long reviewId) {
        productService.deleteReview(reviewId); // <--- ZMIANA
        return "redirect:/admin/product/" + productId;
    }
}