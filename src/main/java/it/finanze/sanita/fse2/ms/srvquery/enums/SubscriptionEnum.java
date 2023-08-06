package it.finanze.sanita.fse2.ms.srvquery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SubscriptionEnum {
	
	ALL(null,null),
	CODESYSTEM_ACTIVE("CodeSystem","active"),
	CODESYSTEM_DRAFT("CodeSystem","draft"),
	CODESYSTEM_RETIRED("CodeSystem","retired"),
	VALUESET_ACTIVE("ValueSet","active"),
	VALUESET_DRAFT("ValueSet","draft"),
	VALUESET_RETIRED("ValueSet","retired"),
	CONCEPTMAP_ACTIVE("ConceptMap","active"),
	CONCEPTMAP_DRAFT("ConceptMap","draft"),
	CONCEPTMAP_RETIRED("ConceptMap","retired");
	
	private String risorsa;
	private String criteria;
	 
}
