package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.srvquery.client.IWebScrapingClient;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebScrapingClient implements IWebScrapingClient {

	@Autowired
	private RestTemplate restTemplate; 

	@Override
	public String webScraper(final String url) {
		String json = "";
		try {
			json = restTemplate.getForObject(url, String.class);
		} catch (Exception ex) {
			log.error("Errow while perform web scraper method:",ex);
		}
		return json;
	}
}
