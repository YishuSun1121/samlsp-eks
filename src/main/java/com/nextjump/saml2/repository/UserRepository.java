package com.nextjump.saml2.repository;

import com.nextjump.saml2.model.User;

public interface UserRepository extends AbstractModelRepository<User> {
    long countByEmail(String email);

    User findByEmail(String email);
}
