package com.nextjump.saml2.enums;

public enum SSOBinding implements LabelEnum {
    HTTPRedirect("HTTP-Redirect"),
    HTTPPost("HTTP-Post"),
    ;

    public String label;

    SSOBinding(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
