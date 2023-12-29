package com.stg.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.stg.entity.*;
import com.stg.entity.customer.Customer;
import com.stg.errors.CustomerNotFoundException;
import com.stg.errors.MbalApiException;
import com.stg.errors.MiniApiException;
import com.stg.repository.*;
import com.stg.service.ExternalMicAPIService;
import com.stg.service.ExternalV2ApiService;
import com.stg.service.MicPackageService;
import com.stg.service.dto.external.MbalIllustrationDownloadRespDto;
import com.stg.service.dto.external.PackageNameEnum;
import com.stg.service.dto.external.request.MicGenerateInsuranceCertReqDto;
import com.stg.service.dto.external.request.MiniMicFeeReqDto;
import com.stg.service.dto.external.requestV2.*;
import com.stg.service.dto.external.response.MicInsuranceResultRespDto;
import com.stg.service.dto.external.responseV2.*;
import com.stg.service.dto.insurance.InsuranceContractsAppDto;
import com.stg.service3rd.mbalv2.MbalV2Api3rdService;
import com.stg.service3rd.mbalv2.dto.resp.InquiryPaymentPolicy;
import com.stg.utils.Common;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import com.stg.utils.PaymentPeriod;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stg.config.redis.CacheConfiguration.RAM_CACHING;
import static com.stg.service.dto.external.PackageNameEnum.*;
import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.Constants.*;
import static com.stg.utils.DateUtil.DATE_YYYY_MM_DD;
import static com.stg.utils.Endpoints.*;
import static com.stg.utils.NlpUtil.isMatchFullNameT24;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ExternalV2ApiServiceImpl implements ExternalV2ApiService {

    private static final String MINI_ERROR_CONVERT_JSON = "[MINI]--Lỗi khi convert error data to json {}";
    private static final String PROTECT_TYPE = "ULLP_PROTECT";
    private static final String INVERT_TYPE = "ULLP_INVEST";

    // TEN_YEARS
    private static final String PROTECT1 = "PROTECT1";
    private static final String PROTECT2 = "PROTECT2";
    private static final String PROTECT3 = "PROTECT3";
    private static final String PROTECT4 = "PROTECT4";
    private static final String PROTECT5 = "PROTECT5";
    private static final String PROTECT6 = "PROTECT6";
    private static final String PROTECT7 = "PROTECT7";

    // FIVE_YEARS
    private static final String ULPROTECT5_1 = "ULPROTECT5_1";
    private static final String ULPROTECT5_2 = "ULPROTECT5_2";
    private static final String ULPROTECT5_3 = "ULPROTECT5_3";
    private static final String ULPROTECT5_4 = "ULPROTECT5_4";
    private static final String ULPROTECT5_5 = "ULPROTECT5_5";
    private static final String ULPROTECT5_6 = "ULPROTECT5_6";
    private static final String ULPROTECT5_7 = "ULPROTECT5_7";

    private final static Long IMAGE_SIZE = (long) (5 * 1024 * 1024); // 5MB
    private static final String NO = "K";
    private static final String SUCCESS = "Success";
    @Value("${spring.redis.cache.external-v2api-service.mbal-send-mail-expire-time}")
    public int mbalSendMailExpireTime;
    @Value("${spring.redis.cache.external-v2api-service.mbal-policy-detail-expire-time}")
    public int mbalPolicyDetailExpiretime;
    @Value("${spring.redis.cache.external-v2api-service.mbal-create-quote-expire-time}")
    public int mbalCreateQuoteExpireTime ;
    @Value("${spring.redis.cache.external-v2api-service.mbal-process-expire-time}")
    public int mbalProcessExpireTime ;

    @Value("${azure.storage.public_container}")
    private String publicContainer;


    @Value("${external.host.mbal_ul_3}")
    private String mbalHostUl3;

    @Value("${external.mbal-key.client_id_ul3}")
    private String clientIdUl3;

    @Value("${external.mbal-key.client_secret_ul3}")
    private String clientSecretUl3;

    @Value("${external.mbal-key.client_id_ul3_policy}")
    private String clientIdUl3Policy;

    @Value("${external.mbal-key.client_secret_ul3_policy}")
    private String clientSecretUl3Policy;

    private final MicPackageRepository micPackageRepository;
    private final MicPackageService micPackageService;

    private final PackagePhotoRepository packagePhotoRepository;

    private final InsuranceContractRepository insuranceContractRepository;

    private final CustomerRepository customerRepository;

    private final ExternalMicAPIService micAPIService;

    private final ProductMbalRepository productMbalRepository;

    private final InsuredMbalRepository insuredMbalRepository;

    private final IdentificationRepository identificationRepository;

    private static Map<String, Long> packageFee;

    static {
        Map<String, Long> map = new HashMap<>();

        map.put(PROTECT1, 6000000L);
        map.put(PROTECT2, 10000000L);
        map.put(PROTECT3, 15000000L);
        map.put(PROTECT4, 20000000L);
        map.put(PROTECT5, 30000000L);
        map.put(PROTECT6, 50000000L);
        map.put(PROTECT7, 100000000L);

        map.put(ULPROTECT5_1, 6000000L);
        map.put(ULPROTECT5_2, 10000000L);
        map.put(ULPROTECT5_3, 15000000L);
        map.put(ULPROTECT5_4, 20000000L);
        map.put(ULPROTECT5_5, 30000000L);
        map.put(ULPROTECT5_6, 50000000L);
        map.put(ULPROTECT5_7, 100000000L);

        packageFee = Collections.unmodifiableMap(map);
    }

    @Autowired
    private UploadFileAsync uploadFileAsync;

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private UploadFIleImpl uploadFIle;

    @Autowired
    private final AsyncObjectImpl asyncObject;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final RedisCommands<String, String> redis;

    @Autowired
    private final Gson gson;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MbalV2Api3rdService mbalV2Api3rdService;

    @Override
    public CreateQuoteV2RespDto mbalCreateProcess(String cif, CreateProcessV2ReqDto reqDto) {
        Customer customer = customerRepository.findByMbId(cif);
        //
        updateCustomerInput(customer, reqDto.getCustomer());
        buildIdentificationTYpe(reqDto.getCustomer());

        if (reqDto.getCustomer().getOccupationId() != null && reqDto.getCustomer().getOccupationId() == 0) {
            reqDto.getCustomer().setOccupationId(null);
        }

        HttpEntity<CreateProcessV2ReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeaderBasicAuth());

        String payloadLog = getPayloadLogCreateProcess(reqDto);

        CreateQuoteV2RespDto createProcessResp = getResponseForPostRequest(mbalHostUl3 + MBAL_BMH_CREATE_PROCESS, HttpMethod.POST, entity, CreateQuoteV2RespDto.class, null, payloadLog, Constants.HOST_PARTY.MBAL);
        if (createProcessResp != null) {
            redis.set(processIdKey(cif), createProcessResp.getId(), SetArgs.Builder.ex(mbalProcessExpireTime));
        }
        return createProcessResp;
    }

    /*
        1. NATIONAL_ID(must be 9/12 numeric characters) - Chứng minh thư
        2. CITIZEN_ID(must be 12 numeric characters) - Căn cước công dân
        3. PASSPORT(>= 6 characters) - Hộ chiếu
        4. MILITARY_ID(= 6,8,10 or 12 characters) - CMT quân đội
        5. BIRTH_CERTIFICATE(character (number & text), max 20) - Giấy khai sinh
     */
    private static void buildIdentificationTYpe(CreateProcessV2ReqDto.Customer customer) {
        if (customer.getIdentificationType().equalsIgnoreCase("CMT") || customer.getIdentificationType().equalsIgnoreCase("CMND")) {
            if (customer.getIdentificationNumber().length() == 6
                    || customer.getIdentificationNumber().length() == 8
                    || customer.getIdentificationNumber().length() == 10) {
                customer.setIdentificationType("MILITARY_ID");
            } else {
                customer.setIdentificationType("NATIONAL_ID");
            }
        } else if (customer.getIdentificationType().equalsIgnoreCase("CCCD")) {
            customer.setIdentificationType("CITIZEN_ID");
        } else if (customer.getIdentificationType().equalsIgnoreCase("CMTQD")) {
            customer.setIdentificationType("MILITARY_ID");
        } else if (customer.getIdentificationType().equalsIgnoreCase("PASSPORT")) {
            customer.setIdentificationType("PASSPORT");
        }
    }

    @Override
    public CreateQuoteV2RespDto mbalCreateQuote(String cif, CreateQuoteV2ReqDto reqDto, Integer processId) {

        long t1 = System.currentTimeMillis();
        verifyCustomerPermission(cif, processId);
        Customer customer = customerRepository.findByMbId(cif);
        updateCustomerInput(customer, reqDto.getCustomer());
        validateInsuranceAge(reqDto.getCustomer().getDob());

        updateCustomerInfoCreateQuote(cif, reqDto);

        String micTransactionId = generateUUIDId(30);
        buildIdentificationTYpe(reqDto.getCustomer());
        HttpEntity<CreateQuoteV2ReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeaderBasicAuth());
        String urlCreateQuote = String.format(MBAL_BMH_CREATE_QUOTE, processId);

        // MIC tinh phí
        MiniMicFeeReqDto micFeeReqDto = new MiniMicFeeReqDto();
        micFeeReqDto.setNhom(reqDto.getMicRequestDto().getNhom());
        micFeeReqDto.setDob(reqDto.getCustomer().getDob());
        Common.GcnMicCareDkbs gcnMicCareDkbs = new Common.GcnMicCareDkbs();
        gcnMicCareDkbs.setBs1(NO);
        gcnMicCareDkbs.setBs2(NO);
        gcnMicCareDkbs.setBs3(NO);
        gcnMicCareDkbs.setBs4(NO);
        if (FREE_STYLE.equalsIgnoreCase(reqDto.getType())) {
            gcnMicCareDkbs.setBs1(reqDto.getMicRequestDto().getBs1());
            gcnMicCareDkbs.setBs2(reqDto.getMicRequestDto().getBs2());
            gcnMicCareDkbs.setBs3(reqDto.getMicRequestDto().getBs3());
            gcnMicCareDkbs.setBs4(reqDto.getMicRequestDto().getBs4());
        }
        micFeeReqDto.setGcn_miccare_dkbs(gcnMicCareDkbs);

        long t2 = System.currentTimeMillis();
        log.debug("---- (Millis) time to init data = " + ((t2 - t1) / 1000L));
        MicInsuranceResultRespDto micInsuranceResult = micAPIService.micFeeResult(micFeeReqDto);
        long t3 = System.currentTimeMillis();
        log.debug("---- (Millis) time to call micFeeResult = " + ((t3 - t2) / 1000L));

        // MIC gen GCNBH
        String dob = reqDto.getCustomer().getDob();
        String address = reqDto.getCustomer().getAddress().getLine1();
        MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh gcnMicCareTtinkh = micAPIService.generateMicCareTtinkh(
                reqDto.getCustomer().getFullName(),
                reqDto.getCustomer().getIdentificationNumber(),
                reqDto.getCustomer().getGender(),
                dob,
                address,
                reqDto.getCustomer().getEmail(),
                reqDto.getCustomer().getPhoneNumber());
        long t4 = System.currentTimeMillis();
        log.debug("---- (Millis) time to call generateMicCareTtinkh = " + ((t4 - t3) / 1000L));

        micAPIService.micGenerateInsuranceCert(gcnMicCareTtinkh, micInsuranceResult.getPhi().longValue(),
                micTransactionId, reqDto.getMicRequestDto().getNhom(), gcnMicCareDkbs);
        long t5 = System.currentTimeMillis();
        log.debug("---- (Millis) time to call micGenerateInsuranceCert = " + ((t5 - t4) / 1000L));

        String payloadLog = getPayloadLogCreateQuote(reqDto);

        CreateQuoteV2RespDto quoteV2RespDto = getResponseForPostRequest(String.format("%s%s", mbalHostUl3, urlCreateQuote), HttpMethod.POST, entity, CreateQuoteV2RespDto.class, null, payloadLog, Constants.HOST_PARTY.MBAL);
        long t6 = System.currentTimeMillis();
        log.debug("---- (Millis) time to call urlCreateQuote mbal = " + ((t6 - t5) / 1000L));

        if (quoteV2RespDto != null) {
            redis.set(micTransactionIdKey(cif), micTransactionId, SetArgs.Builder.ex(mbalCreateQuoteExpireTime));
            quoteV2RespDto.setMicTransactionId(micTransactionId);
            quoteV2RespDto.setMixInsuranceFee(BigDecimal.valueOf(micInsuranceResult.getPhi().longValue() + packageFee.get(reqDto.getProductPackageCode())));
            quoteV2RespDto.setStrMixInsuranceFee(formatCurrency(BigDecimal.valueOf(micInsuranceResult.getPhi().longValue() + packageFee.get(reqDto.getProductPackageCode()))));
            quoteV2RespDto.setStrMicInsuranceFee(formatCurrency(BigDecimal.valueOf(micInsuranceResult.getPhi().longValue())));

            // Saving to cache amount data of process
            Long topupInsuranceFee = quoteV2RespDto.getProductPackage().getTopupInsuranceFee() == null ? 0L : quoteV2RespDto.getProductPackage().getTopupInsuranceFee();
            quoteV2RespDto.setStrTopupInsuranceFee(topupInsuranceFee == 0 ? null : formatCurrency(BigDecimal.valueOf(topupInsuranceFee)));
            Long mbalAmount = quoteV2RespDto.getProductPackage().getBaseInsuranceFee() + topupInsuranceFee;
            redis.set(processAmountIdKey(cif, String.valueOf(processId)), genProcessAmountCacheData(micInsuranceResult.getPhi().longValue(), mbalAmount), SetArgs.Builder.ex(mbalCreateQuoteExpireTime));

            // cache ic
            if (quoteV2RespDto.getSale() != null) {
                redis.set(icCodeKey(cif, String.valueOf(processId)), genICCacheData(quoteV2RespDto.getSale().getCode(), quoteV2RespDto.getSale().getName()), SetArgs.Builder.ex(mbalCreateQuoteExpireTime));
                log.info("mbalCreateQuote: processId={}, icCode={}", processId, quoteV2RespDto.getSale().getCode());
            }
        }

        long t7 = System.currentTimeMillis();
        log.debug("---- time to process all function create quote = " + ((t7 - t1) / 1000L));

        return quoteV2RespDto;
    }

    private String genProcessAmountCacheData(Long micAmount, Long mbalAmount) {
        Map<String, String> map = new HashMap<>();
        map.put(MIC_AMOUNT, String.valueOf(micAmount));
        map.put(MBAL_AMOUNT, String.valueOf(mbalAmount));
        map.put(TOTAL_AMOUNT, String.valueOf(micAmount + mbalAmount));
        return gson.toJson(map);
    }

    private void updateCustomerInfoCreateQuote(String cif, CreateQuoteV2ReqDto reqDto) {
        // Update thông tin khách hàng
        Customer customerByMbId = customerRepository.findByMbId(cif);
        if (customerByMbId == null) {
            throw new CustomerNotFoundException("Không tồn tại Khách hàng có MbId là " + cif);
        }
        if (reqDto.getCustomer().getFullName() != null) {
            if (!isMatchFullNameT24(customerByMbId.getFullNameT24(), reqDto.getCustomer().getFullName())) {
                throw new CustomerNotFoundException(MSG37);
            }
            customerByMbId.setFullName(reqDto.getCustomer().getFullName());
        }

        if (reqDto.getCustomer().getDob() != null) {
            customerByMbId.setBirthday(reqDto.getCustomer().getDob());
        }
        if (reqDto.getCustomer().getGender() != null) {
            customerByMbId.setGender(reqDto.getCustomer().getGender());
        }
        if (reqDto.getCustomer().getNationality() != null) {
            customerByMbId.setNationality(reqDto.getCustomer().getNationality());
        }
        if (reqDto.getCustomer().getOccupationName() != null) {
            customerByMbId.setJob(reqDto.getCustomer().getOccupationName());
        }
        CreateQuoteV2RespDto.Address address = reqDto.getCustomer().getAddress();
        if (address != null && address.getLine1() != null) {
            customerByMbId.setLine1(address.getLine1());
            customerByMbId.setProvinceName(address.getProvinceName());
            customerByMbId.setDistrictName(address.getDistrictName());
            customerByMbId.setWardName(address.getWardName());
        }
        if (reqDto.getCustomer().getEmail() != null) {
            customerByMbId.setEmail(reqDto.getCustomer().getEmail());
        }
        reqDto.getCustomer().setFullName(customerByMbId.getFullName());
        reqDto.getCustomer().setIdentificationNumber(customerByMbId.getIdentification());
        reqDto.getCustomer().setPhoneNumber(customerByMbId.getPhone());
        customerRepository.save(customerByMbId);
    }

    private void verifyCustomerPermission(String cif, Integer processId) {
        if (!String.valueOf(processId).equals(redis.get(processIdKey(cif)))) {
            throw new MiniApiException(MSG13);
        }
    }

    @Override
    public CreateQuoteV2RespDto mbalConfirmQuotation(String cif, Integer processId) {
        verifyCustomerPermission(cif, processId);

        HttpEntity<String> entity = new HttpEntity<>(null, generateDefaultHeaderBasicAuth());
        String urlCreateQuote = String.format(MBAL_CONFIRM_QUOTATION, processId);
        return getResponseForPostRequest(String.format("%s%s", mbalHostUl3, urlCreateQuote), HttpMethod.POST, entity, CreateQuoteV2RespDto.class, null, "", Constants.HOST_PARTY.MBAL);
    }

    @Override
    public CreateQuoteV2RespDto mbalSubmitApplication(String cif, SubmitApplicationV2ReqDto reqDto, Integer processId) {
        verifyCustomerPermission(cif, processId);

        if (reqDto.getFamilyMembers() != null && reqDto.getFamilyMembers().isEmpty()) {
            reqDto.setFamilyMembers(null);
        }
        if (reqDto.getOtherDeclinedContracts() != null && reqDto.getOtherDeclinedContracts().isEmpty()) {
            reqDto.setOtherDeclinedContracts(null);
        }
        if (reqDto.getOtherInsuranceClaims() != null && reqDto.getOtherInsuranceClaims().isEmpty()) {
            reqDto.setOtherInsuranceClaims(null);
        }
        if (reqDto.getOtherActiveContracts() != null && reqDto.getOtherActiveContracts().isEmpty()) {
            reqDto.setOtherActiveContracts(null);
        }
        HttpEntity<SubmitApplicationV2ReqDto> entity = new HttpEntity<>(reqDto, generateDefaultHeaderBasicAuth());
        String urlCreateQuote = String.format(MBAL_SUBMIT_APPLICATION, processId);

        String payloadLog = getPayloadLogSubmitApp(reqDto);

        CreateQuoteV2RespDto response = getResponseForPostRequest(String.format("%s%s", mbalHostUl3, urlCreateQuote), HttpMethod.POST, entity, CreateQuoteV2RespDto.class, null, payloadLog, Constants.HOST_PARTY.MBAL);
        // cache rm
        if (reqDto.getReferer() != null) {
            redis.set(rmCifProcessIdKey(cif, String.valueOf(processId)), gson.toJson(reqDto.getReferer()), SetArgs.Builder.ex(mbalCreateQuoteExpireTime));
            log.info("mbalSubmitApplication: processId={}, rmCode={}, rmCodeRequest={}", processId, reqDto.getReferer().getCode(), reqDto.getReferer().getCode());
        }
        // cache supporter
        if (reqDto.getSupporter() != null) {
            redis.set(supporterCifProcessIdKey(cif, String.valueOf(processId)), gson.toJson(reqDto.getSupporter()), SetArgs.Builder.ex(mbalCreateQuoteExpireTime));
            log.info("mbalSubmitApplication: processId={}, supporterCode={}, supporterCodeRequest={}", processId, response.getSupporter().getCode(), reqDto.getSupporter().getCode());
        }
        return response;
    }

    @Override
    @Cacheable(value = "cache:occupations", unless = "#result.size()==0", cacheManager = RAM_CACHING)
    public List<OccupationV2RespDto> mbalListOccupation() {
        HttpEntity<String> entity = new HttpEntity<>(null, generateDefaultHeaderBasicAuth());
        return getResponseForPostRequest(mbalHostUl3 + MBAL_LIST_OCCUPATION, HttpMethod.GET, entity, List.class, null, "", Constants.HOST_PARTY.MBAL);
    }

    @Override
    public List<PackageV2RespDto> mbalListPackage() {
        HttpEntity<String> entity = new HttpEntity<>(null, generateDefaultHeaderBasicAuth());
        return getResponseForPostRequest(mbalHostUl3 + MBAL_LIST_PACKAGE, HttpMethod.GET, entity, List.class, null, null, Constants.HOST_PARTY.MBAL);
    }

    private boolean validatePolicyHolder(String mbId, PolicyDetailV2RespDto policyDetailV2RespDto) {
        boolean isPolicyHolder = false;
        if (policyDetailV2RespDto.getPolicyHolder() != null && !policyDetailV2RespDto.getPolicyHolder().getIdentifications().isEmpty()) {
            Customer customerByMbId = customerRepository.findByMbId(mbId);
            if (customerByMbId == null) {
                throw new CustomerNotFoundException("Không tồn tại Khách hàng có MbId là " + mbId);
            }
            for (PolicyDetailV2RespDto.Identifications identification : policyDetailV2RespDto.getPolicyHolder().getIdentifications()) {
                if (identification.getId().equals(customerByMbId.getIdentification())) {
                    isPolicyHolder = true;
                }
            }
        }
        return isPolicyHolder;
    }

    private static String buildKeyPolicy(String cif, String appNumber, String policyNumber) {
        return "cif_" + cif + "eapp_" + appNumber + "_policy_" + policyNumber;
    }

    public String convertMbalPeriodicFeePaymentTime(String period) {
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

    private void saveIdentifications(List<PolicyDetailV2RespDto.Identifications> identificationsInput, Customer customer, InsuredMbal insuredMbal, List<Identification> identificationsDb) {
        List<Identification> identificationsToSave = new ArrayList<>();
        Map<String, Identification> mapIdentificationDb = identificationsDb.stream().collect(Collectors.toMap(Identification::getType, Function.identity()));
        for (PolicyDetailV2RespDto.Identifications identificationInput : identificationsInput) {
            Identification identification;
            String identificationType = convertIdentificationTypeMbalToMiniApp(identificationInput.getType());
            if (mapIdentificationDb.get(identificationType) == null) {
                identification = new Identification();
            } else {
                identification = mapIdentificationDb.get(identificationType);
            }
            identification.setIdentification(identificationInput.getId());
            identification.setType(identificationType);
            identification.setIssuePlace(identificationInput.getIssuePlace());
            if (identificationInput.getIssueDate() != null) {
                identification.setIssueDate(identificationInput.getIssueDate().substring(0, 10));
            }
            if (identificationInput.getExpiryDate() != null) {
                identification.setExpiryDate(identificationInput.getExpiryDate().substring(0, 10));
            }
            identification.setCustomer(customer);
            identification.setInsuredMbal(insuredMbal);
            identificationsToSave.add(identification);
        }
        identificationRepository.saveAll(identificationsToSave);
    }

    public String buildStatus(String mbalStatus) {
        switch (mbalStatus) {
            case "Active":
                return "Đang hiệu lực";
            case "In Process":
                return "Đang xử lý";
            case "Reversed":
                return "Hủy";
            case "Lapse":
                return "Mất hiệu lực";
            case "Dormant":
                return "Mất hiệu lực hoàn toàn";
            case "Refused":
                return "Bị từ chối";
            default:
                return mbalStatus;
        }
    }

    /*private Customer saveCustomer(PolicyDetailV2RespDto policyDetailV2RespDto) {
        //save customer
        Customer customer = new Customer();
        if (policyDetailV2RespDto.getPolicyHolder() != null) {
            PolicyDetailV2RespDto.PolicyHolder policyHolder = policyDetailV2RespDto.getPolicyHolder();
            Customer customerByMbId = customerRepository.findByMbId(policyHolder.getBpNumber());
            if (customerByMbId == null) {
                customer.setEmail(policyHolder.getEmail());
                customer.setGender(policyHolder.getGender() == 1 ? "MALE" : "FEMALE");
                customer.setBirthday(policyHolder.getDob().substring(0, 10));
                customer.setPhone(policyHolder.getPhone());
                if (!policyHolder.getIdentifications().isEmpty()) {
                    PolicyDetailV2RespDto.Identifications identification = policyHolder.getIdentifications().get(0);
                    customer.setIdentification(identification.getId());
                    customer.setIdCardType(Common.convertIdentificationTypeMbalToMiniApp(identification.getType()));
                    customer.setIdIssuedPlace(identification.getIssuePlace());
                    customer.setIdentificationDate(identification.getIssueDate().substring(0, 10));
                }
//                customer.setNationality(null);
                customer.setJob(policyHolder.getJob());
                customer.setAddress(policyHolder.getFullAddress());
                customer.setMbId(policyHolder.getBpNumber());
                customer.setFullName(policyHolder.getFullName());
//                customer.setSegment(null);
                customer.setSource("MBAL");

                customer = customerRepository.save(customer);
            } else {
                customer = customerByMbId;
            }
        }
        return customer;
    }*/

    @Override
    public String mbalSendMail(String cif, String processId, SendMailV2ReqDto v2ReqDto) {
        verifyCustomerPermission(cif, Integer.valueOf(processId));
        if (redis.get(sendEmailProcessIdKey(cif, processId)) != null && redis.get(sendEmailProcessIdKey(cif, processId)).equals(SUCCESS)) {
            log.error("Spam send email multi time for cif {} and processId {}", cif, processId);
            return SUCCESS;
        }
        HttpEntity<SendMailV2ReqDto> entity = new HttpEntity<>(v2ReqDto, generateDefaultHeaderBasicAuth());
        String urlCreateQuote = String.format(MBAL_SEND_MAIL, processId);

        String payloadLog = getPayloadLogSendMail(v2ReqDto);
        String response = getResponseForPostRequest(String.format("%s%s", mbalHostUl3, urlCreateQuote), HttpMethod.POST, entity, String.class, null, payloadLog, HOST_PARTY.MBAL);

        if (response != null && response.contains(SUCCESS)) {
            redis.set(sendEmailProcessIdKey(cif, processId), SUCCESS, SetArgs.Builder.ex(mbalSendMailExpireTime));
        }
        log.info("[MINI]--Email send response {}", response);
        return response;

    }

    @Override
    public MbalIllustrationDownloadRespDto downloadFileBMH(String processId, String cif) {

        verifyCustomerPermission(cif, Integer.valueOf(processId));

        HttpEntity<SendMailV2ReqDto> entity = new HttpEntity<>(null, generateDefaultHeaderBasicAuth());
        String urlCreateQuote = String.format(MBAL_DOWNLOAD_FILE_BMH, processId);
        ResponseEntity<byte[]> result = restTemplate.exchange(String.format("%s%s", mbalHostUl3, urlCreateQuote), HttpMethod.GET, entity, byte[].class);
        byte[] body = result.getBody();
        MbalIllustrationDownloadRespDto respDto = new MbalIllustrationDownloadRespDto();
        respDto.setPath(body);
        return respDto;
    }

    /*@Override
    public UploadFileRespDto uploadMultiFileQuestion(MultipartFile[] files, String processId, String cif) throws IOException, ExecutionException, InterruptedException {
        verifyCustomerPermission(cif, Integer.valueOf(processId));

        long t1 = System.currentTimeMillis();

        if (files.length > 15) {
            throw new UploadFileException("Số lượng file quý khách đã đạt mức tối đa cho phép (15 file). Vui lòng xoá file đã chọn trước đó nếu muốn tải lại ");
        }
        List<UploadImageInfo> uploadImageInfos = new ArrayList<>();
        List<CompletableFuture<String>> urlUploadFileFutures = new ArrayList<>();
        for (MultipartFile parts : files) {
            uploadFileCompletable(uploadImageInfos, urlUploadFileFutures, parts);
        }

        if (uploadImageInfos.size() > 0) {
            return new UploadFileRespDto().setUploadImageInfos(uploadImageInfos);
        }

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        UploadMultiFileQuestionRespDto response = new UploadMultiFileQuestionRespDto();
        HttpStatus httpStatus = HttpStatus.OK;

        long t2 = System.currentTimeMillis();
        log.info("time call upload azure=" + (t2 - t1));

        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    map.add("files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBasicAuth(clientIdUl3, clientSecretUl3);

            String urlCreateQuote = String.format(MBAL_UPLOAD_MULTI_FILE_QUESTION, processId);
            String url = String.format("%s%s", mbalHostUl3, urlCreateQuote);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            response = restTemplate.postForObject(url, requestEntity, UploadMultiFileQuestionRespDto.class);

        } catch (HttpStatusCodeException e) {
            httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
            response.setSuccess(e.getResponseBodyAsString());
            log.error("uploadMultiFileQuestion exception HttpStatusCodeException: " + e.getMessage());
        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            response.setSuccess(e.getMessage());
            log.error("[MINI]--uploadMultiFileQuestion exception all: " + e.getMessage());
        }

        long t3 = System.currentTimeMillis();
        log.info("time call save image to mbal=" + (t3 - t2));

        CompletableFuture<String>[] arrUrlUploadFileFutures = new CompletableFuture[urlUploadFileFutures.size()];
        urlUploadFileFutures.toArray(arrUrlUploadFileFutures);
        CompletableFuture.allOf(arrUrlUploadFileFutures).join();
        for (CompletableFuture<String> completableFuture : urlUploadFileFutures) {
            UploadImageInfo info = new UploadImageInfo();
            info.setUrl(completableFuture.get());
            info.setMessage(response == null ? "" : response.getSuccess());
            info.setCode(httpStatus.value());
            info.setStatus(httpStatus);
            uploadImageInfos.add(info);
        }

        long t4 = System.currentTimeMillis();
        log.info("time wait upload azure=" + (t4 - t3));


        return new UploadFileRespDto().setUploadImageInfos(uploadImageInfos);

    }*/

//    private void uploadFileCompletable(List<UploadImageInfo> uploadImageInfos, List<CompletableFuture<String>> urlUploadFileFutures, MultipartFile parts) throws IOException {
//        UploadImageInfo info = new UploadImageInfo();
//        if (parts.getSize() > IMAGE_SIZE) {
//            info.setMessage("Chỉ được phép chọn file có dung lượng nhỏ hơn 5mb. Vui lòng chọn lại file.");
//            info.setCode(400);
//            info.setStatus(HttpStatus.BAD_REQUEST);
//            uploadImageInfos.add(info);
//            return;
//        }
//        String mediaType = detectDocTypeUsingFacade(parts.getInputStream());
//
//        if (!(mediaType.equals(MediaType.IMAGE_PNG_VALUE) || mediaType.equals(MediaType.IMAGE_JPEG_VALUE)
//                || mediaType.equals(MediaType.APPLICATION_PDF_VALUE))) {
//            info.setMessage("Chỉ được phép chọn file có định dạng: PDF, jpg, jpeg, png, live. Vui lòng chọn lại file.");
//            info.setCode(400);
//            info.setStatus(HttpStatus.BAD_REQUEST);
//            uploadImageInfos.add(info);
//        } else {
//            CompletableFuture<String> urlUploadFileFuture = uploadFileAsync.uploadAndDownloadFile(parts, mediaType,
//                    uploadFIle.getBlobContainerClient(publicContainer));
//            urlUploadFileFutures.add(urlUploadFileFuture);
//        }
//    }

    public HttpHeaders generateDefaultHeaderBasicAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientIdUl3, clientSecretUl3);
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    public HttpHeaders generateDefaultHeaderBasicAuthSearchPolicy() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientIdUl3Policy, clientSecretUl3Policy);
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    private <T> T getResponseForPostRequest(String host, HttpMethod method, HttpEntity<?> entity,
                                            Class<T> clazz, Map<Object, Object> params, String payloadLog,
                                            Constants.HOST_PARTY hostParty) {
        String requestId = generateUUIDId(36);
        LocalDateTime sendTime = LocalDateTime.now();
        try {
            ResponseEntity<T> exchange = restTemplate.exchange(host, method, entity, clazz, params);
            T exchangeBody = exchange.getBody();
            HttpHeaders headers = exchange.getHeaders();
            genMbaCombolLog(host, gson.toJson(entity.getBody()), 200, null, sendTime, method.name(),
                    LocalDateTime.now(), requestId, ThirdPartyLog.StepProcess.COMPLETED, exchangeBody, hostParty, headers,
                    entity.getBody() == null ? null : gson.toJson(entity.getBody()));
            return exchangeBody;

        } catch (Exception e) {
            log.error("Call to API error {}", e.getMessage());
            HttpHeaders responseHeaders = null;
            try {
                responseHeaders = ((HttpClientErrorException) e).getResponseHeaders();
            } catch (ClassCastException ex) {
                log.error("[Mini]--Combo can not catch the exception to get header");
            }

            LocalDateTime receivedTime = LocalDateTime.now();
            String message = e.getMessage();
            Integer statusCode = 500;
            if (message.contains("{") && message.contains("}")) {
                String bodyMessage = message.substring(message.indexOf("{"), message.lastIndexOf("}") + 1);
                if (message.contains("502") || message.contains("503")) {
                    message = bodyMessage;
                    asyncObject.saveErrorLog(hostParty, method, host, payloadLog, statusCode, message, sendTime, receivedTime);
                    log.error("[{}]--Call to API error {}, exception: {}", hostParty, host, message);
                    throw new MbalApiException(MSG12);
                } else {
                    Gson g = new Gson();
                    ErrorResponseMbal errorResponseMbal = g.fromJson(bodyMessage, ErrorResponseMbal.class);
                    List<String> messages = errorResponseMbal.getMessages();
                    message = StringUtils.join(messages, ", ");
                    statusCode = errorResponseMbal.getStatusCode();
                }
            }
            asyncObject.saveErrorLog(hostParty, method, host, payloadLog, statusCode, message, sendTime, receivedTime);
            log.error("[{}]--Call to API error {}, exception: {}", hostParty, host, message);

            if ("Application don't exist".equals(message) || "Policy don't exist".equals(message)) { //...?
                if (host.contains("appNumber=") && host.contains("policyNumber=")) {
                    message = "Số hồ sơ yêu cầu bảo hiểm hoặc số hợp đồng không trùng khớp";
                } else {
                    message = MSG34;
                }
            }
            genMbaCombolLog(host, gson.toJson(entity.getBody()), statusCode, message, sendTime, method.name(),
                    LocalDateTime.now(), requestId, ThirdPartyLog.StepProcess.ERROR, null, hostParty, responseHeaders,
                    entity.getBody() == null ? null : gson.toJson(entity.getBody()));

            throw new MbalApiException(message);
        }
    }

    /*@Override
    public PackageTypeV2RespDtos getPackages(MiniPackageV2ReqDto reqDto) {
        validateInsuranceAge(reqDto.getDob());
        PackageTypeV2RespDtos mixPackages = new PackageTypeV2RespDtos();
        List<PackageTypeV2Data> mixPackageType = new ArrayList<>();
        mixPackages.setPackages(mixPackageType);
        List<PackageV2RespDto> mbalPackages = mbalListPackage();
        List<PackageV2RespDto> listMbalPackage = objectMapper.convertValue(mbalPackages, new TypeReference<>() {
        });
        List<PackageV2RespDto> packagesFiveYears = listMbalPackage.stream()
                .filter(o -> o.getCode().equals(ULINVEST5_1)
                        || o.getCode().equals(ULINVEST5_2)
                        || o.getCode().equals(ULPROTECT5_1)
                        || o.getCode().equals(ULPROTECT5_2)
                        || o.getCode().equals(ULPROTECT5_3)
                        || o.getCode().equals(ULPROTECT5_4)
                        || o.getCode().equals(ULPROTECT5_5))
                .collect(Collectors.toList());

        List<PackageV2RespDto> packagesTenYears = listMbalPackage.stream()
                .filter(o -> o.getCode().equals(PROTECT1)
                        || o.getCode().equals(PROTECT2)
                        || o.getCode().equals(PROTECT3)
                        || o.getCode().equals(PROTECT4)
                        || o.getCode().equals(INVEST1_80)
                        || o.getCode().equals(INVEST2_80)
                        || o.getCode().equals(INVEST3_80))
                .collect(Collectors.toList());

        if (reqDto.getDuration().equals("FIVE_YEARS")) {
            listMbalPackage = objectMapper.convertValue(packagesFiveYears, new TypeReference<>() {
            });
        } else {
            listMbalPackage = objectMapper.convertValue(packagesTenYears, new TypeReference<>() {
            });
        }

        // Danh sách gói Phong Cách
        if (reqDto.getType().equals(CUSTOM_TYPE)) {
            List<MicPackage> micPackages = micPackageService.findAll();
            List<MicInsuranceBenefitV2Dto> micInsuranceBenefits = micPackages.stream().map(micPackage -> modelMapper.map(micPackage, MicInsuranceBenefitV2Dto.class))
                    .collect(Collectors.toList());

            List<PackageTypeV2Data> packagesProtect = convertMbalPackageProtectToResponseData(listMbalPackage);
            List<PackageTypeV2Data> packagesInvert = convertMbalPackageInvestToResponseData(listMbalPackage);
            mixPackages.getPackages().addAll(refactorMbalPackage(packagesProtect));
            mixPackages.getPackages().addAll(refactorMbalPackage(packagesInvert));

            List<MicInsuranceBenefitV2Dto> micBenefits = new ArrayList<>();

            //BRONZE
            Optional<MicInsuranceBenefitV2Dto> insuranceBenefitId1 = micInsuranceBenefits.stream().filter(o -> o.getId() == 1).findFirst();
            if (insuranceBenefitId1.isPresent()) {
                MicInsuranceBenefitV2Dto micBronzer = insuranceBenefitId1.get();
                MiniMicFeeReqDto bronzerMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                MicInsuranceResultRespDto bronzeFee = micAPIService.micFeeResult(bronzerMicFeeReq);
                micBronzer.setPhi(bronzeFee.getPhi());

                micBenefits.add(micBronzer);
            }

            //SILVER
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId2 = micInsuranceBenefits.stream().filter(o -> o.getId() == 2).findFirst();
            if (micInsuranceBenefitId2.isPresent()) {
                MicInsuranceBenefitV2Dto micSilver = micInsuranceBenefitId2.get();
                MiniMicFeeReqDto silverMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 2);
                MicInsuranceResultRespDto silverFee = micAPIService.micFeeResult(silverMicFeeReq);
                micSilver.setPhi(silverFee.getPhi());

                micBenefits.add(micSilver);
            }

            //GOLD
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId3 = micInsuranceBenefits.stream().filter(o -> o.getId() == 3).findFirst();
            if (micInsuranceBenefitId3.isPresent()) {
                MicInsuranceBenefitV2Dto micGold = micInsuranceBenefitId3.get();
                MiniMicFeeReqDto goldMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 3);
                MicInsuranceResultRespDto goldFee = micAPIService.micFeeResult(goldMicFeeReq);
                micGold.setPhi(goldFee.getPhi());

                micBenefits.add(micGold);
            }

            //PLATINUM
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId4 = micInsuranceBenefits.stream().filter(o -> o.getId() == 4).findFirst();
            if (micInsuranceBenefitId4.isPresent()) {
                MicInsuranceBenefitV2Dto micPlatinum = micInsuranceBenefitId4.get();
                MiniMicFeeReqDto platinumMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 4);
                MicInsuranceResultRespDto platinumFee = micAPIService.micFeeResult(platinumMicFeeReq);
                micPlatinum.setPhi(platinumFee.getPhi());

                micBenefits.add(micPlatinum);
            }

            //PLATINUM
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId5 = micInsuranceBenefits.stream().filter(o -> o.getId() == 5).findFirst();
            if (micInsuranceBenefitId5.isPresent()) {
                MicInsuranceBenefitV2Dto micDiamond = micInsuranceBenefitId5.get();
                MiniMicFeeReqDto diamondMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 5);
                MicInsuranceResultRespDto diamondFee = micAPIService.micFeeResult(diamondMicFeeReq);
                micDiamond.setPhi(diamondFee.getPhi());
                micBenefits.add(micDiamond);
            }

            mixPackages.setMicInsuranceBenefits(micBenefits);
            mixPackages.getPackages().sort(Comparator.comparing(PackageTypeV2Data::getInsuranceFee, Comparator.nullsLast(Comparator.naturalOrder())));
            return mixPackages;
        }
        else if (reqDto.getPackName().equals(HEALTHY_TYPE)) {
            // MINI_APP sống khỏe
            PackageTypeV2RespDtos healthyPackages = getPackageTypeV2RespDtos(mixPackages, reqDto, INVERT_TYPE, listMbalPackage);
            healthyPackages.getPackages().sort(Comparator.comparing(PackageTypeV2Data::getMixInsuranceFee));
            PackageTypeV2Data stylePackage = new PackageTypeV2Data();
            stylePackage.setId("8");
            stylePackage.setMixPackageName(UR_STYLE.getVal());
            setPackagePhoto(stylePackage, UR_STYLE);
            healthyPackages.getPackages().add(stylePackage);
            return healthyPackages;
        }
        // MINI_APP sống vui
        PackageTypeV2RespDtos packageHappy = getPackageTypeV2RespDtos(mixPackages, reqDto, PROTECT_TYPE, listMbalPackage);
        packageHappy.getPackages().sort(Comparator.comparing(PackageTypeV2Data::getMixInsuranceFee));
        return packageHappy;
    }*/
    private List<PackageTypeV2Data> refactorMbalPackage(List<PackageTypeV2Data> packages) {
        for (PackageTypeV2Data data : packages) {
            if (data.getType().equals(PROTECT_TYPE)) {
                if (data.getCode().equals(PROTECT1)) {
                    data.setId("1");
                    setPackagePhoto(data, CONFIDENT);
                    continue;
                } else if (data.getCode().equals(PROTECT2)) {
                    data.setId("2");
                    setPackagePhoto(data, PackageNameEnum.EQUANIMITY);
                    continue;
                } else if (data.getCode().equals(PROTECT3)) {
                    data.setId("3");
                    setPackagePhoto(data, PackageNameEnum.PEACEFUL);
                    continue;
                } else if (data.getCode().equals(PROTECT4)) {
                    data.setId("4");
                    setPackagePhoto(data, PackageNameEnum.HEALTHY);
                    continue;
                }
                data.setId(null);
            } else {
                if (data.getCode().equals(PROTECT5)) {
                    data.setId("5");
                    setPackagePhoto(data, PackageNameEnum.HAPPY);
                    continue;
                } else if (data.getCode().equals(PROTECT6)) {
                    data.setId("6");
                    setPackagePhoto(data, PackageNameEnum.ENJOY);
                    continue;
                }
                data.setId("7");
                setPackagePhoto(data, PackageNameEnum.LUCKY);
            }
        }
        return packages.stream().filter(o -> o.getId() != null).collect(Collectors.toList());
    }

    private void setPackagePhoto(PackageTypeV2Data data, PackageNameEnum packageNameEnum) {
        PackagePhoto byType = packagePhotoRepository.findByType(packageNameEnum);
        if (byType != null) {
            data.setPhoto(byType.getImage());
            data.setBackgroundImage(byType.getImage());
        }
    }

    private PackageTypeV2RespDtos generatePackageTypeV2RespDtos(PackageTypeV2RespDtos packageTypeV2RespDtos, MiniPackageV2ReqDto reqDto) {
        PackageTypeV2RespDtos respDtos = new PackageTypeV2RespDtos();
        List<PackageTypeV2Data> packages = packageTypeV2RespDtos.getPackages();
        List<PackageTypeV2Data> mixPackages = new ArrayList<>();
        String name;
        MicInsuranceBenefitV2Dto micInsuranceBenefit;
        MicInsuranceResultRespDto micInsuranceResultRespDto;
        MiniMicFeeReqDto micFeeReq;
        List<MicPackage> micPackages = micPackageService.findAll();
        PackageTypeV2Data mixData;
        for (PackageTypeV2Data data : packages) {
            if (data.getCode().equals(PROTECT1) || data.getCode().equals(ULPROTECT5_1)) {
                // Tu tin
                name = CONFIDENT.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitV2Dto.class); // lấy mic bên app mình
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, CONFIDENT);

                mixPackages.add(mixData);
            } else if (data.getCode().equals(PROTECT2) || data.getCode().equals(ULPROTECT5_2)) {
                // An nhien
                name = PackageNameEnum.EQUANIMITY.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitV2Dto.class);
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, PackageNameEnum.EQUANIMITY);
                mixPackages.add(mixData);
            } else if (data.getCode().equals(PROTECT3) || data.getCode().equals(ULPROTECT5_3)) {
                // Binh An
                name = PackageNameEnum.PEACEFUL.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitV2Dto.class);
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, PackageNameEnum.PEACEFUL);

                mixPackages.add(mixData);
            } else if (data.getCode().equals(PROTECT4) || data.getCode().equals(ULPROTECT5_4)) {
                // Vui khoe
                name = PackageNameEnum.HEALTHY.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitV2Dto.class);
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, PackageNameEnum.HEALTHY);

                mixPackages.add(mixData);
            } else if ((data.getCode().equals(PROTECT5)) || data.getCode().equals(ULPROTECT5_5)) {
                // Hanh phuc
                name = HAPPY.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 2);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(1), MicInsuranceBenefitV2Dto.class);
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, HAPPY);

                mixPackages.add(mixData);
            } else if (data.getCode().equals(PROTECT6) || data.getCode().equals(ULPROTECT5_6)) {
                // Tan huong
                name = PackageNameEnum.ENJOY.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 3);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(2), MicInsuranceBenefitV2Dto.class);
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, PackageNameEnum.ENJOY);

                mixPackages.add(mixData);
            } else if (data.getCode().equals(PROTECT7) || data.getCode().equals(ULPROTECT5_7)) {
                // May man
                name = PackageNameEnum.LUCKY.getVal();
                micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 4);
                micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                micInsuranceBenefit = modelMapper.map(micPackages.get(3), MicInsuranceBenefitV2Dto.class);
                mixData = generatePackageTypeV2Data(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                setPackagePhoto(mixData, PackageNameEnum.LUCKY);

                mixPackages.add(mixData);
            }
        }
        return respDtos.setPackages(mixPackages);
    }

    private PackageTypeV2Data generatePackageTypeV2Data(PackageTypeV2Data data, MicInsuranceResultRespDto micInsuranceResultRespDto,
                                                        String packageName, MicInsuranceBenefitV2Dto micInsuranceBenefitV2Dto) {
        if (micInsuranceResultRespDto != null) {
            updatePackageTypeV2Data(data, micInsuranceResultRespDto.getPhi(), micInsuranceBenefitV2Dto);
        }
        data.setMixPackageName(packageName);
        return data;
    }

    private void updatePackageTypeV2Data(PackageTypeV2Data data, BigDecimal phi, MicInsuranceBenefitV2Dto micInsuranceBenefitV2Dto) {
        BigDecimal totalInsuranceFee = (data.getInsuranceFee().add(phi));
        data.setMixInsuranceFee(totalInsuranceFee);
        data.setMicInsuranceFee(phi);
        data.setStrMixInsuranceFee(formatCurrency(totalInsuranceFee));
        Long totalBenefit = (data.getSumAssured() * 3)
                + convertToLong(micInsuranceBenefitV2Dto.getMainOne())
                + convertToLong(micInsuranceBenefitV2Dto.getMainTwo())
                + convertToLong(micInsuranceBenefitV2Dto.getMainThree());
        data.setTotalInsuranceBenefit(BigDecimal.valueOf(totalBenefit));
        data.setStrTotalInsuranceBenefit(convertToMoneyFormat(String.valueOf(totalBenefit)));
    }

    @Override
    public PaymentNotificationV2RespDto paymentNotifications(PaymentNotificationV2ReqDto paymentNotificationV2ReqDto) {
        HttpEntity<PaymentNotificationV2ReqDto> entity = new HttpEntity<>(paymentNotificationV2ReqDto, generateDefaultHeaderBasicAuth());
        String hookUrl = String.format("%s%s", mbalHostUl3, MBAL_PAYMENT_NOTIFICATION);
        LocalDateTime sendTime = LocalDateTime.now();
        String requestId = generateUUIDId(36);

        ResponseEntity<PaymentNotificationV2RespDto> exchange = restTemplate.exchange(hookUrl, HttpMethod.POST, entity, PaymentNotificationV2RespDto.class);
        HttpHeaders headers = exchange.getHeaders();
        genMbaCombolLog(hookUrl, gson.toJson(entity.getBody()), 200, null, sendTime, HttpMethod.POST.name(),
                LocalDateTime.now(),  requestId, ThirdPartyLog.StepProcess.COMPLETED, exchange.getBody(),
                HOST_PARTY.MBAL, headers, entity.getBody() == null ? null : gson.toJson(entity.getBody()));
        return exchange.getBody();
    }

    @Override
    public PackageTypeV2RespDtos insurancePackages(MiniPackageV2ReqDto reqDto) {
        validateInsuranceAge(reqDto.getDob());
        PackageTypeV2RespDtos mixPackages = new PackageTypeV2RespDtos();
        List<PackageTypeV2Data> mixPackageType = new ArrayList<>();
        mixPackages.setPackages(mixPackageType);
        List<PackageV2RespDto> mbalPackages = mbalListPackage();
        List<PackageV2RespDto> listMbalPackage = objectMapper.convertValue(mbalPackages, new TypeReference<>() {
        });
        List<PackageV2RespDto> packagesInFiveYears = listMbalPackage.stream()
                .filter(o -> o.getCode().equals(ULPROTECT5_5)
                        || o.getCode().equals(ULPROTECT5_6)
                        || o.getCode().equals(ULPROTECT5_1)
                        || o.getCode().equals(ULPROTECT5_2)
                        || o.getCode().equals(ULPROTECT5_3)
                        || o.getCode().equals(ULPROTECT5_4)
                        || o.getCode().equals(ULPROTECT5_7))
                .collect(Collectors.toList());

        List<PackageV2RespDto> packagesInTenYears = listMbalPackage.stream()
                .filter(o -> o.getCode().equals(PROTECT1)
                        || o.getCode().equals(PROTECT2)
                        || o.getCode().equals(PROTECT3)
                        || o.getCode().equals(PROTECT4)
                        || o.getCode().equals(PROTECT5)
                        || o.getCode().equals(PROTECT6)
                        || o.getCode().equals(PROTECT7))
                .collect(Collectors.toList());
        // Tạo danh sách 7 gói sống vui 10 năm
        PackageTypeV2Data stylePackage = new PackageTypeV2Data();
        stylePackage.setId("8");
        stylePackage.setOrder(8);
        stylePackage.setMixPackageName(UR_STYLE.getVal());
        setPackagePhoto(stylePackage, UR_STYLE);
        listMbalPackage = objectMapper.convertValue(packagesInTenYears, new TypeReference<>() {
        });
        if (reqDto.getInsurancePackageName().equals(HEALTHY_TYPE)) {
            //Tạo dánh sách 7 gói sống khỏe 5 năm
            stylePackage.setId("16");
            stylePackage.setOrder(16);
            listMbalPackage = objectMapper.convertValue(packagesInFiveYears, new TypeReference<>() {
            });
        }
        // Danh sách gói Phong Cách
        if (reqDto.getType().equals(FREE_STYLE)) {
            List<MicPackage> micPackages = micPackageService.findAll();
            List<MicInsuranceBenefitV2Dto> micInsuranceBenefits = micPackages.stream()
                    .map(micPackage -> modelMapper.map(micPackage, MicInsuranceBenefitV2Dto.class))
                    .collect(Collectors.toList());

            List<PackageTypeV2Data> packages = convertToFixComboPackages(listMbalPackage);
            mixPackages.getPackages().addAll(refactorPackages(packages));
            List<MicInsuranceBenefitV2Dto> micBenefits = new ArrayList<>();

            //BRONZE
            Optional<MicInsuranceBenefitV2Dto> insuranceBenefitId1 = micInsuranceBenefits.stream().filter(o -> o.getId() == 1).findFirst();
            if (insuranceBenefitId1.isPresent()) {
                MicInsuranceBenefitV2Dto micBronzer = insuranceBenefitId1.get();
                MiniMicFeeReqDto bronzerMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                MicInsuranceResultRespDto bronzeFee = micAPIService.micFeeResult(bronzerMicFeeReq);
                micBronzer.setPhi(bronzeFee.getPhi());

                micBenefits.add(micBronzer);
            }

            //SILVER
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId2 = micInsuranceBenefits.stream().filter(o -> o.getId() == 2).findFirst();
            if (micInsuranceBenefitId2.isPresent()) {
                MicInsuranceBenefitV2Dto micSilver = micInsuranceBenefitId2.get();
                MiniMicFeeReqDto silverMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 2);
                MicInsuranceResultRespDto silverFee = micAPIService.micFeeResult(silverMicFeeReq);
                micSilver.setPhi(silverFee.getPhi());

                micBenefits.add(micSilver);
            }

            //GOLD
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId3 = micInsuranceBenefits.stream().filter(o -> o.getId() == 3).findFirst();
            if (micInsuranceBenefitId3.isPresent()) {
                MicInsuranceBenefitV2Dto micGold = micInsuranceBenefitId3.get();
                MiniMicFeeReqDto goldMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 3);
                MicInsuranceResultRespDto goldFee = micAPIService.micFeeResult(goldMicFeeReq);
                micGold.setPhi(goldFee.getPhi());

                micBenefits.add(micGold);
            }

            //PLATINUM
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId4 = micInsuranceBenefits.stream().filter(o -> o.getId() == 4).findFirst();
            if (micInsuranceBenefitId4.isPresent()) {
                MicInsuranceBenefitV2Dto micPlatinum = micInsuranceBenefitId4.get();
                MiniMicFeeReqDto platinumMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 4);
                MicInsuranceResultRespDto platinumFee = micAPIService.micFeeResult(platinumMicFeeReq);
                micPlatinum.setPhi(platinumFee.getPhi());

                micBenefits.add(micPlatinum);
            }

            //PLATINUM
            Optional<MicInsuranceBenefitV2Dto> micInsuranceBenefitId5 = micInsuranceBenefits.stream().filter(o -> o.getId() == 5).findFirst();
            if (micInsuranceBenefitId5.isPresent()) {
                MicInsuranceBenefitV2Dto micDiamond = micInsuranceBenefitId5.get();
                MiniMicFeeReqDto diamondMicFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 5);
                MicInsuranceResultRespDto diamondFee = micAPIService.micFeeResult(diamondMicFeeReq);
                micDiamond.setPhi(diamondFee.getPhi());
                micBenefits.add(micDiamond);
            }

            mixPackages.setMicInsuranceBenefits(micBenefits);
            mixPackages.getPackages().sort(Comparator.comparing(PackageTypeV2Data::getInsuranceFee, Comparator.nullsLast(Comparator.naturalOrder())));
            return mixPackages;
        } else { //Fix combo
            comboFixPackages(mixPackages, reqDto, listMbalPackage);
            mixPackages.getPackages().sort(Comparator.comparing(PackageTypeV2Data::getMixInsuranceFee));
            mixPackages.getPackages().add(stylePackage);

            return mixPackages;
        }
    }

    @Override
    @Cacheable(value = "cache:icInfo")
    public IcInformationResp getIcInformation(String icCode) {
        HttpEntity<String> entity = new HttpEntity<>(null, generateDefaultHeaderBasicAuth());
        String urlIc = String.format("%s%s", mbalHostUl3, MBAL_IC);
        String urlWithParam = UriComponentsBuilder.fromHttpUrl(urlIc)
                .queryParam("code", icCode)
                .toUriString();
        Map<Object, Object> params = new HashMap<>();
        return getResponseForPostRequest(urlWithParam, HttpMethod.GET, entity, IcInformationResp.class, params, "", Constants.HOST_PARTY.MBAL);
    }

    private String getPayloadLogPaymentNotify(PaymentNotificationV2ReqDto reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            Gson gson = new Gson();
            PaymentNotificationV2ReqDto dto = gson.fromJson(gson.toJson(reqDto), PaymentNotificationV2ReqDto.class);
            dto.getSource().setPhoneNumber(null)
                    .setName(null);
            return ow.writeValueAsString(dto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error(MINI_ERROR_CONVERT_JSON, reqDto);
            return null;
        }
    }

    private String getPayloadLogSendMail(SendMailV2ReqDto reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(reqDto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error(MINI_ERROR_CONVERT_JSON, reqDto);
            return null;
        }
    }

    private String getPayloadLogSubmitApp(SubmitApplicationV2ReqDto reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(reqDto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error(MINI_ERROR_CONVERT_JSON, reqDto);
            return null;
        }
    }

    private String getPayloadLogCreateProcess(CreateProcessV2ReqDto reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            Gson gson = new Gson();
            CreateProcessV2ReqDto dto = gson.fromJson(gson.toJson(reqDto), CreateProcessV2ReqDto.class);
            dto.getCustomer().setFullName(null)
                    .setDob(null)
                    .setEmail(null)
                    .setIdentificationNumber(null)
                    .setPhoneNumber(null);
            return ow.writeValueAsString(dto);
        } catch (Exception ex) {
            log.error(MINI_ERROR_CONVERT_JSON, reqDto);
            return null;
        }
    }

    private String getPayloadLogCreateQuote(CreateQuoteV2ReqDto reqDto) {
        try {
            ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
            Gson gson = new Gson();
            CreateQuoteV2ReqDto dto = gson.fromJson(gson.toJson(reqDto), CreateQuoteV2ReqDto.class);
            dto.getCustomer().setFullName(null)
                    .setDob(null)
                    .setEmail(null)
                    .setIdentificationNumber(null)
                    .setPhoneNumber(null);
            return ow.writeValueAsString(dto);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error(MINI_ERROR_CONVERT_JSON, reqDto);
            return null;
        }
    }

    private void comboFixPackages(PackageTypeV2RespDtos mixPackages, MiniPackageV2ReqDto reqDto, List<PackageV2RespDto> listMbalPackage) {
        List<PackageTypeV2Data> packages = convertToFixComboPackages(listMbalPackage);
        mixPackages.setPackages(packages);
        refactorPackages(packages);
        mixPackages.setPackages(generatePackageTypeV2RespDtos(mixPackages, reqDto).getPackages());
    }

    private List<PackageTypeV2Data> convertToFixComboPackages(List<PackageV2RespDto> packageV2RespDtos) {
        return packageV2RespDtos.stream().map(o -> modelMapper.map(o, PackageTypeV2Data.class)).collect(Collectors.toList());
    }

    private List<PackageTypeV2Data> refactorPackages(List<PackageTypeV2Data> packages) {
        for (PackageTypeV2Data data : packages) {
            switch (data.getCode()) {
                case PROTECT1:
                    data.setId("1");
                    data.setOrder(1);
                    setPackagePhoto(data, CONFIDENT);
                    continue;
                case ULPROTECT5_1:
                    data.setId("9");
                    data.setOrder(9);
                    setPackagePhoto(data, CONFIDENT);
                    continue;
                case PROTECT2:
                    data.setId("2");
                    data.setOrder(2);
                    setPackagePhoto(data, PackageNameEnum.EQUANIMITY);
                    continue;
                case ULPROTECT5_2:
                    data.setId("10");
                    data.setOrder(10);
                    setPackagePhoto(data, PackageNameEnum.EQUANIMITY);
                    continue;
                case PROTECT3:
                    data.setId("3");
                    data.setOrder(3);
                    setPackagePhoto(data, PackageNameEnum.PEACEFUL);
                    continue;
                case ULPROTECT5_3:
                    data.setId("11");
                    data.setOrder(11);
                    setPackagePhoto(data, PackageNameEnum.PEACEFUL);
                    continue;
                case PROTECT4:
                    data.setId("4");
                    data.setOrder(4);
                    setPackagePhoto(data, PackageNameEnum.HEALTHY);
                    continue;
                case ULPROTECT5_4:
                    data.setId("12");
                    data.setOrder(12);
                    setPackagePhoto(data, PackageNameEnum.HEALTHY);
                    continue;
                case PROTECT5:
                    data.setId("17");
                    data.setOrder(5);
                    setPackagePhoto(data, PackageNameEnum.HAPPY);
                    continue;
                case ULPROTECT5_5:
                    data.setId("20");
                    data.setOrder(13);
                    setPackagePhoto(data, PackageNameEnum.HAPPY);
                    continue;
                case PROTECT6:
                    data.setId("18");
                    data.setOrder(6);
                    setPackagePhoto(data, PackageNameEnum.ENJOY);
                    continue;
                case ULPROTECT5_6:
                    data.setId("21");
                    data.setOrder(14);
                    setPackagePhoto(data, PackageNameEnum.ENJOY);
                    continue;
                case PROTECT7:
                    data.setId("19");
                    data.setOrder(7);
                    setPackagePhoto(data, PackageNameEnum.LUCKY);
                    continue;
            }
            data.setId("22");
            data.setOrder(15);
            setPackagePhoto(data, PackageNameEnum.LUCKY);
        }
        return packages.stream().filter(o -> o.getId() != null).collect(Collectors.toList());
    }

    /*
        1. NATIONAL_ID(must be 9/12 numeric characters) - Chứng minh thư
        2. CITIZEN_ID(must be 12 numeric characters) - Căn cước công dân
        3. PASSPORT(>= 6 characters) - Hộ chiếu
        4. MILITARY_ID(= 6,8,10 or 12 characters) - CMT quân đội
        5. BIRTH_CERTIFICATE(character (number & text), max 20) - Giấy khai sinh
     */
    private static void updateCustomerInput(Customer customer, CreateProcessV2ReqDto.Customer customerInput) {
        // verify customer name
        if (!isMatchFullNameT24(customer.getFullNameT24(), customerInput.getFullName())) {
            throw new CustomerNotFoundException(MSG37);
        }
        //customerInput.setFullName(customer.getFullName()); => cho phép update

        customerInput.setIdentificationNumber(customer.getIdentification());
        customerInput.setPhoneNumber(customer.getPhone());
//        customerInput.setEmail(customer.getEmail()); // Có thể thay đổi email trong thông tin khách hàng
        customerInput.setIdentificationType(customer.getIdCardType());
        if (customer.getIdentificationDate() != null) {
            customerInput.setIdentificationDate(DateUtil.localDateTimeToString(DATE_YYYY_MM_DD, customer.getIdentificationDate()));
        }
        customerInput.setIdIssuedPlace(customer.getIdIssuedPlace());

    }

    private <T> void genMbaCombolLog(String path, String payload, Integer code, String errorMessage, LocalDateTime sendTime,
                                     String method, LocalDateTime receivedTime, String requestId,
                                     ThirdPartyLog.StepProcess stepProcess, T exchangeBody,
                                     Constants.HOST_PARTY hostParty, HttpHeaders headers, String thirdPartyPayload) {
        ThirdPartyLog thirdPartyLog = new ThirdPartyLog();
        thirdPartyLog.setPath(path);
        thirdPartyLog.setMethod(method);
        thirdPartyLog.setHostParty(hostParty);
        thirdPartyLog.setPayload(payload);
        thirdPartyLog.setCode(code);
        thirdPartyLog.setErrorMessage(errorMessage);
        thirdPartyLog.setSendTime(sendTime);
        thirdPartyLog.setReceivedTime(receivedTime);
        thirdPartyLog.setRequestId(requestId);
        thirdPartyLog.setStepProcess(stepProcess);
        thirdPartyLog.setResponse(exchangeBody == null ? null : gson.toJson(exchangeBody));
        thirdPartyLog.setThirdPartyPayload(thirdPartyPayload);
        if (headers != null) {
            List<String> xRequestIds = headers.get("X-Request-ID");
            thirdPartyLog.setXRequestId(xRequestIds == null ? null : xRequestIds.get(0));
        }
        asyncObject.saveThirdPartyLog(thirdPartyLog);
    }

    private String genICCacheData(String code, String fullname) {
        Map<String, String> map = new HashMap<>();
        map.put(IC_CODE, code);
        map.put(IC_NAME, fullname);
        return gson.toJson(map);
    }

}