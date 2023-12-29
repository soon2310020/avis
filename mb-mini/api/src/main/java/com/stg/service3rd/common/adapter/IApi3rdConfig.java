package com.stg.service3rd.common.adapter;

import com.stg.service3rd.common.dto.Host3rd;
import org.springframework.http.HttpHeaders;

public interface IApi3rdConfig {
    Host3rd getHost3rd();

    HttpHeaders getAuthHeader() throws Exception;
}
