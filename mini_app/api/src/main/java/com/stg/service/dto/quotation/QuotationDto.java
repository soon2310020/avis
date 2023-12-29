package com.stg.service.dto.quotation;

import com.google.gson.Gson;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.quotation.QuotationAmount;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.service3rd.mbal.constant.ApplicationStatus;
import com.stg.service3rd.mbal.constant.QuotationStatus;
import com.stg.service3rd.mbal.dto.AdditionalAssuredOutput;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.QuotationAmountRespDto;
import com.stg.service3rd.mbal.dto.req.FlexibleQuoteReq;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.TypeToken;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq.PRODUCT_DEFAULT;

@Getter
@Setter
@NoArgsConstructor
public class QuotationDto extends QuotationHeaderDto {

    private List<QuotationDetailDto> details;
    private Integer appQuestionNumber;
    private String healthsTxt;

    // more...
    private String productPackageCode;
    private FlexibleCommon.PolicyHolderAndLifeAssured policyHolder;// customer
    private FlexibleCommon.PolicyHolderAndLifeAssured lifeAssured; // người BH chính và các gói BH đi kèm
    private List<FlexibleCommon.PolicyHolderAndLifeAssured> assureds;

    private FlexibleCommon.ProductPackageOutput productPackage; // gói BH chính
    private String applicationNumber;
    private String quotationCode;
    private String quotationDate;
    private String submissionCode;
    private String policyNumber;
    private QuotationStatus quotationStatus;
    private ApplicationStatus applicationStatus;

    private FlexibleCommon.CashFlow cashFlow;
    private List<FlexibleCommon.Rider> riders;

    private BigDecimal totalAmount;
    private String totalAmountStr;
    private String totalMicAmountStr;
    private String totalMbalAmountStr;
    private String additionalMbalAmountStr;

    private List<AdditionalAssuredOutput> additionalAssuredOutputs; // Người bổ sung và các gói đi kèm của người bổ sung

    private String riderAmountStr;
    private String topupAmountStr;
    private String mbalMainAmountStr;
    private Long insuranceRequestId;
    private FlexibleCommon.ReferrerInput refererInput;

    //private FlexibleCommon.Referer referer;
    private List<QuotationAmountRespDto> amounts;
    private List<FlexibleCommon.BeneficiaryOutput> beneficiaries;
    //private List<MicAdditionalProductModel> micProductModels;
    private LocalDateTime createdDate;

    private FlexibleQuoteReq createQuoteReq;

    private DirectInfo direct;

    //@NotNull /* when customerIsAssured = true */
    private QuotationAssuredDto assured;
    private boolean customerIsAssured;

    public QuotationDto(QuotationHeader header) {
        this(header, null);
    }

    public QuotationDto(QuotationHeader header, PotentialCustomerDirect entityDirect) {
        setId(header.getId());
        setProcessId(header.getProcessId());
        setUuid(header.getUuid());
        setType(header.getType());
        setState(header.getState());
        setPackageBenefitType(header.getPackageBenefitType());
        setPackagePolicyTerm(header.getPackagePolicyTerm());
        setPackagePremiumTerm(header.getPackagePremiumTerm());
        setPackagePaymentPeriod(header.getPackagePaymentPeriod());
        setPackageSumAssured(header.getPackageSumAssured());
        setPackagePeriodicPremium(header.getPackagePeriodicPremium());

        setDiscountCode(header.getPackageDiscountCode());

        setRaiderDeductFund(header.getRaiderDeductFund());

        if (header.getAssureds().get(0).getId().equals(header.getCustomer().getId())) {
            setCustomerIsAssured(true);
        } else {
            setCustomer(new QuotationAssuredDto(header.getCustomer()));
        }

        setAssured(new QuotationAssuredDto(header.getAssureds().get(0)));

        if (header.getAssureds().size() > 1) {
            setAdditionalAssureds(header.getAssureds().subList(1, header.getAssureds().size()).stream()
                    .map(QuotationAssuredDto::new).collect(Collectors.toList()));
        }

        if (header.getAmounts() != null && !header.getAmounts().isEmpty()) {
            QuotationAmount amount = header.getAmounts().get(0);
            setAmount(new QuotationAmountDto(amount.getType(), amount.getValue(), amount.getStartYear(), amount.getEndYear()));
        }

        if (header.getDetails() != null) {
            setDetails(header.getDetails().stream().map(QuotationDetailDto::new).collect(Collectors.toList()));
        }

        if (StringUtils.hasText(header.getHealths())) {
            List<FlexibleSubmitQuestionInput> healths = new Gson().fromJson(header.getHealths(), new TypeToken<List<FlexibleSubmitQuestionInput>>() {
            }.getType());
            this.setHealths(healths);
        }

        if (header.getSale() != null) {
            this.setSale(new FlexibleCommon.Sale(header.getSale().getCode(), header.getSale().getName()));
        }
        if (header.getReferrer() != null) {
            this.setReferrer(header.getReferrer().toDto());
        }
        if (header.getSupporter() != null) {
            this.setSupporter(header.getSupporter().toDto());
        }
        this.setCreatedDate(header.getCreatedDate());

        if (entityDirect != null) {
            this.setDirect(new DirectInfo(entityDirect.getId(), entityDirect.getLeadId(),
                    PRODUCT_DEFAULT, entityDirect.getPotentialCustomer().getNote()));
        }
    }

}
