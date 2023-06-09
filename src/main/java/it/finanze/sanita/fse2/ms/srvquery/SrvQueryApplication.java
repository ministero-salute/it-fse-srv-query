/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.srvquery.client.handler.RestTemplateResponseErrorHandler;

/** 
 * SpringBoot Application 
 *
 */
@SpringBootApplication
@ComponentScan
public class SrvQueryApplication {

	/** 
	 * Runs the SpringBoot Application 
	 * @param args  The args given to main for application start 
	 */
	public static void main(String[] args) {
		SpringApplication.run(SrvQueryApplication.class, args);
	}

	/**
	 * Definizione rest template.
	 * 
	 * @return	rest template
	 */
	@Bean 
	@Qualifier("restTemplate")
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
		return new RestTemplate();
	} 
}
