package com.nextjump.saml2.service.request;

import com.nextjump.saml2.enums.MetadataSource;
import com.nextjump.saml2.enums.SSOBinding;

import java.util.Set;

public class DefaultSAMLIdentityProviderCreateRequest implements SAMLIdentityProviderCreateRequest {
    private String alias;
    private MetadataSource metadataSource;
    private String metadataUrl;
    private String metadataXmlContent;
    private String orgDirVal;
    private boolean debugging = false;
    private String entityId;
    private String signX509Certificate;
    private String encryptionX509Certificate;
    private SSOBinding ssoBinding;
    private String ssoUrl;
    private Set<String> spAliases;
    private String oldSignX509Certificate;
    private String oldEncryptionX509Certificate;
    private Long oldValidBefore;

    @Override
    public String getOrgDirVal() {
        return orgDirVal;
    }

    public void setOrgDirVal(String orgDirVal) {
        this.orgDirVal = orgDirVal;
    }

    @Override
    public boolean isDebugging() {
        return debugging;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public MetadataSource getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(MetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }

    @Override
    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    @Override
    public String getMetadataXmlContent() {
        return metadataXmlContent;
    }

    public void setMetadataXmlContent(String metadataXmlContent) {
        this.metadataXmlContent = metadataXmlContent;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getSignX509Certificate() {
        return signX509Certificate;
    }

    public void setSignX509Certificate(String signX509Certificate) {
        this.signX509Certificate = signX509Certificate;
    }

    @Override
    public String getEncryptionX509Certificate() {
        return encryptionX509Certificate;
    }

    public void setEncryptionX509Certificate(String encryptionX509Certificate) {
        this.encryptionX509Certificate = encryptionX509Certificate;
    }

    @Override
    public SSOBinding getSsoBinding() {
        return ssoBinding;
    }

    public void setSsoBinding(SSOBinding ssoBinding) {
        this.ssoBinding = ssoBinding;
    }

    @Override
    public String getSsoUrl() {
        return ssoUrl;
    }

    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    @Override
    public Set<String> getSpAliases() {
        return spAliases;
    }

    public void setSpAliases(Set<String> spAliases) {
        this.spAliases = spAliases;
    }

    @Override
    public String getOldSignX509Certificate() {
        return oldSignX509Certificate;
    }

    public void setOldSignX509Certificate(String oldSignX509Certificate) {
        this.oldSignX509Certificate = oldSignX509Certificate;
    }

    @Override
    public String getOldEncryptionX509Certificate() {
        return oldEncryptionX509Certificate;
    }

    public void setOldEncryptionX509Certificate(String oldEncryptionX509Certificate) {
        this.oldEncryptionX509Certificate = oldEncryptionX509Certificate;
    }

    @Override
    public Long getOldValidBefore() {
        return oldValidBefore;
    }

    public void setOldValidBefore(Long oldValidBefore) {
        this.oldValidBefore = oldValidBefore;
    }
}
