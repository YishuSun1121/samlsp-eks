package com.nextjump.saml2.view;

import com.nextjump.saml2.model.SAMLOperationLog;

public class SAMLOperationLogView extends BaseView {
    public static void convert(SAMLOperationLog model, SAMLOperationLogView view) {
        BaseView.convert(model, view);
    }

    public static SAMLOperationLogView from(SAMLOperationLog model) {
        if (null == model) {
            return null;
        }

        SAMLOperationLogView view = new SAMLOperationLogView();
        convert(model, view);
        return view;
    }

    private String operation;
    private String result;
    private String peerAddress;
    private String peerEntityId;
    private String localEntityId;
    private String principal;

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
}
