package com.nextjump.saml2.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nextjump.saml2.model.SAMLOperationLog;
import com.nextjump.saml2.repository.AbstractRepository;
import com.nextjump.saml2.repository.SAMLOperationLogRepository;
import com.nextjump.saml2.service.SAMLOperationLogService;
import com.nextjump.saml2.service.request.SAMLOperationLogQueryRequest;
import com.nextjump.saml2.view.SAMLOperationLogDetail;
import com.nextjump.saml2.view.SAMLOperationLogView;
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
public class SAMLOperationLogServiceImpl extends AbstractBaseService<SAMLOperationLog>
        implements SAMLOperationLogService {
    @Autowired
    private SAMLOperationLogRepository samlOperationLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SAMLOperationLogView> queryPage(SAMLOperationLogQueryRequest param) {
        return samlOperationLogRepository.findAll(new Specification<SAMLOperationLog>() {
            @Override
            public Predicate toPredicate(Root<SAMLOperationLog> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = Lists.newArrayList();
                if (!Strings.isNullOrEmpty(param.getOperation())) {
                    predicateList.add(criteriaBuilder.equal(root.get("operation"), param.getOperation()));
                }
                if (!Strings.isNullOrEmpty(param.getPeerEntityId())) {
                    predicateList.add(criteriaBuilder.like(root.get("peerEntityId"), "%" + param.getPeerEntityId() +
                            "%"));
                }
                if (!Strings.isNullOrEmpty(param.getResult())) {
                    predicateList.add(criteriaBuilder.equal(root.get("result"), param.getResult()));
                }
                return criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }
        }, param.resolvePageable()).map(SAMLOperationLogView::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SAMLOperationLogDetail getOne(String id) {
        return SAMLOperationLogDetail.from(requiredModelById(id));
    }

    @Override
    AbstractRepository<SAMLOperationLog> getRepository() {
        return samlOperationLogRepository;
    }
}
