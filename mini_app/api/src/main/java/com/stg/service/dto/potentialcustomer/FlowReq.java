package com.stg.service.dto.potentialcustomer;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FlowReq {

    @NotNull
    private BigDecimal amount;
    
}
