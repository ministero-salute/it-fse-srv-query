/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.srvquery.client.impl;


import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.model.api.annotation.Block;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;

@ResourceDef(name="CapabilityStatement", profile="http://hl7.org/fhir/StructureDefinition/CapabilityStatement")
public class CustomCapabilityStatement extends CapabilityStatement {

	private static final long serialVersionUID = 495587345059750716L;
	

	@Block()
	public static class CustomCapabilityStatementRestResourceComponent extends CapabilityStatementRestResourceComponent {

		private static final long serialVersionUID = 3244593658380411139L;
		
		@Child(name = "searchPath", type = {StringType.class}, order=18, min=1, max=4, modifier=false, summary=false)
        @Description(shortDefinition="Path of search parameter", formalDefinition="The path of the search parameter" )
		protected List<StringType> searchPath;
		
		
		
		public List<StringType> getSearchPath() {
			return searchPath;
		}

		public CustomCapabilityStatementRestResourceComponent(String code, List<StringType> searchPaths) {
			super(new CodeType(code));
			this.searchPath = searchPaths;
		}
		
		public CustomCapabilityStatementRestResourceComponent(String code) {
			this(code, new ArrayList<>());
		} 
		
		
		public CustomCapabilityStatementRestResourceComponent() {
			super();
		}
		
	}
	
	public CustomCapabilityStatement() {
		super();
	}
	
	
	@Child(name = "resourceSearchPaths", type = {}, order=0, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=true)
    @Description(shortDefinition="Search paths that are defined by the resources" )
    protected List<CustomCapabilityStatementRestResourceComponent> resourceSearchPaths;
	
	public List<CustomCapabilityStatementRestResourceComponent> getResourceSearchPaths() {
		return resourceSearchPaths;
	}

	public void setResourceSearchPaths(List<CustomCapabilityStatementRestResourceComponent> resourceSearchPaths) {
		this.resourceSearchPaths = resourceSearchPaths;
	}
	
	
}
