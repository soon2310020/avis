package com.stg.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.stg.entity.MicPackage;
import com.stg.errors.MbalApiException;
import com.stg.errors.MicApiException;
import com.stg.errors.MiniApiException;
import com.stg.repository.MicPackageRepository;
import com.stg.service.ExternalMicAPIService;
import com.stg.service.MicPackageService;
import com.stg.service.dto.external.request.*;
import com.stg.service.dto.external.requestFlexible.MicAdditionalProduct;
import com.stg.service.dto.external.response.*;
import com.stg.service.dto.external.responseV2.MicInsuranceBenefitV2Dto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mic_sandbox.MicSandboxApi3rdService;
import com.stg.service3rd.mic_sandbox.exception.MicApi3rdException;
import com.stg.utils.*;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.CommonUtils.replaceWithAsterisks;
import static com.stg.utils.Constants.MIC_CODE_00;
import static com.stg.utils.Constants.MIC_CODE_02;
import static com.stg.utils.DateUtil.*;
import static com.stg.utils.Endpoints.*;
import static com.stg.utils.FlexibleCommon.*;
import static com.stg.utils.Gender.MALE;
import static com.stg.utils.MicUtil.*;
import static com.stg.utils.MicUtil.genParentContract;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_1;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ExternalMicAPIServiceImpl implements ExternalMicAPIService {

    private static final String NO = "K";
    private static final String YES = "C";
    private static final String NV_CODE = "NG_SKC";
    private static final String CONTRACT_TYPE = "G";
    private static final String MIC_PRODUCT_CODE = "MICCARE_MBAL_2";
    private static final String MIC_NO = "Không";

    @Value("${external.host.mic-host}")
    private String micHost;

    @Value("${external.mic-key.merchant-secret}")
    private String micMerchantSecret;
    @Value("${external.mic-key.dvi-code}")
    private String dviCode;
    @Value("${external.mic-key.nsd}")
    private String nsd;
    @Value("${external.mic-key.pas}")
    private String pas;

    @Value("${external.mic-key.username}")
    private String username;
    @Value("${external.mic-key.password}")
    private String password;
    @Value("${external.mic-key.client_id}")
    private String clientId;
    @Value("${external.mic-key.client_secret}")
    private String clientSecret;

    @Value("${external.host.mic-host-sandbox}")
    private String micHostSandbox;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final RedisCommands<String, String> redis;

    private final AsyncObjectImpl asyncObject;
    private final MicPackageService micPackageService;
    private final MicPackageRepository micPackageRepository;
    private final MicSandboxApi3rdService micSandboxApi3rdService;

    @Override
    public MicInsuranceResultRespDto micFeeResult(MiniMicFeeReqDto miniMicFeeReqDto) {
        long t1 = System.currentTimeMillis();
        validateInsuranceAge(miniMicFeeReqDto.getDob());
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(dviCode);
        reqDto.setNsd(nsd);
        reqDto.setPas(pas);
        reqDto.setId_tras("");
        reqDto.setNg_sinh(convertFormat(miniMicFeeReqDto.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNhom(convertGroupMic(miniMicFeeReqDto.getNhom()));
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setGcn_miccare_dkbs(miniMicFeeReqDto.getGcn_miccare_dkbs());
        reqDto.setChecksum(generateMicChecksum(null));
        HttpEntity<MicInsuranceResultReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeader());
        String urlMicInsurance = micHost + MIC_INSURANCE_RESULT;

        String payloadLog = getPayloadLogObject(reqDto);

        log.debug("Calling micFeeResult url = {}", urlMicInsurance);
        MicInsuranceResultRespDto respDto = getResponseForPostRequest(urlMicInsurance, entity, MicInsuranceResultRespDto.class, payloadLog);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            throw new MicApiException(respDto.getMessage());
        }
        long t2 = System.currentTimeMillis();
        log.debug("---- (Millis) time to call micFeeResult mbal = " + (t2 - t1));
        return respDto;
    }

    @Override
    @Async
    public void micGenerateInsuranceCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                         long micFee, String transactionId, int nhom,
                                         Common.GcnMicCareDkbs gcnMicCareDkbs) {
        log.info("Start gen GCNBH voi transactionId {}", transactionId);
        long startTime = System.currentTimeMillis();
        MicGenerateInsuranceCertReqDto certReqDto = new MicGenerateInsuranceCertReqDto();
        certReqDto.setTtoan((double) micFee);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd gcnMicCareTtinhd = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd();
        gcnMicCareTtinhd.setNhom(convertGroupMic(nhom));
        gcnMicCareTtinhd.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        gcnMicCareTtinhd.setSo_hd_bm("");
        gcnMicCareTtinhd.setMa_sp(MIC_PRODUCT_CODE);
        gcnMicCareTtinhd.setGcn_miccare_dkbs(gcnMicCareDkbs);

        certReqDto.setGcn_miccare_ttin_hd(gcnMicCareTtinhd);
        certReqDto.setGcn_miccare_ttin_kh(ttinkh);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtsk gcnMicCareTtsk = new MicGenerateInsuranceCertReqDto.GcnMicCareTtsk();
        gcnMicCareTtsk.setCk(NO);
        gcnMicCareTtsk.setCc(NO);
        gcnMicCareTtsk.setCu(NO);
        certReqDto.setGcn_miccare_ttsk(gcnMicCareTtsk);
        certReqDto.setNv(NV_CODE);
        certReqDto.setKieu_hd(CONTRACT_TYPE);
        certReqDto.setMa_dvi(dviCode);
        certReqDto.setNsd(nsd);
        certReqDto.setPas(pas);
        certReqDto.setId_tras(transactionId);
        certReqDto.setChecksum(generateMicChecksum(transactionId));
        log.info("[MIC]--- Thông tin quyền lợi GCNBH MIC {}", gcnMicCareTtinhd);
        HttpEntity<MicGenerateInsuranceCertReqDto> entity = new HttpEntity<>(certReqDto, generateDefaultHeader());

        String payloadLog = getPayloadLogMicGenCert(certReqDto);
        MicGenerateInsuranceCertRespDto response = getResponseForPostRequest(micHost + MIC_GENERATE_INSURANCE_CERT, entity, MicGenerateInsuranceCertRespDto.class, payloadLog);

        log.debug("Total time micGenerateInsuranceCert with transactionId {} is {}. Response data with code {}, message {}",
                transactionId, System.currentTimeMillis() - startTime, response.getCode(), response.getMessage());
    }

    @Override
    public MicSearchInsuranceCertRespDto micSearchInsuranceCert(String cif, String transactionId) {
        if (!redis.get(micTransactionIdKey(cif)).equals(transactionId)) {
            log.error("[MINI]--Lỗi thay đổi mic transactionID");
            throw new MiniApiException(MSG13);
        }
        long t1 = System.currentTimeMillis();
        MicSearchInsuranceCertReqDto reqDto = new MicSearchInsuranceCertReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(dviCode);
        reqDto.setNsd(nsd);
        reqDto.setPas(pas);
        reqDto.setId_tras(transactionId);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setChecksum(generateMicChecksum(transactionId));
        HttpEntity<MicSearchInsuranceCertReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeader());
        String payloadLog = getPayloadLogObject(reqDto);
        MicSearchInsuranceCertRespDto certRespDto = getResponseForPostRequest(micHost + MIC_SEARCH_INSURANCE_CERT, entity, MicSearchInsuranceCertRespDto.class, payloadLog);
        if (certRespDto.getCode().equals(MIC_CODE_02)) {
            log.error("[MIC]--Lỗi khi lấy GCNBH MIC. code {}, message {}", certRespDto.getCode(), certRespDto.getMessage());
            throw new MicApiException(MSG12);
        }
        long t2 = System.currentTimeMillis();
        log.debug("---- (Millis) time to call micSearchInsuranceCert mbal = " + (t2 - t1));
        return certRespDto;
    }

    @Override
    public MiniMicFeeReqDto genMicFeeReqDto(String dob, int nhom) {
        MiniMicFeeReqDto micFeeReqDto = new MiniMicFeeReqDto();
        micFeeReqDto.setNhom(nhom);
        micFeeReqDto.setDob(dob);
        Common.GcnMicCareDkbs gcnMicCareDkbs = new Common.GcnMicCareDkbs();
        gcnMicCareDkbs.setBs1(NO);
        gcnMicCareDkbs.setBs2(NO);
        gcnMicCareDkbs.setBs3(NO);
        gcnMicCareDkbs.setBs4(NO);
        micFeeReqDto.setGcn_miccare_dkbs(gcnMicCareDkbs);

        return micFeeReqDto;
    }

    @Override
    public MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicCareTtinkh(String fullname, String idCardNo, String gender,
                                                                                 String dob, String address, String email, String phone) {
        MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh();
        ttinkh.setLkh(YES);
        ttinkh.setTen(fullname);
        ttinkh.setCmt(idCardNo);
        ttinkh.setGioi(gender.equals("MALE") ? "1" : "2");
        ttinkh.setNg_sinh(convertFormat(dob, DATE_YYYY_MM_DD, DATE_DMY));
        ttinkh.setDchi(address);
        ttinkh.setEmail(email);
        ttinkh.setMobi(phone);
        return ttinkh;
    }

    @Override
    public List<MicInsuranceBenefitV2Dto> retrieveMicPackages(MicPackageReqDto reqDto) {

        validateAdditionalFxMicInsuranceAge(reqDto.getDob());
        List<MicPackage> micPackages = micPackageService.findAll();
        List<MicInsuranceBenefitV2Dto> micInsuranceBenefits = micPackages.stream()
                .map(micPackage -> modelMapper.map(micPackage, MicInsuranceBenefitV2Dto.class))
                .collect(Collectors.toList());
        List<MicInsuranceBenefitV2Dto> micBenefits = new ArrayList<>();

        //BRONZE
        Optional<MicInsuranceBenefitV2Dto> insuranceBenefitId1 = micInsuranceBenefits.stream().filter(o -> o.getId() == 1).findFirst();
        if (insuranceBenefitId1.isPresent()) {
            MicInsuranceBenefitV2Dto micBronzer = insuranceBenefitId1.get();
            MiniMicFeeReqDto bronzerMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 1);
            MicInsuranceResultRespDto bronzeFee = flexibleMicFeeResult(bronzerMicFeeReq);
            micBronzer.setPhi(bronzeFee.getPhi());

            micBenefits.add(micBronzer);
        }

        //SILVER
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId2 = micInsuranceBenefits.stream().filter(o -> o.getId() == 2).findFirst();
        if (micInsuranceBenefitId2.isPresent()) {
            MicInsuranceBenefitV2Dto micSilver = micInsuranceBenefitId2.get();
            MiniMicFeeReqDto silverMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 2);
            MicInsuranceResultRespDto silverFee = flexibleMicFeeResult(silverMicFeeReq);
            micSilver.setPhi(silverFee.getPhi());

            micBenefits.add(micSilver);
        }

        //GOLD
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId3 = micInsuranceBenefits.stream().filter(o -> o.getId() == 3).findFirst();
        if (micInsuranceBenefitId3.isPresent()) {
            MicInsuranceBenefitV2Dto micGold = micInsuranceBenefitId3.get();
            MiniMicFeeReqDto goldMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 3);
            MicInsuranceResultRespDto goldFee = flexibleMicFeeResult(goldMicFeeReq);
            micGold.setPhi(goldFee.getPhi());

            micBenefits.add(micGold);
        }

        //PLATINUM
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId4 = micInsuranceBenefits.stream().filter(o -> o.getId() == 4).findFirst();
        if (micInsuranceBenefitId4.isPresent()) {
            MicInsuranceBenefitV2Dto micPlatinum = micInsuranceBenefitId4.get();
            MiniMicFeeReqDto platinumMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 4);
            MicInsuranceResultRespDto platinumFee = flexibleMicFeeResult(platinumMicFeeReq);
            micPlatinum.setPhi(platinumFee.getPhi());

            micBenefits.add(micPlatinum);
        }

        //PLATINUM
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId5 = micInsuranceBenefits.stream().filter(o -> o.getId() == 5).findFirst();
        if (micInsuranceBenefitId5.isPresent()) {
            MicInsuranceBenefitV2Dto micDiamond = micInsuranceBenefitId5.get();
            MiniMicFeeReqDto diamondMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 5);
            MicInsuranceResultRespDto diamondFee = flexibleMicFeeResult(diamondMicFeeReq);
            micDiamond.setPhi(diamondFee.getPhi());
            micBenefits.add(micDiamond);
        }

        return micBenefits;
    }

    @Override
    public MicInsuranceResultRespDto flexibleMicFeeResult(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest) {
        validateInsuranceAge(assured.getDob());
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(dviCode);
        reqDto.setNsd(nsd);
        reqDto.setPas(pas);
        reqDto.setId_tras("");
        reqDto.setNg_sinh(convertFormat(assured.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNhom(convertGroupMic(micRequest.getNhom()));
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setGcn_miccare_dkbs(new Common.GcnMicCareDkbs()
                .setBs1(micRequest.getBs1())
                .setBs2(micRequest.getBs2())
                .setBs3(micRequest.getBs3())
                .setBs4(micRequest.getBs4()));
        reqDto.setChecksum(generateMicChecksum(null));
        HttpEntity<MicInsuranceResultReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeader());
        String urlMicInsurance = micHost + MIC_INSURANCE_RESULT;

        MicInsuranceResultRespDto respDto = restCallApi(urlMicInsurance, entity, MicInsuranceResultRespDto.class, reqDto);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            log.error("[MIC]--Call to mic error with detail {}", respDto.getMessage());
            throw new MicApiException(respDto.getMessage());
        }
        return respDto;
    }

    @Override
    public MicSearchInsuranceCertRespDto flexibleMicInsuranceCert(String transactionId) {
        MicSearchInsuranceCertReqDto reqDto = new MicSearchInsuranceCertReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(dviCode);
        reqDto.setNsd(nsd);
        reqDto.setPas(pas);
        reqDto.setId_tras(transactionId);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setChecksum(generateMicChecksum(transactionId));
        HttpEntity<MicSearchInsuranceCertReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeader());
        String payloadLog = getPayloadLogObject(reqDto);
        MicSearchInsuranceCertRespDto certRespDto = getResponseForPostRequest(micHost + MIC_SEARCH_INSURANCE_CERT, entity, MicSearchInsuranceCertRespDto.class, payloadLog);
        if (certRespDto.getCode().equals(MIC_CODE_02)) {
            log.error("[MIC]--Lỗi khi lấy GCNBH MIC. code {}, message {}", certRespDto.getCode(), certRespDto.getMessage());
            throw new MicApiException(MSG12);
        }
        return certRespDto;
    }

    @Override
    public MicInsuranceResultRespDto flexibleMicFeeResult(MiniMicFeeReqDto miniMicFeeReqDto) {
        validateFxMicInsuranceAge(miniMicFeeReqDto.getDob());
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_dvi(dviCode);
        reqDto.setNsd(nsd);
        reqDto.setPas(pas);
        reqDto.setId_tras("");
        reqDto.setNg_sinh(convertFormat(miniMicFeeReqDto.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNhom(convertGroupMic(miniMicFeeReqDto.getNhom()));
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setGcn_miccare_dkbs(miniMicFeeReqDto.getGcn_miccare_dkbs());
        reqDto.setChecksum(generateMicChecksum(null));
        HttpEntity<MicInsuranceResultReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeader());
        String urlMicInsurance = micHost + MIC_INSURANCE_RESULT;

        String payloadLog = getPayloadLogObject(reqDto);

        MicInsuranceResultRespDto respDto = getResponseForPostRequest(urlMicInsurance, entity, MicInsuranceResultRespDto.class, payloadLog);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            throw new MicApiException(respDto.getMessage());
        }

        return respDto;
    }

    private String generateMicChecksum(String transactionId) {
        String data = micMerchantSecret + dviCode + NV_CODE;
        if (transactionId != null) {
            data = micMerchantSecret + dviCode + NV_CODE + transactionId;
        }
        byte[] hmac = new HmacUtils(HMAC_SHA_1, micMerchantSecret).hmac(data);
        String encodedString = Base64.getEncoder().encodeToString(hmac);
        return encodedString.replace("=", "%3d").replace(" ", "+");
    }

    private <T> T getResponseForPostRequest(String host, HttpEntity<?> entity, Class<T> clazz, String payloadLog) {
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
            return clazz.cast(exchange.getBody());
        } catch (Exception e) {
            log.error("[MIC]--Call to API error {}, exception: {}", host, e.getMessage());
            LocalDateTime receivedTime = LocalDateTime.now();
            asyncObject.saveErrorLog(Constants.HOST_PARTY.MIC, HttpMethod.POST, host, payloadLog, 500, e.getMessage(), sendTime, receivedTime);
            throw new MicApiException(MSG12);
        }
    }

    private <T> T restCallApi(String host, HttpEntity<?> entity, Class<T> clazz, Object reqDto) {
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
            return (T) exchange.getBody();
        } catch (Exception e) {
            log.error("[MIC]--Failed to call host {}, detail {}", host, e.getMessage());
            asyncObject.saveErrorLog(host, HttpMethod.POST, reqDto, e.getMessage(), Constants.HOST_PARTY.MIC, sendTime, LocalDateTime.now());
            throw new MbalApiException(MSG12);
        }
    }

    private String getPayloadLogObject(Object reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(reqDto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("[MINI]--Lỗi khi convert error data to json {}", reqDto);
            return null;
        }
    }

    private String getPayloadLogMicGenCert(MicGenerateInsuranceCertReqDto reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            Gson gson = new Gson();
            MicGenerateInsuranceCertReqDto dto = gson.fromJson(gson.toJson(reqDto), MicGenerateInsuranceCertReqDto.class);
            dto.getGcn_miccare_ttin_kh().setTen(null)
                    .setCmt(null)
                    .setDchi(null)
                    .setGioi(null)
                    .setMobi(null)
                    .setEmail(null)
                    .setNg_sinh(null);
            return ow.writeValueAsString(dto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("[MINI]--Lỗi khi convert error data to json {}", reqDto);
            return null;
        }
    }

    @Override
    public MicGetTokenRespDto micSandboxToken() {
        MicGetTokenReqDto reqDto = new MicGetTokenReqDto();
        reqDto.setUsername(username);
        reqDto.setPassword(password);
        reqDto.setClientId(clientId);
        reqDto.setClientSecret(clientSecret);

        HttpEntity<MicGetTokenReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeader());
        String urlMicInsurance = micHostSandbox + MIC_GET_TOKEN;

        String payloadLog = getPayloadLogObject(reqDto);

        MicGetTokenRespDto respDto = getResponseForPostRequest(urlMicInsurance, entity, MicGetTokenRespDto.class, payloadLog);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            throw new MicApiException(respDto.getMessage());
        }

        return respDto;
    }

    @Override
    public MicSandboxFeeCareRespDto flexibleMicSandboxFee(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest) {
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNg_sinh(convertFormat(assured.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNhom(convertGroupMic(micRequest.getNhom()));
        Common.ParentContract parentContract = micRequest.getParentContract() == null ? new Common.ParentContract() : micRequest.getParentContract();
        reqDto.setTtin_hd_bme(parentContract);
        reqDto.setGcn_miccare_dkbs(new Common.GcnMicCareDkbs()
                .setBs1(micRequest.getBs1())
                .setBs2(micRequest.getBs2())
                .setBs3(micRequest.getBs3())
                .setBs4(micRequest.getBs4()));

        MicGetTokenRespDto micToken = micSandboxToken();
        String merchant = "";
        String token = "";
        if (micToken != null && micToken.getData() != null) {
            merchant = micToken.getData().getMerchant();
            token = micToken.getData().getAccess_token();
        }

        HttpEntity<MicInsuranceResultReqDto> entity = new HttpEntity<>(reqDto, generateMicHeader(merchant, token));
        String urlMicInsurance = micHostSandbox + MIC_FEE_CARE;

        MicSandboxFeeCareRespDto respDto = restCallApi(urlMicInsurance, entity, MicSandboxFeeCareRespDto.class, reqDto);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            log.error("[MIC]--Call to mic error with detail {}", respDto.getMessage());
            throw new MicApiException(respDto.getMessage());
        }
        return respDto;
    }

    @Override
//    @Async
    public MicGenerateInsuranceCertSandboxRespDto micSandboxGenerateCert(MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh,
                                                                                            long micFee, String transactionId, int nhom,
                                                                                            Common.GcnMicCareDkbs gcnMicCareDkbs, ParentContract parentContract) {
        log.info("Start gen GCNBH MIC sandbox  voi transactionId {}", transactionId);
        long startTime = System.currentTimeMillis();
        MicGenerateInsuranceCertReqDto certReqDto = new MicGenerateInsuranceCertReqDto();
        certReqDto.setTtoan((double) micFee);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd gcnMicCareTtinhd = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinhd();
        gcnMicCareTtinhd.setNhom(convertGroupMic(nhom));
        gcnMicCareTtinhd.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        gcnMicCareTtinhd.setNg_huong("");
        gcnMicCareTtinhd.setDongy("C");
        gcnMicCareTtinhd.setGcn_miccare_dkbs(gcnMicCareDkbs);
        gcnMicCareTtinhd.setTtin_hd_bme(parentContract == null ? new ParentContract() : parentContract);

        certReqDto.setGcn_miccare_ttin_hd(gcnMicCareTtinhd);

        certReqDto.setGcn_miccare_ttin_kh(ttinkh);

        MicGenerateInsuranceCertReqDto.GcnMicCareTtsk gcnMicCareTtsk = new MicGenerateInsuranceCertReqDto.GcnMicCareTtsk();
        gcnMicCareTtsk.setCk(NO);
        gcnMicCareTtsk.setCc(NO);
        gcnMicCareTtsk.setCu(NO);
        gcnMicCareTtsk.setTt72C(NO);
        gcnMicCareTtsk.setTt73C(NO);
        gcnMicCareTtsk.setTt74C(NO);
        gcnMicCareTtsk.setTt75C(NO);
        gcnMicCareTtsk.setTt76C(NO);
        gcnMicCareTtsk.setTt77C(NO);
        gcnMicCareTtsk.setTt78C(NO);

        certReqDto.setGcn_miccare_ttsk(gcnMicCareTtsk);

        certReqDto.setNv(NV_CODE);
        certReqDto.setMa_sp(MIC_PRODUCT_CODE);
        certReqDto.setKieu_hd(CONTRACT_TYPE);
        certReqDto.setSo_hd_g("");
        certReqDto.setId_tras(transactionId);

        try {
            MicGenerateInsuranceCertSandboxRespDto respDto = micSandboxApi3rdService.micSandboxGenerateCert(certReqDto);
            log.info("Total time micGenerateInsuranceCert with transactionId {} is {}. Response data {}",
                    transactionId, System.currentTimeMillis() - startTime, respDto);
            if (respDto != null && respDto.getCode().equals(MIC_CODE_02) && respDto.getMessage().contains("PL/SQL")) {
                log.error("Error when gen MIC cert for transactionId {}", transactionId);
                throw new MicApiException("Mua bảo hiểm MIC không thành công, vui lòng liên hệ nhân viên tư vấn để được hỗ trợ!");
            }
            if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
                log.error("Error when gen MIC cert for transactionId {}", transactionId);
                throw new MicApiException(respDto.getMessage());
            }
            return respDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public MicSandboxSearchCertRespDto micSandboxSearchCert(String transactionId) {
        try {
            MicSandboxSearchCertRespDto certRespDto = micSandboxApi3rdService.micSandboxSearchCert(transactionId);
            if (certRespDto == null || MIC_CODE_02.equals(certRespDto.getCode())) {
                log.error("[MIC]--Lỗi khi lấy GCNBH MIC sandbox, message {}", certRespDto == null ? null : certRespDto.getMessage());
                throw new MicApiException(MSG12);
            }
            return certRespDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    public MicSandboxFeeCareRespDto flexibleMicFeeSandboxResult(MiniMicFeeReqDto miniMicFeeReqDto) {
        MicInsuranceResultReqDto reqDto = new MicInsuranceResultReqDto();
        reqDto.setNv(NV_CODE);
        reqDto.setKieu_hd(CONTRACT_TYPE);
        reqDto.setMa_sp(MIC_PRODUCT_CODE);
        reqDto.setNgay_hl(DateUtil.localDateTimeToString(DATE_DMY, LocalDateTime.now()));
        reqDto.setNg_sinh(convertFormat(miniMicFeeReqDto.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        reqDto.setNhom(convertGroupMic(miniMicFeeReqDto.getNhom()));
        reqDto.setTtin_hd_bme(miniMicFeeReqDto.getParentContract() == null ? new ParentContract() : miniMicFeeReqDto.getParentContract());
        reqDto.setGcn_miccare_dkbs(miniMicFeeReqDto.getGcn_miccare_dkbs());

        MicSandboxFeeCareRespDto respDto = micSandboxApi3rdService.flexibleMicFeeSandboxResult(reqDto);
        if (respDto != null && respDto.getCode().equals(MIC_CODE_02)) {
            log.error("[MIC]--Call to mic error with detail {}", respDto.getMessage());
            if (respDto.getMessage().contains(MIC_ERROR_CONTRACT)) {
                throw new MicApiException(String.format(MIC_MSG02, miniMicFeeReqDto.getParentContract().getSo_hd_bm(), "30%"));
            } else if (respDto.getMessage().contains(MIC_NOT_EXACTLY) || respDto.getMessage().contains(MIC_EXPIRED)) {
                throw new MicApiException(String.format(MIC_MSG01, miniMicFeeReqDto.getParentContract().getSo_hd_bm()));
            } else if (respDto.getMessage().contains(MIC_CONTRACT_CANCEL)) {
                throw new MicApiException(String.format(MIC_MSG03, miniMicFeeReqDto.getParentContract().getSo_hd_bm()));
            }
            throw new MicApiException(respDto.getMessage());
        }
        return respDto;
    }

    @Override
    public List<MicInsuranceBenefitV2Dto> retrieveMicSandboxPackages(MicPackageReqDto reqDto) {

        int ages = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, reqDto.getDob() + ZERO_TIME), LocalDateTime.now());

        List<MicPackage> micPackages = micPackageRepository.findAll();
        List<MicInsuranceBenefitV2Dto> micInsuranceBenefits = micPackages.stream()
                .map(micPackage -> modelMapper.map(micPackage, MicInsuranceBenefitV2Dto.class))
                .collect(Collectors.toList());
        List<MicInsuranceBenefitV2Dto> micBenefits = new ArrayList<>();
        Common.ParentContract parentContract = reqDto.getParentContract();
        MicSandboxContractInfoRespDto parentContractInfo;
        MicCompareResp groupMic;
        if (parentContract != null) {
            try {
                parentContractInfo = micSandboxSearchContractInfo(parentContract.getSo_hd_bm(),
                        DateUtil.localDateTimeToString(DateUtil.DATE_DMY, LocalDateTime.now()), true);
                groupMic = MicUtil.checkGroupMic(reqDto.getParentInfo() == null ? null : reqDto.getParentInfo().getMicRequest(), new MicAdditionalProduct(parentContractInfo.getData()));
            } catch (MicApiException e) {
                log.error("[MIC]--HD bố/mẹ không đủ điều kiện apply {}", parentContract.getSo_hd_bm());
                groupMic = MicUtil.checkGroupMic(reqDto.getParentInfo() == null ? null : reqDto.getParentInfo().getMicRequest(), null);
            }
        } else {
            groupMic = MicUtil.checkGroupMic(reqDto.getParentInfo() == null ? null : reqDto.getParentInfo().getMicRequest(), null);
        }

        //BRONZE
        Optional<MicInsuranceBenefitV2Dto> insuranceBenefitId1 = micInsuranceBenefits.stream().filter(o -> o.getId() == 1).findFirst();
        if (insuranceBenefitId1.isPresent()) {
            MicInsuranceBenefitV2Dto micBronzer = insuranceBenefitId1.get();
            if (MALE.equals(reqDto.getGender())) {
                micBronzer.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto bronzerMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 1);
            bronzerMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom())) {
                genParentContract(groupMic, bronzerMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto bronzeFee = flexibleMicFeeSandboxResult(bronzerMicFeeReq);
            micBronzer.setPhi(bronzeFee.getData().getPhi());
            addNoDisCountFee(micBronzer, ages, bronzerMicFeeReq);

            micBenefits.add(micBronzer);
        }

        //SILVER
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId2 = micInsuranceBenefits.stream().filter(o -> o.getId() == 2).findFirst();
        if (micInsuranceBenefitId2.isPresent()) {
            MicInsuranceBenefitV2Dto micSilver = micInsuranceBenefitId2.get();
            if (MALE.equals(reqDto.getGender())) {
                micSilver.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto silverMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 2);
            silverMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) &&groupMic.getNhom() >= 2) {
                genParentContract(groupMic, silverMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto silverFee = flexibleMicFeeSandboxResult(silverMicFeeReq);
            micSilver.setPhi(silverFee.getData().getPhi());

            if (Objects.nonNull(groupMic.getNhom()) &&groupMic.getNhom() >= 2) {
                addNoDisCountFee(micSilver, ages, silverMicFeeReq);
            }
            micBenefits.add(micSilver);
        }

        //GOLD
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId3 = micInsuranceBenefits.stream().filter(o -> o.getId() == 3).findFirst();
        if (micInsuranceBenefitId3.isPresent()) {
            MicInsuranceBenefitV2Dto micGold = micInsuranceBenefitId3.get();
            if (ages < 18 || ages > 50 || MALE.equals(reqDto.getGender())) {
                micGold.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto goldMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 3);
            goldMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 3) {
                genParentContract(groupMic, goldMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto goldFee = flexibleMicFeeSandboxResult(goldMicFeeReq);
            micGold.setPhi(goldFee.getData().getPhi());

            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 3) {
                addNoDisCountFee(micGold, ages, goldMicFeeReq);
            }
            micBenefits.add(micGold);
        }

        //PLATINUM
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId4 = micInsuranceBenefits.stream().filter(o -> o.getId() == 4).findFirst();
        if (micInsuranceBenefitId4.isPresent()) {
            MicInsuranceBenefitV2Dto micPlatinum = micInsuranceBenefitId4.get();
            if (ages < 18 || ages > 50 || MALE.equals(reqDto.getGender())) {
                micPlatinum.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto platinumMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 4);
            platinumMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 4) {
                genParentContract(groupMic, platinumMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto platinumFee = flexibleMicFeeSandboxResult(platinumMicFeeReq);
            micPlatinum.setPhi(platinumFee.getData().getPhi());

            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() >= 4) {
                addNoDisCountFee(micPlatinum, ages, platinumMicFeeReq);
            }
            micBenefits.add(micPlatinum);
        }

        //PLATINUM
        Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId5 = micInsuranceBenefits.stream().filter(o -> o.getId() == 5).findFirst();
        if (micInsuranceBenefitId5.isPresent()) {
            MicInsuranceBenefitV2Dto micDiamond = micInsuranceBenefitId5.get();
            if (ages < 18 || ages > 50 || MALE.equals(reqDto.getGender())) {
                micDiamond.setSubThree(MIC_NO);
            }
            MiniMicFeeReqDto diamondMicFeeReq = genMicFeeReqDto(reqDto.getDob(), 5);
            diamondMicFeeReq.setParentContract(reqDto.getParentContract());
            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() == 5) {
                genParentContract(groupMic, diamondMicFeeReq, reqDto);
            }
            MicSandboxFeeCareRespDto diamondFee = flexibleMicFeeSandboxResult(diamondMicFeeReq);
            micDiamond.setPhi(diamondFee.getData().getPhi());
            micBenefits.add(micDiamond);

            if (Objects.nonNull(groupMic.getNhom()) && groupMic.getNhom() == 5) {
                addNoDisCountFee(micDiamond, ages, diamondMicFeeReq);
            }
        }

        return micBenefits;
    }

    @Override
    public MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh generateMicSandboxTtinkh(FlexibleCommon.Assured customer,
                                                                                    FlexibleCommon.Assured assured) {
        MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = new MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh();
        ttinkh.setLkh("C");
        ttinkh.setQhe(assured.getRelationshipWithPolicyHolder().getMicRelationship());
        ttinkh.setTen(assured.getFullName());
        ttinkh.setCmt(assured.getIdentificationNumber());
        ttinkh.setGioi(MALE.equals(assured.getGender()) ? "1" : "2");
        ttinkh.setNg_sinh(convertFormat(assured.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        ttinkh.setDchi(assured.getAddress().fullAddress());
        ttinkh.setEmail(assured.getEmail());
        ttinkh.setMobi(assured.getPhoneNumber());

        ttinkh.setDbhm(customer.getMiniAssuredId().equals(assured.getMiniAssuredId()) ? "K" : "C");
        ttinkh.setLkhm("C");
        ttinkh.setTenm(customer.getFullName());
        ttinkh.setCmtm(customer.getIdentificationNumber());
        ttinkh.setMobim(customer.getPhoneNumber());
        ttinkh.setEmailm(customer.getEmail());
        ttinkh.setNg_sinhm(convertFormat(customer.getDob(), DATE_YYYY_MM_DD, DATE_DMY));
        ttinkh.setDchim(customer.getAddress().fullAddress());

        return ttinkh;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED, dontRollbackOn=MicApiException.class)
    public MicSandboxContractInfoRespDto micSandboxSearchContractInfo(String contractNum, String activeDate, boolean show) {
        try {
            MicSandboxContractInfoRespDto certRespDto = micSandboxApi3rdService.micSandboxSearchContractInfo(contractNum);
            if (certRespDto == null || MIC_CODE_02.equals(certRespDto.getCode())) {
                log.error("[MIC]--Lỗi khi lấy GCNBH MIC sandbox, message {}", certRespDto == null ? null : certRespDto.getMessage());
                if (certRespDto != null && certRespDto.getMessage().contains(MIC_ERROR_CONTRACT)) {
                    throw new MicApiException(String.format(MIC_MSG02, contractNum, "30%"));
                } else if (certRespDto != null && (certRespDto.getMessage().contains(MIC_NOT_EXACTLY) || certRespDto.getMessage().contains(MIC_EXPIRED))) {
                    throw new MicApiException(String.format(MIC_MSG01, contractNum));
                } else if (certRespDto != null && certRespDto.getMessage().contains(MIC_CONTRACT_CANCEL)) {
                    throw new MicApiException(String.format(MIC_MSG03, contractNum));
                }
                throw new MicApiException(certRespDto == null ? MSG12 : certRespDto.getMessage());
            }
            if (!show) {
                certRespDto.getData().setMobi(replaceWithAsterisks(certRespDto.getData().getMobi(), 3, 6));
                certRespDto.getData().setCmt(replaceWithAsterisks(certRespDto.getData().getCmt(), 2, 5));
                certRespDto.getData().setTen(replaceWithAsterisks(certRespDto.getData().getTen(), 3, 6));
                return certRespDto;
            }
            return certRespDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    private void addNoDisCountFee(MicInsuranceBenefitV2Dto micBenefit, int ages, MiniMicFeeReqDto micFeeReq) {
        if (ages < 6) {
            micFeeReq.setParentContract(new ParentContract());
            MicSandboxFeeCareRespDto bronzeNoDiscountFee = flexibleMicFeeSandboxResult(micFeeReq);
            micBenefit.setRegularFee(bronzeNoDiscountFee.getData().getPhi());
        }
    }
}
