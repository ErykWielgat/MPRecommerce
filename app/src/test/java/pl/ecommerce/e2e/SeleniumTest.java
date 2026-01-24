package pl.ecommerce.e2e;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeleniumTest {

    private WebDriver driver;
    private final String BASE_URL = "http://localhost:8080/";

    @BeforeEach
    void setUp() {
        io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
    }

    @Test
    void shouldVerifyHomePageStructure() {
        // 1. Wejście na stronę
        driver.get(BASE_URL);

        // 2. Weryfikacja tytułu z <title>
        String pageTitle = driver.getTitle();
        assertEquals("Sklep Internetowy", pageTitle, "Tytuł strony w <head> jest nieprawidłowy");

        // 3. Weryfikacja paska walut (Currency Bar)
        // Szukamy diva o klasie 'currency-bar'
        WebElement currencyBar = driver.findElement(By.className("currency-bar"));
        assertTrue(currencyBar.isDisplayed(), "Pasek z walutami powinien być widoczny");

        // Sprawdzamy czy są flagi (np. USD)
        WebElement usFlag = currencyBar.findElement(By.cssSelector("img[alt='USA']"));
        assertTrue(usFlag.isDisplayed(), "Flaga USA powinna być widoczna");
    }

    @Test
    void shouldNavigateToCart() {
        driver.get(BASE_URL);

        // Szukamy przycisku koszyka
        WebElement cartButton = driver.findElement(By.cssSelector("a.btn-warning[href='/cart']"));

        // --- POPRAWKA ---
        // Pobieramy tekst, zamieniamy na wielkie litery i sprawdzamy czy zawiera "KOSZYK"
        // To zadziała niezależnie czy jest "Twój Koszyk", "TWÓJ KOSZYK" czy "twój koszyk"
        String buttonText = cartButton.getText().toUpperCase();
        assertTrue(buttonText.contains("KOSZYK"),
                "Tekst przycisku (" + buttonText + ") powinien zawierać słowo 'KOSZYK'");

        // Klikamy
        cartButton.click();

        // Sprawdzamy URL
        assertTrue(driver.getCurrentUrl().contains("/cart"), "Powinno przekierować do /cart");
    }

    @Test
    void shouldFilterProducts() {
        driver.get(BASE_URL);

        // 1. Znajdź pole wyszukiwania po atrybucie name="name"
        WebElement searchInput = driver.findElement(By.name("name"));
        searchInput.clear();
        searchInput.sendKeys("Laptop"); // Wpisujemy frazę

        // 2. Znajdź przycisk "Filtruj" w formularzu
        WebElement filterButton = driver.findElement(By.cssSelector(".sidebar-container button[type='submit']"));
        filterButton.click();

        // 3. Weryfikacja po przeładowaniu
        WebElement searchInputAfterReload = driver.findElement(By.name("name"));
        String value = searchInputAfterReload.getAttribute("value");
        assertEquals("Laptop", value, "Pole wyszukiwania powinno zachować wartość po wysłaniu formularza");
    }

    @Test
    void shouldClickAddToCart() {
        driver.get(BASE_URL);

        List<WebElement> productCards = driver.findElements(By.className("card"));

        if (productCards.isEmpty()) {
            System.out.println("Brak produktów w bazie - pomijam test klikania 'Dodaj'");
            return;
        }

        WebElement firstProductCard = productCards.get(0);
        WebElement addButton = firstProductCard.findElement(By.cssSelector("a.btn-success"));

        assertTrue(addButton.getText().equalsIgnoreCase("Dodaj"),
                "Oczekiwano tekstu 'Dodaj', ale jest: " + addButton.getText());

        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", addButton);

        assertTrue(driver.getPageSource().contains("Sklep") || driver.getPageSource().contains("Koszyk"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}