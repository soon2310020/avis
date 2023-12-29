package com.stg.service3rd.toolcrm.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.toolcrm.constant.QuotationState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationSyncDataResp {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("message")
    private String message;

    @JsonProperty("state")
    private QuotationState state;
}
