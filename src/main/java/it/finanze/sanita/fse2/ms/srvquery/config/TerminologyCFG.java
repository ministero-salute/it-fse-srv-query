package it.finanze.sanita.fse2.ms.srvquery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/** 
 * Terminology Server Config 
 */
@Data
@Component
public class TerminologyCFG {
 
	/** 
	 * Terminology Server URL. 
	 */
	@Value("${terminology-server-url}")
	private String fhirServerUrl;
	
	/** 
	 * Terminology Server User. 
	 */
	@Value("${terminology-server-user}")
	private String fhirServerUser;
	
	/** 
	 * Terminology Server Pwd. 
	 */
	@Value("${terminology-server-pwd}")
	private String fhirServerPwd;
	
	/** 
	 * Terminology Server Pwd. 
	 */
	@Value("${policy-manager-url}")
	private String policyManagerUrl;

}
