package com.stg.service.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GetInstFeeResponse {

    private String clientMessageId;
    private String errorCode;
    private List<String> errorDesc;
    private DataResp data;

    private String periodicConversionFeeStr; //  phí trả góp trên tổng số tiền phải trả
    private String monthlyPaymentStr; // Phi phải trả hằng tháng

    private List<ListCardResponse.DataCardResp> cardResps;

    private String cardErrorCode;

    private String totalFee;

    private String processId;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataResp {

        private String period; // Kỳ hạn

        private String periodicConversionFee; // % phí trả góp trên tổng số tiền phải trả

        private String feesPayable; // Phí trả hàng tháng: (periodicConversionFee * tổng số tiền) / period
    }
}
