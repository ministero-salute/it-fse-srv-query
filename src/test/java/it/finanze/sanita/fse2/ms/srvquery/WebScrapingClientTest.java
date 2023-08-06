package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.WebScrapingClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class WebScrapingClientTest {

	@Mock
    private RestTemplate restTemplate;

	@InjectMocks
    private WebScrapingClient webScrapingClient;

	@BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testWebScraper() {
        String url = "http://mock";
        String expectedResponse = "<html><body>Example HTML</body></html>";

        when(restTemplate.getForObject(url, String.class)).thenReturn(expectedResponse);

        // Perform webScraper()
        String actualResponse = webScrapingClient.webScraper(url);

        assertEquals(expectedResponse, actualResponse);
    }
	
}
