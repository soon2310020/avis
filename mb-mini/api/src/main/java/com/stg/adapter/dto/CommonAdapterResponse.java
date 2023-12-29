package com.stg.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonAdapterResponse {
    private String clientMessageId;
    private Object data;
    private String errorCode;
    private List<String> errorDesc;
    private String signature;
    private String requestId;

    public CommonAdapterResponse(String clientMessageId, String errorCode) {
        this.clientMessageId = clientMessageId;
        this.errorCode = errorCode;
    }
}
