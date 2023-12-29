package com.stg.service.impl;

import com.google.gson.Gson;
import com.stg.entity.InsuranceContractSync;
import com.stg.entity.customer.Customer;
import com.stg.errors.CustomerNotFoundException;
import com.stg.errors.MbalApiException;
import com.stg.errors.ValidationException;
import com.stg.repository.CustomerRepository;
import com.stg.repository.InsuranceContractSyncRepository;
import com.stg.service.ExternalPolicyApiService;
import com.stg.service.dto.external.responseV2.PolicyMbalResponse;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.service.dto.policy.InquiryPolicyDataCache;
import com.stg.service.dto.policy.InquiryPolicySaveReq;
import com.stg.service.dto.policy.InquiryPolicySaveResp;
import com.stg.service3rd.mbalv2.MbalV2Api3rdService;
import com.stg.service3rd.mbalv2.dto.resp.CalcPaymentOuput;
import com.stg.service3rd.mbalv2.dto.resp.CalcPaymentPolicy;
import com.stg.service3rd.mbalv2.dto.resp.CalcPaymentResp;
import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentPolicy;
import com.stg.utils.DateUtil;
import com.stg.utils.PaymentPeriod;
import com.stg.utils.SourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.stg.utils.Common.buildKeyPolicy;
import static com.stg.utils.Common.formatCurrency;
import static com.stg.utils.CommonMessageError.MSG12;
import static com.stg.utils.CommonMessageError.MSG39;
import static com.stg.utils.CommonMessageError.MSG40;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ExternalPolicyApiServiceImpl implements ExternalPolicyApiService {

    @Value("${spring.redis.cache.external-v2api-service.mbal-send-mail-expire-time}")
    public int mbalSendMailExpireTime;
    @Value("${spring.redis.cache.external-v2api-service.mbal-policy-detail-expire-time}")
    public int mbalPolicyDetailExpiretime;
    @Value("${spring.redis.cache.external-v2api-service.mbal-create-quote-expire-time}")
    public int mbalCreateQuoteExpireTime;
    @Value("${spring.redis.cache.external-v2api-service.mbal-process-expire-time}")
    public int mbalProcessExpireTime;

    private final InsuranceContractSyncRepository contractSyncRepository;

    private final CustomerRepository customerRepository;

    @Autowired
    private final AsyncObjectImpl asyncObject;

    @Autowired
    private final Gson gson;

    @Autowired
    private MbalV2Api3rdService mbalV2Api3rdService;

    @Autowired
    private final CommonService commonService;

    /**
     * Đồng bộ by identification
     */
    @Override
    public PolicyMbalResponse syncPolicyMbal(String mbId) {
        log.debug("[MINI]--Start Sync Policy Mbal with mbId=" + mbId);
        Customer customer = getCustomerByMbId(mbId);
        String identification = customer.getIdentification();
        PolicyMbalResponse policyMbalResponse = new PolicyMbalResponse();
        try {
            List<InquiryPaymentPolicy> policies = mbalV2Api3rdService.getInquiryPaymentPolicies(identification);

            savePolicy(policies, customer);

//          async call mbal api to get fee
            for (InquiryPaymentPolicy policy : policies) {
                asyncObject.asyncCalcPaymentPolicies(policy.getPolicyNum());
            }
        } catch (Exception e) {
            log.error("[MINI] Sync contract got error {}", e.getMessage());
            policyMbalResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            policyMbalResponse.setStrVi(MSG12);
            return policyMbalResponse;
        }
        log.info("Done getListPolicyMbal with mbId=" + mbId);
        policyMbalResponse.setCode(HttpStatus.OK.value());
        policyMbalResponse.setStrVi("Thành công");
        return policyMbalResponse;
    }

    /**
     * Truy vấn by identification
     */
    @Override
    public InsuranceContractsAppDto inquiryMbalPolicy(String mbId, String appNumber, String policyNumber) {
        log.info("Starting inquiryMbalPolicy with appNumber={}, policyNumber={}, mbId={}", appNumber, policyNumber, mbId);

        if (StringUtils.isEmpty(appNumber) && StringUtils.isEmpty(policyNumber)) {
            throw new ValidationException("Vui lòng nhập thông tin tìm kiếm");
        }

        InsuranceContractsAppDto contractDto = new InsuranceContractsAppDto();
        Customer customer = getCustomerByMbId(mbId); // always != null

        String identification = customer.getIdentification();
        try {
            // inquiry by policy
            if (StringUtils.isNotEmpty(policyNumber)) {
                log.debug("Truy vấn policyNumber = {}", policyNumber);
                List<InquiryPaymentPolicy> policies = mbalV2Api3rdService.getInquiryPaymentPolicies(identification);

                // verify quyền xem dữ liệu
                InquiryPaymentPolicy inquiryPolicy = policies.stream()
                        .filter(o -> o != null && o.getPolicyNum() != null && o.getPolicyNum().contains(policyNumber))
                        .findAny().orElseThrow(() -> new MbalApiException(MSG40));

                CalcPaymentResp calcPaymentPolicies = mbalV2Api3rdService.getCalcPaymentPolicies(policyNumber);
                CalcPaymentOuput calcPayment = calcPaymentPolicies.getBody().getOuput();
                CalcPaymentPolicy policy = calcPayment.getItem().getPolicy();
                if (Objects.isNull(policy)) {
                    throw new MbalApiException(MSG39);
                }
                contractDto.setPolicyholderName(customer.getFullName());
                contractDto.setMbalProductName(inquiryPolicy.getPrdName());
                contractDto.setMbalPolicyNumber(inquiryPolicy.getPolicyNum());
                contractDto.setPeriodicPrem(inquiryPolicy.getPeriodicPrem() == null ? "" : formatCurrency(new BigDecimal(inquiryPolicy.getPeriodicPrem().trim()))); //Phí bảo hiểm định kỳ
                contractDto.setMbalPeriodicFeePaymentTime(inquiryPolicy.getPayFrequency()); //định kỳ đóng phí mbal
                contractDto.setAdditionalFee(policy.getMinTopup() == null ? "" : formatCurrency(new BigDecimal(policy.getMinTopup().trim()))); // Phí đóng thêm

                if (policy.getMinTopup() != null && inquiryPolicy.getPeriodicPrem() != null) {
                    try {
                        BigDecimal additionalFee = new BigDecimal(policy.getMinTopup().trim());
                        BigDecimal periodicPrem = new BigDecimal(inquiryPolicy.getPeriodicPrem().trim());
                        contractDto.setStrInsuranceFee(formatCurrency(additionalFee.add(periodicPrem)));// Tổng phí thanh toán
                    } catch (Exception ex) {
                        log.error(String.format("Không thể parse được phí MinTopup = %s, PeriodicPrem%s", policy.getMinTopup(), inquiryPolicy.getPeriodicPrem()));
                    }
                }
                contractDto.setPaid(false);
                contractDto.setCurrency(policy.getCurrency());
                contractDto.setStatus(inquiryPolicy.getPolicyStatus());
                contractDto.setAgentCode(inquiryPolicy.getAgentCode());
                contractDto.setAgentName(inquiryPolicy.getAgentName());
                contractDto.setAgentNum(inquiryPolicy.getAgentNum());
                contractDto.setPhName(inquiryPolicy.getPhName());

                String keyPolicy = buildKeyPolicy(mbId, appNumber, policyNumber);
                // lưu cache policy trong 30 minutes cho KH kiểm tra policy và quyết định có lưu lại k
                commonService.setProcessCacheFlexibleTime(keyPolicy, gson.toJson(new InquiryPolicyDataCache()
                        .setInquiryPolicy(inquiryPolicy)
                        .setPolicy(policy)), mbalPolicyDetailExpiretime);
            }
            // TODO: inquiry by appNumber: phase 2
          /*  else if (StringUtils.isNotEmpty(appNumber)) {
                log.debug("Truy vấn appNumber = {}", appNumber);
                CalcPaymentEAppResp calcPaymentEApp = mbalV2Api3rdService.getCalcPaymentEApp(appNumber);
                if (calcPaymentEApp == null
                        || calcPaymentEApp.getBody() == null
                        || calcPaymentEApp.getBody().getOutput() == null) {
                    throw new MbalApiException(MSG34);
                }
                CalcPaymentEAppOutput eApp = calcPaymentEApp.getBody().getOutput();
                hasData = true;
                // verify quyền xem dữ liệu
                if (eApp.getHolderName() == null ||
                        !removeAccentVN(eApp.getHolderName().trim()).equalsIgnoreCase(customer.getFullNameT24()) ||
                        eApp.getHolderDob() == null
                        || !eApp.getHolderDob().trim().equals(DateUtil.localDateTimeToString(DateUtil.DATE_DMY, customer.getBirthday()))) {
                    log.warn("Không tìm thấy hợp đồng của GTTT " + identification);
                    throw new MbalApiException(MSG39);
                }
                contractDto.setPolicyholderName(eApp.getHolderName());
                contractDto.setMbalProductName(eApp.getProductName());
                contractDto.setMbalAppNo(eApp.getPagNoId());
                contractDto.setPeriodicPrem(eApp.getPremafterTaxAm() == null ? "" : formatCurrency(new BigDecimal(eApp.getPremafterTaxAm().trim()))); //Phí bảo hiểm định kỳ
                contractDto.setMbalPeriodicFeePaymentTime(convertMbalPeriodicFeePaymentTime(eApp.getPaymentFrequency())); //định kỳ đóng phí mbal
                contractDto.setAdditionalFee(eApp.getTopUp() == null ? "" : formatCurrency(new BigDecimal(eApp.getTopUp().trim()))); // Phí đóng thêm
                contractDto.setStrInsuranceFee(eApp.getTotalPayAM() == null ? "" : formatCurrency(new BigDecimal(eApp.getTotalPayAM().trim())));// Tổng phí thanh toán

                contractDto.setStatus(eApp.getStatus()); // TODO: cần convert status 1 là gì?

                contractDto.setInsuredBp(eApp.getInsuredBp());
                contractDto.setInsuredName(eApp.getInsuredName());
                contractDto.setInsuredDob(eApp.getInsuredDob());

                String keyPolicy = buildKeyPolicy(mbId, appNumber, policyNumber);
                // lưu cache policy trong 30 minutes cho KH kiểm tra policy và quyết định có lưu lại k
                commonService.setProcessCacheFlexibleTime(keyPolicy, gson.toJson(contractDto), mbalPolicyDetailExpiretime);

                commonService.setProcessCacheFlexibleTime(keyPolicy, gson.toJson(new InquiryPolicyDataCache()
                                .setInquiryPolicy(null)
                                .setPolicy(null)
                                .setPaymentEAppOutput(eApp)),
                        mbalPolicyDetailExpiretime);
            }*/
        } catch (Exception e) {
            log.error("[MINI]--Exception when get contract from MBAL {}, detail message {}", policyNumber, e.getMessage());
            if (MSG40.equals(e.getMessage())) {
                throw new MbalApiException(MSG40);
            }
            throw new MbalApiException(MSG39);
        }
        return contractDto;
    }

    @Override
    public InquiryPolicySaveResp savePolicyMbal(InquiryPolicySaveReq inquiryPolicySaveReq) {
        log.info("[MINI]--Starting savePolicyMbal with appNumber={}, policyNumber={}, mbId={}", inquiryPolicySaveReq.getAppNumber(), inquiryPolicySaveReq.getPolicyNumber(), inquiryPolicySaveReq.getMbId());
        if (StringUtils.isEmpty(inquiryPolicySaveReq.getPolicyNumber()) && StringUtils.isEmpty(inquiryPolicySaveReq.getAppNumber())) {
            throw new ValidationException("Thông tin Hồ sơ, Hợp đồng trống");
        }

        // todo... save hsycbh: phase 2
        try {
            String policyDetailStr = commonService.getProcessCacheData(buildKeyPolicy(inquiryPolicySaveReq.getMbId(), inquiryPolicySaveReq.getAppNumber(), inquiryPolicySaveReq.getPolicyNumber()));
            InquiryPolicyDataCache inquiryPolicyDataCache = gson.fromJson(policyDetailStr, InquiryPolicyDataCache.class);
            if (inquiryPolicyDataCache == null || inquiryPolicyDataCache.getInquiryPolicy() == null) {
                throw new ValidationException("Không tìm thấy hợp đồng này hoặc dữ liệu đã hết hạn. Vui lòng truy vấn lại thông tin hợp đồng!");
            }

            String policyNumber = inquiryPolicyDataCache.getInquiryPolicy().getPolicyNum();
            Customer customer = getCustomerByMbId(inquiryPolicySaveReq.getMbId());
            InsuranceContractSync contractSync;

            contractSync = contractSyncRepository.findInsuranceContractSyncsByMbalPolicyNumber(policyNumber);
            if (contractSync == null) {
                // Chua ton tai du lieu trong sync table
                // saving InsuranceContractSync and InsuranceContractSyncDetail
                log.info("[MINI] Saving new contract sync with PolicyNumber = {}", inquiryPolicySaveReq.getPolicyNumber());
                contractSync = genContractSync(customer, inquiryPolicyDataCache.getInquiryPolicy());
                if (inquiryPolicyDataCache.getPolicy() != null) {
                    contractSync.setPolicyEffDate(DateUtil.strToLocalDateTryCatch(DateUtil.DATE_YYYY_MM_DD,
                            inquiryPolicyDataCache.getPolicy().getPolicyEffDate()));
                }
                contractSync = contractSyncRepository.save(contractSync);
            } else {
                // todo.. update... : phase 2
            }

            return new InquiryPolicySaveResp(contractSync.getId());
        } catch (Exception e) {
            throw new MbalApiException(MSG12, e);
        }
    }

    private String convertMbalPeriodicFeePaymentTime(String period) {
        if (period == null) {
            return "";
        }
        period = period.toUpperCase().trim();
        if (PaymentPeriod.ANNUAL.name().equals(period) || "ANNUALLY".equals(period)) {
            return "Hàng năm";
        } else if (PaymentPeriod.SEMI_ANNUAL.name().equals(period) || "SEMI_ANNUALLY".equals(period)) {
            return "Nửa năm";
        } else if (PaymentPeriod.QUARTERLY.name().equals(period)) {
            return "Hàng quý";
        } else if (PaymentPeriod.SINGLE.name().equals(period)) {
            return "1 lần";
        }
        return period;
    }

    private Customer getCustomerByMbId(String mbId) {
        Customer customerByMbId = customerRepository.findByMbId(mbId);
        if (customerByMbId == null) {
            log.warn("Không tồn tại Khách hàng có MbId là " + mbId);
            throw new CustomerNotFoundException("Không tìm thấy thông tin khách hàng");
        }
        return customerByMbId;
    }

    private void savePolicy(List<InquiryPaymentPolicy> policies, Customer customer) {
        List<InsuranceContractSync> contractSyncs = new ArrayList<>();
        for (InquiryPaymentPolicy paymentPolicy : policies) {
            InsuranceContractSync contractSync = contractSyncRepository.findInsuranceContractSyncsByMbalPolicyNumber(paymentPolicy.getPolicyNum());
            if (contractSync == null) {
                // Chua ton tai du lieu trong sync table
                log.info("[MINI] Sync not exist contract with PolicyNumber = {}", paymentPolicy.getPolicyNum());
                contractSync = genContractSync(customer, paymentPolicy);
                contractSyncs.add(contractSync);
            }
        }
        contractSyncRepository.saveAll(contractSyncs);
    }

    private static InsuranceContractSync genContractSync(Customer customer, InquiryPaymentPolicy paymentPolicy) {
        InsuranceContractSync contractSync = new InsuranceContractSync();
        contractSync.setCustomer(customer);
        contractSync.setMbalPolicyNumber(paymentPolicy.getPolicyNum());
        contractSync.setPolicyStatus(paymentPolicy.getPolicyStatus());
        contractSync.setPrdName(paymentPolicy.getPrdName());
        contractSync.setCurrency(paymentPolicy.getCurrency());
        contractSync.setSource(SourceType.MBAL);
        contractSync.setAgentCode(paymentPolicy.getAgentCode());
        contractSync.setAgentName(paymentPolicy.getAgentName());
        contractSync.setAgentNum(paymentPolicy.getAgentNum());
        contractSync.setPayFrequency(paymentPolicy.getPayFrequency());
        contractSync.setPolicyStatusId(paymentPolicy.getPolicyStatusId());
        contractSync.setPeriodicPrem(paymentPolicy.getPeriodicPrem());
        contractSync.setPhName(paymentPolicy.getPhName());
        return contractSync;
    }

}