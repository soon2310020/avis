package com.stg.service.impl;

import com.google.gson.Gson;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.InsurancePackage;
import com.stg.entity.InsuranceRequest;
import com.stg.entity.QuestionResponse;
import com.stg.errors.InsuranceRequestNotFoundException;
import com.stg.repository.*;
import com.stg.service.InsuranceRequestService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.external.PackageNameEnum;
import com.stg.service.dto.insurance.*;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static com.stg.entity.QuestionName.*;
import static com.stg.service.dto.external.PackageNameEnum.UR_STYLE;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InsuranceRequestImpl implements InsuranceRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsuranceRequestImpl.class);

    private final InsuranceRequestRepository insuranceRequestRepository;
    private final CustomerRepository customerRepository;
    private final InsurancePackageRepository insurancePackageRepository;
    private final MbalPackageRepository mbalPackageRepository;
    private final MicPackageRepository micPackageRepository;
    private final QuestionResponseRepository questionResponseRepository;
    private final PrimaryInsuredRepository primaryInsuredRepository;
    private final AdditionalInsuredRepository additionalInsuredRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final InsuranceContractImpl insuranceContract;
    private static final String YES = "C";

    @Autowired
    private final ModelMapper mapper;

    @Autowired
    private final Gson gson;

    @Override
    public PaginationResponse<InsuranceRequestDto> list(Long user, int page, int size, String query, String segment, Boolean status, String category, String packageName) {
        Set<Integer> filterIdPackage = new HashSet<>();
        String filterUrStyle = filterPackage(packageName, filterIdPackage);
        LOGGER.info("filterIdPackage=" + filterIdPackage + ", filterUrStyle=" + filterUrStyle);

        PaginationResponse<InsuranceRequestDto> response = new PaginationResponse<>();
        List<Boolean> listStatus = setStatusStr(status);
        List<InsuranceRequest> insuranceRequests = insuranceRequestRepository.listInsuranceRequest(page, size, query, segment, listStatus, category, filterUrStyle, filterIdPackage);
        long total = insuranceRequestRepository.totalInsuranceRequest(query, segment, listStatus, category, filterUrStyle, filterIdPackage);
        String nameFreeStyle = getPackageNameFreeStyle();
        List<InsuranceRequestDto> requestDtos = mapIllustrationRequestToDto(insuranceRequests, nameFreeStyle);
        response.setData(requestDtos);
        response.setTotalItem(total);
        response.setPageSize(size);
        return response;
    }

    public String filterPackage(String packageName, Set<Integer> listIdPackage) {
        String hasUrStyle = "";
        if (packageName != null && !"".equals(packageName)) {
            List<InsurancePackage> insurancePackagesByName = insurancePackageRepository.findByPackageName(packageName);
            if (insurancePackagesByName != null) {
                hasUrStyle = "No";
                for (InsurancePackage insurancePackage : insurancePackagesByName) {
                    listIdPackage.add(insurancePackage.getId());
                    // filter gói phong cách
                    if (insurancePackage.getType().equals(Constants.PackageType.FREE_STYLE.name())) {
                        hasUrStyle = "Yes";
                    }
                }
            }
        }
        return hasUrStyle;
    }

    private String getPackageNameFreeStyle() {
        List<InsurancePackage> packageFreeStyle = insurancePackageRepository.findByType(Constants.PackageType.FREE_STYLE.name());
        String nameFreeStyle = PackageNameEnum.UR_STYLE.getVal();
        if (!packageFreeStyle.isEmpty()) {
            nameFreeStyle = packageFreeStyle.get(0).getPackageName();
        }
        return nameFreeStyle;
    }

    private static List<Boolean> setStatusStr(Boolean status) {
        List<Boolean> listStatus = new ArrayList<>();
        if (status != null) {
            listStatus.add(status);
        } else {
            listStatus.add(true);
            listStatus.add(false);
        }
        return listStatus;
    }

    private List<InsuranceRequestDto> mapIllustrationRequestToDto(List<InsuranceRequest> insuranceRequests, String nameFreeStyle) {
        List<InsuranceRequestDto> illustrationRequestDtos = new ArrayList<>();
        for (InsuranceRequest insuranceRequest : insuranceRequests) {
            String timeFrequency = insuranceRequest.getIllustrationTable().getTimeFrequency();
            InsuranceRequestDto insuranceRequestDto = mapper.map(insuranceRequest, InsuranceRequestDto.class);
            if (insuranceRequest.getCustomer() != null && insuranceRequest.getCustomer().getSegment() != null) {
                insuranceRequestDto.setSegment(insuranceRequest.getCustomer().getSegment().getGain());
            }
            if (insuranceRequest.getInsurancePackage() == null) {
                insuranceRequestDto.setInsurancePackage(new InsurancePackageDto().setPackageName(nameFreeStyle));
            }
            insuranceRequestDto.getInsurancePackage().setMbalFeePaymentTime(timeFrequency);
            illustrationRequestDtos.add(insuranceRequestDto);
        }
        return illustrationRequestDtos;
    }

    @Override
    public InsuranceRequestDto insuranceRequestDetail(Long id, Long requestId) {
        Optional<InsuranceRequest> request = insuranceRequestRepository.findById(requestId);
        if (!request.isPresent()) {
            throw new InsuranceRequestNotFoundException("Không tồn tại hợp đồng yêu cầu");
        }
        InsuranceRequest insuranceRequest = request.get();
        InsuranceRequestDto insuranceRequestDto = mapper.map(insuranceRequest, InsuranceRequestDto.class);
        // get address dto
        insuranceRequestDto.getCustomer().setAddress(insuranceRequest.getCustomer().fullAddress());

        List<QuestionResponse> questionResponses = questionResponseRepository.findByInsuranceRequestId(insuranceRequest.getId());
        if (questionResponses.size() == 1) {
            insuranceRequestDto.setComboBigQuestion(gson.fromJson(questionResponses.get(0).getPropertyValue(), CreateIllustrationTableDto.ComboBigQuestion.class));
        } else {
            CreateIllustrationTableDto.ComboSmallQuestion comboSmallQuestion = new CreateIllustrationTableDto.ComboSmallQuestion();
            comboSmallQuestion.setOqs(convertQuestionResponse(questionResponses.stream().filter(o -> o.getId().getPropertyKey().equals(OTHER_QUESTION.name())).findFirst()));
            comboSmallQuestion.setC1(convertQuestionResponse(questionResponses.stream().filter(o -> o.getId().getPropertyKey().equals(QUESTION_ONE.name())).findFirst()));
            comboSmallQuestion.setC2(convertQuestionResponse(questionResponses.stream().filter(o -> o.getId().getPropertyKey().equals(QUESTION_TWO.name())).findAny()));
            comboSmallQuestion.setC3(convertQuestionResponse(questionResponses.stream().filter(o -> o.getId().getPropertyKey().equals(QUESTION_THREE.name())).findFirst()));
            insuranceRequestDto.setComboSmallQuestion(comboSmallQuestion);
        }

        if (insuranceRequest.getPackageType().equals(Constants.PackageType.FLEXIBLE)) {

            Long customerId = insuranceRequest.getCustomer().getId();
            InsuranceContractDto insuranceContractDto = insuranceContract.buildDetailContract(new InsuranceContractDto(), customerId, requestId);
            insuranceRequestDto.setCustomerAdditionalProductDTOS(insuranceContractDto.getCustomerAdditionalProductDTOS());
            insuranceRequestDto.setPrimaryInsuredDTO(insuranceContractDto.getPrimaryInsuredDTO());
            insuranceRequestDto.setPrimaryProductDTO(insuranceContractDto.getPrimaryProductDTO());
            if (insuranceContractDto.getPrimaryInsuredDTO() != null) {
                insuranceRequestDto.setPrimaryInsuredAdditionalProductDTOS(insuranceContractDto.getPrimaryInsuredDTO().getAdditionalProductDTOS());
            }
            insuranceRequestDto.setAdditionalAssuredOutputs(insuranceContractDto.getAdditionalAssuredOutputs());
            insuranceRequestDto.setBeneficiaryDTOS(insuranceContractDto.getBeneficiaryDTOS());

        } else if (insuranceRequest.getPackageType().equals(Constants.PackageType.FIX_COMBO) || insuranceRequest.getPackageType().equals(Constants.PackageType.FREE_STYLE)) {
            // NĐBH chính
            PrimaryInsuredDTO primaryInsuredDTO = mapper.map(insuranceRequestDto.getCustomer(), PrimaryInsuredDTO.class);
            insuranceRequestDto.setPrimaryInsuredDTO(primaryInsuredDTO);
            // Người thụ hưởng
            List<BeneficiaryDTO> beneficiaryDTOS = new ArrayList<>();
            BeneficiaryDTO beneficiaryDTO = mapper.map(insuranceRequestDto.getCustomer(), BeneficiaryDTO.class);
            beneficiaryDTOS.add(beneficiaryDTO);
            insuranceRequestDto.setBeneficiaryDTOS(beneficiaryDTOS);
        }

        return insuranceRequestDto;
    }

    @Override
    public void exportList(Long user, String query, String type, String segment, HttpServletResponse response, Boolean status, String category, String packageName) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportListInsuranceRequest with user=" + user + ", query=" + query + ", type=" + type);
        Set<Integer> filterIdPackage = new HashSet<>();
        String filterUrStyle = filterPackage(packageName, filterIdPackage);
        LOGGER.info("filterIdPackage=" + filterIdPackage + ", filterUrStyle=" + filterUrStyle);

        List<Boolean> listStatus = setStatusStr(status);
        List<InsuranceRequest> insuranceRequests = insuranceRequestRepository.findInsuranceRequestByQuery(query, segment, listStatus, category, filterUrStyle, filterIdPackage);
        String nameFreeStyle = getPackageNameFreeStyle();
        List<InsuranceRequestDto> insuranceRequestsDtos = mapIllustrationRequestToDto(insuranceRequests, nameFreeStyle);

        List<InsuranceRequestExportListDTO> csvDTOList = mapListToCSV(insuranceRequestsDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)) {
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-yeu-cau-bao-hiem-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, InsuranceRequestExportListDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-yeu-cau-bao-hiem-excel-", response);
            List<String> headers = Arrays.asList("MB_ID", "Họ và tên", "Gói bảo hiểm", "Thời gian đóng phí", "Ngày tham gia", "Trạng thái", "Phân khúc");
            Field[] fields = InsuranceRequestExportListDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_insurance_request.xlsx", response);
        }
    }

    public List<InsuranceRequestExportListDTO> mapListToCSV(List<InsuranceRequestDto> listDtos) {
        List<InsuranceRequestExportListDTO> csvDTOList = new ArrayList<>();
        for (InsuranceRequestDto eachDto : listDtos) {
            InsuranceRequestExportListDTO customerListExportDTO = new InsuranceRequestExportListDTO();
            customerListExportDTO.mbId = eachDto.getCustomer().getMbId();
            customerListExportDTO.fullName = eachDto.getCustomer().getFullName();
            customerListExportDTO.packageName = getPackageName(eachDto.getInsurancePackage());
            customerListExportDTO.creationTime = eachDto.getCreationTime();
            customerListExportDTO.status = eachDto.isStatus() ? "Đang tiến hành" : "Từ chối";
            customerListExportDTO.segment = eachDto.getSegment();
            customerListExportDTO.mbalFeePaymentTime = eachDto.getInsurancePackage().getMbalFeePaymentTime();
//            if (InsuranceRequestStatus.FALSE.name().equalsIgnoreCase(String.valueOf(eachDto.isStatus()))) {
//                customerListExportDTO.status = InsuranceRequestStatus.FALSE.label;
//            } else {
//                customerListExportDTO.status = "Not found";
//            }

            csvDTOList.add(customerListExportDTO);
        }
        return csvDTOList;
    }

    private String getPackageName(InsurancePackageDto insurancePackage) {
        String packageName = UR_STYLE.getVal();
        if (insurancePackage != null) {
            packageName = insurancePackage.getPackageName();
        }
        return packageName;
    }

    private String convertQuestionResponse(Optional<QuestionResponse> response) {
        if (response.isPresent()) {
            return gson.fromJson(response.get().getPropertyValue(), String.class);
        }
        return "";
    }

}
