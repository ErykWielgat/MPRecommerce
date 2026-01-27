/*
 * Obiekt transferu danych (DTO) dla kategorii, służący do bezpiecznego przesyłania danych między frontendem a backendem bez odsłaniania całej encji bazy danych.
 */
package pl.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok automatycznie generuje gettery, settery i metody toString, redukując ilość kodu.
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Nazwa kategorii jest wymagana")
    private String name;

    private String description;
}