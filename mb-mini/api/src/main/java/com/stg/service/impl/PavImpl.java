package com.stg.service.impl;

import com.stg.entity.PavImport;
import com.stg.repository.PavImportRepository;
import com.stg.service.PavService;
import com.stg.service.dto.external.requestV2.MbalIllustrationBoardV2ReqDto;
import com.stg.utils.Constants;
import com.stg.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.stg.utils.Common.MONEY_DECIMAL_FORMAT;
import static com.stg.utils.FlexibleCommon.getInsuranceAge;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PavImpl implements PavService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PavImpl.class);
    private final PavImportRepository pavImportRepository;

    private final AsyncObjectImpl asyncObject;

    @Autowired
    private final ModelMapper mapper;

    private static final String VND = " VND";

    @Autowired
    private ExternalAPIServiceImpl externalAPIService;

    public ResponseEntity<Map<String, String>> importPav(MultipartFile parts) {
        String originalFilename = parts.getOriginalFilename();
        String contentType = FileUtil.getRealMimeType(parts);
        if (originalFilename == null) {
            return new ResponseEntity(Constants.MS_WRONG_EXTENSION_EXCEL, HttpStatus.NOT_ACCEPTABLE);
        }
        if (originalFilename.endsWith(Constants.FILE_EXTENSION) && Constants.FILE_TYPE.equals(contentType)) {
            Map<String, String> result = readXlsxAndImportPav(parts);
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity(Constants.MS_WRONG_EXTENSION_EXCEL, HttpStatus.NOT_ACCEPTABLE);
    }

    private Map<String, String> readXlsxAndImportPav(MultipartFile parts) {
        try {
            InputStream inputStream = parts.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);

            List<PavImport> pavDB = pavImportRepository.findAll();
            Map<String, PavImport> map = new HashMap<>();
            for (PavImport pavImport : pavDB) {
                String key = buildKeyPav(pavImport);
                map.put(key, pavImport);
            }

            List<PavImport> objsEXCEL = new ArrayList<>();
            int errorCount = 0;
            int unrealRowCounter = 0;
            List<Integer> errorRows = new ArrayList<>();
            List<Integer> unrealRows = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);
            String fileName = parts.getOriginalFilename();
            LOGGER.info("Starting read pavImports from file {}", fileName);
            //Find number of rows in excel file
            int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
            //Create a loop over all the rows of excel file to read it
            for (int rowIndex = 2; rowIndex < rowCount + 1; rowIndex++) {
                int realIndex = rowIndex + 1;
                Row row = sheet.getRow(rowIndex);
                PavImport obj;
                //add to objsEXCEL
                try {
                    // check row empty
                    unrealRowCounter = unrealRowPav(unrealRowCounter, row);
                    if (unrealRowCounter > 0 && unrealRowCounter <= 3) {
                        unrealRows.add(realIndex);
                        LOGGER.info("unrealRowCounter: {}", unrealRowCounter);
                    } else if (unrealRowCounter > 3) {
                        unrealRows.add(realIndex - 4);
                        break;
                    }
                    obj = buildPavImport(row);
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
            LOGGER.info("Processing pavImports to Update --");

            List<PavImport> listPavToInsertUpdate = new ArrayList<>();
            for (PavImport item : objsEXCEL) {
                String keyPavExcel = buildKeyPav(item);
                PavImport ojbToInsertUpdate = map.get(keyPavExcel);
                if (ObjectUtils.isEmpty(ojbToInsertUpdate)) {
                    ojbToInsertUpdate = new PavImport();
                    ojbToInsertUpdate.setPackageCode(item.getPackageCode());
                    ojbToInsertUpdate.setAge(item.getAge());
                    ojbToInsertUpdate.setGender(item.getGender());
                    ojbToInsertUpdate.setGroupOccupationClass(item.getGroupOccupationClass());
                    ojbToInsertUpdate.setContractYear(item.getContractYear());
                }
                ojbToInsertUpdate.setAgeInsured(item.getAgeInsured());
                ojbToInsertUpdate.setIllustratedAccountValue(item.getIllustratedAccountValue());
                ojbToInsertUpdate.setIllustratedRefundValue(item.getIllustratedRefundValue());
                ojbToInsertUpdate.setCommittedAccountValue(item.getCommittedAccountValue());
                ojbToInsertUpdate.setCommittedRefundValue(item.getCommittedRefundValue());

                listPavToInsertUpdate.add(ojbToInsertUpdate);
            }

//            pavImportRepository.saveAll(objsEXCEL);
//            LOGGER.info("Done save all records pav --");
            // insert
            savePavByBatch(listPavToInsertUpdate);


            Map<String, String> result = new HashMap<>();
            result.put("Success", String.valueOf(listPavToInsertUpdate.size()));
            result.put("Error at row", errorRows.toString());
            return result;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return Collections.singletonMap("Error", Constants.IMPORT_ERROR);
        }
    }

    private void savePavByBatch(List<PavImport> listPavToInsertUpdate) {
        if (!listPavToInsertUpdate.isEmpty()) {
            LOGGER.info("listPavToInsertUpdate size: " + listPavToInsertUpdate.size());
            int batchIndex = 1;
            int count = 0;
            int pageSize = 300;
            while (count < listPavToInsertUpdate.size()) {
                int startIndex = (batchIndex - 1) * pageSize;
                int lastIndex = batchIndex * pageSize;
                if (lastIndex >= listPavToInsertUpdate.size()) {
                    lastIndex = listPavToInsertUpdate.size();
                }
                List<PavImport> batchLeads = listPavToInsertUpdate.subList(startIndex, lastIndex);
                asyncObject.saveImportPavImportToDB(batchLeads, pavImportRepository);
                count = lastIndex;
                batchIndex++;
                log.info("Saved success " + batchLeads.size() + " PavImport in batch");
            }
            log.info("Saved success " + listPavToInsertUpdate.size() + " PavImport");
        }
    }

    public String buildKeyPav(PavImport pavImport) {
        return pavImport.getPackageCode() + "_" + pavImport.getAge() + "_" + pavImport.getGender() + "_" + pavImport.getGroupOccupationClass() + "_" + pavImport.getContractYear();
    }

    private int unrealRowPav(int counter, Row row) {
        if ((Objects.isNull(row.getCell(0)) || StringUtils.isBlank(row.getCell(0).getStringCellValue().trim()))
                && (Objects.isNull(row.getCell(2)) || StringUtils.isBlank(row.getCell(2).getStringCellValue().trim()))
        ) {
            counter++;
        }
        return counter;
    }

    private PavImport buildPavImport(Row row) throws IllegalArgumentException {
        int startCellNum = 0;
        String packageCode = getCellValue(row, startCellNum++);
        Integer age = (int) row.getCell(startCellNum++).getNumericCellValue();
        String gender = getCellValue(row, startCellNum++);
        Integer groupOccupationClass = (int) row.getCell(startCellNum++).getNumericCellValue();
        Integer contractYear = (int) (row.getCell(startCellNum++).getNumericCellValue());
        Integer ageInsured = (int) (row.getCell(startCellNum++).getNumericCellValue());
        String illustratedAccountValue = getMoneyNumericCellValue(row, startCellNum++);
        String illustratedRefundValue = getMoneyNumericCellValue(row, startCellNum++);
        String committedAccountValue = getMoneyNumericCellValue(row, startCellNum++);
        String committedRefundValue = getMoneyNumericCellValue(row, startCellNum++);

        PavImport pavImport = new PavImport();
        pavImport.setPackageCode(packageCode);
        pavImport.setAge(age);
        pavImport.setGender(gender);
        pavImport.setGroupOccupationClass(groupOccupationClass);
        pavImport.setContractYear(contractYear);
        pavImport.setAgeInsured(ageInsured);
        pavImport.setIllustratedAccountValue(illustratedAccountValue);
        pavImport.setIllustratedRefundValue(illustratedRefundValue);
        pavImport.setCommittedAccountValue(committedAccountValue);
        pavImport.setCommittedRefundValue(committedRefundValue);
        return pavImport;
    }

    public String getCellValue(Row row, int cellNum) {
        try {
            return row.getCell(cellNum).getStringCellValue().trim();
        } catch (Exception e) {
            return String.valueOf((row.getCell(0).getNumericCellValue()));
        }
    }

    private static String getMoneyNumericCellValue(Row row, int cellNum) {
        try {
            return MONEY_DECIMAL_FORMAT.format(row.getCell(cellNum).getNumericCellValue());
        } catch (Exception e) {
            log.error("[MINI]--Error extract cell value at row {}. Message {}", row.getRowNum(), e.getMessage());
        }
        return "";
    }

    @Override
    public List<PavImport> filterList(MbalIllustrationBoardV2ReqDto illustrationBoardDto) {
        // calculate age
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(illustrationBoardDto.getAge() + " 00:00", formatter);
        int age = getInsuranceAge(dateTime, LocalDateTime.now());
//        String code = PackageNameEnum.getCodePavFromCodeMbal(illustrationBoardDto.getPackageCode());
        List<PavImport> pavImports = pavImportRepository.listPav(illustrationBoardDto.getPackageCode(), age, illustrationBoardDto.getGender(), illustrationBoardDto.getOccupationClass());
        return pavImports;
    }

}
