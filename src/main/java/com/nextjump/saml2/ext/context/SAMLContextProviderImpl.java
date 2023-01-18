package com.nextjump.saml2.ext.context;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLMessageContext;

public class SAMLContextProviderImpl extends org.springframework.security.saml.context.SAMLContextProviderImpl {
    protected AppSettings appSettings;

    public SAMLContextProviderImpl(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    protected void populateLocalEntityId(SAMLMessageContext context, String requestURI) throws MetadataProviderException {
        if (requestURI == null) {
            requestURI = "";
        }
        boolean handleByExtraSp = false;
        for (int i = 0; i < appSettings.getExtraSpSsoPaths().size(); i++) {
            String extraSpSsoPath = appSettings.getExtraSpSsoPaths().get(i);
            if (!Strings.isNullOrEmpty(extraSpSsoPath)) {
                if (requestURI.indexOf(extraSpSsoPath) > -1) {
                    handleByExtraSp = true;
                    String entityId = appSettings.getExtraSpEntityIds().get(i);
                    context.setLocalEntityId(entityId);
                    context.setLocalEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    break;
                }
            }
        }
        for (int i = 0; i < appSettings.getExtraSpSloPaths().size(); i++) {
            String extraSpSloPath = appSettings.getExtraSpSloPaths().get(i);
            if (!Strings.isNullOrEmpty(extraSpSloPath)) {
                if (requestURI.indexOf(extraSpSloPath) > -1) {
                    handleByExtraSp = true;
                    String entityId = appSettings.getExtraSpEntityIds().get(i);
                    context.setLocalEntityId(entityId);
                    context.setLocalEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    break;
                }
            }
        }

        if (!handleByExtraSp) {
            super.populateLocalEntityId(context, requestURI);
        }
    }
}
