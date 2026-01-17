package pl.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pl.ecommerce.model.Category;
import pl.ecommerce.repository.CategoryRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissingServicesTest {

    // --- TESTY IMAGE SERVICE ---
    @InjectMocks
    private ImageService imageService;

    @Test
    void shouldSaveImage() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "fake-content".getBytes()
        );

        // when
        String path = imageService.saveImage(file);

        // then
        assertNotNull(path);
        assertTrue(path.contains("test.jpg"));
        // Sprzątanie po teście (usuń plik, jeśli się utworzył fizycznie)
    }

    // --- TESTY CATEGORY SERVICE ---
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldReturnAllCategories() {
        // given
        when(categoryRepository.findAll()).thenReturn(List.of(new Category(), new Category()));

        // when
        List<?> result = categoryService.getAllCategories();

        // then
        assertEquals(2, result.size());
    }
}