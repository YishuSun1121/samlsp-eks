package com.nextjump.saml2.controllers;

import com.nextjump.saml2.service.SAMLOperationLogService;
import com.nextjump.saml2.service.request.DefaultSAMLOperationLogQueryRequest;
import com.nextjump.saml2.view.SAMLOperationLogDetail;
import com.nextjump.saml2.view.SAMLOperationLogView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SAMLOperationLogManagement")
@RestController
public class SAMLOperationLogMgrRestController {
    @Autowired
    private SAMLOperationLogService samlOperationLogService;

    @Operation(summary = "Query all saml operation log summary list")
    @GetMapping("/mgr/saml-operation-logs")
    public Page<SAMLOperationLogView> queryPage(DefaultSAMLOperationLogQueryRequest param) {
        return samlOperationLogService.queryPage(param);
    }

    @Operation(summary = "Get one saml operation log detail by id")
    @GetMapping("/mgr/saml-operation-logs/{id}")
    public SAMLOperationLogDetail getOne(@PathVariable String id) {
        return samlOperationLogService.getOne(id);
    }
}
