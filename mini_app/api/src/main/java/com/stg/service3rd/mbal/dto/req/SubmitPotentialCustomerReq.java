package com.stg.service3rd.mbal.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.potentialcustomer.PotentialCustomerRefer;
import com.stg.service3rd.mbal.dto.MbalICDto;
import com.stg.service3rd.mbal.dto.MbalPotentialCustomerDto;
import com.stg.service3rd.mbal.dto.MbalRMDto;
import com.stg.service3rd.mbal.dto.ProductInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SubmitPotentialCustomerReq {
    public static final String PRODUCT_DEFAULT = "17"; // Sống trọn ước mơ

    @NotBlank
    private String leadPartnerId;
    private String applicationNumber; // only required vs direct

    @NotNull
    @Valid
    private MbalPotentialCustomerDto customer;

    //@NotNull => bỏ để call lỗi sẽ save log
    @Valid
    private MbalRMDto relationshipManager;
    //@NotNull => bỏ để call lỗi sẽ save log
    @Valid
    private MbalICDto insuranceConsultant;

    @Valid
    private List<ProductInfoDto> products;

    @JsonIgnore
    private PotentialCustomerDirect direct;

    /**
     * REFER
     */
    public static SubmitPotentialCustomerReq of(PotentialCustomerRefer potentialCustomerRefer,
                                                MbalRMDto rm,
                                                MbalICDto ic,
                                                String product) {

        String leadId = potentialCustomerRefer.getLeadId();

        ProductInfoDto productInfoDto = new ProductInfoDto(
                potentialCustomerRefer.getPotentialCustomer().getInputAmount(), product,
                potentialCustomerRefer.getPotentialCustomer().getNote());

        return new SubmitPotentialCustomerReq(leadId, null, MbalPotentialCustomerDto
                .of(potentialCustomerRefer.getPotentialCustomer(), MbalPotentialCustomerDto.BUSINESS_MODEL_REFER), rm,
                ic, List.of(productInfoDto), null);
    }

    /**
     * DIRECT
     */
    public static SubmitPotentialCustomerReq of(PotentialCustomerDirect potentialCustomerDirect, MbalRMDto rm,
                                                MbalICDto ic, String product) {
        String leadId = potentialCustomerDirect.getLeadId();

        MbalPotentialCustomerDto customerDto = MbalPotentialCustomerDto
                .of(potentialCustomerDirect.getQuotationHeader().getCustomer(), potentialCustomerDirect.getCifNumber());

        ProductInfoDto productInfoDto = new ProductInfoDto(
                potentialCustomerDirect.getQuotationHeader().getPackagePeriodicPremium(), product,
                potentialCustomerDirect.getPotentialCustomer().getNote());

        return new SubmitPotentialCustomerReq(leadId, potentialCustomerDirect.getApplicationNumber(), customerDto, rm, ic, List.of(productInfoDto),
                potentialCustomerDirect);
    }

}
