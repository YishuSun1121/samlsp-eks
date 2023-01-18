package com.nextjump.saml2.view;

import com.nextjump.saml2.enums.MetadataSource;
import com.nextjump.saml2.enums.SSOBinding;
import com.nextjump.saml2.model.SAMLIdentityProvider;
import org.springframework.beans.BeanUtils;

public class SAMLIdentityProviderView extends BaseView {
    private String alias;
    private MetadataSource metadataSource;
    private String metadataUrl;
    private String orgDirVal;
    private boolean debugging = false;
    private String entityId;
    private SSOBinding ssoBinding;
    private String ssoUrl;


    public static void convert(SAMLIdentityProvider model, SAMLIdentityProviderView view) {
        BeanUtils.copyProperties(model, view);
        if (MetadataSource.XML.equals(model.getMetadataSource())
                || MetadataSource.ATTRIBUTE.equals(model.getMetadataSource())) {
            view.setMetadataUrl(null);
        }
    }

    public static SAMLIdentityProviderView from(SAMLIdentityProvider model) {
        if (null == model) {
            return null;
        }
        SAMLIdentityProviderView view = new SAMLIdentityProviderView();
        convert(model, view);
        return view;
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

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
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
}
