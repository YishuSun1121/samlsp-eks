package com.nextjump.saml2.view;

import com.nextjump.saml2.model.SAMLOperationLog;

public class SAMLOperationLogDetail extends SAMLOperationLogView {
    public static SAMLOperationLogDetail from(SAMLOperationLog model) {
        if (null == model) {
            return null;
        }

        SAMLOperationLogDetail view = new SAMLOperationLogDetail();
        SAMLOperationLogView.convert(model, view);
        return view;
    }

    private String message;
    private String error;
    private String httpRequestHeaders;

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
        this.httpRequestHeaders = httpRequestHeaders;
    }
}
