package com.nextjump.saml2.ext.authentication;

import com.nextjump.saml2.AppSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Customize AuthenticationFailureHandler to redirect to the correct AuthenticationFailure URL.
 */
public class SimpleUrlAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final Log LOG = LogFactory.getLog(SimpleUrlAuthenticationFailureHandler.class);

    private String defaultFailureUrl;
    private boolean forwardToDestination = false;
    private boolean allowSessionCreation = true;
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public SimpleUrlAuthenticationFailureHandler() {
    }

    private AppSettings appSettings;

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public SimpleUrlAuthenticationFailureHandler(String defaultFailureUrl) {
        this.setDefaultFailureUrl(defaultFailureUrl);
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        LOG.error("onAuthenticationFailure", exception);
        if (this.defaultFailureUrl == null) {
            if (this.LOG.isTraceEnabled()) {
                this.LOG.trace("Sending 401 Unauthorized error since no failure URL is set");
            } else {
                this.LOG.debug("Sending 401 Unauthorized error");
            }

            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        } else {
            this.saveException(request, exception);
            //get the correct failure url.
            String newDefaultFailureUrl = appSettings.getUrlFromLb(request, defaultFailureUrl);
            LOG.debug("failure url:" + newDefaultFailureUrl);
            if (this.forwardToDestination) {
                this.LOG.debug("Forwarding to " + newDefaultFailureUrl);
                request.getRequestDispatcher(newDefaultFailureUrl).forward(request, response);
            } else {
                this.redirectStrategy.sendRedirect(request, response, newDefaultFailureUrl);
            }

        }
    }

    protected final void saveException(HttpServletRequest request, AuthenticationException exception) {
        if (this.forwardToDestination) {
            request.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null || this.allowSessionCreation) {
                request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
            }

        }
    }

    public void setDefaultFailureUrl(String defaultFailureUrl) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), () -> {
            return "'" + defaultFailureUrl + "' is not a valid redirect URL";
        });
        this.defaultFailureUrl = defaultFailureUrl;
    }

    protected boolean isUseForward() {
        return this.forwardToDestination;
    }

    public void setUseForward(boolean forwardToDestination) {
        this.forwardToDestination = forwardToDestination;
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    protected RedirectStrategy getRedirectStrategy() {
        return this.redirectStrategy;
    }

    protected boolean isAllowSessionCreation() {
        return this.allowSessionCreation;
    }

    public void setAllowSessionCreation(boolean allowSessionCreation) {
        this.allowSessionCreation = allowSessionCreation;
    }
}

