package pl.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data // Lombok: tworzy gettery, settery, toString
public class ProductDto {

    private Long id; // ID odsyłamy klientowi, ale przy tworzeniu będzie puste

    @NotBlank(message = "Nazwa produktu jest wymagana") // Nie może być null ani puste ""
    @Size(min = 3, max = 100, message = "Nazwa musi mieć od 3 do 100 znaków")
    private String name;

    @Size(max = 500, message = "Opis zbyt długi")
    private String description;

    @NotNull(message = "Cena jest wymagana")
    @DecimalMin(value = "0.01", message = "Cena musi być większa od 0")
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;

    @NotNull(message = "Kategoria jest wymagana")
    private Long categoryId; // Klient podaje tylko ID kategorii, np. 5 (Elektronika)
    private String newCategoryName;
}