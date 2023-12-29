package com.stg.service.dto.external.responseV2;

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
public class MicInsuranceResultV2RespDto {

    @JsonProperty("Code")
    private String Code;
    @JsonProperty("Message")
    private String Message;
    @JsonProperty("phi")
    private BigDecimal phi;

}
