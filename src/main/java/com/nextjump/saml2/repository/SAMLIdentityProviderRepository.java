package com.nextjump.saml2.repository;

import com.nextjump.saml2.model.SAMLIdentityProvider;

public interface SAMLIdentityProviderRepository extends AbstractModelRepository<SAMLIdentityProvider> {
    SAMLIdentityProvider findByAlias(String alias);

    SAMLIdentityProvider findByEntityId(String entityId);
}
