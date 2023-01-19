package com.nextjump.saml2.ext.metadata;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.util.URLBuilder;
import org.opensaml.xml.util.Pair;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;

import java.util.Map;

public class MetadataGenerator extends org.springframework.security.saml.metadata.MetadataGenerator {
    private final AppSettings appSettings;

    public MetadataGenerator(AppSettings appSettings) {
        super();
        this.appSettings = appSettings;
    }

    @Override
    protected AssertionConsumerService getAssertionConsumerService(String entityBaseURL, String entityAlias,
                                                                   boolean isDefault, int index, String filterURL,
                                                                   String binding) {

        int aliasIndex = appSettings.getExtraSpAliases().indexOf(entityAlias);
        if (aliasIndex > -1 && !Strings.isNullOrEmpty(appSettings.getExtraSpSsoPaths().get(aliasIndex))) {
            String ssoPath = appSettings.getExtraSpSsoPaths().get(aliasIndex);
            SAMLObjectBuilder<AssertionConsumerService> builder =
                    (SAMLObjectBuilder<AssertionConsumerService>) builderFactory.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
            AssertionConsumerService consumer = builder.buildObject();
            consumer.setLocation(getServerURL(entityBaseURL, ssoPath, null));
            consumer.setBinding(binding);
            if (isDefault) {
                consumer.setIsDefault(true);
            }
            consumer.setIndex(index);
            return consumer;

        } else if (entityAlias.equals(appSettings.getSpAlias())) {
            // default sp
            SAMLObjectBuilder<AssertionConsumerService> builder =
                    (SAMLObjectBuilder<AssertionConsumerService>) builderFactory.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
            AssertionConsumerService consumer = builder.buildObject();
            consumer.setLocation(getServerURL(entityBaseURL, filterURL, null));
            consumer.setBinding(binding);
            if (isDefault) {
                consumer.setIsDefault(true);
            }
            consumer.setIndex(index);
            return consumer;
        } else {
            return super.getAssertionConsumerService(entityBaseURL, entityAlias, isDefault, index, filterURL, binding);
        }
    }

    @Override
    protected SingleLogoutService getSingleLogoutService(String entityBaseURL, String entityAlias, String binding) {

        int aliasIndex = appSettings.getExtraSpAliases().indexOf(entityAlias);
        if (aliasIndex > -1 && !Strings.isNullOrEmpty(appSettings.getExtraSpSloPaths().get(aliasIndex))) {
            String sloPath = appSettings.getExtraSpSloPaths().get(aliasIndex);
            SAMLObjectBuilder<SingleLogoutService> builder =
                    (SAMLObjectBuilder<SingleLogoutService>) builderFactory.getBuilder(SingleLogoutService.DEFAULT_ELEMENT_NAME);
            SingleLogoutService logoutService = builder.buildObject();
            logoutService.setLocation(getServerURL(entityBaseURL, sloPath, null));
            logoutService.setBinding(binding);
            return logoutService;
        } else if (entityAlias.equals(appSettings.getSpAlias())) {
            // default sp
            SAMLObjectBuilder<SingleLogoutService> builder =
                    (SAMLObjectBuilder<SingleLogoutService>) builderFactory.getBuilder(SingleLogoutService.DEFAULT_ELEMENT_NAME);
            SingleLogoutService logoutService = builder.buildObject();
            logoutService.setLocation(getServerURL(entityBaseURL, getSAMLLogoutFilterPath(), null));
            logoutService.setBinding(binding);
            return logoutService;
        } else {
            return super.getSingleLogoutService(entityBaseURL, entityAlias, binding);
        }

    }

    private String getSAMLLogoutFilterPath() {
        if (samlLogoutProcessingFilter != null) {
            return samlLogoutProcessingFilter.getFilterProcessesUrl();
        } else {
            return SAMLLogoutProcessingFilter.FILTER_URL;
        }
    }

    private String getServerURL(String entityBaseURL, String processingURL,
                                Map<String, String> parameters) {

        StringBuilder result = new StringBuilder();
        result.append(entityBaseURL);
        if (!processingURL.startsWith("/")) {
            result.append("/");
        }
        result.append(processingURL);
        String resultString = result.toString();
        if (parameters == null || parameters.size() == 0) {
            return resultString;
        } else {
            // Add parameters
            URLBuilder returnUrlBuilder = new URLBuilder(resultString);
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                returnUrlBuilder.getQueryParams().add(new Pair<String, String>(entry.getKey(), entry.getValue()));
            }
            return returnUrlBuilder.buildURL();
        }

    }
}
