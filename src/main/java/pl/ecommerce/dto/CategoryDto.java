package pl.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Nazwa kategorii jest wymagana")
    private String name;

    private String description;
}