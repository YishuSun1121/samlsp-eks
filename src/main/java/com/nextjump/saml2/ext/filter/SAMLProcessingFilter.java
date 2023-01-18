package com.nextjump.saml2.ext.filter;


public class SAMLProcessingFilter extends org.springframework.security.saml.SAMLProcessingFilter {

    public SAMLProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }
}
