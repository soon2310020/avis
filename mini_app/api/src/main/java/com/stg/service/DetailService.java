package com.stg.service;

import com.stg.utils.AppUtil;

public interface DetailService<T> {

    T get(Long id, String createdBy);

    default public T get(Long id) {
        String username = AppUtil.getCurrentUsername();
        return get(id, username);
    }
}
