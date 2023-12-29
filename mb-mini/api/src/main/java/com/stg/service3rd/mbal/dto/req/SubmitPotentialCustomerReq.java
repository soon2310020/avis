package com.stg.service3rd.mbal.dto.req;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.stg.service3rd.mbal.dto.MbalICDto;
import com.stg.service3rd.mbal.dto.MbalRMDto;
import com.stg.service3rd.mbal.dto.MbalPotentialCustomerDto;
import com.stg.service3rd.mbal.dto.ProductInfoDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SubmitPotentialCustomerReq {
    //public static final String PRODUCT_DEFAULT = "17"; // Sống trọn ước mơ

    @NotBlank
    private String leadPartnerId;
    @NotBlank
    private String applicationNumber;

    @NotNull
    @Valid
    private MbalPotentialCustomerDto customer;

    @NotNull
    @Valid
    private MbalRMDto relationshipManager;
    @NotNull
    @Valid
    private MbalICDto insuranceConsultant;

    @Valid
    private List<ProductInfoDto> products;

}
