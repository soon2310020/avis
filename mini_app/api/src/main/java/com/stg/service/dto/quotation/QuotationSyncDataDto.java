package com.stg.service.dto.quotation;

import com.stg.constant.quotation.QuotationAction;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectSubmitStatusReq;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class QuotationSyncDataDto {
    @NotNull
    private UUID uuid;

    @ApiModelProperty(name = "Action state")
    private QuotationAction state;

    private String healthsTxt; // List<FlexibleSubmitQuestionInput>
    
    private PotentialCustomerDirectSubmitStatusReq directSubmitStatus;
}
