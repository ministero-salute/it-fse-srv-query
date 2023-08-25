package it.finanze.sanita.fse2.ms.srvquery.utility;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.ValueSet;

public class MetadataUtility {
	
	private static final String OID_REF = "urn:oid:";

	public static Optional<String> hasOID(CodeSystem res) {
	    return findOID(res.getIdentifier());
	}

	public static Optional<String> hasOID(ValueSet res) {
	    return findOID(res.getIdentifier());
	}

	public static Optional<String> hasOID(ConceptMap res) {
	    return findOID(Collections.singletonList(res.getIdentifier()));
	}

	private static Optional<String> findOID(List<Identifier> identifiers) {
	    String oid = null;
	    Optional<Identifier> id = identifiers.stream().filter(MetadataUtility::matchOid).findFirst();
	    if(id.isPresent()) {
	        Identifier identifier = id.get();
	        oid = identifier.getSystem();
	    }
	    return Optional.ofNullable(oid);
	}

	private static boolean matchOid(Identifier id) {return id.getSystem().startsWith(OID_REF);}
	
}
