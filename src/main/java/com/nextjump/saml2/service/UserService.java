package com.nextjump.saml2.service;

import com.nextjump.saml2.service.request.UserCreateRequest;
import com.nextjump.saml2.service.request.UserQueryPageRequest;
import com.nextjump.saml2.view.UserView;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserView> queryPage(UserQueryPageRequest param);

    UserView create(UserCreateRequest param);

    void deleteById(String id);

    UserView update(String id, UserCreateRequest param);

    UserView findByEmail(String email);
}
