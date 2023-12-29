package com.stg.service.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MbalCustomerInfoRespDto {

    private int result;
    private String errorCode;
    private String errorMessage;

    private boolean isJetCase;
    private String status;

    private String strMixInsuranceFee;
    private BigDecimal mixInsuranceFee;

    private String mbalAppNo;
    private String mbalPolicyNumber;
    private String micTransactionId;

    // quote data
    private String insuranceFee;
    private String baseInsuranceFee;
    private String topupInsuranceFee;
    private String hscrFee;
    private Double amount;
    private String payFrequency;
    private String timeFrequency;
    @JsonProperty("hsTimeFrequency")
    private String hsTimeFrequency;

}
