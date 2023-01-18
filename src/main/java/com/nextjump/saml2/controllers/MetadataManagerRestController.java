package com.nextjump.saml2.controllers;

import com.nextjump.saml2.view.MetadataRuntimeDetail;
import com.nextjump.saml2.view.MetadataRuntimeView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Tag(name = "Metadata Management(runtime)")
@RestController
public class MetadataManagerRestController {
    @Autowired
    private CachingMetadataManager metadataManager;

    @Operation(summary = "Query all available metadata providers")
    @GetMapping("/mgr/metadata")
    public List<MetadataRuntimeView> queryList() throws Exception {
        Set<String> entityIds = metadataManager.getIDPEntityNames();
        List<MetadataRuntimeView> list = new ArrayList<>();
        for (String entityId : entityIds) {
            MetadataRuntimeView view = MetadataRuntimeView.from(entityId,
                    metadataManager);
            view.setType(MetadataRuntimeView.TYPE_IDP);
            list.add(view);
        }
        Set<String> spEntityIds = metadataManager.getSPEntityNames();
        for (String entityId : spEntityIds) {
            MetadataRuntimeView view = MetadataRuntimeView.from(entityId,
                    metadataManager);
            view.setType(MetadataRuntimeView.TYPE_SP);
            list.add(view);
        }
        return list;
    }

    @Operation(summary = "Query one metadata provider detail by alias")
    @GetMapping("/mgr/metadata/alias/{alias}")
    public MetadataRuntimeDetail queryList(@PathVariable("alias") String alias) throws Exception {
        String entityId = null;
        String type = null;
        for (String idp : metadataManager.getIDPEntityNames()) {
            ExtendedMetadata extendedMetadata = metadataManager.getExtendedMetadata(idp);
            if (alias.equals(extendedMetadata.getAlias())) {
                type = MetadataRuntimeView.TYPE_IDP;
                entityId = idp;
                break;
            }
        }
        if (null == entityId) {
            for (String sp : metadataManager.getSPEntityNames()) {
                ExtendedMetadata extendedMetadata = metadataManager.getExtendedMetadata(sp);
                if (alias.equals(extendedMetadata.getAlias())) {
                    type = MetadataRuntimeView.TYPE_SP;
                    entityId = sp;
                    break;
                }
            }
        }
        MetadataRuntimeDetail result = MetadataRuntimeDetail.from(entityId, metadataManager);
        result.setType(type);
        return result;
    }
}
