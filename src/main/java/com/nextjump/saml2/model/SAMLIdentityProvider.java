package com.nextjump.saml2.model;

import com.nextjump.saml2.enums.MetadataSource;
import com.nextjump.saml2.enums.SSOBinding;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "saml_identity_provider")
public class SAMLIdentityProvider extends AbstractModel {
    @Column(unique = true)
    private String alias;
    @Enumerated(value = EnumType.STRING)
    private MetadataSource metadataSource;
    private String metadataUrl;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String metadataXmlContent;
    private String orgDirVal;
    private boolean debugging = false;
    private String entityId;
    @Column(length = 4096)
    private String signX509Certificate;
    @Column(length = 2048)
    private String encryptionX509Certificate;
    private SSOBinding ssoBinding;
    @Column(length = 1024)
    private String ssoUrl;

    @JoinTable(name = "idp_sp", joinColumns = @JoinColumn(name = "idp_id"))
    @Column(name = "sp_alias")
    @ElementCollection(targetClass = String.class)
    private Set<String> spAliases;

    @Column(length = 4096)
    private String oldSignX509Certificate;
    @Column(length = 2048)
    private String oldEncryptionX509Certificate;

    private Long oldValidBefore; // old certificate valid before this time; always valid if it is null

    public String getSignX509Certificate() {
        return signX509Certificate;
    }

    public void setSignX509Certificate(String signX509Certificate) {
        this.signX509Certificate = signX509Certificate;
    }

    public String getEncryptionX509Certificate() {
        return encryptionX509Certificate;
    }

    public void setEncryptionX509Certificate(String encryptionX509Certificate) {
        this.encryptionX509Certificate = encryptionX509Certificate;
    }

    public SSOBinding getSsoBinding() {
        return ssoBinding;
    }

    public void setSsoBinding(SSOBinding ssoBinding) {
        this.ssoBinding = ssoBinding;
    }

    public String getSsoUrl() {
        return ssoUrl;
    }

    public void setSsoUrl(String ssoUrl) {
        this.ssoUrl = ssoUrl;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOrgDirVal() {
        return orgDirVal;
    }

    public void setOrgDirVal(String orgDirVal) {
        this.orgDirVal = orgDirVal;
    }

    public boolean isDebugging() {
        return debugging;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public MetadataSource getMetadataSource() {
        return metadataSource;
    }

    public void setMetadataSource(MetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }

    public String getMetadataXmlContent() {
        return metadataXmlContent;
    }

    public void setMetadataXmlContent(String metadataXmlContent) {
        this.metadataXmlContent = metadataXmlContent;
    }

    public Set<String> getSpAliases() {
        return spAliases;
    }

    public void setSpAliases(Set<String> spAliases) {
        this.spAliases = spAliases;
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
