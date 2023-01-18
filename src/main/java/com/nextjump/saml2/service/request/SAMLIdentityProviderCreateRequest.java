package com.nextjump.saml2.service.request;

import com.nextjump.saml2.enums.MetadataSource;
import com.nextjump.saml2.enums.SSOBinding;

import java.util.Set;

public interface SAMLIdentityProviderCreateRequest {
    String getOrgDirVal();

    boolean isDebugging();

    String getAlias();

    MetadataSource getMetadataSource();

    String getMetadataUrl();

    String getMetadataXmlContent();

    String getEntityId();

    String getSignX509Certificate();

    String getEncryptionX509Certificate();

    SSOBinding getSsoBinding();

    String getSsoUrl();

    Set<String> getSpAliases();

    String getOldSignX509Certificate();

    String getOldEncryptionX509Certificate();

    Long getOldValidBefore();
}
