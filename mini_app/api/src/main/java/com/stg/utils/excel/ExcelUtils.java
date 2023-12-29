package com.stg.utils.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

public class ExcelUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

    public static Workbook getWorkbook(String filePath) throws IOException {
        Workbook workbook = null;
        InputStream is = Workbook.class.getClassLoader().getResourceAsStream(filePath);
        if (filePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(is);
        } else if (filePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }

    public static <T> void exportExcel(List<T> data, Field[] field, List<String> headers, int indexData, String templateExcel, HttpServletResponse response) throws IOException, IllegalArgumentException {
        try (Workbook workbook = getWorkbook(templateExcel)) {
            Sheet sheet = workbook.getSheetAt(0);

            AtomicInteger rowIdx = new AtomicInteger(indexData);
            data.forEach(d -> {
                Row row = sheet.createRow(rowIdx.getAndIncrement());
                for (int i = 0; i < headers.size(); i++) {
                    try {
                        String value = field[i].get(d) == null ? StringUtils.EMPTY : field[i].get(d).toString();
                        row.createCell(i).setCellValue(value);
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Create Cell Fail, " + e);
                    }
                }
            });
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            LOGGER.info("Write data success, " + data.size());
        }
    }

    // capitalize the first letter of the field name for retrieving value of the
    // field later
    public static String capitalizeInitialLetter(String s) {
        if (s.length() == 0)
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static DataValidation defaultDataValidation(DataValidation dataValidation) {
               dataValidation.setSuppressDropDownArrow(true);
               dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
               dataValidation.createErrorBox("Title", "Message");
               dataValidation.setShowErrorBox(true);
        return dataValidation;
    }

    public static String getConstrain(String sheetName, String columnName, Integer from, Integer to){
        //ex: "=Reference!$A$2:$A$343";
        return  "=" + sheetName + "!$" + columnName.trim()  + "$" + from + ":$" + columnName.trim() + "$" + to;
    }

    public static FileOutputStream write(final Workbook workbook, final String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
        return fos;
    }

    public static CellStyle newBoldCell(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(THIN);
        style.setBorderTop(THIN);
        style.setBorderRight(THIN);
        style.setBorderLeft(THIN);
        return style;
    }
}
