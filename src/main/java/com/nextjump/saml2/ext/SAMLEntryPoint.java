package com.nextjump.saml2.ext;

import com.nextjump.saml2.AppSettings;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.util.URLBuilder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.context.SAMLMessageContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Customize SAMLEntryPoint to redirect to the correct discovery URL.
 */
public class SAMLEntryPoint extends org.springframework.security.saml.SAMLEntryPoint {
    private static final Logger LOG = LoggerFactory
            .getLogger(SAMLEntryPoint.class);

    private AppSettings appSettings;

    public void setAppSettings(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/mgr/") || requestUri.startsWith("/swagger-ui.html")
                || requestUri.startsWith("/swagger-ui/")) {
            response.addHeader("WWW-Authenticate", "Basic realm=\"nextjump\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        } else {
            try {
                SAMLMessageContext context = this.contextProvider.getLocalAndPeerEntity(request, response);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("### SAMLEntryPoint requestUri:" + requestUri);
                    LOG.debug("### SAMLEntryPoint getRequestURL:" + request.getRequestURL());
                    LOG.debug("### SAMLEntryPoint getServerName:" + request.getServerName());
                    LOG.debug("### commence getLocalEntityId: " + context.getLocalEntityId());
                    LOG.debug("### commence getPeerEntityId: " + context.getPeerEntityId());
                }

                if (this.isECP(context)) {
                    this.initializeECP(context, e);
                } else if (this.isDiscovery(context)) {
                    this.initializeDiscovery(context);
                } else {
                    this.initializeSSO(context, e);
                }

            } catch (SAMLException var5) {
                LOG.error("Error initializing entry point", var5);
                throw new ServletException(var5);
            } catch (MetadataProviderException var6) {
                LOG.error("Error initializing entry point", var6);
                throw new ServletException(var6);
            } catch (MessageEncodingException var7) {
                LOG.error("Error initializing entry point", var7);
                throw new ServletException(var7);
            }
        }

    }

    @Override
    protected void initializeDiscovery(SAMLMessageContext context) throws ServletException, IOException,
            MetadataProviderException {
        String discoveryURL = context.getLocalExtendedMetadata().getIdpDiscoveryURL();
        if (LOG.isDebugEnabled()) {
            LOG.debug("### discoveryURL: " + discoveryURL);
            LOG.debug("### getLocalEntityId: " + context.getLocalEntityId());
            LOG.debug("### getPeerEntityId: " + context.getPeerEntityId());
        }

        try {
            if (discoveryURL != null) {
                URLBuilder urlBuilder = new URLBuilder(discoveryURL);
                List<Pair<String, String>> queryParams = urlBuilder.getQueryParams();
                queryParams.add(new Pair<String, String>(SAMLDiscovery.ENTITY_ID_PARAM, context.getLocalEntityId()));
                queryParams.add(new Pair<String, String>(SAMLDiscovery.RETURN_ID_PARAM, IDP_PARAMETER));
                discoveryURL = urlBuilder.buildURL();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using discovery URL from extended metadata");
                }
            } else {

                String discoveryUrl = SAMLDiscovery.FILTER_URL;
                if (samlDiscovery != null) {
                    discoveryUrl = samlDiscovery.getFilterProcessesUrl();
                }

                String contextPath =
                        (String) context.getInboundMessageTransport().getAttribute(SAMLConstants.LOCAL_CONTEXT_PATH);
                discoveryURL =
                        contextPath + discoveryUrl + "?" + SAMLDiscovery.RETURN_ID_PARAM + "=" + IDP_PARAMETER + "&" + SAMLDiscovery.ENTITY_ID_PARAM + "=" + context.getLocalEntityId();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using local discovery URL");
                }
            }
        } catch (Exception ex) {
            LOG.error("", ex);
        }

        InTransport inTransport = context.getInboundMessageTransport();
        HttpServletRequest httpRequest = ((HttpServletRequestAdapter) inTransport).getWrappedRequest();
        if (LOG.isDebugEnabled()) {
            LOG.debug("httpRequest serverName:" + httpRequest.getServerName());
        }
        discoveryURL = URLDecoder.decode(discoveryURL, "utf-8");
        discoveryURL = appSettings.getUrlFromLb(httpRequest, discoveryURL);//get the correct discovery url.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Redirecting to discovery URL {}", discoveryURL);
        }
        HTTPOutTransport response = (HTTPOutTransport) context.getOutboundMessageTransport();
        response.sendRedirect(discoveryURL);
    }
}
