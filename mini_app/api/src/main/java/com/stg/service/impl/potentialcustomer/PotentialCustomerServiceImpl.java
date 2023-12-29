package com.stg.service.impl.potentialcustomer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.constant.ComboCode;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.PotentialCustomerDirectRepository;
import com.stg.repository.PotentialCustomerReferRepository;
import com.stg.repository.PotentialCustomerRepository;
import com.stg.repository.sale.BranchInformationRepository;
import com.stg.service.BackofficeService;
import com.stg.service.ComboSuggestionService;
import com.stg.service.PotentialCustomerService;
import com.stg.service.caching.RmInfoCaching;
import com.stg.service.caching.RmInfoCaching.RmCachingData;
import com.stg.service.dto.DeleteIdsReq;
import com.stg.service.dto.combo.UserComboSuggestionResp;
import com.stg.service.dto.potentialcustomer.CreatePotentialCustomerReq;
import com.stg.service.dto.potentialcustomer.FlowReq;
import com.stg.service.dto.potentialcustomer.FlowResp;
import com.stg.service.dto.potentialcustomer.FlowType;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerExportDto;
import com.stg.service.dto.potentialcustomer.SearchPotentialCustomerRes;
import com.stg.service.dto.quotation.QuotationAssuredDto;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mbal.dto.req.FlexibleCheckTSAReq;
import com.stg.service3rd.mbal.dto.resp.FlexibleCheckTSAResp;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import com.stg.service3rd.mbalv2.MbalV2Api3rdService;
import com.stg.service3rd.mic.exception.MicApi3rdException;
import com.stg.utils.AppUtil;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PotentialCustomerServiceImpl implements PotentialCustomerService {

    private static final BigDecimal FLOW_RANK_1 = BigDecimal.valueOf(20 * 1000 * 1000);
    private static final BigDecimal FLOW_RANK_2 = BigDecimal.valueOf(50 * 1000 * 1000);

    private final MbalApi3rdService mbalApi3rdService;
    private final MbalV2Api3rdService mbalV2Api3rdService;

    private final PotentialCustomerRepository potentialCustomerRepository;

    private final PotentialCustomerReferRepository potentialCustomerReferRepository;

    private final PotentialCustomerDirectRepository potentialCustomerDirectRepository;

    private final BranchInformationRepository branchInformationRepository;

    private final ComboSuggestionService comboSuggestionService;
    private final BackofficeService backofficeService;
    private final RmInfoCaching rmInfoCaching;

    private QuotationDto genQuotationExample(PotentialCustomer potentialCustomer, BigDecimal amount) {
        FlexibleCommon.Address address = new FlexibleCommon.Address();
        address.setLine1("DEFAULT");

        QuotationAssuredDto assured = new QuotationAssuredDto();
        assured.setIdentificationType(potentialCustomer.getIdentificationType());
        assured.setIdentificationId(potentialCustomer.getIdentificationId());
        assured.setFullName(potentialCustomer.getFullName());
        assured.setDob(potentialCustomer.getDob());
        assured.setGender(potentialCustomer.getGender());
//        assured.setEmail(potentialCustomer.getEmail());
        assured.setEmail("tsa.default@mbbank.vn");
        assured.setPhoneNumber(potentialCustomer.getPhoneNumber());
        assured.setOccupationId(potentialCustomer.getOccupationId());
        assured.setAddress(address);
        assured.setNationalityCode("VN");

        QuotationDto quotation = new QuotationDto();
        quotation.setCustomer(assured);
        quotation.setCustomerIsAssured(true);
        quotation.setRaiderDeductFund(true);

        quotation.setPackagePolicyTerm(0);
        quotation.setPackageBenefitType(BenefitType.BASIC);
        quotation.setPackagePremiumTerm(0);
        quotation.setPackagePaymentPeriod(PaymentPeriod.ANNUAL);
        quotation.setPackageSumAssured(amount);
        quotation.setPackagePeriodicPremium(BigDecimal.ZERO);

        return quotation;
    }

    private boolean has12QuestWhenCheckTSA(PotentialCustomer potentialCustomer, BigDecimal amount) {
        try {
            QuotationHeaderDto quotation = genQuotationExample(potentialCustomer, amount);

            FlexibleCheckTSAReq checkTSAReq = new FlexibleCheckTSAReq(quotation);
            FlexibleCheckTSAResp checkTSAResp = mbalApi3rdService.checkTSA(checkTSAReq);

            if (!CollectionUtils.isEmpty(checkTSAResp.getAssureds())) {
                return checkTSAResp.getAssureds().stream().anyMatch(a -> a.getAppQuestionNumber() == 12);
            }

        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            log.debug("CheckTSAResp error, detail={}", errorObject.getErrorMessage());
            throw new MicApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }

        return false;
    }

    private boolean hasPaymentPolicy(String identificationNumber) {
        try {
            return !mbalV2Api3rdService.getInquiryPaymentPolicies(identificationNumber).isEmpty();
        } catch (Exception ex) {
            log.error("hasPaymentPolicy: {}", ex.getMessage());
            return true;
        }
    }

    @Override
    public FlowResp checkReferOrDirect(Long id, FlowReq req) {
        String icCode = AppUtil.getCurrentIcCode();
        String branchCode = AppUtil.getCurrentBranchCode();

        if (!StringUtils.hasLength(icCode) || branchInformationRepository.findByCode(branchCode).isEmpty()) {
            log.info("[CheckReferOrDirect] RM không có quyền bán bán theo DIRECT: icCode={}, branchCode={}", icCode,
                    branchCode);
            return FlowResp.of(FlowType.REFER, null, null);
        }

        if (req.getAmount().compareTo(FLOW_RANK_2) >= 0) {
            return FlowResp.of(FlowType.REFER, null, null);
        } else {
            PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(id, AppUtil.getCurrentUsername());

            boolean has12Quest = has12QuestWhenCheckTSA(potentialCustomer, req.getAmount());
            boolean hasPolicy = hasPaymentPolicy(potentialCustomer.getIdentificationId());
            BigDecimal tsa = has12Quest ? BigDecimal.valueOf(10) : BigDecimal.ZERO;

            if (has12Quest && hasPolicy) {
                return FlowResp.of(FlowType.ALL, tsa, true);
            }

            if (!has12Quest && req.getAmount().compareTo(FLOW_RANK_1) <= 0) {
                return FlowResp.of(FlowType.DIRECT, tsa, false);
            }

            return FlowResp.of(FlowType.ALL, tsa, null);
        }
    }

    @Override
    public PotentialCustomerDto create(CreatePotentialCustomerReq request) {
        PotentialCustomer potentialCustomer = PotentialCustomer.builder().fullName(request.getFullName())
                .dob(request.getDob()).gender(request.getGender()).phoneNumber(request.getPhoneNumber())
                .inputAmount(request.getInputAmount()).note(request.getNote())
                .identificationType(request.getIdentificationType()).identificationId(request.getIdentificationId())
                .email(request.getEmail()).occupationId(request.getOccupationId()).raw(false).syncCrm(false).build();

        potentialCustomer = potentialCustomerRepository.save(potentialCustomer);
        PotentialCustomerDto dto = PotentialCustomerDto.of(potentialCustomer);
        dto.setIdentificationId(null);
        dto.setIdentificationType(null);
        return dto;
    }

    @Override
    public PotentialCustomerDto update(CreatePotentialCustomerReq request) {
        PotentialCustomer potentialCustomer;
        if (request.getId() != null) {
            potentialCustomer = potentialCustomerRepository.getById(request.getId(), AppUtil.getCurrentUsername());
            potentialCustomer.setDob(request.getDob());
            potentialCustomer.setFullName(request.getFullName());
            potentialCustomer.setGender(request.getGender());
            potentialCustomer.setPhoneNumber(request.getPhoneNumber());
            potentialCustomer.setInputAmount(request.getInputAmount());
            potentialCustomer.setNote(request.getNote());
            potentialCustomer.setIdentificationType(request.getIdentificationType());
            potentialCustomer.setIdentificationId(request.getIdentificationId());
            potentialCustomer.setEmail(request.getEmail());
            potentialCustomer.setOccupationId(request.getOccupationId());
        } else {
            throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Không tồn tại KH tiềm năng"));
        }

        potentialCustomer = potentialCustomerRepository.save(potentialCustomer);

        PotentialCustomerDto dto = PotentialCustomerDto.of(potentialCustomer);
        dto.setIdentificationId(null);
        dto.setIdentificationType(null);
        return dto;
    }

    @Override
    @Transactional
    public void delete(DeleteIdsReq req) {
        if (req.getIds().isEmpty()) {
            return;
        }
        String username = AppUtil.getCurrentUsername();
        int updated = potentialCustomerRepository.updateToRaw(req.getIds(), username);
        if (updated != req.getIds().size()) {
            throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Đã có lỗi xảy ra"));
        }
    }

    @Override
    public void delete(Long id) {
    }

    @Override
    public PotentialCustomerDto get(Long id, String createdBy) {
        PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(id, createdBy);
        PotentialCustomerDto dto = PotentialCustomerDto.of(potentialCustomer);

        if (!StringUtils.hasText(createdBy)) {
            RmCachingData cachingData = rmInfoCaching.getData(potentialCustomer.getCreatedBy());

            dto.setRmInfo(cachingData != null ? cachingData.toRmInfo() : null);
            dto.setIcInfo(cachingData != null ? cachingData.toIcInfo() : null);

            OccupationResp occupation = backofficeService.getOccupationById(dto.getOccupationId().longValue());
            dto.setOccupation(occupation.getNameVn());
            
            potentialCustomerDirectRepository
                    .findFirstByPotentialCustomerAndRawAndCifNumberNotNull(potentialCustomer, false)
                    .ifPresent(p -> dto.setCif(p.getCifNumber()));
        }

        return dto;
    }

    @Override
    public Page<SearchPotentialCustomerRes> search(Pageable pageable, String query, LocalDate date, LocalDate from,
            LocalDate to, String createdBy) {
        return potentialCustomerRepository.search(pageable, createdBy, query, from, to)
                .map(SearchPotentialCustomerRes::of);
    }

    @Override
    public List<UserComboSuggestionResp> suggestCombos(Long id, List<ComboCode> comboCodes) {
        String username = AppUtil.getCurrentUsername();
        PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(id, username);

        return comboSuggestionService.suggestCombos(potentialCustomer.getInputAmount(), potentialCustomer.getDob(),
                comboCodes);
    }

    @Override
    public Page<SearchPotentialCustomerRes> adminSearch(Pageable pageable, String query, LocalDate from, LocalDate to) {

        RmCachingData searchCachingData = rmInfoCaching.getData(query);

        String createdBy = searchCachingData != null ? searchCachingData.getUsername() : null;

        PotentialCustomer potentialCustomer = null;
        
        if (StringUtils.hasText(createdBy)) {
            query = null;
        }
        
        if (StringUtils.hasText(query)) {
            PotentialCustomerDirect direct = potentialCustomerDirectRepository.findFirstByCifNumberAndRawIsFalse(query).orElse(null);
            potentialCustomer = direct != null ? direct.getPotentialCustomer() : null;
        }

        return potentialCustomerRepository.search(pageable, createdBy, query, from, to, potentialCustomer).map(c -> {
            SearchPotentialCustomerRes res = SearchPotentialCustomerRes.of(c);
            res.setTotalRefer(potentialCustomerReferRepository.countByPotentialCustomer(c));
            res.setTotalDirect(potentialCustomerDirectRepository.countByPotentialCustomerAndRaw(c, false));
            res.setCreatedBy(c.getCreatedBy());
            res.setInputAmount(c.getInputAmount());
            res.setNote(c.getNote());

            RmCachingData cachingData = rmInfoCaching.getData(c.getCreatedBy());

            res.setRmInfo(cachingData != null ? cachingData.toRmInfo() : null);
            res.setIcInfo(cachingData != null ? cachingData.toIcInfo() : null);

            if (res.getTotalDirect() > 0) {
                potentialCustomerDirectRepository.findFirstByPotentialCustomerAndRawAndCifNumberNotNull(c, false)
                        .ifPresent(p -> res.setCif(p.getCifNumber()));
            }
            
            return res;
        });
    }

    @Override
    public void adminExport(Pageable pageable, String query, LocalDate from, LocalDate to, ExportType type,
            HttpServletResponse response) {
        Page<SearchPotentialCustomerRes> page = adminSearch(PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort()),
                query, from, to);

        try {
            List<PotentialCustomerExportDto> exportData = page.getContent().stream()
                    .map(PotentialCustomerExportDto::of).collect(Collectors.toList());
            if (ExportType.CSV.equals(type)) {
                OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Leads-List-", response);
                OpenCsvUtil.write(response.getWriter(), exportData, PotentialCustomerExportDto.class);
            } else {
                OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Leads-List-",
                        response);
                List<String> headers = Arrays.asList("MB ID", "Tên KH", "Phí bảo hiểm", "Thông tin RM", "Thông tin IC",
                        "Phân loại", "Ngày tham gia", "Ghi chú", "Nguồn");
                Field[] fields = PotentialCustomerExportDto.class.getDeclaredFields();
                ExcelUtils.exportExcel(exportData, fields, headers, 2, "templates/leads_list.xlsx", response);
            }
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("[MINI]--Failed when export waiting payments. Detail {}", e.getMessage());
            throw new ApplicationException("[Failed when export refer");
        }
    }

}
