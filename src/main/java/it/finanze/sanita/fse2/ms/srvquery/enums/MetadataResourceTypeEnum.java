package it.finanze.sanita.fse2.ms.srvquery.enums;

import static org.hl7.fhir.r4.model.Enumerations.*;

public enum MetadataResourceTypeEnum {
	CODE_SYSTEM,
	VALUE_SET,
	CONCEPT_MAP;

	public static MetadataResourceTypeEnum fromFhirType(String fhirType) {
		MetadataResourceTypeEnum type;
		switch (FHIRDefinedType.fromCode(fhirType)) {
			case CODESYSTEM:
				type = CODE_SYSTEM;
				break;
			case VALUESET:
				type = VALUE_SET;
				break;
			case CONCEPTMAP:
				type = CONCEPT_MAP;
				break;
			default:
				throw new IllegalArgumentException("Unknown/Unsupported type " + fhirType);
		}
		return type;
	}
}
