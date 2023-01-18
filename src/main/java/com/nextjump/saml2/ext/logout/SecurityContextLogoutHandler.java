package com.nextjump.saml2.ext.logout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Customize SecurityContextLogoutHandler to get the sp alias and put it into the http request.
 */
public class SecurityContextLogoutHandler implements LogoutHandler {
    private final static Log LOG = LogFactory.getLog(SecurityContextLogoutHandler.class);
    private boolean invalidateHttpSession = true;
    private boolean clearAuthentication = true;

    @Autowired
    private MetadataManager metadata;

    @Autowired
    private SAMLContextProvider contextProvider;

    public SecurityContextLogoutHandler() {
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Assert.notNull(request, "HttpServletRequest required");

        try {
            SAMLMessageContext context = contextProvider.getLocalAndPeerEntity(request, response);
            String localEntityId = context.getLocalEntityId();
            LOG.debug("### logout localEntityId: " + localEntityId);
            String alias = metadata.getExtendedMetadata(localEntityId).getAlias();// get sp alias from metadata and
            // put it into http request.
            LOG.debug("### logout alias: " + alias);
            request.setAttribute("alias", alias);
        } catch (Exception ex) {
            LOG.error("", ex);
        }

        if (this.invalidateHttpSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                if (this.LOG.isDebugEnabled()) {
                    this.LOG.debug(LogMessage.format("Invalidated session %s", session.getId()));
                }
            }
        }

        SecurityContext context = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
        if (this.clearAuthentication) {
            context.setAuthentication(null);
        }

    }

    public boolean isInvalidateHttpSession() {
        return this.invalidateHttpSession;
    }

    public void setInvalidateHttpSession(boolean invalidateHttpSession) {
        this.invalidateHttpSession = invalidateHttpSession;
    }

    public void setClearAuthentication(boolean clearAuthentication) {
        this.clearAuthentication = clearAuthentication;
    }
}

