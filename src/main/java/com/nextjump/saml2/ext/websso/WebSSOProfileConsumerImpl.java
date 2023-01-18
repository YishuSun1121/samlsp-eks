package com.nextjump.saml2.ext.websso;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.metadata.Endpoint;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.context.SAMLMessageContext;

public class WebSSOProfileConsumerImpl extends org.springframework.security.saml.websso.WebSSOProfileConsumerImpl {
    private final static Log LOG = LogFactory.getLog(WebSSOProfileConsumerImpl.class);

    private AppSettings appSettings;

    @Override
    protected void verifyAuthenticationStatement(AuthnStatement auth, RequestedAuthnContext requestedAuthnContext,
                                                 SAMLMessageContext context) throws AuthenticationException {

        // Validate that user wasn't authenticated too long time ago
//        if (!isDateTimeSkewValid(getResponseSkew(), getMaxAuthenticationAge(), auth.getAuthnInstant())) {
//            throw new CredentialsExpiredException("Authentication statement is too old to be used with value " +
//            auth.getAuthnInstant());
//        }

        // Validate users session is still valid
        if (auth.getSessionNotOnOrAfter() != null && auth.getSessionNotOnOrAfter().isBeforeNow()) {
            throw new CredentialsExpiredException("Authentication session is not valid on or after " + auth.getSessionNotOnOrAfter());
        }

        // Verify context
        verifyAuthnContext(requestedAuthnContext, auth.getAuthnContext(), context);
    }

    @Override
    protected void verifyEndpoint(Endpoint endpoint, String destination) throws SAMLException {
        // Verify that destination in the response matches one of the available endpoints
        if (destination != null) {
            if (uriComparator.compare(destination, endpoint.getLocation())) {
                // Expected
            } else if (uriComparator.compare(destination, endpoint.getResponseLocation())) {
                // Expected
            } else {
                // try resolve entityId from destination
                String entityId = appSettings.resolveEntityIdFromSSOUrl(destination);
                boolean hasError = false;
                if (null != entityId) {
                    int index = appSettings.getExtraSpEntityIds().indexOf(entityId);
                    String baseEntityUrl = appSettings.getExtraSpEntityBaseUrls().get(index);
                    String newHostname = baseEntityUrl.split("/")[2];
                    if (!Strings.isNullOrEmpty(baseEntityUrl)) {
                        String oldHostname = destination.split("/")[2];
                        String fixedDestination = destination.replace(oldHostname, newHostname);
                        LOG.info(String.format("fixedDestination=%s", fixedDestination));
                        if (uriComparator.compare(fixedDestination, endpoint.getLocation())) {
                            // Expected
                        } else {
                            hasError = true;
                        }
                    }
                } else {
                    hasError = true;
                }
                if (hasError) {
                    throw new SAMLException("Intended destination " + destination + " doesn't match any of the " +
                            "endpoint " +
                            "URLs on endpoint " + endpoint.getLocation() + " for profile " + getProfileIdentifier());
                }
            }
        }
    }

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }
}
