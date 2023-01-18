package com.nextjump.saml2.service.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class DefaultPageableSearchRequest implements PageableSearchRequest {
    @Schema(defaultValue = "0")
    private int page = 0;
    @Schema(defaultValue = "20")
    private int size = 20;
    @Schema(nullable = true, hidden = true)
    private String[] attributesOrderByAsc;
    @Schema(nullable = true, hidden = true)
    private String[] attributesOrderByDesc;

    @Override
    public String[] getAttributesOrderByAsc() {
        return attributesOrderByAsc;
    }

    public void setAttributesOrderByAsc(String[] attributesOrderByAsc) {
        this.attributesOrderByAsc = attributesOrderByAsc;
    }

    @Override
    public String[] getAttributesOrderByDesc() {
        return attributesOrderByDesc;
    }

    public void setAttributesOrderByDesc(String[] attributesOrderByDesc) {
        this.attributesOrderByDesc = attributesOrderByDesc;
    }

    @Override
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public Pageable resolvePageable() {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 20;
        }

        if (attributesOrderByAsc != null || attributesOrderByDesc != null) {
            List<Sort.Order> orderList = new ArrayList<>();
            if (attributesOrderByAsc != null && attributesOrderByAsc.length > 0) {
                for (int ascInx = 0; ascInx < attributesOrderByAsc.length; ascInx++) {
                    orderList.add(new Sort.Order(Sort.Direction.ASC, attributesOrderByAsc[ascInx]));
                }
            }
            if (attributesOrderByDesc != null && attributesOrderByDesc.length > 0) {
                for (int descInx = 0; descInx < attributesOrderByDesc.length; descInx++) {
                    orderList.add(new Sort.Order(Sort.Direction.DESC, attributesOrderByDesc[descInx]));
                }
            }
            return PageRequest.of(page, size, Sort.by(orderList));
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn"));
    }
}
