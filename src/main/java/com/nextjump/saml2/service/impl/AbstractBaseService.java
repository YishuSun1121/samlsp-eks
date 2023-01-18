package com.nextjump.saml2.service.impl;


import com.nextjump.saml2.exception.AppExceptionCode;
import com.nextjump.saml2.exception.AppValidationException;
import com.nextjump.saml2.repository.AbstractRepository;

public abstract class AbstractBaseService<M> {

    abstract AbstractRepository<M> getRepository();

    public M requiredModelById(String id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new AppValidationException(AppExceptionCode.INVALID_PARAMS, "data record not " +
                        "existed"));
    }
}
