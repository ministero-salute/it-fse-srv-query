package it.finanze.sanita.fse2.ms.srvquery.interceptor;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class ClientTenantPartitionInterceptor implements IClientInterceptor {
    
    private static final String TENANT_ID_HEADER = "X-Tenant-ID";
    private final ThreadLocal<String> tenantId = new ThreadLocal<>();
    
    public void setTenantId(String id) {
        tenantId.set(id);
    }
    
    public void clearTenantId() {
        tenantId.remove();
    }
    
    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        String currentTenantId = tenantId.get();
        if (currentTenantId != null && !currentTenantId.isEmpty()) {
            theRequest.addHeader(TENANT_ID_HEADER, currentTenantId);
        }
    }
    
    @Override
    public void interceptResponse(IHttpResponse theResponse) {
        // no action
    }
}
