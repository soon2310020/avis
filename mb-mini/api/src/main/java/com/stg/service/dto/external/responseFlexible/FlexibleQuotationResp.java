package com.stg.service.dto.external.responseFlexible;

import com.stg.service.dto.external.requestFlexible.FlexibleSubmitMicQuestionRequest;
import com.stg.service3rd.toolcrm.constant.QuotationState;
import com.stg.service3rd.toolcrm.dto.resp.quote.DirectInfo;
import com.stg.service3rd.toolcrm.dto.resp.quote.FlexibleSubmitQuestionInput;
import com.stg.service3rd.toolcrm.dto.resp.quote.QuotationDetailDto;
import com.stg.utils.ApplicationStatus;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FlexibleQuotationResp {
    private UUID toolUid;
    private CrmDataMatchInfo matchInfo;

    private Long processId;
    @Enumerated(EnumType.STRING)
    private QuotationState state;
    private String quotationDate;
    private String productPackageCode;

    private FlexibleCommon.PolicyHolderAndLifeAssured policyHolder;// customer
    private LifeAssuredResp lifeAssured; // người BH chính và các gói BH đi kèm

    private List<FlexibleCommon.PolicyHolderAndLifeAssured> assureds;

    private FlexibleCommon.ProductPackageOutput productPackage; // gói BH chính
    private FlexibleCommon.RefererInput referrer; // ?
    private FlexibleCommon.RefererInput supporter;
    private FlexibleCommon.Sale sale;

    private String quotationCode;
    private String submissionCode;
    private String applicationNumber;
    private String micTransactionId;
    private String policyNumber;
    private QuotationStatus quotationStatus;
    private ApplicationStatus applicationStatus;
    private Integer appQuestionNumber;

    private FlexibleCommon.CashFlow cashFlow;
    private List<FlexibleCommon.Rider> riders;

    private BigDecimal totalAmount;
    private String totalAmountStr;
    private String totalMicAmountStr;
    private String totalMbalAmountStr;
    private String additionalMbalAmountStr;

    private List<AdditionalAssuredOutput> additionalAssuredOutputs; // Người bổ sung và các gói đi kèm của người bổ sung
    private FlexibleCommon.CustomerInfo customer;
    private List<QuotationDetailDto> details;

    private List<FlexibleSubmitQuestionInput> healths;

    private List<FlexibleSubmitMicQuestionRequest> micHealths;

    ////// MORE: FlexibleQuoteRespDto //////
    private String riderAmountStr;
    private String topupAmountStr;
    private String mbalMainAmountStr;
    private Long insuranceRequestId;

    private boolean raiderDeductFund;

    private List<QuotationAmountRespDto> amounts;

    private List<FlexibleCommon.BeneficiaryOutput> beneficiaries;

    private List<MicAdditionalProductModel> micProductModels;

    private FlexibleCommon.RefererInput refererInput;

    private DirectInfo directInfo;


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class CrmDataMatchInfo {
        private boolean matched;
        private String message;

        private boolean expired;
        private boolean tsaNotMatch;

        public boolean hasMustRecreate() {
            return !isMatched() || isExpired();
        }


        private boolean recreated; /*Trạng thái quotation đã được re-created chưa*/
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class LifeAssuredResp extends FlexibleCommon.PolicyHolderAndLifeAssured {
        private Boolean isCustomer = false;
    }
}
