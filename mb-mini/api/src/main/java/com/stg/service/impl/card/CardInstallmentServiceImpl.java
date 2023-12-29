package com.stg.service.impl.card;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.adapter.dto.CommonAdapterResponse;
import com.stg.adapter.service.CallCardPartnerAdapterIService;
import com.stg.entity.InsurancePayment;
import com.stg.errors.BaasApiException;
import com.stg.errors.InstallmentApiException;
import com.stg.repository.CustomerRepository;
import com.stg.repository.InsurancePaymentRepository;
import com.stg.service.card.CardInstallmentService;
import com.stg.service.dto.baas.ManualRequest;
import com.stg.service.dto.baas.InstallmentManualResp;
import com.stg.service.dto.card.*;
import com.stg.service.lock.PaymentLockService;
import com.stg.utils.InstallmentPopup;
import com.stg.utils.enums.ErrorCodeCardPartner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stg.utils.Common.formatCurrency;
import static com.stg.utils.Common.generateUUIDId;
import static com.stg.utils.CommonMessageError.INSTALLMENT_MSG01;
import static com.stg.utils.Constants.BAAS_SUCCESS_CODE;
import static com.stg.utils.Constants.Status;
import static com.stg.utils.enums.ErrorCodeCardPartner.ENOUGH_CONDITION;
import static com.stg.utils.enums.ErrorCodeCardPartner.INSTALLMENT_SUCCESS;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardInstallmentServiceImpl implements CardInstallmentService {
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final CallCardPartnerAdapterIService getInsFeeAdapterIService;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final PaymentLockService paymentLockService;
    private final CustomerRepository customerRepository;

    private static final String RET_REF_NUMBER ="retRefNumber";

    @Value("${external.host.installment_host}")
    private String installmentHost;

    @Value("${external.installment-key.merchant}")
    private String installmentMerchant;

    @Override
    @Async
//    @Retryable(maxAttemptsExpression = "${external.installment-key.max_attempt}",
//            value = InstallmentApiException.class,
//            backoff = @Backoff(
//                    delayExpression = "${external.installment-key.delay}",
//                    multiplierExpression = "${external.installment-key.multiplier}",
//                    maxDelayExpression = "${external.installment-key.max_delay}"
//            ))
    public void createInstallmentNoOtpProcess(String retRefNumber, String period, String cardClass, InsurancePayment insurancePayment) {
        log.info("[INSTALLMENT]-- Đăng ký trả góp Non-OTP cho giao dịch {}", retRefNumber);
        String mbTransactionId = insurancePayment.getTransactionId();

        paymentLockService.lockRegisterInstallment(mbTransactionId, () -> {
            try {
                String conditionCheckUrl = installmentHost + "/private/ms/card-partner/v1.0/installment/condition-check";
                String urlTemplate = UriComponentsBuilder.fromHttpUrl(conditionCheckUrl)
                        .queryParam(RET_REF_NUMBER, "{retRefNumber}")
                        .encode().toUriString();
                Map<String, String> params = new HashMap<>();
                params.put(RET_REF_NUMBER, retRefNumber);
                CommonAdapterResponse conditionCheckResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(urlTemplate, params,
                        HttpMethod.GET, new HttpHeaders(), null);
                CheckConditionResponse conditionResponse = modelMapper.map(conditionCheckResponse, CheckConditionResponse.class);
                CheckConditionResponse.DataResp dataResp = objectMapper.convertValue(conditionCheckResponse.getData(), new TypeReference<>() {
                });

                CommonCardResponse conditionCheckMap = modelMapper.map(conditionCheckResponse, CommonCardResponse.class);
                conditionCheckMap.setErrorDescription(ErrorCodeCardPartner.getEnum(conditionCheckResponse.getErrorCode()));

                log.info("[INSTALLMENT]--AUTO Giao dịch {} check điều kiện trả góp {}, dataResp {}", retRefNumber, conditionCheckMap, dataResp);
                insurancePayment.setCardClass(cardClass);
                insurancePayment.setWay4DocsId(retRefNumber);
                insurancePayment.setVersion(1);
                if (BAAS_SUCCESS_CODE.equals(conditionResponse.getErrorCode())
                        && (ENOUGH_CONDITION.getLabelEn().equals(dataResp.getStatus()) ||
                        ENOUGH_CONDITION.getLabelVn().equals(dataResp.getStatus()))) {
                    //start gen param url and add key header
                    String createInstallmentUrl = installmentHost + "/private/ms/card-partner/v1.0/installment/create-installment";
                    //end gen param url and add key header
                    CreateInstallElementNoOtpRequest noOtpRequest = new CreateInstallElementNoOtpRequest()
                            .setPeriod(period)
                            .setRetRefNumber(retRefNumber)
                            .setMerchant(installmentMerchant);
                    CommonAdapterResponse createInstallmentResp = getInsFeeAdapterIService.callGetCardPartnerAdapter(createInstallmentUrl,
                            new HashMap<>(), HttpMethod.POST, addParamAndKeyHeader(), noOtpRequest);
                    CommonCardResponse createInstallmentMap = modelMapper.map(createInstallmentResp, CommonCardResponse.class);
                    createInstallmentMap.setErrorDescription(ErrorCodeCardPartner.getEnum(createInstallmentResp.getErrorCode()));
                    log.info("[INSTALLMENT]--Giao dịch {} kết quả đăng ký trả góp {}", retRefNumber, createInstallmentMap);
                    if (ENOUGH_CONDITION.getCode().equals(createInstallmentMap.getErrorCode())) {
                        // Dky trả góp thành công
                        insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getLabelEn());
                        insurancePayment.setInstallmentErrorCode(ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getCode());
                        insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                        insurancePaymentRepository.save(insurancePayment);
                        log.info("[INSTALLMENT]--Đăng ký trả góp thành công cho giao dịch {}", retRefNumber);
                    } else {
                        insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_FAILED.getLabelEn());
                        insurancePayment.setInstallmentErrorCode(createInstallmentMap.getErrorCode());
                        insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                        insurancePaymentRepository.save(insurancePayment);
                        log.error("[INSTALLMENT]--Đăng ký trả góp chưa thành công cho giao dịch {}", retRefNumber);
                    }
                } else {
                    if (INSTALLMENT_SUCCESS.getLabelEn().equals(insurancePayment.getInstallmentStatus())) {
                        log.info("[INSTALLMENT]--Giao dịch đã đăng ký trả góp {}. Kết thúc quá trình auto retry", retRefNumber);
                        return;
                    }
                    insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_UNQUALIFIED.getLabelEn());
                    insurancePayment.setInstallmentErrorCode(conditionCheckMap.getErrorCode());
                    insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                    insurancePaymentRepository.save(insurancePayment);
                    log.error("[INSTALLMENT]--Chưa đủ điều kiện đăng ký trả góp giao dịch {}", retRefNumber);
                }
            } catch (Exception ex) {
                log.error("[INSTALLMENT][ERROR] transactionId=" + mbTransactionId, ex);
            }
        });
    }

    @Override
    public GetInstFeeResponse getInsFee(GetInstFeeRequest feeRequest) {
        String url = installmentHost + "/private/ms/card-partner/v1.0/installment/fee-check";
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("period", "{period}")
                .queryParam("valueTransaction", "{valueTransaction}")
                .queryParam("merchant", "{merchant}")
                .queryParam("cardClass", "{cardClass}")
                .encode().toUriString();
        Map<String, String> params = new HashMap<>();
        params.put("period", feeRequest.getPeriod());
        params.put("valueTransaction", feeRequest.getValueTransaction());
        params.put("merchant", installmentMerchant);
        params.put("cardClass", feeRequest.getCardClass());
        CommonAdapterResponse commonAdapterResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(urlTemplate, params, HttpMethod.GET, new HttpHeaders(), null);
        if(!BAAS_SUCCESS_CODE.equals(commonAdapterResponse.getErrorCode())) {
            log.error("[INSTALLMENT]--Get fee error {}", commonAdapterResponse.getErrorDesc());
            throw new BaasApiException(INSTALLMENT_MSG01);
        }

        GetInstFeeResponse insFeeResponse = modelMapper.map(commonAdapterResponse, GetInstFeeResponse.class);
        GetInstFeeResponse.DataResp dataResp = objectMapper.convertValue(commonAdapterResponse.getData(), new TypeReference<>() {
        });
        insFeeResponse.setData(dataResp);

        return insFeeResponse
                .setMonthlyPaymentStr(formatCurrency(BigDecimal.valueOf(Double.parseDouble(insFeeResponse.getData().getFeesPayable()))))
                .setErrorCode(insFeeResponse.getErrorCode());
    }

    @Override
    public CommonCardResponse createInstallmentNoOtp(CreateInstallElementNoOtpRequest request) {
        //start gen param url and add key header
        String url = installmentHost + "/private/ms/card-partner/v1.0/installment/create-installment";
        //end gen param url and add key header
        CommonAdapterResponse commonAdapterResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(url, new HashMap<>(), HttpMethod.POST, addParamAndKeyHeader(), request);
        CommonCardResponse map = modelMapper.map(commonAdapterResponse, CommonCardResponse.class);
        map.setErrorDescription(ErrorCodeCardPartner.getEnum(commonAdapterResponse.getErrorCode()));
        log.info("[INSTALLMENT]--Kết quả đăng ký trả góp manual {}", map);
        return map;
    }

    @Override
    public CommonCardResponse createInstallElementOtp(CreateInstallElementOtpRequest request) {
        //start gen param url and add key header
        String url = installmentHost + "/private/ms/card-partner/v1.0/installment/create-installment/request";
        //end gen param url and add key header
        CommonAdapterResponse commonAdapterResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(url, new HashMap<>(), HttpMethod.POST, addParamAndKeyHeader(), request);
        CommonCardResponse map = modelMapper.map(commonAdapterResponse, CommonCardResponse.class);
        map.setErrorDescription(ErrorCodeCardPartner.getEnum(commonAdapterResponse.getErrorCode()));
        return map;
    }

    @Override
    public CommonCardResponse createInstallElementOtpConfirm(CreateInstallElementOtpConfirmRequest request) {
        String url = installmentHost + "/private/ms/card-partner/v1.0/installment/create-installment/confirm";
        CommonAdapterResponse commonAdapterResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(url, new HashMap<>(), HttpMethod.POST, addParamAndKeyHeader(), request);
        CommonCardResponse map = modelMapper.map(commonAdapterResponse, CommonCardResponse.class);
        map.setErrorDescription(ErrorCodeCardPartner.getEnum(commonAdapterResponse.getErrorCode()));
        return map;
    }

    @Override
    public CommonCardResponse getInstallElementResult(GetInstallElementResultRequest request) {
        String url = installmentHost + "/private/ms/card-partner/v1.0/installment/get-create-installment-result";
        UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("requestId", "{requestId}")
                .encode().toUriString();
        Map<String, String> params = new HashMap<>();
        params.put("requestId", request.getRequestId());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("sourceAppId", request.getSourceAppId());
        CommonAdapterResponse commonAdapterResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(url, params, HttpMethod.GET, httpHeaders, null);
        CommonCardResponse map = modelMapper.map(commonAdapterResponse, CommonCardResponse.class);
        map.setErrorDescription(ErrorCodeCardPartner.getEnum(commonAdapterResponse.getErrorCode()));
        return map;
    }

    @Override
    public CommonCardResponse getInstInfo(GetInstInfoRequest request) {
        String url = installmentHost + "/private/ms/card-partner/v1.0/installment/get-installment-info";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("sourceAppId", request.getSourceAppId());
        CommonAdapterResponse commonAdapterResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(url, new HashMap<>(), HttpMethod.POST, httpHeaders, request);
        CommonCardResponse map = modelMapper.map(commonAdapterResponse, CommonCardResponse.class);
        map.setErrorDescription(ErrorCodeCardPartner.getEnum(commonAdapterResponse.getErrorCode()));
        return map;
    }

    @Override
    public InstallmentManualResp createInstallmentNoOtpManual(ManualRequest reqDto) {
        String mbTransactionId = reqDto.getMbTransactionId();

        return paymentLockService.lockRegisterInstallment(mbTransactionId, () -> {
            InstallmentManualResp manualResp = new InstallmentManualResp();
            try {
                InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(mbTransactionId);
                if (!insurancePayment.isInstallment() || ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getLabelEn().equals(insurancePayment.getInstallmentStatus())) {
                    log.error("[INSTALLMENT-MANUAL]--Giao dich không đủ điều kiện đăng ký trả góp {}", mbTransactionId);
                    throw new InstallmentApiException("Không phải giao dịch trả góp. Không được phép đăng ký");
                }

                String conditionCheckUrl = installmentHost + "/private/ms/card-partner/v1.0/installment/condition-check";
                String urlTemplate = UriComponentsBuilder.fromHttpUrl(conditionCheckUrl)
                        .queryParam(RET_REF_NUMBER, "{retRefNumber}")
                        .encode().toUriString();
                Map<String, String> params = new HashMap<>();
                params.put(RET_REF_NUMBER, insurancePayment.getWay4DocsId());
                CommonAdapterResponse conditionCheckResponse = getInsFeeAdapterIService.callGetCardPartnerAdapter(urlTemplate, params,
                        HttpMethod.GET, new HttpHeaders(), null);
                CommonCardResponse conditionCheckMap = modelMapper.map(conditionCheckResponse, CommonCardResponse.class);
                conditionCheckMap.setErrorDescription(ErrorCodeCardPartner.getEnum(conditionCheckResponse.getErrorCode()));

                //CheckConditionResponse conditionResponse = modelMapper.map(conditionCheckResponse, CheckConditionResponse.class);
                CheckConditionResponse.DataResp dataResp = objectMapper.convertValue(conditionCheckResponse.getData(), new TypeReference<>() {
                });

                log.info("[INSTALLMENT-MANUAL]--Giao dịch {} check điều kiện trả góp manual {}, dataResp {}",
                        insurancePayment.getWay4DocsId(), conditionCheckMap, dataResp);

                //check result condition check
                if (BAAS_SUCCESS_CODE.equals(conditionCheckMap.getErrorCode())
                        && (ENOUGH_CONDITION.getLabelEn().equals(dataResp.getStatus()) ||
                        ENOUGH_CONDITION.getLabelVn().equals(dataResp.getStatus()))) {
                    CommonCardResponse response = createInstallmentNoOtp(new CreateInstallElementNoOtpRequest(insurancePayment.getPeriod(),
                            insurancePayment.getWay4DocsId(), installmentMerchant));
                    if (BAAS_SUCCESS_CODE.equals(response.getErrorCode())) {
                        // Dky trả góp thành công
                        insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getLabelEn());
                        insurancePayment.setInstallmentErrorCode(response.getErrorCode());
                        insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                        insurancePaymentRepository.save(insurancePayment);
                        log.info("[INSTALLMENT-MANUAL]--Đăng ký chuyển đổi trả góp manual thành công cho giao dịch {} với mã w4 {}", mbTransactionId, insurancePayment.getWay4DocsId());
                        manualResp.setMessage(ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getLabelEn());
                        manualResp.setStatus(Status.SUCCESS);
                        manualResp.setErrorCode(response.getErrorCode());
                        return manualResp;
                    } else {
                        insurancePayment.setInstallmentErrorCode(response.getErrorCode());
                        insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                        insurancePaymentRepository.save(insurancePayment);
                        log.error("[INSTALLMENT-MANUAL]--Đăng ký chuyển đổi trả góp manual chưa thành công cho giao dịch {} với mã w4: {}", mbTransactionId, insurancePayment.getWay4DocsId());
                        manualResp.setMessage(ErrorCodeCardPartner.INSTALLMENT_FAILED.getLabelEn());
                        manualResp.setStatus(Status.FAIL);
                        manualResp.setErrorCode(response.getErrorCode());
                        return manualResp;
                    }
                } else {
                    // Dky trả góp chưa du dieu kien
                    insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_UNQUALIFIED.getLabelEn());
                    insurancePayment.setInstallmentErrorCode(conditionCheckMap.getErrorCode());
                    insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                    insurancePaymentRepository.save(insurancePayment);
                    log.error("[INSTALLMENT-MANUAL]--Giao dịch chưa đủ điều kiện chuyển đổi trả góp manual {}", insurancePayment.getWay4DocsId());
                    manualResp.setMessage(ErrorCodeCardPartner.INSTALLMENT_UNQUALIFIED.getLabelEn());
                    manualResp.setStatus(Status.FAIL);
                    manualResp.setErrorCode(conditionCheckMap.getErrorCode());
                    return manualResp;
                }
            } catch (Exception ex) {
                log.error("[INSTALLMENT-MANUAL][ERROR] transactionId=" + mbTransactionId, ex);
                manualResp.setStatus(Status.FAIL);
                if (manualResp.getMessage() == null) manualResp.setMessage(ex.getMessage());
            }
            return manualResp;
        });
    }

    private HttpHeaders addParamAndKeyHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String transactionId = generateUUIDId(20);
        httpHeaders.set("transactionId", transactionId);
        return httpHeaders;
    }
}
