package com.nextjump.saml2.service.request;

public class DefaultUserCreateRequest implements UserCreateRequest {
    private String email;
    private String ecid;
    private String orgdir;

    @Override
    public String getOrgdir() {
        return orgdir;
    }

    public void setOrgdir(String orgdir) {
        this.orgdir = orgdir;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getEcid() {
        return ecid;
    }

    public void setEcid(String ecid) {
        this.ecid = ecid;
    }
}
