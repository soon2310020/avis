package com.stg.service3rd.toolcrm.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.service3rd.toolcrm.constant.DirectSubmitStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectSubmitStatusReq {

    @NotNull
    private DirectSubmitStatus status;

    @NotBlank
    @JsonProperty("application_number")
    private String applicationNumber;

    @NotBlank
    @JsonProperty("cif_number")
    private String cifNumber;

    @JsonProperty("error_http_code")
    private Integer errorHttpCode;

    @JsonProperty("error_message")
    private String errorMessage;

    public static DirectSubmitStatusReq of(SubmitPotentialCustomerReq submitData, DirectSubmitStatus status) {
        DirectSubmitStatusReq req = new DirectSubmitStatusReq();
        req.setApplicationNumber(submitData.getApplicationNumber());
        req.setCifNumber(submitData.getCustomer().getCifNumber());
        req.setStatus(status);
        return req;
    }
    
}
