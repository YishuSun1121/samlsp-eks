package com.nextjump.saml2.ext.logout;

import com.nextjump.saml2.AppSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Customize LogoutSuccessHandler to get the sp alias from http request(added in SecurityContextLogoutHandler)
 * and redirect to the correct logout success url.
 */
public class SimpleUrlLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
    private final static Log LOG = LogFactory.getLog(SimpleUrlLogoutSuccessHandler.class);

    public SimpleUrlLogoutSuccessHandler() {
    }

    private AppSettings appSettings;

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        handle(request, response, authentication);
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = this.determineTargetUrl(request, response, authentication);
        if (response.isCommitted()) {
            this.LOG.debug(LogMessage.format("Did not redirect to %s since response already committed.", targetUrl));
        } else {
            Object alias = request.getAttribute("alias");//get saml SP alias from http request.
            if (alias != null) {
                targetUrl =
                        appSettings.getLbSchema() + "://" + appSettings.getLbServerName() + "/saml/login/alias/" + alias;
                targetUrl = appSettings.getUrlFromLb(request, targetUrl);
            }

            LOG.debug("### 3 LogoutSuccessHandler sendRedirect url:" + targetUrl);
            this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        LOG.debug("### 1 LogoutSuccessHandler url:" + targetUrl);
        targetUrl = appSettings.getUrlFromLb(request, targetUrl);
        LOG.debug("### 2 LogoutSuccessHandler url:" + targetUrl);
        return targetUrl;
    }
}
