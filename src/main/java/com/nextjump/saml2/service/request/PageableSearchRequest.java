package com.nextjump.saml2.service.request;

import org.springframework.data.domain.Pageable;

public interface PageableSearchRequest {
    int getPage();

    int getSize();

    String[] getAttributesOrderByAsc();

    String[] getAttributesOrderByDesc();

    Pageable resolvePageable();
}
