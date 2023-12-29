package com.stg.service3rd.mbal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MicInsuranceResultRespDto {

    @JsonProperty("Code")
    private String Code;
    @JsonProperty("Message")
    private String Message;
    @JsonProperty("phi")
    private BigDecimal phi;

    private String micTransactionId;

    private BigDecimal micSumBenefit;

    //more...
    private String gcn;
    @JsonProperty("so_id")
    private String soId;
    private String file;
}
