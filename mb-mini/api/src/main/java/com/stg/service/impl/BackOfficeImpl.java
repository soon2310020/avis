package com.stg.service.impl;

import com.google.gson.Gson;
import com.stg.entity.ICImport;
import com.stg.entity.ImportICHistory;
import com.stg.entity.InsurancePayment;
import com.stg.entity.PackagePhoto;
import com.stg.entity.user.User;
import com.stg.errors.InsurancePaymentNotFoundException;
import com.stg.errors.MiniApiException;
import com.stg.errors.UserHasNoPermissionException;
import com.stg.errors.UserNotFoundException;
import com.stg.repository.*;
import com.stg.service.BaasService;
import com.stg.service.BackOfficeService;
import com.stg.service.ExternalAPIService;
import com.stg.service.ExternalV2ApiService;
import com.stg.service.caching.RmExcelDataCaching;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.baas.ManualRequest;
import com.stg.service.dto.baas.PaymentCallbackManualResp;
import com.stg.service.dto.crm.RmExcelInfo;
import com.stg.service.dto.crm.RmExcelInfoResp;
import com.stg.service.dto.external.RmInfoResp;
import com.stg.service.dto.external.request.MbCallBackTransactionReqDto;
import com.stg.service.dto.external.responseV2.IcInformationResp;
import com.stg.service.dto.insurance.*;
import com.stg.utils.CallbackType;
import com.stg.utils.Constants;
import com.stg.utils.FileUtil;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.stg.utils.Common.processIdKey;
import static com.stg.utils.CommonMessageError.MSG13;
import static com.stg.utils.CommonMessageError.MSG36;
import static com.stg.utils.Constants.PAID;
import static com.stg.utils.Constants.PENDING;
import static com.stg.utils.Constants.WAITING;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BackOfficeImpl implements BackOfficeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackOfficeImpl.class);
    private final PackagePhotoRepository packagePhotoRepository;
    private final UserRepository userRepository;
    private final ICImportRepository icImportRepository;
    private final ImportICHistoryRepository importICHistoryRepository;
    private final AsyncObjectImpl asyncObject;
    private final ExternalV2ApiService externalV2ApiService;
    private final RmExcelDataCaching rmExcelDataCaching;
    private final BaasService baasService;
    private final ExternalAPIService externalAPIService;
    private final InsurancePaymentRepository paymentRepository;

    @Autowired
    private final ModelMapper mapper;

    @Autowired
    private final Gson gson;

    @Autowired
    private final RedisCommands<String, String> redis;

    public List<PackagePhoto> createPackagePhoto(Long userId, List<PackagePhotoDto> photoDtos) {

        LOGGER.info("Starting createPackagePhoto with userId=" + userId);
        isSuperAdmin(userId);
        List<PackagePhoto> packagePhotos = new ArrayList<>();
        for (PackagePhotoDto packagePhotoDto : photoDtos) {
            PackagePhoto packagePhoto = mapper.map(packagePhotoDto, PackagePhoto.class);
            packagePhotos.add(packagePhoto);
        }

        return packagePhotoRepository.saveAll(packagePhotos);
    }

    private void isSuperAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }

    private User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }

    public ResponseEntity<Map<String, String>> importIC(MultipartFile parts) {
        String originalFilename = parts.getOriginalFilename();
        String contentType = FileUtil.getRealMimeType(parts);
        if (originalFilename == null) {
            return new ResponseEntity(Constants.MS_WRONG_EXTENSION_EXCEL, HttpStatus.NOT_ACCEPTABLE);
        }
        if (originalFilename.endsWith(Constants.FILE_EXTENSION) && Constants.FILE_TYPE.equals(contentType)) {
            Map<String, String> result = readXlsxAndImportIC(parts);
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity(Constants.MS_WRONG_EXTENSION_EXCEL, HttpStatus.NOT_ACCEPTABLE);
    }

    private Map<String, String> readXlsxAndImportIC(MultipartFile parts) {
        ImportICHistory importICHistory = new ImportICHistory();
        String fileName = "";
        int rowNum = 0;
        Map<String, String> result = new HashMap<>();
        boolean statusImport = true;
        List<Integer> errorRows = new ArrayList<>();
        try {
            InputStream inputStream = parts.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);

            List<ICImport> icDB = icImportRepository.findAll();
            Map<String, ICImport> mapDB = new HashMap<>();
            for (ICImport icImport : icDB) {
                mapDB.put(icImport.getCode(), icImport);
            }

            List<ICImport> objsEXCEL = new ArrayList<>();
            int errorCount = 0;
            int unrealRowCounter = 0;
            List<Integer> unrealRows = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);
            fileName = parts.getOriginalFilename();
            LOGGER.info("Starting read IC Imports from file {}", fileName);
            //Find number of rows in excel file
            int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
            int indexFirstRow = 1;
            rowNum = rowCount - indexFirstRow + 1;
            //Create a loop over all the rows of excel file to read it
            for (int rowIndex = indexFirstRow; rowIndex <= rowCount; rowIndex++) {
                int realIndex = rowIndex + 1;
                Row row = sheet.getRow(rowIndex);
                ICImport obj;
                //add to objsEXCEL
                try {
                    // check row empty
                    unrealRowCounter = unrealRowIC(unrealRowCounter, row);
                    if (unrealRowCounter > 0 && unrealRowCounter <= 3) {
                        unrealRows.add(realIndex);
                        LOGGER.info("unrealRowCounter: {}", unrealRowCounter);
                    } else if (unrealRowCounter > 3) {
                        unrealRows.add(realIndex - 4);
                        break;
                    }
                    obj = buildICImport(row);
                    objsEXCEL.add(obj);
                    LOGGER.debug("added {}", objsEXCEL.getClass());
                } catch (Exception e) {
                    errorCount++;
                    errorRows.add(rowIndex);
                }
            }
            errorRows.removeAll(unrealRows);
            LOGGER.info("Unreal Rows : {}", unrealRows);
            LOGGER.info("Done read file & Rows error: {} | {}", errorCount, errorRows);
            LOGGER.info("Processing IC Imports to Update --");

            if (objsEXCEL.isEmpty()) {
                LOGGER.info("File import rỗng. Hãy kiểm tra lại!");
                return Collections.singletonMap("Error", "File import rỗng. Hãy kiểm tra lại!");
            }

            List<ICImport> icDbExistExcel = new ArrayList<>();
            List<ICImport> listICToInsertUpdate = new ArrayList<>();
            for (ICImport item : objsEXCEL) {
                ICImport ojbToInsertUpdate = mapDB.get(item.getCode());
                if (ObjectUtils.isEmpty(ojbToInsertUpdate)) {
                    ojbToInsertUpdate = new ICImport();
                    ojbToInsertUpdate.setCode(item.getCode());
                } else {
                    icDbExistExcel.add(ojbToInsertUpdate);
                }
                ojbToInsertUpdate.setFullName(item.getFullName());
                ojbToInsertUpdate.setStandardContract(item.getStandardContract());
                ojbToInsertUpdate.setStatus(item.getStatus());

                listICToInsertUpdate.add(ojbToInsertUpdate);
            }

            int success = listICToInsertUpdate.size();

            // disable all IC in DB not in excel
            icDB.removeAll(icDbExistExcel);
            for (ICImport item : icDB) {
                item.setStatus(0);
            }
            listICToInsertUpdate.addAll(icDB);

            // insert
            log.info("Saving file IC");
            List<ICImport> icImported = icImportRepository.saveAll(listICToInsertUpdate);
            log.info("Saved success all " + icImported.size() + " IC Import");
//            saveICByBatch(listICToInsertUpdate);

            result.put("Success", String.valueOf(success));
            result.put("Error at row", errorRows.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            statusImport = false;
        }

        // save history
        importICHistory.setRowNumber(rowNum);
        importICHistory.setFilename(fileName);
        String errors = errorRows.toString().replace("[", "").replace("]", "");
        errors = errors.equals("") ? "0" : errors;
        importICHistory.setErrorRow(errors);
        if (statusImport) {
            importICHistory.setStatus("Thành công");
            importICHistoryRepository.save(importICHistory);
            return result;
        } else {
            importICHistory.setStatus("Thất bại");
            importICHistoryRepository.save(importICHistory);
            return Collections.singletonMap("Error", Constants.IMPORT_ERROR);
        }
    }

    private void saveICByBatch(List<ICImport> listICToInsertUpdate) {
        if (!listICToInsertUpdate.isEmpty()) {
            LOGGER.info("listICToInsertUpdate size: " + listICToInsertUpdate.size());
            int batchIndex = 1;
            int count = 0;
            int pageSize = 300;
            while (count < listICToInsertUpdate.size()) {
                int startIndex = (batchIndex - 1) * pageSize;
                int lastIndex = batchIndex * pageSize;
                if (lastIndex >= listICToInsertUpdate.size()) {
                    lastIndex = listICToInsertUpdate.size();
                }
                List<ICImport> batch = listICToInsertUpdate.subList(startIndex, lastIndex);
                asyncObject.saveImportICToDB(batch, icImportRepository);
                count = lastIndex;
                batchIndex++;
                log.info("Saved success " + batch.size() + " IC Import in batch");
            }
            log.info("Saved success all " + listICToInsertUpdate.size() + " IC Import");
        }
    }

    private int unrealRowIC(int counter, Row row) {
        if ((Objects.isNull(row.getCell(0)) || StringUtils.isBlank(getCellValue(row, 0)))
                && (Objects.isNull(row.getCell(1)) || StringUtils.isBlank(getCellValue(row, 1)))
                && (Objects.isNull(row.getCell(2)) || StringUtils.isBlank(getCellValue(row, 2)))
        ) {
            counter++;
        }
        return counter;
    }

    private ICImport buildICImport(Row row) throws IllegalArgumentException {
        int startCellNum = 0;
        String code = getCellValue(row, startCellNum++);
        String fullName = getCellValue(row, startCellNum++);
        String standardContract = getCellValue(row, startCellNum++);

        ICImport icImport = new ICImport();
        icImport.setCode(code);
        icImport.setFullName(fullName);
        icImport.setStandardContract(standardContract);
        icImport.setStatus(1); // 1 = active, 0 = deactive
        return icImport;
    }

    private String getCellValue(Row row, int cellNum) {
        try {
            return row.getCell(cellNum).getStringCellValue().trim();
        } catch (Exception e) {
            return String.valueOf((row.getCell(0).getNumericCellValue()));
        }
    }

    private String getNumericCellValue(Row row, int cellnum) {
        try {
            return String.valueOf(row.getCell(cellnum).getNumericCellValue());
        } catch (Exception e) {
            log.error("Error extract value illustratedAccountValue, row = " + row.getRowNum());
            log.error("getNumericCellValue exception: " + e.getMessage());
        }
        return "";
    }

    @Override
    public PaginationResponse<ICImportDto> list(Long user, int page, int size, String query) {
        PaginationResponse<ICImportDto> response = new PaginationResponse<>();

        List<ICImport> ics = icImportRepository.filterList(page, size, query.trim(), 1);
        long total = icImportRepository.totalByQueryAndStatus(query.trim(), 1);
        List<ICImportDto> dtos = new ArrayList<>();
        for (ICImport icImport : ics) {
            ICImportDto dto = mapper.map(icImport, ICImportDto.class);
            dtos.add(dto);
        }
        response.setData(dtos);
        response.setTotalItem(total);
        response.setPageSize(size);
        return response;
    }

    @Override
    public PaginationResponse<ImportICHistoryDto> listHistory(Long user, int page, int size) {
        PaginationResponse<ImportICHistoryDto> response = new PaginationResponse<>();
        List<ImportICHistory> importICHistories = importICHistoryRepository.listICHistory(page, size);

        List<ImportICHistoryDto> importICHistoryDtos = new ArrayList<>();
        for (ImportICHistory importICHistory : importICHistories) {
            ImportICHistoryDto importICHistoryDto = mapper.map(importICHistory, ImportICHistoryDto.class);
            importICHistoryDtos.add(importICHistoryDto);
        }

        long totalICHistory = importICHistoryRepository.totalICHistory();
        response.setData(importICHistoryDtos);
        response.setTotalItem(totalICHistory);
        response.setPageSize(size);
        return response;
    }

    @Override
    public ICImportDto getIcInformation(String cif, String icCode, String processId, String type) {
        verifyCustomerPermission(cif, processId);

        IcInformationResp icInformation = externalV2ApiService.getIcInformation(icCode);
        if (icInformation == null || icInformation.getName().isEmpty()) {
            throw new MiniApiException("Không tồn tại IC với code " + icCode);
        }

        log.info("GetIcInformation: cif={}, processId={}, icCode={}, type={}", cif, processId, icCode, type);
        return new ICImportDto().setCode(icCode).setFullName(icInformation.getName());
    }

    @Override
    public RmInfoResp getCrmData(String cif, String processId, String rmCode, String type) {
        verifyCustomerPermission(cif, processId);

        log.info("GetCrmData: cif={}, processId={}, rmCode={}, type={}", cif, processId, rmCode, type);
        return baasService.getCrmData(rmCode);
    }


    @Override
    public RmExcelInfoResp readXlsxAndImportRmInfo(InputStream inputStream, String fileName) throws IOException {
        RmExcelInfoResp resp = new RmExcelInfoResp();

        log.info("[readXlsxAndImportRmInfo] Starting read file {}", fileName);
        List<RmExcelInfo> successData = new ArrayList<>();
        Map<Integer, String> errorData = new HashMap<>();

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // next header

        int rowIdx = 1; // start from 1
        while (rowIterator.hasNext()) {
            rowIdx++;
            Row row = rowIterator.next();
            try {
                RmExcelInfo rmExcelInfo = new RmExcelInfo();
                // Mã Hris
                Cell cell = row.getCell(0);
                String cellValue = cell == null ? null : cell.getStringCellValue();
                if (!org.springframework.util.StringUtils.hasText(cellValue)) continue;
                rmExcelInfo.setMbCode(cellValue.trim());

                // Họ và tên
                cell = row.getCell(1);
                cellValue = cell == null ? null : cell.getStringCellValue();
                if (org.springframework.util.StringUtils.hasText(cellValue)) {
                    rmExcelInfo.setFullName(cellValue.trim());
                }

                // Điện thoại
                cell = row.getCell(2);
                cellValue = cell == null ? null : cell.getStringCellValue();
                if (org.springframework.util.StringUtils.hasText(cellValue)) {
                    cellValue = cellValue.trim().replaceAll("[. ]", "");
                    String[] values = cellValue.split("[-/]");
                    rmExcelInfo.setPhone(values[0].trim());
                    if (values.length > 1) rmExcelInfo.setPhone2(values[1].trim());
                }

                // Email
                cell = row.getCell(3);
                cellValue = cell == null ? null : cell.getStringCellValue();
                if (org.springframework.util.StringUtils.hasText(cellValue)) {
                    rmExcelInfo.setEmail(cellValue.trim());
                }

                successData.add(rmExcelInfo); // add data
            } catch (Exception ex) {
                errorData.put(rowIdx, ex.getMessage());
                log.info("[readXlsxAndImportRmInfo] Error when read file {" + fileName + "} at row: " + rowIdx, ex);
            }
        }

        // SAVE DATA
        if (!successData.isEmpty()) {
            rmExcelDataCaching.setData(successData);
        }

        // RESPONSE
        if (errorData.isEmpty()) {
            resp.setSuccess(true);
            resp.setErrorData(null);
        } else {
            resp.setSuccess(false);
            resp.setErrorData(errorData);
        }

        log.info("[readXlsxAndImportRmInfo] Total record: {} ", rowIdx - 1);
        log.info("[readXlsxAndImportRmInfo] End read file {}", fileName);
        return resp;
    }

    @Override
    public PaymentCallbackManualResp manualCallbackTransaction(ManualRequest reqDto) {
        InsurancePayment byTransactionId = paymentRepository.findByTransactionId(reqDto.getMbTransactionId());
        if (byTransactionId == null || !WAITING.equals(byTransactionId.getTranStatus())) {
            throw new MiniApiException("Giao dịch không đủ điều kiện");
        }
        String mbCallback = byTransactionId.getMbCallback();
        externalAPIService.mbCallBackTransaction(gson.fromJson(mbCallback, MbCallBackTransactionReqDto.class), CallbackType.MANUAL);
        return new PaymentCallbackManualResp()
                .setMessage("In-progress")
                .setStatus(Constants.Status.SUCCESS);
    }

    @Override
    public PaymentDetailDto checkTransaction(ManualRequest reqDto) {
        InsurancePayment insurancePayment = paymentRepository.findByTransactionId(reqDto.getMbTransactionId());
        if (insurancePayment != null) {
            MbCallBackTransactionReqDto paymentHubCallback = externalAPIService.getTransaction(insurancePayment.getTransactionId());
            paymentHubCallback.setMerchant(null);

            if (PENDING.equals(insurancePayment.getTranStatus()) && PAID.equals(paymentHubCallback.getStatus())) {
                log.info("[MINI]--Update trạng thái giao dịch {}", reqDto.getMbTransactionId());
                insurancePayment.setTranStatus(WAITING);
                insurancePayment.setMbCallback(gson.toJson(paymentHubCallback));
                paymentRepository.save(insurancePayment);
            }

            return new PaymentDetailDto()
                    .setInsurancePayment(mapper.map(insurancePayment, InsurancePaymentDto.class))
                    .setPaymentHubCallback(paymentHubCallback);
        }
        throw new InsurancePaymentNotFoundException(MSG36);
    }

    private void verifyCustomerPermission(String cif, String processId) {
        if (!processId.equals(redis.get(processIdKey(cif)))) {
            throw new MiniApiException(MSG13);
        }
    }

    private void isSuperAdminAndAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }
}
