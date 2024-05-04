import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

// TODO:
//  1. Move each component to its own package for reuse (text cleaner, http client, etc.)
//  2. Create URLs with builder (not in constants)
//  3. Use POM for UI tests
//  4. Handle errors/exceptions
//  5. Use logger instead of sout

public class SepioTest {
    public static final String SECTION_NAME = "Test-driven development";
    public static final String WIKI_PAGE_NAME = "Test_automation";
    private static final String WIKI_URL = "https://en.wikipedia.org";
    private static final String WIKI_UI_URL = WIKI_URL + "/wiki/" + WIKI_PAGE_NAME;
    private static final String API_URL = WIKI_URL + "/w/api.php?action=query&format=json&prop=extracts&titles=" + WIKI_PAGE_NAME + "&explaintext=True";

    private WebDriver driver;

    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void testWordCount() throws IOException {
        // UI Testing
        driver.get(WIKI_UI_URL);
        String uiText = extractTextFromUI();
        String cleanUiText = cleanUpText(uiText);
        Map<String, Integer> uiWordCount = countWords(cleanUiText);

        // API Testing
        String apiText = extractTextFromAPI();
        String cleanApiText = cleanUpText(apiText);
        Map<String, Integer> apiWordCount = countWords(cleanApiText);

        // Assertion
        System.out.println("FromUI: ");
        printUniqueWords(uiWordCount);
        System.out.println("\n\n\nFromAPI: ");
        printUniqueWords(apiWordCount);

        assertEquals(uiWordCount.size(), apiWordCount.size());
    }

    private String extractTextFromUI() {
        WebElement page_content = driver.findElement(By.xpath("//div[@id='mw-content-text']"));
        WebElement title = page_content.findElement(By.xpath("//span[@id='Test-driven_development']"));
        WebElement section = title.findElement(By.xpath("parent::*/following-sibling::p"));
        return title.getText() + " " + section.getText();
        }

    private String extractTextFromAPI() throws IOException {
        StringBuilder response = new StringBuilder();
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Extract section
        String title = "=== " + SECTION_NAME + " ===";
        String sectionEnd = "[===|==]";
        String section = response.toString().split(title)[1].split(sectionEnd)[0].replace("\\n", "");
        return title + " " + section;
    }

    private String cleanUpText(String text) {
        List<String> blackListWords = new LinkedList<>();
        blackListWords.add("citation needed");
        String cleanedText = text.toLowerCase().replaceAll("[^a-zA-Z]", " ");
        for (String blackListWord : blackListWords) {
            cleanedText = cleanedText.replace(blackListWord, "");
        }
        return cleanedText;
    }

    private Map<String, Integer> countWords(String text) {
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.trim().isEmpty()) {
                continue;
            }
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        return wordCount;
    }

    private void printUniqueWords(Map<String, Integer> words) {
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
