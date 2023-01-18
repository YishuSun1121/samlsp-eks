package com.nextjump.saml2.repository;


import com.nextjump.saml2.model.AbstractModel;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractModelRepository<T extends AbstractModel> extends AbstractRepository<T> {
}
