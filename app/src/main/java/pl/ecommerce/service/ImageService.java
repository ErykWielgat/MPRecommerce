package pl.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }
        try {
            // Tworzymy folder jeśli nie istnieje
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generujemy unikalną nazwę pliku (żeby się nie nadpisały jak ktoś wrzuci dwa razy "kot.jpg")
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Zapisujemy
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Zwracamy ścieżkę do użycia w HTML (od katalogu static)
            return "/uploads/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}