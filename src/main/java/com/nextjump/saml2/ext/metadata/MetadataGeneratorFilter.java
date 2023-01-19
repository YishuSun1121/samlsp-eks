package com.nextjump.saml2.ext.metadata;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.metadata.MetadataMemoryProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class MetadataGeneratorFilter extends org.springframework.security.saml.metadata.MetadataGeneratorFilter {
    private boolean generated = false;

    private final AppSettings appSettings;

    /**
     * Default constructor.
     *
     * @param generator generator
     */
    public MetadataGeneratorFilter(MetadataGenerator generator, AppSettings appSettings) {
        super(generator);
        this.appSettings = appSettings;
    }

    @Override
    protected void processMetadataInitialization(HttpServletRequest request) throws ServletException {

        // In case the hosted SP metadata weren't initialized, let's do it now
        if (!generated) {
            synchronized (MetadataManager.class) {
                if (!generated) {
                    try {

                        for (int i = 0; i < appSettings.getExtraSpEntityIds().size(); i++) {
                            String entityId = appSettings.getExtraSpEntityIds().get(i);
                            if (Strings.isNullOrEmpty(entityId)) {
                                continue;
                            }
                            generator.setEntityId(entityId);
                            String alias = appSettings.getExtraSpAliases().get(i);
                            generator.getExtendedMetadata().setAlias(alias);
                            String entityBaseURL = appSettings.getExtraSpEntityBaseUrls().get(i);
                            generator.setEntityBaseURL(entityBaseURL);
                            String signKey = appSettings.getExtraSpKeystoreKeys().get(i);
                            generator.getExtendedMetadata().setSigningKey(signKey);


                            log.info("No default metadata configured, generating with default values, please " +
                                    "pre-configure metadata for production use");
                            // Defaults
//                            String alias = generator.getExtendedMetadata().getAlias();
                            String baseURL = getDefaultBaseURL(request);

                            // Use default baseURL if not set
                            if (generator.getEntityBaseURL() == null || generator.getEntityBaseURL().isEmpty()) {
                                log.warn("Generated default entity base URL {} based on values in the first server " +
                                        "request. Please set property entityBaseURL on MetadataGenerator bean to " +
                                        "fixate " +
                                        "the value.", baseURL);
                                generator.setEntityBaseURL(baseURL);
                            } else {
                                baseURL = generator.getEntityBaseURL();
                            }

                            // Use default entityID if not set
                            if (generator.getEntityId() == null) {
                                generator.setEntityId(getDefaultEntityID(baseURL, alias));
                            }

                            EntityDescriptor descriptor = generator.generateMetadata();
                            ExtendedMetadata extendedMetadata = generator.generateExtendedMetadata();

                            log.info("Created service provider metadata for system with entityID: " + descriptor.getEntityID());
                            MetadataMemoryProvider memoryProvider = new MetadataMemoryProvider(descriptor);
                            memoryProvider.initialize();
                            MetadataProvider metadataProvider = new ExtendedMetadataDelegate(memoryProvider,
                                    extendedMetadata);

                            manager.addMetadataProvider(metadataProvider);
                            generated = true;
                            manager.refreshMetadata();
                        }


                    } catch (MetadataProviderException e) {
                        log.error("Error generating system metadata", e);
                        throw new ServletException("Error generating system metadata", e);
                    }
                }
            }
        }

    }
}
