package com.nextjump.saml2.view;

import com.nextjump.saml2.model.User;
import org.springframework.beans.BeanUtils;

public class UserView extends BaseView {
    public static final UserView from(User model) {
        if (null == model) {
            return null;
        }
        UserView view = new UserView();
        BeanUtils.copyProperties(model, view);
        return view;
    }

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
