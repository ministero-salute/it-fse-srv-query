package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceResDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.ArrayList;
import java.util.Optional;

import static it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryUtils.asOID;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceResDTO.ResourceItemDTO;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceResDTO.ResourceMetaDTO;
import static org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionComponent;
import static org.hl7.fhir.r4.model.ValueSet.ValueSetExpansionContainsComponent;

@RequiredArgsConstructor
public class CompactVS {

    private final String resourceId;
    private final String versionId;
    private final ValueSet vs;

    public HistoryResourceResDTO convert() throws MalformedResourceException {

        Optional<String> oid = asOID(vs);

        if(!oid.isPresent()) {
            throw new MalformedResourceException(resourceId, versionId, "Missing OID identifier");
        }

        HistoryResourceResDTO res = new HistoryResourceResDTO(
            resourceId,
            versionId,
            new ResourceMetaDTO(
                oid.get(),
                vs.getVersion(),
                vs.getResourceType().getPath(),
                vs.getDate(),
                vs.getExpansion().isEmpty()
            ),
            new ArrayList<>()
        );

        ValueSetExpansionComponent expansion = vs.getExpansion();

        for (ValueSetExpansionContainsComponent v : expansion.getContains()) {
            res.getItems().add(new ResourceItemDTO(v.getCode(), v.getDisplay()));
        }

        return res;
    }

}
