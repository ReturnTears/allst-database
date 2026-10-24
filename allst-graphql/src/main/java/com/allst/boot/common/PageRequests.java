package com.allst.boot.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequests {

    private PageRequests() {
    }

    public static Pageable of(PageInput input) {
        int page = 0;
        int size = 10;
        if (input != null) {
            if (input.getPage() != null && input.getPage() > 0) {
                page = input.getPage();
            }
            if (input.getSize() != null) {
                size = input.getSize();
            }
        }
        if (size < 1) {
            size = 1;
        }
        if (size > 50) {
            size = 50;
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
