package com.nextjump.saml2.view;

import com.nextjump.saml2.model.SAMLIdentityProvider;

import java.util.Set;

public class SAMLIdentityProviderDetail extends SAMLIdentityProviderView {
    public final static SAMLIdentityProviderDetail from(SAMLIdentityProvider model) {
        if (null == model) {
            return null;
        }
        SAMLIdentityProviderDetail view = new SAMLIdentityProviderDetail();
        SAMLIdentityProviderView.convert(model, view);
        return view;
    }

    private String metadataXmlContent;
    private Set<String> spAliases;
    private String oldSignX509Certificate;
    private String oldEncryptionX509Certificate;
    private Long oldValidBefore;

    public Set<String> getSpAliases() {
        return spAliases;
    }

    public void setSpAliases(Set<String> spAliases) {
        this.spAliases = spAliases;
    }

    public String getMetadataXmlContent() {
        return metadataXmlContent;
    }

    public void setMetadataXmlContent(String metadataXmlContent) {
        this.metadataXmlContent = metadataXmlContent;
    }

    public String getOldSignX509Certificate() {
        return oldSignX509Certificate;
    }

    public void setOldSignX509Certificate(String oldSignX509Certificate) {
        this.oldSignX509Certificate = oldSignX509Certificate;
    }

    public String getOldEncryptionX509Certificate() {
        return oldEncryptionX509Certificate;
    }

    public void setOldEncryptionX509Certificate(String oldEncryptionX509Certificate) {
        this.oldEncryptionX509Certificate = oldEncryptionX509Certificate;
    }

    public Long getOldValidBefore() {
        return oldValidBefore;
    }

    public void setOldValidBefore(Long oldValidBefore) {
        this.oldValidBefore = oldValidBefore;
    }
}
