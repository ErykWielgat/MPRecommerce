package pl.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {

    private Long id;

    @NotBlank(message = "Nazwa produktu jest wymagana")
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
    private Long categoryId;
    private String newCategoryName;
}