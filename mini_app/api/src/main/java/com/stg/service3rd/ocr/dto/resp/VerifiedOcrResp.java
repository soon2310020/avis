package com.stg.service3rd.ocr.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
public class VerifiedOcrResp {
    private String clientMessageId;

    private int status;

    @JsonProperty("data")
    private VerifiedData data;

    private String error;

    private String path;

    @JsonProperty("soa_error_code")
    private String soaErrorCode;

    @JsonProperty("soa_error_desc")
    private String soaErrorDesc;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class VerifiedObject {
        @JsonProperty("is_active")
        private boolean isActive;

        @JsonProperty("created_by")
        private String createdBy;

        @JsonProperty("created_time")
        private String createdTime;

        @JsonProperty("modified_time")
        private String modifiedTime;

        @JsonProperty("session_request_id")
        private String sessionRequestId;

        @JsonProperty("ocr_public_key_exchange")
        private String ocrPublicKeyExchange;

        @JsonProperty("device_id")
        private String deviceId;

        @JsonProperty("session_verified_at")
        private Instant sessionVerifiedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class VerifiedData {
        @JsonProperty("result")
        private VerifiedObject result;
    }

}
