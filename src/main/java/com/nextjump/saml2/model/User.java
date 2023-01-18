package com.nextjump.saml2.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User extends AbstractModel {
    @Column(unique = true)
    private String email;
    private String ecid;
    private String orgdir;

    public String getOrgdir() {
        return orgdir;
    }

    public void setOrgdir(String orgdir) {
        this.orgdir = orgdir;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEcid() {
        return ecid;
    }

    public void setEcid(String ecid) {
        this.ecid = ecid;
    }
}
