package com.stg.service.impl;

import com.google.gson.Gson;
import com.stg.entity.*;
import com.stg.repository.*;
import com.stg.service.dto.external.requestFlexible.PaymentNotificationFlexibleReqDto;
import com.stg.service.dto.external.requestFlexible.ProcessFlexibleReqDto;
import com.stg.service.dto.external.responseV2.ErrorResponseMbal;
import com.stg.service3rd.mbalv2.MbalV2Api3rdService;
import com.stg.service3rd.mbalv2.dto.resp.*;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AsyncObjectImpl {

    private final LogErrorRepository logErrorRepository;
    private final ThirdPartyLogRepository thirdPartyLogRepository;
    private final InsuranceContractRepository insuranceContractRepository;
    private final InsuranceContractSyncDetailRepository contractSyncDetailRepository;
    private final InsuranceContractSyncRepository contractSyncRepository;

    @Autowired
    private final Gson gson;

    @Autowired
    private MbalV2Api3rdService mbalV2Api3rdService;

    @Async
    public void saveImportPavImportToDB(List<PavImport> batchPavImports, PavImportRepository pavImportRepository) {
        pavImportRepository.saveAll(batchPavImports);
    }

    @Async
    public void saveImportICToDB(List<ICImport> batch, ICImportRepository icImportRepository) {
        icImportRepository.saveAll(batch);
    }

    @Async
    public void saveErrorLog(Constants.HOST_PARTY hostParty, HttpMethod method, String path,
                             String reqDtoLog, int code, String detailMessage, LocalDateTime sendTime, LocalDateTime receivedTime) {
        LogError logError = new LogError();
        logError.setMethod(method.name());
        logError.setPath(path);
        logError.setPayload("".equals(reqDtoLog) ? null : reqDtoLog);
        logError.setCode(code);
        logError.setErrorMessage(detailMessage);
        logError.setSendTime(sendTime);
        logError.setReceivedTime(receivedTime);
        logError.setHostParty(hostParty.name());
        logErrorRepository.saveAndFlush(logError);
    }

    @Async
    public void saveErrorLog(String path, HttpMethod method, Object obj, String errorMessage,
                             Constants.HOST_PARTY party, LocalDateTime sendTime, LocalDateTime receivedTime) {

        String payload;
        if (obj instanceof ProcessFlexibleReqDto) {
            ((ProcessFlexibleReqDto) obj).getCustomer()
                    .setFullName(null)
                    .setDob(null)
                    .setEmail(null)
                    .setIdentificationNumber(null)
                    .setPhoneNumber(null);
            payload = gson.toJson(obj);
        } else if (obj instanceof PaymentNotificationFlexibleReqDto) {
            ((PaymentNotificationFlexibleReqDto) obj).getSource().setPhoneNumber(null);
            payload = gson.toJson(obj);
        } else {
            payload = gson.toJson(obj);
        }
        Integer code = 500;
        if (errorMessage.contains("{") && errorMessage.contains("}") && party.equals(Constants.HOST_PARTY.MBAL)) {
            String bodyMessage = errorMessage.substring(errorMessage.indexOf("{"), errorMessage.lastIndexOf("}") + 1);
            ErrorResponseMbal errorResponseMbal = gson.fromJson(bodyMessage, ErrorResponseMbal.class);
            List<String> messages = errorResponseMbal.getMessages();
            errorMessage = StringUtils.join(messages, ", ");
            code = errorResponseMbal.getStatusCode();
        }
        LogError logError = new LogError();
        logError.setMethod(method.name());
        logError.setPath(path);
        logError.setPayload(payload);
        logError.setCode(code);
        logError.setErrorMessage(errorMessage);
        logError.setSendTime(sendTime);
        logError.setReceivedTime(receivedTime);
        logError.setHostParty(party.name());
        logErrorRepository.saveAndFlush(logError);
    }

    @Async
    public void saveThirdPartyLog(ThirdPartyLog thirdPartyLog) {
        log.debug("[MINI]--Saving process log for requestId {}", thirdPartyLog.getRequestId());
        thirdPartyLogRepository.save(thirdPartyLog);
    }

    @Async
    public void asyncCalcPaymentPolicies(String policyNum) {
        log.debug("Call async CalcPaymentPolicies with policyNum = {}", policyNum);
        try {
            CalcPaymentResp calcPaymentPolicies = mbalV2Api3rdService.getCalcPaymentPolicies(policyNum);

            if (calcPaymentPolicies.getBody() == null || calcPaymentPolicies.getBody().getOuput() == null ||
                    calcPaymentPolicies.getBody().getOuput().getItem() == null || calcPaymentPolicies.getBody().getOuput().getItem().getPolicy() == null) {
                log.warn("CalcPaymentPolicies with policyNum = {}, Invalid response={}", policyNum, calcPaymentPolicies);
                return;
            }

            CalcPaymentPolicy policy = calcPaymentPolicies.getBody().getOuput().getItem().getPolicy();
            InsuranceContractSync contractSync = contractSyncRepository.findInsuranceContractSyncsByMbalPolicyNumber(policyNum);
            if (contractSync == null) {
                log.error("Not exist Contract with policy number = " + policy.getPolicyNum());
                return;
            }

            contractSync.setPolicyEffDate(DateUtil.strToLocalDateTryCatch(DateUtil.DATE_YYYY_MM_DD, policy.getPolicyEffDate()));

            List<CalcPaymentPolicy.DuePremium> duePremiums = policy.getDuePremiums();
            if (!CollectionUtils.isEmpty(duePremiums)) {
                List<InsuranceContractSyncDetail> contractSyncDetails = new ArrayList<>();
                for (CalcPaymentPolicy.DuePremium duePremium : duePremiums) {
                    InsuranceContractSyncDetail contractSyncDetail = contractSyncDetailRepository.getCurrentContractSyncDetail(contractSync.getId()
                            ,DateUtil.strToLocalDateTryCatch(DateUtil.DATE_YYYY_MM_DD, duePremium.getDueFromDate())
                            ,DateUtil.strToLocalDateTryCatch(DateUtil.DATE_YYYY_MM_DD, duePremium.getDueToDate()));

                    if (contractSyncDetail != null) {
                        log.info("[MINI]--Update exist contract detail with id: {}", contractSyncDetail.getId());
                        contractSyncDetail.setPremiumType(duePremium.getPremiumType());
                        contractSyncDetail.setLastUpdated(LocalDateTime.now());
                        contractSyncDetails.add(contractSyncDetail);
                        continue;
                    }

                    contractSyncDetail = new InsuranceContractSyncDetail();
                    contractSyncDetail.setInsuranceContractSync(contractSync);
                    contractSyncDetail.setProductId(policy.getProductId());
                    contractSyncDetail.setProductName(policy.getProductName());
                    contractSyncDetail.setInquiryDate(DateUtil.strToLocalDateTryCatch(DateUtil.DATE_YYYY_MM_DD, policy.getInquiryDate()));
                    contractSyncDetail.setPolicyEffDate(DateUtil.strToLocalDateTryCatch(DateUtil.DATE_YYYY_MM_DD, policy.getPolicyEffDate()));
                    contractSyncDetail.setPayfreqText(policy.getPayfreqText());
                    contractSyncDetail.setPeriodicPrem(policy.getPeriodicPrem());
                    contractSyncDetail.setFeeAmt(policy.getFeeAmt());
                    contractSyncDetail.setOveDueAmt(policy.getOverDueAmt());
                    contractSyncDetail.setSuspenseAmt(policy.getSuspenseAmt());
                    contractSyncDetail.setPaymentAmt(policy.getPaymentAmt());
                    contractSyncDetail.setPaymentMinAmt(policy.getPaymentMinAmt());
                    contractSyncDetail.setInsurDuration(policy.getInsurDuration());
                    contractSyncDetail.setInsuredName(policy.getInsuredName());
                    contractSyncDetail.setPremiumType(duePremium.getPremiumType());
                    contractSyncDetail.setDueFromDate(DateUtil.strToLocalDate(DateUtil.DATE_YYYY_MM_DD, duePremium.getDueFromDate()));
                    contractSyncDetail.setDueToDate(DateUtil.strToLocalDate(DateUtil.DATE_YYYY_MM_DD, duePremium.getDueToDate()));
                    contractSyncDetail.setDueAmount(duePremium.getDueAmount() != null ? new BigDecimal(duePremium.getDueAmount()) : BigDecimal.ZERO);
                    contractSyncDetail.setInsuredBp(null);//TODO
                    contractSyncDetail.setInsuredDob(null);//TODO
                    contractSyncDetail.setMinTopUp(policy.getMinTopup() == null ? BigDecimal.ZERO : new BigDecimal(policy.getMinTopup()));
                    contractSyncDetail.setMaxTopUp(policy.getMaxTopup() == null ? BigDecimal.ZERO : new BigDecimal(policy.getMaxTopup()));
                    contractSyncDetails.add(contractSyncDetail);
                }
                contractSyncDetailRepository.saveAll(contractSyncDetails);
            }
            contractSyncRepository.save(contractSync);
        } catch (Exception e) {
            log.error("Error when calling async CalcPaymentPolicies with policyNum = {}", policyNum);
        }
    }

}
