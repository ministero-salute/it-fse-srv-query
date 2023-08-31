package it.finanze.sanita.fse2.ms.srvquery.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.srvquery.client.impl.WebScrapingClient;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class WebScrapingClientTest {

	@MockBean
    private RestTemplate restTemplate;

	@Autowired
    private WebScrapingClient webScrapingClient;

    @Test
    void testWebScraper() {
    	// Mock
        String url = "http://mock";
        String expectedResponse = "<html><body>Example HTML</body></html>";
        when(restTemplate.getForObject(url, String.class)).thenReturn(expectedResponse);
        // Perform webScraper()
        String actualResponse = webScrapingClient.webScraper(url);
        // Assertion
        assertEquals(expectedResponse, actualResponse);
    }
	
}
