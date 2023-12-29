package com.stg.adapter.service;

import com.stg.utils.Constants;
import org.springframework.http.HttpEntity;

public interface CallExternalIService {
    <T> T callExternal(String host, HttpEntity<?> entity, Class<T> clazz, String payloadLog, Constants.HOST_PARTY hostParty);

}
