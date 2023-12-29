package com.stg.service3rd.common.adapter;

import com.stg.service3rd.common.adapter.func.IFunction;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface IApi3rdSimpleMethods {
    <B> B post(IFunction func, Object payload, Class<B> responseType) throws Exception;

    <B> B get(IFunction func, Object payload, Class<B> responseType, @Nullable Map<String, ?> uriVariables) throws Exception;

    /***/
    default <B> B get(IFunction func, Object payload, Class<B> responseType) throws Exception {
        return get(func, payload, responseType, null);
    }

    default <B> B get(IFunction func, Class<B> responseType, @Nullable Map<String, ?> uriVariables) throws Exception {
        return get(func, null, responseType, uriVariables);
    }

    default <B> B get(IFunction func, Class<B> responseType) throws Exception {
        return get(func, null, responseType, null);
    }
}
