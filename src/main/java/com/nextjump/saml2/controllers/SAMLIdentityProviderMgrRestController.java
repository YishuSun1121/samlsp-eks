package com.nextjump.saml2.controllers;

import com.nextjump.saml2.service.SAMLIdentityProviderService;
import com.nextjump.saml2.service.request.DefaultSAMLIdentityProviderCreateRequest;
import com.nextjump.saml2.service.request.DefaultSAMLIdentityProviderQueryRequest;
import com.nextjump.saml2.view.SAMLIdentityProviderDetail;
import com.nextjump.saml2.view.SAMLIdentityProviderView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "IdentityProvider Management")
@RestController
public class SAMLIdentityProviderMgrRestController {
    @Autowired
    private SAMLIdentityProviderService samlIdentityProviderService;

    @Operation(summary = "Query all identity provider definition")
    @GetMapping("/mgr/saml-identity-providers")
    public Page<SAMLIdentityProviderView> queryPage(DefaultSAMLIdentityProviderQueryRequest param) {
        return samlIdentityProviderService.queryPage(param);
    }

    @Operation(summary = "Create new identity provider definition")
    @PostMapping("/mgr/saml-identity-providers")
    public SAMLIdentityProviderView create(@RequestBody DefaultSAMLIdentityProviderCreateRequest param) {
        return samlIdentityProviderService.create(param);
    }

    @Operation(summary = "Update identity provider definition by id")
    @PostMapping("/mgr/saml-identity-providers/{id}")
    public SAMLIdentityProviderView update(@PathVariable String id,
                                           @RequestBody DefaultSAMLIdentityProviderCreateRequest param) {
        return samlIdentityProviderService.update(id, param);
    }

    @Operation(summary = "Get one identity provider definition detail by id")
    @GetMapping("/mgr/saml-identity-providers/{id}")
    public SAMLIdentityProviderDetail getOne(@PathVariable String id) {
        return samlIdentityProviderService.getOne(id);
    }

    @Operation(summary = "Delete identity provider definition by id")
    @DeleteMapping("/mgr/saml-identity-providers/{id}")
    public void deleteById(@PathVariable String id) {
        samlIdentityProviderService.deleteById(id);
    }
}
