/* SPDX-License-Identifier: AGPL-3.0-or-later */
package it.finanze.sanita.fse2.ms.srvquery.isolation;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import it.finanze.sanita.fse2.ms.srvquery.config.CustomSwaggerCFG;
import it.finanze.sanita.fse2.ms.srvquery.config.OpenApiCFG;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@Tag("ISOLATION")
@SpringJUnitConfig({CustomSwaggerCFG.class, OpenApiCFG.class})
@TestPropertySource(properties = {"docs.info.api-id=query", "docs.info.summary=Query API", "docs.info.title=Query",
        "info.app.version=1.0", "docs.info.description=FHIR query", "docs.info.termsOfService=https://example.org/terms",
        "docs.info.contact.name=Support", "docs.info.contact.url=https://example.org", "docs.info.contact.mail=support@example.org",
        "server.port=8088", "validation.file-max-size=1024"})
class OpenApiIsolationTest {
    @Autowired OpenApiCFG configuration;
    @Autowired CustomSwaggerCFG properties;

    @Test void customizerPublishesConfiguredMetadataContactAndSandboxServer() {
        OpenAPI api = new OpenAPI().info(new Info());
        configuration.openApiCustomizer().customise(api);
        assertEquals("Query", api.getInfo().getTitle());
        assertEquals("1.0", api.getInfo().getVersion());
        assertEquals("FHIR query", api.getInfo().getDescription());
        assertEquals("https://example.org/terms", api.getInfo().getTermsOfService());
        assertEquals("Support", api.getInfo().getContact().getName());
        assertEquals("https://example.org", api.getInfo().getContact().getUrl());
        assertEquals("support@example.org", api.getInfo().getContact().getEmail());
        assertEquals("query", api.getInfo().getExtensions().get("x-api-id"));
        assertEquals("Query API", api.getInfo().getExtensions().get("x-summary"));
        assertEquals("http://localhost:8088", api.getServers().getFirst().getUrl());
        assertEquals(true, api.getServers().getFirst().getExtensions().get("x-sandbox"));
        assertEquals(1024, properties.getFileMaxLength());
    }
}
