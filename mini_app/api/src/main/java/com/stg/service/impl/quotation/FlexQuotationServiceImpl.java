package com.stg.service.impl.quotation;

import com.google.gson.Gson;
import com.stg.common.Jackson;
import com.stg.constant.Gender;
import com.stg.constant.quotation.QuotationState;
import com.stg.entity.insurance.MicPackage;
import com.stg.entity.potentialcustomer.DirectState;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.entity.quotation.QuotationSupporter;
import com.stg.errors.ApplicationException;
import com.stg.errors.MicApiException;
import com.stg.errors.ValidationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.errors.mbal.MicPackageNotFoundException;
import com.stg.errors.quote.ConfirmQuoteException;
import com.stg.errors.quote.CreateProcessException;
import com.stg.errors.quote.CreateQuoteException;
import com.stg.errors.quote.CustomerPermissionException;
import com.stg.repository.PotentialCustomerDirectRepository;
import com.stg.repository.QuotationHeaderRepository;
import com.stg.service.FlexQuotationService;
import com.stg.service.MicPackageService;
import com.stg.service.PotentialCustomerDirectService;
import com.stg.service.caching.FlexibleQuotationCaching;
import com.stg.service.dto.mbal.ConfirmQuoteFlexibleReqDto;
import com.stg.service.dto.mbal.ProductType;
import com.stg.service.dto.mbal.QuotationModel;
import com.stg.service.dto.mic.MicCompareResp;
import com.stg.service.dto.quotation.*;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.constant.RelationshipPolicyHolderType;
import com.stg.service3rd.mbal.dto.*;
import com.stg.service3rd.mbal.dto.FlexibleCommon.Assured;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mbal.dto.req.FlexibleProcessReq;
import com.stg.service3rd.mbal.dto.req.FlexibleQuoteReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleRespModel;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import com.stg.service3rd.mic.MicApi3rdService;
import com.stg.service3rd.mic.dto.req.MicGenerateInsuranceCertReqDto;
import com.stg.service3rd.mic.dto.resp.MicGenerateInsuranceCertRespDto;
import com.stg.service3rd.mic.exception.MicApi3rdException;
import com.stg.utils.AppUtil;
import com.stg.utils.DateUtil;
import com.stg.utils.quotation.QuoteMapperUtil;
import com.stg.utils.quotation.QuoteUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.stg.common.Endpoints.QUOTATION.LINK_QRCODE_QUOTE_DETAIL;
import static com.stg.constant.Gender.MALE;
import static com.stg.service3rd.mbal.constant.AssuredType.ADDITIONAL_LIVES;
import static com.stg.service3rd.mbal.constant.AssuredType.LIFE_ASSURED;
import static com.stg.service3rd.mbal.constant.Common.formatCurrency;
import static com.stg.service3rd.mbal.constant.Common.generateUUIDId;
import static com.stg.constant.CommonMessageError.*;
import static com.stg.service3rd.mbal.constant.ProductType.COI_RIDER;
import static com.stg.service3rd.mbal.constant.ProductType.PWR;
import static com.stg.service3rd.mbal.constant.RelationshipPolicyHolderType.*;
import static com.stg.service3rd.mbal.dto.FlexibleCommon.*;
import static com.stg.service3rd.mic.utils.MicUtil.*;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;
import static com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq.PRODUCT_DEFAULT;
import static com.stg.utils.DateUtil.localDateTimeToString;
import static com.stg.utils.quotation.QuoteMapperUtil.micInsuranceContractToEntity;
import static com.stg.utils.quotation.QuoteMapperUtil.quotationHeaderToEntity;
import static com.stg.utils.quotation.QuoteMapperUtil.quotationModelToEntity;
import static com.stg.utils.quotation.QuoteUtil.*;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlexQuotationServiceImpl implements FlexQuotationService {
    private final ModelMapper modelMapper;
    private final Jackson jackson;
    private final Gson gson;

    private final QuotationHeaderRepository quotationHeaderRepository;
    private final FlexibleQuotationCaching flexibleQuotationCaching;
    private final MbalApi3rdService mbalApi3rdService;
    private final MicPackageService micPackageService;
    private final MicApi3rdService micApi3rdService;

    private final PotentialCustomerDirectRepository directRepository;

    private final PotentialCustomerDirectService potentialCustomerDirectService;

    @Override
    public FlexibleRespModel flexCreateProcess(FlexibleProcessReq reqDto) {
        String username = AppUtil.getCurrentUsername();

        List<OccupationResp> occupationList = mbalApi3rdService.getOccupations();
        Optional<OccupationResp> occupationResp = occupationList.stream().filter(o -> Objects.nonNull(o) && o.getId().longValue() == reqDto.getCustomer().getOccupationId()).findFirst();

        if (occupationResp.isPresent()) {
            OccupationResp occupation = occupationResp.get();
            if (occupation.getGroupId() != 1 && occupation.getGroupId() != 2 && occupation.getGroupId() != 3 && occupation.getGroupId() != 4) {
                throw new CreateProcessException(MSG02);
            }
        }

        if (reqDto.isCustomerIsAssured()) {
            FlexibleCommon.validateMainInsuranceAge(reqDto.getCustomer().getDob());
        }

        try {
            FlexibleRespModel response = mbalApi3rdService.flexCreateProcess(reqDto);
            if (response != null) {
                QuotationDto quotationData = modelMapper.map(response, QuotationDto.class);
                // caching
                quotationData.setAssured(reqDto.getAssured());
                quotationData.setCustomerIsAssured(reqDto.isCustomerIsAssured());
                flexibleQuotationCaching.setQuotationData(username, response.getProcessId(), quotationData);
                if (reqDto.getDirectId() != null) {
                    flexibleQuotationCaching.setDirectId(username, response.getProcessId(), reqDto.getDirectId());
                }
            }
            return response;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new CreateProcessException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }


    @Override
    @Transactional
    public QuotationDto flexCreateQuote(QuotationHeaderDto reqDto) {
        String username = AppUtil.getCurrentUsername();
        this.verifyCustomerPermission(username, reqDto.getProcessId());
        if (reqDto.getCustomer() == null || reqDto.getCustomer().getIdentificationId() == null) {
            throw new CreateQuoteException("", new ErrorDto(HttpStatus.BAD_REQUEST, "customer is required"));
        }
        // Get input from previous step for pentest
//        TODO:Comment: pentest
//        QuotationDto processCacheDto = flexibleQuotationCaching.getQuotationData(username, reqDto.getProcessId());
//        if (processCacheDto == null) {
//            log.error("[MBAL]--Create process lỗi với cache is null");
//            throw new ConfirmQuoteException(MSG13);
//        }
//        reqDto.setCustomerIsAssured(processCacheDto.isCustomerIsAssured());
//        QuotationAssuredDto dtoAssured = reqDto.getAssured();
//        reqDto.setAssured(dtoAssured);
//        reqDto.getAssured().setMicRequest(dtoAssured.getMicRequest());
//        reqDto.getAssured().setAdditionalProducts(dtoAssured.getAdditionalProducts());

        Assured customer = modelMapper.map(reqDto.getCustomer(), Assured.class);
        customer.setIsCustomer(true);
        QuotationAssuredDto mainAssuredDto = reqDto.isCustomerIsAssured() ? reqDto.getCustomer() : reqDto.getAssured();
        if (reqDto.isCustomerIsAssured()) {
            mainAssuredDto.setRelationshipWithPolicyHolder(POLICY_HOLDER);
        }
        mainAssuredDto.setType(LIFE_ASSURED.name());
        Assured mainAssured = modelMapper.map(mainAssuredDto, Assured.class);
//        if (mainAssuredDto.getMicRequest() == null && mainAssuredDto.getAdditionalProducts() == null) {
//            throw new CreateQuoteException("Bạn phải chọn ít nhất 1 gói BHBS MIC hoặc MBAL.");
//        }

        LocalDateTime createdDate = LocalDateTime.now();
        // GET ALL ASSURED (MBAL + MIC)
        //List<FlexibleCommon.Assured> allAssured = new ArrayList<>();
        List<QuotationAssuredDto> allAssuredDto = new ArrayList<>();
        LinkedHashMap<FlexibleCommon.Assured, QuoteAssuredOutput> allAssuredOutputMap = new LinkedHashMap<>();

        List<FlexibleCommon.Assured> mbalAssureds = new ArrayList<>();
        List<MbalAdditionalProductInput> mbalProducts = new ArrayList<>();

        /*// assured:main
        //allAssured.add(mainAssured);
        allAssuredDto.add(mainAssuredDto);
        allAssuredOutputMap.put(mainAssured, new QuoteAssuredOutput().setMicRequest(mainAssuredDto.getMicRequest()));
        validateMicRequest(mainAssured.getDob(), mainAssuredDto.getMicRequest());
        mbalAssureds.add(mainAssured);
        if (!CollectionUtils.isEmpty(mainAssuredDto.getAdditionalProducts())) {
            int currentIdx = mbalAssureds.size() - 1;
            mainAssuredDto.getAdditionalProducts().forEach(el -> {
                MbalAdditionalProductInput product = modelMapper.map(el, MbalAdditionalProductInput.class);
                product.setAssuredIndex(currentIdx);
                mbalProducts.add(product);
            });
        }*/

        // assured:customer is additionalAssured
        boolean flag = false;
        if (!reqDto.isCustomerIsAssured() && (!CollectionUtils.isEmpty(reqDto.getCustomer().getAdditionalProducts()) || reqDto.getCustomer().getMicRequest() != null)) {
            customer.setType(ADDITIONAL_LIVES);
            //allAssured.add(customer);
            customer.setRelationshipWithPolicyHolder(POLICY_HOLDER);
            allAssuredDto.add(reqDto.getCustomer());
            MicAdditionalProduct micRequestCustomer = reqDto.getCustomer().getMicRequest();
            validateMicRequest(reqDto.getCustomer().getDob().toString(), micRequestCustomer, reqDto.getCustomer().getGender());

            // setup flag for customer who buy MIC
            if(micRequestCustomer != null) {
                flag = true;
            }
            allAssuredOutputMap.put(customer, new QuoteAssuredOutput().setMicRequest(micRequestCustomer));

            if (!CollectionUtils.isEmpty(reqDto.getCustomer().getAdditionalProducts())) {
                mbalAssureds.add(customer);
                int currentIdx = mbalAssureds.size() - 1;
                reqDto.getCustomer().getAdditionalProducts().forEach(el -> {
                    MbalAdditionalProductInput product = modelMapper.map(el, MbalAdditionalProductInput.class);
                    product.setAssuredIndex(currentIdx);
                    mbalProducts.add(product);
                });
            }
        }

        // assured:main
        //allAssured.add(mainAssured);
        allAssuredDto.add(mainAssuredDto);
        allAssuredOutputMap.put(mainAssured, new QuoteAssuredOutput().setMicRequest(mainAssuredDto.getMicRequest()));
        validateMicRequest(mainAssured.getDob(), mainAssuredDto.getMicRequest(), mainAssured.getGender());
        mbalAssureds.add(mainAssured);
        int mainAges = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, mainAssured.getDob() + ZERO_TIME), LocalDateTime.now());
        //Set flag if customer is main
        if(mainAssuredDto.getMicRequest() != null
                && (POLICY_HOLDER.equals(mainAssured.getRelationshipWithPolicyHolder())
                || (COUPLE.equals(mainAssured.getRelationshipWithPolicyHolder()) && mainAges >= 18))
                || (CHILDREN.equals(mainAssured.getRelationshipWithPolicyHolder()) && mainAges < 6)) {
            flag = true;
        }
        if (!CollectionUtils.isEmpty(mainAssuredDto.getAdditionalProducts())) {
            int currentIdx = mbalAssureds.size() - 1;
            mainAssuredDto.getAdditionalProducts().forEach(el -> {
                MbalAdditionalProductInput product = modelMapper.map(el, MbalAdditionalProductInput.class);
                product.setAssuredIndex(currentIdx);
                mbalProducts.add(product);
            });
        }

        // assured:additionalAssureds
        if (reqDto.getAdditionalAssureds() != null) {
            // Sort assured input
            List<QuotationAssuredDto> additionalAssureds = reqDto.getAdditionalAssureds();
            for (QuotationAssuredDto assuredDto : additionalAssureds) {
                if (CollectionUtils.isEmpty(assuredDto.getAdditionalProducts()) && assuredDto.getMicRequest() == null) {
                    throw new CreateQuoteException("Người hưởng BHBS phải có ít nhất 1 gói bổ sung MBAL hoặc MIC");
                }
                FlexibleCommon.validateAdditionalInsuranceAge(assuredDto.getDob().toString());

                if (assuredDto.getType() == null) {
                    assuredDto.setType(ADDITIONAL_LIVES.name());
                } else if (!ADDITIONAL_LIVES.name().equals(assuredDto.getType())) {
                    throw new CreateQuoteException("additional-assured invalid");
                }
                if (assuredDto.getMicRequest() != null) {
                    validateMicRequest(assuredDto.getDob().toString(), assuredDto.getMicRequest(), assuredDto.getGender());
                    int assuredAges = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assuredDto.getDob() + ZERO_TIME), LocalDateTime.now());
                    if (COUPLE.equals(assuredDto.getRelationshipWithPolicyHolder()) && assuredAges >= 18) {
                        flag = true;
                    }
                    if (CHILDREN.equals(assuredDto.getRelationshipWithPolicyHolder()) && assuredAges < 6) {
                        flag = true;
                    }
                }
                FlexibleCommon.Assured assured = modelMapper.map(assuredDto, Assured.class);
                //allAssured.add(assured);
                allAssuredDto.add(assuredDto);
                allAssuredOutputMap.put(assured, new QuoteAssuredOutput().setMicRequest(assuredDto.getMicRequest()));

                if (!CollectionUtils.isEmpty(assuredDto.getAdditionalProducts())) {
                    mbalAssureds.add(assured);
                    int currentIdx = mbalAssureds.size() - 1;
                    assuredDto.getAdditionalProducts().forEach(el -> {
                        MbalAdditionalProductInput product = modelMapper.map(el, MbalAdditionalProductInput.class);
                        product.setAssuredIndex(currentIdx);
                        mbalProducts.add(product);
                    });
                }
            }
        }
        // verify-MIC
//        allAssuredDto.stream().filter(o -> Objects.nonNull(o.getMicRequest())).forEach(el -> validateAdditionalFxMicInsuranceAge(localDateTimeToString(el.getDob())));

        // GENERATE BMH MBAL
        FlexibleQuoteReq flexibleQuoteReq = QuoteMapperUtil.genFlexibleQuoteReq(reqDto, customer, mbalAssureds, mbalProducts);

        try {
            long startTime = System.currentTimeMillis();
            FlexibleCreateQuoteModel createQuoteFlexibleModel = mbalApi3rdService.flexCreateQuote(flexibleQuoteReq);
            long t1 = System.currentTimeMillis();
            log.debug("Total time Generate Quotation: {}", t1 - startTime);
            if (createQuoteFlexibleModel == null) {
                log.error("[MBAL]--Body is empty");
                throw new CreateQuoteException("Tạo bảng minh họa lỗi. Vui lòng thử lại!");
            }
            if (createQuoteFlexibleModel.getCashFlow() == null) {
                log.error("[MBAL]--Quotation not found");
                throw new CreateQuoteException("Tạo bảng minh họa lỗi. Vui lòng thử lại!");
            }

            /// RESPONSE
            BigDecimal totalMicAmount = BigDecimal.valueOf(0L);
            BigDecimal totalMbalAmount = BigDecimal.valueOf(0L);
            BigDecimal totalMbalMainAmount = BigDecimal.valueOf(0L);
            BigDecimal riderAmount;
            BigDecimal topupInsuranceFee = BigDecimal.valueOf(0L);

            totalMbalAmount = totalMbalAmount.add(createQuoteFlexibleModel.getProductPackage().getPeriodicPremium());
            totalMbalMainAmount = totalMbalMainAmount.add(totalMbalAmount);
            riderAmount = BigDecimal.valueOf(createQuoteFlexibleModel.getRiders().stream()
                    .filter(Objects::nonNull)
                    .filter(o -> !COI_RIDER.equals(o.getProductType()))
                    .mapToLong(o -> o.getBasePremium().longValue()).sum());
            totalMbalAmount = totalMbalAmount.add(riderAmount);
            List<QuotationAmountRespDto> amounts = createQuoteFlexibleModel.getAmounts();
            if (amounts != null && !amounts.isEmpty() && amounts.get(0).getStartYear() == 1) {
                topupInsuranceFee = amounts.get(0).getValue();
            }
            totalMbalAmount = createQuoteFlexibleModel.getAmounts() == null ?
                    totalMbalAmount.add(BigDecimal.valueOf(0L)) : totalMbalAmount.add(BigDecimal.valueOf(createQuoteFlexibleModel.getAmounts()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(o -> o.getStartYear() == 1)
                    .mapToLong(o -> o.getValue().longValue()).sum()));

            Map<FlexibleAssuredKey, FlexibleCommon.PolicyHolderAndLifeAssured> mbalAssuredsMap = createQuoteFlexibleModel.getAssureds()
                    .stream()
                    .collect(Collectors.toMap(el -> new FlexibleAssuredKey(el)
                                    .setGender(el.getGender())
                                    .setFullName(el.getFullName())
                                    .setIdentificationType(el.getIdentificationType())
                                    .setDob(el.getDob())
                                    .setIdentificationNumber(el.getIdentificationNumber()),
                            i -> i,
                            (i1, i2) -> i1)
                    );

            List<MicPackage> micPackages = micPackageService.list();
            List<MicBenefitRespDto> micInsuranceBenefits = micPackages.stream()
                    .map(micPackage -> modelMapper.map(micPackage, MicBenefitRespDto.class))
                    .collect(Collectors.toList());

            List<AdditionalAssuredOutput> additionalAssuredOutputs = new ArrayList<>();
            //Map<MicAdditionalProduct, QuoteAssuredOutput> assuredIdAndMicProductMap = new HashMap<>(); /*<micRequest, micProductInfo>*/
            Map<String, QuoteAssuredOutput> assuredIdAndMicProductMap = new HashMap<>(); /*<identify, micProductInfo>*/

            log.debug("MIC processId {} with flag {}: ", reqDto.getProcessId(), flag);

            FlexibleCommon.ParentContract parentContract = null;
            MicAdditionalProduct policyHolderMicRequest = null;
            MicGenerateInsuranceCertSandboxRespDto.GenerateInsuranceCertData policyMicInsuranceResp = null;
            MicGenerateInsuranceCertSandboxRespDto.GenerateInsuranceCertData coupleCertSandboxResp = null;
            MicAdditionalProduct coupleMicRequest = null;
            FlexibleCommon.Assured coupleAssured = null;

            // Sort allAssuredOutputMap : Must handle policy-holder, couple first.
            for (Map.Entry<FlexibleCommon.Assured, QuoteAssuredOutput> entry : allAssuredOutputMap.entrySet()) {
                FlexibleCommon.Assured assured = entry.getKey();
                QuoteAssuredOutput entryValue = entry.getValue();
                if (assured != null && COUPLE.equals(assured.getRelationshipWithPolicyHolder())) {
                    log.debug("[MINI]--Before refactor map data {}", allAssuredOutputMap);
                    allAssuredOutputMap.remove(assured);
                    LinkedHashMap<FlexibleCommon.Assured, QuoteAssuredOutput> mapClone = (LinkedHashMap<Assured, QuoteAssuredOutput>) allAssuredOutputMap.clone();
                    allAssuredOutputMap.clear();
                    allAssuredOutputMap.put(assured, entryValue);
                    allAssuredOutputMap.putAll(mapClone);
                    log.debug("[MINI]--After refactor map data {}", allAssuredOutputMap);
                    break;
                }
            }

            for (Map.Entry<FlexibleCommon.Assured, QuoteAssuredOutput> entry : allAssuredOutputMap.entrySet()) {
                QuoteAssuredOutput productInfo = entry.getValue();
                FlexibleAssuredKey key = new FlexibleAssuredKey(entry.getKey());

                FlexibleCommon.Assured assured = entry.getKey();
                FlexibleCommon.PolicyHolderAndLifeAssured mbalAssured = mbalAssuredsMap.get(key);
                if (assured == null) {
                    log.warn("[CREATE-QUOTATION] Assured not found....");
                    continue;
                }
                int ages = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assured.getDob() + ZERO_TIME), LocalDateTime.now());
                // HANDLE FOR MIC
                MicAdditionalProduct micRequest = productInfo.getMicRequest();
//                boolean hasParentMic = false;
                if (micRequest != null) {
                    micRequest.setMaSp(MIC_PRODUCT_CODE);
                    MicBenefitRespDto micBenefit = micInsuranceBenefits.stream().filter(o -> o.getId() == micRequest.getNhom()).findFirst()
                            .orElseThrow(() -> new MicPackageNotFoundException("Không tồn tại gói bảo hiểm MIC."));
                    MicCompareResp hasParentMic = new MicCompareResp();
                    parentContract = micRequest.getParentContract();
                    if (flag && ages < 6 && (policyMicInsuranceResp != null || coupleCertSandboxResp != null || parentContract!= null)
                            && CHILDREN.equals(assured.getRelationshipWithPolicyHolder())) {
                        log.debug("Assured apply policy with name {} and age: {}", assured.getFullName(), ages);

                        if (parentContract != null) {
                            try {
                                MicSandboxContractInfoRespDto parentContractInfo = micApi3rdService.micSandboxSearchContractInfo(parentContract.getSo_hd_bm(),
                                        DateUtil.localDateTimeToString(DateUtil.DATE_DMY, LocalDateTime.now()));
                                parentContract.setSo_hd_bm(parentContract.getSo_hd_bm());
                                parentContract.setCmt_bm(parentContractInfo.getData().getCmt());
                                parentContract.setTen_bm(parentContractInfo.getData().getTen());
                                parentContract.setMobi_bm(parentContractInfo.getData().getMobi());
                                hasParentMic = checkMicParentRequestWithChildrenRequest(micRequest, policyHolderMicRequest, customer,
                                        coupleMicRequest, coupleAssured, parentContractInfo.getData(), parentContract);
                            } catch (MicApiException e) {
                                log.error("[MIC]--HD bố/mẹ không đủ điều kiện apply {}", parentContract.getSo_hd_bm());
                                hasParentMic = checkMicParentRequestWithChildrenRequest(micRequest, policyHolderMicRequest,
                                        customer, coupleMicRequest, coupleAssured, null, null);
                            }
                        } else {
                            hasParentMic = checkMicParentRequestWithChildrenRequest(micRequest, policyHolderMicRequest,
                                    customer, coupleMicRequest, coupleAssured, null, null);
                        }
                    } else {
                        if (parentContract != null) {
                            micRequest.setParentContract(null);
                        }
                    }
                    String micTransactionId = generateUUIDId(30);
                    MicSandboxFeeCareRespDto micFeeResult = micApi3rdService.flexibleMicSandboxFee(assured, micRequest);
                    productInfo.setMicResult(micFeeResult);

                    // Tính tổng phí quyền lợi MIC
                    long micMainBenefit = convertToLong(micBenefit.getMainOne())
                            + convertToLong(micBenefit.getMainTwo())
                            + convertToLong(micBenefit.getMainThree());
                    if (micRequest.getBs1().equals(YES)) {
                        micMainBenefit = micMainBenefit + convertToLong(micBenefit.getSubOne());
                    }
                    if (micRequest.getBs2().equals(YES)) {
                        micMainBenefit = micMainBenefit + convertToLong(micBenefit.getSubTwo().substring(micBenefit.getSubTwo().indexOf("và tối đa")));
                    }

                    if (micRequest.getBs3().equals(YES)) {
                        micMainBenefit = micMainBenefit + convertToLong(micBenefit.getSubThree());
                    }

                    if (micRequest.getBs4().equals(YES)) {
                        micMainBenefit = micMainBenefit + convertToLong(micBenefit.getSubFour());
                    }

                    productInfo.setMicBenefit(micBenefit);

                    micFeeResult.setMicTransactionId(micTransactionId);
                    micFeeResult.setMicSumBenefit(BigDecimal.valueOf(micMainBenefit));

                    //// GENERATE GCNBH MIC
                    MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = micApi3rdService.generateMicSandboxTtinkh(customer, assured);

                    if (flag && ages < 6 && (policyMicInsuranceResp != null || coupleCertSandboxResp != null || micRequest.getParentContract() != null)
                            && CHILDREN.equals(assured.getRelationshipWithPolicyHolder()) && hasParentMic.isResult()) {
                        switch (hasParentMic.getCompareType()) {
                            case POLICY_HOLDER:
                                micRequest.getParentContract().setSo_hd_bm(policyMicInsuranceResp == null ? "" : policyMicInsuranceResp.getSo_hd());
                                break;
                            case COUPLE_INSURED:
                                micRequest.getParentContract().setSo_hd_bm(coupleCertSandboxResp == null ? "" : coupleCertSandboxResp.getSo_hd());
                                break;
                            case PARENT_CONTRACT:
                                micRequest.getParentContract().setSo_hd_bm(Objects.requireNonNull(parentContract).getSo_hd_bm());
                                break;
                            default:
                                micRequest.getParentContract().setSo_hd_bm("");
                        }
                    }
                    MicGenerateInsuranceCertSandboxRespDto micInsuranceResp = micApi3rdService.micSandboxGenerateCert(ttinkh, micFeeResult.getPhi().longValue(),
                            micTransactionId, micRequest.getNhom(), new FlexibleCommon.GcnMicCareDkbs()
                                    .setBs1(micRequest.getBs1())
                                    .setBs2(micRequest.getBs2())
                                    .setBs3(micRequest.getBs3())
                                    .setBs4(micRequest.getBs4()),
                            micRequest.getParentContract());

                    MicGenerateInsuranceCertSandboxRespDto.GenerateInsuranceCertData micCertData = micInsuranceResp.getData();
                    if (micCertData == null) {
                        log.error("Error ");
                        throw new CreateQuoteException("Có lỗi xảy ra khi tạo giấy chứng nhận bảo hiểm MIC, xin vui lòng thử lại");
                    }
                    log.info("micInsuranceResp.getData(): " + jackson.toJson(micInsuranceResp.getData()));
                    micFeeResult.setGcn(micCertData.getSo_hd());
                    micFeeResult.setSoId(micCertData.getSo_id());
                    micFeeResult.setFile(micCertData.getFile());
                    if (POLICY_HOLDER.equals(assured.getRelationshipWithPolicyHolder()) && flag) {
                        policyHolderMicRequest = micRequest;
                        policyMicInsuranceResp = micInsuranceResp.getData();
                    }

                    if (COUPLE.equals(assured.getRelationshipWithPolicyHolder()) && flag && ages >= 18) {
                        coupleMicRequest = micRequest;
                        coupleCertSandboxResp = micInsuranceResp.getData();
                        coupleAssured = assured;
                    }
                    totalMicAmount = totalMicAmount.add(micFeeResult.getPhi());
                    assuredIdAndMicProductMap.put(genIdentifyKey(String.valueOf(assured.getIdentificationType()), assured.getIdentificationId()), productInfo);
                }

                // UPDATE FOR MBAL/only MIC
                String mbalAssuredId = generateUUIDId(18);
                if (mbalAssured != null) {
                    mbalAssuredId = mbalAssured.getId();
                    productInfo.setRiders(createQuoteFlexibleModel.getRiders().stream()
                            .filter(Objects::nonNull)
                            .filter(o -> o.getAssuredId().equals(mbalAssured.getId()))
                            .map(el -> {
                                el.setSumAssuredOutput(el.getSumAssured());
                                if (PWR.equals(el.getProductType())) el.setSumAssured(BigDecimal.ZERO);
                                return el;
                            })
                            .collect(Collectors.toList()));
                }

                if (LIFE_ASSURED.equals(assured.getType()) && mbalAssured != null) {
                    mbalAssured.setAdditionalProduct(productInfo);
                    mbalAssuredsMap.putIfAbsent(key, mbalAssured);
                }
                if (ADDITIONAL_LIVES.equals(assured.getType())) {
                    additionalAssuredOutputs.add(new AdditionalAssuredOutput()
                            .setAssured(assured.setId(mbalAssuredId))
                            .setAdditionalProduct(productInfo));
                }
            }
            long t2 = System.currentTimeMillis();
            log.debug("[ToolCrm]--Milli seconds createQuote t2-t1: {}", t2 - t1);

            // SAVE QUOTATION
            QuotationModel model = new QuotationModel(Long.parseLong(createQuoteFlexibleModel.getQuotationCode()), Long.parseLong(createQuoteFlexibleModel.getSubmissionCode()), createQuoteFlexibleModel.getCashFlow());

            QuotationHeader quotationEntity = quotationHeaderToEntity(reqDto);
            quotationEntity.setUuid(UUID.randomUUID());
            quotationEntity.setQuotationId(model.getQuotationId());
            quotationEntity.setSubmissionId(model.getSubmissionId());
            quotationEntity.setDetails(quotationModelToEntity(model, quotationEntity));
            quotationEntity.setRaw(false);
            quotationEntity.setState(QuotationState.CREATED);
            //add::MIC:customer
            String customerIdentify = genIdentifyKey(String.valueOf(quotationEntity.getCustomer().getIdentificationType()), quotationEntity.getCustomer().getIdentificationId());
            if (assuredIdAndMicProductMap.get(customerIdentify) != null) {
                quotationEntity.getCustomer().setMicInsuranceContract(micInsuranceContractToEntity(assuredIdAndMicProductMap.get(customerIdentify)));
            }
            //add::MIC:assureds
            if (!CollectionUtils.isEmpty(quotationEntity.getAssureds())) {
                quotationEntity.getAssureds().forEach(el -> {
                    String identify = genIdentifyKey(String.valueOf(el.getIdentificationType()), el.getIdentificationId());
                    if (assuredIdAndMicProductMap.get(identify) != null) {
                        el.setMicInsuranceContract(micInsuranceContractToEntity(assuredIdAndMicProductMap.get(identify)));
                    }
                });
            }

            quotationEntity.setCreatedDate(createdDate);
            QuotationHeader quotationDb = quotationHeaderRepository.save(quotationEntity);

            QuotationDto quotationDto = new QuotationDto(quotationEntity);

            long t3 = System.currentTimeMillis();
            log.debug("[ToolCrm]--Milli seconds createQuote t3-t2: {}", t3 - t2);
            quotationDto.setCreateQuoteReq(flexibleQuoteReq);
            quotationDto.setCreatedDate(createdDate);
            quotationDto.setMbalMainAmountStr(formatCurrency(totalMbalMainAmount));
            log.warn("totalMbalMainAmount: ", totalMbalMainAmount);
            quotationDto.setApplicationStatus(createQuoteFlexibleModel.getApplicationStatus());
            quotationDto.setQuotationStatus(createQuoteFlexibleModel.getQuotationStatus());
            quotationDto.setProductPackage(createQuoteFlexibleModel.getProductPackage());
            quotationDto.setPolicyHolder(createQuoteFlexibleModel.getPolicyHolder()
                    .setMiniAssuredId(customer.getMiniAssuredId())
                    .setAdditionalProduct(allAssuredOutputMap.get(customer)));
            quotationDto.setAdditionalAssuredOutputs(additionalAssuredOutputs);
            quotationDto.setLifeAssured(mbalAssuredsMap.get(new FlexibleAssuredKey(mainAssured))
                    .setAdditionalProduct(allAssuredOutputMap.get(mainAssured))
                    .setMiniAssuredId(mainAssured.getMiniAssuredId()));
            quotationDto.setQuotationDate(createQuoteFlexibleModel.getQuotationDate());
            quotationDto.setProductPackageCode(createQuoteFlexibleModel.getProductPackageCode());
            quotationDto.setSale(createQuoteFlexibleModel.getSale());
            //quotationDto.setReferer(createQuoteFlexibleModel.getReferer());
            if (createQuoteFlexibleModel.getReferrer() != null) {
                quotationDto.setReferrer(createQuoteFlexibleModel.getReferrer().toDto());
            }
            if (reqDto.getReferrer() != null) {
                quotationDto.setRefererInput(reqDto.getReferrer());
            }
            if (createQuoteFlexibleModel.getSupporter() != null) {
                quotationDto.setSupporter(createQuoteFlexibleModel.getSupporter().toDto());
            }
            quotationDto.setProcessId(createQuoteFlexibleModel.getProcessId());
            quotationDto.setApplicationNumber(createQuoteFlexibleModel.getApplicationNumber());

            BigDecimal totalAmount = totalMbalAmount.add(totalMicAmount);
            quotationDto.setTotalAmount(totalAmount);
            quotationDto.setTotalAmountStr(formatCurrency(totalAmount));
            quotationDto.setTotalMbalAmountStr(formatCurrency(totalMbalAmount));
            quotationDto.setTotalMicAmountStr(totalMicAmount.longValue() == 0 ? null : formatCurrency(totalMicAmount));
            quotationDto.setRiderAmountStr(formatCurrency(riderAmount));
            log.warn("topupInsuranceFee: {}", topupInsuranceFee);
            quotationDto.setTopupAmountStr(formatCurrency(topupInsuranceFee));

            // CUSTOMER && ASSURED
            quotationDto.setCustomer(reqDto.getCustomer());
            quotationDto.setAssured(reqDto.getAssured());
            quotationDto.setAdditionalAssureds(reqDto.getAdditionalAssureds());

            quotationDto.setRaiderDeductFund(createQuoteFlexibleModel.isRaiderDeductFund());
            if (createQuoteFlexibleModel.getBeneficiaries() != null && !createQuoteFlexibleModel.getBeneficiaries().isEmpty()) {
                FlexibleCommon.BeneficiaryOutput beneficiary = createQuoteFlexibleModel.getBeneficiaries().get(0);
                beneficiary.setPhoneNumber(reqDto.getBeneficiary().getPhoneNumber());
                beneficiary.setAddress(reqDto.getBeneficiary().getAddress());
                beneficiary.setEmail(reqDto.getBeneficiary().getEmail());
                quotationDto.setBeneficiary(beneficiary);
            }
            //quotationDto.setMicProductModels(micProductModels);
            //more...
            quotationDto.setAssureds(createQuoteFlexibleModel.getAssureds());
            quotationDto.setQuotationCode(createQuoteFlexibleModel.getQuotationCode());
            quotationDto.setSubmissionCode(createQuoteFlexibleModel.getSubmissionCode());
            quotationDto.setPolicyNumber(createQuoteFlexibleModel.getPolicyNumber());
            //quotationDto.setCashFlow(createQuoteFlexibleModel.getCashFlow()); // details
            quotationDto.setRiders(createQuoteFlexibleModel.getRiders());
            quotationDto.setAmounts(createQuoteFlexibleModel.getAmounts() == null ? null : createQuoteFlexibleModel.getAmounts()
                    .stream().map(o -> modelMapper.map(o, QuotationAmountRespDto.class)).collect(Collectors.toList()));
            quotationDto.setBeneficiaries(createQuoteFlexibleModel.getBeneficiaries());

            if (quotationDto.getCustomer() != null) {
//                Optional<PolicyHolderAndLifeAssured> assured = createQuoteFlexibleModel.getAssureds().stream().filter(el -> isMatchedIdentify(quotationDto.getCustomer(), el)).findFirst();
                Optional<AdditionalAssuredOutput> assured = additionalAssuredOutputs.stream().filter(el -> isMatchedIdentify(quotationDto.getCustomer(), el.getAssured())).findFirst();
                assured.ifPresent(o -> {
                    quotationDto.getCustomer().setUuid(o.getAssured().getId());
                    quotationDto.getCustomer().setAdditionalProducts(createQuoteFlexibleModel.getRiders().stream()
                            .filter(Objects::nonNull)
                            .filter(el -> el.getAssuredId().equals(o.getAssured().getId())).map(el -> {
                                QuotationProductDto prd = modelMapper.map(el, QuotationProductDto.class);
                                prd.setSumAssuredOutput(prd.getSumAssured());
                                if (ProductType.PWR.equals(prd.getProductType())) prd.setSumAssured(BigDecimal.ZERO);
                                return prd;
                            }).collect(Collectors.toList()));

                    QuoteAssuredOutput micProduct = assuredIdAndMicProductMap.get(genIdentifyKey(String.valueOf(customer.getIdentificationType()), customer.getIdentificationId()));
                    quotationDto.getCustomer().setMicAdditionalProduct(micProduct);
                });
            }
            if (quotationDto.getAssured() != null) {
                Optional<PolicyHolderAndLifeAssured> assured = createQuoteFlexibleModel.getAssureds().stream().filter(el -> isMatchedIdentify(quotationDto.getAssured(), el)).findFirst();
                assured.ifPresent(o -> {
                    quotationDto.getAssured().setUuid(o.getId());
                    quotationDto.getAssured().setAdditionalProducts(createQuoteFlexibleModel.getRiders().stream()
                            .filter(Objects::nonNull)
                            .filter(el -> el.getAssuredId().equals(o.getId())).map(el -> {
                                QuotationProductDto prd = modelMapper.map(el, QuotationProductDto.class);
                                prd.setSumAssuredOutput(prd.getSumAssured());
                                if (ProductType.PWR.equals(prd.getProductType())) prd.setSumAssured(BigDecimal.ZERO);
                                return prd;
                            }).collect(Collectors.toList()));

                    QuoteAssuredOutput micProduct = assuredIdAndMicProductMap.get(genIdentifyKey(String.valueOf(quotationDto.getAssured().getIdentificationType()), quotationDto.getAssured().getIdentificationId()));
                    quotationDto.getAssured().setMicAdditionalProduct(micProduct);
                });
            }
            if (!CollectionUtils.isEmpty(quotationDto.getAdditionalAssureds())) {
                for (QuotationAssuredDto addAssured : quotationDto.getAdditionalAssureds()) {
//                    Optional<PolicyHolderAndLifeAssured> assured = createQuoteFlexibleModel.getAssureds().stream().filter(el -> isMatchedIdentify(addAssured, el)).findFirst();
                    Optional<AdditionalAssuredOutput> assured = additionalAssuredOutputs.stream().filter(el -> isMatchedIdentify(addAssured, el.getAssured())).findAny();
                    assured.ifPresent(o -> {
                        addAssured.setUuid(o.getAssured().getId());
                        addAssured.setAdditionalProducts(createQuoteFlexibleModel.getRiders().stream()
                                .filter(Objects::nonNull)
                                .filter(el -> el.getAssuredId().equals(o.getAssured().getId())).map(el -> {
                                    QuotationProductDto prd = modelMapper.map(el, QuotationProductDto.class);
                                    prd.setSumAssuredOutput(prd.getSumAssured());
                                    if (ProductType.PWR.equals(prd.getProductType())) prd.setSumAssured(BigDecimal.ZERO);
                                    return prd;
                                }).collect(Collectors.toList()));

                        QuoteAssuredOutput micProduct = assuredIdAndMicProductMap.get(genIdentifyKey(String.valueOf(addAssured.getIdentificationType()), addAssured.getIdentificationId()));
                        addAssured.setMicAdditionalProduct(micProduct);
                    });
                }
            }

            // caching...
            flexibleQuotationCaching.setQuotationData(username, reqDto.getProcessId(), quotationDto);

            int updated = quotationHeaderRepository.updateRawData(quotationDb.getId(), jackson.toJson(quotationDto));
            if (updated < 1) {
                throw new CreateQuoteException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Update failed"));
            }

            // Update cusotmer direct
            Long directId = flexibleQuotationCaching.getDirectId(username, reqDto.getProcessId());
            if (directId != null) {
                PotentialCustomerDirect direct = directRepository.getById(directId, username);
                QuotationHeader header = new QuotationHeader();
                header.setId(quotationDto.getId());
                direct.setQuotationHeader(header);
                direct.setState(DirectState.CREATE_BMHX);
                direct.setRaw(false);
                directRepository.save(direct);
                flexibleQuotationCaching.delDirectId(username, reqDto.getProcessId());
            }

            long t4 = System.currentTimeMillis();
            log.debug("[ToolCrm]--Milli seconds createQuote t4-t3: {}", t4 - t3);
            return quotationDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new CreateQuoteException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    @Transactional
    public QuotationDto flexConfirmQuote(ConfirmQuoteFlexibleReqDto reqDto) {
        String username = AppUtil.getCurrentUsername();
        this.verifyCustomerPermission(username, reqDto.getProcessId());

        try {
            long t1 = System.currentTimeMillis();
            FlexibleRespModel confirmQuote = mbalApi3rdService.flexConfirmQuote(reqDto.getProcessId());
            long t2 = System.currentTimeMillis();
            log.debug("[ToolCrm]--Milli seconds confirmQuote t2-t1: {}", t2 - t1);
            if (confirmQuote == null) {
                log.error("[MBAL]--Confirm quote lỗi với processId {}", reqDto.getProcessId());
                throw new ConfirmQuoteException(MSG12);
            }
            confirmQuote.setProcessId(reqDto.getProcessId());

            QuotationDto quotationDto = flexibleQuotationCaching.getQuotationData(username, reqDto.getProcessId());
            if (quotationDto == null || quotationDto.getId() == null) {
                log.error("[MBAL]--Confirm quote lỗi với quotationId is null");
                throw new ConfirmQuoteException(MSG12);
            }
            quotationDto.setAppQuestionNumber(confirmQuote.getAppQuestionNumber());
            FlexibleCommon.PolicyHolderAndLifeAssured mainAssured = quotationDto.getLifeAssured();

            List<PolicyHolderAndLifeAssured> assureds = confirmQuote.getAssureds();
            quotationDto.setAssureds(assureds);

            Map<String, PolicyHolderAndLifeAssured> assuredMap = assureds.stream().collect(toMap(PolicyHolderAndLifeAssured::getId, Function.identity()));
            Optional<PolicyHolderAndLifeAssured> lifeAssured = assureds.stream().filter(o -> o.getId().equals(mainAssured.getId())).findFirst();
            List<AdditionalAssuredOutput> additionalAssuredOutputs = quotationDto.getAdditionalAssuredOutputs();
            for (AdditionalAssuredOutput assuredOutput : additionalAssuredOutputs) {
                PolicyHolderAndLifeAssured assured = assuredMap.get(assuredOutput.getAssured().getId());
                if (assured != null) assuredOutput.setAppQuestionNumber(assured.getAppQuestionNumber());
            }
            mainAssured.setAppQuestionNumber(lifeAssured.orElseThrow(() -> new ConfirmQuoteException(MSG12)).getAppQuestionNumber());
            quotationDto.setApplicationNumber(confirmQuote.getApplicationNumber());
            quotationDto.getAssured().setAppQuestionNumber(mainAssured.getAppQuestionNumber());

            Map<String, PolicyHolderAndLifeAssured> identifyAssuredMap = assureds.stream().collect(toMap(el -> genIdentifyKey(String.valueOf(el.getIdentificationType()), el.getIdentificationId()), Function.identity()));

            QuotationAssuredDto customer = quotationDto.getCustomer();
            PolicyHolderAndLifeAssured customerAssured = identifyAssuredMap.get(genIdentifyKey(String.valueOf(customer.getIdentificationType()), customer.getIdentificationId()));
            if (customerAssured != null) customer.setAppQuestionNumber(customerAssured.getAppQuestionNumber());

            if (!CollectionUtils.isEmpty(quotationDto.getAdditionalAssureds())) {
                quotationDto.getAdditionalAssureds().forEach(el -> {
                    PolicyHolderAndLifeAssured assured = identifyAssuredMap.get(genIdentifyKey(String.valueOf(el.getIdentificationType()), el.getIdentificationId()));
                    if (assured != null) el.setAppQuestionNumber(assured.getAppQuestionNumber());
                });
            }

            // caching...
            flexibleQuotationCaching.setQuotationData(username, reqDto.getProcessId(), quotationDto);
            int updated = quotationHeaderRepository.updateRawDataAndState(quotationDto.getId(), jackson.toJson(quotationDto), QuotationState.CONFIRMED);
            if (updated < 1) {
                throw new ConfirmQuoteException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Update failed"));
            }

            return quotationDto;
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new ConfirmQuoteException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    @Override
    @Transactional
    public GenQRCodeResp updateAndGenQRCode(UpdateQuoteAndGenQRCodeReq reqDto) {
        String username = AppUtil.getCurrentUsername();
        this.verifyCustomerPermission(username, reqDto.getProcessId());

        Optional<QuotationHeader> quotationOtp = quotationHeaderRepository.findFirstByCreatedByAndProcessIdOrderByIdDesc(username, reqDto.getProcessId());
        if (quotationOtp.isEmpty()) {
            throw new ApplicationException("Quotation notfound");
        }
        QuotationHeader quotation = quotationOtp.get();
        verifyCreateQrCode(quotation.getState());

        QuotationDto quotationDto = flexibleQuotationCaching.getQuotationData(username, reqDto.getProcessId());

        // update DB and clear cache
        boolean hasUpdate = false;
        if (reqDto.getHealths() != null) {
            QuoteUtil.verifyHealths(quotationDto, reqDto.getHealths()); // verify

            quotation.setHealths(jackson.toJson(reqDto.getHealths()));
            //quotationDto.setHealths(reqDto.getHealths());
            hasUpdate = true;
        }

        if (reqDto.getMicHealths() != null) {
            quotation.setMicHealths(jackson.toJson(reqDto.getMicHealths()));
            hasUpdate = true;
        }

        if (reqDto.getSale() != null && StringUtils.hasText(reqDto.getSale().getCode())) {
            QuotationSupporter sale = new QuotationSupporter();
            sale.setCode(reqDto.getSale().getCode());
            sale.setName(reqDto.getSale().getName());
            quotation.setSale(sale);
            quotationDto.setSale(modelMapper.map(sale, FlexibleCommon.Sale.class));
            hasUpdate = true;
        }
        if (reqDto.getReferrer() != null && StringUtils.hasText(reqDto.getReferrer().getCode())) {
            quotation.setReferrer(reqDto.getReferrer().toEntity());
            quotationDto.setReferrer(reqDto.getReferrer());
            hasUpdate = true;
        }
        if (reqDto.getSupporter() != null && StringUtils.hasText(reqDto.getSupporter().getCode())) {
            quotation.setSupporter(reqDto.getSupporter().toEntity());
            quotationDto.setSupporter(reqDto.getSupporter());
            hasUpdate = true;
        }

        if (hasUpdate) {
            quotation.setRawData(jackson.toJson(quotationDto));
            quotationHeaderRepository.save(quotation);
        }
        flexibleQuotationCaching.delQuotationData(username, reqDto.getProcessId());

        String url = ApiUtil.map(LINK_QRCODE_QUOTE_DETAIL, Map.of(
                "cardID", quotation.getCustomer().getIdentificationId(),
                "cardType", quotation.getCustomer().getIdentificationType(),
                "quotationUid", quotation.getUuid()
        ));
        return new GenQRCodeResp(url);
    }

    @Override
    public QuotationDto findQuotationDetail(UUID uuid) {
        Optional<QuotationHeader> quotationOpt = quotationHeaderRepository.findByUuid(uuid);
        if (quotationOpt.isEmpty() || quotationOpt.get().getRawData() == null) {
            throw new ValidationException("Không tìm thấy dữ liệu");
        }
        QuotationHeader quotation = quotationOpt.get();
        if (!QuotationState.CONFIRMED.equals(quotation.getState()) &&
                !QuotationState.SUBMITTED.equals(quotation.getState()) &&
                !QuotationState.UN_MATCH.equals(quotation.getState())) {
            throw new ValidationException("Không thể quét mã, trạng thái hồ sơ không hợp lệ (" + QuotationState.toTextCode(quotation.getState()) + ")");
        }

        QuotationDto quotationDto = jackson.fromJson(quotation.getRawData(), QuotationDto.class);
        quotationDto.setState(quotation.getState());

        if (StringUtils.hasText(quotation.getHealths())) {
            List<FlexibleSubmitQuestionInput> healths = gson.fromJson(quotation.getHealths(), new TypeToken<List<FlexibleSubmitQuestionInput>>() {
            }.getType());
            quotationDto.setHealths(healths);

            quotationDto.setHealthsTxt(quotation.getHealths());
        }

        if (StringUtils.hasText(quotation.getMicHealths())) {
            List<FlexibleSubmitMicQuestionRequest> healths = gson.fromJson(quotation.getMicHealths(), new TypeToken<List<FlexibleSubmitMicQuestionRequest>>() {
            }.getType());
            quotationDto.setMicHealths(healths);
        }

        if (quotation.getDirect() != null) {
            DirectInfo direct = new DirectInfo();
            quotationDto.setDirect(direct);

            direct.setDirectId(quotation.getDirect().getId());
            if (quotation.getDirect().getPotentialCustomer() != null) {
                direct.setLeadId(quotation.getDirect().getLeadId());
                direct.setNote(quotation.getDirect().getPotentialCustomer().getNote());
            }
            direct.setProductPackage(PRODUCT_DEFAULT);
        }

        return quotationDto;
    }

    /**
     * state from:
     * - tool: CREATED, CONFIRMED
     * - miniApp: UN_MATCH || SUBMITTED, COMPLETED || RE_CREATED, RE_CONFIRMED, RE_SUBMITTED, RE_COMPLETED (customer edit or info not-match)
     */
    @Override
    @Transactional
    public QuotationSyncDataResp syncQuotation(QuotationSyncDataDto request) {
        log.info("SyncQuotation-Input: " + gson.toJson(request));

        Optional<QuotationHeader> quotationOpt = quotationHeaderRepository.findByUuid(request.getUuid());
        if (quotationOpt.isEmpty()) {
            throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Not found"));
        }

        QuotationHeader quotation = quotationOpt.get();
        if (request.getState() != null) {
            switch (request.getState()) {
                case UN_MATCH:
                    quotation.setState(QuotationState.UN_MATCH);
                    break;
                case CREATED:
                    quotation.setState(QuotationState.RE_CREATED);
                    break;
                case CONFIRMED:
                    quotation.setState(QuotationState.RE_CONFIRMED);
                    break;
                case SUBMITTED:
                    if (QuotationState.RE_CONFIRMED.equals(quotation.getState())) {
                        quotation.setState(QuotationState.RE_SUBMITTED);
                    } else {
                        quotation.setState(QuotationState.SUBMITTED);
                    }
                    break;
                case COMPLETED:
                    if (QuotationState.RE_SUBMITTED.equals(quotation.getState())) {
                        quotation.setState(QuotationState.RE_COMPLETED);
                    } else {
                        quotation.setState(QuotationState.COMPLETED);
                    }
                    break;
                default:
                    log.error("State of quotation invalid");
            }
        }
        if (StringUtils.hasText(request.getHealthsTxt())) {
            quotation.setHealths(request.getHealthsTxt());
        }

        log.info("SyncQuotation-State: " + quotation.getState());
        quotationHeaderRepository.save(quotation);

        if (quotation.getDirect() != null && request.getDirectSubmitStatus() != null) {
            potentialCustomerDirectService.saveSubmitStatus(quotation.getDirect(), request.getDirectSubmitStatus());
        }

        return new QuotationSyncDataResp(true, "Successfully", quotation.getState());
    }

    /***/
    public void verifyCustomerPermission(String username, Long processId) {
        if (processId == null || !flexibleQuotationCaching.existsProcess(username, processId)) {
            throw new CustomerPermissionException(MSG14);
        }
    }

}
