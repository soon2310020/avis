package com.stg.service3rd.ocr.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessedOcrResp {
    private OcrData data;

    @JsonProperty("client_message_id")
    private String clientMessageId;

    private Integer status;

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
    public static class OcrResult {
        @JsonProperty("created_by")
        private String createdBy;

        @JsonProperty("created_time")
        private String createdTime;

        @JsonProperty("process_code")
        private String processCode;

        @JsonProperty("is_active")
        private Boolean isActive;

        private String id;

        private String status;
        private List<Doc> docs;

        @JsonProperty("pre_check_result")
        private List<PreCheckResult> preCheckResult;

        @JsonProperty("fraud_check_result")
        private List<FraudCheckResult> fraudCheckResult;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class Doc {
        @JsonProperty("doctype_code")
        private String docTypeCode;

        @JsonProperty("template_codes")
        private List<String> templateCodes;

        private List<String> attachments;

        @JsonProperty("single_result")
        private List<SingleResult> singleResult;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class SingleResult {
        private String name;
        private String value;
        private float conf;
        private Box box;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class PreCheckResult {
        private String name;
        private String value;
        private float conf;
        private Box box;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class FraudCheckResult {
        private String name;
        private String value;
        private float conf;
        private Box box;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class Box {
        private float xmin;
        private float ymin;
        private float xmax;
        private float ymax;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class  OcrData {
       private OcrResult result;
    }
}


