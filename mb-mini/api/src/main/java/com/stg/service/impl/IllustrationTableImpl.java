package com.stg.service.impl;

import com.google.gson.Gson;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.*;
import com.stg.entity.customer.Customer;
import com.stg.errors.*;
import com.stg.repository.*;
import com.stg.service.IllustrationTableService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.external.PackageNameEnum;
import com.stg.service.dto.insurance.*;
import com.stg.utils.Constants;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.stg.entity.QuestionName.*;
import static com.stg.service.dto.external.PackageNameEnum.UR_STYLE;
import static com.stg.service.dto.external.PackageNameEnum.getPackageNumFromVal;
import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.MSG13;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class IllustrationTableImpl implements IllustrationTableService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IllustrationTableImpl.class);
    private static final String YES = "C";
    @Value("${spring.redis.cache.illustration-table-service.create-illustration-table-expire-time}")
    public int createIllustrationExpireTime;

    private final IllustrationTableRepository illustrationTableRepository;
    private final CustomerRepository customerRepository;
    private final InsurancePackageRepository insurancePackageRepository;
    private final MbalPackageRepository mbalPackageRepository;
    private final MicPackageRepository micPackageRepository;
    private final PavTablesRepository pavTablesRepository;

    private final InsuranceRequestRepository insuranceRequestRepository;

    private final PackagePhotoRepository packagePhotoRepository;

    private final InsuranceContractImpl insuranceContract;

    @Autowired
    private final ModelMapper mapper;

    @Autowired
    private final Gson gson;

    @Autowired
    private final RedisCommands<String, String> redis;

    @Override
    public PaginationResponse<IllustrationTableDto> list(Long user, int page, int size, String query, String packageName, String segment, String category) {
        PaginationResponse<IllustrationTableDto> response = new PaginationResponse<>();

        // search and filter by package name
        Set<Integer> searchIdPackage = new HashSet<>();
        String searchUrStyle = searchPackage(query, searchIdPackage);
        LOGGER.info("searchIdPackage=" + searchIdPackage + ", searchUrStyle=" + searchUrStyle);

        Set<Integer> filterIdPackage = new HashSet<>();
        String filterUrStyle = filterPackage(packageName, filterIdPackage);
        LOGGER.info("filterIdPackage=" + filterIdPackage + ", filterUrStyle=" + filterUrStyle);

        List<IllustrationTable> illustrationTables = illustrationTableRepository.listIllustrationTable(page, size, query.trim(), segment, searchIdPackage, searchUrStyle, filterIdPackage, filterUrStyle, category);
        long total = illustrationTableRepository.totalIllustrationTable(query.trim(), segment, searchIdPackage, searchUrStyle, filterIdPackage, filterUrStyle, category);
        List<IllustrationTableDto> illustrationTableDtos = mapIllustrationTableToDto(illustrationTables);
        response.setData(illustrationTableDtos);
        response.setTotalItem(total);
        response.setPageSize(size);
        return response;
    }

    private List<IllustrationTableDto> mapIllustrationTableToDto(List<IllustrationTable> illustrationTables) {
        List<IllustrationTableDto> illustrationTableDtos = new ArrayList<>();
        for (IllustrationTable illustrationTable : illustrationTables) {
            IllustrationTableDto illustrationTableDto = mapper.map(illustrationTable, IllustrationTableDto.class);
            Customer customer = illustrationTable.getCustomer();
            if (customer != null) {
                if (customer.getSegment() != null) {
                    illustrationTableDto.setSegment(customer.getSegment().getGain());
                }
                if (customer.getBirthday() != null) {
                    illustrationTableDto.getCustomer().setBirthday(DateUtil.localDateTimeToString(DateUtil.DATE_DMY_DASH, customer.getBirthday()));
                }
            }
            illustrationTableDto.setInsurancePackage(buildNameInsurancePackage(illustrationTable.getInsurancePackage()));
            illustrationTableDtos.add(illustrationTableDto);
        }
        return illustrationTableDtos;
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

    public String searchPackage(String query, Set<Integer> listIdPackage) {
        String hasUrStyle = "";
        if (query != null && !"".equals(query)) {
            List<InsurancePackage> insurancePackages = insurancePackageRepository.listNoPaging(query, "", "");
            if (!insurancePackages.isEmpty()) {
                Set<Integer> ids = insurancePackages.stream().map(insurancePackage -> insurancePackage.getId()).collect(Collectors.toSet());
                listIdPackage.addAll(ids);
                // search gói phong cách
                hasUrStyle = "No";
                List<InsurancePackage> packageFreeStyle = insurancePackages.stream().filter(insurancePackage -> insurancePackage.getType().equals(Constants.PackageType.FREE_STYLE.name())).collect(Collectors.toList());
                if (!packageFreeStyle.isEmpty()) {
                    hasUrStyle = "Yes";
                }
            }
        }

        return hasUrStyle;
    }

    @Override
    public IllustrationTableDto illustrationTableDetail(Long userId, String illustrationId, String packageName) {
        Optional<IllustrationTable> illustrationTable = illustrationTableRepository.findById(illustrationId);
        if (!illustrationTable.isPresent()) {
            throw new IllustrationTableNotFoundException("Không tồn tại bảng minh họa.");
        }
        IllustrationTableDto illustrationTableDto = mapper.map(illustrationTable, IllustrationTableDto.class);
        InsurancePackageDto insurancePackage = new InsurancePackageDto();
        PackageNameEnum packageNameEnum = UR_STYLE;
        if (illustrationTableDto.getInsurancePackage() != null) {
            insurancePackage = illustrationTableDto.getInsurancePackage();
            packageNameEnum = getPackageNumFromVal(insurancePackage.getPackageName());
        } else {
            insurancePackage.setPackageName(UR_STYLE.getVal());
            insurancePackage.setIssuer("MBAL + MIC");
        }
        PackagePhoto byType = packagePhotoRepository.findByType(packageNameEnum);
        insurancePackage.setPackagePhoto(byType.getImage());
        illustrationTableDto.setInsurancePackage(insurancePackage);
        illustrationTableDto.setMixInsuranceFee(illustrationTableDto.getMixInsuranceFee() == null ? illustrationTableDto.getInsuranceFee() : illustrationTableDto.getMixInsuranceFee());

        List<PavTableDto> pavTables = new ArrayList<>();
        List<PavTable> pavTablesDb = illustrationTable.get().getPavTables();
        if (pavTablesDb != null) {
            for (PavTable pavTable : pavTablesDb) {
                PavTableDto pavTableDto = mapper.map(pavTable, PavTableDto.class);
                pavTableDto.setContractYear(pavTable.getId().getContractYear());
                pavTableDto.setIllustrationNumber(pavTable.getId().getIllustrationTable().getIllustrationNumber());
                pavTables.add(pavTableDto);
            }
            illustrationTableDto.setPavTables(pavTables);
        }

        Long customerId = illustrationTableDto.getCustomer().getId();
        Long requestId = illustrationTableRepository.findInsuranceRequestIdByIllustrationTable(illustrationTableDto.getIllustrationNumber());
        InsuranceContractDto insuranceContractDto = insuranceContract.buildDetailContract(new InsuranceContractDto(), customerId, requestId);
        illustrationTableDto.setCustomerAdditionalProductDTOS(insuranceContractDto.getCustomerAdditionalProductDTOS());
        illustrationTableDto.setPrimaryInsuredDTO(insuranceContractDto.getPrimaryInsuredDTO());
        illustrationTableDto.setPrimaryProductDTO(insuranceContractDto.getPrimaryProductDTO());
        if (insuranceContractDto.getPrimaryInsuredDTO() != null) {
            illustrationTableDto.setPrimaryInsuredAdditionalProductDTOS(insuranceContractDto.getPrimaryInsuredDTO().getAdditionalProductDTOS());
        }
        illustrationTableDto.setAdditionalAssuredOutputs(insuranceContractDto.getAdditionalAssuredOutputs());
        illustrationTableDto.setBeneficiaryDTOS(insuranceContractDto.getBeneficiaryDTOS());

        return illustrationTableDto;
    }

    @Override
    public void exportList(Long idUser, String query, String type, String packageName, HttpServletResponse response, String segment, String category) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportList Illustration Table with idUser=" + idUser + ", query=" + query + ", type=" + type);

        // search and filter by package name
        Set<Integer> searchIdPackage = new HashSet<>();
        String searchUrStyle = searchPackage(query, searchIdPackage);
        LOGGER.info("searchIdPackage=" + searchIdPackage + ", searchUrStyle=" + searchUrStyle);

        Set<Integer> filterIdPackage = new HashSet<>();
        String filterUrStyle = filterPackage(packageName, filterIdPackage);
        LOGGER.info("filterIdPackage=" + filterIdPackage + ", filterUrStyle=" + filterUrStyle);

        List<IllustrationTable> illustrationTables = illustrationTableRepository.listIllustrationTableNoPaging(query.trim(), segment, searchIdPackage, searchUrStyle, filterIdPackage, filterUrStyle, category);
        List<IllustrationTableDto> illustrationTableDtos = mapIllustrationTableToDto(illustrationTables);
        List<IllustrationTableExportListDTO> csvDTOList = mapListToCSV(illustrationTableDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)) {
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-bang-minh-hoa-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, IllustrationTableExportListDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-bang-minh-hoa-excel-", response);
            List<String> headers = Arrays.asList("Số BMH/GCN", "Họ và tên", "Ngày sinh", "Tham gia gói bảo hiểm", "Thời hạn đóng phí", "Đơn vị phát hành", "Phân khúc");
            Field[] fields = IllustrationTableExportListDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_illustration_table.xlsx", response);
        }
    }

    public List<IllustrationTableExportListDTO> mapListToCSV(List<IllustrationTableDto> listDtos) {
        List<IllustrationTableExportListDTO> csvDTOList = new ArrayList<>();
        for (IllustrationTableDto eachDto : listDtos) {
            IllustrationTableExportListDTO exportDTO = new IllustrationTableExportListDTO();
            exportDTO.illustrationNumber = eachDto.getIllustrationNumber();
            exportDTO.fullName = eachDto.getCustomer().getFullName();
            exportDTO.birthday = eachDto.getCustomer().getBirthday();
            exportDTO.packageName = eachDto.getInsurancePackage().getPackageName();
            exportDTO.segment = eachDto.getSegment();
            exportDTO.issuer = eachDto.getInsurancePackage().getIssuer();
            exportDTO.mbalFeePaymentTime = eachDto.getInsurancePackage().getMbalFeePaymentTime();

            csvDTOList.add(exportDTO);
        }
        return csvDTOList;
    }

    @Transactional
    @Override
    public void createIllustrationTable(String mbId, CreateIllustrationTableDto requestDto, Integer processId) {
        verifyCustomerPermission(mbId, processId);
        IllustrationTable illustrationTable = new IllustrationTable();
        InsuranceRequest insuranceRequest = new InsuranceRequest();
        long insuranceRequestId = redis.get(insuranceRequestIdAndProcessIdKey(String.valueOf(processId)))
                == null ? 0L : Long.parseLong(redis.get(insuranceRequestIdAndProcessIdKey(String.valueOf(processId))));
        Customer customer = customerRepository.findByMbId(mbId);
        if (customer == null) {
            throw new CustomerNotFoundException("Không tồn tại Khách hàng có ID = " + mbId);
        }
        if (insuranceRequestId > 0) {
            insuranceRequest = insuranceRequestRepository.getOne(insuranceRequestId);
            log.info("[MINI]--Update response question combo for insuranceRequestId {}", insuranceRequestId);
            insuranceRequest.setQuestionResponse(mapQuestionResponse(requestDto, customer.getGender(), insuranceRequest));
            insuranceRequestRepository.save(insuranceRequest);
            return;
        }

        illustrationTable.setCustomer(customer);

        InsurancePackage insurancePackage = insurancePackageRepository.findById(requestDto.getInsurancePackageId())
                .orElseThrow(() -> new ApplicationException("Không tồn tại Gói bảo hiểm có ID = " + requestDto.getInsurancePackageId()));
        illustrationTable.setInsurancePackage(insurancePackage);
        insuranceRequest.setInsurancePackage(insurancePackage);
        if (requestDto.getType().equals(Constants.PackageType.FIX_COMBO.name())) {
            insuranceRequest.setPackageType(Constants.PackageType.FIX_COMBO);
        } else {
            if (requestDto.getMicPackageId() == null || requestDto.getMbalPackageId() == null) {
                throw new ValidationException("Bạn phải cung cấp ID gói MIC và MBAL");
            }
            MbalPackage mbalPackage = mbalPackageRepository.findById(requestDto.getMbalPackageId())
                    .orElseThrow(() -> new ApplicationException("Không tồn tại Gói bảo hiểm MBAL có ID = " + requestDto.getMbalPackageId()));
            illustrationTable.setMbalPackage(mbalPackage);

            MicPackage micPackage = micPackageRepository.findById(requestDto.getMicPackageId())
                    .orElseThrow(() -> new ApplicationException("Không tồn tại Gói bảo hiểm MIC có ID = " + requestDto.getMicPackageId()));
            illustrationTable.setMicPackage(micPackage);

            insuranceRequest.setMbalPackage(mbalPackage);
            insuranceRequest.setMicPackage(micPackage);
            insuranceRequest.setPackageType(Constants.PackageType.FREE_STYLE);
        }

        illustrationTable = buildIllustrationTable(illustrationTable, requestDto, insurancePackage.getMbalFeePaymentTime());
        illustrationTable = illustrationTableRepository.saveAndFlush(illustrationTable);
        log.info("Done save Illustration Table Id = {}", illustrationTable.getIllustrationNumber());

        // save YCBH
        insuranceRequest.setQuestionResponse(mapQuestionResponse(requestDto, customer.getGender(), insuranceRequest));
        insuranceRequest.setCustomer(customer);
        insuranceRequest.setIllustrationTable(illustrationTable);

        log.info("Saving insurance request which has rejected for customer ID {}, Illustration Table Id = {}", insuranceRequest.getCustomer().getMbId(), illustrationTable.getIllustrationNumber());
        insuranceRequest = insuranceRequestRepository.saveAndFlush(insuranceRequest);
        redis.set(insuranceRequestIdAndProcessIdKey(String.valueOf(processId)), String.valueOf(insuranceRequest.getId()), SetArgs.Builder.ex(createIllustrationExpireTime));

        List<PavTable> pavTables = new ArrayList<>();
        for (PavTableDto pavTableDto : requestDto.getPavTables()) {
            PavTable pavTable = mapper.map(pavTableDto, PavTable.class);
            PavTable.PavTableId pavId = new PavTable.PavTableId();
            pavId.setIllustrationTable(illustrationTable);
            pavId.setContractYear(pavTableDto.getContractYear());
            pavTable.setId(pavId);
            pavTables.add(pavTable);
        }

        pavTablesRepository.saveAll(pavTables);
        log.info("ILLUSTRATION_PREFIX for mbId {}", insuranceRequest.getId());
        redis.set(illustrationKey(mbId), String.valueOf(insuranceRequest.getId()), SetArgs.Builder.ex(createIllustrationExpireTime));
        log.info("Khách hàng với mbID {}. Tạo Bảng minh họa thành công với ID {}, Tạo YCBH thành công với ID={}", mbId, illustrationTable.getIllustrationNumber(), insuranceRequest.getId());
    }

    private Map<QuestionName, String> mapQuestionResponse(CreateIllustrationTableDto requestDto, String gender, InsuranceRequest insuranceRequest) {
        Map<QuestionName, String> response = new EnumMap<>(QuestionName.class);
        if (requestDto.getComboSmallQuestion() == null && requestDto.getComboBigQuestion() == null) {
            throw new ValidationException("Bạn phải truyền thông tin trả lời các câu hỏi sức khỏe");
        }

        insuranceRequest.setStatus(true);
        if (requestDto.getComboSmallQuestion() != null) {
            response.put(QUESTION_ONE, convertObjectToJson(requestDto.getComboSmallQuestion().getC1()));
            response.put(QUESTION_TWO, convertObjectToJson(requestDto.getComboSmallQuestion().getC2()));
            response.put(QUESTION_THREE, convertObjectToJson(requestDto.getComboSmallQuestion().getC3()));
            response.put(OTHER_QUESTION, convertObjectToJson(requestDto.getComboSmallQuestion().getOqs()));

            // set status
            if (YES.equals(requestDto.getComboSmallQuestion().getC1()) || YES.equals(requestDto.getComboSmallQuestion().getC2()) ||
                    YES.equals(requestDto.getComboSmallQuestion().getC3()) || YES.equals(requestDto.getComboSmallQuestion().getOqs())) {
                insuranceRequest.setStatus(false);
            }
            return response;
        }

        if (requestDto.getComboBigQuestion().getFiles() != null && requestDto.getComboBigQuestion().getFiles().size() > 15) {
            throw new MiniApiException("Tổng file đính kèm vượt quá 15 files");
        }
        response.put(COMBO_BIG_QUESTION, convertObjectToJson(requestDto.getComboBigQuestion()));
        // set status
        if (YES.equals(requestDto.getComboBigQuestion().getOqs())) {
            insuranceRequest.setStatus(false);
        }

        return response;
    }

    public String convertObjectToJson(Object obj) {
        return gson.toJson(Objects.requireNonNullElse(obj, ""));
    }

    public IllustrationTable buildIllustrationTable(IllustrationTable illustrationTable, CreateIllustrationTableDto illustrationTableDto, String feePaymentTime) {
        illustrationTable.setIllustrationNumber();
        illustrationTable.setPackageType(Constants.PackageType.valueOf(illustrationTableDto.getType()));
        illustrationTable.setPackageCode(illustrationTableDto.getPackageCode());
        illustrationTable.setDeathNoAccidentFrom(illustrationTableDto.getDeathNoAccidentFrom());
        illustrationTable.setStrDeathNoAccidentFrom(illustrationTableDto.getStrDeathNoAccidentFrom());
        illustrationTable.setDeathNoAccidentTo(illustrationTableDto.getDeathNoAccidentTo());
        illustrationTable.setStrDeathNoAccidentTo(illustrationTableDto.getStrDeathNoAccidentTo());
        illustrationTable.setDeathAccident(illustrationTableDto.getDeathAccident());
        illustrationTable.setStrDeathAccident(illustrationTableDto.getStrDeathAccident());
        illustrationTable.setDeathAccidentYesTraffic(illustrationTableDto.getDeathAccidentYesTraffic());
        illustrationTable.setDeathAccidentNoTraffic(illustrationTableDto.getDeathAccidentNoTraffic());
        illustrationTable.setSupInpatientHospitalFee(illustrationTableDto.getSupInpatientHospitalFee());
        illustrationTable.setInsuranceFee(illustrationTableDto.getInsuranceFee());
        illustrationTable.setBaseInsuranceFee(illustrationTableDto.getBaseInsuranceFee());
        illustrationTable.setTopupInsuranceFee(illustrationTableDto.getTopupInsuranceFee());
        illustrationTable.setPayFrequency("Hàng năm");
        illustrationTable.setTimeFrequency(feePaymentTime);
        illustrationTable.setAmount(illustrationTableDto.getAmount());
        illustrationTable.setMixInsuranceFee(illustrationTableDto.getMixInsuranceFee());

        if (Objects.nonNull(illustrationTableDto.getMicCareDkbs())) {
            illustrationTable.setBs1(illustrationTableDto.getMicCareDkbs().getBs1());
            illustrationTable.setBs2(illustrationTableDto.getMicCareDkbs().getBs2());
            illustrationTable.setBs3(illustrationTableDto.getMicCareDkbs().getBs3());
            illustrationTable.setBs4(illustrationTableDto.getMicCareDkbs().getBs4());
        }

        return illustrationTable;
    }

    private void verifyCustomerPermission(String cif, Integer processId) {
        if (!String.valueOf(processId).equals(redis.get(processIdKey(cif)))) {
            throw new MiniApiException(MSG13);
        }
    }
}
