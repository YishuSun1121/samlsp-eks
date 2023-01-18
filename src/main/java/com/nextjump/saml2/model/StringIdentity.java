package com.nextjump.saml2.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public abstract class StringIdentity implements IdentityProvider<String>, Serializable {
    private static final long serialVersionUID = 1L;
    @Access(AccessType.PROPERTY)
    @GeneratedValue(
            generator = "uuid2"
    )
    @GenericGenerator(
            name = "uuid2",
            strategy = "uuid2"
    )
    @Column(
            name = "ID"
    )
    @Id
    private String id;

    public StringIdentity() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else if (!(obj instanceof StringIdentity)) {
            return false;
        } else {
            StringIdentity other = (StringIdentity) obj;
            return Objects.equals(this.getId(), other.getId()) && super.equals(obj);
        }
    }

    public int hashCode() {
        return this.getId().hashCode();
    }
}
