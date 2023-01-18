package com.nextjump.saml2.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nextjump.saml2.exception.AppExceptionCode;
import com.nextjump.saml2.exception.AppValidationException;
import com.nextjump.saml2.model.User;
import com.nextjump.saml2.repository.AbstractRepository;
import com.nextjump.saml2.repository.UserRepository;
import com.nextjump.saml2.service.UserService;
import com.nextjump.saml2.service.request.UserCreateRequest;
import com.nextjump.saml2.service.request.UserQueryPageRequest;
import com.nextjump.saml2.view.UserView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Service
public class UserServiceImpl extends AbstractBaseService<User> implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    AbstractRepository<User> getRepository() {
        return userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserView> queryPage(UserQueryPageRequest param) {
        return userRepository.findAll(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = Lists.newArrayList();

                return criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }
        }, param.resolvePageable()).map(UserView::from);
    }

    @Override
    @Transactional
    public UserView create(UserCreateRequest param) {
        if (Strings.isNullOrEmpty(param.getEmail())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "email is empty not allowed");
        }
        if (Strings.isNullOrEmpty(param.getEcid())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "ecid is empty not allowed");
        }
        if (userRepository.countByEmail(param.getEmail()) > 0) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format("email %s existing"));
        }
        User model = new User();
        BeanUtils.copyProperties(param, model);
        return UserView.from(userRepository.save(model));
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        User model = requiredModelById(id);
        userRepository.delete(model);
    }

    @Override
    @Transactional
    public UserView update(String id, UserCreateRequest param) {
        if (Strings.isNullOrEmpty(param.getEmail())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "email is empty not allowed");
        }
        if (Strings.isNullOrEmpty(param.getEcid())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "ecid is empty not allowed");
        }
        User sameEmail = userRepository.findByEmail(param.getEmail());
        if (null != sameEmail && !sameEmail.getId().equals(id)) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format("email %s existing"));
        }
        User model = requiredModelById(id);
        BeanUtils.copyProperties(param, model);
        return UserView.from(model);
    }

    @Override
    @Transactional
    public UserView findByEmail(String email) {
        return UserView.from(userRepository.findByEmail(email));
    }
}
