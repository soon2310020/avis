package com.stg.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.stg.config.event.SendEmailEvent;
import com.stg.entity.*;
import com.stg.entity.customer.Customer;
import com.stg.entity.customer.CustomerDetail;
import com.stg.errors.*;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.*;
import com.stg.service.ExternalFlexibleApiService;
import com.stg.service.ExternalMicAPIService;
import com.stg.service.ExternalV2ApiService;
import com.stg.service.MicPackageService;
import com.stg.service.UploadFileService;
import com.stg.service.caching.FlexibleQuotationCaching;
import com.stg.service.dto.enumcode.ErrorCodeFlexQuotation;
import com.stg.service.dto.external.MbalIllustrationDownloadRespDto;
import com.stg.service.dto.external.request.MicGenerateInsuranceCertReqDto;
import com.stg.service.dto.external.requestFlexible.*;
import com.stg.service.dto.external.requestV2.SendMailV2ReqDto;
import com.stg.service.dto.external.response.*;
import com.stg.service.dto.external.responseFlexible.*;
import com.stg.service.dto.external.responseV2.ErrorResponseMbal;
import com.stg.service.dto.external.responseV2.MultipartInputStreamFileResource;
import com.stg.service.dto.external.responseV2.OccupationV2RespDto;
import com.stg.service.dto.insurance.UploadFileRespDto;
import com.stg.service3rd.common.dto.enumcode.IEnumCode;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.dto.MbalBranchDto;
import com.stg.service3rd.mbal.dto.MbalICDto;
import com.stg.service3rd.mbal.dto.MbalPotentialCustomerDto;
import com.stg.service3rd.mbal.dto.MbalRMDto;
import com.stg.service3rd.mbal.dto.ProductInfoDto;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.service3rd.mbal.dto.req.FlexibleCheckTSAReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleCheckTSAResp;
import com.stg.service3rd.toolcrm.ToolCrmApi3rdService;
import com.stg.service3rd.toolcrm.constant.QuotationAction;
import com.stg.service3rd.toolcrm.constant.QuotationState;
import com.stg.service3rd.toolcrm.dto.req.DirectSubmitStatusReq;
import com.stg.service3rd.toolcrm.dto.req.QuotationDetailReq;
import com.stg.service3rd.toolcrm.dto.req.QuotationSyncDataReq;
import com.stg.service3rd.toolcrm.dto.resp.QuotationDetailResp;
import com.stg.service3rd.toolcrm.dto.resp.QuotationSyncDataResp;
import com.stg.service3rd.toolcrm.dto.resp.quote.DirectInfo;
import com.stg.service3rd.toolcrm.dto.resp.quote.FlexibleQuoteReq;
import com.stg.service3rd.toolcrm.dto.resp.quote.FlexibleSubmitQuestionInput;
import com.stg.service3rd.toolcrm.dto.resp.quote.QuotationAssuredDto;
import com.stg.utils.*;
import com.stg.service3rd.toolcrm.error.ToolCrmApi3rdException;
import com.stg.utils.flexible.FlexibleCrmUtil;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.stg.config.AsyncConfiguration.TAKE_TIME_LOW_CPU;
import static com.stg.service3rd.common.utils.ApiUtil.SERVER_ERROR_MESSAGE;
import static com.stg.utils.CommonUtils.*;
import static com.stg.utils.Gender.FEMALE;
import static com.stg.utils.Gender.MALE;
import static com.stg.utils.IdentificationType.NATIONAL_ID;
import static com.stg.utils.MicGroup.getMicGroup;
import static com.stg.utils.NlpUtil.removeAccent;
import static com.stg.utils.AssuredType.ADDITIONAL_LIVES;
import static com.stg.utils.AssuredType.LIFE_ASSURED;
import static com.stg.utils.Common.convertIdentificationTypeMbalToMiniApp;
import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.CommonUtils.*;
import static com.stg.utils.Constants.*;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;
import static com.stg.utils.DateUtil.DATE_YYYY_MM_DD;
import static com.stg.utils.Endpoints.*;
import static com.stg.utils.FlexibleCommon.*;
import static com.stg.utils.Gender.FEMALE;
import static com.stg.utils.Gender.MALE;
import static com.stg.utils.IdentificationType.NATIONAL_ID;
import static com.stg.utils.NlpUtil.isMatchFullNameT24;
import static com.stg.utils.NlpUtil.removeAccent;
import static com.stg.utils.ProductType.COI_RIDER;
import static com.stg.utils.RelationshipPolicyHolderType.*;
import static com.stg.utils.flexible.FlexibleCrmUtil.isMatchedIdentify;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.*;
import static com.stg.service3rd.common.utils.ApiUtil.SERVER_ERROR_MESSAGE;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ExternalFlexibleApiServiceImpl implements ExternalFlexibleApiService {

    @Value("${external.host.mbal_ul_3}")
    private String mbalHostUl3;

    @Value("${external.mbal-key.client_id_ul3}")
    private String clientIdUl3;

    @Value("${external.mbal-key.client_secret_ul3}")
    private String clientSecretUl3;

    private final CustomerRepository customerRepository;
    private final ExternalMicAPIService micAPIService;
    private final MicPackageRepository micPackageRepository;
    private final MicPackageService micPackageService;
    private final PrimaryInsuredRepository primaryInsuredRepository;
    private final PrimaryProductRepository primaryProductRepository;
    private final AdditionalInsuredRepository additionalInsuredRepository;
    private final AdditionalProductRepository additionalProductRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final CustomerDetailRepository customerDetailRepository;

    @Autowired
    private HttpHeaders mbalBasicAuth;

    @Autowired
    private final RedisCommands<String, String> redis;

    @Autowired
    private final AsyncObjectImpl asyncObject;

    @Autowired
    private final CommonService commonService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private final Gson gson;

    @Autowired
    private final UploadFileService uploadFileService;

    @Autowired
    private final ModelMapper modelMapper;

    @Autowired
    private final ExternalV2ApiService externalV2ApiService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final InsurancePackageRepository insurancePackageRepository;

    @Autowired
    private final IllustrationTableRepository illustrationTableRepository;

    @Autowired
    private final InsuranceRequestRepository insuranceRequestRepository;

    @Autowired
    private final PavTablesRepository pavTablesRepository;

    private final ToolCrmApi3rdService toolCrmApi3rdService;
    private final FlexibleQuotationCaching flexibleQuotationCaching;
    private final MbalApi3rdService mbalApi3rdService;

    /***/
    @Override
    public FlexibleQuotationResp findQuotationDetail(String cif, QuotationDetailReq reqDto) {
        log.info("findQuotationDetail(quotationUid={})", reqDto.getQuotationUid());
        String identifyTypeOrg = reqDto.getIdentificationType();
        String idCardType = convertIdentificationTypeMbalToMiniApp(identifyTypeOrg);
        reqDto.setIdentificationType(idCardType);

        // 1. verify reqDto vs mb-customer
        Customer mbCustomer = customerRepository.findByMbId(cif);
        if (
            //!reqDto.getIdentificationType().equals(mbCustomer.getIdCardType()) ||
                !reqDto.getIdentificationNumber().equals(mbCustomer.getIdentification())
        ) {
            throw new MiniApiException("Số giấy tờ tuỳ thân không trùng khớp. Vui lòng cập nhật lại!");
        }

        // 2. get quotation data
        QuotationDetailResp quotationData = toolCrmApi3rdService.findQuotationDetail(reqDto);
        if (quotationData.getProcessId() == null) {
            throw new MiniApiException("Không tìm thấy dữ liệu");
        }
        if (StringUtils.isNotEmpty(quotationData.getHealthsTxt())) {
            quotationData.setHealths(gson.fromJson(quotationData.getHealthsTxt(), new TypeToken<List<FlexibleSubmitQuestionInput>>() {
            }.getType()));
        }
        QuotationAssuredDto customerAssured = quotationData.getCustomer();
        if (
            //!identifyTypeOrg.equals(customerAssured.getIdentificationType().toString()) ||
                customerAssured.getFullName() == null ||
                !removeAccent(mbCustomer.getFullNameT24().trim()).equalsIgnoreCase(removeAccent(customerAssured.getFullName().trim())) ||
                !reqDto.getIdentificationNumber().equals(customerAssured.getIdentificationNumber())
        ) {
            throw new MiniApiException("Họ và tên không trùng khớp. Vui lòng cập nhật lại!");
        }
        if (QuotationState.COMPLETED.equals(quotationData.getState())) {
            throw new MiniApiException("Hợp đồng đã hoàn thành, vui lòng tạo hợp đồng mới!");
        }

        FlexibleQuotationResp response = quotationData.toResponse();
        FlexibleQuotationResp.CrmDataMatchInfo matchInfo = new FlexibleQuotationResp.CrmDataMatchInfo();
        response.setMatchInfo(matchInfo);

        // 3.1 check customer matched
        boolean isMatched = FlexibleCrmUtil.isMatchedCustomer(mbCustomer, customerAssured);
        matchInfo.setMatched(isMatched);
        if (!isMatched) {
            matchInfo.setMessage("Thông tin của quý khách đang không trùng khớp với thông tin đăng ký với ngân hàng");
        }

        // 3.2 check quotation-date expired
        boolean expired = FlexibleCrmUtil.hasExpiredQuotationData(quotationData.getCreatedDate());
        matchInfo.setExpired(expired);

        // 3.3 must check TSA // && FlexibleCrmUtil.hasMustCheckTSA(quotationDate)
        if (!expired) {
            this.verifyAndMappingTSA(response, quotationData);
        }

        // 4. create & save data for submit
        if (!matchInfo.hasMustRecreate()) {
            flexibleQuotationCaching.setProcessId(mbCustomer.getMbId(), quotationData.getProcessId());
            flexibleQuotationCaching.setQuotationData(mbCustomer.getMbId(), quotationData.getProcessId(), response);
            flexibleQuotationCaching.setQuotationUuid(mbCustomer.getMbId(), quotationData.getProcessId(), quotationData.getUuid()); //for update state tool!
        } else {
            response.setProcessId(null); // must re-create
            if (!QuotationState.UN_MATCH.equals(quotationData.getState())) {
                QuotationSyncDataResp syncResponse = toolCrmApi3rdService.syncData(new QuotationSyncDataReq(quotationData.getUuid(), QuotationAction.UN_MATCH));
                log.info("Sync data tool-crm: toolUuid {}, action={}, state={}", quotationData.getUuid(), QuotationAction.UN_MATCH, syncResponse.getState());
            }
        }
        flexibleQuotationCaching.setDirectInfo(mbCustomer.getMbId(), quotationData.getUuid(), response.getDirectInfo());
        log.info("findQuotationDetail(quotationUid={}, state={}), processId={}", quotationData.getUuid(), quotationData.getState(), quotationData.getProcessId());

        // 5. update customer (NOTE: update when recreate quotation)
        return response;
    }

    private void verifyAndMappingTSA(FlexibleQuotationResp quotationResp, QuotationDetailResp quotationData) {
        try {
            FlexibleQuoteReq createQuoteReq = quotationData.getCreateQuoteReq();
            if (createQuoteReq == null) {
                log.warn("CheckTSAResp createQuoteReq is empty, processId={}, quoteUuid={}", quotationResp.getProcessId(), quotationData.getUuid());
                return;
            }

            FlexibleCheckTSAReq checkTSAReq = new FlexibleCheckTSAReq();
            checkTSAReq.setCustomer(createQuoteReq.getCustomer());
            checkTSAReq.setAssureds(createQuoteReq.getAssureds());
            checkTSAReq.setBeneficiaries(createQuoteReq.getBeneficiaries());
            checkTSAReq.setAdditionalProducts(createQuoteReq.getAdditionalProducts());
            checkTSAReq.setProductPackage(createQuoteReq.getProductPackage());
            checkTSAReq.setAmounts(createQuoteReq.getAmounts());
            checkTSAReq.setRaiderDeductFund(createQuoteReq.isRaiderDeductFund());
            FlexibleCheckTSAResp checkTSAResp = mbalApi3rdService.checkTSA(checkTSAReq);

            if (CollectionUtils.isEmpty(checkTSAResp.getAssureds())) {
                log.warn("CheckTSAResp is empty, processId={}", quotationResp.getProcessId());
                return;
            }

            AtomicBoolean tsaNotMatch = new AtomicBoolean(false);

            // lifeAssured
            Optional<FlexibleCheckTSAResp.Assured> assuredTsa = checkTSAResp.getAssureds().stream().filter(el -> isMatchedIdentify(quotationResp.getLifeAssured(), el)).findFirst();
            assuredTsa.ifPresent(tsa -> {
                if (!Objects.equals(quotationResp.getLifeAssured().getAppQuestionNumber(), tsa.getAppQuestionNumber())) {
                    quotationResp.getLifeAssured().setAppQuestionNumber(tsa.getAppQuestionNumber());
                    tsaNotMatch.set(true);
                }
            });

            // additionalAssuredOutputs
            for (AdditionalAssuredOutput additionAssured : quotationResp.getAdditionalAssuredOutputs()) {
                assuredTsa = checkTSAResp.getAssureds().stream().filter(el -> isMatchedIdentify(additionAssured, el)).findFirst();
                assuredTsa.ifPresent(tsa -> {
                    if (!Objects.equals(additionAssured.getAppQuestionNumber(), tsa.getAppQuestionNumber())) {
                        additionAssured.setAppQuestionNumber(tsa.getAppQuestionNumber());
                        tsaNotMatch.set(true);
                    }
                });
            }

            // assureds
            for (FlexibleCommon.PolicyHolderAndLifeAssured assured : quotationResp.getAssureds()) {
                assuredTsa = checkTSAResp.getAssureds().stream().filter(el -> isMatchedIdentify(assured, el)).findFirst();
                assuredTsa.ifPresent(tsa -> {
                    if (!Objects.equals(assured.getAppQuestionNumber(), tsa.getAppQuestionNumber())) {
                        assured.setAppQuestionNumber(tsa.getAppQuestionNumber());
                        tsaNotMatch.set(true);
                    }
                });
            }
            quotationResp.getMatchInfo().setTsaNotMatch(tsaNotMatch.get());

        } catch (Exception ex) {
            log.warn("CheckTSAResp error, processId={}, detail={}", quotationResp.getProcessId(), ex.getMessage());
            quotationResp.getMatchInfo().setTsaNotMatch(true); //???
            /*quotationResp.getMatchInfo().setMatched(false);
            quotationResp.getMatchInfo().setMessage("Tổng số tiền bảo hiểm của quý khách có sự thay đổi. Quý khách vui lòng trả lời lại câu hỏi sức khoẻ");*/
            throw new ToolCrmApi3rdException(SERVER_ERROR_MESSAGE);
        }
    }


    @Override
    public FlexibleRespModel createProcess(String cif, ProcessFlexibleReqDto reqDto) {
        Customer customer = customerRepository.findByMbId(cif);

        updateCustomerInfo(customer, reqDto.getCustomer());
        validateCustomerInfo(reqDto.getCustomer().getDob(), reqDto.getCustomer().getIdentificationNumber(),
                reqDto.getCustomer().getGender(), reqDto.getCustomer().getIdentificationType());
        HttpEntity<ProcessFlexibleReqDto> entity = new HttpEntity<>(reqDto, mbalBasicAuth);
        List<OccupationV2RespDto> listOccupation = externalV2ApiService.mbalListOccupation();
        List<OccupationV2RespDto> listOccupationList = objectMapper.convertValue(listOccupation, new TypeReference<>() {
        });
        Optional<OccupationV2RespDto> occupationV2RespDto = listOccupationList.stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.equals(o.getId(), Long.valueOf(reqDto.getCustomer().getOccupationId()))).findFirst();
        if (occupationV2RespDto.isPresent()) {
            OccupationV2RespDto occupation = occupationV2RespDto.get();
            if (occupation.getGroupId() != 1 && occupation.getGroupId() != 2 && occupation.getGroupId() != 3 && occupation.getGroupId() != 4) {
                throw new MiniApiException(MSG02);
            }
        }

        if (reqDto.isCustomerIsAssured()) {
            validateMainInsuranceAge(reqDto.getCustomer().getDob());
        }

        FlexibleRespModel response = restCallApi(mbalHostUl3 + MBAL_FLEXIBLE_CREATE_PROCESS, entity, FlexibleRespModel.class, reqDto);
        if (response != null) {
            commonService.setProcessCacheData(processIdKey(cif), String.valueOf(response.getProcessId()));
            Assured assured = new Assured(reqDto.getCustomer());
            assured.setType(ADDITIONAL_LIVES);
            assured.setIdentificationDate(reqDto.getCustomer().getIdentificationDate());
            assured.setIdIssuedPlace(reqDto.getCustomer().getIdIssuedPlace());
            assured.setAnnualIncome(reqDto.getCustomer().getAnnualIncome());
            assured.setIsCustomer(true);
            assured.setRelationshipWithPolicyHolder(POLICY_HOLDER);
            if (reqDto.isCustomerIsAssured()) {
                assured.setType(LIFE_ASSURED);
            }
            commonService.setProcessCacheData(customerAssuredProcessIdKey(cif, String.valueOf(response.getProcessId())), gson.toJson(assured));
//            List<FlexibleAssuredRespDto> assuredRespDtos = addAssureds(cif, response.getProcessId(), new ArrayList<>());
//            response.setAssureds(assuredRespDtos);
        }

        if (reqDto.getToolUid() != null) {
            flexibleQuotationCaching.setQuotationUuid(cif, response.getProcessId(), reqDto.getToolUid()); //for update state tool!
            log.info("createProcess(processId={}) : QuotationDetail(quotationUid={})", response.getProcessId(), reqDto.getToolUid());
        }

        return response;
    }

    @Override
    public List<FlexibleCommon.Assured> addAssureds(String cif, Integer processId, List<FlexibleCommon.Assured> reqDto) {
        verifyCustomerPermission(cif, processId);
        String customerAssuredStr = commonService.getProcessCacheData(customerAssuredProcessIdKey(cif, String.valueOf(processId)));
        if (customerAssuredStr == null) {
            log.error("[MINI]--Thông tin người mua bắt buộc");
            throw new MiniApiException(MSG12);
        }
        Assured assuredCustomer = gson.fromJson(customerAssuredStr, Assured.class);
        reqDto.add(0, assuredCustomer);

        if (reqDto.stream().noneMatch(assuredInput -> assuredInput.getType().equals(LIFE_ASSURED))
                || reqDto.stream().filter(assuredInput -> assuredInput.getType().equals(LIFE_ASSURED)).count() > 1) {
            log.error("[MINI]--Chỉ được phép duy nhất 1 NĐBH chính");
            throw new MiniApiException(FL_MSG45);
        }

        if (reqDto.stream()
                .filter(o -> ADDITIONAL_LIVES.equals(o.getType()))
                .filter(assuredInput -> COUPLE.equals(assuredInput.getRelationshipWithPolicyHolder()))
                .count() > 1) {
            log.error("[MINI]--Chỉ được phép duy nhất 1 NĐBHBS là couple với policy holder");
            throw new MiniApiException(FL_MSG61);
        }

//        if (reqDto.stream()
//                .filter(o -> ADDITIONAL_LIVES.equals(o.getType()))
//                .filter(assuredInput -> COUPLE.equals(assuredInput.getRelationshipWithPolicyHolder()))
//                .anyMatch(assuredInput ->
//                        getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assuredInput.getDob() + ZERO_TIME),
//                                LocalDateTime.now()) < 18)) {
//            log.error("[MINI]--Người có mối quan hệ vợ/chồng phải đủ 18 tuổi with assured data {}, cif {}, processId {}", reqDto, cif, processId);
//            throw new MiniApiException(FL_MSG62);
//        }

//        if (reqDto.stream()
//                .filter(o -> ADDITIONAL_LIVES.equals(o.getType()))
//                .filter(assuredInput -> COUPLE.equals(assuredInput.getRelationshipWithPolicyHolder()))
//                .anyMatch(assuredInput -> assuredInput.getGender().equals(assuredCustomer.getGender()))) {
//            log.error("[MINI]--Người có mối quan hệ vợ/chồng phải là nam-nữ {}, cif {}, processId {}", reqDto, cif, processId);
//            throw new MiniApiException(FL_MSG63);
//        }

        reqDto.stream().filter(o -> o.getType().equals(ADDITIONAL_LIVES))
                .forEach(o -> validateAdditionalInsuranceAge(o.getDob()));
        reqDto.stream()
                .filter(o -> Boolean.FALSE.equals(o.getIsCustomer()))
                .forEach(FlexibleCommon::validateAnnualIncome);
        List<OccupationV2RespDto> listOccupation = externalV2ApiService.mbalListOccupation();
        List<OccupationV2RespDto> occupations = objectMapper.convertValue(listOccupation, new TypeReference<>() {
        });
        reqDto.forEach(o -> validateOccupation(occupations, o.getOccupationId()));
        commonService.setProcessCacheData(fxCifAssuredKey(cif, String.valueOf(processId)), gson.toJson(reqDto));
        Optional<Assured> mainAssured = reqDto.stream().filter(o -> o.getType().equals(LIFE_ASSURED)).findFirst();
        if (mainAssured.isPresent()) {
            Assured assured = mainAssured.get();
            validateMainInsuranceAge(assured.getDob());
            if (Boolean.TRUE.equals(assured.getIsCustomer()) && reqDto.size() > 4) {
                throw new MiniApiException(FL_MSG39);
            }
            validateCustomerInfo(assured.getDob(), assured.getIdentificationNumber(),
                    assured.getGender(), assured.getIdentificationType());
        }
        if (reqDto.size() > 5) {
            throw new MiniApiException(FL_MSG39);
        }
        return reqDto;
    }

    @Override
    public FlexibleQuoteRespDto createQuote(String cif, GenerateQuotationDto reqDto) {
        long t1 = System.currentTimeMillis();
        log.debug("[MINI]--reqDto {}", reqDto);;
        verifyCustomerPermission(cif, reqDto.getProcessId());
        String processCacheData = commonService.getProcessCacheData(fxCifAssuredKey(cif, String.valueOf(reqDto.getProcessId())));

        List<FlexibleCommon.Assured> assuredList = gson.fromJson(processCacheData, new TypeToken<List<FlexibleCommon.Assured>>() {
        }.getType());

        BigDecimal totalMicAmount = BigDecimal.valueOf(0L);
        BigDecimal totalMbalAmount = BigDecimal.valueOf(0L);
        BigDecimal riderAmount;
        BigDecimal topupInsuranceFee = BigDecimal.valueOf(0L);
        String customerAssuredStr = commonService.getProcessCacheData(customerAssuredProcessIdKey(cif, String.valueOf(reqDto.getProcessId())));
        Assured customer = gson.fromJson(customerAssuredStr, Assured.class);

        Map<String, FlexibleCommon.Assured> assuredMap = assuredList.stream().collect(Collectors.toMap(Assured::getMiniAssuredId, Function.identity()));

        List<QuoteAssuredInput> assuredInputs = reqDto.getAssuredInputs();
        OptionalInt indexOpt = IntStream.range(0, assuredInputs.size())
                .filter(i -> customer.getMiniAssuredId().equals(assuredInputs.get(i).getAssuredId()))
                .findFirst();
        if (indexOpt.isPresent()) {
            int indexOptAsInt = indexOpt.getAsInt();
            QuoteAssuredInput assuredInput = assuredInputs.get(indexOptAsInt);
            assuredInputs.remove(indexOptAsInt);
            assuredInputs.add(0, assuredInput);

        }
        OptionalInt coupleRelationship = IntStream.range(0, assuredInputs.size())
                .filter(i -> COUPLE.equals(assuredMap.get(assuredInputs.get(i).getAssuredId())
                        .getRelationshipWithPolicyHolder()))
                .findFirst();
        if (coupleRelationship.isPresent()) {
            int indexOptAsInt = coupleRelationship.getAsInt();
            QuoteAssuredInput assuredInput = assuredInputs.get(indexOptAsInt);
            assuredInputs.remove(indexOptAsInt);
            if (indexOpt.isPresent()) {
                assuredInputs.add(1, assuredInput);
            } else {
                assuredInputs.add(0, assuredInput);
            }
        }
        log.debug("[MINI]--Assured input {}", assuredInputs);
//        assuredInputs.stream().filter(o -> Objects.nonNull(o.getMicRequest()))
//                .forEach(o -> validateAdditionalFxMicInsuranceAge(assuredMap.get(o.getAssuredId()).getDob()));

        List<FlexibleCommon.Assured> assuredMustHasAdditionalProduct = assuredList.stream()
                .filter(o -> o.getType().equals(ADDITIONAL_LIVES))
                .filter(o -> o.getIsCustomer().equals(Boolean.FALSE)).collect(Collectors.toList());

        List<String> assuredIdsInput = assuredInputs.stream().map(QuoteAssuredInput::getAssuredId).distinct().collect(Collectors.toList());
        List<FlexibleCommon.Assured> assuredAdditionalInput = assuredIdsInput.stream().map(assuredMap::get).filter(Objects::nonNull).collect(Collectors.toList());

        List<Assured> countAdditionalLives = assuredAdditionalInput.stream()
                .filter(o -> o.getType().equals(ADDITIONAL_LIVES))
                .filter(o -> o.getIsCustomer().equals(Boolean.FALSE)).collect(Collectors.toList());


        if (countAdditionalLives.size() != assuredMustHasAdditionalProduct.size()) {
            throw new MiniApiException("Người hưởng BHBS phải có ít nhất 1 gói bổ sung MBAL hoặc MIC");
        }

        List<QuoteAssuredInput> mbalProducts = assuredInputs.stream().filter(o -> o.getMbalAdditionalProductInput() != null).collect(Collectors.toList());
//        customer.setAnnualIncome(reqDto.getAnnualIncome()); //validate after
//        boolean anyMatch = mbalProducts.stream().anyMatch(o -> ProductType.HSCR.equals(o.getMbalAdditionalProductInput().getProductType()));
//        if (anyMatch) {
//            if (reqDto.getAnnualIncome() == null) {
//                throw new MiniApiException(FL_MSG47);
//            }
//            customer.setAnnualIncome(reqDto.getAnnualIncome());
//        }
        boolean flag = false;
        LinkedHashMap<FlexibleCommon.Assured, QuoteAssuredOutput> quoteAssuredOutputMap = new LinkedHashMap<>();
        for (QuoteAssuredInput item : assuredInputs) {
            FlexibleCommon.Assured assured = assuredMap.get(item.getAssuredId());
            int ages = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assured.getDob() + ZERO_TIME), LocalDateTime.now());
            // Comment for next phase
            if (item.getMicRequest() == null && item.getMbalAdditionalProductInput() == null) {
                throw new MiniApiException("Bạn phải chọn ít nhất 1 gói BHBS MIC hoặc MBAL.");
            }
//            if (item.getMbalAdditionalProductInput() == null) {
//                throw new MiniApiException("Bạn phải chọn ít nhất 1 gói BHBS MBAL.");
//            }
            if (item.getMicRequest() != null && (POLICY_HOLDER.equals(assured.getRelationshipWithPolicyHolder())
                    || (COUPLE.equals(assured.getRelationshipWithPolicyHolder()) && ages >= 18))) {
                flag = true;
            }

            if (item.getMicRequest() != null && item.getMicRequest().getParentContract() != null
                    && (CHILDREN.equals(assured.getRelationshipWithPolicyHolder()) && ages < 6)) {
                flag = true;
            }
            QuoteAssuredOutput quoteAssuredOutput = quoteAssuredOutputMap.get(assuredMap.get(item.getAssuredId()));
            log.debug("[MINI]--QuoteAssuredOutput {}, item {}, assured {}" , assuredMap, item, assured);
            if (quoteAssuredOutput == null) {
                validateMicRequest(assured, item.getMicRequest());
                quoteAssuredOutputMap.put(assuredMap.get(item.getAssuredId()),
                        new QuoteAssuredOutput()
                                .setMicResult(null)
                                .setMicBenefit(null)
                                .setMicRequest(item.getMicRequest()) // Comment for next phase
                                .setRiders(new ArrayList<>()));
            } else {
                // Comment for next phase
                if (quoteAssuredOutput.getMicRequest() != null && item.getMicRequest() != null) {
                    throw new MiniApiException("Sản phẩm Mic đã được chọn cho Người được bảo hiểm này. Vui lòng chọn sản phẩm Mic với Người được bảo hiểm khác.");
                }
                quoteAssuredOutput.setRiders(new ArrayList<>());
                // Comment for next phase
                if (item.getMicRequest() != null) {
                    validateMicRequest(assured, item.getMicRequest());
                    quoteAssuredOutput.setMicRequest(item.getMicRequest());
                }
            }
        }
        log.debug("[MINI]-- Flag {}", flag);
        FlexibleCommon.Assured mainAssured = assuredList
                .stream()
                .filter(assured -> assured.getType().equals(AssuredType.LIFE_ASSURED)).findFirst()
                .orElseThrow(() -> new MiniApiException(MSG12));
        validateMainProductPackage(customer, mainAssured, reqDto.getProductPackage());
//        mainAssured.setAnnualIncome(reqDto.getAnnualIncome());
        QuoteFlexibleReqDto quoteFlexible = genQuoteFlexibleReqDto(mbalProducts, assuredMap, customer, reqDto, mainAssured);
//        quoteFlexible.getCustomer().setAnnualIncome(reqDto.getAnnualIncome());
        updateCustomerInfoCreateQuote(cif, quoteFlexible.getCustomer());
        HttpEntity<QuoteFlexibleReqDto> entity = new HttpEntity<>(quoteFlexible, mbalBasicAuth);
        String urlCreateQuote = String.format(MBAL_FLEXIBLE_CREATE_QUOTE, reqDto.getProcessId());

        long t2 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds createQuote t2-t1: {}", t2 - t1);
        CreateQuoteFlexibleModel createQuoteFlexibleModel = restCallApi(String.format("%s%s", mbalHostUl3, urlCreateQuote),
                entity, CreateQuoteFlexibleModel.class, reqDto);
        if (createQuoteFlexibleModel == null) {
            log.error("[MBAL]--Body is empty");
            throw new MiniApiException("Tạo bảng minh họa lỗi. Vui lòng thử lại!");
        }
        if (createQuoteFlexibleModel.getCashFlow() == null) {
            log.error("[MBAL]--Quotation not found");
            throw new MiniApiException("Tạo bảng minh họa lỗi. Vui lòng thử lại!");
        }

        FlexibleQuoteRespDto quoteResponse = new FlexibleQuoteRespDto();
        long t3 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds t3-t2: {}", t3 - t2);
        // Comment for next phase
        List<MicPackage> micPackages = micPackageService.findAll();
        List<MicBenefitRespDto> micInsuranceBenefits = micPackages.stream()
                .map(micPackage -> modelMapper.map(micPackage, MicBenefitRespDto.class))
                .collect(Collectors.toList());

        totalMbalAmount = totalMbalAmount.add(createQuoteFlexibleModel.getProductPackage().getPeriodicPremium());
        quoteResponse.setMbalMainAmountStr(formatCurrency(totalMbalAmount));
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
        quoteResponse.setDetails(createQuoteFlexibleModel.getCashFlow().getYearItems().stream().map(item -> {
            QuotationDetailDto detail = new QuotationDetailDto();
            detail.setPolicyYear(item.getPolicyYear());
            detail.setInsuredAge(item.getInsuredAge());
            detail.setBasePremium(item.getBasePremium());
            detail.setTopupPremium(item.getTopupPremium());
            detail.setWithdrawal(item.getWithdrawal());
            buildQuotationDetailWithInterestRate(item, detail);
            buildQuotationDetailWithInvestment(item, detail);
            return detail;
        }).collect(Collectors.toList()));

        Map<FlexibleAssuredKey, PolicyHolderAndLifeAssured> assuredsMap = createQuoteFlexibleModel.getAssureds()
                .stream()
                .collect(Collectors.toMap((i) -> new FlexibleAssuredKey()
                                .setGender(i.getGender())
                                .setFullName(i.getFullName())
                                .setIdentificationType(i.getIdentificationType())
                                .setDob(i.getDob())
                                .setIdentificationNumber(i.getIdentificationNumber()),
                        (i) -> i,
                        (i1, i2) -> i1)
                );

        List<AdditionalAssuredOutput> additionalAssuredOutputs = new ArrayList<>();
        Common.ParentContract parentContract;
        MicGenerateInsuranceCertSandboxRespDto.GenerateInsuranceCertData policyHolderCertSandboxResp = null;
        MicGenerateInsuranceCertSandboxRespDto.GenerateInsuranceCertData coupleCertSandboxResp = null;
        MicAdditionalProduct policyHolderMicRequest = null;
        MicAdditionalProduct coupleMicRequest = null;
        FlexibleCommon.Assured coupleAssured = null;
        for (Map.Entry<FlexibleCommon.Assured, QuoteAssuredOutput> entry : quoteAssuredOutputMap.entrySet()) {
            FlexibleAssuredKey key = new FlexibleAssuredKey(entry.getKey());
            FlexibleCommon.Assured assured = entry.getKey();
            PolicyHolderAndLifeAssured mbalAssured = assuredsMap.get(key);
            QuoteAssuredOutput value = entry.getValue();
            if (assured != null) {
                String assuredId = null;
                if (mbalAssured != null) {
                    assuredId = mbalAssured.getId();
                    value.setRiders(createQuoteFlexibleModel.getRiders().stream()
                            .filter(Objects::nonNull)
                            .filter(o -> o.getAssuredId().equals(mbalAssured.getId())).collect(Collectors.toList()));
                }
                // Comment for next phase
                MicAdditionalProduct micRequest = value.getMicRequest();
                if (micRequest != null) {
                    int ages = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assured.getDob() + ZERO_TIME), LocalDateTime.now());
                    MicBenefitRespDto micBenefit = micInsuranceBenefits.stream().filter(o -> o.getId() == micRequest.getNhom()).findFirst()
                            .orElseThrow(() -> new MicPackageNotFoundException("Không tồn tại gói bảo hiểm MIC."));
                    MicCompareResp hasParentMic = new MicCompareResp();
                    parentContract = micRequest.getParentContract();
                    if (flag && ages < 6 && (policyHolderCertSandboxResp != null || coupleCertSandboxResp != null || parentContract != null)
                            && CHILDREN.equals(assured.getRelationshipWithPolicyHolder())) {
                        log.info("Assured apply policy with name {} and age: {}", assured.getFullName(), ages);
                        if (parentContract != null) {
                            try {
                                MicSandboxContractInfoRespDto parentContractInfo = micAPIService.micSandboxSearchContractInfo(parentContract.getSo_hd_bm(),
                                        DateUtil.localDateTimeToString(DateUtil.DATE_DMY, LocalDateTime.now()), true);
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
                        micRequest.setParentContract(null);
                    }
                    String micTransactionId = generateUUIDId(30);
                    MicSandboxFeeCareRespDto micFeeResult = micAPIService.flexibleMicSandboxFee(assured, micRequest);
                    value.setMicResult(micFeeResult);
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

                    value.setMicBenefit(micBenefit);

                    micFeeResult.setMicTransactionId(micTransactionId);
                    micFeeResult.setMicSumBenefit(BigDecimal.valueOf(micMainBenefit));
                    // MIC generate GCNBH
                    MicGenerateInsuranceCertReqDto.GcnMicCareTtinkh ttinkh = micAPIService.generateMicSandboxTtinkh(
                            customer,
                            assured
                    );

                    if (POLICY_HOLDER.equals(assured.getRelationshipWithPolicyHolder()) && flag) {
                        policyHolderMicRequest = micRequest;

                        MicGenerateInsuranceCertSandboxRespDto respDto = micAPIService.micSandboxGenerateCert(
                                ttinkh, micFeeResult.getPhi().longValue(),
                                micTransactionId, micRequest.getNhom(), new Common.GcnMicCareDkbs()
                                        .setBs1(micRequest.getBs1())
                                        .setBs2(micRequest.getBs2())
                                        .setBs3(micRequest.getBs3())
                                        .setBs4(micRequest.getBs4()),
                                micRequest.getParentContract());

                        log.info("[MINI]--Thông tin GCNBH khách hàng {}", respDto);
                        if (Objects.nonNull(respDto.getData())) {
                            policyHolderCertSandboxResp = respDto.getData();
                        }

                    } else if (COUPLE.equals(assured.getRelationshipWithPolicyHolder()) && flag && ages >= 18) {
                        coupleMicRequest = micRequest;

                        MicGenerateInsuranceCertSandboxRespDto coupleMicRespDto = micAPIService.micSandboxGenerateCert(
                                ttinkh, micFeeResult.getPhi().longValue(),
                                micTransactionId, micRequest.getNhom(), new Common.GcnMicCareDkbs()
                                        .setBs1(micRequest.getBs1())
                                        .setBs2(micRequest.getBs2())
                                        .setBs3(micRequest.getBs3())
                                        .setBs4(micRequest.getBs4()),
                                micRequest.getParentContract());
                        log.info("[MINI]--Thông tin GCNBH vợ/chồng {}", coupleMicRespDto);
                        if (Objects.nonNull(coupleMicRespDto.getData())) {
                            coupleCertSandboxResp = coupleMicRespDto.getData();
                            coupleAssured = assured;
                        }

                    } else {
                        if (flag && ages < 6
                                && (policyHolderCertSandboxResp != null || coupleCertSandboxResp != null || parentContract != null)
                                && CHILDREN.equals(assured.getRelationshipWithPolicyHolder()) && hasParentMic.isResult()) {
                            switch (hasParentMic.getCompareType()) {
                                case POLICY_HOLDER:
                                    micRequest.getParentContract().setSo_hd_bm(policyHolderCertSandboxResp == null ? "" : policyHolderCertSandboxResp.getSo_hd());
                                    break;
                                case COUPLE_INSURED:
                                    micRequest.getParentContract().setSo_hd_bm(coupleCertSandboxResp == null ? "" : coupleCertSandboxResp.getSo_hd());
                                    break;
                                default:
                                    micRequest.getParentContract().setSo_hd_bm("");
                            }
                        }
                        micAPIService.micSandboxGenerateCert(ttinkh, micFeeResult.getPhi().longValue(),
                                micTransactionId, micRequest.getNhom(), new Common.GcnMicCareDkbs()
                                        .setBs1(micRequest.getBs1())
                                        .setBs2(micRequest.getBs2())
                                        .setBs3(micRequest.getBs3())
                                        .setBs4(micRequest.getBs4()),
                                micRequest.getParentContract());
                    }
                    totalMicAmount = totalMicAmount.add(micFeeResult.getPhi());
                }
                if (LIFE_ASSURED.equals(assured.getType()) && mbalAssured != null) {
                    mbalAssured.setAdditionalProduct(value);
                    assuredsMap.putIfAbsent(key, mbalAssured);
                }
                if (ADDITIONAL_LIVES.equals(assured.getType())) {
                    additionalAssuredOutputs.add(new AdditionalAssuredOutput()
                            .setAssured(assured.setId(assuredId))
                            .setAdditionalProduct(value));
                }
            }
        }

        long t4 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds createQuote t4-t3: {}", t4 - t3);
//        quoteResponse.setFromCrmData(reqDto.isFromCrmData()); /*SET isFromCrmData*/
        quoteResponse.setApplicationStatus(createQuoteFlexibleModel.getApplicationStatus());
        quoteResponse.setQuotationStatus(createQuoteFlexibleModel.getQuotationStatus());
        quoteResponse.setProductPackage(createQuoteFlexibleModel.getProductPackage());
        quoteResponse.setPolicyHolder(createQuoteFlexibleModel.getPolicyHolder()
                .setMiniAssuredId(customer.getMiniAssuredId()));
        quoteResponse.setAdditionalAssuredOutputs(additionalAssuredOutputs);
        quoteResponse.setLifeAssured(assuredsMap.get(new FlexibleAssuredKey(mainAssured))
//                    .setAdditionalProduct(quoteAssuredOutputMap.get(mainAssured))
                .setMiniAssuredId(mainAssured.getMiniAssuredId()));
        quoteResponse.setQuotationDate(createQuoteFlexibleModel.getQuotationDate());
        quoteResponse.setProductPackageCode(createQuoteFlexibleModel.getProductPackageCode());
        quoteResponse.setSale(createQuoteFlexibleModel.getSale());
        quoteResponse.setReferer(createQuoteFlexibleModel.getReferrer());
        quoteResponse.setReferrer(createQuoteFlexibleModel.getReferrer());
        quoteResponse.setRefererInput(reqDto.getReferer());
        quoteResponse.setSupporter(createQuoteFlexibleModel.getSupporter());
        quoteResponse.setProcessId(createQuoteFlexibleModel.getProcessId());
        quoteResponse.setApplicationNumber(createQuoteFlexibleModel.getApplicationNumber());


        BigDecimal totalAmount = totalMbalAmount.add(totalMicAmount);
        quoteResponse.setTotalAmount(totalAmount);
        quoteResponse.setTotalAmountStr(formatCurrency(totalAmount));
        quoteResponse.setTotalMbalAmountStr(formatCurrency(totalMbalAmount));
        quoteResponse.setTotalMicAmountStr(totalMicAmount.longValue() == 0 ? null : formatCurrency(totalMicAmount));
        quoteResponse.setRiderAmountStr(formatCurrency(riderAmount));
        quoteResponse.setTopupAmountStr(formatCurrency(topupInsuranceFee));

        quoteResponse.setCustomer(quoteFlexible.getCustomer());
        quoteResponse.setAmounts(createQuoteFlexibleModel.getAmounts() == null ? null : createQuoteFlexibleModel.getAmounts()
                .stream().map(o -> modelMapper.map(o, QuotationAmountRespDto.class)).collect(Collectors.toList()));
        quoteResponse.setRaiderDeductFund(createQuoteFlexibleModel.isRaiderDeductFund());

        if (createQuoteFlexibleModel.getBeneficiaries() != null && !createQuoteFlexibleModel.getBeneficiaries().isEmpty()) {
            BeneficiaryOutput beneficiary = createQuoteFlexibleModel.getBeneficiaries().get(0);
            beneficiary.setPhoneNumber(reqDto.getBeneficiary().getPhoneNumber());
            beneficiary.setAddress(reqDto.getBeneficiary().getAddress());
            beneficiary.setEmail(reqDto.getBeneficiary().getEmail());
            createQuoteFlexibleModel.getBeneficiaries().set(0, beneficiary);
        }

        quoteResponse.setBeneficiaries(createQuoteFlexibleModel.getBeneficiaries());
//            quoteResponse.setMicProductModels(micProductModels);
        long t5 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds createQuote t5-t4: {}", t5 - t4);

        UUID toolQuotationUuid = flexibleQuotationCaching.getQuotationUuid(cif, reqDto.getProcessId());
        if (toolQuotationUuid != null) {
            if (quoteResponse.getMatchInfo() != null) quoteResponse.getMatchInfo().setRecreated(true);
            else log.error("Crm-data match info not found, createQuote toolQuotationUuid {}", toolQuotationUuid);
        }
        commonService.setProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(reqDto.getProcessId())), gson.toJson(quoteResponse));
        if (toolQuotationUuid != null) {
            QuotationSyncDataResp syncResponse = toolCrmApi3rdService.syncData(new QuotationSyncDataReq(toolQuotationUuid, QuotationAction.CREATED));
            log.info("Sync data tool-crm: toolUuid {}, action={}, state={}", toolQuotationUuid, QuotationAction.CREATED, syncResponse.getState());
        }
        log.debug("createQuote(processId={}) : QuotationDetail(quotationUid={})", reqDto.getProcessId(), toolQuotationUuid);
        return quoteResponse;
    }

    @Override
    public String sendEmail(String cif, SendMailFlexibleReqDto reqDto) {
        verifyCustomerPermission(cif, reqDto.getProcessId());
        boolean invalidParam = false;
        try {
            String quoteResponse = commonService.getProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(reqDto.getProcessId())));
            FlexibleQuoteRespDto quoteRespDto = gson.fromJson(quoteResponse, FlexibleQuoteRespDto.class);
            invalidParam = quoteRespDto.getCustomer() == null || quoteRespDto.getCustomer().getEmail() == null ||
                    !quoteRespDto.getCustomer().getEmail().equalsIgnoreCase(reqDto.getRecipient());
            if (invalidParam) {
                log.error("Data invalid, send email for cif {} and processId {}", cif, reqDto.getProcessId());
                throw new SendEmailException("Dữ liệu không chính xác");
            }

            String responseCache = commonService.getProcessCacheData(sendEmailProcessIdKey(cif, String.valueOf(reqDto.getProcessId())));
            if (EMAIL_SUCCESS.equals(responseCache)) {
                log.error("Spam send email multi time for cif {} and processId {}", cif, reqDto.getProcessId());
                return "Successfully";
            }

            log.info("[MINI]--Main flow send email for processId {}", reqDto.getProcessId());
            applicationEventPublisher.publishEvent(new SendEmailEvent()
                    .setRecipient(reqDto.getRecipient())
                    .setCif(cif)
                    .setProcessId(reqDto.getProcessId()));
            return responseCache;
        } catch (ApplicationException e) {
            if (invalidParam) return "Dữ liệu không chính xác";

            applicationEventPublisher.publishEvent(new SendEmailEvent()
                    .setRecipient(reqDto.getRecipient())
                    .setCif(cif)
                    .setProcessId(reqDto.getProcessId()));
            String emailResponse = commonService.getProcessCacheDataNotThrow(sendEmailProcessIdKey(cif, String.valueOf(reqDto.getProcessId())));
            if (emailResponse == null) {
                log.info("[MINI]--Waiting get response for processId {}", reqDto.getProcessId());
                return "Sending";
            }
            return emailResponse;
        }
    }

    @Override
    public FlexibleQuoteRespDto confirmQuote(String cif, ConfirmQuoteFlexibleReqDto reqDto) {
        long t1 = System.currentTimeMillis();
        verifyCustomerPermission(cif, reqDto.getProcessId());
        String quoteResponse = commonService.getProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(reqDto.getProcessId())));
        FlexibleQuoteRespDto quoteRespDto = gson.fromJson(quoteResponse, FlexibleQuoteRespDto.class);
        FlexibleCommon.PolicyHolderAndLifeAssured mainAssured = quoteRespDto.getLifeAssured();

        HttpEntity<String> entity = new HttpEntity<>(null, mbalBasicAuth);
        String urlConfirmQuote = String.format(MBAL_FLEXIBLE_CONFIRM_QUOTE, reqDto.getProcessId());
        long t2 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds confirmQuote t2-t1: {}", t2 - t1);
        FlexibleRespModel confirmQuote = restCallApi(String.format("%s%s", mbalHostUl3, urlConfirmQuote), entity, FlexibleRespModel.class, reqDto);
        log.debug("[Mini]-- confirmQuote {}", confirmQuote);
        long t3 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds confirmQuote t3-t2: {}", t3 - t2);
        if (confirmQuote == null) {
            log.error("[MBAL]--Confirm quote lỗi với processId {}", reqDto.getProcessId());
            throw new MiniApiException(MSG12);
        }
        List<PolicyHolderAndLifeAssured> assureds = confirmQuote.getAssureds();

        Map<String, PolicyHolderAndLifeAssured> assuredMap = assureds.stream().collect(toMap(PolicyHolderAndLifeAssured::getId, Function.identity()));
        Optional<PolicyHolderAndLifeAssured> lifeAssured = assureds.stream().filter(o -> o.getId().equals(mainAssured.getId())).findFirst();
        List<AdditionalAssuredOutput> additionalAssuredOutputs = quoteRespDto.getAdditionalAssuredOutputs();
        for (AdditionalAssuredOutput assuredOutput : additionalAssuredOutputs) {
            assuredOutput.setAppQuestionNumber(assuredMap.get(assuredOutput.getAssured().getId()) == null ? null : assuredMap.get(assuredOutput.getAssured().getId()).getAppQuestionNumber());
        }
        mainAssured.setAppQuestionNumber(lifeAssured.orElseThrow(() -> new MiniApiException(MSG12)).getAppQuestionNumber());
        quoteRespDto.setApplicationNumber(confirmQuote.getApplicationNumber());
        commonService.setProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(reqDto.getProcessId())), gson.toJson(quoteRespDto));

        UUID toolQuotationUuid = flexibleQuotationCaching.getQuotationUuid(cif, reqDto.getProcessId());
        log.debug("confirmQuote:QuotationDetail(quotationUid={})", toolQuotationUuid);
        if (toolQuotationUuid != null) {
            QuotationSyncDataResp syncResponse = toolCrmApi3rdService.syncData(new QuotationSyncDataReq(toolQuotationUuid, QuotationAction.CONFIRMED));
            log.info("Sync data tool-crm: processId={}, toolUuid={}, action={}, state={}", reqDto.getProcessId(), toolQuotationUuid, QuotationAction.CONFIRMED, syncResponse.getState());
        }
        log.debug("[Mini]-- confirmQuote data  {}", quoteRespDto);
        return quoteRespDto;
    }

    @Override
    public FlexibleRespModel submitApp(String cif, SubmitQuotationDto reqDto) {
        verifyCustomerPermission(cif, reqDto.getProcessId());
        String quoteResponse = commonService.getProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(reqDto.getProcessId())));
        FlexibleQuoteRespDto quoteRespDto = gson.fromJson(quoteResponse, FlexibleQuoteRespDto.class);

        UUID toolQuotationUuid = flexibleQuotationCaching.getQuotationUuid(cif, reqDto.getProcessId());
        if (toolQuotationUuid != null) {
            if (quoteRespDto.getMatchInfo() == null) {
                log.error("Crm-data match info not found, submitApp toolQuotationUuid {}", toolQuotationUuid);
            } else if (!quoteRespDto.getMatchInfo().isRecreated()) { // tại đây: chỉ update cho luồng: quét QR => submit
                updateCustomerInfoCreateQuote(cif, quoteRespDto.getCustomer());
            }
        }

        Integer processId = quoteRespDto.getProcessId();
        verifyCustomerPermission(cif, processId);
        Assured lifeAssured = new Assured(quoteRespDto.getLifeAssured());
        List<Assured> assuredList = quoteRespDto.getAdditionalAssuredOutputs().stream().map(AdditionalAssuredOutput::getAssured).collect(toList());
        assuredList.add(lifeAssured);
//        Map<String, Gender> genderMap = assuredList.stream().collect(toMap(Assured::getId, Assured::getGender, (first, second) -> first));
        Map<String, Assured> assuredMap = assuredList.stream().collect(toMap(Assured::getId, Function.identity(), (first, second) -> first));
        SubmitFlexibleReqDto submitFlexibleReqDto = new SubmitFlexibleReqDto();
        submitFlexibleReqDto.setSale(quoteRespDto.getSale());

        //submitFlexibleReqDto.setReferrer(quoteRespDto.getRefererInput());
        if (quoteRespDto.getRefererInput() != null) {
            submitFlexibleReqDto.setReferrer(quoteRespDto.getRefererInput().to3rdInput());
        } else {
            submitFlexibleReqDto.setReferrer(new Referrer3rdInput());
        }
        submitFlexibleReqDto.setSupporter(quoteRespDto.getSupporter());
        submitFlexibleReqDto.setCustomer(reqDto.getCustomer());
        submitFlexibleReqDto.setAssureds(reqDto.getHealths().stream()
                .map(o -> genHealthInfoRequest(o, assuredMap)).collect(toList()));
        HttpEntity<SubmitFlexibleReqDto> entity = new HttpEntity<>(submitFlexibleReqDto, mbalBasicAuth);
        String urlSubmitApp = String.format(MBAL_FLEXIBLE_SUBMIT_APP, reqDto.getProcessId());
        long t1 = System.currentTimeMillis();
        FlexibleRespModel submitFlexible = restCallApi(String.format("%s%s", mbalHostUl3, urlSubmitApp), entity, FlexibleRespModel.class, reqDto);
        long t2 = System.currentTimeMillis();
        log.debug("[Mini]--Milli seconds submitApp t2-t1: {}", t2 - t1);
        if (submitFlexible == null) {
            throw new MiniApiException(MSG12);
        }

        try {
            boolean isNew = true;
            String insuranceRequestIdCache = commonService.getProcessCacheDataNotThrow(fxInsuranceRequestKey(cif, String.valueOf(processId)));
            if (insuranceRequestIdCache != null && insuranceRequestRepository.findById(Long.valueOf(insuranceRequestIdCache)).isPresent()) {
                isNew = false;
            }
            InsuranceRequest insuranceRequest = savingInsuranceRequestAndIllustration(cif, processId, quoteRespDto, insuranceRequestIdCache, reqDto.getHealths());
            InsurancePayment insurancePayment = insurancePaymentRepository.findByInsuranceRequest(insuranceRequest.getId());
            if (insurancePayment != null) {
                log.error("[MINI]--Không thể submit khi đã khởi tạo giao dịch thanh toán với processId {}", processId);
                throw new MiniApiException(MSG12);
            }
            long t3 = System.currentTimeMillis();
            log.debug("[Mini]--Milli seconds submitApp t3-t2: {}", t3 - t2);
            saveInsuredAndProduct(quoteRespDto, insuranceRequest, cif, reqDto, isNew, reqDto);
            long t4 = System.currentTimeMillis();
            log.debug("[Mini]--Milli seconds submitApp t4-t3: {}", t4 - t3);
            quoteRespDto.setInsuranceRequestId(insuranceRequest.getId());
        } catch (Exception e) {
            log.error("[MINI]--Lỗi khi lưu thông tin BMH và YCBH với detail error {}", e.getMessage());
            throw new MiniApiException(MSG12);
        }
        commonService.setProcessCacheData(processCifCreateQuoteIdKey(cif, String.valueOf(reqDto.getProcessId())), gson.toJson(quoteRespDto));

        if (toolQuotationUuid != null) {
            DirectInfo directInfo = flexibleQuotationCaching.getDirectInfo(cif, toolQuotationUuid);
            // submit KHTN
            DirectSubmitStatusReq directSubmitStatus = null;
            if (directInfo != null) {
                String leadId = directInfo.getLeadId();

                MbalPotentialCustomerDto customerDto = MbalPotentialCustomerDto.of(quoteRespDto.getCustomer(), cif);

                FlexibleCommon.RefererInput rmInfo = quoteRespDto.getRefererInput();
                FlexibleCommon.Referrer3rdInput icInfo = quoteRespDto.getSupporter();

                MbalRMDto rm = new MbalRMDto(rmInfo.getCode(), rmInfo.getName(), rmInfo.getPhoneNumber(),
                        rmInfo.getEmail(), new MbalBranchDto(rmInfo.getBranchCode(), rmInfo.getBranchName()));
                MbalICDto ic = new MbalICDto(icInfo.getCode(), icInfo.getName(), icInfo.getPhoneNumber(),
                        new MbalBranchDto(rmInfo.getBranchCode(), rmInfo.getBranchName()));

                BigDecimal mbalTotalAmount = convertToBigDecimal(quoteRespDto.getTotalMbalAmountStr());
                ProductInfoDto productInfoDto = new ProductInfoDto(mbalTotalAmount, directInfo.getProductPackage(),
                        directInfo.getNote());
                
                SubmitPotentialCustomerReq req = new SubmitPotentialCustomerReq(leadId,
                        quoteRespDto.getApplicationNumber(), customerDto, rm, ic, List.of(productInfoDto));

                directSubmitStatus = mbalApi3rdService.submitPotentialCustomer(req);
            }

            ////////////////SYNC DATA
            List<FlexibleSubmitQuestionInput> healths = reqDto.getHealths().stream().map(el -> modelMapper.map(el, FlexibleSubmitQuestionInput.class)).collect(toList());
            String healthsJson = reqDto.getHealths() != null ? gson.toJson(healths) : null;

            QuotationSyncDataResp syncResponse = toolCrmApi3rdService.syncData(new QuotationSyncDataReq(toolQuotationUuid, QuotationAction.SUBMITTED, healthsJson, directSubmitStatus));
            log.info("Sync data tool-crm: processId={}, toolUuid={}, action={}, state={}", reqDto.getProcessId(), toolQuotationUuid, QuotationAction.SUBMITTED, syncResponse.getState());
            if (syncResponse.isSuccess()) {
                flexibleQuotationCaching.delQuotationUuid(cif, reqDto.getProcessId());
            }
        }

        return submitFlexible.toResponse();
    }


    // Call in baas service
    @Override
    public PaymentNotificationFlexibleRespDto paymentNotification(PaymentNotificationFlexibleReqDto reqDto) {
        HttpEntity<PaymentNotificationFlexibleReqDto> entity = new HttpEntity<>(reqDto, mbalBasicAuth);
        ResponseEntity<PaymentNotificationFlexibleRespDto> exchange = restTemplate.exchange(mbalHostUl3 + MBAL_FLEXIBLE_NOTICE_PAYMENT, HttpMethod.POST, entity, PaymentNotificationFlexibleRespDto.class);
        return exchange.getBody();
    }

    @Override
    public MbalIllustrationDownloadRespDto getPDFQuotation(String cif, Integer processId) {
        verifyCustomerPermission(cif, processId);

        HttpEntity<SendMailV2ReqDto> entity = new HttpEntity<>(null, mbalBasicAuth);
        String urlGetPdf = String.format(MBAL_FLEXIBLE_GET_PDF, processId);
        ResponseEntity<byte[]> result = restTemplate.exchange(String.format("%s%s", mbalHostUl3, urlGetPdf), HttpMethod.GET, entity, byte[].class);
        byte[] body = result.getBody();
        return new MbalIllustrationDownloadRespDto(body);
    }

    @Override
    public PolicyDetailRespDto mbalPolicyDetail(String cif, PolicyDetailReqDto reqDto) {
        // validate check pentest input data
        HttpEntity<String> entity = new HttpEntity<>(null, mbalBasicAuth);
        String urlPolicyDetail = String.format("%s%s", mbalHostUl3, MBAL_FLEXIBLE_GET_POLICY);
        String urlWithParam = UriComponentsBuilder.fromHttpUrl(urlPolicyDetail)
                .queryParam("appNumber", "{appNumber}")
                .queryParam("policyNumber", "{policyNumber}")
                .encode()
                .toUriString();
        Map<Object, Object> params = new HashMap<>();
        params.put("appNumber", reqDto.getAppNumber());
        params.put("policyNumber", reqDto.getPolicyNumber());
        try {
            return restTemplate.exchange(urlWithParam, HttpMethod.GET, entity, PolicyDetailRespDto.class, params).getBody();
        } catch (Exception e) {
            log.error("[MBAL]--Lỗi khi call MBAL policy {}", e.getMessage());
            throw new MbalApiException(MSG12);
        }
    }

    @Override
    public InsuranceRequest savingInsuranceRequestAndIllustration(String cif, Integer processId, FlexibleQuoteRespDto flexibleProcessResp,
                                                                  String insuranceRequestIdCache, List<FlexibleSubmitQuestionRequest> healths) {
        InsuranceRequest insuranceRequest = insuranceRequestIdCache != null ? insuranceRequestRepository.findById(Long.valueOf(insuranceRequestIdCache)).orElse(null) : null;
        if (insuranceRequest != null) {
            log.debug("[MINI]--Get cache insurance request with id {}", insuranceRequestIdCache);
            return insuranceRequest;
        }

        insuranceRequest = new InsuranceRequest();
        IllustrationTable illustrationTable = new IllustrationTable();

        Customer customer = customerRepository.findByMbId(cif);

        InsurancePackage insurancePackage = insurancePackageRepository.findById(23)
                .orElseThrow(() -> new ApplicationException("Không tồn tại Gói bảo hiểm Flexible"));

        insuranceRequest.setProcessId(Long.valueOf(processId));
        insuranceRequest.setCustomer(customer);
        illustrationTable.setCustomer(customer);
        illustrationTable.setInsurancePackage(insurancePackage);
        insuranceRequest.setInsurancePackage(insurancePackage);
        insuranceRequest.setPackageType(Constants.PackageType.FLEXIBLE);

        genFlexibleIllustrationTable(illustrationTable, flexibleProcessResp);
        IllustrationTable illustrationTableSaved = illustrationTableRepository.saveAndFlush(illustrationTable);

        // save YCBH
        insuranceRequest.setCustomer(customer);
        insuranceRequest.setIllustrationTable(illustrationTable);

        // set status
        insuranceRequest.setStatus(true);
        for (FlexibleSubmitQuestionRequest eachHealth : healths) {
            FlexibleSubmitQuestionRequest.ComboBigQuestion comboBigQuestion = eachHealth.getComboBigQuestion();
            if (comboBigQuestion != null && YES.equals(comboBigQuestion.getOqs())) {
                insuranceRequest.setStatus(false);
            }

            FlexibleSubmitQuestionRequest.ComboSmallQuestion comboSmallQuestion = eachHealth.getComboSmallQuestion();
            if ((comboSmallQuestion != null) && (YES.equals(comboSmallQuestion.getC1()) || YES.equals(comboSmallQuestion.getC2()) ||
                    YES.equals(comboSmallQuestion.getC3()) || YES.equals(comboSmallQuestion.getOqs()))) {
                insuranceRequest.setStatus(false);
            }
        }
        // saving customer detail
        CustomerDetail customerDetail = commonService.generateCustomerDetail(customer, flexibleProcessResp);
        customerDetail.setCreationTime(LocalDateTime.now());
        customerDetailRepository.saveAndFlush(customerDetail);
        insuranceRequest.setCustomerDetail(customerDetail);

        UUID toolQuotationUuid = flexibleQuotationCaching.getQuotationUuid(cif, flexibleProcessResp.getProcessId());
        if (toolQuotationUuid != null) {
            insuranceRequest.setCrmToolUuid(toolQuotationUuid); /*Ghi vết cho data từ CRM*/
        }

        insuranceRequestRepository.saveAndFlush(insuranceRequest);

        List<PavTable> pavTables = new ArrayList<>();
        for (QuotationDetailDto quotationDetailDto : flexibleProcessResp.getDetails()) {
            PavTable pavTable = buildPavTable(illustrationTableSaved, quotationDetailDto);
            pavTables.add(pavTable);
        }
        pavTablesRepository.saveAll(pavTables);

        log.info("Saved insurance request for customer ID {}, Illustration Table Id = {}", cif, illustrationTable.getIllustrationNumber());

        //Lưu cache YCBH và BMH ID để update
        commonService.setProcessCacheData(fxInsuranceRequestKey(cif, String.valueOf(processId)), String.valueOf(insuranceRequest.getId()));
        return insuranceRequest;
    }

    @Override
    public UploadFileRespDto uploadMultiFileQuestion(MultipartFile[] files, Integer processId, String cif) {
        verifyCustomerPermission(cif, processId);
        UploadFileRespDto uploadFileRespDto = uploadFileService.uploadMultiPathFileAzure(files);
        if (uploadFileRespDto.getUploadImageInfos().stream().anyMatch(uploadImageInfo -> uploadImageInfo.getCode() == 400)) {
            throw new UploadFileException(FL_MSG36);
        }
        // call to MBAL to upload file
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    map.add("files", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientIdUl3, clientSecretUl3);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String urlUploadFiles = String.format(MBAL_FLEXIBLE_UPLOAD_FILES, processId);
            String url = String.format("%s%s", mbalHostUl3, urlUploadFiles);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            UploadMultiFileQuestionRespDto respDto = restTemplate.postForObject(url, requestEntity, UploadMultiFileQuestionRespDto.class);
            log.info("[MBAL]--Upload files to MBAL result {}", respDto);
        } catch (Exception e) {
            log.error("[MBAL]--uploadMultiFileQuestion exception all: " + e.getMessage());
            throw new MBApiException(MSG12);
        }
        return uploadFileRespDto;
    }

    private void saveInsuredAndProduct(FlexibleQuoteRespDto quoteRespDto, InsuranceRequest insuranceRequest,
                                       String cif, SubmitQuotationDto submitQuotation, boolean isNew, SubmitQuotationDto reqDto) {

        try {
            Map<String, FlexibleSubmitQuestionRequest> questionRequestMap = submitQuotation.getHealths()
                    .stream().collect(toMap(FlexibleSubmitQuestionRequest::getAssuredId, Function.identity()));

            // Lưu NĐBH chính
            PolicyHolderAndLifeAssured lifeAssured = quoteRespDto.getLifeAssured();
            PolicyHolderAndLifeAssured policyHolder = quoteRespDto.getPolicyHolder();
            List<AdditionalAssuredOutput> additionalAssuredOutputs = quoteRespDto.getAdditionalAssuredOutputs();
            Map<String, AdditionalAssuredOutput> assuredOutputMap = additionalAssuredOutputs.stream()
                    .collect(toMap(o -> o.getAssured().getMiniAssuredId(), Function.identity()));

            List<OccupationV2RespDto> listOccupation = externalV2ApiService.mbalListOccupation();
            List<OccupationV2RespDto> occupations = objectMapper.convertValue(listOccupation, new TypeReference<>() {
            });
            Map<String, FlexibleSubmitMicQuestionRequest.MicQuestion> micQuestionMap = reqDto.getMicHealths().stream()
                    .collect(toMap(FlexibleSubmitMicQuestionRequest::getMiniAssuredId, FlexibleSubmitMicQuestionRequest::getMicQuestion));
            Map<Long, String> mapIdJobName = occupations.stream().collect(toMap(OccupationV2RespDto::getId, OccupationV2RespDto::getNameVn));

            if (!isNew) {
                savePrimaryInsured(insuranceRequest, lifeAssured, policyHolder, questionRequestMap.get(lifeAssured.getId()),
                        false, mapIdJobName, micQuestionMap.get(lifeAssured.getMiniAssuredId()));
                for (AdditionalAssuredOutput eachAdditionalAssured : additionalAssuredOutputs) {
                    //Comment for next phase: Validate when have MIC package
                    saveAdditionalInsured(insuranceRequest, eachAdditionalAssured,
                            questionRequestMap.get(eachAdditionalAssured.getAssured().getId()), false, mapIdJobName,
                            false, micQuestionMap.get(eachAdditionalAssured.getAssured().getMiniAssuredId()));
                }
                return;
            }
            PrimaryInsured primaryInsuredSaved = savePrimaryInsured(insuranceRequest, lifeAssured, policyHolder,
                    questionRequestMap.get(lifeAssured.getId()), true, mapIdJobName, micQuestionMap.get(lifeAssured.getMiniAssuredId()));

            // Lưu gói bổ sung của NĐBH chính
            if (lifeAssured.getAdditionalProduct() != null) {
                AdditionalAssuredOutput additionalAssuredOutput = new AdditionalAssuredOutput().setAdditionalProduct(lifeAssured.getAdditionalProduct());
                saveAdditionalProducts(insuranceRequest, primaryInsuredSaved, null, null, additionalAssuredOutput);
            }

            // lưu gói BH chính
            ProductPackageOutput productPackage = quoteRespDto.getProductPackage();
            if (productPackage != null) {
                savePrimaryProduct(insuranceRequest, primaryInsuredSaved, productPackage);
            }

            // Lưu NĐBHBS và gói BHBS
            for (AdditionalAssuredOutput eachAdditionalAssured : additionalAssuredOutputs) {
                if (eachAdditionalAssured.getAssured().getMiniAssuredId().equals(policyHolder.getMiniAssuredId())) {
                    AdditionalInsured additionalInsuredSaved = saveAdditionalInsured(insuranceRequest, eachAdditionalAssured,
                            questionRequestMap.get(eachAdditionalAssured.getAssured().getId()), true, mapIdJobName, true,
                            micQuestionMap.get(eachAdditionalAssured.getAssured().getMiniAssuredId()));
                    // Lưu gói bổ sung của BMBH
                    Customer customerByMbId = customerRepository.findByMbId(cif);
                    saveAdditionalProducts(insuranceRequest, null, customerByMbId, additionalInsuredSaved,
                            assuredOutputMap.get(eachAdditionalAssured.getAssured().getMiniAssuredId()));
                } else {
                    AdditionalInsured additionalInsuredSaved = saveAdditionalInsured(insuranceRequest, eachAdditionalAssured,
                            questionRequestMap.get(eachAdditionalAssured.getAssured().getId()), true, mapIdJobName, false,
                            micQuestionMap.get(eachAdditionalAssured.getAssured().getMiniAssuredId()));
                    saveAdditionalProducts(insuranceRequest, null, null, additionalInsuredSaved,
                            assuredOutputMap.get(eachAdditionalAssured.getAssured().getMiniAssuredId()));
                }
            }

            // Lưu người thụ hưởng
            List<FlexibleCommon.BeneficiaryOutput> beneficiaries = quoteRespDto.getBeneficiaries();
            if (beneficiaries != null && !beneficiaries.isEmpty()) {
                saveListBeneficiary(insuranceRequest, beneficiaries, mapIdJobName);
            }
        } catch (Exception ex) {
            log.error("[MINI]--Lỗi khi lưu thông tin NDBH, gói BHBS cho cif {}. Detail Error {} ", cif, ex.getMessage());
            throw new MiniApiException(MSG12);
        }
    }

    private void saveListBeneficiary(InsuranceRequest insuranceRequest, List<FlexibleCommon.BeneficiaryOutput> beneficiaries, Map<Long, String> mapIdJobName) {
        List<Beneficiary> beneficiaryToSave = new ArrayList<>();
        for (FlexibleCommon.BeneficiaryOutput beneficiaryInput : beneficiaries) {
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setFullName(beneficiaryInput.getFullName());
            beneficiary.setGender(beneficiaryInput.getGender().name());
            beneficiary.setEmail(beneficiaryInput.getEmail());
            beneficiary.setBirthday(beneficiaryInput.getDob());
            beneficiary.setPhone(beneficiaryInput.getPhoneNumber());
            beneficiary.setIdentification(beneficiaryInput.getIdentificationNumber());
            beneficiary.setIdCardType(Common.convertIdentificationTypeMbalToMiniApp(beneficiaryInput.getIdentificationType()));
            beneficiary.setNationality(getCountryName(beneficiaryInput.getNationalityCode()));
            beneficiary.setAddress(beneficiaryInput.getAddress() != null ? beneficiaryInput.getAddress().getLine1() : "");
            beneficiary.setInsuredId(beneficiaryInput.getId());
            beneficiary.setInsuranceRequest(insuranceRequest);
            beneficiary.setJob(beneficiaryInput.getOccupationId() == null ? "" : mapIdJobName.get(Long.valueOf(beneficiaryInput.getOccupationId())));
            beneficiaryToSave.add(beneficiary);
        }
        beneficiaryRepository.saveAll(beneficiaryToSave);
    }

    private AdditionalInsured saveAdditionalInsured(InsuranceRequest insuranceRequest, AdditionalAssuredOutput eachAdditionalAssured,
                                                    FlexibleSubmitQuestionRequest additionalQuestionResponse, boolean isNew,
                                                    Map<Long, String> mapIdJobName, boolean isCustomer, FlexibleSubmitMicQuestionRequest.MicQuestion micQuestion) {
        if (!isNew) {
            log.info("[MINI]--Update additionalAssured with miniAssuredId: {}", eachAdditionalAssured.getAssured().getMiniAssuredId());
            AdditionalInsured additionalInsured = additionalInsuredRepository.findByMiniAssuredId(eachAdditionalAssured.getAssured().getMiniAssuredId());
            savingHealthQuestions(additionalQuestionResponse, micQuestion, additionalInsured, isNew, eachAdditionalAssured);
            return additionalInsured;
        }
        log.info("Starting saveAdditionalInsured");
        Assured assured = eachAdditionalAssured.getAssured();
        AdditionalInsured additionalInsured = new AdditionalInsured();
        additionalInsured.setFullName(assured.getFullName());
        additionalInsured.setBirthday(assured.getDob());
        additionalInsured.setGender(assured.getGender().name());
        additionalInsured.setIdCardType(Common.convertIdentificationTypeMbalToMiniApp(assured.getIdentificationType()));
        additionalInsured.setIdentification(assured.getIdentificationNumber());
        additionalInsured.setPhone(assured.getPhoneNumber());
        additionalInsured.setJob(mapIdJobName.get(Long.valueOf(assured.getOccupationId())));
        additionalInsured.setInsuranceRequest(insuranceRequest);
        additionalInsured.setInsuredId(assured.getMiniAssuredId());
        additionalInsured.setEmail(assured.getEmail());
        additionalInsured.setAddress(assured.getAddress() != null ? assured.getAddress().fullAddress() : "");
        additionalInsured.setRelationshipWithMainAssured(assured.getRelationshipWithMainAssured());
        additionalInsured.setRelationshipWithPolicyHolder(assured.getRelationshipWithPolicyHolder());
        additionalInsured.setNationality(getCountryName(assured.getNationalityCode()));
        additionalInsured.setAppQuestionNumber(eachAdditionalAssured.getAppQuestionNumber());
        savingHealthQuestions(additionalQuestionResponse, micQuestion, additionalInsured, isNew, eachAdditionalAssured);

        additionalInsured.setMiniAssuredId(assured.getMiniAssuredId());
        additionalInsured.setCustomerIsAssured(isCustomer);
        additionalInsured.setMicQuestionResponse(micQuestion == null ? null : gson.toJson(micQuestion));
        additionalInsured.setAnnualIncome(assured.getAnnualIncome());
        return additionalInsuredRepository.save(additionalInsured);
    }

    private void savingHealthQuestions(FlexibleSubmitQuestionRequest additionalQuestionResponse,
                                       FlexibleSubmitMicQuestionRequest.MicQuestion micQuestion,
                                       AdditionalInsured additionalInsured, boolean isNew,
                                       AdditionalAssuredOutput eachAdditionalAssured) {
        additionalInsured.setMicQuestionResponse(micQuestion == null ? null : gson.toJson(micQuestion)); // MIC

        if (isNew) {
            if (eachAdditionalAssured.getAppQuestionNumber() != null) {
                if (additionalQuestionResponse.isFrom3To12()) {
                    additionalInsured.setAppQuestionNumber(12); // override AppQuestionNumber
                    additionalInsured.setMbalQuestionResponse(gson.toJson(additionalQuestionResponse.getComboBigQuestion()));
                } else if (eachAdditionalAssured.getAppQuestionNumber() == 12) {
                    additionalInsured.setMbalQuestionResponse(gson.toJson(additionalQuestionResponse.getComboBigQuestion()));
                } else if (eachAdditionalAssured.getAppQuestionNumber() == 3) {
                    additionalInsured.setMbalQuestionResponse(gson.toJson(additionalQuestionResponse.getComboSmallQuestion()));
                } else {
                    additionalInsured.setMbalQuestionResponse(null);
                }
            } else {
                additionalInsured.setMbalQuestionResponse(null);
            }
            return; //!
        }

        // update!
        if (additionalInsured.getAppQuestionNumber() != null) {
            if (additionalQuestionResponse.isFrom3To12()) {
                additionalInsured.setAppQuestionNumber(12); // override AppQuestionNumber
                additionalInsured.setMbalQuestionResponse(gson.toJson(additionalQuestionResponse.getComboBigQuestion()));
            } else if (additionalInsured.getAppQuestionNumber() == 12) {
                additionalInsured.setMbalQuestionResponse(gson.toJson(additionalQuestionResponse.getComboBigQuestion()));
            } else if (additionalInsured.getAppQuestionNumber() == 3) {
                additionalInsured.setMbalQuestionResponse(gson.toJson(additionalQuestionResponse.getComboSmallQuestion()));
            } else {
                additionalInsured.setMbalQuestionResponse(null);
            }
        } else {
            additionalInsured.setMbalQuestionResponse(null);
        }
    }

    private void savePrimaryProduct(InsuranceRequest insuranceRequest, PrimaryInsured primaryInsuredSaved, ProductPackageOutput productPackage) {
        log.debug("Starting savePrimaryProduct");
        PrimaryProduct primaryProduct = new PrimaryProduct();
        primaryProduct.setPrimaryInsured(primaryInsuredSaved);
        primaryProduct.setPolicyTerm(productPackage.getPolicyTerm());
        primaryProduct.setInsuredBenefit(productPackage.getInsuredBenefit());
        primaryProduct.setPremiumTerm(productPackage.getPremiumTerm());
        primaryProduct.setPaymentPeriod(productPackage.getPaymentPeriod());
        primaryProduct.setSumAssured(productPackage.getSumAssured() == null ? null : productPackage.getSumAssured().longValue());
        primaryProduct.setPeriodicPremium(productPackage.getPeriodicPremium() == null ? null : productPackage.getPeriodicPremium().longValue());
        primaryProduct.setDiscountGroup(productPackage.getDiscountGroup());
        primaryProduct.setBaseInsuranceFee(productPackage.getBaseInsuranceFee() == null ? null : productPackage.getBaseInsuranceFee().longValue());
        primaryProduct.setTopupInsuranceFee(productPackage.getTopupInsuranceFee() == null ? null : productPackage.getTopupInsuranceFee().longValue());
        primaryProduct.setInsuranceRequest(insuranceRequest);
        primaryProductRepository.save(primaryProduct);
        log.debug("End savePrimaryProduct");
    }

    private PrimaryInsured savePrimaryInsured(InsuranceRequest insuranceRequest, PolicyHolderAndLifeAssured lifeAssured,
                                              PolicyHolderAndLifeAssured policyHolder, FlexibleSubmitQuestionRequest questionResponse,
                                              boolean isNew, Map<Long, String> mapIdJobName, FlexibleSubmitMicQuestionRequest.MicQuestion micQuestion) {
        if (!isNew) {
            log.info("[MINI]--Update question response combo for insurance request {}", insuranceRequest.getId());
            PrimaryInsured primaryInsured = primaryInsuredRepository.findByInsuranceRequestId(insuranceRequest.getId());

            if (primaryInsured.getAppQuestionNumber() != null) {
                if (questionResponse.isFrom3To12()) {
                    primaryInsured.setAppQuestionNumber(12); // override AppQuestionNumber
                    primaryInsured.setMbalQuestionResponse(gson.toJson(questionResponse.getComboBigQuestion()));
                } else if (primaryInsured.getAppQuestionNumber() == 12) {
                    primaryInsured.setMbalQuestionResponse(gson.toJson(questionResponse.getComboBigQuestion()));
                } else if (primaryInsured.getAppQuestionNumber() == 3) {
                    primaryInsured.setMbalQuestionResponse(gson.toJson(questionResponse.getComboSmallQuestion()));
                } else {
                    primaryInsured.setMbalQuestionResponse(null);
                }
            } else {
                primaryInsured.setMbalQuestionResponse(null);
            }

            primaryInsured.setMicQuestionResponse(micQuestion == null ? null : gson.toJson(micQuestion));
            log.debug("[MINI]--End update PrimaryInsured");
            return primaryInsured;
        }
        log.debug("[MINI]--Starting save new PrimaryInsured");
        PrimaryInsured primaryInsured = new PrimaryInsured();
        primaryInsured.setFullName(lifeAssured.getFullName());
        primaryInsured.setGender(lifeAssured.getGender().name());
        primaryInsured.setEmail(lifeAssured.getEmail());
        primaryInsured.setBirthday(lifeAssured.getDob());
        primaryInsured.setPhone(lifeAssured.getPhoneNumber());
        primaryInsured.setIdentification(lifeAssured.getIdentificationNumber());
        primaryInsured.setIdCardType(Common.convertIdentificationTypeMbalToMiniApp(lifeAssured.getIdentificationType()));
        primaryInsured.setNationality(getCountryName(lifeAssured.getNationalityCode()));
        primaryInsured.setAddress(lifeAssured.getAddress() != null ? lifeAssured.getAddress().fullAddress() : "");
        primaryInsured.setInsuredId(lifeAssured.getId());
        primaryInsured.setMiniAssuredId(lifeAssured.getMiniAssuredId());
        primaryInsured.setInsuranceRequest(insuranceRequest);
        primaryInsured.setCustomerIsAssured(false);

        // health quests
        if (lifeAssured.getAppQuestionNumber() != null) {
            if (questionResponse.isFrom3To12()) {
                lifeAssured.setAppQuestionNumber(12); // override AppQuestionNumber
                primaryInsured.setMbalQuestionResponse(gson.toJson(questionResponse.getComboBigQuestion()));
            } else if (lifeAssured.getAppQuestionNumber() == 12) {
                primaryInsured.setMbalQuestionResponse(gson.toJson(questionResponse.getComboBigQuestion()));
            } else if (lifeAssured.getAppQuestionNumber() == 3) {
                primaryInsured.setMbalQuestionResponse(gson.toJson(questionResponse.getComboSmallQuestion()));
            } else {
                primaryInsured.setMbalQuestionResponse(null);
            }
        } else {
            primaryInsured.setMbalQuestionResponse(null);
        }
        primaryInsured.setAppQuestionNumber(lifeAssured.getAppQuestionNumber());
        primaryInsured.setMicQuestionResponse(micQuestion == null ? null : gson.toJson(micQuestion));

        if (policyHolder.getIdentificationNumber() != null && lifeAssured.getIdentificationNumber() != null
                && policyHolder.getIdentificationNumber().equals(lifeAssured.getIdentificationNumber())) {
            primaryInsured.setCustomerIsAssured(true);
        }

        primaryInsured.setJob(lifeAssured.getOccupationId() == null ? "" : mapIdJobName.get(Long.valueOf(lifeAssured.getOccupationId())));
        primaryInsured.setRelationshipWithPolicyHolder(lifeAssured.getRelationshipWithPolicyHolder());
        primaryInsured.setAnnualIncome(lifeAssured.getAnnualIncome());
        PrimaryInsured primaryInsuredSaved = primaryInsuredRepository.save(primaryInsured);
        log.debug("[MINI]--End save new PrimaryInsured {}", primaryInsuredSaved.getId());
        return primaryInsuredSaved;
    }

    private String getCountryName(String code) {
        if ("VN" .equalsIgnoreCase(code)) {
            return "Việt Nam";
        } else
            return code;
    }

    private void saveAdditionalProducts(InsuranceRequest insuranceRequest, PrimaryInsured primaryInsuredSaved, Customer customer,
                                        AdditionalInsured additionalInsured, AdditionalAssuredOutput additionalAssuredOutput) {
        log.debug("[MINI]--Starting saveAdditionalProducts");
        List<AdditionalProduct> additionalProducts = new ArrayList<>();
        QuoteAssuredOutput quoteAssuredOutput = additionalAssuredOutput.getAdditionalProduct();
        List<FlexibleCommon.Rider> riders = quoteAssuredOutput.getRiders() == null ? Collections.emptyList() : quoteAssuredOutput.getRiders();

        for (Rider rider : riders) {
            AdditionalProduct additionalProduct = buildAdditionalProduct(insuranceRequest, primaryInsuredSaved,
                    customer, rider, additionalInsured, MBAL, quoteAssuredOutput);
            additionalProducts.add(additionalProduct);
        }
        if (quoteAssuredOutput.getMicRequest() != null && quoteAssuredOutput.getMicResult() != null && quoteAssuredOutput.getMicBenefit() != null) {
            AdditionalProduct additionalProduct = buildAdditionalProduct(insuranceRequest, primaryInsuredSaved,
                    customer, null, additionalInsured, MIC, quoteAssuredOutput);

            additionalProducts.add(additionalProduct);
        }
        additionalProductRepository.saveAll(additionalProducts);
        log.debug("[MINI]--End saveAdditionalProducts");
    }

    private AdditionalProduct buildAdditionalProduct(InsuranceRequest insuranceRequest, PrimaryInsured primaryInsuredSaved, Customer customer, Rider rider,
                                                     AdditionalInsured additionalInsured, String productType, QuoteAssuredOutput quoteAssuredOutput) {
        AdditionalProduct additionalProduct = new AdditionalProduct();
        additionalProduct.setPrimaryInsured(primaryInsuredSaved);
        additionalProduct.setCustomer(customer);
        additionalProduct.setAdditionalInsured(additionalInsured);
        additionalProduct.setInsuranceRequest(insuranceRequest);

        if (MBAL.equals(productType)) {
            additionalProduct.setCode(rider.getCode());
            additionalProduct.setPolicyTerm(rider.getPolicyTerm());
            additionalProduct.setPremiumTerm(rider.getPremiumTerm());
            additionalProduct.setSumAssured(rider.getSumAssured() == null ? null : rider.getSumAssured().longValue());
            additionalProduct.setPaymentPeriod(rider.getPaymentPeriod());
            additionalProduct.setInsuredBenefit(rider.getInsuredBenefit());
            additionalProduct.setBeneficiaryName(rider.getBeneficiaryName());
            additionalProduct.setBasePremium(rider.getBasePremium() == null ? null : rider.getBasePremium().longValue());
            additionalProduct.setRegBasePrem(rider.getRegBasePrem() == null ? null : rider.getRegBasePrem().longValue());
            additionalProduct.setAssuredId(rider.getAssuredId());
            additionalProduct.setProductType(rider.getProductType());
            additionalProduct.setType(Constants.PackageType.MBAL);
            return additionalProduct;
        }
        MicAdditionalProduct micRequest = quoteAssuredOutput.getMicRequest();
        MicSandboxFeeCareRespDto micResult = quoteAssuredOutput.getMicResult();
        MicBenefitRespDto micBenefit = quoteAssuredOutput.getMicBenefit();
        additionalProduct.setBs1(micRequest.getBs1());
        additionalProduct.setBs2(micRequest.getBs2());
        additionalProduct.setBs3(micRequest.getBs3());
        additionalProduct.setBs4(micRequest.getBs4());
        additionalProduct.setMicPackage(micPackageRepository.findByName(micBenefit.getName()));
        additionalProduct.setMicFee(micResult.getPhi());
        additionalProduct.setMicTransactionId(micResult.getMicTransactionId());
        additionalProduct.setParentMicContract(gson.toJson(micRequest.getParentContract()));
        additionalProduct.setType(Constants.PackageType.MIC);

        additionalProduct.setPaymentPeriod(PaymentPeriod.ANNUAL);
        additionalProduct.setMicSumBenefit(micResult.getMicSumBenefit());
        additionalProduct.setPolicyTerm(1);
        MicSandboxSearchCertRespDto certRespDto = micAPIService.micSandboxSearchCert(micResult.getMicTransactionId());
        if (MIC_CODE_02.equals(certRespDto.getCode()) ||
                (MIC_CODE_00.equals(certRespDto.getCode()) && certRespDto.getData().getSo_hd() == null)) {
            log.error("[MIC]--Không tạo được GCNBH MIC cho gói bảo hiểm. Details {}", certRespDto.getMessage());
            throw new MicApiException(MSG12);
        }
        additionalProduct.setMicContractNum(certRespDto.getData().getSo_hd());
        return additionalProduct;

    }

    private static PavTable buildPavTable(IllustrationTable illustrationTableSaved, QuotationDetailDto quotationDetailDto) {
        PavTable pavTable = new PavTable();
        PavTable.PavTableId pavId = new PavTable.PavTableId();
        pavId.setIllustrationTable(illustrationTableSaved);
        pavId.setContractYear(quotationDetailDto.getPolicyYear());
        pavTable.setId(pavId);

        pavTable.setAgeInsured(quotationDetailDto.getInsuredAge());
        pavTable.setBasePremium(quotationDetailDto.getBasePremium() == null ? null : quotationDetailDto.getBasePremium().longValue());
        pavTable.setTopupPremium(quotationDetailDto.getTopupPremium() == null ? null : quotationDetailDto.getTopupPremium().longValue());
        pavTable.setWithdrawal(quotationDetailDto.getWithdrawal() == null ? null : quotationDetailDto.getWithdrawal().longValue());
        pavTable.setSelectedRateBaseValue(quotationDetailDto.getSelectedRateBaseValue() == null ? null : quotationDetailDto.getSelectedRateBaseValue().longValue());
        pavTable.setSelectedRateTopupValue(quotationDetailDto.getSelectedRateTopupValue() == null ? null : quotationDetailDto.getSelectedRateTopupValue().longValue());
        pavTable.setSelectedRateAccountValue(quotationDetailDto.getSelectedRateAccountValue() == null ? null : quotationDetailDto.getSelectedRateAccountValue().longValueExact());
        pavTable.setSelectedRateSurenderValue(quotationDetailDto.getSelectedRateSurenderValue() == null ? null : quotationDetailDto.getSelectedRateSurenderValue().longValueExact());
        pavTable.setCommittedRateBaseValue(quotationDetailDto.getCommittedRateBaseValue() == null ? null : quotationDetailDto.getCommittedRateBaseValue().longValue());
        pavTable.setCommittedRateTopupValue(quotationDetailDto.getCommittedRateTopupValue() == null ? null : quotationDetailDto.getCommittedRateTopupValue().longValue());
        pavTable.setCommittedRateAccountValue(quotationDetailDto.getCommittedRateAccountValue() == null ? null : quotationDetailDto.getCommittedRateAccountValue().longValue());
        pavTable.setCommittedRateSurenderValue(quotationDetailDto.getCommittedRateSurenderValue() == null ? null : quotationDetailDto.getCommittedRateSurenderValue().longValue());
        pavTable.setLowRateDeathBenefit(quotationDetailDto.getLowRateDeathBenefit() == null ? null : quotationDetailDto.getLowRateDeathBenefit().longValue());
        pavTable.setLowRateLoyaltyBonus(quotationDetailDto.getLowRateLoyaltyBonus() == null ? null : quotationDetailDto.getLowRateLoyaltyBonus().longValue());
        pavTable.setLowRateAccountValue(quotationDetailDto.getLowRateAccountValue() == null ? null : quotationDetailDto.getLowRateAccountValue().longValue());
        pavTable.setLowRateSurenderValue(quotationDetailDto.getLowRateSurenderValue() == null ? null : quotationDetailDto.getLowRateSurenderValue().longValue());
        pavTable.setHighRateDeathBenefit(quotationDetailDto.getHighRateDeathBenefit() == null ? null : quotationDetailDto.getHighRateDeathBenefit().longValue());
        pavTable.setHighRateLoyaltyBonus(quotationDetailDto.getHighRateLoyaltyBonus() == null ? null : quotationDetailDto.getHighRateLoyaltyBonus().longValue());
        pavTable.setHighRateAccountValue(quotationDetailDto.getHighRateAccountValue() == null ? null : quotationDetailDto.getHighRateAccountValue().longValue());
        pavTable.setHighRateSurenderValue(quotationDetailDto.getHighRateSurenderValue() == null ? null : quotationDetailDto.getHighRateSurenderValue().longValue());
        return pavTable;
    }

    @EventListener
    @Async(value = TAKE_TIME_LOW_CPU)
    public void handleGenerationEvent(SendEmailEvent emailEvent) {
        if (EMAIL_SUCCESS.equals(commonService.getProcessCacheDataNotThrow(sendEmailProcessIdKey(emailEvent.getCif(), String.valueOf(emailEvent.getProcessId()))))) {
            log.error("Spam send email multi time for cif {} and processId {}", emailEvent.getCif(), emailEvent.getProcessId());
            return;
        }
        log.info("[MINI]--Send email for cif {} to {}", emailEvent.getCif(), emailEvent.getRecipient());
        HttpEntity<SendEmailEvent> entity = new HttpEntity<>(emailEvent, mbalBasicAuth);
        String urlSendEmail = String.format(MBAL_FLEXIBLE_SEND_MAIL, emailEvent.getProcessId());
        try {
            String response = restCallApi(String.format("%s%s", mbalHostUl3, urlSendEmail), entity, String.class, emailEvent);
            if (response != null && response.contains(EMAIL_SUCCESS)) {
                commonService.setEmailProcessCacheData(sendEmailProcessIdKey(emailEvent.getCif(), String.valueOf(emailEvent.getProcessId())), EMAIL_SUCCESS);
            }
        } catch (MbalApiException e) {
            commonService.setEmailProcessCacheData(sendEmailProcessIdKey(emailEvent.getCif(), String.valueOf(emailEvent.getProcessId())), "Failed");
        }
    }

    private void genFlexibleIllustrationTable(IllustrationTable illustrationTable, FlexibleQuoteRespDto flexibleProcessResp) {

        illustrationTable.setIllustrationNumber();
        illustrationTable.setPackageType(Constants.PackageType.FLEXIBLE);
        illustrationTable.setInsuranceFee(flexibleProcessResp.getTotalAmountStr());
        illustrationTable.setBaseInsuranceFee(String.valueOf(flexibleProcessResp.getProductPackage().getPeriodicPremium()));
        illustrationTable.setPayFrequency(flexibleProcessResp.getProductPackage().getPaymentPeriod().name());
        illustrationTable.setTimeFrequency(flexibleProcessResp.getProductPackage().getPremiumTerm() == null ? "" : flexibleProcessResp.getProductPackage().getPremiumTerm() + " năm");
    }

    private void updateCustomerInfoCreateQuote(String cif, FlexibleCommon.CustomerInfo customerInfo) {
        // Update thông tin khách hàng
        Customer customerByMbId = customerRepository.findByMbId(cif);
        if (customerByMbId == null) {
            throw new CustomerNotFoundException("Không tồn tại Khách hàng có MbId là " + cif);
        }

        if (customerInfo.getFullName() != null) {
            if (!isMatchFullNameT24(customerByMbId.getFullNameT24(), customerInfo.getFullName())) {
                throw new CustomerNotFoundException(MSG37);
            }
            customerByMbId.setFullName(customerInfo.getFullName());
        }

        if (customerInfo.getDob() != null) {
            customerByMbId.setBirthday(customerInfo.getDob());
        }
        if (customerInfo.getGender() != null) {
            customerByMbId.setGender(customerInfo.getGender().name());
        }
        Address address = customerInfo.getAddress();
        if (address != null && address.getLine1() != null) {
            customerByMbId.setLine1(address.getLine1());
            customerByMbId.setProvinceName(address.getProvinceName());
            customerByMbId.setDistrictName(address.getDistrictName());
            customerByMbId.setWardName(address.getWardName());
        }
        if (customerInfo.getEmail() != null) {
            customerByMbId.setEmail(customerInfo.getEmail());
        }
        customerRepository.save(customerByMbId);
    }

    private void verifyCustomerPermission(String cif, Integer processId) {
        if (!String.valueOf(processId).equals(redis.get(processIdKey(cif)))) {
            throw new MiniApiException(MSG13);
        }
    }

    private <T> T restCallApi(String host, HttpEntity<?> entity, Class<T> clazz, Object reqDto) {
        LocalDateTime sendTime = LocalDateTime.now();
        String requestId = generateUUIDId(36);
        try {
            ResponseEntity<T> exchange = restTemplate.exchange(host, HttpMethod.POST, entity, clazz);
            T exchangeBody = exchange.getBody();
            HttpHeaders headers = exchange.getHeaders();

            genFlexibleMbalLog(host, gson.toJson(reqDto), 200, null, sendTime, LocalDateTime.now(),
                    requestId, ThirdPartyLog.StepProcess.COMPLETED, exchangeBody, headers, entity.getBody() == null ? null : gson.toJson(entity.getBody()));
            return exchangeBody;
        } catch (Exception e) {
            log.error("[MBAL]--Failed to call host {}, detail {}", host, e.getMessage());
            asyncObject.saveErrorLog(host, HttpMethod.POST, reqDto, e.getMessage(), Constants.HOST_PARTY.MBAL, sendTime, LocalDateTime.now());

            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, ErrorResponseMbal.class);
            IEnumCode enumMessage = ErrorCodeFlexQuotation.findByCode(errorObject.getErrorCode(), errorObject.getErrorMessage());
            ErrorDto errorDto = errorObject.toErrorDto();
            errorDto.setMessage(enumMessage.getMessageVn() + " (" + enumMessage.getCode() + ")");

            HttpHeaders responseHeaders = e instanceof HttpClientErrorException ? ((HttpClientErrorException) e).getResponseHeaders() : null;
            // save log
            genFlexibleMbalLog(host, gson.toJson(reqDto), errorObject.getHttpStatus(), errorObject.getErrorMessage(), sendTime, LocalDateTime.now(),
                    requestId, ThirdPartyLog.StepProcess.ERROR, null, responseHeaders, entity.getBody() == null ? null : gson.toJson(entity.getBody()));

            throw new MbalApiException(enumMessage.getMessageVn(), errorDto);


//            HttpHeaders responseHeaders = null;
//            try {
//                responseHeaders = ((HttpClientErrorException) e).getResponseHeaders();
//            } catch (ClassCastException ex) {
////                responseHeaders = ((HttpServerErrorException) e).getResponseHeaders();
//                log.error("[Mini]--Flex can not catch the exception to get header");
//            }
//            String errorMessage = e.getMessage();
//            Integer code = 500;
//            if (errorMessage.contains("{") && errorMessage.contains("}")) {
//                String bodyMessage = errorMessage.substring(errorMessage.indexOf("{"), errorMessage.lastIndexOf("}") + 1);
//                ErrorResponseMbal errorResponseMbal = gson.fromJson(bodyMessage, ErrorResponseMbal.class);
//                List<String> messages = errorResponseMbal.getMessages();
//                errorMessage = StringUtils.join(messages, ", ");
//                code = errorResponseMbal.getStatusCode();
//                if (code == 650010 || errorMessage.contains("650010") || errorMessage.contains("Chứng minh nhân dân có 9 hoặc 12 kí tự số")) {
//                    errorMessage = "Thông tin Giấy tờ tùy thân của Quý khách chưa hợp lệ. Vui lòng kiểm tra lại (" + code + ")";
//                } else if (errorMessage.contains("Lỗi khi xử lý ở PQM")
//                            || errorMessage.contains("Có lỗi xảy ra")
//                            || errorMessage.contains("Lỗi hệ thống")
//                            || errorMessage.contains("timed out")) {
//                    errorMessage = "Rất tiếc!!! Hệ thống đang nâng cấp. Vui lòng thử lại sau 2 phút nữa (" + code + ")";
//                }
//            }
//            genFlexibleMbalLog(host, gson.toJson(reqDto), code, errorMessage, sendTime, LocalDateTime.now(),
//                    requestId, ThirdPartyLog.StepProcess.ERROR, null, responseHeaders, entity.getBody() == null ? null : gson.toJson(entity.getBody()));
//            if (code == null || code == 500) {
//                throw new MbalApiException(MSG12);
//            }
//            throw new MbalApiException(errorMessage);
        }
    }

    private QuoteFlexibleReqDto genQuoteFlexibleReqDto(List<QuoteAssuredInput> mbalProducts, Map<String, Assured> assuredMap, Assured customer,
                                                       GenerateQuotationDto quotationDto, FlexibleCommon.Assured mainAssured) {
        QuoteFlexibleReqDto reqDto = new QuoteFlexibleReqDto();

        List<FlexibleCommon.Assured> assuredMbal = new ArrayList<>();
        assuredMbal.add(0, mainAssured);
        List<MbalAdditionalProductInput> additionalProductMbal = new ArrayList<>();
        for (QuoteAssuredInput assuredInput : mbalProducts) {
            Assured assured = assuredMap.get(assuredInput.getAssuredId());
//            assured.setAnnualIncome(assuredInput.getMbalAdditionalProductInput().getAnnualIncome());
            Integer indexOf = !assuredMbal.contains(assured) ? assuredMbal.size() : assuredMbal.indexOf(assured);
            additionalProductMbal.add(assuredInput.getMbalAdditionalProductInput().setAssuredIndex(indexOf));
            if (!assuredMbal.contains(assuredMap.get(assuredInput.getAssuredId()))) {
                assuredMbal.add(assured);
            }
            if (ProductType.PWR.equals(assuredInput.getMbalAdditionalProductInput().getProductType())
                    && (Boolean.FALSE.equals(assured.getIsCustomer()) || assured.getType().equals(LIFE_ASSURED))) {
                throw new MiniApiException(FL_MSG37);
            }
//            if (ProductType.HSCR.equals(assuredInput.getMbalAdditionalProductInput().getProductType()) && ADDITIONAL_LIVES.equals(assured.getType())) {
//                if (assuredInput.getMbalAdditionalProductInput().getAnnualIncome() == null) {
//                    throw new MiniApiException(FL_MSG47);
//                }
//                assured.setAnnualIncome(assuredInput.getMbalAdditionalProductInput().getAnnualIncome());
//            }
        }

        validateAdditionalProducts(additionalProductMbal);
        reqDto.setAssureds(assuredMbal);
        reqDto.setAdditionalProducts(additionalProductMbal);
        reqDto.setSale(quotationDto.getSale());
        reqDto.setProcessId(quotationDto.getProcessId());

        if (quotationDto.getSupporter() != null) {
            reqDto.setSupporter(quotationDto.getSupporter().to3rdInput());
        }
        if (quotationDto.getReferer() != null) {
            reqDto.setReferrer(quotationDto.getReferer().to3rdInput());
        }

        reqDto.setQuotationDate(DateUtil.localDateTimeToString(DATE_YYYY_MM_DD, LocalDateTime.now()));
        reqDto.setCustomer(new FlexibleCommon.CustomerInfo(customer));
        reqDto.setProductPackage(quotationDto.getProductPackage());
        reqDto.setRaiderDeductFund(quotationDto.isRaiderDeductFund());
        reqDto.setBeneficiaries(quotationDto.getBeneficiary() == null ? new ArrayList<>() : List.of(quotationDto.getBeneficiary()));
        reqDto.setAmounts(quotationDto.getAmount() == null ? new ArrayList<>() : List.of(quotationDto.getAmount()));
        return reqDto;
    }

    private void validateAdditionalProducts(List<MbalAdditionalProductInput> additionalProducts) {
        List<MbalAdditionalProductInput> duplicates = additionalProducts.stream()
                .collect(groupingBy(identity(), counting()))
                .entrySet().stream()
                .filter(n -> n.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(toList());
        if (!duplicates.isEmpty()) {
            log.error("Duplicate mua 1 mã gói bổ sung cho cùng 1 người hoặc mua quá 2 gói MIC cho 1 người. {}", additionalProducts);
            throw new MiniApiException(FL_MSG35);
        }
    }

    private static void buildQuotationDetailWithInvestment(YearItem item, QuotationDetailDto detail) {
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

    private static void buildQuotationDetailWithInterestRate(YearItem item, QuotationDetailDto detail) {
        if (item.getAccountByInterestRate() != null) {
            if (item.getAccountByInterestRate().getSelectedRate() != null) {
                detail.setSelectedRateBaseValue(
                        item.getAccountByInterestRate().getSelectedRate().getBaseAccountValue());
                detail.setSelectedRateTopupValue(
                        item.getAccountByInterestRate().getSelectedRate().getTopupAccountValue());
                detail.setSelectedRateAccountValue(item.getAccountByInterestRate().getSelectedRate().getAccountValue());
                detail.setSelectedRateSurenderValue(
                        item.getAccountByInterestRate().getSelectedRate().getSurenderValue());
            }
            if (item.getAccountByInterestRate().getCommittedRate() != null) {
                detail.setCommittedRateBaseValue(
                        item.getAccountByInterestRate().getCommittedRate().getBaseAccountValue());
                detail.setCommittedRateTopupValue(
                        item.getAccountByInterestRate().getCommittedRate().getTopupAccountValue());
                detail.setCommittedRateAccountValue(
                        item.getAccountByInterestRate().getCommittedRate().getAccountValue());
                detail.setCommittedRateSurenderValue(
                        item.getAccountByInterestRate().getCommittedRate().getSurenderValue());
            }

        }
    }

    private static void validateOccupation(List<OccupationV2RespDto> occupations, Integer occupationId) {
        Optional<OccupationV2RespDto> occupationV2RespDto = occupations.stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.equals(o.getId(), Long.valueOf(occupationId))).findFirst();
        if (occupationV2RespDto.isPresent()) {
            OccupationV2RespDto occupation = occupationV2RespDto.get();
            if (occupation.getGroupId() == 5) {
                throw new MiniApiException(MSG02);
            }
        }
    }

    private <T> void genFlexibleMbalLog(String path, String payload, Integer code, String errorMessage, LocalDateTime sendTime,
                                        LocalDateTime receivedTime, String requestId, ThirdPartyLog.StepProcess stepProcess,
                                        T exchangeBody, HttpHeaders headers, String thirdPartyPayload) {
        ThirdPartyLog thirdPartyLog = new ThirdPartyLog();
        thirdPartyLog.setPath(path);
        thirdPartyLog.setMethod("POST");
        thirdPartyLog.setHostParty(Constants.HOST_PARTY.MBAL);
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

    private static void updateCustomerInfo(Customer customer, FlexibleCommon.CustomerInfo customerInfo) {
        // verify customer name
        if (!isMatchFullNameT24(customer.getFullNameT24(), customerInfo.getFullName())) {
            throw new CustomerNotFoundException(MSG37);
        }
        //customerInfo.setFullName(customer.getFullName()); => cho phép update name

        customerInfo.setIdentificationNumber(customer.getIdentification());
        customerInfo.setPhoneNumber(customer.getPhone());
        if (customer.getIdentificationDate() != null) {
            customerInfo.setIdentificationDate(DateUtil.localDateTimeToString(DATE_YYYY_MM_DD, customer.getIdentificationDate()));
        }
        customerInfo.setIdIssuedPlace(customer.getIdIssuedPlace());
    }

    private void validateMainProductPackage(Assured customer, Assured mainAssured, FlexibleCommon.ProductPackageInput productPackage) {
        int age = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, mainAssured.getDob() + ZERO_TIME), LocalDateTime.now());
        BigDecimal maxPeriodicPremium = customer.getAnnualIncome()
                .multiply(BigDecimal.valueOf(productPackage.getPaymentPeriod().getMultiple()))
                .multiply(BigDecimal.valueOf(0.3));
        if (productPackage.getPeriodicPremium().compareTo(maxPeriodicPremium) >= 0) {
            throw new MiniApiException(FL_MSG51);
        }

        if (age < 18 && (TWO_BILL_MAX_AMOUNT.compareTo(productPackage.getSumAssured()) < 0)) {
            throw new MiniApiException(FL_MSG50);
        }
        if (maxSumAssured(age, customer.getAnnualIncome()).compareTo(productPackage.getSumAssured()) < 0) {
            throw new MiniApiException(FL_MSG49);
        }
        BigDecimal minSA;
        BigDecimal maxSA;
        switch (productPackage.getPaymentPeriod()) {
            case ANNUAL:
                if (productPackage.getPeriodicPremium().compareTo(productPackage.getPaymentPeriod().getMin()) < 0) {
                    throw new MiniApiException(FL_MSG54);
                }
                minSA = getValidateMinSA(productPackage.getPeriodicPremium().multiply(BigDecimal.valueOf(1)));
                if (productPackage.getSumAssured().compareTo(minSA) < 0) {
                    throw new MiniApiException(String.format(FL_MSG52, formatCurrency(minSA)));
                }
                maxSA = getValidateMaxSA(age, productPackage.getPeriodicPremium().multiply(BigDecimal.valueOf(1)));
                if (productPackage.getSumAssured().compareTo(maxSA) >= 0) {
                    throw new MiniApiException(String.format(FL_MSG53, formatCurrency(maxSA)));
                }
                break;
            case SEMI_ANNUAL:
                if (productPackage.getPeriodicPremium().compareTo(productPackage.getPaymentPeriod().getMin()) < 0) {
                    throw new MiniApiException(FL_MSG55);
                }
                minSA = getValidateMinSA(productPackage.getPeriodicPremium().multiply(BigDecimal.valueOf(2)));
                if (productPackage.getSumAssured().compareTo(minSA) < 0) {
                    throw new MiniApiException(String.format(FL_MSG52, formatCurrency(minSA)));
                }
                maxSA = getValidateMaxSA(age, productPackage.getPeriodicPremium().multiply(BigDecimal.valueOf(2)));
                if (productPackage.getSumAssured().compareTo(maxSA) >= 0) {
                    throw new MiniApiException(String.format(FL_MSG53, formatCurrency(maxSA)));
                }
                break;
            case QUARTERLY:
                if (productPackage.getPeriodicPremium().compareTo(productPackage.getPaymentPeriod().getMin()) < 0) {
                    throw new MiniApiException(FL_MSG56);
                }
                minSA = getValidateMinSA(productPackage.getPeriodicPremium().multiply(BigDecimal.valueOf(4)));
                if (productPackage.getSumAssured().compareTo(minSA) < 0) {
                    throw new MiniApiException(String.format(FL_MSG52, formatCurrency(minSA)));
                }
                maxSA = getValidateMaxSA(age, productPackage.getPeriodicPremium().multiply(BigDecimal.valueOf(4)));
                if (productPackage.getSumAssured().compareTo(maxSA) >= 0) {
                    throw new MiniApiException(String.format(FL_MSG53, formatCurrency(maxSA)));
                }
                break;
            default:
                throw new MiniApiException(MSG12);
        }
    }

    private static void validateCustomerInfo(String dob, String identificationNumber, Gender gender, IdentificationType identificationType) {
        if (identificationNumber.length() == 12 && NATIONAL_ID.equals(identificationType)) {
            String genderNum = identificationNumber.substring(3, 4);
            String yearNum = identificationNumber.substring(4, 6);
            if ((MALE.equals(gender) && !MALE_NUM.contains(genderNum)) || (FEMALE.equals(gender) && !FEMALE_NUM.contains(genderNum))) {
                throw new MiniApiException(FL_MSG58);
            }
            String yearBirth = dob.substring(2, 4);
            if (!yearBirth.equals(yearNum)) {
                throw new MiniApiException(FL_MSG57);
            }
        }
    }

    private static void validateMicRequest(FlexibleCommon.Assured assured, MicAdditionalProduct micRequest) {
        if (Objects.nonNull(micRequest) && MALE.equals(assured.getGender()) && YES.equals(micRequest.getBs3())) {
            throw new MiniApiException(FL_MSG60);
        }
        int age = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assured.getDob() + ZERO_TIME), LocalDateTime.now());
        if ((age < 18 || age > 50) && Objects.nonNull(micRequest) &&
                (micRequest.getNhom() == 3 || micRequest.getNhom() == 4 || micRequest.getNhom() == 5) && YES.equals(micRequest.getBs3())) {
            throw new MiniApiException(FL_MSG59);
        }
    }

    private static boolean checkMicParentRequest(MicSandboxContractInfoRespDto.ContractInfo micContractInfo,
                                                 MicAdditionalProduct policyHolderRequest) {

        if (getMicGroup(micContractInfo.getNhom()) >= policyHolderRequest.getNhom()) {
            // check dkbs
            if (micContractInfo.getBs1().equals(NO) && policyHolderRequest.getBs1().equals(YES)) {
                return false;
            }
            if (micContractInfo.getBs2().equals(NO) && policyHolderRequest.getBs2().equals(YES)) {
                return false;
            }
            if (micContractInfo.getBs3().equals(NO) && policyHolderRequest.getBs3().equals(YES)) {
                return false;
            }
            return !micContractInfo.getBs4().equals(NO) || !policyHolderRequest.getBs4().equals(YES);
        }
        return false;
    }

    private static boolean checkMicParentRequestWithChildrenRequest(MicAdditionalProduct micChildrenRequest,
                                                                    MicAdditionalProduct micCompareRequest) {

        if (Objects.nonNull(micCompareRequest) && micCompareRequest.getNhom() >= micChildrenRequest.getNhom()) {
            // check dkbs
            if (micCompareRequest.getBs1().equals(NO) && micChildrenRequest.getBs1().equals(YES)) {
                return false;
            }
            if (micCompareRequest.getBs2().equals(NO) && micChildrenRequest.getBs2().equals(YES)) {
                return false;
            }
            if (micCompareRequest.getBs3().equals(NO) && micChildrenRequest.getBs3().equals(YES)) {
                return false;
            }
            return !micCompareRequest.getBs4().equals(NO) || !micChildrenRequest.getBs4().equals(YES);
        }
        return false;
    }

    private static MicCompareResp checkMicParentRequestWithChildrenRequest(MicAdditionalProduct micChildrenRequest,
                                                                           MicAdditionalProduct policyHolderRequest,
                                                                           Assured customer,
                                                                           MicAdditionalProduct coupleRequest,
                                                                           FlexibleCommon.Assured coupleAssured,
                                                                           MicSandboxContractInfoRespDto.ContractInfo contractInfo,
                                                                           Common.ParentContract parentContract) {
        MicCompareResp compareResp = new MicCompareResp();
        micChildrenRequest.setParentContract(new ParentContract()
                .setSo_hd_bm("")
                .setCmt_bm("")
                .setMobi_bm("")
                .setTen_bm(""));
        boolean childrenWithHolder = checkMicParentRequestWithChildrenRequest(micChildrenRequest, policyHolderRequest);
        if (childrenWithHolder) {
            micChildrenRequest.setParentContract(new ParentContract()
                    .setSo_hd_bm("")
                    .setCmt_bm(customer.getIdentificationNumber())
                    .setMobi_bm(customer.getPhoneNumber())
                    .setTen_bm(customer.getFullName()));
            compareResp.setResult(true);
            compareResp.setFromContractInput(false);
            compareResp.setCompareType(MicCompareType.POLICY_HOLDER);
            return compareResp;
        }
        boolean withCouple = checkMicParentRequestWithChildrenRequest(micChildrenRequest, coupleRequest);
        if (withCouple) {
            micChildrenRequest.setParentContract(new ParentContract()
                    .setSo_hd_bm("")
                    .setCmt_bm(coupleAssured.getIdentificationNumber())
                    .setMobi_bm(coupleAssured.getPhoneNumber())
                    .setTen_bm(coupleAssured.getFullName()));
            compareResp.setResult(true);
            compareResp.setFromContractInput(false);
            compareResp.setCompareType(MicCompareType.COUPLE_INSURED);
            return compareResp;
        }
        boolean withParentContract = checkMicParentRequestWithChildrenRequest(micChildrenRequest, contractInfo == null ? null : new MicAdditionalProduct(contractInfo));
        if (withParentContract) {
            micChildrenRequest.setParentContract(new ParentContract()
                    .setSo_hd_bm(parentContract.getSo_hd_bm())
                    .setCmt_bm(contractInfo.getCmt())
                    .setMobi_bm(contractInfo.getMobi())
                    .setTen_bm(contractInfo.getTen()));
            compareResp.setResult(true);
            compareResp.setFromContractInput(true);
            compareResp.setCompareType(MicCompareType.PARENT_CONTRACT);
            return compareResp;
        }
        return compareResp;
    }

}
