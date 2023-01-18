package com.nextjump.saml2.service;

import com.nextjump.saml2.service.request.SAMLIdentityProviderCreateRequest;
import com.nextjump.saml2.service.request.SAMLIdentityProviderQueryRequest;
import com.nextjump.saml2.view.SAMLIdentityProviderDetail;
import com.nextjump.saml2.view.SAMLIdentityProviderView;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.data.domain.Page;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;

public interface SAMLIdentityProviderService {
    Page<SAMLIdentityProviderView> queryPage(SAMLIdentityProviderQueryRequest param);

    SAMLIdentityProviderDetail create(SAMLIdentityProviderCreateRequest param);

    ExtendedMetadataDelegate generateMetadata(SAMLIdentityProviderDetail samlIdentityProvider) throws MetadataProviderException;

    void deleteById(String id);

    SAMLIdentityProviderView queryOneByAlias(String alias);

    SAMLIdentityProviderDetail update(String id, SAMLIdentityProviderCreateRequest param);

    SAMLIdentityProviderDetail getOne(String id);
}
