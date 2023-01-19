package com.nextjump.saml2.ext.metadata;

import com.nextjump.saml2.model.SAMLIdentityProvider;
import com.nextjump.saml2.repository.SAMLIdentityProviderRepository;
import org.opensaml.saml2.metadata.provider.AbstractReloadingMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;

import java.nio.charset.StandardCharsets;
import java.util.Timer;

public class ModelMetadataProvider extends AbstractReloadingMetadataProvider {
    private final String id;
    private final SAMLIdentityProviderRepository samlIdentityProviderRepository;

    public ModelMetadataProvider(Timer backgroundTaskTimer, String id,
                                 SAMLIdentityProviderRepository samlIdentityProviderRepository) {
        super(backgroundTaskTimer);
        this.id = id;
        this.samlIdentityProviderRepository = samlIdentityProviderRepository;
    }

    @Override
    protected String getMetadataIdentifier() {
        return id;
    }

    @Override
    protected byte[] fetchMetadata() throws MetadataProviderException {
        SAMLIdentityProvider model = samlIdentityProviderRepository.findById(this.id).orElse(null);
        if (null == model) {
            return null;
        } else {
            return model.getMetadataXmlContent().getBytes(StandardCharsets.UTF_8);
        }
    }
}
