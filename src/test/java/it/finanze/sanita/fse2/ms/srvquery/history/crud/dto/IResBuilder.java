package it.finanze.sanita.fse2.ms.srvquery.history.crud.dto;

import org.hl7.fhir.r4.model.BaseResource;

import java.util.Date;

import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus;

public interface IResBuilder {

    String OID_REF = "urn:ietf:rfc:3986";
    String OID_PREFIX = "urn:oid:";

    void addCodes(String code, String display);
    void addIdentifier(String oid);
    void addUrl(String url);
    void addVersion(String version);
    void addStatus(PublicationStatus status);
    void addDate(Date date);

    BaseResource build();

}
