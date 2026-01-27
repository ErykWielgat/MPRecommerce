/*
 * Globalny uchwyt wyjątków dla API REST: przechwytuje błędy z całej aplikacji i zwraca je klientowi jako czytelny JSON.
 */
package pl.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Wskazuje, że klasa obsługuje błędy dla wszystkich kontrolerów REST i odpowiedzi będą automatycznie serializowane do JSON.
public class GlobalExceptionHandler {

    // Obsługa wyjątku (gdy nie ma produktu)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Obsługa błędów walidacji (np. pusta nazwa, ujemna cena)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Wyciągamy pola, które mają błędy i komunikaty
        // Pętla przetwarza listę błędów z @Valid, przypisując komunikat błędu do nazwy pola, którego dotyczy.
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        //W razie błędu nie zwraca „Whitelabel Error Page”, ale czysty JSON:
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}