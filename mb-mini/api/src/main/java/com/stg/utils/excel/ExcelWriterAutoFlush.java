package com.stg.utils.excel;

import com.stg.service.dto.customer.CustomerDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ExcelWriterAutoFlush {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelWriterAutoFlush.class);

    // using auto flush and default window size 100
    public void writeToExcelAutoFlush(List<CustomerDto> excelData, List<String> headers, HttpServletResponse response) {
        ServletOutputStream fos = null;
        try (SXSSFWorkbook wb = new SXSSFWorkbook(SXSSFWorkbook.DEFAULT_WINDOW_SIZE/* 100 */)){
            // keep 100 rows in memory, exceeding rows will be flushed to disk

            Sheet sh = wb.createSheet();
            @SuppressWarnings("unchecked")
            int rowNum = 0;
            Row row = sh.createRow(rowNum);
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = row.createCell(col);
                cell.setCellValue(headers.get(col));
            }

            for (CustomerDto excelModel : excelData) {
                row = sh.createRow(rowNum++ + 1);

                Cell cell = row.createCell(0);
                cell.setCellValue(excelModel.getMbId());

                cell = row.createCell(1);
                cell.setCellValue(excelModel.getFullName());

                cell = row.createCell(2);
                cell.setCellValue(excelModel.getEmail());

                cell = row.createCell(3);
                cell.setCellValue(excelModel.getBirthday());
            }

            fos = response.getOutputStream();
            wb.write(fos);

        } catch (Exception ex) {
            System.out.println("writeToExcelAutoFlush exception: " + ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                System.out.println("writeToExcelAutoFlush fos != null error: " + e.getMessage());
            }
        }
    }
}
