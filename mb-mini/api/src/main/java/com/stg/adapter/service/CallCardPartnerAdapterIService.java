package com.stg.adapter.service;

import com.stg.adapter.dto.CommonAdapterResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

public interface CallCardPartnerAdapterIService {
    <P> CommonAdapterResponse callGetCardPartnerAdapter(String urlTemplate, Map<String, String> params, HttpMethod method, HttpHeaders headers,P param);
}