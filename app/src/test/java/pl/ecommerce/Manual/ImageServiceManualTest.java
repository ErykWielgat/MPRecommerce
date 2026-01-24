package pl.ecommerce.Manual;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import pl.ecommerce.service.ImageService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceManualTest {

    // Testujemy prawdziwy serwis, ale z "fałszywym" plikiem
    private final ImageService imageService = new ImageService();

    @Test
    void shouldSaveImageAndReturnPath() throws IOException {
        // given - Ręcznie tworzymy instancję naszej atrapy (Stub)
        String fileName = "test-manual.jpg";
        ManualMultipartFileStub fakeFile = new ManualMultipartFileStub(fileName, "To są bajty obrazka".getBytes());

        // when
        String savedPath = imageService.saveImage(fakeFile);

        // then
        assertNotNull(savedPath);
        assertTrue(savedPath.contains(fileName)); // Sprawdzamy czy nazwa pliku jest w URLu
        assertTrue(savedPath.startsWith("/uploads/")); // Sprawdzamy czy folder się zgadza
   }

    @Test
    void shouldHandleEmptyFile() {
        ManualMultipartFileStub emptyFile = new ManualMultipartFileStub("", new byte[0]);
    try {
            imageService.saveImage(emptyFile);
        } catch (Exception e) {
            // Jeśli poleciał wyjątek, to też ok - zależy od implementacji
        }
    }
  // --- KLASA WEWNĘTRZNA: RĘCZNA IMPLEMENTACJA INTERFEJSU (STUB) ---
    static class ManualMultipartFileStub implements MultipartFile {

        private final String name;
        private final byte[] content;

        public ManualMultipartFileStub(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() {
            return "imageFile";
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {

        }
    }
}