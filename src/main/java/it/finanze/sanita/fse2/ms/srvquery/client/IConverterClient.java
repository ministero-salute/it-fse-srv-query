package it.finanze.sanita.fse2.ms.srvquery.client;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;

public interface IConverterClient {

	ConversionResponseDTO callConvertToFhirJson(FormatEnum format, RequestDTO creationInfo, MultipartFile file) throws IOException;
	
	ConversionResponseDTO callConvertFromFhirJson(FormatEnum format,String oid, byte[] file);
}
