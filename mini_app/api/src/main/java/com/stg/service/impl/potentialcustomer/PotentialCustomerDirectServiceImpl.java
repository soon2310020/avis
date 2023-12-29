package com.stg.service.impl.potentialcustomer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.constant.UserComboAttribute;
import com.stg.constant.quotation.QuotationType;
import com.stg.entity.combo.UserCombo;
import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.DirectState;
import com.stg.entity.potentialcustomer.DirectSubmitStatus;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.entity.potentialcustomer.PotentialCustomerDirect;
import com.stg.entity.potentialcustomer.PotentialCustomerDirectSubmitStatus;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.entity.quotation.QuotationSupporter;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.PotentialCustomerDirectRepository;
import com.stg.repository.PotentialCustomerDirectSubmitStatusRepository;
import com.stg.repository.PotentialCustomerRepository;
import com.stg.repository.QuotationHeaderRepository;
import com.stg.repository.QuotationSupporterRepository;
import com.stg.repository.UserComboRepository;
import com.stg.service.BackofficeService;
import com.stg.service.ComboSuggestionService;
import com.stg.service.PotentialCustomerDirectService;
import com.stg.service.dto.DeleteIdsReq;
import com.stg.service.dto.combo.ComboSuggestionDto;
import com.stg.service.dto.combo.ComboSuggestionResponse;
import com.stg.service.dto.mbal.DiscountCode;
import com.stg.service.dto.mbal.ProductType;
import com.stg.service.dto.potentialcustomer.CreateBmhnReq;
import com.stg.service.dto.potentialcustomer.CreateBmhnRes;
import com.stg.service.dto.potentialcustomer.InitDirectReq;
import com.stg.service.dto.potentialcustomer.InitDirectRes;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectExportDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectSubmitResp;
import com.stg.service.dto.potentialcustomer.PotentialCustomerDirectSubmitStatusReq;
import com.stg.service.dto.potentialcustomer.SearchDirectPotentialCustomerRes;
import com.stg.service.dto.quotation.QuotationAssuredDto;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service.dto.quotation.QuotationProductDto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.FlexibleCommon.ReferrerInput;
import com.stg.service3rd.mbal.dto.MbalBranchDto;
import com.stg.service3rd.mbal.dto.MbalICDto;
import com.stg.service3rd.mbal.dto.MbalRMDto;
import com.stg.service3rd.mbal.dto.MicAdditionalProduct;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import com.stg.utils.AppUtil;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq.PRODUCT_DEFAULT;

@Slf4j
@Service
@RequiredArgsConstructor
public class PotentialCustomerDirectServiceImpl implements PotentialCustomerDirectService {

    private static final int DIRECT_SUBMIT_MAX_RETRY = 5;
    private static final int DIRECT_SUBMIT_RETRY_PAGE_SIZE = 100;

    private static final long ONE_THOUSAND = 1000;
    private static final long ONE_MILLION = 1000 * 1000;
    private static final long ONE_BILLION = 1000 * 1000 * 1000;

    private static final String ONE_THOUSAND_TEXT = "nghìn";
    private static final String ONE_MILLION_TEXT = "triệu";
    private static final String ONE_BILLION_TEXT = "tỷ";

    private static final String PACKAGE_COPPER = "Gói Đồng";
    private static final String PACKAGE_SILVER = "Gói Bạc";
    private static final String PACKAGE_GOLD = "Gói Vàng";
    private static final String PACKAGE_PLATINUM = "Gói Bạch Kim";
    private static final String PACKAGE_DIAMOND = "Gói Kim Cương";

    private static final String NATIONALITY_CODE_DEFAULT = "VN";

    private static final String MIC_CHECK = "Ngoại trú";

    private static final String MIC_YES_TEXT = "C";

    private final PotentialCustomerRepository potentialCustomerRepository;

    private final PotentialCustomerDirectRepository potentialCustomerDirectRepository;

    private final QuotationHeaderRepository quotationHeaderRepository;

    private final QuotationSupporterRepository quotationSupporterRepository;

    private final UserComboRepository userComboRepository;

    private final ComboSuggestionService comboSuggestionService;

    private final ObjectMapper objectMapper;
    private final Gson gson;

    private final PotentialCustomerDirectSubmitStatusRepository potentialCustomerDirectSubmitStatusRepository;

    private final MbalApi3rdService mbalApi3rdService;

    private final BackofficeService backofficeService;

    @Override
    public Page<SearchDirectPotentialCustomerRes> search(Pageable pageable, String query, LocalDate date,
            LocalDate from, LocalDate to, String createdBy) {
        return potentialCustomerDirectRepository.search(pageable, createdBy, query, from, to)
                .map(SearchDirectPotentialCustomerRes::of);
    }

    @Override
    public Page<SearchDirectPotentialCustomerRes> searchByPotentialCustomer(Pageable pageable, Long potentialCustomerId) {
        PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(potentialCustomerId, null);
        return potentialCustomerDirectRepository.search(pageable, null, null, null, null, potentialCustomer)
                .map(SearchDirectPotentialCustomerRes::of);
    }

    @Override
    public PotentialCustomerDirectDetailDto get(Long id, String createdBy) {
        PotentialCustomerDirect direct = potentialCustomerDirectRepository.getById(id, createdBy);

        PotentialCustomerDirectDetailDto dto = PotentialCustomerDirectDetailDto.of(direct);

        if (!StringUtils.hasText(createdBy) && direct.getQuotationHeader() != null) {
            QuotationHeader quotation = direct.getQuotationHeader();

            Long rmInfoId = quotation.getReferrer() != null ? quotation.getReferrer().getId() : null;
            Long icInfoId = quotation.getSupporter() != null ? quotation.getSupporter().getId() : null;

            QuotationSupporter rmInfo = null;
            QuotationSupporter icInfo = null;

            if (rmInfoId != null) {
                rmInfo = quotationSupporterRepository.findById(rmInfoId).orElse(null);
            }
            if (icInfoId != null) {
                icInfo = quotationSupporterRepository.findById(icInfoId).orElse(null);
            }

            if (rmInfo != null) {
                ReferrerInput rm = new ReferrerInput();
                rm.setName(rmInfo.getName());
                rm.setCode(rmInfo.getCode());
                rm.setPhoneNumber(rmInfo.getPhone());
                rm.setEmail(rmInfo.getEmail());
                rm.setCode(rmInfo.getCode());
                rm.setBranchCode(rmInfo.getBranchCode());
                rm.setBranchName(rmInfo.getBranchName());
                dto.setRmInfo(rm);
            }

            if (icInfo != null) {
                ReferrerInput ic = new ReferrerInput();
                ic.setName(icInfo.getName());
                ic.setCode(icInfo.getCode());
                ic.setBranchCode(icInfo.getBranchCode());
                ic.setBranchName(icInfo.getBranchName());
                dto.setIcInfo(ic);
            }

            OccupationResp occupation = backofficeService
                    .getOccupationById(dto.getPotentialCustomer().getOccupationId().longValue());
            dto.getPotentialCustomer().setOccupation(occupation.getNameVn());
        }

        return dto;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        this.delete(new DeleteIdsReq(List.of(id)));
    }

    @Override
    @Transactional
    public void delete(DeleteIdsReq req) {
        if (req.getIds().isEmpty()) {
            return;
        }
        String username = AppUtil.getCurrentUsername();
        int updated = potentialCustomerDirectRepository.updateToRaw(req.getIds(), username);
        if (updated != req.getIds().size()) {
            throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Đã có lỗi xảy ra"));
        }
        List<Long> quotationHeaderIds = potentialCustomerDirectRepository.getQuotationIdByDirectIds(req.getIds());
        quotationHeaderRepository.updateToRaw(quotationHeaderIds);
        List<Long> useComboIds = potentialCustomerDirectRepository.getUserComboIdByDirectIds(req.getIds());
        userComboRepository.updateToRaw(useComboIds, username);
    }

    @Override
    @Transactional
    public InitDirectRes create(InitDirectReq input) {
        PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(input.getPotentialCustomerId(),
                AppUtil.getCurrentUsername());
        PotentialCustomerDirect direct = PotentialCustomerDirect.builder().state(DirectState.INIT_DIRECT)
                .potentialCustomer(potentialCustomer).appStatus(AppStatus.DRAFT).raw(Boolean.TRUE).build();
        potentialCustomerDirectRepository.save(direct);
        return new InitDirectRes(direct.getId());
    }

    @Override
    public InitDirectRes update(InitDirectReq input) {
        return null;
    }

    @Override
    @Transactional
    public CreateBmhnRes createBmhn(CreateBmhnReq request) {
        String createdBy = AppUtil.getCurrentUsername();
        PotentialCustomerDirect direct = potentialCustomerDirectRepository.getById(request.getDirectId(), createdBy);

        if (direct.getState() != DirectState.INIT_DIRECT && direct.getState() != DirectState.CREATE_BMHN) {
            throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Đã có lỗi xảy ra"));
        }

        direct.setState(DirectState.CREATE_BMHN);

        CreateBmhnRes res = new CreateBmhnRes();

        if (direct.getUserCombo() != null && request.getComboCode().equals(direct.getUserCombo().getComboCode())) {
            res.setId(direct.getUserCombo().getId());
        } else {

            PotentialCustomer potentialCustomer = direct.getPotentialCustomer();

            ComboSuggestionDto dto = new ComboSuggestionDto();
            dto.setDob(DateUtil.localDateTimeToString(potentialCustomer.getDob(), DateUtil.DATE_DMY));
            dto.setComboCode(request.getComboCode());
            dto.setGender(potentialCustomer.getGender());
            dto.setInputAmount(potentialCustomer.getInputAmount());

            ResponseEntity<ComboSuggestionResponse> response = comboSuggestionService.suggestCombo(dto, false);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Đã có lỗi xảy ra"));
            }
            ComboSuggestionResponse resBody = response.getBody();
            res.setData(resBody.getData());
            res.setId(resBody.getId());

            if (direct.getUserCombo() != null) {
                userComboRepository.updateToRaw(List.of(direct.getUserCombo().getId()), createdBy);
            }

            direct.setUserCombo(new UserCombo(resBody.getId()));
            direct.setRaw(false);

            potentialCustomerDirectRepository.save(direct);

        }

        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QuotationDto generateQuotation(Long id) {
        String username = AppUtil.getCurrentUsername();

        PotentialCustomerDirect direct = potentialCustomerDirectRepository.getById(id, username);

        QuotationAssuredDto assured = new QuotationAssuredDto();
        assured.setIdentificationType(direct.getPotentialCustomer().getIdentificationType());
        assured.setIdentificationId(direct.getPotentialCustomer().getIdentificationId());
        assured.setFullName(direct.getPotentialCustomer().getFullName());
        assured.setDob(direct.getPotentialCustomer().getDob());
        assured.setGender(direct.getPotentialCustomer().getGender());
        assured.setEmail(direct.getPotentialCustomer().getEmail());
        assured.setPhoneNumber(direct.getPotentialCustomer().getPhoneNumber());
        assured.setOccupationId(direct.getPotentialCustomer().getOccupationId());
        assured.setNationalityCode("VN");

        QuotationDto quotation = new QuotationDto();
        quotation.setType(QuotationType.ULRP_3_0);
        quotation.setCustomer(assured);
        quotation.setCustomerIsAssured(true);
        quotation.setDiscountCode(DiscountCode.NOT_APPLY);
        quotation.setRaiderDeductFund(false);

        Map<String, Object> attributes;

        if (direct.getUserCombo() != null) {

            try {
                attributes = objectMapper.readValue(direct.getUserCombo().getAttributes(), Map.class);
                quotation.setPackageBenefitType(BenefitType.BASIC);
                quotation.setPackagePremiumTerm((Integer) attributes.get(UserComboAttribute.FEE_PAYMENT_TIME));
                quotation.setPackagePaymentPeriod(PaymentPeriod.ANNUAL);
                quotation.setPackagePeriodicPremium(
                        stringToNumber((String) attributes.get(UserComboAttribute.MBAL_FEE_YEAR)));

                assured.setAdditionalProducts(
                        List.of(new QuotationProductDto(ProductType.COI_RIDER, null, null, null)));

                String packageName = (String) attributes.get(UserComboAttribute.PACKAGE_NAME);
                int nhom = 1;
                if (packageName.startsWith(PACKAGE_COPPER)) {
                    nhom = 1;
                } else if (packageName.startsWith(PACKAGE_SILVER)) {
                    nhom = 2;
                } else if (packageName.startsWith(PACKAGE_GOLD)) {
                    nhom = 3;
                } else if (packageName.startsWith(PACKAGE_PLATINUM)) {
                    nhom = 4;
                } else if (packageName.startsWith(PACKAGE_DIAMOND)) {
                    nhom = 5;
                }

                MicAdditionalProduct micRequest = new MicAdditionalProduct();
                micRequest.setNhom(nhom);
                if (packageName.endsWith(MIC_CHECK)) {
                    micRequest.setBs1(MIC_YES_TEXT);
                }
                assured.setMicRequest(micRequest);

            } catch (Exception e) {
                log.debug(e.getMessage());
            }

        }

        FlexibleCommon.BeneficiaryOutput beneficiary = new FlexibleCommon.BeneficiaryOutput();
        beneficiary.setIdentificationType(direct.getPotentialCustomer().getIdentificationType());
        beneficiary.setIdentificationId(direct.getPotentialCustomer().getIdentificationId());
        beneficiary.setFullName(direct.getPotentialCustomer().getFullName());
        beneficiary.setDob(DateUtil.localDateTimeToString(direct.getPotentialCustomer().getDob()));
        beneficiary.setGender(direct.getPotentialCustomer().getGender());
        beneficiary.setEmail(direct.getPotentialCustomer().getEmail());
        beneficiary.setPhoneNumber(direct.getPotentialCustomer().getPhoneNumber());
        beneficiary.setOccupationId(direct.getPotentialCustomer().getOccupationId());
        beneficiary.setNationalityCode(NATIONALITY_CODE_DEFAULT);

        quotation.setBeneficiary(beneficiary);

        return quotation;
    }

    private BigDecimal stringToNumber(String text) {

        long t = 1;

        if (text.endsWith(ONE_BILLION_TEXT)) {
            text = text.replace(ONE_BILLION_TEXT, "").trim();
            t = ONE_BILLION;
        }

        if (text.endsWith(ONE_MILLION_TEXT)) {
            text = text.replace(ONE_MILLION_TEXT, "").trim();
            t = ONE_MILLION;
        }

        if (text.endsWith(ONE_THOUSAND_TEXT)) {
            text = text.replace(ONE_THOUSAND_TEXT, "").trim();
            t = ONE_THOUSAND;
        }

        BigDecimal bigDecimal = new BigDecimal(text);

        return bigDecimal.multiply(new BigDecimal(t));
    }

    @Override
    @Transactional
    public void saveSubmitStatus(PotentialCustomerDirect direct, PotentialCustomerDirectSubmitStatusReq request) {

        PotentialCustomerDirectSubmitStatus submitStatus = potentialCustomerDirectSubmitStatusRepository
                .findByPotentialCustomerDirect(direct).orElse(new PotentialCustomerDirectSubmitStatus());

        submitStatus.setPotentialCustomerDirect(direct);
        submitStatus.setStatus(request.getStatus());

        if (DirectSubmitStatus.ERROR.equals(submitStatus.getStatus())) {
            if (!StringUtils.hasLength(request.getErrorMessage())) {
                throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "error_message is required"));
            }
            submitStatus.setRetry(submitStatus.getRetry() == null ? 0 : submitStatus.getRetry() + 1);

            submitStatus.setErrorHttpCode(request.getErrorHttpCode());
            submitStatus.setErrorMessage(request.getErrorMessage());
        }

        if (DirectSubmitStatus.SUCCESS.equals(submitStatus.getStatus()) && AppStatus.DRAFT.equals(direct.getAppStatus())) {
            direct.setAppStatus(AppStatus.SUBMITTED);
        }

        potentialCustomerDirectSubmitStatusRepository.save(submitStatus);

        direct.setApplicationNumber(request.getApplicationNumber());
        direct.setCifNumber(request.getCifNumber());

        potentialCustomerDirectRepository.save(direct);
    }

    @Override
    @Transactional
    @Async
    public Future<DirectSubmitStatus> submit(SubmitPotentialCustomerReq request) {
        // verify field required
        if (request.getRelationshipManager() == null || request.getInsuranceConsultant() == null) {
            PotentialCustomerDirectSubmitStatusReq status = new PotentialCustomerDirectSubmitStatusReq();

            String errorMsg = "Not found: ";
            errorMsg += "RM=" + (request.getRelationshipManager() != null ? gson.toJson(request.getRelationshipManager()) : null);
            errorMsg += ", IC=" + (request.getInsuranceConsultant() != null ? gson.toJson(request.getInsuranceConsultant()) : null);
            status.setErrorMessage(errorMsg);

            status.setStatus(DirectSubmitStatus.ERROR);
            status.setApplicationNumber(request.getDirect().getApplicationNumber());
            status.setCifNumber(request.getDirect().getCifNumber());

            saveSubmitStatus(request.getDirect(), status);
        }
        // do send to MBAL
        else {
            try {
                mbalApi3rdService.submitPotentialCustomer(request); // ? check code success...
                saveSubmitStatus(request.getDirect(), PotentialCustomerDirectSubmitStatusReq.of(request, DirectSubmitStatus.SUCCESS));
                return AsyncResult.forValue(DirectSubmitStatus.SUCCESS);
            } catch (Exception e) {
                IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
                PotentialCustomerDirectSubmitStatusReq status = PotentialCustomerDirectSubmitStatusReq.of(request, DirectSubmitStatus.ERROR);
                status.setErrorHttpCode(errorObject.getHttpStatus());
                status.setErrorMessage(errorObject.getErrorMessage());
                saveSubmitStatus(request.getDirect(), status);
            }
        }

        return AsyncResult.forValue(DirectSubmitStatus.ERROR);
    }

    @Override
    public List<SubmitPotentialCustomerReq> getNextRetry(int page) {
        Page<PotentialCustomerDirect> directs = potentialCustomerDirectRepository.findNextRetry(
                DirectSubmitStatus.ERROR, DIRECT_SUBMIT_MAX_RETRY,
                PageRequest.of(page, DIRECT_SUBMIT_RETRY_PAGE_SIZE, Sort.by(Order.asc("submitStatus.id"))));

        return directs.getContent().stream().map(this::buildSubmitPotentialCustomerReq).collect(Collectors.toList());
    }

    private SubmitPotentialCustomerReq buildSubmitPotentialCustomerReq(PotentialCustomerDirect direct) {
        QuotationHeader quotation = direct.getQuotationHeader();

            Long rmInfoId = quotation.getReferrer() != null ?  quotation.getReferrer().getId() : null;
            Long icInfoId = quotation.getSupporter() != null ? quotation.getSupporter().getId() : null;

        QuotationSupporter rmInfo = null;
        QuotationSupporter icInfo = null;

        if (rmInfoId != null) {
            rmInfo = quotationSupporterRepository.findById(rmInfoId).orElse(null);
        }
        if (icInfoId != null) {
            icInfo = quotationSupporterRepository.findById(icInfoId).orElse(null);
        }

        MbalRMDto rm = null;
        MbalICDto ic = null;

        if (rmInfo != null && icInfo != null) {
            rm = new MbalRMDto(rmInfo.getCode(), rmInfo.getName(), rmInfo.getPhone(), rmInfo.getEmail(),
                    new MbalBranchDto(rmInfo.getBranchCode(), rmInfo.getBranchName()));
            ic = new MbalICDto(icInfo.getCode(), icInfo.getName(), icInfo.getPhone(),
                    new MbalBranchDto(rmInfo.getBranchCode(), rmInfo.getBranchName()));
        }

        return SubmitPotentialCustomerReq.of(direct, rm, ic, PRODUCT_DEFAULT);
    }

    @Override
    public PotentialCustomerDirectSubmitResp submit(Long id) {
        PotentialCustomerDirect direct = potentialCustomerDirectRepository.getById(id, null);
        if(direct.getSubmitStatus() == null) {
            return new PotentialCustomerDirectSubmitResp(DirectSubmitStatus.ERROR);
        }
        if (direct.getSubmitStatus().getStatus() == DirectSubmitStatus.SUCCESS) {
            return new PotentialCustomerDirectSubmitResp(DirectSubmitStatus.SUCCESS);
        }
        SubmitPotentialCustomerReq req = buildSubmitPotentialCustomerReq(direct);
        Future<DirectSubmitStatus> future = submit(req);
        try {
            return new PotentialCustomerDirectSubmitResp(future.get());
        } catch (Exception e) {
            throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Xử lý lỗi"));
        }
    }

    @Override
    public Page<SearchDirectPotentialCustomerRes> adminSearch(Pageable pageable, String query, LocalDate from,
            LocalDate to, AppStatus appStatus) {
        return potentialCustomerDirectRepository.search(pageable, query, from, to, appStatus, null).map(d -> {
            SearchDirectPotentialCustomerRes dto = SearchDirectPotentialCustomerRes.of(d);

            QuotationHeader quotation = d.getQuotationHeader();

            if (quotation != null) {
                Long rmInfoId = quotation.getReferrer() != null ? quotation.getReferrer().getId() : null;
                Long icInfoId = quotation.getSupporter() != null ? quotation.getSupporter().getId() : null;

                QuotationSupporter rmInfo = null;
                QuotationSupporter icInfo = null;

                if (rmInfoId != null) {
                    rmInfo = quotationSupporterRepository.findById(rmInfoId).orElse(null);
                }
                if (icInfoId != null) {
                    icInfo = quotationSupporterRepository.findById(icInfoId).orElse(null);
                }

                if (rmInfo != null) {
                    ReferrerInput rm = new ReferrerInput();
                    rm.setName(rmInfo.getName());
                    rm.setCode(rmInfo.getCode());
                    rm.setPhoneNumber(rmInfo.getPhone());
                    rm.setEmail(rmInfo.getEmail());
                    rm.setBranchCode(rmInfo.getBranchCode());
                    rm.setBranchName(rmInfo.getBranchName());
                    dto.setRmInfo(rm);
                }

                if (icInfo != null) {
                    ReferrerInput ic = new ReferrerInput();
                    ic.setName(icInfo.getName());
                    ic.setCode(icInfo.getCode());
                    ic.setBranchCode(icInfo.getBranchCode());
                    ic.setBranchName(icInfo.getBranchName());
                    dto.setIcInfo(ic);
                }
            }

            return dto;
        });
    }

    @Override
    public void adminExport(Pageable pageable, String query, LocalDate from, LocalDate to, AppStatus appStatus,
            ExportType type, HttpServletResponse response) {
        Page<SearchDirectPotentialCustomerRes> page = adminSearch(
                PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort()), query, from, to, appStatus);

        try {
            List<PotentialCustomerDirectExportDto> exportData = page.getContent().stream()
                    .map(PotentialCustomerDirectExportDto::of).collect(Collectors.toList());
            if (ExportType.CSV.equals(type)) {
                OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Direct-List-", response);
                OpenCsvUtil.write(response.getWriter(), exportData, PotentialCustomerDirectExportDto.class);
            } else {
                OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Direct-List-",
                        response);
                List<String> headers = Arrays.asList("MB ID", "Lead ID", "Tên KH", "Thông tin RM", "Thông tin IC", "GTTT",
                        "Trạng thái HSYCBH", "Ngày tham gia", "Nguồn");
                Field[] fields = PotentialCustomerDirectExportDto.class.getDeclaredFields();
                ExcelUtils.exportExcel(exportData, fields, headers, 2, "templates/direct_list.xlsx", response);
            }
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("[MINI]--Failed when export waiting payments. Detail {}", e.getMessage());
            throw new ApplicationException("[Failed when export refer");
        }
    }

}
