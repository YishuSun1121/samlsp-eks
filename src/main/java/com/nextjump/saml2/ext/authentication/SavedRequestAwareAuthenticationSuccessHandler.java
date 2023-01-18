package com.nextjump.saml2.ext.authentication;

import com.nextjump.saml2.AppSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Customize AuthenticationSuccessHandler to redirect to the correct AuthenticationSuccess URL.
 */
public class SavedRequestAwareAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final static Log LOG = LogFactory.getLog(SavedRequestAwareAuthenticationSuccessHandler.class);
    private RequestCache requestCache = new HttpSessionRequestCache();

    public SavedRequestAwareAuthenticationSuccessHandler() {
    }

    private AppSettings appSettings;

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);
        targetUrl = appSettings.getUrlFromLb(request, targetUrl);//get the correct success url.
        // using global lb server name for now
        // TODO delete this after take over the php service
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetUrl);
        targetUrl = builder.host(appSettings.getLbServerName()).toUriString();

        LOG.debug("target url:" + targetUrl);
        return targetUrl;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        SavedRequest savedRequest = this.requestCache.getRequest(request, response);
        if (savedRequest == null) {
            super.onAuthenticationSuccess(request, response, authentication);
        } else {
            String targetUrlParameter = this.getTargetUrlParameter();
            if (!this.isAlwaysUseDefaultTargetUrl() && (targetUrlParameter == null || !StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
                this.clearAuthenticationAttributes(request);
                String targetUrl = savedRequest.getRedirectUrl();
                this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } else {
                this.requestCache.removeRequest(request, response);
                super.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
