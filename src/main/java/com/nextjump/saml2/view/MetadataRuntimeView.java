package com.nextjump.saml2.view;

import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;

public class MetadataRuntimeView {
    public final static String TYPE_SP = "ServiceProvider";
    public final static String TYPE_IDP = "IdentityProvider";


    public final static void covert(String entityId,
                                    CachingMetadataManager metadataManager, MetadataRuntimeView view) throws Exception {
        view.setEntityId(entityId);
        ExtendedMetadata defaultMetadata = metadataManager.getExtendedMetadata(entityId);
        view.setLocal(defaultMetadata.isLocal());
        view.setAlias(defaultMetadata.getAlias());
    }

    public static MetadataRuntimeView from(String entityId,
                                           CachingMetadataManager metadataManager) throws
            Exception {
        MetadataRuntimeView view = new MetadataRuntimeView();
        covert(entityId, metadataManager, view);

        return view;
    }

    private boolean local = false;
    private String type; //SP or IDP
    private String alias;
    private String entityId;

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
