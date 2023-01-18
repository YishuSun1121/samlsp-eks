package com.nextjump.saml2.model.listener;


import com.nextjump.saml2.model.AbstractModel;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AbstractModelListener {
    @PrePersist
    public void prePersist(AbstractModel model) {
        if (null == model.getCreatedOn())
            model.setCreatedOn(System.currentTimeMillis());

        model.setLastModifiedOn(System.currentTimeMillis());
    }

    @PreUpdate
    public void preUpdate(AbstractModel model) {
        model.setLastModifiedOn(System.currentTimeMillis());
    }
}
