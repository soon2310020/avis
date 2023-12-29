package com.stg.service3rd.common.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface IApi3rdMethods {
    <B> B post(IFunction func, HttpHeaders headers, Object payload, Class<B> responseType) throws Exception;

    <B> B get(IFunction func, HttpHeaders headers, Object payload, Class<B> responseType, @Nullable Map<String, ?> uriVariables) throws Exception;

    /***/
    default <B> B get(IFunction func, HttpHeaders headers, Object payload, Class<B> responseType) throws Exception {
        return get(func, headers, payload, responseType, null);
    }

    default <B> B get(IFunction func, HttpHeaders headers, Class<B> responseType, @Nullable Map<String, ?> uriVariables) throws Exception {
        return get(func, headers, null, responseType, uriVariables);
    }

    default <B> B get(IFunction func, HttpHeaders headers, Class<B> responseType) throws Exception {
        return get(func, headers, null, responseType, null);
    }
}
