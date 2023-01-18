package com.nextjump.saml2.service;

import com.nextjump.saml2.service.request.SAMLOperationLogQueryRequest;
import com.nextjump.saml2.view.SAMLOperationLogDetail;
import com.nextjump.saml2.view.SAMLOperationLogView;
import org.springframework.data.domain.Page;

public interface SAMLOperationLogService {
    Page<SAMLOperationLogView> queryPage(SAMLOperationLogQueryRequest param);

    SAMLOperationLogDetail getOne(String id);
}
