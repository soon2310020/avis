package com.stg.service3rd.toolcrm.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.errors.ApplicationException;
import com.stg.service.dto.external.responseFlexible.AdditionalAssuredOutput;
import com.stg.service3rd.toolcrm.dto.resp.quote.DirectInfo;
import com.stg.service.dto.external.responseFlexible.FlexibleQuotationResp;
import com.stg.service.dto.external.responseFlexible.QuotationAmountRespDto;
import com.stg.service3rd.toolcrm.dto.resp.quote.FlexibleQuoteReq;
import com.stg.service3rd.toolcrm.dto.resp.quote.QuotationDetailDto;
import com.stg.service3rd.toolcrm.dto.resp.quote.QuotationHeaderDto;
import com.stg.utils.ApplicationStatus;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.QuotationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

import static com.stg.utils.flexible.FlexibleCrmUtil.isMatchedIdentify;

@Getter
@Setter
@NoArgsConstructor
public class QuotationDetailResp extends QuotationHeaderDto {
    private static final ModelMapper modelMapper = new ModelMapper();

    @JsonProperty("details")
    private List<QuotationDetailDto> details;
    @JsonProperty("app_question_number")
    private Integer appQuestionNumber;
    @JsonProperty("healths_txt")
    private String healthsTxt;

    // more...
    @JsonProperty("product_package_code")
    private String productPackageCode;
    @JsonProperty("policy_holder")
    private FlexibleCommon.PolicyHolderAndLifeAssured policyHolder;// customer
    @JsonProperty("life_assured")
    private FlexibleCommon.PolicyHolderAndLifeAssured lifeAssured; // người BH chính và các gói BH đi kèm
    @JsonProperty("assureds")
    private List<FlexibleCommon.PolicyHolderAndLifeAssured> assureds;

    @JsonProperty("product_package")
    private FlexibleCommon.ProductPackageOutput productPackage; // gói BH chính
    @JsonProperty("application_number")
    private String applicationNumber;
    @JsonProperty("quotation_code")
    private String quotationCode;
    @JsonProperty("quotation_date")
    private String quotationDate;
    @JsonProperty("submission_code")
    private String submissionCode;
    @JsonProperty("policy_number")
    private String policyNumber;
    @JsonProperty("quotation_status")
    private QuotationStatus quotationStatus;
    @JsonProperty("application_status")
    private ApplicationStatus applicationStatus;

    @JsonProperty("cash_flow")
    private FlexibleCommon.CashFlow cashFlow;
    @JsonProperty("riders")
    private List<FlexibleCommon.Rider> riders;

    @JsonProperty("total_amount_str")
    private String totalAmountStr;
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    @JsonProperty("total_mic_amount_str")
    private String totalMicAmountStr;
    @JsonProperty("total_mbal_amount_str")
    private String totalMbalAmountStr;
    @JsonProperty("additional_mbal_amount_str")
    private String additionalMbalAmountStr;

    @JsonProperty("additional_assured_outputs")
    private List<AdditionalAssuredOutput> additionalAssuredOutputs; // Người bổ sung và các gói đi kèm của người bổ sung

    @JsonProperty("rider_amount_str")
    private String riderAmountStr;
    @JsonProperty("topup_amount_str")
    private String topupAmountStr;
    @JsonProperty("mbal_main_amount_str")
    private String mbalMainAmountStr;
    @JsonProperty("insurance_request_id")
    private Long insuranceRequestId;
    @JsonProperty("referer_input")
    private FlexibleCommon.RefererInput refererInput;

    @JsonProperty("amounts")
    private List<QuotationAmountRespDto> amounts;
    @JsonProperty("beneficiaries")
    private List<FlexibleCommon.BeneficiaryOutput> beneficiaries;
    private LocalDateTime createdDate;
    private FlexibleQuoteReq createQuoteReq;

    private DirectInfo direct;

    public FlexibleQuotationResp toResponse() {
        FlexibleQuotationResp resp = new FlexibleQuotationResp();
        resp.setToolUid(this.getUuid());
        resp.setProcessId(this.getProcessId());
        resp.setState(this.getState());
        resp.setQuotationDate(this.getQuotationDate());
        resp.setProductPackageCode(this.getProductPackageCode());

        if (this.getCustomer() != null) {
            resp.setCustomer(modelMapper.map(this.getCustomer(), FlexibleCommon.CustomerInfo.class));
        }
        resp.setPolicyHolder(this.getPolicyHolder());
        if (this.getLifeAssured() != null) {
            FlexibleQuotationResp.LifeAssuredResp lifeAssuredResp = modelMapper.map(this.getLifeAssured(), FlexibleQuotationResp.LifeAssuredResp.class);
            lifeAssuredResp.setIsCustomer(isMatchedIdentify(resp.getCustomer(), this.getLifeAssured()));
            resp.setLifeAssured(lifeAssuredResp);
        }
        resp.setAssureds(this.getAssureds());

        resp.setProductPackage(this.getProductPackage());
        resp.setReferrer(this.getReferrer());
        resp.setSupporter(this.getSupporter());
        resp.setSale(this.getSale());

        resp.setQuotationCode(this.getQuotationCode());
        resp.setSubmissionCode(this.getSubmissionCode());
        resp.setApplicationNumber(this.getApplicationNumber());
        //resp.setMicTransactionId(this.getMicTransactionId());
        resp.setPolicyNumber(this.getPolicyNumber());
        resp.setQuotationStatus(this.getQuotationStatus());
        resp.setApplicationStatus(this.getApplicationStatus());
        resp.setAppQuestionNumber(this.getAppQuestionNumber());

        resp.setCashFlow(this.getCashFlow());
        resp.setRiders(this.getRiders());
        resp.setTotalAmount(this.getTotalAmount());
        resp.setTotalAmountStr(this.getTotalAmountStr());
        resp.setTotalMicAmountStr(this.getTotalMicAmountStr());
        resp.setTotalMbalAmountStr(this.getTotalMbalAmountStr());
        resp.setAdditionalMbalAmountStr(this.getAdditionalMbalAmountStr());

        if (this.getAdditionalAssuredOutputs() != null) {
            this.getAdditionalAssuredOutputs().forEach(el -> {
                if (el.getAssured() != null) {
                    el.getAssured().setIsCustomer(isMatchedIdentify(resp.getCustomer(), el.getAssured()));
                } else {
                    throw new ApplicationException("Sản phẩm bổ trợ không tìm thấy người được bảo hiểm");
                }
            });
            resp.setAdditionalAssuredOutputs(this.getAdditionalAssuredOutputs());
        }

        resp.setDetails(this.getDetails());
        resp.setHealths(this.getHealths());
        resp.setMicHealths(this.getMicHealths());

        resp.setRiderAmountStr(this.getRiderAmountStr());
        resp.setTopupAmountStr(this.getTopupAmountStr());
        resp.setMbalMainAmountStr(this.getMbalMainAmountStr());
        resp.setInsuranceRequestId(this.getInsuranceRequestId());

        resp.setRaiderDeductFund(this.isRaiderDeductFund());
//        if (this.getAmount() != null) {
//            resp.setAmounts(List.of(this.getAmount()));
//        }
        resp.setAmounts(this.getAmounts());
//        if (this.getBeneficiary() != null) {
//            resp.setBeneficiaries(List.of(this.getBeneficiary()));
//        }
        resp.setBeneficiaries(this.getBeneficiaries());
        //resp.setMicProductModels(this.getMicProductModels());
        if (this.getRefererInput() != null) {
            resp.setRefererInput(this.getRefererInput());
        } else {
            resp.setRefererInput(this.getReferrer());
        }

        resp.setDirectInfo(this.getDirect());

        return resp;
    }
}
