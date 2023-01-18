package com.nextjump.saml2.view;

import com.nextjump.saml2.model.AbstractModel;
import com.nextjump.saml2.model.StringIdentity;
import org.springframework.beans.BeanUtils;

public class BaseView extends StringIdentity {
    public static void convert(AbstractModel model, BaseView view) {
        BeanUtils.copyProperties(model, view);
    }

    private Long createdOn;
    private Long lastModifiedOn;
    private Long versionNumber;

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Long getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(Long lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Long versionNumber) {
        this.versionNumber = versionNumber;
    }
}
