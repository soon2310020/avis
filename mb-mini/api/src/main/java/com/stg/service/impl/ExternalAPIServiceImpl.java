package com.stg.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.stg.adapter.dto.CommonAdapterResponse;
import com.stg.entity.*;
import com.stg.entity.customer.Customer;
import com.stg.errors.*;
import com.stg.repository.*;
import com.stg.service.*;
import com.stg.service.card.CardInstallmentService;
import com.stg.service.dto.address.AddressDTO;
import com.stg.service.dto.baas.OauthToken;
import com.stg.service.dto.card.GetCardListRequest;
import com.stg.service.dto.card.GetInstFeeRequest;
import com.stg.service.dto.card.GetInstFeeResponse;
import com.stg.service.dto.card.ListCardResponse;
import com.stg.service.dto.external.ControlStateEnum;
import com.stg.service.dto.external.MicInsuranceBenefitDto;
import com.stg.service.dto.external.PackageNameEnum;
import com.stg.service.dto.external.SegmentDto;
import com.stg.service.dto.external.request.*;
import com.stg.service.dto.external.requestFlexible.FlexibleTransactionReqDto;
import com.stg.service.dto.external.response.*;
import com.stg.service.dto.external.responseFlexible.FlexibleQuoteRespDto;
import com.stg.service.dto.external.responseV2.MultipartInputStreamFileResource;
import com.stg.service.dto.insurance.*;
import com.stg.service.lock.PaymentLockService;
import com.stg.service.token.JwtAccessTokenFactory;
import com.stg.service3rd.hcm.HcmApi3rdService;
import com.stg.service3rd.toolcrm.ToolCrmApi3rdService;
import com.stg.service3rd.toolcrm.constant.QuotationAction;
import com.stg.service3rd.toolcrm.dto.req.QuotationSyncDataReq;
import com.stg.service3rd.toolcrm.dto.resp.QuotationSyncDataResp;
import com.stg.utils.*;
import com.stg.utils.address.AddressInfo;
import com.stg.utils.address.AddressUtil;
import com.stg.utils.enums.ErrorCodeCardPartner;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.codec.digest.HmacUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.stg.service.dto.external.PackageNameEnum.CONFIDENT;
import static com.stg.service.dto.external.PackageNameEnum.UR_STYLE;
import static com.stg.service.dto.external.PackageNameEnum.getPackageNumFromVal;
import static com.stg.utils.CallbackType.AUTO;
import static com.stg.utils.CardLabel.CARD_EXPIRED;
import static com.stg.utils.CardLabel.LIMIT_NOT_ENOUGH;
import static com.stg.utils.CardLabel.PASS;
import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.Constants.*;
import static com.stg.utils.DateUtil.DATE_DMY;
import static com.stg.utils.DateUtil.DATE_YYYY_MM_DD;
import static com.stg.utils.DateUtil.compareTime;
import static com.stg.utils.Endpoints.*;
import static com.stg.utils.FlexibleCommon.METADATA_FLEX_KEY;
import static com.stg.utils.FlexibleCommon.fxTransactionMetadataKey;
import static com.stg.utils.FlexibleCommon.processCifCreateQuoteIdKey;
import static com.stg.utils.FundingSource.CARD;
import static com.stg.utils.InstallmentPopup.SHOW;
import static com.stg.utils.InsurancePackageType.COMBO;
import static com.stg.utils.NlpUtil.isMatchFullNameT24;
import static com.stg.utils.SourceType.MB;
import static com.stg.utils.enums.ErrorCodeCardPartner.INSTALLMENT_SUCCESS;
import static com.stg.utils.enums.ErrorCodeCardPartner.INSTALLMENT_UNQUALIFIED_CANCEL;
import static com.stg.utils.enums.ErrorCodeCardPartner.SOURCE_MONEY_INVALID;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ExternalAPIServiceImpl implements ExternalAPIService {

    private static final String MINI_ERROR_CONVERT_JSON = "[MINI]--Lỗi khi convert error data to json {}";
    private static final String NO = "K";
    private static final String PROTECT_TYPE = "PROTECT";
    private static final String INVERT_TYPE = "INVEST";
    private static final BigDecimal SIX_MILIONS = BigDecimal.valueOf(6000000);
    private static final BigDecimal TEN_MILIONS = BigDecimal.valueOf(10000000);
    private static final BigDecimal FIFTEEN_MILIONS = BigDecimal.valueOf(15000000);
    private static final BigDecimal TWENTY_MILIONS = BigDecimal.valueOf(20000000);
    private static final BigDecimal THIRTY_MILIONS = BigDecimal.valueOf(30000000);
    private static final BigDecimal FIFTY_MILIONS = BigDecimal.valueOf(50000000);
    private static final BigDecimal ONE_HUNDERED_MILIONS = BigDecimal.valueOf(100000000);
    private static final String VND = " VND";

    private static final String ONE_YEAR = "1 năm";
    private static final String EVERY_YEAR = "Hàng năm";
    private static final String PACKAGE_MIX = "MB Ageas Life + MIC";
    private static final String PACKAGE_MBAL = "MB Ageas Life";
    private static final String PACKAGE_MB_AL = "Bảo hiểm MB Ageas Life";

    @Value("${external.host.mb-host}")
    private String mbHost;
    @Value("${external.host.mbal-host}")
    private String mbalHost;

    @Value("${external.mb-key.merchant-code}")
    private String mbMerchantCode;
    @Value("${external.mb-key.merchant-secret}")
    private String mbMerchantSecret;
    @Value("${external.mb-key.checksum-secret}")
    private String mbCallbackChecksumSecret;
    @Value("${external.mb-key.transaction-type}")
    private String transactionType;
    @Value("${external.mbal-key.merchant_code}")
    private String mbalMerchantCode;

    @Value("${cron.num_day_before}")
    private Integer numDayBefore;

    @Value("${api3rd_endpoint.baas.list_card}")
    private String baasListCardUrl;
    @Value("${api3rd.hcm.mb-unit-code}")
    private String mbUnitCode;

    private final InsuranceContractRepository insuranceContractRepository;
    private final MicPackageRepository micPackageRepository;
    private final MicPackageService micPackageService;
    private final BaasService baasService;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final CustomerRepository customerRepository;
    private final JwtAccessTokenFactory jwtAccessTokenFactory;

    private final PackagePhotoRepository packagePhotoRepository;
    private final InsurancePackageRepository insurancePackageRepository;
    private final MbalPackageRepository mbalPackageRepository;
    private final ExternalMicAPIService micAPIService;
    private final SegmentRepository segmentRepository;
    private final AsyncObjectImpl asyncObject;
    private final InsuranceRequestRepository insuranceRequestRepository;
    private final ProductMbalRepository productMbalRepository;
    private final InsuredMbalRepository insuredMbalRepository;

    private final AddressProvinceRepository addressProvinceRepository;

    private final MbEmployeeRepository mbEmployeeRepository;
    private final IdentificationService identificationService;

    private final ToolCrmApi3rdService toolCrmApi3rdService;
    private final AutoDebitPaymentService autoDebitPaymentService;

    private final InsuranceContractService insuranceContractService;
    private final CardInstallmentService cardInstallmentService;
    private final PrimaryProductRepository primaryProductRepository;
    private final AdditionalProductRepository additionalProductRepository;
    private final PaymentLockService paymentLockService;
    private final InsuranceContractSyncRepository contractSyncRepository;
    private final InsuranceContractSyncDetailRepository contractSyncDetailRepository;

    private final HcmApi3rdService hcmApi3rdService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final Gson gson;

    @Autowired
    private final CommonService commonService;

    @Autowired
    private final RedisCommands<String, String> redis;


    @Override
    public MbCustomerInfoRespDto customerInfo(TokenReqDto reqDto) {
        HttpEntity<?> entity = new HttpEntity<>(reqDto, generateHeader());
        String token = reqDto.getToken();

        String payloadLog = getPayloadLogObject(reqDto);
        MbCustomerInfoRespDto customerInfo = getResponseForPostRequest(mbHost + MB_VALIDATE_TOKEN, entity, MbCustomerInfoRespDto.class, payloadLog, Constants.HOST_PARTY.MB);
        if (customerInfo != null) {
            OauthToken accessToken = jwtAccessTokenFactory.createAccessToken(token, customerInfo.getCif());
            customerInfo.setOauthToken(accessToken);

            Segment segment = segmentRepository.findBySector(customerInfo.getSector());
            if (segment == null) {
                customerInfo.setIsSpecial(true);
                segment = segmentRepository.findBySector("Còn lại");
            }
            customerInfo.setIsSpecial(true);
            customerInfo.setSegment(new SegmentDto(segment.getSegment(), segment.getGain()));

            Optional<MbEmployee> mbEmployee = mbEmployeeRepository.findFirstByIdentityCardNumber(customerInfo.getIdCardNo());
            if (mbEmployee.isPresent()) {
                customerInfo.setManagingUnit(mbEmployee.get().getManagingUnit());
            } else if (hcmApi3rdService.isEmployeeFromHcm(customerInfo.getIdCardNo())) {
                customerInfo.setManagingUnit(mbUnitCode);
            } else {
                customerInfo.setManagingUnit(null);
            }

            Customer customerByMbId = customerRepository.findByMbId(customerInfo.getCif());
            if (customerByMbId == null) {
                Customer customer = new Customer();
                customer.setMbId(customerInfo.getCif());
                customer.setFullNameT24(customerInfo.getFullname()); // fullName sẽ có dấu
                customer.setManagingUnit(customerInfo.getManagingUnit()); // thông tin đơn vị quản lý
                customer.setGender(customerInfo.getGender());
                customer.setBirthday(customerInfo.getDob());
                customer.setPhone(customerInfo.getMobile());
                customer.setIdentification(customerInfo.getIdCardNo());
                customer.setIdCardType(customerInfo.getIdCardType());

                customer.setAddress(customerInfo.getAddress());

                AddressInfo addressInfo = AddressUtil.splitAddress(customerInfo.getAddress());
                String street = addressInfo.getStreetName() == null ? "" : addressInfo.getStreetName().toUpperCase();
                String district = addressInfo.getDistrictName() == null ? street : addressInfo.getDistrictName().toUpperCase();
                String province = addressInfo.getProvinceName() == null ? street : addressInfo.getProvinceName().toUpperCase();
                String ward = addressInfo.getWardName() == null ? street : addressInfo.getWardName().toUpperCase();

                Optional<AddressDTO> addressProvince = addressProvinceRepository.findByUnsignedName(district,province,ward);
                addressProvince.ifPresent(v -> {
                    customer.setProvinceName(v.getProvinceName());
                    customer.setDistrictName(v.getDistrictName());
                    customer.setWardName(v.getWardName());
                });
                customer.setLine1(addressInfo.getStreetName());

                customer.setSegment(segment);
                customer.setIdentificationDate(customerInfo.getIdentificationDate());
                customer.setIdIssuedPlace(customerInfo.getIdIssuedPlace());
                Customer customerSaved = customerRepository.save(customer);
                customerInfo.setCustomerId(customerSaved.getId());

                // set full-name by updated
                customerInfo.setFullnameT24(customer.getFullNameT24());
                customerInfo.setFullname(customer.getFullName());
            } else {
                if (customerInfo.getDob() == null) {
                    customerInfo.setDob(DateUtil.localDateTimeToString(DATE_YYYY_MM_DD, customerByMbId.getBirthday()));
                }
                if (customerInfo.getGender() == null) {
                    customerInfo.setGender(customerByMbId.getGender());
                }

                updateCustomer(customerInfo, customerByMbId); // update!

                customerInfo.setCustomerId(customerByMbId.getId());
                customerInfo.setIdentificationDate(customerByMbId.getIdentificationDate() == null ? null : DateUtil.localDateTimeToString(DATE_YYYY_MM_DD, customerByMbId.getIdentificationDate()));
                customerInfo.setIdIssuedPlace(customerByMbId.getIdIssuedPlace());
                //add address
                customerInfo.setProvinceName(customerByMbId.getProvinceName());
                customerInfo.setDistrictName(customerByMbId.getDistrictName());
                customerInfo.setWardName(customerByMbId.getWardName());
                customerInfo.setLine1(customerByMbId.getLine1());
                // Line1 for old customers
                if (StringUtils.hasText(customerInfo.getProvinceName()) &&
                        StringUtils.hasText(customerInfo.getDistrictName()) &&
                        StringUtils.hasText(customerInfo.getWardName()) &&
                        !StringUtils.hasText(customerInfo.getLine1())) {
                    customerInfo.setLine1(customerByMbId.getLine1());
                }

                // set full-name by updated
                customerInfo.setFullnameT24(customerByMbId.getFullNameT24());
                if (customerInfo.isHasMustNameAccent()) {
                    customerInfo.setFullname(null); // lần đầu tiên đăng nhập với user cũ
                } else {
                    customerInfo.setFullname(customerByMbId.getFullName());
                }
            }

            customerInfo.setPopupData(retrieveInstallmentShowPopup(customerInfo.getCif()));
            return customerInfo;
        }
        throw new MBApiAuthenticationException(MSG12);
    }

    private void updateCustomer(MbCustomerInfoRespDto customerInfo, Customer customer) {
        if (customer.getFullNameT24() == null) {
            //customer.setFullName(null); // reset fullName => nhập có dấu
            customerInfo.setHasMustNameAccent(true);
        }
        customer.setFullNameT24(customerInfo.getFullname());
        customer.setManagingUnit(customerInfo.getManagingUnit()); // thông tin đơn vị quản lý

        customer.setPhone(customerInfo.getMobile());
        if (!customerInfo.getIdCardType().equalsIgnoreCase(customer.getIdCardType()) || !customerInfo.getIdCardNo().equalsIgnoreCase(customer.getIdentification())) {
            customer.setIdentification(customerInfo.getIdCardNo());
            customer.setIdCardType(customerInfo.getIdCardType());
            customer.setIdIssuedPlace(customerInfo.getIdIssuedPlace());
            customer.setIdentificationDate(customerInfo.getIdentificationDate());
        } else {
            if (customerInfo.getIdIssuedPlace() != null) {
                customer.setIdIssuedPlace(customerInfo.getIdIssuedPlace());
            }
            if (customerInfo.getIdentificationDate() != null) {
                customer.setIdentificationDate(customerInfo.getIdentificationDate());
            }
        }
        //update address: for old customers
        if (customerInfo.getAddress() != null &&
                (customerInfo.getProvinceName() == null || //Từng vào webview, nhưng chưa mua (&& customerInfo.getDistrictName() == null && customerInfo.getWardName() == null)
                !customer.getAddress().equals(customerInfo.getAddress()) // T24 update...?
                )) {
            customer.setAddress(customerInfo.getAddress());

            AddressInfo addressInfo = AddressUtil.splitAddress(customerInfo.getAddress());
            String street = addressInfo.getStreetName() == null ? "" : addressInfo.getStreetName().toUpperCase();
            String district = addressInfo.getDistrictName() == null ? street : addressInfo.getDistrictName().toUpperCase();
            String province = addressInfo.getProvinceName() == null ? street : addressInfo.getProvinceName().toUpperCase();
            String ward = addressInfo.getWardName() == null ? street : addressInfo.getWardName().toUpperCase();

            Optional<AddressDTO> addressProvince = addressProvinceRepository.findByUnsignedName(district,province,ward);
            addressProvince.ifPresent(v -> {
                customer.setProvinceName(v.getProvinceName());
                customer.setDistrictName(v.getDistrictName());
                customer.setWardName(v.getWardName());
            });
            customer.setLine1(addressInfo.getStreetName());
        }
        customerRepository.saveAndFlush(customer);
    }

    @Override
    public MbTransactionRespDto miniStartTransaction(String cif, MiniTransactionReqDto reqDto) {

        log.info("[MB] Start new transaction with sessionId {}", reqDto.getSessionId());
        verifyCustomerPermission(cif, reqDto.getProcessId());
        paymentLockService.doLockMbStartTransOfCombo(reqDto.getProcessId());

        Long totalAmount = convertToLong(reqDto.getTotalAmount());
        Long micAmount = convertToLong(reqDto.getMicAmount());
        Long mbalAmount = convertToLong(reqDto.getMbalAmount());
        validateInputCreateTransaction(cif, totalAmount, micAmount, mbalAmount, reqDto);
        if (totalAmount.equals(MIN_AMOUNT) || totalAmount.equals(MAX_AMOUNT)) {
            log.error("[MB]--So tien giao dich vuot ngoai gioi han hoac = 0");
            throw new MBApiException("So tien giao dich vuot ngoai gioi han hoac = 0");
        }

        String insuranceRequestId = redis.get(illustrationKey(cif));
        String rmCache = redis.get(rmCifProcessIdKey(cif, reqDto.getProcessId()));
        String supporterCache = redis.get(supporterCifProcessIdKey(cif, reqDto.getProcessId()));

        if (insuranceRequestId == null) {
            log.error("[MINI]-Lỗi khi tạo YCBH cho cif {}", cif);
            throw new ApplicationException(MSG12);
        }
        InsuranceRequest insuranceRequest = insuranceRequestRepository.getOne(Long.valueOf(insuranceRequestId));
        // create customer data
        Customer customer = customerRepository.findByMbId(cif);
        // Create payment data
        InsurancePayment insurancePayment = new InsurancePayment();
        InsurancePayment insurancePaymentExist = insurancePaymentRepository.findByInsuranceRequest(insuranceRequest.getId());
        if (insurancePaymentExist != null) {
            log.info("[MINI]--Combo redirect app failed with input data {}", reqDto);
            if (PAID.equals(insurancePaymentExist.getTranStatus())) {
                throw new ValidationException("Bạn không thể tiếp tục. Giao dịch đã thanh toán thành công!");
            }

            insurancePayment = insurancePaymentExist;
            insurancePayment.setPaymentTime(LocalDateTime.now());
            insurancePayment.setUpdateTime(LocalDateTime.now());
        }
        insuranceRequest.setProcessId(Long.valueOf(reqDto.getProcessId()));
        insurancePayment.setCustomer(customer);
        insurancePayment.setTotalFee(reqDto.getTotalAmount());
        insurancePayment.setMicInsuranceFee(reqDto.getMicAmount());
        insurancePayment.setMbalInsuranceFee(reqDto.getMbalAmount());
        insurancePayment.setFeePaymentTime(insuranceRequest.getInsurancePackage().getMbalFeePaymentTime());
        insurancePayment.setPeriodicFee(EVERY_YEAR);
        insurancePayment.setInstallment(reqDto.isInstallment());
        insurancePayment.setPresenterPhone(reqDto.getRmPhone());
        insurancePayment.setRootAccount(null);
        insurancePayment.setTransactionType(reqDto.getTransactionType());
        insurancePayment.setTranStatus(PENDING);
        insurancePayment.setControlState(ControlStateEnum.WAITING);
        insurancePayment.setInsuranceRequest(insuranceRequest);
        insurancePayment.setNormal(true);
        insurancePayment.setMicContractNum(reqDto.getMicContractNum());
        insurancePayment.setMbalAppNo(reqDto.getMbalAppNo());

        insurancePayment.setAutoPay(reqDto.isAutoPay());

        String icCache = redis.get(icCodeKey(cif, reqDto.getProcessId()));
        log.info("[MINI]--icCache {}", icCache);
        if (icCache != null) {
            Map<String, String> icCacheMap = revertMetadata(icCache);
            String icCode = icCacheMap.get(IC_CODE);
            String icName = icCacheMap.get(IC_NAME);
            insurancePayment.setIcCode(icCode);
            insurancePayment.setIcName(icName);
        }
        if (rmCache != null) {
            Common.Referer referer = gson.fromJson(rmCache, Common.Referer.class);
            insurancePayment.setRmName(referer.getName());
            insurancePayment.setRmCode(referer.getCode());
            insurancePayment.setRmPhoneNumber(referer.getPhoneNumber());

            insurancePayment.setRmEmail(referer.getEmail());
            insurancePayment.setBranchCode(referer.getBranchCode());
            insurancePayment.setBranchName(referer.getBranchName());
            insurancePayment.setDepartmentCode(referer.getDepartmentCode());
            insurancePayment.setDepartmentName(referer.getDepartmentName());
        }
        if (supporterCache != null) {
            Common.Supporter supporter = gson.fromJson(supporterCache, Common.Supporter.class);
            insurancePayment.setSupportCode(supporter.getCode());
            insurancePayment.setSupportName(supporter.getName());

            insurancePayment.setSupportPhoneNumber(supporter.getPhoneNumber());
            insurancePayment.setSupportEmail(supporter.getEmail());
        }

        // Installment
        String installmentCache = commonService.getProcessCacheDataNotThrow(installmentCifProcessIdKey(cif, reqDto.getProcessId()));
        if (reqDto.isInstallment() && installmentCache != null) {
            GetInstFeeResponse insFee = gson.fromJson(installmentCache, GetInstFeeResponse.class);
            insurancePayment.setPeriod(insFee.getData().getPeriod().replace(".0", ""));
            insurancePayment.setPeriodicConversionFee(insFee.getData().getPeriodicConversionFee());
            insurancePayment.setFeesPayable(insFee.getData().getFeesPayable());
            insurancePayment.setNormal(false);
            insurancePayment.setInstallment(reqDto.isInstallment());
        }

//        if (mbalAccessToken != null) {
//            metadata = getMetadataTokenNull(mbalAccessToken, reqDto, totalAmount, micAmount, mbalAmount);
//        } else {
        String metadata = generateUllV3Metadata(micAmount, mbalAmount,
                reqDto.getSessionId(), reqDto.getMbalPolicyNumber(), reqDto.getMicContractNum(),
                reqDto.getMbalAppNo(), totalAmount, ULL_V3, cif, reqDto.getMicPackageId() == null ? 0 : reqDto.getMicPackageId(),
                reqDto.getMbalPackageId() == null ? 0 : reqDto.getMbalPackageId(),
                reqDto.getInsurancePackageId() == null ? 0 : reqDto.getInsurancePackageId(), reqDto.getMixPackageName(),
                reqDto.isInstallment());
//        }
        MbTransactionReqDto mbTransactionReq = new MbTransactionReqDto()
                .setAllowCard(true)
                .setSessionId(reqDto.getSessionId())
                .setDescription(reqDto.getDescription())
                .setCif(cif)
                .setMetadata(metadata)
                .setType(transactionType)
                .setSuccessMessage(MSG23)
                .setAmount(totalAmount);

        HttpEntity<MbTransactionReqDto> entity = new HttpEntity<>(mbTransactionReq, generateHeader());

        try {
            String payloadLog = getPayloadLogObject(reqDto);
            MbTransactionRespDto respDto = getResponseForPostRequest(mbHost + MB_START_TRANSACTION, entity, MbTransactionRespDto.class, payloadLog, Constants.HOST_PARTY.MB);
            if (respDto != null) {
                insurancePayment.setTransactionId(respDto.getId());
                insurancePaymentRepository.save(insurancePayment);
            }
            return respDto;
        } catch (Exception e) {
            log.error("[MB] Have exception when create transaction {}", e.getMessage());
            throw new MBApiException("Khởi tạo giao dịch thất bại!");
        }
    }

    private void validateInputCreateTransaction(String cif, Long totalAmount, Long micAmount, Long mbalAmount, MiniTransactionReqDto reqDto) {
        // Validate amount
        String amountCache = redis.get(processAmountIdKey(cif, reqDto.getProcessId()));
        if (amountCache == null) {
            log.error("[MINI]--Lỗi thay đổi giá trị thanh toán bảo hiểm");
            throw new MiniApiException(MSG12);
        }
        Map<String, String> amountCacheMap = revertMetadata(amountCache);
        Long micAmountCache = Long.valueOf(amountCacheMap.get(MIC_AMOUNT));
        Long mbalAmountCache = Long.valueOf(amountCacheMap.get(MBAL_AMOUNT));
        Long totalAmountCache = Long.valueOf(amountCacheMap.get(TOTAL_AMOUNT));
        if (!micAmountCache.equals(micAmount) || !mbalAmountCache.equals(mbalAmount) || !totalAmountCache.equals(totalAmount)) {
            log.error("[MINI]--Giá trị hợp đồng đầu vào {}", reqDto);
            log.error("[MINI]--Giá trị hợp đồng bị thay đổi đầu vào");
            log.error("[MINI]--amountCacheMap {}", amountCacheMap);
            throw new MiniApiException(MSG12);
        }
    }

    //UL2.0: remove
//    private String getMetadataTokenNull(String mbalAccessToken, MiniTransactionReqDto reqDto, Long totalAmount, Long micAmount, Long mbalAmount) {
//        String metadata;
//        String ulVersion;
//        ulVersion = ULL_V2;
//        // Call to MBAL create order
//        MbalCreateOrderRespDto mbalCreateOrder = mbalCreateOrder(mbalAccessToken, reqDto);
//        if (mbalCreateOrder == null) {
//            log.error("[MBAL]--Lỗi khi tạo create order cho UL2.0");
//            throw new MbalApiException(MSG12);
//        }
//        metadata = generateUllV2Metadata(micAmount, mbalAmount, reqDto.getSessionId(), reqDto.getMbalPolicyNumber(),
//                reqDto.getMicContractNum(), reqDto.getMbalAppNo(), totalAmount, ulVersion, reqDto.getCif(), mbalCreateOrder.getId(),
//                reqDto.getMicPackageId() == null ? 0 : reqDto.getMicPackageId(),
//                reqDto.getMbalPackageId() == null ? 0 : reqDto.getMbalPackageId(),
//                reqDto.getInsurancePackageId() == null ? 0 : reqDto.getInsurancePackageId(), reqDto.getMixPackageName(),
//                mbalCreateOrder.getType().getCode());
//        return metadata;
//    }

    @Override
    public MbCallBackTransactionReqDto getTransaction(String transactionId) {
        HttpEntity<Object> entity = new HttpEntity<>(generateHeader());
        String urlGetTransaction = String.format(MB_GET_TRANSACTION, transactionId);
        try {
            return restTemplate.exchange(String.format("%s%s", mbHost, urlGetTransaction), HttpMethod.GET, entity, MbCallBackTransactionReqDto.class).getBody();
        } catch (Exception e) {
            log.error("[MB]--Failed when get transaction with id {}", transactionId);
            return null;
        }
    }

    /* Exception?save to DB */
    @Override
    public void mbCallBackTransaction(MbCallBackTransactionReqDto reqDto, CallbackType callbackType) {
        log.info("[MB]--callback data {}, callbackType {} ", reqDto, callbackType);

        String paymentTransId = reqDto.getId();
        paymentLockService.lockMbCallbackTrans(paymentTransId, () -> {
            try {
                if (AUTO.equals(callbackType)) {
                    boolean checksumCallBackTransaction = validateMBChecksumCallBackTransaction(reqDto.getChecksum(), paymentTransId,
                            reqDto.getType().getCode(), reqDto.getCif(), reqDto.getAmount(), reqDto.getStatus());
                    if (!checksumCallBackTransaction) {
                        log.error("[MB] Checksum callback invalid: {}", reqDto.getChecksum());
                        throw new MBApiException("Checksum callback invalid!");
                    }
                }

                InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(paymentTransId);
                Map<String, String> revertMetadata = revertMetadata(reqDto.getMetadata());
                insurancePayment.setFundingSource(FundingSource.valueOf(reqDto.getFundingSource()));

                if (revertMetadata == null) {
                    log.error("[MINI]--Lỗi revert meta data callback from MB. Request {}", reqDto);
                    throw new ApplicationException(MSG12);
                }

                //get-customer
                String cif = revertMetadata.get(CIF);
                Customer customer = customerRepository.findByMbId(cif);

                //check if flexible transaction
                if (TRANS_FLEXIBLE_TYPE.equals(revertMetadata.get(TRANSACTION_TYPE_KEY))) {
                    InsuranceContract flexibleInsuranceContract = genFlexibleContract(customer, 23, reqDto.getMbReferenceNumber(), insurancePayment);
                    flexibleInsuranceContract.setTransactionId(insurancePayment.getTransactionId());
                    flexibleInsuranceContract.setMbReferenceNumber(reqDto.getMbReferenceNumber());
                    log.info("[MINI]--Callback payment flexible transaction {}", reqDto);
                    List<AdditionalProduct> micAdditionalProducts = additionalProductRepository.findByInsuranceRequestAndType(insurancePayment.getInsuranceRequest().getId(), MIC);

                    if (reqDto.getStatus().equals(PAID)) {
                        if (PAID.equals(insurancePayment.getTranStatus())) {
                            log.info("[MB]--Duplicate Flexible callback from payment hub with transaction {}", paymentTransId);
                            return;
                        }
                        insurancePayment.setTranStatus(PAID);
                        insurancePayment.setUpdateTime(LocalDateTime.now());
                        insuranceContractRepository.save(flexibleInsuranceContract);
                        OauthToken oauthToken = baasService.generateToken();
                        baasService.flexiblePayOnBehalfProcess(cif, oauthToken.getAccessToken(), paymentTransId, insurancePayment, micAdditionalProducts);
                        //Dang ky tra gop
                        if (insurancePayment.isInstallment()) {
                            createInstallmentProcess(insurancePayment, reqDto);
                        }

                        //Dang ky auto-debit
                        if (insurancePayment.isAutoPay()) {
                            autoDebitPaymentService.createAutoDebitNonOTP(customer, insurancePayment, reqDto);
                        }
                    } else {
                        insuranceContractRepository.save(flexibleInsuranceContract);
                        log.error("[MB] Callback trạng thái pending khi thanh toán với sessionId {}", paymentTransId);
                    }

                    // UPDATE CRM
                    InsuranceRequest insuranceRequest = insurancePayment.getInsuranceRequest();
                    if (insuranceRequest.getCrmToolUuid() != null) {
                        QuotationSyncDataResp syncResponse = toolCrmApi3rdService.syncData(new QuotationSyncDataReq(insuranceRequest.getCrmToolUuid(), QuotationAction.COMPLETED));
                        log.info("Sync data tool-crm: mbTransactionId={}, toolUuid={}, action={}, state={}", paymentTransId, insuranceRequest.getCrmToolUuid(), QuotationAction.COMPLETED, syncResponse.getState());
                    }

                    return;
                }

                // Saving payment data and call BAAS
                InsuranceContract insuranceContract = genContract(customer, revertMetadata.get(MIX_PACKAGE_NAME),
                        Integer.valueOf(revertMetadata.get(INSURANCE_PACKAGE_ID)),
                        Integer.valueOf(revertMetadata.get(MIC_ID)), Integer.valueOf(revertMetadata.get(MBAL_ID)),
                        revertMetadata.get(MIC_GCNBH), revertMetadata.get(MBAL_APP_NO),
                        insurancePayment.getTotalFee(), insurancePayment.getMbalInsuranceFee(), insurancePayment.getMicInsuranceFee());
                insuranceContract.setTransactionId(insurancePayment.getTransactionId());
                insuranceContract.setMbReferenceNumber(reqDto.getMbReferenceNumber());
                String ullVersion = revertMetadata.get(ULL_VERSION);
                insuranceContract.setMbalProductName(ullVersion.equals(ULL_V2) ? MBAL_PRODUCT_NAME_V2 : MBAL_PRODUCT_NAME_V3);
                insuranceContract.setMbalFeePaymentTime(insurancePayment.getFeePaymentTime());
                if (reqDto.getStatus().equals(PAID)) {
                    if (PAID.equals(insurancePayment.getTranStatus())) {
                        log.info("[MB]--Duplicate callback from payment hub with transaction {}", paymentTransId);
                        return;
                    }
                    insurancePayment.setTranStatus(PAID);
                    insurancePayment.setUpdateTime(LocalDateTime.now());
                    insuranceContractRepository.save(insuranceContract);
                    OauthToken oauthToken = baasService.generateToken();
                    baasService.payOnBehalfProcess(oauthToken.getAccessToken(), revertMetadata.get(MIC_AMOUNT),
                            revertMetadata.get(MBAL_AMOUNT), revertMetadata.get(MIC_GCNBH), revertMetadata.get(MBAL_APP_NO),
                            paymentTransId, ullVersion, revertMetadata.get(CIF),
                            reqDto.getFundingSource(), reqDto.getCardType(), revertMetadata.get(CREATE_ORDER_ID),
                            revertMetadata.get(HOOK_TYPE_COE));

                    //Dang ky tra gop
                    if (insurancePayment.isInstallment()) {
                        createInstallmentProcess(insurancePayment, reqDto);
                    }

                    //Dang ky auto-debit
                    if (insurancePayment.isAutoPay()) {
                        autoDebitPaymentService.createAutoDebitNonOTP(customer, insurancePayment, reqDto);
                    }
                } else {
                    insuranceContractRepository.save(insuranceContract);
                    log.error("[MB] Callback trạng thái pending khi thanh toán với sessionId {}", paymentTransId);
                }
            } catch (Exception ex) {
                log.error("[MB][ERROR] transactionId=" + paymentTransId, ex);
            }
        });
    }

        //Dang ky tra gop
        private void createInstallmentProcess (InsurancePayment insurancePayment, MbCallBackTransactionReqDto reqDto){
            if (CARD.getLabel().equals(reqDto.getFundingSource())) {
                log.info("[MINI]--Bắt đầu đăng ký trả góp cho giao dịch mbReferenceNumber: {}, way4DocsId: {}", reqDto.getMbReferenceNumber(), reqDto.getWay4DocsId());
                cardInstallmentService.createInstallmentNoOtpProcess(reqDto.getMbReferenceNumber(), insurancePayment.getPeriod(),
                        reqDto.getCardType(), insurancePayment);
            } else {
                log.info("[MINI]--Khách hàng thanh toán gói bảo hiểm không phải bằng thẻ. Hủy đăng ký trả góp. Giao dịch {}", reqDto.getMbReferenceNumber());
                insurancePayment.setInstallmentStatus(ErrorCodeCardPartner.INSTALLMENT_UNQUALIFIED_CANCEL.getLabelEn());
                insurancePayment.setInstallment(false);
                insurancePayment.setNormal(true);
                insurancePayment.setInstallmentPopup(SHOW);
                insurancePayment.setInstallmentErrorCode(SOURCE_MONEY_INVALID.getCode());
                insurancePaymentRepository.save(insurancePayment);
            }
        }

        @Override
        public PackageTypeRespDtos packageInfo (String mbalAccessToken, PackageTypeReqDto packageTypeReqDto){
            HttpEntity<PackageTypeReqDto> entity = new HttpEntity<>(packageTypeReqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogObject(packageTypeReqDto);
            return getResponseForPostRequest(mbalHost + MBAL_PACKAGES_INFO, entity, PackageTypeRespDtos.class, payloadLog, Constants.HOST_PARTY.MBAL);
        }

        @Override
        public MbalCustomerInfoRespDto mbalCheckCustomerInfo (String cif, String mbalAccessToken, MbalCustomerInfoReqDto
        reqDto){
            String micTransactionId = generateUUIDId(30);
            validateInsuranceAge(reqDto.getDob());
            if (reqDto.getOccupationClass() == 5) {
                throw new MbalApiException(MSG02);
            }
            HttpEntity<MbalCustomerInfoReqDto> entity = new HttpEntity<>(reqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogCheckCustomerInfo(reqDto);
            MbalCustomerInfoRespDto responseData = getResponseForPostRequest(mbalHost + MBAL_CHECK_CUSTOMER_INFO, entity, MbalCustomerInfoRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
            if (Objects.requireNonNull(responseData).getResult() == 0) {
                throw new MbalApiException(responseData.getErrorMessage());
            }

            updateCustomerInfoCreateQuote(cif, reqDto);

            // MBAL checking jetCase or non-jetCase
            MbalValidateBpRespDto validateBpRespDto = validateBp(mbalAccessToken, reqDto);
            if (validateBpRespDto.getStatus().equals(STATUS_OK)) {
                responseData.setJetCase(true);
            }
            responseData.setStatus(validateBpRespDto.getStatus());
            responseData.setErrorMessage(validateBpRespDto.getMessage());

            // MIC tinh phí
            MiniMicFeeReqDto micFeeReqDto = new MiniMicFeeReqDto();
            micFeeReqDto.setNhom(reqDto.getNhom());
            micFeeReqDto.setDob(reqDto.getDob());
            Common.GcnMicCareDkbs gcnMicCareDkbs = new Common.GcnMicCareDkbs();
            gcnMicCareDkbs.setBs1(NO);
            gcnMicCareDkbs.setBs2(NO);
            gcnMicCareDkbs.setBs3(NO);
            gcnMicCareDkbs.setBs4(NO);
            if (FREE_STYLE.equalsIgnoreCase(reqDto.getType())) {
                gcnMicCareDkbs.setBs1(reqDto.getBs1());
                gcnMicCareDkbs.setBs2(reqDto.getBs2());
                gcnMicCareDkbs.setBs3(reqDto.getBs3());
                gcnMicCareDkbs.setBs4(reqDto.getBs4());
            }
            micFeeReqDto.setGcn_miccare_dkbs(gcnMicCareDkbs);
            MicInsuranceResultRespDto micInsuranceResult = micAPIService.micFeeResult(micFeeReqDto);
            long totalInsuranceFee = reqDto.getInsuranceFee() + micInsuranceResult.getPhi().longValue();
            responseData.setStrMixInsuranceFee(formatCurrency(BigDecimal.valueOf(totalInsuranceFee)));
            responseData.setMixInsuranceFee(BigDecimal.valueOf(totalInsuranceFee));

            // MBAL Gen Quote
            GenerateQuoteRespDto quote = mbalGenerateQuote(mbalAccessToken, reqDto);
            responseData.setInsuranceFee(quote.getInsuranceFee());
            responseData.setBaseInsuranceFee(quote.getBaseInsuranceFee());
            responseData.setTopupInsuranceFee(quote.getTopupInsuranceFee());
            responseData.setHscrFee(quote.getHscrFee());
            responseData.setAmount(quote.getAmount());
            responseData.setPayFrequency(quote.getPayFrequency());
            responseData.setTimeFrequency(quote.getTimeFrequency());
            responseData.setHsTimeFrequency(quote.getHsTimeFrequency());

            // MBAL gen application number
            MbalGenAppNumberRespDto genAppNumber = genAppNumber(mbalAccessToken, reqDto);
            responseData.setMbalAppNo(genAppNumber.getAppNo());
            responseData.setMbalPolicyNumber(genAppNumber.getPolicyNumber());

            // MIC generate GCNBH
            MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = micAPIService.generateMicCareTtinkh(reqDto.getFullname(),
                    reqDto.getIdCardNo(), reqDto.getGender(),
                    reqDto.getDob(), reqDto.getAddress(), reqDto.getEmail(), reqDto.getPhone());
            micAPIService.micGenerateInsuranceCert(ttinkh, micInsuranceResult.getPhi().longValue(),
                    micTransactionId, reqDto.getNhom(), gcnMicCareDkbs);
            responseData.setMicTransactionId(micTransactionId);
            return responseData;
        }

        private void updateCustomerInfoCreateQuote (String cif, MbalCustomerInfoReqDto reqDto){
            // Update thông tin khách hàng
            Customer customerByMbId = customerRepository.findByMbId(cif);
            if (customerByMbId == null) {
                throw new CustomerNotFoundException("Không tồn tại Khách hàng có MbId là " + cif);
            }
            if (reqDto.getFullname() != null) {
                if (!isMatchFullNameT24(customerByMbId.getFullNameT24(), reqDto.getFullname())) {
                    throw new CustomerNotFoundException(MSG37);
                }
                customerByMbId.setFullName(reqDto.getFullname());
            }

            if (reqDto.getDob() != null) {
                customerByMbId.setBirthday(reqDto.getDob());
            }
            if (reqDto.getGender() != null) {
                customerByMbId.setGender(reqDto.getGender());
            }
            if (reqDto.getNationality() != null) {
                customerByMbId.setNationality(reqDto.getNationality());
            }
            if (reqDto.getOccupation() != null) {
                customerByMbId.setJob(reqDto.getOccupation());
            }
            if (reqDto.getAddress() != null) {
                customerByMbId.setAddress(reqDto.getAddress());
            }
            if (reqDto.getEmail() != null) {
                customerByMbId.setEmail(reqDto.getEmail());
            }
            customerRepository.save(customerByMbId);
        }

        @Override
        public GenerateQuoteRespDto mbalGenerateQuote (String mbalAccessToken, MbalCustomerInfoReqDto
        mbalGenerateQuoteReqDto){
            HttpEntity<MbalCustomerInfoReqDto> entity = new HttpEntity<>(mbalGenerateQuoteReqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogCheckCustomerInfo(mbalGenerateQuoteReqDto);
            return getResponseForPostRequest(mbalHost + MBAL_BMH_GENERATE_QUOTE, entity, GenerateQuoteRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
        }

        @Override
        public PackageProductRespDto mbalPackageProduct (String mbalAccessToken, MbalPackageProductReqDto
        mbalPackageProductReqDto){
            HttpEntity<MbalPackageProductReqDto> entity = new HttpEntity<>(mbalPackageProductReqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogObject(mbalPackageProductReqDto);
            PackageProductRespDto packageProductRespDto = getResponseForPostRequest(mbalHost + MBAL_BMH_PACKAGE_PRODUCT, entity, PackageProductRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
            if (packageProductRespDto != null) {
                packageProductRespDto.setStrDeathAccident(convertToVnd(packageProductRespDto.getDeathAccident()));
                packageProductRespDto.setStrDeathNoAccidentFrom(convertToVnd(packageProductRespDto.getDeathNoAccidentFrom()));
                packageProductRespDto.setStrDeathNoAccidentTo(convertToMoneyFormat(packageProductRespDto.getDeathNoAccidentTo()));
                return packageProductRespDto;
            }
            return new PackageProductRespDto();
        }

        @Override
        public List mbalIllustrationBoardSummary (String mbalAccessToken, MbalIllustrationBoardReqDto
        illustrationBoardDto){
            HttpEntity<MbalIllustrationBoardReqDto> entity = new HttpEntity<>(illustrationBoardDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogObject(illustrationBoardDto);
            List illustrationBoardRespDtos = getResponseForPostRequest(mbalHost + MAL_BMH_SUMMARY, entity, List.class, payloadLog, Constants.HOST_PARTY.MBAL);
            for (Object dtos : illustrationBoardRespDtos) {
                ((LinkedHashMap) dtos).put("insuranceFee", illustrationBoardDto.getInsuranceFee());
                ((LinkedHashMap) dtos).put("deathBenefit", (illustrationBoardDto.getDeathBenefit()).replace(".", "").replace(VND, ""));
            }

            return illustrationBoardRespDtos;
        }

        @Override
        public void mbalSendEmail (String mbalAccessToken, MbalSendEmailReqDto emailReqDto){
            HttpEntity<MbalSendEmailReqDto> entity = new HttpEntity<>(emailReqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogMbalSendEmail(emailReqDto);
            getResponseForPostRequest(mbalHost + MBAL_BMH_SEND_EMAIL, entity, Void.class, payloadLog, Constants.HOST_PARTY.MBAL);
        }

        @Override
        public AllCategoryRespDto getAllCategory (String mbalAccessToken){
            HttpEntity<?> entity = new HttpEntity<>(null, generateMbalDefaultHeader(mbalAccessToken));
            return restTemplate.exchange(mbalHost + MBAL_THREE_QUESTION_INFO, HttpMethod.GET, entity, AllCategoryRespDto.class).getBody();
        }

        @Override
        public MbalIllustrationBoardDetailRespDto viewIllustrationBoard (String mbalAccessToken){
            HttpEntity<?> entity = new HttpEntity<>(null, generateMbalDefaultHeader(mbalAccessToken));
            return getResponseForPostRequest(mbalHost + MBAL_VIEW_ILLUSTRATION_BOARD, entity, MbalIllustrationBoardDetailRespDto.class, "", Constants.HOST_PARTY.MBAL);
        }

        @Override
        public MbalEmployeeSetRespDto employeeSet (String mbalAccessToken, MbalEmployeeSetReqDto reqDto){
            HttpEntity<MbalEmployeeSetReqDto> entity = new HttpEntity<>(reqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogObject(reqDto);
            return getResponseForPostRequest(mbalHost + MBAL_EMPLOYEE_SET, entity, MbalEmployeeSetRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
        }

        @Override
        public MbalValidateBpRespDto validateBp (String mbalAccessToken, MbalCustomerInfoReqDto reqDto){
            HttpEntity<MbalCustomerInfoReqDto> entity = new HttpEntity<>(reqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogCheckCustomerInfo(reqDto);
            return getResponseForPostRequest(mbalHost + MBAL_VALIDATE_BP, entity, MbalValidateBpRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
        }

        @Override
        public MbalGenAppNumberRespDto genAppNumber (String mbalAccessToken, MbalCustomerInfoReqDto reqDto){
            long startTime = System.currentTimeMillis();
            HttpEntity<MbalCustomerInfoReqDto> entity = new HttpEntity<>(reqDto, generateMbalDefaultHeader(mbalAccessToken));
            String payloadLog = getPayloadLogCheckCustomerInfo(reqDto);
            MbalGenAppNumberRespDto response = getResponseForPostRequest(mbalHost + MBAL_GENERATE_APP_NUMBER, entity, MbalGenAppNumberRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
            log.info("Total time genAppNumber: {}", System.currentTimeMillis() - startTime);
            return response;
        }

    @Override
    public List<InsuranceContractDto> getContractsOfCustomer(String mbId, int count, Long lastId) {
        Pageable pageable = PageRequest.of(0, count);
        List<InsuranceContract> insuranceContracts = insuranceContractRepository.insuranceContractByMbId(mbId, lastId, "", "", pageable);
        List<InsuranceContractDto> packageDtos = insuranceContracts.stream().map(o -> modelMapper.map(o, InsuranceContractDto.class)).collect(Collectors.toList());
        for (InsuranceContractDto contractDto : packageDtos) {
            updateStatus(contractDto);
        }
        return packageDtos;

    }

    @Override
    public List<InsuranceContractsAppDto> getListContractsOfCustomer(FilterContractParam filterParam, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        if (MB.equals(filterParam.getSource())) {
            List<InsuranceContract> insuranceContracts = insuranceContractRepository.insuranceContractByMbIdAndCombo(filterParam, pageable);

            List<InsuranceContractsAppDto> contractsAppDtos = new ArrayList<>();
            for (InsuranceContract contract : insuranceContracts) {
                InsuranceContractsAppDto contractsAppDto = new InsuranceContractsAppDto();
                contractsAppDto.setId(contract.getId());
                contractsAppDto.setLogo(contract.getLogo());

                buildProvidedAndPackageName(contract, contractsAppDto);

                contractsAppDto.setStrInsuranceFee(contract.getStrInsuranceFee());
                if (PackageType.MBAL.equals(contract.getPackageType())) {
                    contractsAppDto.setStrInsuranceFee(contract.getMbalAmount());
                }
                contractsAppDto.setPackageType(contract.getPackageType());
                contractsAppDto.setStatus(contract.getStatus());
                contractsAppDto.setInceptionDate(contract.getMbalIssueDate());

                if (contract.getInsurancePackage() != null && HAPPY_TYPE.equals(contract.getInsurancePackage().getCategory())) {
                    contractsAppDto.setType(HAPPY_TYPE); // sống vui
                } else if (contract.getPackageType().equals(PackageType.FLEXIBLE)) {
                    contractsAppDto.setType(PackageType.FLEXIBLE.name());
                } else if (contract.getInsurancePackage() != null && HEALTHY_TYPE.equals(contract.getInsurancePackage().getCategory())) {
                    contractsAppDto.setType(HEALTHY_TYPE); // sống khỏe
                }

                contractsAppDto.setPolicyholderName(contract.getCustomer().getFullName());
                contractsAppDto.setCreatedDate(DateUtil.localDateTimeToString(DateUtil.DATE_DMY, contract.getMicIssueDate()));

                // todo: thông tin tái tục phase 2
//                contractsAppDto.setDueFromDate(DateUtil.localDateToString(DateUtil.DATE_DMY, contract.getDueFromDate()));
//                contractsAppDto.setDueToDate(DateUtil.localDateToString(DateUtil.DATE_DMY, contract.getDueToDate()));
//                contractsAppDto.setDueAmount(contract.getDueAmount());
//                contractsAppDto.setRenewalStatus(contract.getPremiumType());
//
//                if (contract.getDueToDate() != null) {
//                    long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), contract.getDueToDate());
//                    if (contract.getPackageType().equals(PackageType.MBAL) && daysBetween >= 1 && daysBetween <= 30) {
//                        contractsAppDto.setRemainingRenewalTime(String.format("Sắp đến hạn đóng  phí (%d ngày)", daysBetween));
//                    } else if (contract.getPackageType().equals(PackageType.MBAL) && daysBetween <= -1 && daysBetween >= -30) {
//                        contractsAppDto.setRemainingRenewalTime(String.format("Quá hạn đóng  phí (%d ngày)", Math.abs(daysBetween)));
//                    }
//                }

                contractsAppDtos.add(contractsAppDto);
            }
            return contractsAppDtos;
        }

        List<InsuranceContractVo> insuranceContractVos = contractSyncRepository.listInsuranceContract(filterParam, page, size);
        List<InsuranceContractsAppDto> contractDtos = new ArrayList<>();
        insuranceContractVos.forEach(o -> {
            InsuranceContractsAppDto contractsAppDto = modelMapper.map(o, InsuranceContractsAppDto.class);
            long additionalFee = 0; // o.getMinTopUp() == null ? 0 : o.getMinTopUp(); // không cộng topup!
            BigDecimal totalFee = BigDecimal.valueOf(o.getPeriodicPrem()).add(BigDecimal.valueOf(additionalFee));
            contractsAppDto.setCreatedDate(o.getCreatedDate());
            contractsAppDto.setStrInsuranceFee(formatCurrency(totalFee));
            contractsAppDto.setStatus(o.getStatus());
            contractsAppDto.setPackageName(PACKAGE_MB_AL);
            contractsAppDto.setType(PackageType.MBAL.name());
            contractsAppDto.setProvidedBy(PACKAGE_MBAL);
            contractsAppDto.setPeriodicPrem(formatCurrency(BigDecimal.valueOf(o.getPeriodicPrem())));
            contractsAppDto.setDueFromDate(DateUtil.localDateToString(DateUtil.DATE_YYYY_MM_DD, o.getDueFromDate()));
            contractsAppDto.setDueToDate(DateUtil.localDateToString(DateUtil.DATE_YYYY_MM_DD, o.getDueToDate()));
            contractsAppDto.setPremiumType(o.getPremiumType());
            contractsAppDto.setDueAmount(o.getDueAmount());
            contractDtos.add(contractsAppDto);
        });

        return contractDtos;
    }

    private static void buildProvidedAndPackageName(InsuranceContract contract, InsuranceContractsAppDto contractsAppDto) {
        switch (contract.getPackageType()) {
            case FIX_COMBO:
                contractsAppDto.setProvidedBy(PACKAGE_MIX);
                if (contract.getInsurancePackage() != null) {
                    contractsAppDto.setPackageName(contract.getInsurancePackage().getPackageName());
                }
                break;
            case FLEXIBLE:
                contractsAppDto.setProvidedBy(PACKAGE_MBAL);
                if (contract.getInsurancePackage() != null) {
                    contractsAppDto.setPackageName(contract.getInsurancePackage().getPackageName());
                    if (contract.getInsurancePackage().getMicPackage() != null) {
                        contractsAppDto.setProvidedBy(PACKAGE_MIX);
                    }
                }
                break;
            case FREE_STYLE:
                contractsAppDto.setProvidedBy(PACKAGE_MIX);
                contractsAppDto.setPackageName(UR_STYLE.getVal());
                break;
            case MBAL:
                contractsAppDto.setPackageName(PACKAGE_MB_AL);
                contractsAppDto.setType(PackageType.MBAL.name());
                contractsAppDto.setProvidedBy(PACKAGE_MBAL);
                if (contract.getInsurancePackage() != null && contract.getInsurancePackage().getMicPackage() != null) {
                    contractsAppDto.setProvidedBy(PACKAGE_MIX);
                }
                break;
            case ONEID:
                contractsAppDto.setPackageName("Bảo hiểm MIC");
                contractsAppDto.setType(PackageType.ONEID.name());
                break;
            default:
        }
    }

    @Override
    public InsuranceContractDto getDetailContract(String mbId, Long contractId, SourceType source) {
        if (MB.equals(source)) {
            InsuranceContract contract = insuranceContractRepository.findById(contractId).orElseThrow(() -> new InsuranceContractNotFoundException(String.format(MSG38, contractId)));
            Customer customer = contract.getCustomer();
            if (customer == null) {
                throw new MiniApiException("Lỗi không tìm thấy thông tin khách hàng của hợp đồng này!");
            }
            boolean validate = Objects.equals(customer.getMbId(), mbId);
            if (!validate) {
                throw new MiniApiException(MSG39);
            }

            InsuranceContractDto contractDto = modelMapper.map(contract, InsuranceContractDto.class);

            if (contract.getPackageType().equals(PackageType.FIX_COMBO) && contract.getInsurancePackage() != null) {
                contractDto.setPackageNames(contract.getInsurancePackage().getPackageName());
                contractDto.setProductNameMic(contract.getInsurancePackage().getMicPackage() != null ? contract.getInsurancePackage().getMicPackage().getName() : "");
            } else if (contract.getPackageType().equals(PackageType.FREE_STYLE)) {
                contractDto.setPackageNames(UR_STYLE.getVal());
                contractDto.setMicContractNum(contract.getMicContractNum());
                contractDto.setMicAmount(contract.getMicAmount());
                contractDto.setMicFeePaymentTime(contract.getMicFeePaymentTime());
                contractDto.setMicPeriodicFee(contract.getMicPeriodicFee());
                contractDto.setMbalPolicyNumber(contract.getMbalPolicyNumber());
                contractDto.setMbalAmount(contract.getMbalAmount());
                contractDto.setMbalFeePaymentTime(contract.getMbalFeePaymentTime());
                contractDto.setMbalPeriodicFeePaymentTime(contract.getMbalPeriodicFeePaymentTime());
                contractDto.setProductNameMic(contract.getMicPackage() != null ? contract.getMicPackage().getName() : "");
            }
            contractDto.setProductNameMbal(contract.getMbalProductName());

            contractDto.setStrInsuranceFee(contract.getStrInsuranceFee());
            contractDto.setCoverageYear(contract.getCoverageYear() == null ? "" : contract.getCoverageYear() + " năm");

            List<IdentificationDetailDto> identificationsDtoCustomer = identificationService.getIdentificationDetailDtos(null, customer);
            contractDto.getCustomer().setIdentifications(identificationsDtoCustomer);
            contractDto.getCustomer().setMbId(null);
            List<IdentificationDetailDto> identificationsDto = identificationService.getIdentificationDetailCustomer(contractDto.getCustomer());
            if (contractDto.getCustomer().getIdentifications().isEmpty()) {
                contractDto.getCustomer().setIdentifications(identificationsDto);
            }

            if (contract.getPackageType() == PackageType.MBAL) {
                // bảo hiểm tạo từ mbal
                List<ProductMbal> productMbalList = productMbalRepository.findByInsuranceContractId(contract.getId(), true);
                if (!productMbalList.isEmpty() && productMbalList.get(0) != null) {
                    ProductMbal productMbal = productMbalList.get(0);
                    InsuredMbal insuredMbal = insuredMbalRepository.findByInsuranceContractId(productMbal.getId());
                    InsuranceContractDto.Insured insured = new InsuranceContractDto.Insured();
                    insured.setFullName(insuredMbal.getFullName());
                    insured.setIdCardType(insuredMbal.getIdCardType());
                    insured.setIdentification(insuredMbal.getIdentification());
                    insured.setPhone(insuredMbal.getPhone());
                    insured.setAddress(insuredMbal.getAddress());
                    insured.setIdIssuedPlace(insuredMbal.getIdIssuedPlace());

                    List<IdentificationDetailDto> mainIdentificationsDto = identificationService.getIdentificationDetailDtos(insuredMbal, null);
                    insured.setIdentifications(mainIdentificationsDto);

                    contractDto.setInsured(insured);
                    contractDto.setPackageNames(productMbal.getProductName());
                    contractDto.setProductNameMbal(productMbal.getProductName());
                    contractDto.setSumInsured(formatCurrency(BigDecimal.valueOf(productMbal.getSumInsured())));
                }
                List<ProductMbal> additionalProductMbals = productMbalRepository.findByInsuranceContractId(contract.getId(), false);
                if (!additionalProductMbals.isEmpty()) {
                    List<String> additionalProductNames = new ArrayList<>();
                    BigDecimal sumInsuredAdditional = BigDecimal.ZERO;
                    for (ProductMbal productMbal : additionalProductMbals) {
                        additionalProductNames.add(productMbal.getProductName());
                        if (productMbal.getSumInsured() != null) {
                            sumInsuredAdditional = sumInsuredAdditional.add(BigDecimal.valueOf(productMbal.getSumInsured()));
                        }
                    }
                    contractDto.setAdditionalProductNames(additionalProductNames);
                    contractDto.setSumInsuredAdditional(formatCurrency(sumInsuredAdditional));
                }
            } else {
                // bảo hiểm tạo từ Mini app
                InsuranceContractDto.Insured insured = new InsuranceContractDto.Insured();
                insured.setFullName(customer.fullNameOrDefaultT24());
                insured.setIdCardType(customer.getIdCardType());
                insured.setIdentification(customer.getIdentification());
                insured.setPhone(customer.getPhone());
                insured.setAddress(customer.getAddress());
                insured.setIdIssuedPlace(customer.getIdIssuedPlace());

                insured.setIdentifications(identificationsDtoCustomer);

                if (insured.getIdentifications().isEmpty()) {
                    insured.setIdentifications(identificationsDto);
                }

                contractDto.setInsured(insured);
            }

            Long customerId = contract.getCustomer().getId();
            Long requestId = insuranceContractRepository.findInsuranceRequestIdByContractId(contract.getId());
            insuranceContractService.buildDetailContract(contractDto, customerId, requestId);

            if (contractDto.getPrimaryInsuredDTO() == null) {
                // primary Insured for mbal
                PrimaryInsuredDTO primaryInsuredDTO = new PrimaryInsuredDTO();
//                primaryInsuredDTO.setFullName(contract.getInsuredName());
//                primaryInsuredDTO.setBirthday(DateUtil.localDateToString(DateUtil.DATE_DMY, contract.getInsuredDob()));
                contractDto.setPrimaryInsuredDTO(primaryInsuredDTO);
            }

            InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(contract.getTransactionId());
            contractDto.setInsurancePayment(insurancePayment);
//            contractDto.setDueFromDate(DateUtil.localDateToString(DateUtil.DATE_DMY, contract.getDueFromDate()));
//            contractDto.setDueToDate(DateUtil.localDateToString(DateUtil.DATE_DMY, contract.getDueToDate()));

            return contractDto;
        }
        //get detail from contract sync
        InsuranceContractSync insuranceContractSync = contractSyncRepository
                .findById(contractId).orElseThrow(() -> new InsuranceContractNotFoundException(String.format(MSG38, contractId)));
        Customer customer = insuranceContractSync.getCustomer();
        if (customer == null) {
            throw new MiniApiException("Lỗi không tìm thấy thông tin khách hàng của hợp đồng này!");
        }
        boolean validate = Objects.equals(customer.getMbId(), mbId);
        if (!validate) {
            throw new MiniApiException(MSG39);
        }
        InsuranceContractSyncDetail syncDetail = contractSyncDetailRepository.getNearestContractSyncDetail(insuranceContractSync.getMbalPolicyNumber());

        InsuranceContractDto insuranceContractDto = modelMapper.map(insuranceContractSync, InsuranceContractDto.class);
        insuranceContractDto.setMbalAmount(formatCurrency(BigDecimal.valueOf(Long.parseLong(insuranceContractSync.getPeriodicPrem().trim()))));
        insuranceContractDto.setStrInsuranceFee(formatCurrency(BigDecimal.valueOf(Long.parseLong(insuranceContractSync.getPeriodicPrem().trim()))));
        insuranceContractDto.setSource(String.valueOf(SourceType.MBAL));
        insuranceContractDto.setMbalPeriodicFeePaymentTime(insuranceContractSync.getPayFrequency());
        insuranceContractDto.setProductNameMbal(insuranceContractSync.getPrdName());

        if (syncDetail != null) {
            insuranceContractDto.setDueFromDate(DateUtil.localDateToString(DateUtil.DATE_YYYY_MM_DD, syncDetail.getDueFromDate()));
            insuranceContractDto.setDueToDate(DateUtil.localDateToString(DateUtil.DATE_YYYY_MM_DD, syncDetail.getDueToDate()));
            insuranceContractDto.setDueAmount(syncDetail.getDueAmount());
            insuranceContractDto.setPremiumType(syncDetail.getPremiumType());
        }
        return insuranceContractDto;
    }

    @Override
    public MbalCreateOrderRespDto mbalCreateOrder (String mbalAccessToken, MiniTransactionReqDto reqDto){
        MbalCreateOrderReqDto createOrderReqDto = new MbalCreateOrderReqDto();
        createOrderReqDto.setDob(reqDto.getDob());
        createOrderReqDto.setGender(reqDto.getGender());
        createOrderReqDto.setPackageCode(reqDto.getPackageCode());
        createOrderReqDto.setOccupationClass(reqDto.getOccupationClass());
        createOrderReqDto.setPackageName(reqDto.getPackageName());
        createOrderReqDto.setEmail(reqDto.getEmail());
        createOrderReqDto.setPackageType(reqDto.getPackageType());
        createOrderReqDto.setAmount(convertToLong(reqDto.getMbalAmount()));
        createOrderReqDto.setAppNo(reqDto.getMbalAppNo());
        createOrderReqDto.setPolicyNumber(reqDto.getMbalPolicyNumber());
        HttpEntity<MbalCreateOrderReqDto> entity = new HttpEntity<>(createOrderReqDto, generateMbalDefaultHeader(mbalAccessToken));
        String payloadLog = getPayloadLogMbalCreateOrder(createOrderReqDto);
        return getResponseForPostRequest(mbalHost + MBAL_BMH_CREATE_ORDER, entity, MbalCreateOrderRespDto.class, payloadLog, Constants.HOST_PARTY.MBAL);
    }

    @Override
    public ResponseEntity<Void> insertUpdateUl2020Question (String reqDto, MultipartFile[]files, String cookie){
        ResponseEntity<Void> response = null;
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    map.add("files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
                }
            }

            map.add("input", reqDto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Cookie", cookie);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            response = restTemplate.exchange(mbalHost + INSERT_UPDATE_UL2020_QUESTION, HttpMethod.GET, requestEntity, Void.class);
        } catch (Exception e) {
            log.error("insertUpdateUl2020Question exception: " + e.getMessage());
            throw new MBApiException("Lỗi kết nối tới hệ thống MBAGEAS");
        }
        return response;
    }

        @Override
        public MbTransactionRespDto flexibleStartTransaction(String cif, FlexibleTransactionReqDto reqDto){
            Integer processId = reqDto.getProcessId();
            verifyCustomerPermission(cif, String.valueOf(processId));
            log.info("[MB]--Start new flexible transaction with sessionId {}", reqDto.getSessionId());
            paymentLockService.doLockMbStartTransOfFlex(processId);

        // get amount payment
        String flexibleProcessCacheData = commonService.getProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(processId)));
        if (flexibleProcessCacheData == null) {
            log.error("[MINI]--Không tồn tại dữ liệu quotation response cho processId {}", processId);
            throw new MiniApiException(MSG12);
        }

        FlexibleQuoteRespDto flexibleProcessResp = gson.fromJson(flexibleProcessCacheData, FlexibleQuoteRespDto.class);
        Long totalAmount = convertToLong(flexibleProcessResp.getTotalAmountStr());

        if (totalAmount.equals(MIN_AMOUNT) || totalAmount.equals(MAX_AMOUNT)) {
            log.error("[MB]--So tien giao dich flexible vuot ngoai gioi han hoac = 0");
            throw new MBApiException("So tien giao dich vuot ngoai gioi han hoac = 0");
        }
        InsuranceRequest insuranceRequest = insuranceRequestRepository.getOne(flexibleProcessResp.getInsuranceRequestId());

            // Saving insurance payment data for admin
            Customer customer = customerRepository.findByMbId(cif);
            // Create payment data
            InsurancePayment insurancePayment = new InsurancePayment();

            InsurancePayment insurancePaymentExist = insurancePaymentRepository.findByInsuranceRequest(insuranceRequest.getId());
            if (insurancePaymentExist != null) {
                log.info("[MINI]--Flexible redirect app failed with input data {}", reqDto);
                if (PAID.equals(insurancePaymentExist.getTranStatus())) {
                    throw new ValidationException("Bạn không thể tiếp tục. Giao dịch đã thanh toán thành công!");
                }

                insurancePayment = insurancePaymentExist;
                insurancePayment.setPaymentTime(LocalDateTime.now());
                insurancePayment.setUpdateTime(LocalDateTime.now());
            }
            insurancePayment.setCustomer(customer);
            insurancePayment.setTotalFee(flexibleProcessResp.getTotalAmountStr());
            insurancePayment.setMbalInsuranceFee(flexibleProcessResp.getTotalMbalAmountStr());
//        insurancePayment.setFeePaymentTime(insuranceRequest.getInsurancePackage().getMbalFeePaymentTime());
            insurancePayment.setFeePaymentTime(flexibleProcessResp.getProductPackage().getPremiumTerm() + " năm");
            insurancePayment.setPeriodicFee(flexibleProcessResp.getProductPackage().getPaymentPeriod().getVal());
            insurancePayment.setInstallment(reqDto.isInstallment());
            insurancePayment.setRootAccount(null);
            insurancePayment.setTransactionType(reqDto.getTransactionType());
            insurancePayment.setTranStatus(PENDING);
            insurancePayment.setControlState(ControlStateEnum.WAITING);
        if (flexibleProcessResp.getSale() != null) {
            insurancePayment.setIcCode(flexibleProcessResp.getSale().getCode());
            insurancePayment.setIcName(flexibleProcessResp.getSale().getName());
        }
        if (flexibleProcessResp.getRefererInput() != null) {
            insurancePayment.setRmName(flexibleProcessResp.getRefererInput().getName());
            insurancePayment.setRmCode(flexibleProcessResp.getRefererInput().getCode());
            insurancePayment.setRmPhoneNumber(flexibleProcessResp.getRefererInput().getPhoneNumber());
            insurancePayment.setRmEmail(flexibleProcessResp.getRefererInput().getEmail());
            insurancePayment.setBranchCode(flexibleProcessResp.getRefererInput().getBranchCode());
            insurancePayment.setBranchName(flexibleProcessResp.getRefererInput().getBranchName());
            insurancePayment.setDepartmentCode(flexibleProcessResp.getRefererInput().getDepartmentCode());
            insurancePayment.setDepartmentName(flexibleProcessResp.getRefererInput().getDepartmentName());
        }
        if (flexibleProcessResp.getSupporter() != null) {
            insurancePayment.setSupportCode(flexibleProcessResp.getSupporter().getCode());
            insurancePayment.setSupportName(flexibleProcessResp.getSupporter().getName());
            insurancePayment.setSupportPhoneNumber(flexibleProcessResp.getSupporter().getPhoneNumber());
            insurancePayment.setSupportEmail(flexibleProcessResp.getSupporter().getEmail());
        }
            insurancePayment.setInsuranceRequest(insuranceRequest);
            insurancePayment.setNormal(true);
            insurancePayment.setInstallment(false);
            insurancePayment.setMbalAppNo(flexibleProcessResp.getApplicationNumber());

            // Installment
            String installmentCache = commonService.getProcessCacheDataNotThrow(installmentCifProcessIdKey(cif, String.valueOf(processId)));
            if (reqDto.isInstallment() && installmentCache != null) {
                GetInstFeeResponse insFee = gson.fromJson(installmentCache, GetInstFeeResponse.class);
                insurancePayment.setPeriod(insFee.getData().getPeriod().replace(".0", ""));
                insurancePayment.setPeriodicConversionFee(insFee.getData().getPeriodicConversionFee());
                insurancePayment.setFeesPayable(insFee.getData().getFeesPayable());
                insurancePayment.setNormal(false);
                insurancePayment.setInstallment(true);
            }

            insurancePayment.setAutoPay(reqDto.isAutoPay());

        Map<String, String> flexibleMetadata = genFlexibleMetadata(cif, processId);
        MbTransactionReqDto mbTransactionReq = new MbTransactionReqDto()
                .setSessionId(reqDto.getSessionId())
                .setDescription(reqDto.getDescription())
                .setCif(cif)
                .setMetadata(gson.toJson(flexibleMetadata))
                .setType(transactionType)
                .setSuccessMessage(MSG23)
                .setAmount(totalAmount);
        log.info("[MINI]-Flexible create transaction request {}", mbTransactionReq);

//        TODO: Test local
//        insurancePayment.setTransactionId(generateTransactionId());
//        insurancePaymentRepository.save(insurancePayment);
//        return null;

        HttpEntity<MbTransactionReqDto> entity = new HttpEntity<>(mbTransactionReq, generateHeader());
        try {
            MbTransactionRespDto respDto = restCallApi(mbHost + MB_START_TRANSACTION, entity, MbTransactionRespDto.class, mbTransactionReq);
            if (respDto != null) {
                insurancePayment.setTransactionId(respDto.getId());
                insurancePaymentRepository.save(insurancePayment);
            }

            return respDto;
        } catch (Exception e) {
            log.error("[MB]--Khởi tạo giao dịch flexible thất bại! {}", e.getMessage());
            throw new MBApiException(MSG12);
        }
    }

    private Map<String, String> genFlexibleMetadata (String cif, Integer processId){
        Map<String, String> flexibleMetadataMap = new HashMap<>();
        flexibleMetadataMap.put(CIF, cif);
        flexibleMetadataMap.put(PROCESS_ID_KEY, String.valueOf(processId));
        flexibleMetadataMap.put(METADATA_FLEX_KEY, fxTransactionMetadataKey(cif, String.valueOf(processId)));
        flexibleMetadataMap.put(TRANSACTION_TYPE_KEY, TRANS_FLEXIBLE_TYPE);

        return flexibleMetadataMap;
    }

        @Override
        public GetInstFeeResponse getInsFee(String cif, GetInstFeeRequest reqDto) {
            verifyCustomerPermission(cif, reqDto.getProcessId());
            if (COMBO.equals(reqDto.getType())) {
                String amountCache = commonService.getProcessCacheDataNotThrow(processAmountIdKey(cif, reqDto.getProcessId()));
                if (amountCache == null) {
                    log.error("[MINI]--Tổng giá trị hợp đồng bị thay đổi.");
                    throw new MiniApiException(MSG12);
                }
                Map<String, String> amountCacheMap = revertMetadata(amountCache);

            if (amountCacheMap == null) {
                log.error("[MINI]--Giá trị hợp đồng không tồn tại, không thể thực hiện chuyển đổi trả góp. Input data {}", amountCacheMap);
                throw new MiniApiException(MSG12);
            }

                Long totalAmountCache = Long.valueOf(amountCacheMap.get(TOTAL_AMOUNT));
                if (!String.valueOf(totalAmountCache).equals(reqDto.getValueTransaction())) {
                    log.error("[MINI]--Giá trị giao dịch hợp đồng không chính xác. TotalAmountCache {}. Input data {}", totalAmountCache, reqDto);
                    throw new MiniApiException(MSG12);
                }
                GetInstFeeResponse insFee = cardInstallmentService.getInsFee(reqDto);
                insFee.setPeriodicConversionFeeStr(formatCurrency(BigDecimal.valueOf(totalAmountCache *
                        ((Double.parseDouble(insFee.getData().getPeriodicConversionFee())) / 100))));
                insFee.setTotalFee(amountCacheMap.get(TOTAL_AMOUNT));
                getCardList(cif, insFee, reqDto.getPeriod(), totalAmountCache);
                commonService.setProcessCacheData(installmentCifProcessIdKey(cif, reqDto.getProcessId()), gson.toJson(insFee));
                return insFee;
            } else {
                String flexibleProcessCacheData = commonService.getProcessCacheData(processCifCreateQuoteIdKey(cif, reqDto.getProcessId()));
                if (flexibleProcessCacheData == null) {
                    log.error("[MINI]--Không tồn tại dữ liệu quotation response cho processId {}", reqDto.getProcessId());
                    throw new MiniApiException(MSG12);
                }

                FlexibleQuoteRespDto flexibleProcessResp = gson.fromJson(flexibleProcessCacheData, FlexibleQuoteRespDto.class);
                BigDecimal totalAmount = flexibleProcessResp.getTotalAmount();
                if (!String.valueOf(totalAmount.longValue()).equals(reqDto.getValueTransaction())) {
                    log.error("[MINI]--Giá trị giao dịch hợp đồng không chính xác. totalAmount {}. Input data {}", totalAmount, reqDto);
                    throw new MiniApiException(MSG12);
                }
                GetInstFeeResponse insFee = cardInstallmentService.getInsFee(reqDto);
                insFee.setPeriodicConversionFeeStr(formatCurrency(BigDecimal.valueOf(Long.parseLong(reqDto.getValueTransaction()) * ((Double.parseDouble(insFee.getData().getPeriodicConversionFee())) / 100))));
                insFee.setTotalFee(String.valueOf(totalAmount.longValue()));
                getCardList(cif, insFee, reqDto.getPeriod(), Long.parseLong(reqDto.getValueTransaction()));
                commonService.setProcessCacheData(installmentCifProcessIdKey(cif, reqDto.getProcessId()), gson.toJson(insFee));
                return insFee;
            }
        }

        @Override
        public InstallmentPaymentPopupDTO retrieveInstallmentShowPopup(String mbId) {
            InsurancePayment installmentShowPopup = insurancePaymentRepository.retrieveInstallmentShowPopup(mbId);
            if (installmentShowPopup != null) {
                InsuranceContract insuranceContract = insuranceContractRepository.findByTransactionId(installmentShowPopup.getTransactionId());
                if (INSTALLMENT_UNQUALIFIED_CANCEL.getEnumEn().equals(installmentShowPopup.getInstallmentStatus())) {
                    return new InstallmentPaymentPopupDTO()
                            .setId(insuranceContract.getId())
                            .setInstallmentPopup(SHOW)
                            .setInstallmentErrorCode(installmentShowPopup.getInstallmentErrorCode())
                            .setInstallmentStatus(installmentShowPopup.getInstallmentStatus())
                            .setMbalAppNo(insuranceContract.getMbalAppNo())
                            .setPaymentTime(installmentShowPopup.getPaymentTime())
                            .setMessageType(InstallmentMessageType.TG_01);
                } else if (INSTALLMENT_SUCCESS.getEnumEn().equals(installmentShowPopup.getInstallmentStatus())) {
                    return new InstallmentPaymentPopupDTO()
                            .setId(insuranceContract.getId())
                            .setInstallmentPopup(SHOW)
                            .setInstallmentErrorCode(installmentShowPopup.getInstallmentErrorCode())
                            .setInstallmentStatus(installmentShowPopup.getInstallmentStatus())
                            .setMbalAppNo(insuranceContract.getMbalAppNo())
                            .setPaymentTime(installmentShowPopup.getPaymentTime())
                            .setMessageType(InstallmentMessageType.TG_03);
                }
                return new InstallmentPaymentPopupDTO()
                        .setId(insuranceContract.getId())
                        .setInstallmentPopup(InstallmentPopup.SHOW)
                        .setInstallmentErrorCode(installmentShowPopup.getInstallmentErrorCode())
                        .setInstallmentStatus(installmentShowPopup.getInstallmentStatus())
                        .setMbalAppNo(insuranceContract.getMbalAppNo())
                        .setPaymentTime(installmentShowPopup.getPaymentTime())
                        .setMessageType(InstallmentMessageType.TG_02);
            }
            return null;
        }

    @Override
    public void confirmInstallmentShowPopup (String mbId, Long contractId){
        InsurancePayment installmentShowPopup = insurancePaymentRepository.getInstalmentPayment(contractId, mbId);

        if (installmentShowPopup == null) {
            throw new InsurancePackageNotFoundException("Không tồn tại giao dịch đăng ký trả góp. Bạn không thể xác nhận.");
        }
        installmentShowPopup.setInstallmentPopup(InstallmentPopup.CONFIRMED);
        insurancePaymentRepository.save(installmentShowPopup);
        log.info("Customer confirmed for contract {}", contractId);
    }

    @Scheduled(cron = "${cron.daily_scan_transaction}")
    @SchedulerLock(name = "checkPendingTransaction")
    public void scheduleCheckPendingTransaction() {
        LocalDate localDate = LocalDate.now().minusDays(numDayBefore);
        String startOfDay = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, localDate.atTime(LocalTime.MIN));
        String endOfDay = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, LocalDate.now().atTime(LocalTime.MIN));
        List<InsurancePayment> insurancePayments = insurancePaymentRepository.retrievedInsurancePaymentPendingInDay(startOfDay, endOfDay);
        log.info("[CHECK-PENDING_TRANS]--Total transaction pending at {}. Num {}", localDate, insurancePayments.size());

        List<InsurancePayment> paymentsWaiting = new ArrayList<>();
        for (InsurancePayment insurancePayment : insurancePayments) {
            String paymentTransId = insurancePayment.getTransactionId();

            paymentLockService.lockMbCallbackTrans(paymentTransId, () -> {
                try {
                    MbCallBackTransactionReqDto mbTransactionResp = getTransaction(paymentTransId);
                    log.info("[CHECK-PENDING_TRANS]--getTransaction({}), response={}", paymentTransId, gson.toJson(mbTransactionResp));
                    if (mbTransactionResp != null && PAID.equals(mbTransactionResp.getStatus())) {
                        String callbackResponse = gson.toJson(mbTransactionResp);
                        insurancePayment.setTranStatus(WAITING);
                        insurancePayment.setMbCallback(callbackResponse);
                        insurancePaymentRepository.saveAndFlush(insurancePayment);
                        paymentsWaiting.add(insurancePayment);
                    }
                } catch (Exception ex) {
                    log.error("[CHECK-PENDING_TRANS][ERROR] transactionId=" + paymentTransId, ex);
                }
            });
        }
        log.info("[CHECK-PENDING_TRANS]--Total transaction waiting at {}. Num {}", localDate, paymentsWaiting.size());
    }

        private void updateStatus (InsuranceContractDto contractDto){
            try {
                contractDto.setCheckMicIssueAfterNow(DateUtil.checkStatus(DATE_DMY, contractDto.getMicIssueDate()));
            } catch (ParseException e) {
                contractDto.setCheckMicIssueAfterNow(false);
                log.error("[MINI]--update status error {}", e.getMessage());
            }
        }

        private PackageTypeRespDtos generatePackageTypeRespDtos (PackageTypeRespDtos
        packageTypeRespDtos, MiniPackageReqDto reqDto){
            PackageTypeRespDtos respDtos = new PackageTypeRespDtos();
            List<PackageTypeData> packages = packageTypeRespDtos.getPackages();
            List<PackageTypeData> mixPackages = new ArrayList<>();
            String name;
            MicInsuranceResultRespDto micInsuranceResultRespDto;
            MiniMicFeeReqDto micFeeReq;
            MicInsuranceBenefitDto micInsuranceBenefit;
        List<MicPackage> micPackages = micPackageService.findAll();
            for (PackageTypeData data : packages) {
                if (data.getInsuranceFee().compareTo(SIX_MILIONS) == 0) {
                    // Tu tin
                    name = CONFIDENT.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitDto.class); // lấy mic trong db mini app
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, CONFIDENT);

                    mixPackages.add(mixData);
                } else if (data.getInsuranceFee().compareTo(TEN_MILIONS) == 0) {
                    // An nhien
                    name = PackageNameEnum.EQUANIMITY.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitDto.class);
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, PackageNameEnum.EQUANIMITY);

                    mixPackages.add(mixData);
                } else if (data.getInsuranceFee().compareTo(FIFTEEN_MILIONS) == 0) {
                    // Binh An
                    name = PackageNameEnum.PEACEFUL.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitDto.class);
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, PackageNameEnum.PEACEFUL);

                    mixPackages.add(mixData);
                } else if (data.getInsuranceFee().compareTo(TWENTY_MILIONS) == 0) {
                    // Vui khoe
                    name = PackageNameEnum.HEALTHY.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 1);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(0), MicInsuranceBenefitDto.class);
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, PackageNameEnum.HEALTHY);

                    mixPackages.add(mixData);
                } else if (data.getInsuranceFee().compareTo(THIRTY_MILIONS) == 0 && data.getType().equals(INVERT_TYPE)) {
                    // Hanh phuc
                    name = PackageNameEnum.HAPPY.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 2);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(1), MicInsuranceBenefitDto.class);
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, PackageNameEnum.HAPPY);

                    mixPackages.add(mixData);
                } else if (data.getInsuranceFee().compareTo(FIFTY_MILIONS) == 0) {
                    // Tan huong
                    name = PackageNameEnum.ENJOY.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 3);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(2), MicInsuranceBenefitDto.class);
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, PackageNameEnum.ENJOY);

                    mixPackages.add(mixData);
                } else if (data.getInsuranceFee().compareTo(ONE_HUNDERED_MILIONS) == 0) {
                    // May man
                    name = PackageNameEnum.LUCKY.getVal();
                    micFeeReq = micAPIService.genMicFeeReqDto(reqDto.getDob(), 4);
                    micInsuranceResultRespDto = micAPIService.micFeeResult(micFeeReq);
                    micInsuranceBenefit = modelMapper.map(micPackages.get(3), MicInsuranceBenefitDto.class);
                    PackageTypeData mixData = generatePackageTypeData(data, micInsuranceResultRespDto, name, micInsuranceBenefit);
                    mixData.setMicInsuranceBenefit(micInsuranceBenefit);

                    mixData = setPackagePhoto(mixData, PackageNameEnum.LUCKY);

                    mixPackages.add(mixData);
                }
            }
            return respDtos.setPackages(mixPackages);
        }

        private PackageTypeData generatePackageTypeData (PackageTypeData data, MicInsuranceResultRespDto
        micInsuranceResultRespDto,
                String packageName, MicInsuranceBenefitDto micInsuranceBenefitDto){
            if (micInsuranceResultRespDto != null) {
                updatePackageTypeData(data, micInsuranceResultRespDto.getPhi(), micInsuranceBenefitDto);
            }
            data.setMixPackageName(packageName);
            return data;
        }

        private void updatePackageTypeData (PackageTypeData data, BigDecimal phi, MicInsuranceBenefitDto
        micInsuranceBenefitDto){
            BigDecimal totalInsuranceFee = (data.getInsuranceFee().add(phi));
            data.setMixInsuranceFee(totalInsuranceFee);
            data.setMicInsuranceFee(phi);
            data.setStrMixInsuranceFee(formatCurrency(totalInsuranceFee));
            Long totalBenefit = convertToLong(data.getDeathBenefit())
                    + convertToLong(micInsuranceBenefitDto.getMainOne())
                    + convertToLong(micInsuranceBenefitDto.getMainTwo())
                    + convertToLong(micInsuranceBenefitDto.getMainThree());
            data.setTotalInsuranceBenefit(BigDecimal.valueOf(totalBenefit));
            data.setStrTotalInsuranceBenefit(convertToMoneyFormat(String.valueOf(totalBenefit)));
        }

        private String convertToVnd (String strMoney){
            BigDecimal moneyValue;
            if (strMoney.contains("triệu")) {
                moneyValue = BigDecimal.valueOf(convertToDouble(strMoney) * 1000000);
            } else if (strMoney.contains("nghìn")) {
                moneyValue = BigDecimal.valueOf(convertToDouble(strMoney) * 1000);
            } else {
                moneyValue = BigDecimal.valueOf(convertToDouble(strMoney) * 1000000000);
            }
            return formatCurrency(moneyValue);
        }

        private HttpHeaders generateHeader () {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(APPLICATION_JSON);
            headers.set(MERCHANT_CODE_HEADER, mbMerchantCode);
            headers.set(MERCHANT_SECRET_HEADER, mbMerchantSecret);
            return headers;
        }

        private boolean validateMBChecksumCallBackTransaction (String mbChecksum, String transactionId, String
        typeCode, String cif, Long amount, String status){
            String data = mbMerchantCode + transactionId + typeCode + cif + amount + status;
            byte[] hmac = new HmacUtils(HMAC_SHA_256, mbCallbackChecksumSecret).hmac(data);
            String encodedString = Base64.getEncoder().encodeToString(hmac);
            return encodedString.equals(mbChecksum);
        }

        private String generateUllV3Metadata (Long micAmount, Long mbalAmount, String sessionId, String
        mbalPolicyNumber,
                String micGcnbh, String mbalAppNo, Long totalAmount, String ulVersion, String cif,
                Integer micId, Integer mbalId, Integer packageId, String mixPackageName, Boolean isInstallment){
            Map<String, String> map = genDefaultMetadata(micAmount, mbalAmount, sessionId, mbalPolicyNumber, micGcnbh,
                    mbalAppNo, totalAmount, ulVersion, cif, mixPackageName);
            map.put(MIC_ID, String.valueOf(micId));
            map.put(MBAL_ID, String.valueOf(mbalId));
            map.put(INSURANCE_PACKAGE_ID, String.valueOf(packageId));
            map.put(INSTALLMENT_KEY, isInstallment == null ? String.valueOf(false) : String.valueOf(isInstallment));
            return gson.toJson(map);
        }

        private Map<String, String> genDefaultMetadata (Long micAmount, Long mbalAmount, String sessionId, String
        mbalPolicyNumber, String micGcnbh,
                String mbalAppNo, Long totalAmount, String ulVersion, String cif, String mixPackageName){
            Map<String, String> map = new HashMap<>();
            map.put(MIC_AMOUNT, String.valueOf(micAmount));
            map.put(MBAL_AMOUNT, String.valueOf(mbalAmount));
            map.put(MB_SESSION_ID, sessionId);
            map.put(MBAL_HDBH, mbalPolicyNumber);
            map.put(MBAL_APP_NO, mbalAppNo);
            map.put(MIC_GCNBH, micGcnbh);
            map.put(TOTAL_AMOUNT, String.valueOf(totalAmount));
            map.put(ULL_VERSION, ulVersion);
            map.put(CIF, cif);
            map.put(MIX_PACKAGE_NAME, mixPackageName);
            return map;
        }

        private String generateUllV2Metadata (Long micAmount, Long mbalAmount, String sessionId, String
        mbalPolicyNumber,
                String micGcnbh, String mbalAppNo, Long totalAmount, String ulVersion, String cif, String createOrderId,
                Integer micId, Integer mbalId, Integer packageId, String mixPackageName, String hookTypeCode){
            Map<String, String> map = genDefaultMetadata(micAmount, mbalAmount, sessionId, mbalPolicyNumber, micGcnbh,
                    mbalAppNo, totalAmount, ulVersion, cif, mixPackageName);
            map.put(CREATE_ORDER_ID, createOrderId);
            map.put(MIC_ID, String.valueOf(micId));
            map.put(MBAL_ID, String.valueOf(mbalId));
            map.put(INSURANCE_PACKAGE_ID, String.valueOf(packageId));
            map.put(HOOK_TYPE_COE, hookTypeCode);
            return gson.toJson(map);
        }

        private Map<String, String> revertMetadata (String metadata){
            try {
                return objectMapper.readValue(metadata, Map.class);
            } catch (JsonProcessingException e) {
                log.error("[Mini]--Exception when revert metadata");
                throw new ApplicationException(MSG12);
            }
        }

        private void refactorMbalPackage (PackageTypeRespDtos packageTypeRespDtos){
            List<PackageTypeData> packages = packageTypeRespDtos.getPackages();
            for (PackageTypeData data : packages) {
                if (data.getType().equals(PROTECT_TYPE)) {
                    if (data.getInsuranceFee().compareTo(SIX_MILIONS) == 0) {
                        data.setId("1");
                        setPackagePhoto(data, CONFIDENT);
                        continue;
                    } else if (data.getInsuranceFee().compareTo(TEN_MILIONS) == 0) {
                        data.setId("2");
                        setPackagePhoto(data, PackageNameEnum.EQUANIMITY);
                        continue;
                    } else if (data.getInsuranceFee().compareTo(FIFTEEN_MILIONS) == 0) {
                        data.setId("3");
                        setPackagePhoto(data, PackageNameEnum.PEACEFUL);
                        continue;
                    } else if (data.getInsuranceFee().compareTo(TWENTY_MILIONS) == 0) {
                        data.setId("4");
                        setPackagePhoto(data, PackageNameEnum.HEALTHY);
                        continue;
                    }
                    data.setId(null);
                } else {
                    if (data.getInsuranceFee().compareTo(THIRTY_MILIONS) == 0) {
                        data.setId("5");
                        setPackagePhoto(data, PackageNameEnum.HAPPY);
                        continue;
                    } else if (data.getInsuranceFee().compareTo(FIFTY_MILIONS) == 0) {
                        data.setId("6");
                        setPackagePhoto(data, PackageNameEnum.ENJOY);
                        continue;
                    }
                    data.setId("7");
                    setPackagePhoto(data, PackageNameEnum.LUCKY);

                }
                packageTypeRespDtos.setPackages(packages.stream().filter(o -> o.getId() != null).collect(Collectors.toList()));
            }
        }

        private PackageTypeData setPackagePhoto (PackageTypeData data, PackageNameEnum packageNameEnum){
            PackagePhoto byType = packagePhotoRepository.findByType(packageNameEnum);
            if (byType != null) {
                data.setPhoto(byType.getImage());
                data.setBackgroundImage(byType.getImage());
            }
            return data;
        }

        private <T > T getResponseForPostRequest(String host, HttpEntity < ? > entity, Class < T > clazz, String
        payloadLog, Constants.HOST_PARTY hostParty){
            LocalDateTime sendTime = LocalDateTime.now();
            try {
                ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
                return clazz.cast(exchange.getBody());
            } catch (Exception e) {
                LocalDateTime receivedTime = LocalDateTime.now();
                asyncObject.saveErrorLog(hostParty, HttpMethod.POST, host, payloadLog, 500, e.getMessage(), sendTime, receivedTime);
                if (host.contains(mbHost)) {
                    log.error("[MB]--Call to API error {}, exception: {}", host, e.getMessage());
                    throw new MBApiException(MSG12);
                } else if (host.contains(mbalHost)) {
                    log.error("[MBAL]--Call to API error {}, exception: {}", host, e.getMessage());
                    throw new MbalApiException(MSG12);
                }
                log.error("[MIC]--Call to API error {}, exception: {}", host, e.getMessage());
                throw new MicApiException(MSG12);
            }
        }

        private <T > T restCallApi(String host, HttpEntity < ? > entity, Class < T > clazz, Object reqDto){
            LocalDateTime sendTime = LocalDateTime.now();
            try {
                ResponseEntity<?> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
                return (T) exchange.getBody();
            } catch (Exception e) {
                log.error("[MBAL]--Failed to call host {}, detail {}", host, e.getMessage());
                asyncObject.saveErrorLog(host, HttpMethod.POST, reqDto, e.getMessage(), Constants.HOST_PARTY.MB, sendTime, LocalDateTime.now());
                throw new MbalApiException(MSG12);
            }
        }

        @SuppressWarnings("unused")
        private OauthToken generateMbalAccessToken (String masterSesstionToken){
            // call to MB to create merchant token
            MbCreateMerchantTokenReqDto tokenReqDto = new MbCreateMerchantTokenReqDto().setMasterSessionToken(masterSesstionToken).setMerchantCode(mbalMerchantCode);
            HttpEntity<?> entity = new HttpEntity<>(tokenReqDto, generateHeader());
            String payloadLog = getPayloadLogObject(tokenReqDto);
            MbMasterSessionToken mbMasterSessionToken = getResponseForPostRequest(mbHost + MB_CREATE_MERCHANT_TOKEN, entity, MbMasterSessionToken.class, payloadLog, Constants.HOST_PARTY.MB);
            //call to MBAL to get accessToken
            MbalTokenReqDto reqDto = new MbalTokenReqDto().setToken(mbMasterSessionToken.getToken());
            HttpEntity<?> mbalEntity = new HttpEntity<>(reqDto, generateDefaultHeader());
            payloadLog = getPayloadLogObject(tokenReqDto);
            return getResponseForPostRequest(mbalHost + MBAL_GENERATE_ACCESS_TOKEN, mbalEntity, OauthToken.class, payloadLog, Constants.HOST_PARTY.MBAL);
        }

    private InsuranceContract genFlexibleContract(Customer customer, Integer insurancePackageId,
                                                  String mbReferenceNumber, InsurancePayment insurancePayment) {
            PrimaryProduct primaryProduct = primaryProductRepository.findByInsuranceRequestId(insurancePayment.getInsuranceRequest().getId());
            InsuranceContract insuranceContract = new InsuranceContract();
            InsurancePackage insurancePackage = insurancePackageRepository.findById(insurancePackageId)
                    .orElseThrow(() -> new InsurancePackageNotFoundException("Không tồn tại gói bảo hiểm."));
            insuranceContract.setInsurancePackage(insurancePackage);
            insuranceContract.setCustomer(customer);
            insuranceContract.setStrInsuranceFee(insurancePayment.getTotalFee());
            insuranceContract.setMbalAppNo(insurancePayment.getMbalAppNo());
            insuranceContract.setMbalPeriodicFeePaymentTime(EVERY_YEAR);
            insuranceContract.setMbalAmount(insurancePayment.getMbalInsuranceFee());
            insuranceContract.setMbReferenceNumber(mbReferenceNumber);
            insuranceContract.setMbalFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
            insuranceContract.setPackageType(PackageType.FLEXIBLE);
            insuranceContract.setMbalPolicyNumber(null);
            insuranceContract.setMbalProductName(MBAL_PRODUCT_NAME_V3);
            return insuranceContract;
        }

    private InsuranceContract genContract(Customer customer, String mixPackageName, Integer insurancePackageId, Integer micPackageId, Integer mbalPackageId,
                                          String micGcnbh, String mbalAppNo, String totalAmount, String mbalAmount, String micAmount) {
        // Create Insurance Contract data
        InsuranceContract insuranceContract = new InsuranceContract();
        InsurancePackage insurancePackage = insurancePackageRepository.findById(insurancePackageId)
                .orElseThrow(() -> new InsurancePackageNotFoundException("Không tồn tại gói bảo hiểm."));

            insuranceContract.setInsurancePackage(insurancePackage);
            if (!mixPackageName.equals(UR_STYLE.getVal())) {
                insuranceContract.setPackageType(Constants.PackageType.FIX_COMBO);
            } else {
                MbalPackage mbalPackage = mbalPackageRepository.findById(mbalPackageId)
                        .orElseThrow(() -> new MbalPackageNotFoundException("Không tồn tại gói bảo hiểm MBAL."));
                insuranceContract.setMbalPackage(mbalPackage);
                MicPackage micPackage = micPackageRepository.findById(micPackageId)
                        .orElseThrow(() -> new MicPackageNotFoundException("Không tồn tại gói bảo hiểm MIC."));
                insuranceContract.setMicPackage(micPackage);
                insuranceContract.setPackageType(Constants.PackageType.FREE_STYLE);
            }
            insuranceContract.setCustomer(customer);
            insuranceContract.setLogo(getLogo(mixPackageName));
            insuranceContract.setStrInsuranceFee(totalAmount);
            insuranceContract.setMicContractNum(micGcnbh);
            insuranceContract.setMbalAppNo(mbalAppNo);
            insuranceContract.setMicFeePaymentTime(ONE_YEAR);
            insuranceContract.setMicPeriodicFee(ONE_YEAR);
//        insuranceContract.setMbalFeePaymentTime(insuranceContract.getMbalFeePaymentTime());
            insuranceContract.setMbalPeriodicFeePaymentTime(EVERY_YEAR);
            insuranceContract.setMicAmount(micAmount);
            insuranceContract.setMbalAmount(mbalAmount);
            return insuranceContract;
        }

        private String getLogo (String mixPackageName){
            // convert
            PackageNameEnum packageNameEnum = getPackageNumFromVal(mixPackageName);
            PackagePhoto byType = packagePhotoRepository.findByType(packageNameEnum);
            if (byType != null) {
                return byType.getImage();
            }
            return "";
        }

        private String getPayloadLogMbalSendEmail (MbalSendEmailReqDto reqDto){
            try {
                ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
                Gson gson = new Gson();
                MbalSendEmailReqDto dto = gson.fromJson(gson.toJson(reqDto), MbalSendEmailReqDto.class);
                dto.setMail(null);
                return ow.writeValueAsString(dto);
            } catch (JsonProcessingException jsonProcessingException) {
                log.error(MINI_ERROR_CONVERT_JSON, reqDto);
                return null;
            }
        }

        private String getPayloadLogCheckCustomerInfo (MbalCustomerInfoReqDto reqDto){
            try {
                ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
                Gson gson = new Gson();
                MbalCustomerInfoReqDto dto = gson.fromJson(gson.toJson(reqDto), MbalCustomerInfoReqDto.class);
                dto.setDob(null)
                        .setGender(null)
                        .setPhone(null)
                        .setEmail(null)
                        .setAddress(null)
                        .setFullname(null);
                return ow.writeValueAsString(dto);
            } catch (JsonProcessingException jsonProcessingException) {
                log.error(MINI_ERROR_CONVERT_JSON, reqDto);
                return null;
            }
        }

        private String getPayloadLogMbalCreateOrder (MbalCreateOrderReqDto reqDto){
            try {
                ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
                Gson gson = new Gson();
                MbalCreateOrderReqDto dto = gson.fromJson(gson.toJson(reqDto), MbalCreateOrderReqDto.class);
                dto.setDob(null)
                        .setGender(null)
                        .setEmail(null)
                        .setPhoneRefer(null);
                return ow.writeValueAsString(dto);
            } catch (JsonProcessingException jsonProcessingException) {
                log.error(MINI_ERROR_CONVERT_JSON, reqDto);
                return null;
            }
        }

        private String getPayloadLogObject (Object reqDto){
            try {
                ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
                return ow.writeValueAsString(reqDto);
            } catch (JsonProcessingException jsonProcessingException) {
                log.error(MINI_ERROR_CONVERT_JSON, reqDto);
                return null;
            }
        }

    private void verifyCustomerPermission(String cif, String processId) {
        if (!processId.equals(redis.get(processIdKey(cif)))) {
            throw new MiniApiException(MSG13);
        }
    }

    private void getCardList(String cif, GetInstFeeResponse insFee, String period, long valueTransaction) {
        Customer customer = customerRepository.findByMbId(cif);
        GetCardListRequest cardListRequest = new GetCardListRequest()
                .setRequestType("1")
                .setRequestNumber(customer.getMbId())
                .setPhoneNumber(customer.getPhone())
                .setCustomerName(customer.getFullNameT24());
        //installmentHost + "/private/ms/card-partner/new/v1.0/get-card-list";
        Map<String, String> params = new HashMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        String listCardCache = commonService.getProcessCacheDataNotThrow(listCardCifProcessIdKey(customer.getMbId(), insFee.getProcessId()));
        if (listCardCache != null) {
            // get from cache
            ListCardResponse cardResponse = gson.fromJson(listCardCache, ListCardResponse.class);
            if(cardResponse != null) {
                sortingListCard(insFee, period, valueTransaction, cardResponse);
                insFee.setCardErrorCode(cardResponse.getErrorCode());
            }
            return;
        }
        CommonAdapterResponse commonAdapterResponse = baasService.getListCard(baasListCardUrl, params, HttpMethod.POST, httpHeaders, cardListRequest);

        List<ListCardResponse.CardResp> dataResp = objectMapper.convertValue(commonAdapterResponse.getData(), new TypeReference<>() {
        });
        ListCardResponse listCardResponse = modelMapper.map(commonAdapterResponse, ListCardResponse.class);
        listCardResponse.setData(dataResp == null ? null : dataResp.get(0));
        listCardResponse.setErrorCode(commonAdapterResponse.getErrorCode());
        if (BAAS_SUCCESS_CODE.equals(listCardResponse.getErrorCode())) {
            if (listCardResponse.getData() == null) {
                listCardResponse.setCardResps(null);
                commonService.setProcessCacheData(listCardCifProcessIdKey(customer.getMbId(), insFee.getProcessId()), gson.toJson(listCardResponse));
            } else {
                List<ListCardResponse.DataCardResp> dataCardResps = listCardResponse.getData().getData();
//                List<ListCardResponse.DataCardResp> cardFilterResps = dataCardResps.stream()
//                        .filter(dataCardResp -> Double.parseDouble(dataCardResp.getAmountAvailable()) >= Long.parseLong(insFee.getTotalFee()))
//                        .map(o -> o.setAmountAvailable(null)
//                                .setCreditLimit(null)
//                                .setTotalBlocked(null))
//                        .collect(Collectors.toList());
                listCardResponse.setCardResps(dataCardResps);
                commonService.setProcessCacheData(listCardCifProcessIdKey(customer.getMbId(), insFee.getProcessId()), gson.toJson(listCardResponse));
                sortingListCard(insFee, period, valueTransaction, listCardResponse);
            }
        }
    }

    private void sortingListCard(GetInstFeeResponse insFee, String period, long valueTransaction, ListCardResponse listCardResponse) {
        List<ListCardResponse.DataCardResp> dataCardResps = listCardResponse.getCardResps();
        if (dataCardResps.isEmpty()) {
            insFee.setCardResps(null);
            return;
        }

        Collections.sort(dataCardResps, (a, b) -> {
            if ((Double.parseDouble(a.getAmountAvailable()) >= valueTransaction && DateUtil.checkCardExpired(a.getCardDateExpire(), period))
                    && (Double.parseDouble(b.getAmountAvailable()) < valueTransaction || !DateUtil.checkCardExpired(b.getCardDateExpire(), period))) {
                return -1;
            } else if ((Double.parseDouble(a.getAmountAvailable()) < valueTransaction || !DateUtil.checkCardExpired(a.getCardDateExpire(), period))
                    && (Double.parseDouble(b.getAmountAvailable()) >= valueTransaction && DateUtil.checkCardExpired(b.getCardDateExpire(), period))) {
                return 1;
            } else if (Double.parseDouble(a.getCreditLimit()) - Double.parseDouble(b.getCreditLimit()) != 0) {
                return Double.parseDouble(a.getCreditLimit()) - Double.parseDouble(b.getCreditLimit()) > 0 ? -1 : 1;
            } else {
                if (compareTime(a.getCardDateExpire(), b.getCardDateExpire()))
                    return 1;
                else return -1;
            }
        });

        for (ListCardResponse.DataCardResp card : dataCardResps) {
            if (Double.parseDouble(card.getAmountAvailable()) < valueTransaction) {
                card.setLabel(LIMIT_NOT_ENOUGH);
                card.setAmountAvailable(null);
                card.setCreditLimit(null);
                card.setTotalBlocked(null);
                continue;
            }
            if (!DateUtil.checkCardExpired(card.getCardDateExpire(), period)) {
                card.setLabel(CARD_EXPIRED);
                card.setAmountAvailable(null);
                card.setCreditLimit(null);
                card.setTotalBlocked(null);
                continue;
            }
            card.setLabel(PASS);
            card.setAmountAvailable(null);
            card.setCreditLimit(null);
            card.setTotalBlocked(null);
        }
        insFee.setCardResps(dataCardResps);
    }

}

