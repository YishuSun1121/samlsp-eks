package com.nextjump.saml2.ext.context;

import com.nextjump.saml2.AppSettings;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Customize SAMLContextProvider to get to the correct LB URL.
 */
public class SAMLContextProviderLB extends SAMLContextProviderImpl {
    private String scheme;
    private String serverName;
    private boolean includeServerPortInRequestURL;
    private int serverPort;
    private String contextPath;

    public SAMLContextProviderLB(AppSettings appSettings) {
        super(appSettings);
    }

    protected void populateGenericContext(HttpServletRequest request, HttpServletResponse response,
                                          SAMLMessageContext context) throws MetadataProviderException {
        super.populateGenericContext(new LPRequestWrapper(request), response, context);
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setIncludeServerPortInRequestURL(boolean includeServerPortInRequestURL) {
        this.includeServerPortInRequestURL = includeServerPortInRequestURL;
    }

    public void setContextPath(String contextPath) {
        if (contextPath == null || "/".equals(contextPath)) {
            contextPath = "";
        }

        this.contextPath = contextPath;
    }

    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        Assert.hasText(this.scheme, "Scheme must be set");
        Assert.hasText(this.serverName, "Server name must be set");
        Assert.notNull(this.contextPath, "Context path must be set");
        if (StringUtils.hasLength(this.contextPath)) {
            Assert.isTrue(this.contextPath.startsWith("/"), "Context path must be set and start with a forward slash");
        }

    }

    private class LPRequestWrapper extends HttpServletRequestWrapper {
        HttpServletRequest request;

        private LPRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        public String getContextPath() {
            return SAMLContextProviderLB.this.contextPath;
        }

        public String getScheme() {
            return SAMLContextProviderLB.this.scheme;
        }

        public String getServerName() {
            return request.getServerName();
        }

        public int getServerPort() {
            return SAMLContextProviderLB.this.serverPort;
        }

        public String getRequestURI() {
            StringBuilder sb = new StringBuilder(SAMLContextProviderLB.this.contextPath);
            sb.append(this.getServletPath());
            return sb.toString();
        }

        public StringBuffer getRequestURL() {
            StringBuffer sb = new StringBuffer();
            //sb.append(SAMLContextProviderLB.this.scheme).append("://").append(SAMLContextProviderLB.this.serverName);
//            sb.append(SAMLContextProviderLB.this.scheme).append("://").append(this.getServerName());
            //get the correct server name
            String newServerName = SAMLContextProviderLB.this.appSettings.getServerNameFromLb(request);
            sb.append(SAMLContextProviderLB.this.scheme).append("://").append(newServerName);

            if (SAMLContextProviderLB.this.includeServerPortInRequestURL) {
                sb.append(":").append(SAMLContextProviderLB.this.serverPort);
            }

            sb.append(SAMLContextProviderLB.this.contextPath);
            sb.append(this.getServletPath());
            if (this.getPathInfo() != null) {
                sb.append(this.getPathInfo());
            }

            return sb;
        }

        public boolean isSecure() {
            return "https".equalsIgnoreCase(SAMLContextProviderLB.this.scheme);
        }
    }
}
