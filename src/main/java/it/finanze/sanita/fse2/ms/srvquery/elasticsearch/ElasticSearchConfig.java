package it.finanze.sanita.fse2.ms.srvquery.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/** 
 * Contains the ElasticSearch configuration 
s *
 */
@Configuration
public class ElasticSearchConfig {

	/** 
	 * The ElasticSearch username
	 */
	@Value("${elasticsearch.username}")
	private String elasticSearchUsername; 
	
	/** 
	 * The ElasticSearch password
	 */
	@Value("${elasticsearch.password}")
	private String elasticSearchPassword; 
	
	/** 
	 * The ElasticSearch search URL 
	 */
	@Value("${elasticsearch.url}")
	private String elasticSearchUrl; 
	
	/** 
	 * The ElasticSearch port
	 */
	@Value("${elasticsearch.port}")
	private Integer elasticSearchPort; 
	
	/** 
	 * Returns the ElasticSearch client 
	 * @return ElasticsearchClient  The ElasticsearchClient 
	 */
	@Bean
    public ElasticsearchClient buildElasticSearchClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticSearchUsername, elasticSearchPassword));
        RestClient client = RestClient.builder(new HttpHost(elasticSearchUrl, elasticSearchPort, "http"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                    httpAsyncClientBuilder.disableAuthCaching();
                    return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }).build(); 
        
        ElasticsearchTransport transport = new RestClientTransport(client, new JacksonJsonpMapper());
        
        return new ElasticsearchClient(transport);
    }

}
