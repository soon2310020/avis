package com.stg.service.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MicSandboxFeeCareRespDto extends MicSandboxResp {

    private DataMicFeeCare data;

    private BigDecimal phi;

    private String micTransactionId;

    private BigDecimal micSumBenefit;

    public BigDecimal getPhi() {
        if (data != null) {
            return data.getPhi();
        }
        return phi;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataMicFeeCare {
        private BigDecimal phi;
        private BigDecimal thue; // Thuế
        private BigDecimal ttoan; //Số tiền thanh toán
    }

}
