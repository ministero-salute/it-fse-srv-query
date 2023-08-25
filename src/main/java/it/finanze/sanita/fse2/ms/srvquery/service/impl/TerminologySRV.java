package it.finanze.sanita.fse2.ms.srvquery.service.impl;

import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Resource.SECURITY_CODE_NORMAL;
import static it.finanze.sanita.fse2.ms.srvquery.config.Constants.Resource.SECURITY_SYSTEM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.srvquery.client.IConverterClient;
import it.finanze.sanita.fse2.ms.srvquery.client.IWebScrapingClient;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.TerminologyClient;
import it.finanze.sanita.fse2.ms.srvquery.config.TerminologyCFG;
import it.finanze.sanita.fse2.ms.srvquery.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.GetActiveResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.InvalidateResultDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.MetadataResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.RequestDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.SystemUrlDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.request.CreateCodeSystemReqDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.ConversionResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.CreateCodeSystemResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.GetResDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.SummaryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.GetResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.dto.response.terminology.UploadResponseDTO;
import it.finanze.sanita.fse2.ms.srvquery.enums.FormatEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.MetadataResourceTypeEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.ResultPushEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.SubscriptionEnum;
import it.finanze.sanita.fse2.ms.srvquery.enums.TypeEnum;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.DiffCheckerFirstVersionException;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MetadataResourceNotFoundException;
import it.finanze.sanita.fse2.ms.srvquery.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.srvquery.utility.FHIRR4Helper;
import it.finanze.sanita.fse2.ms.srvquery.utility.MetadataUtility;
import it.finanze.sanita.fse2.ms.srvquery.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/** 
 * FHIR Service Implementation 
 */
@Service
@Slf4j
public class TerminologySRV implements ITerminologySRV {

	@Autowired
	private TerminologyCFG terminologyCFG;

	private TerminologyClient terminologyClient;

	@Autowired
	private IConverterClient converter;

	@Autowired
	private IWebScrapingClient client;

	private TerminologyClient getTerminologyClient() {
		if (terminologyClient == null) {
			synchronized (this) {
				if (terminologyClient == null) {
					terminologyClient = new TerminologyClient(terminologyCFG.getFhirServerUrl(), terminologyCFG.getFhirServerUser(), terminologyCFG.getFhirServerPwd());
				}
			}
		}
		return terminologyClient;
	}

	@Override
	public void manageSubscription(SubscriptionEnum subscriptionEnum, SubscriptionStatus actionEnum) {
		TerminologyClient terminologyClient = getTerminologyClient();

		if(SubscriptionEnum.ALL.equals(subscriptionEnum)) {
			for(SubscriptionEnum s : SubscriptionEnum.values()) {
				if(SubscriptionEnum.ALL.equals(s)) {
					continue;
				}
				terminologyClient.manageSubscription(s,actionEnum,terminologyCFG.getPolicyManagerUrl());		
			}
		} else {
			terminologyClient.manageSubscription(subscriptionEnum,actionEnum,terminologyCFG.getPolicyManagerUrl());
		}
	}

	@Override
	public String insertCodeSystem(String name, String oid, String version, List<CodeDTO> codes) {
		TerminologyClient terminologyClient = getTerminologyClient();
		return terminologyClient.insertCS(oid, name, version, codes);
	}


	@Override
	public CreateCodeSystemResDTO manageCodeSystem(final CreateCodeSystemReqDTO dto) {
		CreateCodeSystemResDTO out = new CreateCodeSystemResDTO();

		CodeSystem codeSystem = getCodeSystemById("urn:oid:"+dto.getOid());

		if(codeSystem==null) {
			String id = insertCodeSystem(dto.getName(), dto.getOid(), dto.getVersion(), dto.getCodes()); 
			out.setId(id);
		} else if(PublicationStatus.DRAFT.equals(codeSystem.getStatusElement().getValue())) {
			updateCodeSystem(codeSystem, dto.getCodes());
			out.setId(codeSystem.getId());
		}

		return out;
	}

	private CodeSystem getCodeSystemById(final String oid) {
		TerminologyClient terminologyClient = getTerminologyClient();
		return terminologyClient.getCodeSystemById(oid);
	}

	private void updateCodeSystem(final CodeSystem codeSystem, List<CodeDTO> codes) {
		TerminologyClient terminologyClient = getTerminologyClient();
		terminologyClient.updateCS(codeSystem, codes);
	}

	@Override
	public List<MetadataResourceDTO> manageMetadataResource(final List<SystemUrlDTO> list) {
		TerminologyClient terminologyClient = getTerminologyClient();
		List<MetadataResourceDTO> out = new ArrayList<>();		

		for(SystemUrlDTO entry : list) {
			ResultPushEnum esito = null;

			String res = client.webScraper(entry.getUrl());
			if(StringUtility.isNullOrEmpty(res)) {
				esito = ResultPushEnum.RESOURCE_NOT_FOUND;
				out.add(new MetadataResourceDTO(entry.getSystem(),entry.getUrl(),  esito));
				continue;
			}
			esito = terminologyClient.handlePullMetadataResource(res, entry.getForceDraft());
			out.add(new MetadataResourceDTO(entry.getSystem(), entry.getUrl(),esito));
		}
		return out;
	}

	@Override
	public UploadResponseDTO uploadTerminology(FormatEnum formatEnum,RequestDTO creationInfo, MultipartFile file) throws IOException {
		log.info("Upload terminology with format:" + formatEnum);
		TerminologyClient terminologyClient = getTerminologyClient();

		String fhirBundle = new String(file.getBytes() ,StandardCharsets.UTF_8);
		if(!FormatEnum.FHIR_R4_JSON.equals(formatEnum)) {
			if(creationInfo == null) {
				creationInfo = new RequestDTO();
				creationInfo.setType(TypeEnum.CODE_SYSTEM);
			}
			ConversionResponseDTO res = converter.callConvertToFhirJson(formatEnum,creationInfo,file);
			fhirBundle = res.getResult();
		}

		CodeSystem codeSystem = FHIRR4Helper.deserializeResource(CodeSystem.class, fhirBundle, true);
		String location = terminologyClient.transaction(codeSystem);

		UploadResponseDTO out = new UploadResponseDTO();
		out.setLocation(location);
		out.setInsertedItems(codeSystem.getConcept().size());
		return out;
	}


	@Override
	public GetResponseDTO isPresent(String oid, String version) {
		GetResponseDTO out = new GetResponseDTO(); 
		TerminologyClient terminologyClient = getTerminologyClient();
		CodeSystem codeSystem = terminologyClient.getCodeSystemByIdAndVersion(oid, version);
		out.setPresent(codeSystem!=null);
		out.setCounter(codeSystem!=null ? codeSystem.getCount() : null);
		out.setId(codeSystem!=null ? codeSystem.getIdElement().getIdPartAsLong().toString() : null);
		return out;
	}

	@Override
	public void deleteById(String id) {
		TerminologyClient terminologyClient = getTerminologyClient();
		terminologyClient.deleteCS(id);
	}

	@Override
	public List<GetActiveResourceDTO> getSummaryNameActiveResource() {
		List<GetActiveResourceDTO> out = new ArrayList<>();
		TerminologyClient terminologyClient = getTerminologyClient();
		List<MetadataResource> metadataResources = terminologyClient.searchAllMRSummaryNames(true);
		for(MetadataResource metadataResource : metadataResources) {
			GetActiveResourceDTO activeResource = new GetActiveResourceDTO();
			String id = metadataResource.getIdElement().getIdPartAsLong().toString();
			activeResource.setId(id); 
			Optional<String> oid = Optional.empty();
			if(metadataResource instanceof CodeSystem) {
				CodeSystem codeSystem = (CodeSystem)metadataResource;
				oid = MetadataUtility.hasOID(codeSystem);
				activeResource.setMetadataType(MetadataResourceTypeEnum.CODE_SYSTEM);
			} else if(metadataResource instanceof ValueSet) {
				ValueSet valueSet = (ValueSet)metadataResource;
				oid = MetadataUtility.hasOID(valueSet);
				activeResource.setMetadataType(MetadataResourceTypeEnum.VALUE_SET);
			} else if(metadataResource instanceof ConceptMap) {
				ConceptMap conceptMap = (ConceptMap)metadataResource;
				oid = MetadataUtility.hasOID(conceptMap);
				activeResource.setMetadataType(MetadataResourceTypeEnum.CONCEPT_MAP);
			}
			
			if(oid.isPresent()) {
				activeResource.setOid(StringUtility.removeUrnOidFromSystem(oid.get()));
			}
				
			out.add(activeResource);
		}
		return out;
	}

	@Override
	public GetResDTO export(String id, FormatEnum format) {
		GetResDTO out = new GetResDTO();
		TerminologyClient terminologyClient = getTerminologyClient();
		CodeSystem codeSystem = terminologyClient.getContentById(id);
		String resource = FHIRR4Helper.serializeResource(codeSystem, true, true, false);

		String oid = StringUtility.removeUrnOidFromSystem(codeSystem.getIdentifier().get(0).getValue());
		out.setOid(oid);
		out.setExportable(false);


		if(codeSystem.getMeta().getSecurity()==null || 
				codeSystem.getMeta().getSecurity().isEmpty() || codeSystem.getMeta().getSecurity(SECURITY_SYSTEM, SECURITY_CODE_NORMAL)!=null) {
			try {
				out.setExportable(true);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				if(FormatEnum.FHIR_R4_JSON.equals(format)) {
					outputStream.write(resource.getBytes());
					byte[] finalBytes = outputStream.toByteArray();
					out.setContent(finalBytes);
				} else {
					ConversionResponseDTO conversionResponseDTO = converter.callConvertFromFhirJson(format, oid,resource.getBytes());
					outputStream.write(conversionResponseDTO.getResult().getBytes());
					byte[] finalBytes = outputStream.toByteArray();
					out.setContent(finalBytes);
				}
			}catch(Exception ex) {
				log.error("Error while export : " , ex);
				throw new BusinessException(ex);
			}
		}  
		return out;
	}

	@Override
	public List<ResourceDTO> searchResourceByIdAndVersion(String identifier, String versionFrom, String versionTo, TypeEnum type) {
		List<ResourceDTO> out = new ArrayList<>();
		
		if(StringUtility.validateOid(identifier)){
			out.addAll(searchResourceByIdAndResourceVersion(identifier, versionFrom, versionTo, type));
		} else {
			out.addAll(searchResourceByIdAndHistoryVersion(identifier, versionFrom, versionTo, type));
		}
	 
		return out;
	}
	
	private List<ResourceDTO> searchResourceByIdAndHistoryVersion(String identifier, String versionFrom, String versionTo, TypeEnum type){
		List<ResourceDTO> out = new ArrayList<>();
		TerminologyClient terminologyClient = getTerminologyClient();
		MetadataResource mrNew = terminologyClient.searchMetadataResourceByIdAndHistory(terminologyCFG.getFhirServerUrl(), type.getMetadataResourceClass(), identifier, versionTo);
		Integer toVersionRetrieved = Integer.parseInt(mrNew.getMeta().getVersionIdElement().asStringValue());
		
		if(toVersionRetrieved==1) {
			throw new DiffCheckerFirstVersionException("Esiste solo una versione per l'id selezionato. Non è possibile eseguire la diff");
		}
		
		ResourceDTO resourceNew = new ResourceDTO();
		resourceNew.setMetadataresource(FHIRR4Helper.serializeResource(mrNew, false, false, false));
		resourceNew.setVersion(""+toVersionRetrieved);
		
		
		if(StringUtility.isNullOrEmpty(versionFrom)) {
			versionFrom = "" + (toVersionRetrieved - 1);
		}
		MetadataResource mrOld = terminologyClient.searchMetadataResourceByIdAndHistory(terminologyCFG.getFhirServerUrl(),type.getMetadataResourceClass(), identifier, versionFrom);
		ResourceDTO resourceOld = new ResourceDTO();
		resourceOld.setMetadataresource(FHIRR4Helper.serializeResource(mrOld, false, false, false));
		resourceOld.setVersion(versionFrom);

		out.add(resourceOld);
		out.add(resourceNew);
		return out;
	}
	
	private List<ResourceDTO> searchResourceByIdAndResourceVersion(String identifier, String versionFrom, String versionTo, TypeEnum type){
		List<ResourceDTO> out = new ArrayList<>();
		TerminologyClient terminologyClient = getTerminologyClient();
		MetadataResource mrNew = terminologyClient.getMetadataResourceByOidAndVersion(identifier,versionTo,type.getMetadataResourceClass());
		if(mrNew==null) {
			throw new MetadataResourceNotFoundException(String.format("Risorsa con oid %s e version %s non trovata sul server fhir", identifier,versionTo));
		}
		
		
		ResourceDTO resourceNew = new ResourceDTO();
		resourceNew.setMetadataresource(FHIRR4Helper.serializeResource(mrNew, false, false, false));
		resourceNew.setVersion(versionTo);
		
		MetadataResource mrOld = terminologyClient.getMetadataResourceByOidAndVersion(identifier,versionFrom ,type.getMetadataResourceClass());
		if(mrOld==null) {
			throw new MetadataResourceNotFoundException(String.format("Risorsa con oid %s e version %s non trovata sul server fhir", identifier,versionFrom));
		}
		ResourceDTO resourceOld = new ResourceDTO();
		resourceOld.setMetadataresource(FHIRR4Helper.serializeResource(mrOld, false, false, false));
		resourceOld.setVersion(versionFrom);

		out.add(resourceOld);
		out.add(resourceNew);
		return out;
	}

	@Override
	public void expandValuesetAfterChangeCodeySystem(final String oid) {
		TerminologyClient terminologyClient = getTerminologyClient();
		CodeSystem codeSystem = terminologyClient.getCodeSystemById(oid);
		if(codeSystem!=null) {
			List<ValueSet> valuesets = terminologyClient.getAllVSByCodeSystemUrl(codeSystem.getUrl());
			for(ValueSet valueset : valuesets) {
				String valuesetId = valueset.getId();
				Date lastExpandedDate = terminologyClient.getLastDateExpandVS(valuesetId);
				if(lastExpandedDate.before(codeSystem.getDate())) {
					terminologyClient.getInvalidateExpandVS(valuesetId);
				}
			}
		}
	}

	
	@Override
	public List<SummaryResourceDTO> getSummaryNameAllResource() {
		List<SummaryResourceDTO> out = new ArrayList<>();
		TerminologyClient terminologyClient = getTerminologyClient();
		List<MetadataResource> metadataResources = terminologyClient.searchAllMRSummaryNames(false);
		for(MetadataResource metadataResource : metadataResources) {
			SummaryResourceDTO summaryResource = new SummaryResourceDTO();
			String id = metadataResource.getIdElement().getIdPartAsLong().toString();
			summaryResource.setResourceId(id);
			summaryResource.setStatus(metadataResource.hasStatusElement() ? metadataResource.getStatusElement().asStringValue() : "");
			summaryResource.setUrl(metadataResource.getUrl());
			summaryResource.setVersion(metadataResource.getVersion());
			summaryResource.setLastUpdated(metadataResource.getMeta().getLastUpdated());
			summaryResource.setExportable(isExportable(metadataResource));
			Optional<String> oid = Optional.empty();
			if(metadataResource instanceof CodeSystem) {
				CodeSystem codeSystem = (CodeSystem)metadataResource;
				oid = MetadataUtility.hasOID(codeSystem);
				summaryResource.setMetadataType(MetadataResourceTypeEnum.CODE_SYSTEM);
				if(codeSystem.getContent() != null) {
					summaryResource.setContent(codeSystem.getContent().getDisplay());
				}
			} else if(metadataResource instanceof ValueSet) {
				ValueSet valueSet = (ValueSet)metadataResource;
				oid = MetadataUtility.hasOID(valueSet);
				summaryResource.setMetadataType(MetadataResourceTypeEnum.VALUE_SET);
			} else if(metadataResource instanceof ConceptMap) {
				ConceptMap conceptMap = (ConceptMap)metadataResource;
				oid = MetadataUtility.hasOID(conceptMap);
				summaryResource.setMetadataType(MetadataResourceTypeEnum.CONCEPT_MAP);
			}
			
			if(oid.isPresent()) {
				summaryResource.setOid(StringUtility.removeUrnOidFromSystem(oid.get()));
			}
				
			out.add(summaryResource);
		}
		return out;
	}

	public List<InvalidateResultDTO> invalidateExpansion(String oidCS, String versionCS) {
		TerminologyClient tc = getTerminologyClient();
		CodeSystem codeSystem = tc.getCodeSystemByIdAndVersion(oidCS, versionCS);
		List<ValueSet> vss = tc.getRelatedVS(codeSystem);
		return tc.invalidateExpansion(vss);
	}
	
	public boolean isExportable(MetadataResource resource) {
		return  resource.getMeta().getSecurity() == null || 
				resource.getMeta().getSecurity().isEmpty() ||
				resource.getMeta().getSecurity(SECURITY_SYSTEM, SECURITY_CODE_NORMAL) != null;
	}
	
}
