package com.stg.service.dto.external.responseFlexible;

import com.stg.utils.ApplicationStatus;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateQuoteFlexibleModel {

    private Integer processId;
    private String quotationDate;
    private String productPackageCode;

    private FlexibleCommon.PolicyHolderAndLifeAssured policyHolder;
    private List<FlexibleCommon.PolicyHolderAndLifeAssured> assureds;

    private FlexibleCommon.ProductPackageOutput productPackage;
    private FlexibleCommon.Referrer3rdInput referer;
    private FlexibleCommon.Referrer3rdInput referrer;
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
    private boolean raiderDeductFund;
    private List<QuotationAmountRespDto> amounts;
    private List<FlexibleCommon.BeneficiaryOutput> beneficiaries;
}
