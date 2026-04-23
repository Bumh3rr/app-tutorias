package com.bumh3r.service.utils;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Primary
@Service
public class PaginationUtil {

    public Pageable getPageable(Integer page, Integer pageSize, String sortBy, String sort) {
        Pageable pageable;
        if (sort.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        return pageable;
    }
}
