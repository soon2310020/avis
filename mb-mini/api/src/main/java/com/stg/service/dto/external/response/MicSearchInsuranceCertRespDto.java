package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MicSearchInsuranceCertRespDto {
    @JsonProperty("Code")
    private String Code;
    @JsonProperty("Message")
    private String Message;
    @JsonProperty("data")
    private SearchInsuranceCertData data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchInsuranceCertData {
        @JsonProperty("gcn")
        private String gcn;
        @JsonProperty("phi")
        private String phi;
        @JsonProperty("ten")
        private String ten;
        @JsonProperty("cmt")
        private String cmt;
        @JsonProperty("dchi")
        private String dchi;
        @JsonProperty("mobi")
        private String mobi;
        @JsonProperty("ngay_hl")
        private String ngay_hl;
        @JsonProperty("ngay_kt")
        private String ngay_kt;
        @JsonProperty("file")
        private String file;
    }
}
