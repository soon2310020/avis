package com.stg.service.dto.external.responseFlexible;

import com.stg.utils.ApplicationStatus;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

import static com.stg.utils.Constants.ZERO_FEE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleQuoteRespDto {
    private FlexibleQuotationResp.CrmDataMatchInfo matchInfo; // NOTE: from tool => matchInfo always != null

    private Integer processId;
    private String quotationDate;
    private String productPackageCode;

    private FlexibleCommon.PolicyHolderAndLifeAssured policyHolder;// customer
    private FlexibleCommon.PolicyHolderAndLifeAssured lifeAssured; // người BH chính và các gói BH đi kèm
    private FlexibleCommon.ProductPackageOutput productPackage; // gói BH chính
    private FlexibleCommon.Referrer3rdInput referer;
    private FlexibleCommon.Referrer3rdInput referrer;
    private FlexibleCommon.Referrer3rdInput supporter;
    private String quotationCode;
    private String submissionCode;
    private String applicationNumber;
    private String policyNumber;
    private QuotationStatus quotationStatus;
    private ApplicationStatus applicationStatus;
    private Integer appQuestionNumber;
    private FlexibleCommon.Sale sale;

    private BigDecimal totalAmount;
    private String totalAmountStr;
    private String totalMicAmountStr;
    private String totalMbalAmountStr;
    private String riderAmountStr;
    private String topupAmountStr;
    private String mbalMainAmountStr;
    private Long insuranceRequestId;

    private List<AdditionalAssuredOutput> additionalAssuredOutputs; // Người bổ sung và các gói đi kèm của người bổ sung

    private FlexibleCommon.CustomerInfo customer;

    private List<QuotationDetailDto> details;

    private boolean raiderDeductFund;

    private List<QuotationAmountRespDto> amounts;

    private List<FlexibleCommon.BeneficiaryOutput> beneficiaries;

    private List<MicAdditionalProductModel> micProductModels;

    private FlexibleCommon.RefererInput refererInput;

    public String getTotalMicAmountStr() {
        if ((ZERO_FEE).equals(totalMicAmountStr)) {
            return null;
        }
        return totalMicAmountStr;
    }

    public String getRiderAmountStr() {
        if ((ZERO_FEE).equals(riderAmountStr)) {
            return null;
        }
        return riderAmountStr;
    }

    public String getTopupAmountStr() {
        if ((ZERO_FEE).equals(topupAmountStr)) {
            return null;
        }
        return topupAmountStr;
    }
}
