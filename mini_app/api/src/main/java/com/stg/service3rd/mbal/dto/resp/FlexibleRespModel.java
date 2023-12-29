package com.stg.service3rd.mbal.dto.resp;

import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.constant.ApplicationStatus;
import com.stg.service3rd.mbal.constant.QuotationStatus;
import com.stg.service3rd.mbal.dto.AdditionalAssuredOutput;
import com.stg.service3rd.mbal.dto.QuotationDetailDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleRespModel {

    private Long processId;
    private String quotationDate;
    private String productPackageCode;

    private FlexibleCommon.PolicyHolderAndLifeAssured policyHolder;
    private FlexibleCommon.PolicyHolderAndLifeAssured lifeAssured;

    private List<FlexibleCommon.PolicyHolderAndLifeAssured> assureds;

    private FlexibleCommon.ProductPackageOutput productPackage;
    private FlexibleCommon.Referrer3rdInput referer;
    private FlexibleCommon.Referrer3rdInput supporter;
    private String quotationCode;
    private String submissionCode;
    private String applicationNumber;
    private String micTransactionId;
    private String policyNumber;
    private QuotationStatus quotationStatus;
    private ApplicationStatus applicationStatus;
    private Integer appQuestionNumber;
    private FlexibleCommon.Sale sale;
    private FlexibleCommon.CashFlow cashFlow;
    private List<FlexibleCommon.Rider> riders;

    private String totalAmountStr;
    private String totalMicAmountStr;
    private String totalMbalAmountStr;
    private String additionalMbalAmountStr;

    private List<AdditionalAssuredOutput> additionalAssuredOutputs;
    private FlexibleCommon.CustomerInfo customer;
    private List<QuotationDetailDto> details;

}
