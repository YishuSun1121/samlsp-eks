package com.nextjump.saml2.enums;

public enum MetadataSource implements LabelEnum {
    HTTP("HTTP"),
    XML("XML"),
//    @Deprecated FILE("FILE"),
    ATTRIBUTE("ATTRIBUTE"),
    ;

    public String label;

    MetadataSource(String label) {
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
