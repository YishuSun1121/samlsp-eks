package com.nextjump.saml2.ext.metadata;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLMessageContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MetadataDisplayFilter extends org.springframework.security.saml.metadata.MetadataDisplayFilter {

    @Override
    protected void processMetadataDisplay(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            SAMLMessageContext context = contextProvider.getLocalEntity(request, response);
            String entityId = context.getLocalEntityId();
            response.setContentType("application/samlmetadata+xml"); // SAML_Meta, 4.1.1 - line 1235
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + entityId + ".xml\"");
            displayMetadata(entityId, response.getWriter());
        } catch (MetadataProviderException e) {
            throw new ServletException("Error initializing metadata", e);
        }
    }
}
