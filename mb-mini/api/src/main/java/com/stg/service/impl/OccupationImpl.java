package com.stg.service.impl;

import com.stg.entity.ImportOccupationHistory;
import com.stg.entity.Occupation;
import com.stg.repository.ImportOccupationHistoryRepository;
import com.stg.repository.OccupationRepository;
import com.stg.service.OccupationService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.occupation.ImportOccupationHistoryDto;
import com.stg.service.dto.occupation.OccupationDTO;
import com.stg.utils.Constants;
import com.stg.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OccupationImpl implements OccupationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OccupationImpl.class);
    private static final int INDEX_FIRST_ROW = 3;

    private final OccupationRepository occupationRepository;
    private final ImportOccupationHistoryRepository importOccupationHistoryRepository;

    @Autowired
    private final ModelMapper mapper;


    @Override
    public PaginationResponse<OccupationDTO> list(Long user) {
        PaginationResponse<OccupationDTO> response = new PaginationResponse<>();
        List<Occupation> occupations = occupationRepository.findAll();
        List<OccupationDTO> occupationDTOS = new ArrayList<>();
        for (Occupation occupation : occupations) {
            OccupationDTO occupationDTO = mapper.map(occupation, OccupationDTO.class);
            occupationDTOS.add(occupationDTO);
        }

        response.setData(occupationDTOS);
        return response;
    }

    @Override
    public PaginationResponse<ImportOccupationHistoryDto> listHistory(Long user, int page, int size) {
        PaginationResponse<ImportOccupationHistoryDto> response = new PaginationResponse<>();
        List<ImportOccupationHistory> occupationHistories = importOccupationHistoryRepository.listOccupationHistory(page, size);

        List<ImportOccupationHistoryDto> occupationHistoryDtos = new ArrayList<>();
        for (ImportOccupationHistory importOccupationHistory : occupationHistories) {
            ImportOccupationHistoryDto occupationHistoryDto = mapper.map(importOccupationHistory, ImportOccupationHistoryDto.class);
            occupationHistoryDtos.add(occupationHistoryDto);
        }

        long totalOccupationHistory = importOccupationHistoryRepository.totalOccupationHistory();
        response.setData(occupationHistoryDtos);
        response.setTotalItem(totalOccupationHistory);
        response.setPageSize(size);
        return response;
    }

    public ResponseEntity<Map<String, String>> importList(MultipartFile parts) {
        String originalFilename = parts.getOriginalFilename();
        if (originalFilename == null) {
            return new ResponseEntity(Constants.MS_WRONG_EXTENSION_EXCEL, HttpStatus.NOT_ACCEPTABLE);
        }
        if (originalFilename.endsWith(Constants.FILE_EXTENSION) && Constants.FILE_TYPE.equals(FileUtil.getRealMimeType(parts))) {
            Map<String, String> result = readXlsxAndImportOccupation(parts);
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity(Constants.MS_WRONG_EXTENSION_EXCEL, HttpStatus.NOT_ACCEPTABLE);
    }

    private Map<String, String> readXlsxAndImportOccupation(MultipartFile parts) {
        ImportOccupationHistory importOccupationHistory = new ImportOccupationHistory();
        String fileName = "";
        int rowNum = 0;
        Map<String, String> result = new HashMap<>();
        boolean statusImport = true;
        List<Integer> errorRows = new ArrayList<>();

        try {
            InputStream inputStream = parts.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            List<Occupation> occupations = occupationRepository.findAll();
            Map<String, Occupation> map = new HashMap<>();
            for (Occupation occupation : occupations) {
                map.put(occupation.getJob(), occupation);
            }
            List<Occupation> objsEXCEL = new ArrayList<>();
            List<Occupation> objsToSave = new ArrayList<>();
            int errorCount = 0;
            int unrealRowCounter = 0;

            List<Integer> unrealRows = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);
            fileName = parts.getOriginalFilename();
            LOGGER.info("Starting read occupations from file {}", fileName);
            //Find number of rows in excel file
            int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
            rowNum = rowCount - INDEX_FIRST_ROW;

            //Create a loop over all the rows of excel file to read it
            for (int rowIndex = INDEX_FIRST_ROW; rowIndex < rowCount + 1; rowIndex++) {
                if (rowIndex == 2223 || rowIndex == 2267 || rowIndex == 2275) {
                    log.info("error??");
                }
                int realIndex = rowIndex + 1;
                Row row = sheet.getRow(rowIndex);
                Occupation obj;
                //add to objsEXCEL
                try {
                    // check row empty
                    unrealRowCounter = unrealRowCounter(unrealRowCounter, row);
                    if (unrealRowCounter > 0 && unrealRowCounter <= 3) {
                        unrealRows.add(realIndex);
                        LOGGER.info("unrealRowCounter: {}", unrealRowCounter);
                    } else if (unrealRowCounter > 3) {
                        unrealRows.add(realIndex - 4);
                        break;
                    }
                    obj = buildOccupation(row);
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
            LOGGER.debug("Before populate: In database size {} | objsEXCEL size {} ", occupations.size(), objsEXCEL.size());
            LOGGER.info("Processing occupations to Update --");

            if (objsEXCEL.isEmpty()) {
                LOGGER.info("File import rỗng. Hãy kiểm tra lại!");
                return Collections.singletonMap("Error", "File import rỗng. Hãy kiểm tra lại!");
            }
            // xóa all
            LOGGER.info("Deleting all occupations");
            occupationRepository.deleteAll();

            // save
            for (Occupation item : objsEXCEL) {
                Occupation ojbDB = new Occupation();
                ojbDB.setNo(item.getNo());
                ojbDB.setJob(item.getJob());
                ojbDB.setOccupation(item.getOccupation());
                ojbDB.setOccupationGroup(item.getOccupationGroup());
                ojbDB.setColumn1(item.getColumn1());
                ojbDB.setColumn2(item.getColumn2());
                ojbDB.setColumn3(item.getColumn3());
                ojbDB.setShorten(item.getShorten());
                objsToSave.add(ojbDB);
            }
            occupationRepository.saveAll(objsToSave);
            LOGGER.info("Done objsToSave, success {}", objsToSave.size());

            result.put("Success", String.valueOf(objsToSave.size()));
            result.put("Error at row", errorRows.toString());

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            statusImport = false;
        }

        // save history
        importOccupationHistory.setRowNumber(rowNum);
        importOccupationHistory.setFilename(fileName);
        String errors = errorRows.toString().replace("[", "").replace("]", "");
        errors = errors.equals("") ? "0" : errors;
        importOccupationHistory.setErrorRow(errors);
        if (statusImport) {
            importOccupationHistory.setStatus("Thành công");
            importOccupationHistoryRepository.save(importOccupationHistory);
            return result;
        } else {
            importOccupationHistory.setStatus("Thất bại");
            importOccupationHistoryRepository.save(importOccupationHistory);
            return Collections.singletonMap("Error", Constants.IMPORT_ERROR);
        }
    }

    private int unrealRowCounter(int counter, Row row) {
        if ((Objects.isNull(row.getCell(1)) || StringUtils.isBlank(row.getCell(1).getStringCellValue().trim()))
                && (Objects.isNull(row.getCell(2)) || StringUtils.isBlank(row.getCell(2).getStringCellValue().trim()))
        ) {
            counter++;
        }
        return counter;
    }

    private Occupation buildOccupation(Row row) throws IllegalArgumentException {
        Integer no = (int) row.getCell(0).getNumericCellValue();
        String job = row.getCell(1).getStringCellValue().trim();
        String occupation = row.getCell(2).getStringCellValue().trim();
        String occupationGroup = getCellValue(row, 3);
        String column1 = getCellValue(row, 4);
        String column2 = getCellValue(row, 5);
        String shorten = getCellValue(row, 6);
        String column3 = getCellValue(row, 7);
        return Occupation.builder()
                .no(no)
                .job(job)
                .occupation(occupation)
                .occupationGroup(occupationGroup)
                .column1(column1)
                .column2(column2)
                .shorten(shorten)
                .column3(column3)
                .build();
    }

    public String getCellValue(Row row, int cellNum) {
        try {
            return row.getCell(cellNum).getStringCellValue().trim();
        } catch (Exception e) {
            return String.valueOf(row.getCell(cellNum).getNumericCellValue());
        }
    }
}
