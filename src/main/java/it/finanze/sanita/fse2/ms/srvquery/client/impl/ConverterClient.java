package it.finanze.sanita.fse2.ms.srvquery.client.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.client.IConverterClient;
import it.finanze.sanita.fse2.ms.srvquery.config.MsUrlCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;

@Component
public class ConverterClient implements IConverterClient {

	@Autowired
	private MsUrlCFG msUrlCFG;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public ConversionResponseDTO callConvertToFhirJson(FormatEnum format, RequestDTO creationInfo, MultipartFile file) throws IOException {

	    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	    body.add("creationInfo", creationInfo);
	    body.add("file", new ByteArrayResource(file.getBytes()) {
	        @Override
	        public String getFilename() {
	            return file.getOriginalFilename();
	        }
	    });

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	    HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

	    String url = msUrlCFG.getMsConverterHost() + "/v1/metadata-resource/from/"+format.toString()+"/to-fhir-json";
	    ConversionResponseDTO out = null;
	    try {
	    	out = restTemplate.postForObject(url, entity, ConversionResponseDTO.class);
	    } catch (ResourceAccessException ex) {
			//TODO - Gestisci il timeout
		}
	    
	    return out;
	}
	
	@Override
	public ConversionResponseDTO callConvertFromFhirJson(FormatEnum format,String oid, byte[] file) throws IOException {

	    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
	    body.add("file", new ByteArrayResource(file) {
	        @Override
	        public String getFilename() {
	            return "TestOid.json";
	        }
	    });

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	    HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
	    String url = msUrlCFG.getMsConverterHost() + "/v1/metadata-resource/from-fhir-json/to/"+format.toString();
	    ConversionResponseDTO out = null;
	    try {
	    	out = restTemplate.postForObject(url, entity, ConversionResponseDTO.class);
	    } catch (ResourceAccessException ex) {
			//TODO - Gestisci il timeout
		}
	    
	    return out;
	}
}
