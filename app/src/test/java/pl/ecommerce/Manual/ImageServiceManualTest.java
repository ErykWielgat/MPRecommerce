package pl.ecommerce.Manual;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import pl.ecommerce.service.ImageService;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class ImageServiceManualTest {

    // --- RĘCZNA ATRAPA (STUB) ---
    // Dziedziczy po prawdziwym serwisie, ale NADPISUJE metodę zapisu.
    static class ImageServiceStub extends ImageService {
        @Override
        public String saveImage(MultipartFile file) {
            if (file.isEmpty()) return null;
            // Zwracamy "udawaną" ścieżkę, nie wykonując operacji na plikach
            return "/uploads/fake_" + file.getOriginalFilename();
        }
    }

    private final ImageService imageService = new ImageServiceStub();

    @Test
    void shouldReturnFakePathUsingPolymorphism() {
        // given
        String fileName = "manual-test.jpg";
        MultipartFile fakeFile = new ManualMultipartFileStub(fileName, "test".getBytes());

        // when
        String savedPath = imageService.saveImage(fakeFile);

        // then
        assertNotNull(savedPath);
        assertEquals("/uploads/fake_manual-test.jpg", savedPath);

    }

    @Test
    void shouldHandleEmptyFileInStub() {
        MultipartFile emptyFile = new ManualMultipartFileStub("", new byte[0]);
        String result = imageService.saveImage(emptyFile);
        assertNull(result);
    }


    static class ManualMultipartFileStub implements MultipartFile {
        private final String name;
        private final byte[] content;

        public ManualMultipartFileStub(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override public String getName() { return "imageFile"; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return "image/jpeg"; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(content); }
        @Override public void transferTo(java.io.File dest) {}
    }
}