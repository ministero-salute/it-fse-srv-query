package it.finanze.sanita.fse2.ms.srvquery.service;

import java.io.IOException;
import java.util.List;

import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.GetActiveResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;

public interface ITerminologySRV {

	void manageSubscription(SubscriptionEnum subscriptionEnum, SubscriptionStatus actionEnum);

	String insertCodeSystem(String name, String oid, String version, List<CodeDTO> codes);
	
	List<MetadataResourceDTO> manageMetadataResource(List<SystemUrlDTO> list);
	
	CreateCodeSystemResDTO manageCodeSystem(CreateCodeSystemReqDTO dto);
	
	UploadResponseDTO uploadTerminology(FormatEnum formatEnum,RequestDTO creationInfo, MultipartFile file) throws IOException;
	
	GetResponseDTO isPresent(String oid, String version);
	
	void deleteById(String id);
	
	List<GetActiveResourceDTO> getSummaryNameActiveResource();
	
	GetResDTO export(String id, FormatEnum format, boolean strict);
}
