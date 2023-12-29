package com.stg.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.stg.utils.AppUtil;

public interface SearchService<T> {

    Page<T> search(Pageable pageable, String query, LocalDate date, LocalDate from, LocalDate to, String createdBy);
    
    default public Page<T> search(Pageable pageable, String query, LocalDate date, LocalDate from, LocalDate to) {
        String username = AppUtil.getCurrentUsername();
        return search(pageable, query, date, from, to, username);
    }

}
