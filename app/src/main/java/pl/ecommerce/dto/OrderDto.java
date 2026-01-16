package pl.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderDto {

    @NotBlank(message = "Imię jest wymagane")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    private String lastName;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Niepoprawny format email")
    private String email;

    @NotBlank(message = "Adres jest wymagany")
    private String address;

    @NotBlank(message = "Kod pocztowy jest wymagany")
    // To jest REGEX: Dwie cyfry, myślnik, trzy cyfry
    @Pattern(regexp = "\\d{2}-\\d{3}", message = "Kod pocztowy musi być w formacie XX-XXX (np. 01-234)")
    private String zipCode;

    @NotBlank(message = "Miasto jest wymagane")
    private String city;

    @NotBlank(message = "Wybierz metodę dostawy")
    private String deliveryMethod; // "KURIER" lub "PACZKOMAT"
}