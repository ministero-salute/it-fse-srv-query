/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import com.google.gson.JsonObject;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import it.finanze.sanita.fse2.ms.srvquery.config.Constants;
import it.finanze.sanita.fse2.ms.srvquery.service.impl.ElasticSearchSRV;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class ElasticSearchTest {

	@Autowired
	public ElasticSearchSRV elasticSearchService; 
	
	
	public static final String TEST_INDEX = "testIndex"; 
	public static final String TEST_ID = "testId"; 
	
	
	@Test
	public void indexTest() throws ElasticsearchException, IOException {
		JsonObject json = new JsonObject(); 
		json.addProperty("key", "value"); 
		IndexResponse response = elasticSearchService.index(TEST_INDEX, TEST_ID, json); 
		
		assertNotNull(response); 
		assertEquals(IndexResponse.class, response.getClass()); 
		
		
	}
}
