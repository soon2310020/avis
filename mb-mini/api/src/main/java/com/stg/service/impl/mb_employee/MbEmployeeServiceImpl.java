package com.stg.service.impl.mb_employee;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.MbEmployee;
import com.stg.entity.MbEmployeeImportHis;
import com.stg.entity.user.User;
import com.stg.errors.EmployeeImportException;
import com.stg.errors.UserNotFoundException;
import com.stg.errors.ValidationException;
import com.stg.repository.CustomerRepository;
import com.stg.repository.MbEmployeeImportHisRepository;
import com.stg.repository.MbEmployeeRepository;
import com.stg.repository.UserRepository;
import com.stg.service.MbEmployeeService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.mb_employee.*;
import com.stg.service.lock.ImportLockService;
import com.stg.service3rd.hcm.HcmApi3rdService;
import com.stg.service3rd.hcm.dto.resp.HcmEmployeeInfo;
import com.stg.service3rd.hcm.dto.resp.HcmEmployeeRes;
import com.stg.utils.CustomHashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.stg.config.AsyncConfiguration.TAKE_TIME_HIGH_CPU;

@Slf4j
@RequiredArgsConstructor
@Service
public class MbEmployeeServiceImpl implements MbEmployeeService {
    public static final int HEADER_ROW_INDEX = 0;
    public static final int SHEET_INDEX = 0;
    public static final int MAX_COLUMN = 8;

    public static final String EMPLOYEE_NAME_HEADER_NAME = "Tên NV";
    public static final String MANAGER_UNIT_HEADER_NAME = "Đơn vị quản lý";
    public static final String CIF_HEADER_NAME = "cif";
    public static final String IDENTITY_CARD_NUMBER_HEADER_NAME = "cmnd";

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;
    @Value("${api3rd.hcm.mb-unit-code}")
    private String mbUnitCode;

    private final ModelMapper modelMapper;
    private final MbEmployeeRepository mbEmployeeRepository;
    private final MbEmployeeImportHisRepository mbEmployeeImportHisRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final EntityManager entityManager;
    private final ImportLockService importLockService;
    private final HcmApi3rdService hcmApi3rdService;

    @Lazy
    @Autowired
    private MbEmployeeService mbEmployeeService;


    @Override
    public PaginationResponse<? extends IMbEmployeeDataDTO> listEmployees(int size, int page, String query, MbEmployeeSearchType searchType, MbManagerUnit mbManagerUnit) {

        if (MbEmployeeSearchType.HCM_SYSTEM.equals(searchType)) {
            if (StringUtils.isEmpty(query)) throw new ValidationException("Thông tin tìm kiếm không được để trống");

            HcmEmployeeRes hcmEmployeeRes = hcmApi3rdService.findEmployeeFromHcm(query);

            PaginationResponse<HcmEmployeeInfo> response = new PaginationResponse<>();
            response.setPageSize(size);
            response.setPage(page);

            if (hcmEmployeeRes.hasData()) {
                response.setTotalItem(1);
                response.setData(hcmEmployeeRes.getData().getEmployeeList());
                return response;
            }

            response.setTotalItem(0);
            response.setData(List.of());
            return response;
        }
        String managerUnit = mbManagerUnit !=null ? mbManagerUnit.name() : "";

        List<MbEmployee> mbEmployees = mbEmployeeRepository.findAllByQuery(page, size, query, managerUnit);

        //change to DTO object
        List<MbEmployeeDTO> employeeDTOS = mbEmployees.stream()
                .map(mbEmployee -> modelMapper.map(mbEmployee, MbEmployeeDTO.class))
                .collect(Collectors.toList());
        Long totalItem = mbEmployeeRepository.totalFindAllByQuery(query,managerUnit );

        PaginationResponse<MbEmployeeDTO> response = new PaginationResponse<>();
        response.setData(employeeDTOS);
        response.setPageSize(size);
        response.setPage(page);
        response.setTotalItem(totalItem);
        return response;
    }

    @Override
    public MbEmployeeDTO employeeDetail(String mbId) {
        Optional<MbEmployee> mbEmployee = mbEmployeeRepository.findFirstByMbId(mbId);
        return mbEmployee.map(employee -> modelMapper.map(employee, MbEmployeeDTO.class)).orElse(null);
    }

    @Override
    public PaginationResponse<MbEmployeeHIsDTO> listEmployeeHis(int size, int page) {
        PaginationResponse<MbEmployeeHIsDTO> response = new PaginationResponse<>();

        //convert time module
        List<MbEmployeeImportHis> mbEmployeeImportHis = mbEmployeeImportHisRepository.findAllByQuery(page, size);
        List<MbEmployeeHIsDTO> mbEmployeeHIsDTOList = mbEmployeeImportHis.stream()
                .map(h -> modelMapper.map(h, MbEmployeeHIsDTO.class)).collect(Collectors.toList());

        response.setData(mbEmployeeHIsDTOList);
        response.setPageSize(size);
        response.setPage(page);
        response.setTotalItem(mbEmployeeImportHisRepository.count());

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveImportHistory(MbEmployeeImportHis mbEmployeeImportHis) {
        mbEmployeeImportHisRepository.save(mbEmployeeImportHis);
    }

    @Override
    @Transactional
    public MbEmployeeDTO editMbEmployee(MbEmployeeReq req, String id) {
        Optional<MbEmployee> mbEmployeeOptional = mbEmployeeRepository.findFirstByMbId(id);
        if (mbEmployeeOptional.isEmpty()) throw new ValidationException("Employee not found");

        MbEmployee employee = mbEmployeeOptional.get();

        // Kiểm tra và set giá trị cho từng trường một
        if (req.getEmployeeName() != null) {
            employee.setEmployeeName(req.getEmployeeName());
        }

        if (req.getEmail() != null) {
            employee.setEmail(req.getEmail());
        }

        if (req.getEmployeeCode() != null) {
            employee.setEmployeeCode(req.getEmployeeCode());
        }

        boolean hasUpdateManagingUnit = req.getManagingUnit() != null;
        if (hasUpdateManagingUnit) {
            employee.setManagingUnit(req.getManagingUnit());
        }

        if (req.getStatus() != null) {
            employee.setStatus(req.getStatus());
        }
        employee.setUpdatedAt(LocalDateTime.now());

        // Lưu vào cơ sở dữ liệu
        MbEmployee mbEmployeeSaved = mbEmployeeRepository.save(employee);

        // update for customer
        if (hasUpdateManagingUnit) {
            customerRepository.updateManagingUnitByIdentity(req.getManagingUnit(), employee.getIdentityCardNumber());
        }

        return modelMapper.map(mbEmployeeSaved, MbEmployeeDTO.class);
    }

    @Override
    public void importMbEmployeeData(MultipartFile multipartFile, CustomUserDetails userDetails) {
        User user = userRepository.findUserById(Long.valueOf(userDetails.getUserId())).orElseThrow(() -> new UserNotFoundException("There is no user with id=" + userDetails.getUserId()));

        importLockService.lockImportDataMbEmployee(userDetails.getUserId(), () -> {
            log.info("ImportDataMbEmployee :: [START] user={}, fileName={}, withBatchSize={}", user.getEmail(), multipartFile.getOriginalFilename(), batchSize);
            LocalDateTime startTime = LocalDateTime.now();

            try {
                /* Section 1: read and save employee */
                MbImportDataResult result = readAndSaveExcelData(multipartFile);

                /* Section 2: Saving history */
                int totalSuccess = 0;
                List<String> detailErrors = new ArrayList<>(result.getFutures().size());
                for (Future<MbImportThreadResult> f : result.getFutures()) {
                    try { //NOSONAR
                        MbImportThreadResult subResult = f.get();
                        if (subResult.isSuccess()) {
                            totalSuccess += subResult.getTotal();
                        } else {
                            detailErrors.add(subResult.getErrorDetail());
                        }
                    } catch (InterruptedException e) { //NOSONAR
                        log.warn("Thread interrupted", e);
                        detailErrors.add(e.getMessage());
                    } catch (ExecutionException e) {
                        log.warn("ExecutionException", e);
                        detailErrors.add(e.getMessage());
                    }
                }

                /*HISTORY*/
                MbEmployeeImportHis mbEmployeeImportHis = new MbEmployeeImportHis();
                mbEmployeeImportHis.setFileName(multipartFile.getOriginalFilename());
                mbEmployeeImportHis.setErrorContent(result.toStringErrorLines());
                mbEmployeeImportHis.setTotalSuccess(totalSuccess);
                mbEmployeeImportHis.setTotalFailed(result.getTotalRecord() - totalSuccess);
                mbEmployeeImportHis.setCreatedBy(user);

                // error
                if (!detailErrors.isEmpty()) {
                    mbEmployeeImportHis.setStatus(EmployeeHisResult.FAILURE);
                    String errorDetail = StringUtils.join(detailErrors, ", ");
                    mbEmployeeImportHis.setErrorSysDetail(errorDetail);
                    /*mbEmployeeService.saveImportHistory(mbEmployeeImportHis);
                    throw new EmployeeImportException(errorDetail);*/ // vẫn là success
                }

                // success
                mbEmployeeImportHis.setStatus(EmployeeHisResult.SUCCESS);
                mbEmployeeService.saveImportHistory(mbEmployeeImportHis);

                log.info("ImportDataMbEmployee :: [INFO] user={}, fileName={}, totalSuccess={}, totalFailed={}", user.getEmail(), multipartFile.getOriginalFilename(), totalSuccess, mbEmployeeImportHis.getTotalFailed());
            } catch (Exception e) {
                MbEmployeeImportHis mbEmployeeImportHis = new MbEmployeeImportHis();
                mbEmployeeImportHis.setFileName(multipartFile.getOriginalFilename());
                mbEmployeeImportHis.setStatus(EmployeeHisResult.FAILURE);
                mbEmployeeImportHis.setErrorSysDetail(e.getMessage());
                mbEmployeeImportHis.setCreatedBy(user);
                mbEmployeeService.saveImportHistory(mbEmployeeImportHis);
                throw new EmployeeImportException(e.getMessage(), e);
            } finally {
                long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();
                log.info("ImportDataMbEmployee :: [END] user={}, fileName={}, duration={}", user.getEmail(), multipartFile.getOriginalFilename(), duration);
            }
        });
    }


    /**
     * Import MB employee
     */
    private MbImportDataResult readAndSaveExcelData(MultipartFile multipartFile) throws IOException {
        try (InputStream inputStream = multipartFile.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(SHEET_INDEX); // get data from sheet index
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // header has 1 row

            //get column index by header name
            int employeeNameIndex = getRowIndexByHeaderName(sheet, EMPLOYEE_NAME_HEADER_NAME);
            int managerUnitIndex = getRowIndexByHeaderName(sheet, MANAGER_UNIT_HEADER_NAME);
            int cifIndex = getRowIndexByHeaderName(sheet, CIF_HEADER_NAME);
            int identityCardNumberIndex = getRowIndexByHeaderName(sheet, IDENTITY_CARD_NUMBER_HEADER_NAME);
            DataFormatter dataFormatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            List<Future<MbImportThreadResult>> futures = new ArrayList<>();
            CustomHashSet<MbEmployee> employeesToSave = new CustomHashSet<>();
            List<String> errorLines = new ArrayList<>();

            int totalCount = 0;
            int batchCount = 0;
            int mbUnitCount = 0;
            String cellValStr;
            while (rows.hasNext()) {
                Row row = rows.next();
                ++totalCount;
                if (isRowEmpty(row)) {
                    errorLines.add((row.getRowNum() + 1) + ":Dữ liệu trống");
                    continue;
                }

                MbEmployee employee = new MbEmployee();

                // IdentityCardNumber
                cellValStr = dataFormatter.formatCellValue(row.getCell(identityCardNumberIndex), evaluator);
                if (isCellDataEmpty(cellValStr)) {
                    errorLines.add((row.getRowNum() + 1) + ":GTTT null hoặc không hợp lệ");
                    continue;
                } else {
                    employee.setIdentityCardNumber(cellValStr.trim());
                }

                // ManagingUnit
                cellValStr = dataFormatter.formatCellValue(row.getCell(managerUnitIndex), evaluator);
                if (isCellDataEmpty(cellValStr)) {
                    errorLines.add((row.getRowNum() + 1) + ":Đơn vị quản lý null hoặc không hợp lệ");
                    continue;
                } else {
                    cellValStr = cellValStr.trim();
                    if (mbUnitCode.equals(cellValStr)) {
                        ++mbUnitCount;
                        continue; // không import đơn vị MB
                    }
                    employee.setManagingUnit(cellValStr);
                }

                // verify: identityCardNumber, mbId, employeeCode
                MbEmployee old = employeesToSave.get(employee);
                if (old != null && !old.isUnique(employee)) {
                    errorLines.add((row.getRowNum() + 1) + ":Trùng GTTT nhưng thông tin mbId, employeeCode sai khác");
                    continue;
                }

                // MbId
                cellValStr = dataFormatter.formatCellValue(row.getCell(cifIndex), evaluator);
                if (isCellDataEmpty(cellValStr)) {
                    //log.warn("ImportDataMbEmployee - MbId is empty, row={}", row.getRowNum() + 1);
                } else {
                    employee.setMbId(cellValStr.trim());
                }

                // EmployeeName
                cellValStr = dataFormatter.formatCellValue(row.getCell(employeeNameIndex), evaluator);
                if (isCellDataEmpty(cellValStr)) {
                    //log.warn("ImportDataMbEmployee - EmployeeName is empty, row={}", row.getRowNum() + 1);
                } else {
                    employee.setEmployeeName(cellValStr.trim());
                }

                employee.setStatus(true);
                employeesToSave.add(employee);

                if (employeesToSave.size() == batchSize) {
                    futures.add(mbEmployeeService.saveOrUpdateEmployees(employeesToSave));
                    employeesToSave = new CustomHashSet<>(batchSize);
                    log.info("ImportDataMbEmployee :: [SAVING] fileName={}, batchNumb={}, withBatchSize={}", multipartFile.getOriginalFilename(), ++batchCount, batchSize);
                }
            }
            // last save
            if (!employeesToSave.isEmpty()) {
                futures.add(mbEmployeeService.saveOrUpdateEmployees(employeesToSave));
                log.info("ImportDataMbEmployee :: [SAVING] fileName={}, batchNumb={}, withBatchSize={}", multipartFile.getOriginalFilename(), ++batchCount, batchSize);
            }

            if (mbUnitCount > 0) {
                log.warn("ImportDataMbEmployee :: [WARNING] Skip import MB Unit, total={}", mbUnitCount);
            }

            return new MbImportDataResult(totalCount, errorLines, futures);
        }
    }

    @Async(TAKE_TIME_HIGH_CPU)
    @Transactional
    public CompletableFuture<MbImportThreadResult> saveOrUpdateEmployees(Collection<MbEmployee> employees) {
        try {
            StringBuilder upsertEmpBuilder = new StringBuilder(INSERT_MB_EMPLOYEE);
            boolean isFirst = true;
            for (int i = 0; i < employees.size(); i++) {
                if (isFirst) {
                    isFirst = false;
                    upsertEmpBuilder.append("(?, ?, ?, ?, ?, ?, ?)");
                } else {
                    upsertEmpBuilder.append(", (?, ?, ?, ?, ?, ?, ?)");
                }
            }
            upsertEmpBuilder.append("\n").append(UPDATE_MB_EMPLOYEE);

            Query upsertEmpQuery = entityManager.createNativeQuery(upsertEmpBuilder.toString());

            int idx = 0;
            for (MbEmployee e : employees) {
                upsertEmpQuery.setParameter(++idx, e.getIdentityCardNumber());
                upsertEmpQuery.setParameter(++idx, e.getEmail());
                upsertEmpQuery.setParameter(++idx, e.getEmployeeCode());
                upsertEmpQuery.setParameter(++idx, e.getEmployeeName());
                upsertEmpQuery.setParameter(++idx, e.getManagingUnit());
                upsertEmpQuery.setParameter(++idx, e.getMbId());
                upsertEmpQuery.setParameter(++idx, e.getStatus());
            }
            upsertEmpQuery.executeUpdate(); // EXECUTE UpsertEmployee
            mbEmployeeService.updateManagingUnitForCustomers(employees);

            entityManager.flush();
            entityManager.clear();
            return CompletableFuture.completedFuture(MbImportThreadResult.completed(employees.size()));
        } catch (Exception e) {
            log.error("SaveOrUpdateEmployees error!", e);
            return CompletableFuture.completedFuture(MbImportThreadResult.failed(employees.size(), e.getMessage()));
        }
    }

    @Transactional
    public void updateManagingUnitForCustomers(Collection<MbEmployee> employees) {
        Map<String, List<String>> managingUnitIdentifyMap = new HashMap<>();

        for (MbEmployee e : employees) {
            managingUnitIdentifyMap.computeIfAbsent(e.getManagingUnit(), k -> new ArrayList<>())
                    .add(e.getIdentityCardNumber());
        }

        for (Map.Entry<String, List<String>> entry : managingUnitIdentifyMap.entrySet()) {
            List<String> params = entry.getValue();
            if (!params.isEmpty()) {
                String sqlBuilder = "update customer set managing_unit = ? where identification in (" +
                        params.stream().map(e -> "?").collect(Collectors.joining(",")) +
                        ")";
                Query updateCustomerQuery = entityManager.createNativeQuery(sqlBuilder); //NOSONAR non injection!!!

                int idx = 0;
                updateCustomerQuery.setParameter(++idx, entry.getKey()); // for managingUnit
                for (String identity : params) {
                    updateCustomerQuery.setParameter(++idx, identity); // for identity
                }
                updateCustomerQuery.executeUpdate(); // EXECUTE UpdateCustomer
            }
        }
    }

    private static final String INSERT_MB_EMPLOYEE = "INSERT INTO mb_employee (\n" +
            "\tidentity_card_number,\n" +
            "\temail,\n" +
            "\temployee_code,\n" +
            "\temployee_name,\n" +
            "\tmanaging_unit,\n" +
            "\tmb_id,\n" +
            "\tstatus\n" +
            ")\n" +
            "VALUES ";
    private static final String UPDATE_MB_EMPLOYEE =
            "ON CONFLICT (identity_card_number) \n" +
                    "DO \n" +
                    "   UPDATE set\n" +
                    "   email = EXCLUDED.email,\n" +
                    "   employee_code = EXCLUDED.employee_code,\n" +
                    "   employee_name = EXCLUDED.employee_name,\n" +
                    "   managing_unit = EXCLUDED.managing_unit,\n" +
                    "   mb_id = EXCLUDED.mb_id,\n" +
                    "   status = EXCLUDED.status,\n" +
                    "   updated_at = CURRENT_TIMESTAMP; ";

    public int getRowIndexByHeaderName(Sheet sheet, String headerName) {
        Row headerRow = sheet.getRow(HEADER_ROW_INDEX);

        for (Cell cell : headerRow) {
            if (headerName.equalsIgnoreCase(cell.getStringCellValue())) {
                return cell.getColumnIndex(); // return  suitable cell index
            }
        }
        throw new EmployeeImportException("Invalid header name in this template :" + headerName);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i <= MAX_COLUMN; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                return false; // Nếu có ít nhất một ô có dữ liệu, trả về false
            }
        }
        return true; // Hàng chỉ chứa ô trống
    }

    private boolean isCellDataEmpty(String cellVal) {
        return cellVal == null || cellVal.isBlank() || "#N/A".equalsIgnoreCase(cellVal) || "NULL".equalsIgnoreCase(cellVal);
    }
}
