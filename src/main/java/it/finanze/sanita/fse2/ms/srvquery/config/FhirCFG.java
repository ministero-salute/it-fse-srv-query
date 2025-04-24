/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.srvquery.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative2.NullNarrativeGenerator;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import lombok.Data;

/** 
 * FHIR Server Config 
 *
 */
@Data
@Configuration
public class FhirCFG {
 
	/** 
	 * FHIR Server URL. 
	 */
	@Value("${fhir-server-url}")
	private String fhirServerUrl;
	
	/** 
	 * FHIR Server User. 
	 */
	@Value("${fhir-server-user}")
	private String fhirServerUser;
	
	/** 
	 * FHIR Server Pwd. 
	 */
	@Value("${fhir-server-pwd}")
	private String fhirServerPwd;
	
    @Bean
    public FhirContext fhirContext() {
        FhirContext context = FhirContext.forR4();
        context.setNarrativeGenerator(new NullNarrativeGenerator());
        return context;
    }

    @Bean
    public IGenericClient fhirGenericClient(@Autowired FhirContext fhirContext) {
        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServerUrl);
        client.registerInterceptor(new BasicAuthInterceptor(fhirServerUser, fhirServerPwd));
        return client;
    }
}