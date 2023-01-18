package com.nextjump.saml2.service.request;

import io.swagger.v3.oas.annotations.media.Schema;

public class DefaultSAMLOperationLogQueryRequest extends DefaultPageableSearchRequest
        implements SAMLOperationLogQueryRequest {
    @Schema(defaultValue = "")
    private String operation;
    @Schema(defaultValue = "")
    private String peerEntityId;
    @Schema(defaultValue = "")
    private String result;

    @Override
    public String getPeerEntityId() {
        return peerEntityId;
    }

    public void setPeerEntityId(String peerEntityId) {
        this.peerEntityId = peerEntityId;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
