package com.nextjump.saml2.model;

import com.nextjump.saml2.model.listener.AbstractModelListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@EntityListeners(value = AbstractModelListener.class)
@MappedSuperclass
public abstract class AbstractModel extends StringIdentity {
    @Column(name = "CREATED_ON", updatable = false)
    private Long createdOn;

    @Column(name = "LAST_MODIFIED_ON")
    private Long lastModifiedOn;

    @Version
    @Column(name = "VERSION_NUMBER")
    private long versionNumber = 1;

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

    public long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }
}
