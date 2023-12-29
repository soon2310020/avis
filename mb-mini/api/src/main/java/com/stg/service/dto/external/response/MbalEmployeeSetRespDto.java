package com.stg.service.dto.external.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MbalEmployeeSetRespDto {

    @JsonProperty("appNo")
    private String appNo;
    @JsonProperty("policyNumber")
    private String policyNumber;
    @JsonProperty("quoteOptionId")
    private String quoteOptionId;

}
