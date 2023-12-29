package com.stg.service3rd.mbal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


@Data
@Accessors(chain = true)
public class MicGenerateInsuranceCertSandboxRespDto {
    private String code;
    private String message;
    @JsonProperty("data")
    private GenerateInsuranceCertData data;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateInsuranceCertData {
        private String so_hd; //Số giấy chứng nhận(số hợp đồng
        private String gcn; //Link file GCN
        private String so_id; //Số ID GCN
        private String file; //Link file đính kèm
        private BigDecimal phi; //Phí trả bảo hiểm
        private BigDecimal thue; //Thuế
        private BigDecimal ttoan; //Thanh toán
    }
}
