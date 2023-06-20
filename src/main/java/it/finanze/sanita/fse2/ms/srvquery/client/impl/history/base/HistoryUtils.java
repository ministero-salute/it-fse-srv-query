package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hl7.fhir.instance.model.api.IBaseBundle.LINK_NEXT;
import static org.hl7.fhir.r4.model.Enumerations.PublicationStatus;

public class HistoryUtils {

    private static final String OID_REF = "urn:ietf:rfc:3986";
    private static final String OID_PREFIX = "urn:oid:";

    public static String asId(String uri) {
        int idx = uri.lastIndexOf("/") + 1;
        return uri.substring(idx);
    }

    public static String asId(IBaseResource res) {
        return res.getIdElement().getIdPart();
    }

    public static String asVersionId(IBaseResource res) {
        return res.getMeta().getVersionId();
    }

    public static Optional<String> asOID(CodeSystem res) {
        String oid = null;
        Optional<Identifier> id = res.getIdentifier().stream().filter(HistoryUtils::matchOid).findFirst();
        if(id.isPresent()) {
            Identifier identifier = id.get();
            String value = identifier.getValue();
            oid = value.replace(OID_PREFIX, "");
        }
        return Optional.ofNullable(oid);
    }

    public static Optional<String> asOID(ValueSet res) {
        String oid = null;
        Optional<Identifier> id = res.getIdentifier().stream().filter(HistoryUtils::matchOid).findFirst();
        if(id.isPresent()) {
            Identifier identifier = id.get();
            String value = identifier.getValue();
            oid = value.replace(OID_PREFIX, "");
        }
        return Optional.ofNullable(oid);
    }

    public static List<String> asId(List<Resource> res) {
        return res.stream().map(r -> r.getIdElement().getIdPart()).collect(Collectors.toList());
    }

    public static PublicationStatus getPublicationStatus(Resource res) {
        return PublicationStatus.fromCode(
            res
            .getNamedProperty(CodeSystem.SP_STATUS)
            .getValues()
            .get(0)
            .primitiveValue()
        );
    }

    @Nullable
    public static Bundle hasNextPage(IGenericClient client, Bundle bundle) {
        // Verify if next page is available
        if(bundle.getLink(LINK_NEXT) != null) {
            // Request next page
            bundle = client.loadPage().next(bundle).execute();
        } else {
            bundle = null;
        }
        return bundle;
    }

    private static boolean matchOid(Identifier id) {return id.getSystem().equalsIgnoreCase(OID_REF);}
}
