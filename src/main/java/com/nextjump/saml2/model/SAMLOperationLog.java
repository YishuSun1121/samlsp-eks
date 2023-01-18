package com.nextjump.saml2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "saml_operation_log")
public class SAMLOperationLog extends AbstractModel {
    private String operation;
    private String result;
    private String peerAddress;
    private String peerEntityId;
    private String localEntityId;
    private String principal;
    @Column(length = 8192)
    @Lob
    private String message;
    @Column(length = 8192)
    @Lob
    private String error;
    @Column(length = 8192)
    @Lob
    private String httpRequestHeaders;

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public String getPeerEntityId() {
        return peerEntityId;
    }

    public void setPeerEntityId(String peerEntityId) {
        this.peerEntityId = peerEntityId;
    }

    public String getLocalEntityId() {
        return localEntityId;
    }

    public void setLocalEntityId(String localEntityId) {
        this.localEntityId = localEntityId;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getHttpRequestHeaders() {
        return httpRequestHeaders;
    }

    public void setHttpRequestHeaders(String httpRequestHeaders) {
        if (null != httpRequestHeaders && httpRequestHeaders.length() > 8192) {
            this.httpRequestHeaders = httpRequestHeaders.substring(0, 8192);
        } else {
            this.httpRequestHeaders = httpRequestHeaders;
        }

    }
}
