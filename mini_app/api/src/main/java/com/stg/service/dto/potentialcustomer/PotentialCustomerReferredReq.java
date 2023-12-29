package com.stg.service.dto.potentialcustomer;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PotentialCustomerReferredReq {
    @NotBlank
    private String icCode;
    @NotBlank
    private String name;
    private String phoneNumber;
    private String reason;
}
