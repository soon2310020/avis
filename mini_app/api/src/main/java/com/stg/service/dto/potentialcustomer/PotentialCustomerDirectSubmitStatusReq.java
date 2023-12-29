package com.stg.service.dto.potentialcustomer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.stg.entity.potentialcustomer.DirectSubmitStatus;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PotentialCustomerDirectSubmitStatusReq {

    @NotNull
    private DirectSubmitStatus status;

    @NotBlank
    private String applicationNumber;

    @NotBlank
    private String cifNumber;
    
    private Integer errorHttpCode;

    private String errorMessage;
    
    public static PotentialCustomerDirectSubmitStatusReq of(SubmitPotentialCustomerReq submitData,
            DirectSubmitStatus status) {
        PotentialCustomerDirectSubmitStatusReq req = new PotentialCustomerDirectSubmitStatusReq();
        req.setApplicationNumber(submitData.getApplicationNumber());
        req.setCifNumber(submitData.getCustomer().getCifNumber());
        req.setStatus(status);
        return req;
    }

}
