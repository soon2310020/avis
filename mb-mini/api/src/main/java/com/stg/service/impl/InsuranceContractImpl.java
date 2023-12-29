package com.stg.service.impl;

import com.google.gson.Gson;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.*;
import com.stg.entity.user.User;
import com.stg.errors.InsuranceContractNotFoundException;
import com.stg.errors.UserHasNoPermissionException;
import com.stg.errors.UserNotFoundException;
import com.stg.repository.*;
import com.stg.repository.InsuranceContractRepository;
import com.stg.repository.InsurancePackageRepository;
import com.stg.repository.InsurancePaymentRepository;
import com.stg.repository.InsuredMbalRepository;
import com.stg.repository.ProductMbalRepository;
import com.stg.service.IdentificationService;
import com.stg.service.InsuranceContractService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.external.requestFlexible.FlexibleSubmitMicQuestionRequest;
import com.stg.service.dto.external.requestFlexible.FlexibleSubmitQuestionRequest;
import com.stg.service.dto.insurance.*;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import com.stg.utils.ProductType;
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
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stg.service.dto.external.PackageNameEnum.UR_STYLE;
import static com.stg.utils.Common.formatCurrency;
import static com.stg.utils.CommonMessageError.MSG38;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InsuranceContractImpl implements InsuranceContractService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsuranceContractImpl.class);

    private final InsuranceContractRepository insuranceContractRepository;
    private final InsurancePackageRepository insurancePackageRepository;
    private final ProductMbalRepository productMbalRepository;
    private final InsuredMbalRepository insuredMbalRepository;
    private final IdentificationService identificationService;
    private final InsurancePaymentRepository insurancePaymentRepository;

    private final AdditionalProductRepository additionalProductRepository;
    private final PrimaryInsuredRepository primaryInsuredRepository;
    private final AdditionalInsuredRepository additionalInsuredRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final PrimaryProductRepository primaryProductRepository;

    private final UserRepository userRepository;

    @Autowired
    private final ModelMapper mapper;

    @Autowired
    private final Gson gson;

    @Override
    public PaginationResponse<InsuranceContractListDto> list(Long user, int page, int size, String query, String segment, String packageName, String category){
        LOGGER.info("Starting get list InsuranceContract with idUser=" + user + ", query=" + query + ", packageName=" + packageName);
        PaginationResponse<InsuranceContractListDto> response = new PaginationResponse<>();

        // search and filter by package name
        Set<Integer> listIdPackage = new HashSet<>();
        String hasUrStyle = filterPackage(packageName, listIdPackage);
        LOGGER.info("filterIdPackage=" + listIdPackage + ", filterUrStyle=" + hasUrStyle);

        List<InsuranceContractVo> insuranceContracts = insuranceContractRepository.listInsuranceContract(page, size, query.trim(), segment, listIdPackage, hasUrStyle, category);

        // update thời gian đóng phí và định kỳ đóng phí Flexible
        Map<Long, PrimaryProduct> mapPrimaryProduct = getMapPrimaryProduct(insuranceContracts);
        List<InsuranceContractListDto> contractListDtos = buildResponseContractList(insuranceContracts, mapPrimaryProduct);

        long total = insuranceContractRepository.totalInsuranceContract(query.trim(), segment, listIdPackage, hasUrStyle, category);

        response.setData(contractListDtos);
        response.setTotalItem(total);
        response.setPageSize(size);
        return response;
    }

    private Map<Long, PrimaryProduct> getMapPrimaryProduct(List<InsuranceContractVo> insuranceContracts) {
        List<Long> requestIds = insuranceContracts.stream()
                .filter(vo -> vo.getPackageType().equals(Constants.PackageType.FLEXIBLE.name()) && vo.getInsuranceRequestId() != null)
                .map(InsuranceContractVo::getInsuranceRequestId)
                .collect(Collectors.toList());
        List<PrimaryProduct> primaryProducts = primaryProductRepository.findPrimaryProductByRequestIds(requestIds);
        Map<Long, PrimaryProduct> mapPrimaryProduct = new HashMap<>();
        for (PrimaryProduct primaryProduct : primaryProducts) {
            if (primaryProduct.getInsuranceRequest() != null) {
                mapPrimaryProduct.put(primaryProduct.getInsuranceRequest().getId(), primaryProduct);
            }
        }
        return mapPrimaryProduct;
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

    public List<InsuranceContractListDto> buildResponseContractList(List<InsuranceContractVo> insuranceContractVos, Map<Long, PrimaryProduct> mapPrimaryProduct) {
        List<InsuranceContractListDto> contractListDtos = new ArrayList<>();
        for (InsuranceContractVo insuranceContractVo : insuranceContractVos) {
            InsuranceContractListDto contractDetailDTO = mapper.map(insuranceContractVo, InsuranceContractListDto.class);
            contractDetailDTO.setPackageName(insuranceContractVo.getPackageName() == null ? UR_STYLE.getVal() : insuranceContractVo.getPackageName());

            PrimaryProduct primaryProduct = mapPrimaryProduct.get(insuranceContractVo.getInsuranceRequestId());
            if (primaryProduct != null && insuranceContractVo.getPackageType().equals(Constants.PackageType.FLEXIBLE.name())) {
                contractDetailDTO.setMbalFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
            }

            contractListDtos.add(contractDetailDTO);
        }
        return contractListDtos;
    }

    private List<InsuranceContractDto> getInsuranceContractDtos(List<InsuranceContract> insuranceContracts) {
        Map<String, InsuranceContract> mapTransactionId = insuranceContracts.stream().collect(Collectors.toMap(InsuranceContract::getTransactionId, Function.identity()));
        List<TransactionContractDto> insuranceContractVos = insuranceContractRepository.listTransactionContract(mapTransactionId.keySet());
        Map<String, TransactionContractDto> mapTransactionContract = insuranceContractVos.stream().collect(Collectors.toMap(TransactionContractDto::getTransactionId, Function.identity()));
        List<InsuranceContractDto> contractDtos = mapIllustrationRequestToDto(insuranceContracts, mapTransactionContract);
        return contractDtos;
    }

    private List<InsuranceContractDto> mapIllustrationRequestToDto(List<InsuranceContract> insuranceContracts, Map<String, TransactionContractDto> mapTransactionContract) {
        List<InsuranceContractDto> insuranceContractDtos = new ArrayList<>();
        for (InsuranceContract insuranceContract : insuranceContracts) {
            InsuranceContractDto insuranceContractDto = mapper.map(insuranceContract, InsuranceContractDto.class);
            if (insuranceContract.getCustomer() != null && insuranceContract.getCustomer().getSegment() != null) {
                insuranceContractDto.setSegment(insuranceContract.getCustomer().getSegment().getGain());
            }
            updateStatus(insuranceContractDto);
            insuranceContractDto.setInsurancePackage(buildNameInsurancePackage(insuranceContract.getInsurancePackage()));

            TransactionContractDto insuranceContractVo = mapTransactionContract.get(insuranceContract.getTransactionId());
            if (insuranceContractVo != null) {
                insuranceContractDto.setMbTransactionId(insuranceContractVo.getTransactionId());
                insuranceContractDto.setMbFt(insuranceContractVo.getMbReferenceNumber());
                insuranceContractDto.setMicFtNumber(insuranceContractVo.getMicFtNumber());
                insuranceContractDto.setMbalFtNumber(insuranceContractVo.getMbalFtNumber());
            }

            insuranceContractDtos.add(insuranceContractDto);
        }
        return insuranceContractDtos;
    }

    private InsurancePackageDto buildNameInsurancePackage(InsurancePackage insurancePackage) {
        InsurancePackageDto iPackage;
        if (insurancePackage == null) {
            iPackage = new InsurancePackageDto();
            iPackage.setPackageName(UR_STYLE.getVal());
            iPackage.setIssuer("MBAL + MIC");
        } else {
            iPackage = mapper.map(insurancePackage, InsurancePackageDto.class);
        }
        return iPackage;
    }

    @Override
    public InsuranceContractDto contractThirdPartyDetail(Long idUser, Long contractId) {
        isSuperAdminAndAdmin(idUser);

        InsuranceContract contract = insuranceContractRepository.findById(contractId).orElseThrow(() -> new InsuranceContractNotFoundException(String.format(MSG38, contractId)));

        InsuranceContractDto contractDto = mapper.map(contract, InsuranceContractDto.class);
        contractDto.setCoverageYear(contractDto.getCoverageYear() == null ? "" : contractDto.getCoverageYear() + " năm");
        if (contract.getPackageType() == Constants.PackageType.MBAL) {
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

                List<IdentificationDetailDto> identificationsDto = identificationService.getIdentificationDetailDtos(insuredMbal, null);
                insured.setIdentifications(identificationsDto);

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
        } else if (contract.getPackageType() == Constants.PackageType.ONEID) {
            // bảo hiểm MIC - OneID

        }

        List<IdentificationDetailDto> identificationsDtoCustomer = identificationService.getIdentificationDetailDtos(null, contract.getCustomer());
        contractDto.getCustomer().setIdentifications(identificationsDtoCustomer);
        if (contractDto.getCustomer().getIdentifications().isEmpty()) {
            List<IdentificationDetailDto> identificationsDto = identificationService.getIdentificationDetailCustomer(contractDto.getCustomer());
            contractDto.getCustomer().setIdentifications(identificationsDto);
        }

        Long customerId = contract.getCustomer().getId();
        Long requestId = insuranceContractRepository.findInsuranceRequestIdByContractId(contract.getId());
        buildDetailContract(contractDto, customerId, requestId);

        return contractDto;
    }

    public InsuranceContractDto buildDetailContract(InsuranceContractDto contractDto, Long customerId, Long requestId) {
        if (requestId == null) {
            return contractDto;
        }

        // gói bổ sung của bmbh Customer
        List<AdditionalProductDTO> customerAdditionalProductDTOS = new ArrayList<>();
        List<AdditionalProduct> additionalProductsCustomer = additionalProductRepository.findByInsuranceRequestIdAndCustomer(true, requestId);
        for (AdditionalProduct additionalProduct : additionalProductsCustomer) {
            AdditionalProductDTO additionalProductDTO = mapper.map(additionalProduct, AdditionalProductDTO.class);
            additionalProductDTO.setProductName(additionalProduct.getProductType() == null ? null : ProductType.getProductTypeVal(additionalProduct.getProductType().name()));
            customerAdditionalProductDTOS.add(additionalProductDTO);
        }
        contractDto.setCustomerAdditionalProductDTOS(customerAdditionalProductDTOS);

        // NĐBH chính
        PrimaryInsured primaryInsured = primaryInsuredRepository.findByInsuranceRequestId(requestId);
        if (primaryInsured != null) {
            PrimaryInsuredDTO primaryInsuredDTO = mapper.map(primaryInsured, PrimaryInsuredDTO.class);

            if (primaryInsured.getAppQuestionNumber() != null && primaryInsured.getMbalQuestionResponse() != null) {
                if (primaryInsured.getAppQuestionNumber() == 3) {
                    primaryInsuredDTO.setMbalComboSmallQuestion(gson.fromJson(primaryInsured.getMbalQuestionResponse(), FlexibleSubmitQuestionRequest.ComboSmallQuestion.class));
                } else {
                    primaryInsuredDTO.setMbalComboBigQuestion(gson.fromJson(primaryInsured.getMbalQuestionResponse(), FlexibleSubmitQuestionRequest.ComboBigQuestion.class));
                }
            }
            primaryInsuredDTO.setMicQuestionResponse(primaryInsured.getMicQuestionResponse()
                    == null ? null : gson.fromJson(primaryInsured.getMicQuestionResponse(), FlexibleSubmitMicQuestionRequest.MicQuestion.class));
            contractDto.setPrimaryInsuredDTO(primaryInsuredDTO);
            // SP chính
            PrimaryProduct primaryProduct = primaryProductRepository.findByInsuranceRequestId(requestId);
            if (primaryProduct != null) {
                PrimaryProductDTO primaryProductDTO = mapper.map(primaryProduct, PrimaryProductDTO.class);
                contractDto.setPrimaryProductDTO(primaryProductDTO);
            }

            // SP bổ trợ của NĐBH chính
            List<AdditionalProductDTO> primaryInsuredAdditionalProductDTOS = new ArrayList<>();
            List<AdditionalProduct> primaryInsuredAdditionalProducts = additionalProductRepository.findByPrimaryInsuredId(primaryInsured.getId());
            for (AdditionalProduct additionalProduct : primaryInsuredAdditionalProducts) {
                AdditionalProductDTO additionalProductDTO = mapper.map(additionalProduct, AdditionalProductDTO.class);
                additionalProductDTO.setProductName(additionalProduct.getProductType() == null ? null : ProductType.getProductTypeVal(additionalProduct.getProductType().name()));
                primaryInsuredAdditionalProductDTOS.add(additionalProductDTO);
            }
            primaryInsuredDTO.setAdditionalProductDTOS(primaryInsuredAdditionalProductDTOS);
//            contractDto.setPrimaryInsuredAdditionalProductDTOS(primaryInsuredAdditionalProductDTOS);
        }

        // NĐBHBS và gói SP
        List<AdditionalInsured> additionalInsureds = additionalInsuredRepository.findByInsuranceRequestId(requestId);
        if (!additionalInsureds.isEmpty()) {
            List<AdditionalInsuredDTO> insuredDTOS = new ArrayList<>();

            List<AdditionalProduct> additionalInsuredAdditionalProducts = additionalProductRepository.findByInsuranceRequestIdAndCustomer(false, requestId);
            Map<Long, List<AdditionalProductDTO>> mapAdditionalProduct = new HashMap<>();
            for (AdditionalProduct additionalProduct : additionalInsuredAdditionalProducts) {
                List<AdditionalProductDTO> additionalProducts = new ArrayList<>();
                if (mapAdditionalProduct.get(additionalProduct.getAdditionalInsured().getId()) != null && !mapAdditionalProduct.get(additionalProduct.getAdditionalInsured().getId()).isEmpty()) {
                    additionalProducts = mapAdditionalProduct.get(additionalProduct.getAdditionalInsured().getId());
                }
                AdditionalProductDTO additionalProductDTO = mapper.map(additionalProduct, AdditionalProductDTO.class);
                additionalProductDTO.setProductName(additionalProduct.getProductType() == null ? null : ProductType.getProductTypeVal(additionalProduct.getProductType().name()));
//                    additionalProductDTO.setProductType(additionalProduct.getProductType().name());
                additionalProducts.add(additionalProductDTO);
                mapAdditionalProduct.put(additionalProduct.getAdditionalInsured().getId(), additionalProducts);
            }

            for (AdditionalInsured additionalInsured : additionalInsureds) {
                if (additionalInsured == null) continue;

                InsuredDTO insuredDTO = mapper.map(additionalInsured, InsuredDTO.class);
                if (additionalInsured.getAppQuestionNumber() != null && additionalInsured.getMbalQuestionResponse() != null) {
                    if (additionalInsured.getAppQuestionNumber() == 3) {
                        insuredDTO.setMbalComboSmallQuestion(gson.fromJson(additionalInsured.getMbalQuestionResponse(), FlexibleSubmitQuestionRequest.ComboSmallQuestion.class));
                    } else {
                        insuredDTO.setMbalComboBigQuestion(gson.fromJson(additionalInsured.getMbalQuestionResponse(), FlexibleSubmitQuestionRequest.ComboBigQuestion.class));
                    }
                }

                AdditionalInsuredDTO additionalInsuredDTO = new AdditionalInsuredDTO();
                additionalInsuredDTO.setAssured(insuredDTO);

                AdditionalInsuredDTO.AdditionalProductDTOS additionalProduct = new AdditionalInsuredDTO.AdditionalProductDTOS();
                additionalProduct.setRiders(mapAdditionalProduct.get(additionalInsured.getId()));
//              additionalInsuredDTO.setAdditionalProduct(additionalProduct);
                additionalInsuredDTO.setAdditionalProducts(mapAdditionalProduct.get(additionalInsured.getId()));
                insuredDTOS.add(additionalInsuredDTO);
            }
            contractDto.setAdditionalAssuredOutputs(insuredDTOS);
        }

        // Người thụ hưởng
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByInsuranceRequestId(requestId);
        List<BeneficiaryDTO> beneficiaryDTOS = new ArrayList<>();
        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary != null) {
                BeneficiaryDTO beneficiaryDTO = mapper.map(beneficiary, BeneficiaryDTO.class);
                beneficiaryDTOS.add(beneficiaryDTO);
            }
        }
        contractDto.setBeneficiaryDTOS(beneficiaryDTOS);

        return contractDto;
    }

    @Override
    public void listContractRenewal() {

    }

    private void updateStatus(InsuranceContractDto contractDto) {
        try {
            contractDto.setCheckMicIssueAfterNow(DateUtil.checkStatus(DateUtil.DATE_DMY, contractDto.getMicIssueDate()));
        } catch (ParseException e) {
            contractDto.setCheckMicIssueAfterNow(false);
            log.error("[MINI]--Exception {}", e.getMessage());
        }
    }

    @Override
    public void exportList(Long idUser, String query, String type, String segment, String packageName, String category, HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportList InsuranceContract with idUser=" + idUser + ", query=" + query + ", type=" + type);

        // search and filter by package name
        Set<Integer> listIdPackage = new HashSet<>();
        String hasUrStyle = filterPackage(packageName, listIdPackage);
        LOGGER.info("filterIdPackage=" + listIdPackage + ", filterUrStyle=" + hasUrStyle);

        List<InsuranceContractVo> insuranceContracts = insuranceContractRepository.listInsuranceContractNoPaging(query.trim(), segment, listIdPackage, hasUrStyle, category);
        Map<Long, PrimaryProduct> mapPrimaryProduct = getMapPrimaryProduct(insuranceContracts);
        List<InsuranceContractListDto> contractListDtos = buildResponseContractList(insuranceContracts, mapPrimaryProduct);
        List<InsuranceContractExportListDTO> csvDTOList = mapListToCSV(contractListDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)){
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-hop-dong-bao-hiem-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, InsuranceContractExportListDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-hop-dong-bao-hiem-excel-", response);
            List<String> headers = Arrays.asList("Họ và tên", "AW", "Mã FT", "GCNBH", "HĐBH", "Gói bảo hiểm", "Thời gian đóng phí", "Phí bảo hiểm", "Phân khúc");
            Field[] fields = InsuranceContractExportListDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_insurance_contract.xlsx", response);
        }
    }

    public List<InsuranceContractExportListDTO> mapListToCSV(List<InsuranceContractListDto> listDtos) {
        List<InsuranceContractExportListDTO> csvDTOList = new ArrayList<>();
        for (InsuranceContractListDto eachDto : listDtos) {
            InsuranceContractExportListDTO exportDTO = new InsuranceContractExportListDTO();
            exportDTO.fullName = eachDto.getFullName();
            exportDTO.mbTransactionId = eachDto.getMbTransactionId();
            exportDTO.mbFt = eachDto.getMbFt();
            exportDTO.micContractNum = String.format("Mã FT: %s, Số GCNBH: %s", eachDto.getMicFtNumber(), eachDto.getMicContractNum());
            exportDTO.mbalPolicyNumber = String.format("Mã FT: %s, Số HĐBH: %s", eachDto.getMbalFtNumber(), eachDto.getMbalAppNo());
            exportDTO.packageName = eachDto.getPackageName();
            exportDTO.mbalFeePaymentTime = eachDto.getMbalFeePaymentTime();
            exportDTO.strInsuranceFee = eachDto.getStrInsuranceFee();
            exportDTO.segment = eachDto.getSegment();
            csvDTOList.add(exportDTO);
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

    @Override
    public PaginationResponse<InsuranceContractThirdPartyListDto> listThirdParty(Long user, int page, int size, String query, String source){
        isSuperAdminAndAdmin(user);
        LOGGER.info("Starting get list InsuranceContract with idUser=" + user + ", query=" + query + ", source=" + source);
        PaginationResponse<InsuranceContractThirdPartyListDto> response = new PaginationResponse<>();
        List<String> sources = getSources(source);

        List<InsuranceContractThirdPartyVo> insuranceContracts = insuranceContractRepository.listInsuranceContractThirdParty(page, size, query.trim(), sources);
        List<InsuranceContractThirdPartyListDto> contractListDtos = buildResponseContractThirdPartyList(insuranceContracts);
        long total = insuranceContractRepository.totalInsuranceContractThirdParty(query.trim(), sources);
        response.setData(contractListDtos);
        response.setTotalItem(total);
        response.setPageSize(size);
        return response;
    }

    private static List<String> getSources(String source) {
        List<String> sources = new ArrayList<>();
        if (!source.equals("")) {
            sources.add(source);
        } else {
            sources.add("MBAL");
            sources.add("ONEID");
        }
        return sources;
    }

    public List<InsuranceContractThirdPartyListDto> buildResponseContractThirdPartyList(List<InsuranceContractThirdPartyVo> insuranceContractVos) {
        List<InsuranceContractThirdPartyListDto> contractListDtos = new ArrayList<>();
        for (InsuranceContractThirdPartyVo insuranceContractThirdPartyVo : insuranceContractVos) {
            InsuranceContractThirdPartyListDto contractListDto = buildInsuranceContractThirdPartyDto(insuranceContractThirdPartyVo);
            contractListDtos.add(contractListDto);
        }
        return contractListDtos;
    }

    public InsuranceContractThirdPartyListDto buildInsuranceContractThirdPartyDto(InsuranceContractThirdPartyVo contractThirdPartyVo) {
        InsuranceContractThirdPartyListDto dto = new InsuranceContractThirdPartyListDto();
        dto.setId(contractThirdPartyVo.getId());
        dto.setMbId(contractThirdPartyVo.getMbId());
        dto.setFullName(contractThirdPartyVo.getFullName());
        dto.setMbalAppNo(contractThirdPartyVo.getMbalAppNo());
        dto.setMbalPolicyNumber(contractThirdPartyVo.getMbalPolicyNumber());
        dto.setMicContractNum(contractThirdPartyVo.getMicContractNum());
        dto.setProductName(contractThirdPartyVo.getProductName());
        dto.setStrInsuranceFee(contractThirdPartyVo.getStrInsuranceFee());
        dto.setMbalFeePaymentTime(contractThirdPartyVo.getMbalFeePaymentTime());
        dto.setStatus(contractThirdPartyVo.getStatus());
        dto.setSource(contractThirdPartyVo.getSource());

        return dto;
    }

    @Override
    public void exportListThirdParty(Long idUser, String query, String source, String type, HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportList InsuranceContract with idUser=" + idUser + ", query=" + query + ", source=" + source);
        isSuperAdminAndAdmin(idUser);

        List<String> sources = getSources(source);

        List<InsuranceContractThirdPartyVo> insuranceContracts = insuranceContractRepository.listInsuranceContractThirdPartyNoPaging(query.trim(), sources);
        List<InsuranceContractThirdPartyListDto> contractListDtos = buildResponseContractThirdPartyList(insuranceContracts);

        List<InsuranceContractThirdPartyExportListDTO> csvDTOList = mapListThirdPartyToCSV(contractListDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)){
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-hop-dong-bao-hiem-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, InsuranceContractThirdPartyExportListDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-hop-dong-bao-hiem-excel-", response);
            List<String> headers = Arrays.asList("Họ và tên", "Số HSYCBH", "Số HĐBH", "GCNBH", "Tên sản phẩm", "Phí bảo hiểm", "Thời gian đóng phí", "Trạng thái", "Nguồn truy vấn");
            Field[] fields = InsuranceContractThirdPartyExportListDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_insurance_contract_third_party.xlsx", response);
        }
    }

    public List<InsuranceContractThirdPartyExportListDTO> mapListThirdPartyToCSV(List<InsuranceContractThirdPartyListDto> listDtos) {
        List<InsuranceContractThirdPartyExportListDTO> csvDTOList = new ArrayList<>();
        for (InsuranceContractThirdPartyListDto eachDto : listDtos) {
            InsuranceContractThirdPartyExportListDTO exportDTO = new InsuranceContractThirdPartyExportListDTO();
            exportDTO.fullName = eachDto.getFullName();
            exportDTO.mbalAppNo = eachDto.getMbalAppNo();
            exportDTO.mbalPolicyNumber = eachDto.getMbalPolicyNumber();
            exportDTO.micContractNum = eachDto.getMicContractNum();
            exportDTO.productName = eachDto.getProductName();
            exportDTO.strInsuranceFee = eachDto.getStrInsuranceFee();
            exportDTO.mbalFeePaymentTime = eachDto.getMbalFeePaymentTime();
            exportDTO.status = eachDto.getStatus();
            exportDTO.source = eachDto.getSource();
            csvDTOList.add(exportDTO);
        }
        return csvDTOList;
    }

    @Override
    public InsuranceContractDto contractDetail(Long id, Long contractId) {
        InsuranceContract contract = insuranceContractRepository.findById(contractId).orElseThrow( () -> new InsuranceContractNotFoundException(String.format(MSG38, contractId)));

        InsuranceContractDto contractDto = mapper.map(contract, InsuranceContractDto.class);
        updateStatus(contractDto);

        // get address dto
        contractDto.getCustomer().setAddress(contract.getCustomer().fullAddress());

        if ((contract.getPackageType().equals(Constants.PackageType.FIX_COMBO) || contract.getPackageType().equals(Constants.PackageType.FLEXIBLE))
                && contract.getInsurancePackage() != null) {
            contractDto.setPackageNames(contract.getInsurancePackage().getPackageName());
        } else {
            contractDto.setPackageNames(UR_STYLE.getVal());
        }

        Set<String> transactionId = new HashSet<>();
        transactionId.add(contract.getTransactionId());
        List<TransactionContractDto> insuranceContractVos = insuranceContractRepository.listTransactionContract(transactionId);
        if (!insuranceContractVos.isEmpty()) {
            TransactionContractDto transactionContractDto = insuranceContractVos.get(0);
            contractDto.setMbTransactionId(transactionContractDto.getTransactionId());
            contractDto.setMbFt(transactionContractDto.getMbReferenceNumber());
            contractDto.setMicFtNumber(transactionContractDto.getMicFtNumber());
            contractDto.setMbalFtNumber(transactionContractDto.getMbalFtNumber());
        }

        Long customerId = contract.getCustomer().getId();
        Long requestId = insuranceContractRepository.findInsuranceRequestIdByContractId(contract.getId());
        buildDetailContract(contractDto, customerId, requestId);

        InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(contract.getTransactionId());
        contractDto.setInsurancePayment(insurancePayment);

        return contractDto;
    }

    private void isSuperAdminAndAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }

    private User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }
}
