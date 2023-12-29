package com.stg.service3rd.common.adapter.soap;

import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.soap.SoapRequest;
import com.stg.service3rd.common.dto.soap.SoapResponse;
import com.stg.service3rd.common.dto.soap.SoapResponseBody;
import org.springframework.http.HttpHeaders;

public interface ISoap3rdMethods {
    <B extends SoapResponseBody, R extends SoapResponse<B>> R post(IFunction func, HttpHeaders headers, SoapRequest payload, Class<R> responseType) throws Exception;

    <B extends SoapResponseBody, R extends SoapResponse<B>> R post(IFunction func, SoapRequest payload, Class<R> responseType) throws Exception;
}
