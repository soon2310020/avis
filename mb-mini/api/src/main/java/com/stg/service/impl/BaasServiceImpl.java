package com.stg.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.stg.entity.AdditionalProduct;
import com.stg.adapter.dto.CommonAdapterResponse;
import com.stg.adapter.service.CallCardPartnerAdapterIService;
import com.stg.entity.BaasPayOnBehalf;
import com.stg.entity.InsuranceContract;
import com.stg.entity.InsurancePayment;
import com.stg.entity.user.User;
import com.stg.errors.*;
import com.stg.repository.BaasPayOnBehalfRepository;
import com.stg.repository.InsuranceContractRepository;
import com.stg.repository.InsurancePaymentRepository;
import com.stg.repository.UserRepository;
import com.stg.service.BaasService;
import com.stg.service.ExternalFlexibleApiService;
import com.stg.service.ExternalV2ApiService;
import com.stg.service.caching.RmExcelDataCaching;
import com.stg.service.card.CardInstallmentService;
import com.stg.service.dto.baas.*;
import com.stg.service.dto.card.CheckConditionResponse;
import com.stg.service.dto.card.CommonCardResponse;
import com.stg.service.dto.card.CreateInstallElementNoOtpRequest;
import com.stg.service.dto.crm.RmExcelInfo;
import com.stg.service.dto.external.RmInfoResp;
import com.stg.service.dto.card.GetCardListRequest;
import com.stg.service.dto.external.request.MbalHookReqDto;
import com.stg.service.dto.external.requestFlexible.PaymentNotificationFlexibleReqDto;
import com.stg.service.dto.external.requestV2.PaymentNotificationV2ReqDto;
import com.stg.service.dto.external.responseFlexible.PaymentNotificationFlexibleRespDto;
import com.stg.service.dto.external.responseV2.PaymentNotificationV2RespDto;
import com.stg.service.lock.PaymentLockService;
import com.stg.utils.Constants;
import com.stg.utils.InstallmentPopup;
import com.stg.utils.enums.ErrorCodeCardPartner;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.codec.digest.HmacUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.Constants.*;
import static com.stg.utils.Endpoints.*;
import static com.stg.utils.enums.ErrorCodeCardPartner.ENOUGH_CONDITION;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import static org.apache.http.client.config.AuthSchemes.BASIC;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BaasServiceImpl implements BaasService {

    private static final String THANH_TOAN_CHO_HSYC_BAO_HIEM = " Thanh toán cho HSYC bảo hiểm";
    private static final String NOP_PHI_CHO_MB_AGEAS_LIFE = "-Nop phi cho MB Ageas Life ";
    private static final String FALSE = "FALSE";
    private static final String BAAS_TRANSACTION_ID_TRANSFER_RESULT = " voi baasTransactionId {}. Transfer result: {}";
    private static final String BAAS_DETAIL_ERROR_MESSAGE_FOR_BAAS_TRANSACTION_ID_DETAIL = "[BAAS]-Detail error message for baasTransactionId {}. Detail: {}";
    private static final String BAAS_TRANSACTION_ID_REQUEST_DATA = " voi baasTransactionId {}. Request data: {}";
    private static final String AUTHORIZATION = "Authorization";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String CLIENT_MESSAGE_ID = "clientMessageId";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String SIGNATURE = "signature";
    private static final String SHA256_INSTANCE = "SHA256withRSA";
    private static final String ERROR_SIGN = "error";
    private static final String PAYMENT_HUB_KEY = "PAYMENT_HUB_KEY";
    private static final String PAYMENT_HUB_SECRET = "PAYMENT_HUB_SECRET";
    private static final String REMARK_TRANSFER = "APPBAOHIEM-CHUYENTIEN-";
    private static String RET_REF_NUMBER = "retRefNumber";

    @Value("${external.host.baas-host}")
    private String baasHost;

    @Value("${external.baas-key.client_id}")
    private String clientId;

    @Value("${external.baas-key.client_secret}")
    private String clientSecret;

    @Value("${api3rd_endpoint.baas.client_id}")
    private String baasListCardClientId;
    @Value("${api3rd_endpoint.baas.client_secret}")
    private String baasListCardSecretKey;

    @Value("${external.baas-key.bank_account}")
    private String baasBankAccount;
    @Value("${external.baas-key.account_name}")
    private String baasAccountName;
    @Value("${external.mic-key.account_name}")
    private String micAccountName;
    @Value("${external.mic-key.bank_account}")
    private String micBankAccount;
    @Value("${external.mbal-key.account_name}")
    private String mbalAccountName;
    @Value("${external.mbal-key.bank_account}")
    private String mbalBankAccount;
    @Value("${external.baas-key.channel}")
    private String baasChannel;
    @Value("${external.baas-key.business_code}")
    private String baasBusinessCode;

    @Value("${external.mbal-key.payment_hub_key}")
    private String mbalPaymentHubKey;
    @Value("${external.mbal-key.payment_hub_secrtet}")
    private String mbalPaymentHubSecret;
    @Value("${external.host.mbal-portal-host}")
    private String mbalPortalHost;

    @Value("${external.mbal-key.merchant_code}")
    private String mbalMerchantCode;

    @Value("${external.baas-key.crm_client_id}")
    private String crmClientId;

    @Value("${external.baas-key.crm_client_secret}")
    private String crmClientSecret;

    @Value("${external.baas-key.crm_basic_value}")
    private String crmBasicValue;

    @Value("${external.mbal-key.checksum_secret}")
    private String mbalChecksumSecret;

    @Value("${external.host.crm_host}")
    private String crmHost;

    @Value("${external.host.installment_host}")
    private String installmentHost;

    @Value("${external.installment-key.merchant}")
    private String installmentMerchant;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PrivateKey privateKey;

    @Autowired
    private final Gson gson;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final RedisCommands<String, String> redis;

    private final BaasPayOnBehalfRepository baasPayOnBehalfRepository;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final InsuranceContractRepository insuranceContractRepository;
    private final ExternalV2ApiService v2ApiService;
    private final ExternalFlexibleApiService flexibleApiService;
    private final UserRepository userRepository;
    private final AsyncObjectImpl asyncObject;
    private final CardInstallmentService installmentService;
    private final CallCardPartnerAdapterIService getInsFeeAdapterIService;
    private final ModelMapper modelMapper;
    private final RmExcelDataCaching rmExcelDataCaching;
    private final PaymentLockService paymentLockService;

    @Override
    @Async
    public void payOnBehalfProcess(String accessToken, String micAmount, String mbalAmount, String gcnbh,
                                   String appNoId, String paymentTransId, String ullVersion, String cif,
                                   String fundingSource, String cardType, String createOrderId, String hookTypeCode) {
        paymentLockService.lockPayOnBehalf(paymentTransId, () -> {
            try {
                TransferNonOtpReqDto nonOtpReqDto = genCommonTransferNonOtpReqDto();
                InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(paymentTransId);
                // Call to MIC
                payOnBehalfForMic(nonOtpReqDto, accessToken, micAmount, gcnbh, insurancePayment, paymentTransId);
                // Call to MBAL
                payOnBehalfForMbal(nonOtpReqDto, accessToken, mbalAmount, appNoId, insurancePayment, paymentTransId,
                        ullVersion, cif, fundingSource, cardType, createOrderId, hookTypeCode);
            } catch (Exception ex) {
                log.error("[COMBO-PAY_ON_BEHALF][ERROR] transactionId=" + paymentTransId, ex);
            }
        });
    }

    @Override
    public OauthToken generateToken() {
        // Get token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBasicAuth(clientId, clientSecret);
        tokenHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        HttpEntity<MultiValueMap<String, String>> tokenRequestEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
        OauthToken oauthToken = getResponseForPostRequest(baasHost + BAAS_GENERATE_TOKEN, tokenRequestEntity, OauthToken.class, "", HOST_PARTY.BAAS);
        if (oauthToken == null) {
            log.error("[BAAS]--Can not get token from BAAS");
            return new OauthToken().setAccessToken(null);
        }
        return oauthToken;
    }

    @Override
    public OauthToken generateTokenGetListCard() {
        // Get token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setBasicAuth(baasListCardClientId, baasListCardSecretKey);
        tokenHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        HttpEntity<MultiValueMap<String, String>> tokenRequestEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
        OauthToken oauthToken = getResponseForPostRequest(baasHost + BAAS_GENERATE_TOKEN, tokenRequestEntity, OauthToken.class, "", HOST_PARTY.BAAS);
        if (oauthToken == null) {
            log.error("[BAAS]--Can not get token from BAAS");
            return new OauthToken().setAccessToken(null);
        }
        return oauthToken;
    }

    @Override
    @Cacheable(value = "cache:crmInfo")
    public RmInfoResp getCrmData(String rmCode) {
        String clientMessageId = generateUUIDId(20);
        OauthToken crmToken = generateCrmToken(clientMessageId);
        if (crmToken == null) {
            log.error("[CRM]--Co loi khi get RM code in clientMessageId {}", clientMessageId);
            throw new MiniApiException(MSG12);
        }
        HttpHeaders headers = generateDefaultHeader();
        headers.add(AUTHORIZATION, BEARER + crmToken.getAccessToken());
        headers.add(CLIENT_MESSAGE_ID, clientMessageId);
        HttpEntity<?> entity = new HttpEntity<>(null, headers);
        try {
            String response = restTemplate.exchange(((crmHost + CRM_CORE_VALUE)
                    .replace("rmCode", rmCode)), HttpMethod.GET, entity, String.class).getBody();
            if (response != null) {
                response = response.substring(1);
            }
            RmInfoResp rmInfoResp = gson.fromJson(response, RmInfoResp.class);
            rmInfoResp.setMbCode(rmCode);
            /**/
            RmExcelInfo rmExcelInfo = rmExcelDataCaching.getData(rmCode);
            if (rmExcelInfo != null) {
                RmInfoResp.HrisEmployee hrisEmployee = rmInfoResp.getHrisEmployee();
                if (!StringUtils.hasText(hrisEmployee.getFullName())) {
                    hrisEmployee.setFullName(rmExcelInfo.getFullName());
                }
                if (!StringUtils.hasText(hrisEmployee.getEmail())) {
                    hrisEmployee.setEmail(rmExcelInfo.getEmail());
                }
                if (!StringUtils.hasText(hrisEmployee.getPhoneNumber())) {
                    hrisEmployee.setPhoneNumber(rmExcelInfo.getPhone());
                }
                if (!StringUtils.hasText(hrisEmployee.getPhoneNumber2())) {
                    hrisEmployee.setPhoneNumber2(rmExcelInfo.getPhone2());
                }
            }
            /**/
            return rmInfoResp;
        } catch (Exception e) {
            log.error("[CRM]--Lỗi khi get RM code với giá trị {}. Detail mess {}", rmCode, e.getMessage());
            throw new CrmDataNotFoundException("Không tồn tại thông tin RM");
        }
    }

    @Override
    public BaasPayOnBehalfRespDto micManualPayOnBehalf(String idGiaoDich) {
        InsurancePayment insurancePayment = findByTransactionId(idGiaoDich);
        InsuranceContract insuranceContract = findContractByTransactionId(idGiaoDich);
        TransferNonOtpReqDto nonOtpReqDto = genCommonTransferNonOtpReqDto();

        List<BaasPayOnBehalf> baasMics = baasPayOnBehalfRepository.findBaasPayOnBehalvesByMbTransactionIdAndTypeAndStatus(idGiaoDich);
        BaasPayOnBehalfRespDto onBehalfRespDto = new BaasPayOnBehalfRespDto();
        for (BaasPayOnBehalf baasMic : baasMics) {
            onBehalfRespDto = payOnBehalfManual(baasMic, nonOtpReqDto, insurancePayment, insuranceContract, MIC);
            if (Status.FAIL.equals(onBehalfRespDto.getStatus())) {
                onBehalfRespDto.setStatus(Status.FAIL);
            }
        }
        return onBehalfRespDto;
    }

    @Override
    public BaasPayOnBehalfRespDto mbalManualPayOnBehalf(String idGiaoDich) {
        BaasPayOnBehalfRespDto onBehalfRespDto = new BaasPayOnBehalfRespDto();
        InsurancePayment insurancePayment = findByTransactionId(idGiaoDich);
        InsuranceContract insuranceContract = findContractByTransactionId(idGiaoDich);
        TransferNonOtpReqDto nonOtpReqDto = genCommonTransferNonOtpReqDto();
        if (insuranceContract.getMbalProductName().equals(MBAL_PRODUCT_NAME_V2)) {
            log.error("Không đủ dữ liệu chi hộ cho ul2.0 với ID {}", idGiaoDich);
            throw new MiniApiException("Không đủ dữ liệu chi hộ cho ul2.0");
        }
        BaasPayOnBehalf baasMbal = baasPayOnBehalfRepository.findBaasPayOnBehalvesByMbTransactionIdAndType(idGiaoDich, MBAL);
        if (baasMbal.getMbalHookStatus().equals(Boolean.TRUE)) {
            log.error("Bạn đang chi hộ MBAL cho giao dịch đã hoàn thành.");
            throw new MiniApiException("Không thể chi hộ cho giao dịch đã thành công");
        }
        if (baasMbal.getStatus().equals(Status.SUCCESS) && Boolean.TRUE.equals(!baasMbal.getMbalHookStatus())) {
            boolean hookValue = hookMbalManualAfterPayOnBehalf(baasMbal, insuranceContract.getMbalAppNo(), insurancePayment.getCustomer().getMbId());
            if (hookValue) {
                return onBehalfRespDto.setStatus(Status.SUCCESS).setMessage(MSG32);
            }
            return onBehalfRespDto.setStatus(Status.FAIL).setMessage(MSG31);
        }
        onBehalfRespDto = payOnBehalfManual(baasMbal, nonOtpReqDto, insurancePayment, insuranceContract, MBAL);

        return onBehalfRespDto;
    }

    @Override
    public void checkPermission(Long userId) {
        isSuperAdmin(userId);
    }

    @Override
    @Async
    public void flexiblePayOnBehalfProcess(String cif, String accessToken, String mbTransactionId,
                                           InsurancePayment insurancePayment, List<AdditionalProduct> micAdditionalProducts) {
        paymentLockService.lockPayOnBehalf(mbTransactionId, () -> {
            try {
                String mbalAmount = String.valueOf(convertToLong(insurancePayment.getMbalInsuranceFee()));
                TransferNonOtpReqDto nonOtpReqDto = genCommonTransferNonOtpReqDto();
                flexiblePayOnBehalfForMbal(nonOtpReqDto, accessToken, mbalAmount, insurancePayment.getMbalAppNo(), insurancePayment, mbTransactionId, cif); //NOSONAR
                if (!micAdditionalProducts.isEmpty()) {
                    for (AdditionalProduct additionalProduct : micAdditionalProducts) {
                        payOnBehalfForMic(nonOtpReqDto, accessToken, String.valueOf(additionalProduct.getMicFee().longValue()), additionalProduct.getMicContractNum(), insurancePayment, mbTransactionId);
                    }
                }
            } catch (Exception ex) {
                log.error("[FLEX-PAY_ON_BEHALF][ERROR] transactionId=" + mbTransactionId, ex);
            }
        });
    }

    @Override
    public <P> CommonAdapterResponse callGetCardPartnerAdapter(String urlTemplate, Map<String, String> params,
                                                               HttpMethod method, HttpHeaders headers, P param) {

        String clientMessageId = generateUUIDId(20);
        String signature = null;
        if (param instanceof GetCardListRequest) {
            signature = generateSign(((GetCardListRequest) param).getRequestType() +
                    ((GetCardListRequest) param).getRequestNumber() +
                    ((GetCardListRequest) param).getCustomerName() +
                    ((GetCardListRequest) param).getPhoneNumber());
        }
        // Get token
        OauthToken oauthToken = generateToken();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, BEARER + oauthToken.getAccessToken());
        headers.set(CLIENT_MESSAGE_ID, clientMessageId);
        headers.set(SIGNATURE, signature);
        HttpEntity<?> entity  = new HttpEntity<>(param, headers);
        log.info("[MINI]-- call template {} with clientMessageId: {}, header: ***, params: {}, param: {}", urlTemplate, clientMessageId, params, param);
        try {
            ResponseEntity<String> response = restTemplate.exchange(urlTemplate, method, entity, String.class, params);
            CommonAdapterResponse commonAdapterResponse = gson.fromJson(response.getBody(), CommonAdapterResponse.class);
            log.info("[CARD]--end call card partner success ---------->. Response {}", commonAdapterResponse);
            return commonAdapterResponse;
        } catch (HttpStatusCodeException e) {
            try {
                CommonAdapterResponse commonAdapterResponse = gson.fromJson(e.getResponseBodyAsString(), CommonAdapterResponse.class);
                log.error("[CARD]--end call card partner error {} ------------", commonAdapterResponse);
                return commonAdapterResponse;
            }catch (Exception ex){
                log.error("[CARD]--end no parse response call card partner {} ---------",ex.getMessage());
                return new CommonAdapterResponse(clientMessageId,"500");
            }
        }catch (Exception ex){
            log.error("[CARD]--end call card partner error {} ---------",ex.getMessage());
            return new CommonAdapterResponse(clientMessageId,"500");
        }
    }

    @Override
    public <P> CommonAdapterResponse getListCard(String urlTemplate, Map<String, String> params,
                                                               HttpMethod method, HttpHeaders headers, P param) {
        String clientMessageId = generateUUIDId(20);
        String signature = null;
        if (param instanceof GetCardListRequest) {
            signature = generateSign(((GetCardListRequest) param).getRequestType() +
                    ((GetCardListRequest) param).getRequestNumber() +
                    ((GetCardListRequest) param).getCustomerName() +
                    ((GetCardListRequest) param).getPhoneNumber());
        }
        // Get token
        OauthToken oauthToken = generateTokenGetListCard();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, BEARER + oauthToken.getAccessToken());
        headers.set(CLIENT_MESSAGE_ID, clientMessageId);
        headers.set(SIGNATURE, signature);
        HttpEntity<?> entity  = new HttpEntity<>(param, headers);
        log.info("[MINI]-- call template {} with clientMessageId: {}, header: ***, params: {}, param: {}", urlTemplate, clientMessageId, params, param);
        try {
            ResponseEntity<String> response = restTemplate.exchange(urlTemplate, method, entity, String.class, params);
            CommonAdapterResponse commonAdapterResponse = gson.fromJson(response.getBody(), CommonAdapterResponse.class);
            log.info("[CARD]--end call card partner success ---------->. Response {}", commonAdapterResponse);
            return commonAdapterResponse;
        } catch (HttpStatusCodeException e) {
            try {
                CommonAdapterResponse commonAdapterResponse = gson.fromJson(e.getResponseBodyAsString(), CommonAdapterResponse.class);
                log.error("[CARD]--end call card partner error {} ------------", commonAdapterResponse);
                return commonAdapterResponse;
            }catch (Exception ex){
                log.error("[CARD]--end no parse response call card partner {} ---------",ex.getMessage());
                return new CommonAdapterResponse(clientMessageId,"500");
            }
        }catch (Exception ex){
            log.error("[CARD]--end call card partner error {} ---------",ex.getMessage());
            return new CommonAdapterResponse(clientMessageId,"500");
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    @SchedulerLock(name = "AutoRetryPayOnBehalf")
    public void scheduleAutoPayOnBehalf() {
        log.debug("[PAY_ON_BEHALF-SCHEDULE]--Retry auto pay on behalf at {}", LocalDateTime.now());

        /*[micOnBehalf]*/
        List<BaasPayOnBehalf> micOnBehalfList = baasPayOnBehalfRepository.collectMicDataForRetryPay();
        if (!micOnBehalfList.isEmpty()) {
            log.info("[PAY_ON_BEHALF-SCHEDULE]--Danh sách giao dịch MIC với size {} cần retry chi hộ {}", micOnBehalfList.size(),
                    micOnBehalfList.stream().map(BaasPayOnBehalf::getMbTransactionId).collect(Collectors.toList()));

            Map<String, List<BaasPayOnBehalf>> micOnBehalMap = new HashMap<>(); //<transactionId, List<BaasPayOnBehalf>>
            micOnBehalfList.forEach(el -> {
                List<BaasPayOnBehalf> payments = micOnBehalMap.computeIfAbsent(el.getMbTransactionId(), k -> new ArrayList<>());
                payments.add(el);
            });

            for (Map.Entry<String, List<BaasPayOnBehalf>> paymentEntry : micOnBehalMap.entrySet()) {
                String mbTransactionId = paymentEntry.getKey();
                List<BaasPayOnBehalf> payments = paymentEntry.getValue();

                paymentLockService.lockPayOnBehalf(mbTransactionId, () -> {
                    for (BaasPayOnBehalf micOnBehalf : payments) {
                        try {
                            micManualPayOnBehalf(micOnBehalf.getMbTransactionId());
                        } catch (MiniApiException e) {
                            micOnBehalf.setVersion(micOnBehalf.getVersion() + 1);
                            baasPayOnBehalfRepository.save(micOnBehalf);
                            log.error("[PAY_ON_BEHALF-SCHEDULE][ERROR] Retry chi hộ MIC auto cho giao dịch {} không thành công.", micOnBehalf.getMbTransactionId());
                        }
                    }
                });
            }
        }

        /*[mbalManualPayOnBehalf]*/
        List<BaasPayOnBehalf> mbalOnBehalfList = baasPayOnBehalfRepository.collectMbalDataForRetryPay();
        if (!mbalOnBehalfList.isEmpty()) {
            log.info("[PAY_ON_BEHALF-SCHEDULE]--Danh sách giao dịch MBAL với size {} cần retry chi hộ {}", mbalOnBehalfList.size(),
                    mbalOnBehalfList.stream().map(BaasPayOnBehalf::getMbTransactionId).collect(Collectors.toList()));

            Map<String, List<BaasPayOnBehalf>> mbalOnBehalMap = new HashMap<>(); //<transactionId, List<BaasPayOnBehalf>>
            mbalOnBehalfList.forEach(el -> {
                List<BaasPayOnBehalf> payments = mbalOnBehalMap.computeIfAbsent(el.getMbTransactionId(), k -> new ArrayList<>());
                payments.add(el);
            });

            for (Map.Entry<String, List<BaasPayOnBehalf>> paymentEntry : mbalOnBehalMap.entrySet()) {
                String mbTransactionId = paymentEntry.getKey();
                List<BaasPayOnBehalf> payments = paymentEntry.getValue();

                paymentLockService.lockPayOnBehalf(mbTransactionId, () -> {
                    for (BaasPayOnBehalf mbalOnBehalf : payments) {
                        try {
                            mbalManualPayOnBehalf(mbalOnBehalf.getMbTransactionId());
                        } catch (MiniApiException e) {
                            mbalOnBehalf.setVersion(mbalOnBehalf.getVersion() + 1);
                            baasPayOnBehalfRepository.save(mbalOnBehalf);
                            log.error("[PAY_ON_BEHALF-SCHEDULE][ERROR] Retry chi hộ MBAL auto cho giao dịch {} không thành công.", mbalOnBehalf.getMbTransactionId());
                        }
                    }
                });
            }
        }
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    @SchedulerLock(name = "AutoRetryCreateInstallment")
    public void scheduleAutoCreateInstallment() {
        log.info("[MINI]--Retry auto creat instalment {}", LocalDateTime.now());
        List<InsurancePayment> insurancePayments = insurancePaymentRepository.collectInstallmentPayment();
        for (InsurancePayment insurancePayment : insurancePayments) {
            String mbTransactionId = insurancePayment.getTransactionId();

            paymentLockService.lockRegisterInstallment(mbTransactionId, () -> {
                try {
                    int version = insurancePayment.getVersion() == null ? 0 : insurancePayment.getVersion();
                    insurancePayment.setVersion(version + 1);
                    if (!insurancePayment.isInstallment() || ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getLabelEn().equals(insurancePayment.getInstallmentStatus())) {
                        log.error("[INSTALLMENT-SCHEDULED]--Giao dich không đủ điều kiện đăng ký trả góp {}", mbTransactionId);
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

                    CheckConditionResponse.DataResp dataResp = objectMapper.convertValue(conditionCheckResponse.getData(),
                            new TypeReference<>() {
                            });

                    log.info("[INSTALLMENT-SCHEDULED]--Giao dịch {} check điều kiện trả góp {}, dataResp {}", insurancePayment.getWay4DocsId(), conditionCheckMap, dataResp);
                    InstallmentManualResp manualResp = new InstallmentManualResp();
                    //check result condition check
                    if (BAAS_SUCCESS_CODE.equals(conditionCheckMap.getErrorCode())
                            && ENOUGH_CONDITION.getLabelEn().equals(dataResp.getStatus()) ||
                            ENOUGH_CONDITION.getLabelVn().equals(dataResp.getStatus())) {
                        CommonCardResponse response = installmentService.createInstallmentNoOtp(new CreateInstallElementNoOtpRequest(insurancePayment.getPeriod(),
                                insurancePayment.getWay4DocsId(), installmentMerchant));
                        if (BAAS_SUCCESS_CODE.equals(response.getErrorCode())) {
                            // Dky trả góp thành công
                            insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getLabelEn());
                            insurancePayment.setInstallmentErrorCode(ErrorCodeCardPartner.INSTALLMENT_SUCCESS.getCode());
                            insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                            insurancePaymentRepository.save(insurancePayment);
                            log.info("[INSTALLMENT-SCHEDULED]--Đăng ký chuyển đổi trả góp retry thành công cho giao dịch {} với mã w4 {}", mbTransactionId, insurancePayment.getWay4DocsId());
                        } else {
                            log.error("[INSTALLMENT-SCHEDULED]--Đăng ký chuyển đổi trả góp retry chưa thành công cho giao dịch {} với mã w4: {}", mbTransactionId, insurancePayment.getWay4DocsId());
                            manualResp.setMessage(ErrorCodeCardPartner.INSTALLMENT_FAILED.getLabelEn());
                            insurancePayment.setInstallmentErrorCode(response.getErrorCode());
                            insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                            insurancePaymentRepository.save(insurancePayment);
                        }
                    } else {
                        // Dky trả góp retry chưa đủ điều kiện
                        insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_UNQUALIFIED.getLabelEn());
                        insurancePayment.setInstallmentErrorCode(conditionCheckMap.getErrorCode());
                        insurancePayment.setInstallmentPopup(InstallmentPopup.SHOW);
                        insurancePaymentRepository.save(insurancePayment);
                        log.error("[INSTALLMENT-SCHEDULED]--Giao dịch chưa đủ điều kiện chuyển đổi trả góp retry {}", insurancePayment.getWay4DocsId());
                    }
                } catch (Exception ex) {
                    log.error("[INSTALLMENT-SCHEDULED][ERROR] transactionId=" + mbTransactionId, ex);
                }
            });
        }
    }

    private InsurancePayment findByTransactionId(String mbTransactionId) {
        InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(mbTransactionId);
        if (insurancePayment == null || insurancePayment.getTranStatus().equals(PENDING)) {
            log.error("[MINI]--Giao dịch với id {} chưa hoàn thành.", mbTransactionId);
            throw new MiniApiException("Giao dịch chưa hoàn tất. Bạn không thể thực hiện chi hộ");
        }
        return insurancePayment;
    }

    private InsuranceContract findContractByTransactionId(String mbTransactionId) {
        InsuranceContract insuranceContract = insuranceContractRepository.findByTransactionId(mbTransactionId);
        if (insuranceContract == null) {
            log.error("[MINI]--Giao dịch với id {} chưa hoàn thành.", mbTransactionId);
            throw new MiniApiException("Giao dịch thanh toán chưa hoàn tất. Bạn không thể thực hiện chi hộ");
        }
        return insuranceContract;
    }

    private void isSuperAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN)) {
            throw new UserHasNoPermissionException("Tài khoản không có quyền thực hiện nghiệp vụ.");
        }
    }

    private User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }

    private OauthToken generateCrmToken(String clientMessageId) {
        // Get crm token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.add(AUTHORIZATION, BASIC + " " + crmBasicValue);
        tokenHeaders.add(CLIENT_MESSAGE_ID, clientMessageId);
        tokenHeaders.setContentType(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
        tokenRequest.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        tokenRequest.add("client_secret", crmClientSecret);
        tokenRequest.add("client_id", crmClientId);
        HttpEntity<MultiValueMap<String, String>> tokenRequestEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
        OauthToken oauthToken = getResponseForPostRequest(crmHost + CRM_CORE_GENERATE_TOKEN, tokenRequestEntity, OauthToken.class, "", HOST_PARTY.CRM);
        if (oauthToken == null) {
            log.error("[CRM]--Can not get token from CRM");
            return null;
        }
        return oauthToken;
    }

    @SuppressWarnings("unused")
    private TransferNonOtpRespDto checkingBaasTranStatus(String accessToken, String transactionId) {
        HttpHeaders mbalTransferHeaders = new HttpHeaders();
        mbalTransferHeaders.setContentType(APPLICATION_JSON);
        mbalTransferHeaders.set(AUTHORIZATION, BEARER + accessToken);
        mbalTransferHeaders.set(CLIENT_MESSAGE_ID, generateUUIDId(31));
        HttpEntity<Object> entity = new HttpEntity<>(mbalTransferHeaders);
        return restTemplate.exchange(
                baasHost + BAAS_CHECK_STATUS_TRAN,
                HttpMethod.GET,
                entity,
                TransferNonOtpRespDto.class,
                transactionId).getBody();
    }

    private String generateSign(String data) {
        try {
            Signature rsa = Signature.getInstance(SHA256_INSTANCE);
            rsa.initSign(privateKey);
            rsa.update(data.getBytes());
            return Base64.getEncoder().encodeToString(rsa.sign());
        } catch (Exception e) {
            return ERROR_SIGN;
        }
    }

    private String generateSignData(String creditResourceNumber, String creditName, String transferAmount) {
        return baasBankAccount + baasAccountName + creditResourceNumber + creditName + transferAmount;
    }

    private TransferNonOtpReqDto genCommonTransferNonOtpReqDto() {
        TransferNonOtpReqDto.AddInfo addInfo = new TransferNonOtpReqDto.AddInfo();
        addInfo.setName("");
        addInfo.setType("");
        addInfo.setValue("");
        List<TransferNonOtpReqDto.AddInfo> addInfoList = new ArrayList<>();
        addInfoList.add(addInfo);
        TransferNonOtpReqDto nonOtpReqDto = new TransferNonOtpReqDto();
        nonOtpReqDto.setCustomerLevel("1");
        nonOtpReqDto.setCustomerType("DOANH_NGHIEP");
        nonOtpReqDto.setBankCode("");
        nonOtpReqDto.setCreditType("ACCOUNT");
        nonOtpReqDto.setDebitName(baasAccountName);
        nonOtpReqDto.setDebitResourceNumber(baasBankAccount);
        nonOtpReqDto.setDebitType("ACCOUNT");
        nonOtpReqDto.setServiceType("CHI_HO");

        nonOtpReqDto.setTransferFee("0");
        nonOtpReqDto.setTransferType("INHOUSE");
        nonOtpReqDto.setAddInfoList(addInfoList);

        return nonOtpReqDto;
    }

    private <T> T getResponseForPostRequest(String host, HttpEntity<?> entity, Class<T> clazz, String payloadLog, Constants.HOST_PARTY hostParty) {
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
            return clazz.cast(exchange.getBody());
        } catch (Exception e) {
            log.error("[BAAS]--Call to API error {}, detail {}", host, e.getMessage());
            LocalDateTime receivedTime = LocalDateTime.now();
            asyncObject.saveErrorLog(hostParty, HttpMethod.POST, host, payloadLog, 500, e.getMessage(), sendTime, receivedTime);
            throw new BaasApiException("Lỗi kết nối tới hệ thống BAAS");
        }
    }

    private BaasPayOnBehalfRespDto payOnBehalfManual(BaasPayOnBehalf baasPayOnBehalf, TransferNonOtpReqDto nonOtpReqDto,
                                                     InsurancePayment insurancePayment, InsuranceContract insuranceContract, String type) {

        BaasPayOnBehalfRespDto respDto = new BaasPayOnBehalfRespDto();
        String baasTransactionId = baasChannel + baasBusinessCode + generateTransactionId();
        OauthToken oauthToken = generateToken();
        if (oauthToken.getAccessToken() == null) {
            throw new BaasApiException("Lỗi tạo accessToken khi chi hộ");
        }
        String clientMessageId = generateUUIDId(31);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set(AUTHORIZATION, BEARER + oauthToken.getAccessToken());
        httpHeaders.set(CLIENT_MESSAGE_ID, clientMessageId);
        httpHeaders.set(TRANSACTION_ID, baasTransactionId);
        if (type.equals(MIC)) {
            String micAmount = String.valueOf(baasPayOnBehalf.getAmount());
            nonOtpReqDto.setTransferAmount(micAmount);
            nonOtpReqDto.setRemark(REMARK_TRANSFER + baasTransactionId + "-" + insuranceContract.getTransactionId() + "-Nop phi cho GCN [" + baasPayOnBehalf.getMicContractNum() + "]");
            nonOtpReqDto.setCreditName(micAccountName);
            nonOtpReqDto.setCreditResourceNumber(micBankAccount);
            String signature = generateSign(generateSignData(micBankAccount, micAccountName, micAmount));
            if (Objects.equals(signature, ERROR_SIGN)) {
                log.error("[MINI]--Chi hộ manual sang MIC lỗi do không tạo được chữ ký số với transactionId {}", baasTransactionId);
                respDto.setMessage("Chi hộ manual MIC lỗi khi gen signature");
                respDto.setStatus(Status.FAIL);
                return respDto;
            }
            httpHeaders.set(SIGNATURE, signature);
            HttpEntity<TransferNonOtpReqDto> transferRequestEntity = new HttpEntity<>(nonOtpReqDto, httpHeaders);
            log.info("[MINI]--Call chi hộ manual " + MIC + BAAS_TRANSACTION_ID_REQUEST_DATA, baasTransactionId, nonOtpReqDto);
            String response;
            try {
                response = restTemplate.exchange(baasHost + BAAS_MAKE_TRANSFER_PARTNER, HttpMethod.POST, transferRequestEntity, String.class).getBody();
            } catch (Exception e) {
                log.error(BAAS_DETAIL_ERROR_MESSAGE_FOR_BAAS_TRANSACTION_ID_DETAIL, baasTransactionId, e.getMessage());
                baasPayOnBehalf.setClientMessageId(clientMessageId);
                baasPayOnBehalf.setDetailErrorMessage(e.getMessage());
                baasPayOnBehalfRepository.save(baasPayOnBehalf);
                respDto.setMessage(MSG33);
                respDto.setStatus(Status.FAIL);
                String payloadLog = getPayloadLogObject(nonOtpReqDto);
                asyncObject.saveErrorLog(HOST_PARTY.BAAS, HttpMethod.POST, baasHost + BAAS_MAKE_TRANSFER_PARTNER, payloadLog,
                        500, e.getMessage(), LocalDateTime.now(), LocalDateTime.now());
                return respDto;
            }

            TransferNonOtpRespDto transferResult = gson.fromJson(response, TransferNonOtpRespDto.class);
            log.info("[MINI]--Kết quả chi hộ manual " + MIC + BAAS_TRANSACTION_ID_TRANSFER_RESULT, baasTransactionId, transferResult == null ? FALSE : transferResult.toString());

            return updateBaasPayOnBehalf(baasPayOnBehalf, transferResult);
        } else {
            String mbalAmount = String.valueOf(baasPayOnBehalf.getAmount());
            nonOtpReqDto.setTransferAmount(mbalAmount);
            nonOtpReqDto.setRemark(REMARK_TRANSFER + baasTransactionId + "-" + insuranceContract.getTransactionId() + NOP_PHI_CHO_MB_AGEAS_LIFE + insuranceContract.getMbalAppNo());
            nonOtpReqDto.setCreditName(mbalAccountName);
            nonOtpReqDto.setCreditResourceNumber(mbalBankAccount);
            String signature = generateSign(generateSignData(mbalBankAccount, mbalAccountName, mbalAmount));
            if (Objects.equals(signature, ERROR_SIGN)) {
                log.error("[MINI]--Chi hộ manual sang MBAL lỗi do không tạo được chữ ký số với transactionId {}", baasTransactionId);
                respDto.setMessage("Chi hộ manual MBAL lỗi khi gen signature");
                respDto.setStatus(Status.FAIL);
                return respDto;
            }
            httpHeaders.set(SIGNATURE, signature);
            HttpEntity<TransferNonOtpReqDto> transferRequestEntity = new HttpEntity<>(nonOtpReqDto, httpHeaders);
            log.info("[MINI]--Call chi hộ manual " + MBAL + BAAS_TRANSACTION_ID_REQUEST_DATA, baasTransactionId, nonOtpReqDto);
            String response;
            try {
                response = restTemplate.exchange(baasHost + BAAS_MAKE_TRANSFER_PARTNER, HttpMethod.POST, transferRequestEntity, String.class).getBody();
            } catch (Exception e) {
                log.error(BAAS_DETAIL_ERROR_MESSAGE_FOR_BAAS_TRANSACTION_ID_DETAIL, baasTransactionId, e.getMessage());
                baasPayOnBehalf.setClientMessageId(clientMessageId);
                baasPayOnBehalf.setDetailErrorMessage(e.getMessage());
                baasPayOnBehalfRepository.save(baasPayOnBehalf);
                respDto.setMessage(MSG33);
                respDto.setStatus(Status.FAIL);
                String payloadLog = getPayloadLogObject(nonOtpReqDto);
                asyncObject.saveErrorLog(HOST_PARTY.BAAS, HttpMethod.POST, baasHost + BAAS_MAKE_TRANSFER_PARTNER, payloadLog,
                        500, e.getMessage(), LocalDateTime.now(), LocalDateTime.now());
                return respDto;
            }
            TransferNonOtpRespDto transferResult = gson.fromJson(response, TransferNonOtpRespDto.class);
            log.info("[MINI]--Kết quả chi hộ manual " + type + BAAS_TRANSACTION_ID_TRANSFER_RESULT,
                    baasTransactionId, transferResult == null ? FALSE : transferResult.toString());
            baasPayOnBehalf.setBaasTransactionId(baasTransactionId);
            boolean hookValue = hookMbalManualAfterPayOnBehalf(baasPayOnBehalf, insuranceContract.getMbalAppNo(), insurancePayment.getCustomer().getMbId());
            log.debug("[MINI]--Hook value {}", hookValue);
            return updateBaasPayOnBehalf(baasPayOnBehalf, transferResult);
        }
    }

    private void payOnBehalfForMic(TransferNonOtpReqDto nonOtpReqDto, String accessToken, String amount,
                                   String gcnbh, InsurancePayment insurancePayment, String mbTransactionId) {
        String baasTransactionId = baasChannel + baasBusinessCode + generateTransactionId();
        BaasPayOnBehalf baasPayOnBehalf = new BaasPayOnBehalf();
        baasPayOnBehalf.setInsurancePayment(insurancePayment);
        baasPayOnBehalf.setBaasTransactionId(baasTransactionId);
        baasPayOnBehalf.setAmount(Long.valueOf(amount));
        baasPayOnBehalf.setType(MIC);
        baasPayOnBehalf.setMbTransactionId(mbTransactionId);
        baasPayOnBehalf.setMicContractNum(gcnbh);

        String clientMessageId = generateUUIDId(31);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set(AUTHORIZATION, BEARER + accessToken);
        httpHeaders.set(CLIENT_MESSAGE_ID, clientMessageId);
        httpHeaders.set(TRANSACTION_ID, baasTransactionId);

        nonOtpReqDto.setTransferAmount(amount);
        nonOtpReqDto.setRemark(REMARK_TRANSFER + baasTransactionId + "-" + mbTransactionId + "-Nop phi cho GCN [" + gcnbh + "]");
        nonOtpReqDto.setCreditName(micAccountName);
        nonOtpReqDto.setCreditResourceNumber(micBankAccount);
        String signature = generateSign(generateSignData(micBankAccount, micAccountName, amount));
        if (Objects.equals(signature, ERROR_SIGN)) {
            log.error("[MIC]--Chi hộ sang MIC lỗi do không tạo được chữ ký số với transactionId {}", baasTransactionId);
        }

        httpHeaders.set(SIGNATURE, signature);
        HttpEntity<TransferNonOtpReqDto> transferRequestEntity = new HttpEntity<>(nonOtpReqDto, httpHeaders);
        log.info("Call chi hộ " + MIC + BAAS_TRANSACTION_ID_REQUEST_DATA, baasTransactionId, nonOtpReqDto);

        String response;
        try {
            response = restTemplate.exchange(baasHost + BAAS_MAKE_TRANSFER_PARTNER, HttpMethod.POST, transferRequestEntity, String.class).getBody();
        } catch (Exception e) {
            log.error(BAAS_DETAIL_ERROR_MESSAGE_FOR_BAAS_TRANSACTION_ID_DETAIL, baasTransactionId, e.getMessage());
            baasPayOnBehalf.setClientMessageId(clientMessageId);
            baasPayOnBehalf.setDetailErrorMessage(e.getMessage());
            baasPayOnBehalf.setStatus(Status.FAIL);
            baasPayOnBehalfRepository.save(baasPayOnBehalf);
            return;
        }

        TransferNonOtpRespDto transferResult = gson.fromJson(response, TransferNonOtpRespDto.class);
        log.info("Kết quả chi hộ " + MIC + BAAS_TRANSACTION_ID_TRANSFER_RESULT, baasTransactionId, transferResult == null ? FALSE : transferResult.toString());

        updateBaasPayOnBehalf(baasPayOnBehalf, transferResult);

    }

    private void payOnBehalfForMbal(TransferNonOtpReqDto nonOtpReqDto, String accessToken, String amount, String appNoId,
                                    InsurancePayment insurancePayment, String mbTransactionId, String ullVersion, String cif,
                                    String fundingSource, String cardType, String createOrderId, String hookTypeCode) {
        String baasTransactionId = baasChannel + baasBusinessCode + generateTransactionId();
        BaasPayOnBehalf baasPayOnBehalf = new BaasPayOnBehalf();
        String clientMessageId = generateUUIDId(31);
        baasPayOnBehalf.setInsurancePayment(insurancePayment);
        baasPayOnBehalf.setBaasTransactionId(baasTransactionId);
        baasPayOnBehalf.setMbTransactionId(mbTransactionId);
        baasPayOnBehalf.setAmount(Long.valueOf(amount));
        baasPayOnBehalf.setType(MBAL);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set(AUTHORIZATION, BEARER + accessToken);
        httpHeaders.set(CLIENT_MESSAGE_ID, clientMessageId);
        httpHeaders.set(TRANSACTION_ID, baasTransactionId);

        // Transfer money to MBAL without OTP
        nonOtpReqDto.setTransferAmount(amount);
        nonOtpReqDto.setRemark(REMARK_TRANSFER + baasTransactionId + "-" + mbTransactionId + NOP_PHI_CHO_MB_AGEAS_LIFE + appNoId);
        if (ullVersion.equals(ULL_V2)) {
            nonOtpReqDto.setRemark(REMARK_TRANSFER + createOrderId + "-" + mbTransactionId + NOP_PHI_CHO_MB_AGEAS_LIFE + appNoId);
        }
        nonOtpReqDto.setCreditName(mbalAccountName);
        nonOtpReqDto.setCreditResourceNumber(mbalBankAccount);
        String signature = generateSign(generateSignData(mbalBankAccount, mbalAccountName, amount));

        if (Objects.equals(signature, ERROR_SIGN)) {
            log.error("[BAAS]--Chi hộ sang MBAL lỗi do không tạo được chữ ký số với baasTransactionId {}", baasTransactionId);
            baasPayOnBehalf.setStatus(Status.FAIL);
            baasPayOnBehalfRepository.save(baasPayOnBehalf);
            return;
        }

        httpHeaders.set(SIGNATURE, signature);
        HttpEntity<TransferNonOtpReqDto> transferRequestEntity = new HttpEntity<>(nonOtpReqDto, httpHeaders);
        log.info("Call chi hộ " + MBAL + BAAS_TRANSACTION_ID_REQUEST_DATA, baasTransactionId, nonOtpReqDto);

        String response;
        try {
            response = restTemplate.exchange(baasHost + BAAS_MAKE_TRANSFER_PARTNER, HttpMethod.POST, transferRequestEntity, String.class).getBody();
        } catch (Exception e) {
            log.error("[BAAS]--Detail error message for baasTransactionId {}. Detail: {}", baasTransactionId, e.getMessage());
            baasPayOnBehalf.setClientMessageId(clientMessageId);
            baasPayOnBehalf.setDetailErrorMessage(e.getMessage());
            baasPayOnBehalf.setStatus(Status.FAIL);
            baasPayOnBehalfRepository.save(baasPayOnBehalf);
            return;
        }

        TransferNonOtpRespDto transferResult = gson.fromJson(response, TransferNonOtpRespDto.class);
        log.info("Kết quả chi hộ " + MBAL + " voi transactionId {}. Transfer result: {}", baasTransactionId, transferResult == null ? FALSE : transferResult.toString());
        updateBaasPayOnBehalf(baasPayOnBehalf, transferResult);
        if (transferResult != null && transferResult.getErrorCode().equals("000")) {
            if (ullVersion.equals(ULL_V2)) {
                MbalHookReqDto hookReqDto = new MbalHookReqDto();
                hookReqDto.setCreatedTime(getIsoTimeFromDate());
                hookReqDto.setPaidTime(getIsoTimeFromDate());
                hookReqDto.setCif(cif);
                hookReqDto.setId(createOrderId);
                hookReqDto.setAmount(Long.valueOf(amount));
                hookReqDto.setMetadata(appNoId);
                hookReqDto.setSuccessMessage(String.format(MSG24, appNoId));
                hookReqDto.setFundingSource(fundingSource);
                hookReqDto.setCardType(cardType);
                hookReqDto.setDescription(appNoId + THANH_TOAN_CHO_HSYC_BAO_HIEM);
                MbalHookReqDto.Type type = new MbalHookReqDto.Type();
                type.setCode(hookTypeCode);
                type.setName("Thanh toán Phí bảo hiểm lần đầu (thẻ)");
                type.setAllowCard(true);
                hookReqDto.setType(type);
                mbalHookMarkOrderAsPaid(hookReqDto);
            } else {
                PaymentNotificationV2ReqDto notificationV2ReqDto = new PaymentNotificationV2ReqDto();
                notificationV2ReqDto.setApplicationNumber(appNoId);
                notificationV2ReqDto.setSuccess(true);
                notificationV2ReqDto.setNote(String.format(MSG24, appNoId));
                PaymentNotificationV2ReqDto.Transaction transaction = new PaymentNotificationV2ReqDto.Transaction();
                transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount)));
                transaction.setCode(mbTransactionId);
                transaction.setDescription(appNoId + THANH_TOAN_CHO_HSYC_BAO_HIEM);
                transaction.setCompletedAt(getIsoTimeFromDate());
                transaction.setPaymentId(baasTransactionId);
                notificationV2ReqDto.setTransaction(transaction);
                PaymentNotificationV2ReqDto.Source source = new PaymentNotificationV2ReqDto.Source();
                source.setAccountId(null);
                source.setCif(cif);
                source.setPhoneNumber(null);
                source.setName(null);
                notificationV2ReqDto.setSource(source);
                log.info("Hook data for ULL_V3 with request data {}", notificationV2ReqDto);
                PaymentNotificationV2RespDto v2RespDto;
                try {
                    v2RespDto = v2ApiService.paymentNotifications(notificationV2ReqDto);
                    baasPayOnBehalf.setMbalHookStatus("success".equalsIgnoreCase(v2RespDto.getResult()));
                } catch (Exception e) {
                    log.error("Exception when call hook to MBAL for appNo {}", appNoId);
                    baasPayOnBehalf.setMbalHookStatus(false);
                    //: Todo: saving log third party after
                }
            }
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void flexiblePayOnBehalfForMbal(TransferNonOtpReqDto nonOtpReqDto, String accessToken, String amount, String appNoId,
                                    InsurancePayment insurancePayment, String mbTransactionId, String cif) {
        String baasTransactionId = baasChannel + baasBusinessCode + generateTransactionId();
        BaasPayOnBehalf baasPayOnBehalf = new BaasPayOnBehalf();
        String clientMessageId = generateUUIDId(31);
        baasPayOnBehalf.setInsurancePayment(insurancePayment);
        baasPayOnBehalf.setBaasTransactionId(baasTransactionId);
        baasPayOnBehalf.setMbTransactionId(mbTransactionId);
        baasPayOnBehalf.setAmount(Long.valueOf(amount));
        baasPayOnBehalf.setType(MBAL);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(APPLICATION_JSON);
        httpHeaders.set(AUTHORIZATION, BEARER + accessToken);
        httpHeaders.set(CLIENT_MESSAGE_ID, clientMessageId);
        httpHeaders.set(TRANSACTION_ID, baasTransactionId);

        // Transfer money to MBAL without OTP
        nonOtpReqDto.setTransferAmount(amount);
        nonOtpReqDto.setRemark(REMARK_TRANSFER + baasTransactionId + "-" + mbTransactionId + NOP_PHI_CHO_MB_AGEAS_LIFE + appNoId);
        nonOtpReqDto.setCreditName(mbalAccountName);
        nonOtpReqDto.setCreditResourceNumber(mbalBankAccount);
        String signature = generateSign(generateSignData(mbalBankAccount, mbalAccountName, amount));

        if (Objects.equals(signature, ERROR_SIGN)) {
            log.error("[BAAS]--Chi hộ sang MBAL lỗi do không tạo được chữ ký số với baasTransactionId {}", baasTransactionId);
            baasPayOnBehalf.setStatus(Status.FAIL);
            baasPayOnBehalfRepository.save(baasPayOnBehalf);
            return;
        }

        httpHeaders.set(SIGNATURE, signature);
        HttpEntity<TransferNonOtpReqDto> transferRequestEntity = new HttpEntity<>(nonOtpReqDto, httpHeaders);
        log.info("Call chi hộ " + MBAL + BAAS_TRANSACTION_ID_REQUEST_DATA, baasTransactionId, nonOtpReqDto);

        String response;
        try {
            response = restTemplate.exchange(baasHost + BAAS_MAKE_TRANSFER_PARTNER, HttpMethod.POST, transferRequestEntity, String.class).getBody();
        } catch (Exception e) {
            log.error("[BAAS]--Detail error message for baasTransactionId {}. Detail: {}", baasTransactionId, e.getMessage());
            baasPayOnBehalf.setClientMessageId(clientMessageId);
            baasPayOnBehalf.setDetailErrorMessage(e.getMessage());
            baasPayOnBehalf.setStatus(Status.FAIL);
            baasPayOnBehalfRepository.save(baasPayOnBehalf);
            return;
        }

        TransferNonOtpRespDto transferResult = gson.fromJson(response, TransferNonOtpRespDto.class);
        log.info("Kết quả chi hộ " + MBAL + " voi transactionId {}. Transfer result: {}", baasTransactionId, transferResult == null ? FALSE : transferResult.toString());
        updateBaasPayOnBehalf(baasPayOnBehalf, transferResult);
        if (transferResult != null && transferResult.getErrorCode().equals("000")) {
            PaymentNotificationFlexibleReqDto reqDto = new PaymentNotificationFlexibleReqDto();
            reqDto.setApplicationNumber(appNoId);
            reqDto.setSuccess(true);
            reqDto.setNote(String.format(MSG24, appNoId));
            PaymentNotificationFlexibleReqDto.Transaction transaction = new PaymentNotificationFlexibleReqDto.Transaction();
            transaction.setAmount(BigDecimal.valueOf(Long.parseLong(amount)));
            transaction.setCode(mbTransactionId);
            transaction.setDescription(appNoId + THANH_TOAN_CHO_HSYC_BAO_HIEM);
            transaction.setCompletedAt(getIsoTimeFromDate());
            transaction.setPaymentId(baasTransactionId);
            reqDto.setTransaction(transaction);
            PaymentNotificationFlexibleReqDto.Source source = new PaymentNotificationFlexibleReqDto.Source();
            source.setAccountId(null);
            source.setCif(cif);
            source.setPhoneNumber(null);
            source.setName(null);
            reqDto.setSource(source);
            log.info("Hook data for flexible insurance with request data {}", reqDto);
            PaymentNotificationFlexibleRespDto notification;
            try {
                notification = flexibleApiService.paymentNotification(reqDto);
                baasPayOnBehalf.setMbalHookStatus("success".equalsIgnoreCase(notification.getResult()));
                log.info("Hook flexible response {}", notification.getResult());
            } catch (Exception e) {
                log.error("Exception when call hook flexible to MBAL for appNo {}", appNoId);
                baasPayOnBehalf.setMbalHookStatus(false);
                //: Todo: saving log third party after
            }
        }
    }

    private boolean hookMbalManualAfterPayOnBehalf(BaasPayOnBehalf baasMbal, String appNoId, String cif) {
        PaymentNotificationV2ReqDto notificationV2ReqDto = new PaymentNotificationV2ReqDto();
        notificationV2ReqDto.setApplicationNumber(appNoId);
        notificationV2ReqDto.setSuccess(true);
        notificationV2ReqDto.setNote(String.format(MSG24, appNoId));
        PaymentNotificationV2ReqDto.Transaction transaction = new PaymentNotificationV2ReqDto.Transaction();
        transaction.setAmount(BigDecimal.valueOf(baasMbal.getAmount()));
        transaction.setCode(baasMbal.getMbTransactionId());
        transaction.setDescription(appNoId + THANH_TOAN_CHO_HSYC_BAO_HIEM);
        transaction.setCompletedAt(getIsoTimeFromDate());
        transaction.setPaymentId(baasMbal.getBaasTransactionId());
        notificationV2ReqDto.setTransaction(transaction);
        PaymentNotificationV2ReqDto.Source source = new PaymentNotificationV2ReqDto.Source();
        source.setAccountId(null);
        source.setCif(cif);
        source.setPhoneNumber(null);
        source.setName(null);
        notificationV2ReqDto.setSource(source);
//        log.info("Hook MBAL sau khi chi hộ ULL_V3 với request data {}", notificationV2ReqDto);
//        PaymentNotificationV2RespDto v2RespDto = v2ApiService.paymentNotifications(notificationV2ReqDto);
        PaymentNotificationV2RespDto v2RespDto = new PaymentNotificationV2RespDto();
        try {
            v2RespDto = v2ApiService.paymentNotifications(notificationV2ReqDto);
            log.info("Hook mbal manual response {}", v2RespDto.getResult());
            if (v2RespDto.getResult().equalsIgnoreCase("success")) {
                baasMbal.setMbalHookStatus(true);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Exception when call hook to MBAL for appNo {}", appNoId);
            baasMbal.setMbalHookStatus(false);
            return false;
        }
    }

    private BaasPayOnBehalfRespDto updateBaasPayOnBehalf(BaasPayOnBehalf baasPayOnBehalf, TransferNonOtpRespDto transferResult) {
        BaasPayOnBehalfRespDto respDto = new BaasPayOnBehalfRespDto();
        respDto.setStatus(Status.FAIL);
        respDto.setMessage(MSG30);
        if (transferResult != null) {
            baasPayOnBehalf.setClientMessageId(transferResult.getClientMessageId());
            baasPayOnBehalf.setErrorCode(transferResult.getErrorCode());
            baasPayOnBehalf.setFtNumber(transferResult.getData() == null ? null : transferResult.getData().getFtNumber());
            baasPayOnBehalf.setTranStatus(transferResult.getData().getTransStatus());
            if (transferResult.getErrorCode().equals("000")) {
                respDto.setStatus(Status.SUCCESS);
                respDto.setMessage(MSG29);
                baasPayOnBehalf.setStatus(Status.SUCCESS);
            }
        }
        baasPayOnBehalfRepository.save(baasPayOnBehalf);
        return respDto;
    }

    private void mbalHookMarkOrderAsPaid(MbalHookReqDto hookReqDto) {
        MbalHookReqDto.Merchant merchant = new MbalHookReqDto.Merchant();
        merchant.setCode(mbalMerchantCode);
        merchant.setName("Bảo hiểm nhân thọ MB Ageas Life");
        hookReqDto.setMerchant(merchant);
        hookReqDto.setStatus(PAID);
        hookReqDto.setChecksum(generateMbalChecksum(hookReqDto));

        HttpHeaders headers = generateDefaultHeader();
        headers.add(PAYMENT_HUB_KEY, mbalPaymentHubKey);
        headers.add(PAYMENT_HUB_SECRET, mbalPaymentHubSecret);
        HttpEntity<MbalHookReqDto> entity = new HttpEntity<>(hookReqDto, headers);

        String payloadLog = getPayloadLogObject(hookReqDto);
        getResponseForPostRequest(mbalPortalHost + MBAL_MARK_ORDER_AS_PAID, entity, String.class, payloadLog, Constants.HOST_PARTY.MBAL);

    }

    private static String getPayloadLogObject(Object reqDto) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(reqDto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("[MINI]--Lỗi khi convert error data to json {}", reqDto);
            return null;
        }
    }

    private String generateMbalChecksum(MbalHookReqDto hookReqDto) {
        String data = hookReqDto.getMerchant().getCode() + hookReqDto.getId() + hookReqDto.getType().getCode()
                + hookReqDto.getCif() + hookReqDto.getAmount() + hookReqDto.getStatus();
        byte[] hmac = new HmacUtils(HMAC_SHA_256, mbalChecksumSecret).hmac(data);
        return Base64.getEncoder().encodeToString(hmac);
    }

    private void verifyCustomerPermission(String cif, String processId) {
        if (!processId.equals(redis.get(processIdKey(cif)))) {
            throw new MiniApiException(MSG13);
        }
    }

    private Map<String, String> revertMetadata(String metadata) {
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (JsonProcessingException e) {
            log.error("[Mini]--Exception when revert metadata");
            throw new ApplicationException(MSG12);
        }
    }
}
