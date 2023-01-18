package com.nextjump.saml2.view;

import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.util.SAMLUtil;

import java.util.List;

public class MetadataRuntimeDetail extends MetadataRuntimeView {

    public final static MetadataRuntimeDetail from(String entityId,
                                                   CachingMetadataManager metadataManager) throws Exception {
        MetadataRuntimeDetail view = new MetadataRuntimeDetail();
        MetadataRuntimeView.covert(entityId, metadataManager, view);

        MetadataProvider provider = null;
        List<MetadataProvider> providers = metadataManager.getProviders();
        for (MetadataProvider item : providers) {
            if (item.getEntityDescriptor(entityId) != null) {
                provider = item;
                break;
            }
        }

        if (null != provider) {
            view.setXml(XMLHelper.nodeToString(SAMLUtil.marshallMessage(provider.getMetadata())));
        }

        return view;
    }

    private String xml;

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
