package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.srvquery.client.GenericClient;
import it.finanze.sanita.fse2.ms.srvquery.config.MsUrlCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;

public class ConverterClient extends GenericClient {
    
    @Autowired
	private MsUrlCFG msUrlCFG;

    @Autowired
	private RestTemplate restTemplate;

    public List<ConversionResponseDTO> listToCsv(List<String> resources) {
        List<ConversionResponseDTO> converted = new ArrayList<>();

        for(String resource : resources) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            final String url = msUrlCFG.getMsConverterHost() + "/v1/from-fhir-json/to/{format}";
            ResponseEntity<ConversionResponseDTO> response = restTemplate.postForEntity(url, resource, ConversionResponseDTO.class, headers);
        
            converted.add(response.getBody());
        }

        return converted;
    }
    
}
