package it.finanze.sanita.fse2.ms.srvquery.client;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import it.finanze.sanita.fse2.ms.srvquery.client.impl.CustomCapabilityStatement;

public interface IFHIRClient {
    
	boolean create(final Bundle bundle, String partitionName);

	boolean delete(Bundle bundle, String partitionName);

	boolean replace(Bundle bundle, String partitionName);

	String transaction(Bundle bundle, String partitionName);

	boolean update(final DocumentReference documentReference, String partitionName);
   
	Bundle getDocument(final String idComposition, final String url, final String partitionId);

	CustomCapabilityStatement getServerCapabilities();

	DocumentReference getDocumentReferenceBundle(final String masterIdentifier, String partitionName);

	public Bundle findByMasterIdentifier(String masterIdentifier, String partitionName);

}
