package it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.types;

import it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO;
import it.finanze.sanita.fse2.ms.srvquery.exceptions.MalformedResourceException;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeSystem;

import java.util.ArrayList;
import java.util.Optional;

import static it.finanze.sanita.fse2.ms.srvquery.client.impl.history.base.HistoryUtils.*;
import static it.finanze.sanita.fse2.ms.srvquery.dto.response.history.HistoryResourceDTO.*;

@RequiredArgsConstructor
public class CompactCS {

    private final String resourceId;
    private final String versionId;
    private final CodeSystem cs;

    public HistoryResourceDTO convert() throws MalformedResourceException {

        Optional<String> oid = asOID(cs);

        if(!oid.isPresent()) {
            throw new MalformedResourceException(resourceId, versionId, "Missing OID identifier");
        }

        HistoryResourceDTO res = new HistoryResourceDTO(
            oid.get(),
            cs.getVersion(),
            new ResourceMetaDTO(
                resourceId,
                versionId,
                cs.fhirType(),
                cs.getDate()
            ),
            new ArrayList<>()
        );

        for (CodeSystem.ConceptDefinitionComponent def : cs.getConcept()) {
            res.getItems().add(new ResourceItemDTO(def.getCode(), def.getDisplay()));
        }

        return res;
    }

}
