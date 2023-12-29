package com.stg.utils.excel;

import com.stg.errors.ExcelException;
import com.stg.utils.DateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import static java.sql.Types.NUMERIC;
import static org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType.FORMULA;

public class ExcelUtil {
    public static final DecimalFormat df = new DecimalFormat("###.#");

    // validate loại dữ liệu
    public static String validateCell(CellInfo cellInfo, String value) throws ExcelException {
        if(value == null || value.isEmpty()) {
            if(cellInfo.isRequired()) {
                throw new ExcelException("Không được để trống");
            }
            return value;
        }

        if(cellInfo.getCellType() == CellValueType.KEY_VALUE && value != null) {
            value = value.split("-")[0].trim();
        }
        // Check list in
        if(cellInfo.getCellType() == CellValueType.KEY_VALUE && cellInfo.getValueIn() != null) {
            if(!cellInfo.getValueIn().contains(value)) {
                throw new ExcelException(String.format("Chỉ nhận giá trị (%s)", String.join(",",cellInfo.getValueIn())));
            }
        }
        // check length
        if(cellInfo.getMaxLength() > 0 && value.length() > cellInfo.getMaxLength()) {
            throw new ExcelException(String.format("Vượt quá độ dài cho phép(%s)", cellInfo.getMaxLength()));
        }

        // check Kiểu dữ liệu
        value = validateDataType(cellInfo, value);
        return value;
    }

    private static String validateDataType(CellInfo cellInfo, String value) throws ExcelException {
        if(cellInfo.getCellType() == CellValueType.FULL_DATE || cellInfo.getCellType() == CellValueType.SHORT_DATE) {
            String format = cellInfo.getCellType() == CellValueType.FULL_DATE ? "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy";
            java.util.Date date = DateUtil.stringToDate(value, format);
            if(date == null) {
                throw new ExcelException("Không phải kiểu ngày tháng");
            }
            if(cellInfo.getCellType() == CellValueType.SHORT_DATE) {
                value += " 00:00:00";
            }
        } else {
            if(cellInfo.getCellType() == CellValueType.LONG_NUMBER || cellInfo.getCellType() == CellValueType.DOUBLE_NUMBER) {
                value = value.replace(",", "");
            }

            try{
                Class[] cls = new Class[] { String.class };
                cellInfo.getClassType().getConstructor(cls).newInstance(value);
            } catch (Exception e) {
                throw new ExcelException("Kiểu dữ liệu không đúng");
            }
        }
        return value;
    }

    // get dữ liệu từ cell
    public static String getCellValue(Cell cell, FormulaEvaluator evaluator, CellInfo cellInfo) {
        DataFormatter dataFormatter = new DataFormatter();
        String value = "";

        if (cell == null) {
            return value;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    if (cell.getDateCellValue() != null) {
                        String formatted = cellInfo.getCellType() == CellValueType.FULL_DATE ? "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(formatted);
                        value = sdf.format(cell.getDateCellValue());
                    }
                } else {
                    value = df.format(cell.getNumericCellValue());
                }
                break;
            case FORMULA:
                return dataFormatter.formatCellValue(cell, evaluator).trim();
            default:
                value = cell.getStringCellValue().trim();
        }
        return value.trim();
    }
}
