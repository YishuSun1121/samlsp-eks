package com.nextjump.saml2.service.request;

public interface SAMLOperationLogQueryRequest extends PageableSearchRequest {
    String getPeerEntityId();

    String getOperation();

    String getResult();
}
