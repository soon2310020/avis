package com.stg.utils.quotation;

import com.stg.entity.mic.MicAdditionProduct;
import com.stg.entity.mic.MicInsuranceContract;
import com.stg.entity.quotation.QuotationAmount;
import com.stg.entity.quotation.QuotationCustomer;
import com.stg.entity.quotation.QuotationDetail;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.entity.quotation.QuotationProduct;
import com.stg.entity.quotation.QuotationSupporter;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.errors.quote.CreateQuoteException;
import com.stg.service.dto.mbal.AmountType;
import com.stg.service.dto.mbal.QuotationModel;
import com.stg.service.dto.mbal.QuotationModelYearItem;
import com.stg.service.dto.quotation.QuotationAmountDto;
import com.stg.service.dto.quotation.QuotationAssuredDto;
import com.stg.service.dto.quotation.QuotationCustomerDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service.dto.quotation.QuotationProductDto;
import com.stg.service3rd.mbal.constant.ProductType;
import com.stg.service3rd.mbal.dto.*;
import com.stg.service3rd.mbal.dto.req.FlexibleQuoteReq;
import com.stg.utils.DateUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.stg.constant.CommonMessageError.FL_MSG37;
import static com.stg.service3rd.mbal.constant.AssuredType.LIFE_ASSURED;
import static com.stg.utils.DateUtil.DATE_YYYY_MM_DD;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteMapperUtil {
    public static final String MBAL_API_REQUEST_FAILED = "MBAL API request failed";

    public static List<QuotationDetail> quotationModelToEntity(QuotationModel model, QuotationHeader header) {

        if (model.getCashFlow() == null) {
            throw new ApplicationException(MBAL_API_REQUEST_FAILED, new ErrorDto(HttpStatus.BAD_REQUEST, "Quotation not found"));
        }

        return model.getCashFlow().getYearItems().stream().map(item -> {
            QuotationDetail detail = new QuotationDetail();

            detail.setPolicyYear(item.getPolicyYear());
            detail.setInsuredAge(item.getInsuredAge());
            detail.setBasePremium(item.getBasePremium());
            detail.setTopupPremium(item.getTopupPremium());
            detail.setWithdrawal(item.getWithdrawal());
            buildQuotationDetailWithInterestRate(item, detail);
            buildQuotationDetailWithInvestment(item, detail);

            detail.setQuotationHeader(header);
            return detail;
        }).collect(Collectors.toList());

    }

    private static void buildQuotationDetailWithInvestment(QuotationModelYearItem item, QuotationDetail detail) {
        if (item.getBenefitByInvestmentRate() != null) {
            if (item.getBenefitByInvestmentRate().getLowRate() != null) {
                detail.setLowRateDeathBenefit(item.getBenefitByInvestmentRate().getLowRate().getDeathBenefit());
                detail.setLowRateLoyaltyBonus(item.getBenefitByInvestmentRate().getLowRate().getLoyaltyBonus());
                detail.setLowRateAccountValue(item.getBenefitByInvestmentRate().getLowRate().getAccountValue());
                detail.setLowRateSurenderValue(item.getBenefitByInvestmentRate().getLowRate().getSurenderValue());
            }
            if (item.getBenefitByInvestmentRate().getHighRate() != null) {
                detail.setHighRateDeathBenefit(item.getBenefitByInvestmentRate().getHighRate().getDeathBenefit());
                detail.setHighRateLoyaltyBonus(item.getBenefitByInvestmentRate().getHighRate().getLoyaltyBonus());
                detail.setHighRateAccountValue(item.getBenefitByInvestmentRate().getHighRate().getAccountValue());
                detail.setHighRateSurenderValue(item.getBenefitByInvestmentRate().getHighRate().getSurenderValue());
            }
        }
    }

    private static void buildQuotationDetailWithInterestRate(QuotationModelYearItem item, QuotationDetail detail) {
        if (item.getAccountByInterestRate() != null) {
            if (item.getAccountByInterestRate().getSelectedRate() != null) {
                detail.setSelectedRateBaseValue(item.getAccountByInterestRate().getSelectedRate().getBaseAccountValue());
                detail.setSelectedRateTopupValue(item.getAccountByInterestRate().getSelectedRate().getTopupAccountValue());
                detail.setSelectedRateAccountValue(item.getAccountByInterestRate().getSelectedRate().getAccountValue());
                detail.setSelectedRateSurenderValue(item.getAccountByInterestRate().getSelectedRate().getSurenderValue());
            }
            if (item.getAccountByInterestRate().getCommittedRate() != null) {
                detail.setCommittedRateBaseValue(item.getAccountByInterestRate().getCommittedRate().getBaseAccountValue());
                detail.setCommittedRateTopupValue(item.getAccountByInterestRate().getCommittedRate().getTopupAccountValue());
                detail.setCommittedRateAccountValue(item.getAccountByInterestRate().getCommittedRate().getAccountValue());
                detail.setCommittedRateSurenderValue(item.getAccountByInterestRate().getCommittedRate().getSurenderValue());
            }

        }
    }

    public static QuotationHeader quotationHeaderToEntity(QuotationHeaderDto dto) {
        QuotationHeader entity = new QuotationHeader();

        entity.setProcessId(dto.getProcessId());
        entity.setType(dto.getType());
        entity.setPackageBenefitType(dto.getPackageBenefitType());
        entity.setPackagePolicyTerm(dto.getPackagePolicyTerm());
        entity.setPackagePremiumTerm(dto.getPackagePremiumTerm());
        entity.setPackagePaymentPeriod(dto.getPackagePaymentPeriod());
        entity.setPackageSumAssured(dto.getPackageSumAssured());
        entity.setPackagePeriodicPremium(dto.getPackagePeriodicPremium());
        entity.setPackageDiscountCode(dto.getDiscountCode());
        entity.setRaiderDeductFund(dto.isRaiderDeductFund());

        QuotationCustomer customer = quotationCustomerToEntity(dto.getCustomer());
        entity.setCustomer(customer);

        QuotationCustomer assured = dto.isCustomerIsAssured() ? customer : quotationCustomerToEntity(dto.getAssured());
        assured.setQuotationHeader(entity);

        List<QuotationCustomer> assureds = new ArrayList<>();
        assureds.add(assured);

        if (dto.getAdditionalAssureds() != null) {
            assureds.addAll(dto.getAdditionalAssureds().stream().map(a -> {
                QuotationCustomer additionalAssured = quotationCustomerToEntity(a);
                additionalAssured.setQuotationHeader(entity);
                return additionalAssured;
            }).collect(Collectors.toList()));
        }

        entity.setAssureds(assureds);

        if (dto.getAmount() != null) {
            QuotationAmount amount = quotationAmountToEntity(dto.getAmount());
            amount.setQuotationHeader(entity);
            entity.setAmounts(List.of(amount));
        }

        entity.setSearchName(entity.getCustomer().getFullName());
        entity.setSearchPhoneNumber(entity.getCustomer().getPhoneNumber());

        if (dto.getSale() != null && StringUtils.hasText(dto.getSale().getCode())) {
            QuotationSupporter sale = new QuotationSupporter();
            sale.setCode(dto.getSale().getCode());
            sale.setName(dto.getSale().getName());
            entity.setSale(sale);
        }
        if (dto.getReferrer() != null && StringUtils.hasText(dto.getReferrer().getCode())) {
            QuotationSupporter referrer = new QuotationSupporter();
            referrer.setCode(dto.getReferrer().getCode());
            referrer.setName(dto.getReferrer().getName());
            entity.setReferrer(referrer);
        }
        if (dto.getSupporter() != null && StringUtils.hasText(dto.getSupporter().getCode())) {
            QuotationSupporter supporter = new QuotationSupporter();
            supporter.setCode(dto.getSupporter().getCode());
            supporter.setName(dto.getSupporter().getName());
            entity.setSupporter(supporter);
        }

        return entity;
    }

    private static QuotationCustomer quotationCustomerToEntity(QuotationCustomerDto dto) {

        QuotationCustomer entity = new QuotationCustomer();

        entity.setFullName(dto.getFullName());
        entity.setDob(dto.getDob());
        entity.setGender(dto.getGender());
        entity.setOccupationId(dto.getOccupationId());
        entity.setMarried(dto.getMarried());
        entity.setIdentificationType(dto.getIdentificationType());
        entity.setIdentificationId(dto.getIdentificationId());
        entity.setPhoneNumber(dto.getPhoneNumber());

        if (dto instanceof QuotationAssuredDto) {
            QuotationAssuredDto assured = (QuotationAssuredDto) dto;

            if (assured.getAdditionalProducts() != null) {
                entity.setAdditionalProducts(assured.getAdditionalProducts().stream().map(p -> {
                    QuotationProduct product = quotationProductToEntity(p);
                    product.setQuotationCustomer(entity);
                    return product;
                }).collect(Collectors.toList()));
            }

            entity.setEmail(assured.getEmail());
            setAddressToEntity(entity, assured.getAddress());
        }

        return entity;
    }

    private static void setAddressToEntity(QuotationCustomer customer, FlexibleCommon.Address address) {
        if (address == null) {
            return;
        }
        customer.setAddressLine1(address.getLine1());
        customer.setAddressWardName(address.getWardName());
        customer.setAddressDistrictName(address.getDistrictName());
        customer.setAddressProvinceName(address.getProvinceName());
    }

    private static QuotationProduct quotationProductToEntity(QuotationProductDto dto) {

        QuotationProduct entity = new QuotationProduct();

        entity.setType(dto.getType());
        entity.setPolicyTerm(dto.getPolicyTerm());
        entity.setPremiumTerm(dto.getPremiumTerm());
        entity.setSumAssured(dto.getSumAssured());

        return entity;
    }

    private static QuotationAmount quotationAmountToEntity(QuotationAmountDto dto) {

        QuotationAmount entity = new QuotationAmount();

        entity.setType(AmountType.TOP_UP);
        entity.setValue(dto.getValue());
        entity.setStartYear(dto.getStartYear());
        entity.setEndYear(dto.getEndYear());

        return entity;
    }

    public static MicInsuranceContract micInsuranceContractToEntity(QuoteAssuredOutput micProductInfo) {
        MicInsuranceContract entity = new MicInsuranceContract();

        MicSandboxFeeCareRespDto micResult = micProductInfo.getMicResult();
        entity.setTransactionId(micResult.getMicTransactionId());
        entity.setStatus(micResult.getMessage());

        entity.setGcn(micResult.getGcn());
        entity.setSoId(micResult.getSoId());
        entity.setPhi(micResult.getPhi());
        entity.setSumBenefit(micResult.getMicSumBenefit());
        entity.setFile(micResult.getFile());
        entity.setAdditionProduct(micAdditionProductToEntity(micProductInfo));

        return entity;
    }

    private static MicAdditionProduct micAdditionProductToEntity(QuoteAssuredOutput micProductInfo) {
        MicAdditionProduct entity = new MicAdditionProduct();

        MicAdditionalProduct micRequest = micProductInfo.getMicRequest();
        entity.setNhom(micRequest.getNhom());

        entity.setMaSp(micRequest.getMaSp());
        entity.setBs1(micRequest.getBs1());
        entity.setBs2(micRequest.getBs2());
        entity.setBs3(micRequest.getBs3());
        entity.setBs4(micRequest.getBs4());
        return entity;
    }


    /***/
    public static FlexibleQuoteReq genFlexibleQuoteReq(QuotationHeaderDto quotationDto, FlexibleCommon.Assured customer,
                                                       List<FlexibleCommon.Assured> mbalAssureds,
                                                       List<MbalAdditionalProductInput> mbalProducts) {
        for (MbalAdditionalProductInput product : mbalProducts) {
            FlexibleCommon.Assured assured = mbalAssureds.get(product.getAssuredIndex());

            if (ProductType.PWR.equals(product.getProductType())
                    && (Boolean.FALSE.equals(assured.getIsCustomer()) || assured.getType().equals(LIFE_ASSURED))) {
                throw new CreateQuoteException(FL_MSG37);
            }
        }

        QuoteUtil.validateAdditionalProducts(mbalProducts);

        FlexibleQuoteReq reqDto = new FlexibleQuoteReq();
        reqDto.setAssureds(mbalAssureds);
        reqDto.setAdditionalProducts(mbalProducts);
        reqDto.setSale(quotationDto.getSale());
        reqDto.setProcessId(quotationDto.getProcessId());

        if (quotationDto.getSupporter() != null) {
            reqDto.setSupporter(quotationDto.getSupporter().to3rdInput());
        }
        if (quotationDto.getReferrer() != null) {
            reqDto.setReferrer(quotationDto.getReferrer().to3rdInput());
        }

        reqDto.setQuotationDate(DateUtil.localDateTimeToString(DATE_YYYY_MM_DD, LocalDateTime.now()));
        reqDto.setCustomer(new FlexibleCommon.CustomerInfo(customer));
        reqDto.setProductPackage(new FlexibleCommon.ProductPackageInput(quotationDto));
        reqDto.setRaiderDeductFund(quotationDto.isRaiderDeductFund());
        reqDto.setBeneficiaries(quotationDto.getBeneficiary() == null ? new ArrayList<>() : List.of(quotationDto.getBeneficiary()));
        reqDto.setAmounts(quotationDto.getAmount() == null ? new ArrayList<>() : List.of(quotationDto.getAmount()));

        return reqDto;
    }
}
