package com.stg.utils.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ExcelService {

    public static Workbook getWorkbook(String filePath) throws IOException {
        Workbook workbook = null;
        InputStream is = Workbook.class.getClassLoader().getResourceAsStream("templates/" + filePath);
        if (filePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(is);
        } else if (filePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }
    public static Workbook writeExcel(String filePath, CellInfo[] rowInfo, List<Map<String, Object>> dataMap, int startIndex) throws IOException {
        Workbook workbook = getWorkbook(filePath);
        Sheet sheet = workbook.getSheetAt(0);

        for(int i = 0; i< dataMap.size(); i++) {
            Row headerRow = sheet.createRow(i + startIndex);
            Map<String, Object> map = dataMap.get(i);

            int j = 0;
            for(CellInfo cellInfo: rowInfo) {
                Cell cell = headerRow.createCell(j);
                String value = "";
                if(map.containsKey(cellInfo.getKey())) {
                    Object valueObj = map.get(cellInfo.getKey());
                    if(valueObj != null) {
                        value = valueObj.toString();
                    }
                }
                cell.setCellValue(value);
                j++;
            }
        }
        return workbook;
    }


}
