package com.stg.service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


@Data
@Accessors(chain = true)
public class MicGenerateInsuranceCertRespDto {

    @JsonProperty("Code")
    private String Code;
    @JsonProperty("Message")
    private String Message;
    @JsonProperty("data")
    private GenerateInsuranceCertData data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateInsuranceCertData {
        @JsonProperty("gcn")
        private String gcn;
        @JsonProperty("phi")
        private String phi;
        @JsonProperty("so_id")
        private String so_id;
        @JsonProperty("file")
        private String file;
    }

    @JsonProperty("Phí trả bảo hiểm")
    private BigDecimal phi;

    @JsonProperty("Số giấy chứng nhận(số hợp đồng)")
    private String so_hd;

    @JsonProperty("Số ID GCN")
    private String so_id;

    @JsonProperty("Link file GCN")
    private String gcn;

    @JsonProperty("Link file đính kèm")
    private String file;

    @JsonProperty("Thuế")
    private BigDecimal thue;

    @JsonProperty("Thanh toán")
    private BigDecimal ttoan;

}
