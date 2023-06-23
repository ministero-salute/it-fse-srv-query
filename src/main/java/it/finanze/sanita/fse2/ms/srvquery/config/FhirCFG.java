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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/** 
 * FHIR Server Config 
 *
 */
@Data
@Component
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

}
