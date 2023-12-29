package saleson.common.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.util.Pair;
import saleson.dto.HeaderDTO;
import saleson.model.customField.CustomFieldValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelCommonUtils {

    public static CellStyle cloneCellStyle(Sheet sheet, int rowIndex, int colIndex) {
//        try {
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        Row row = sheet.getRow(rowIndex);
        try {
            cellStyle.cloneStyleFrom(row.getCell(colIndex).getCellStyle());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        cellStyle.setWrapText(true);
        return cellStyle;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return createCellStyleValue(sheet.getWorkbook());
    }
    public static void setFullBorderStyle(CellStyle cellStyle){
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);

        cellStyle.setWrapText(true);
    }
    public static Row getOrCreateRow(Sheet sheet, int indexRow) {
        Row row = sheet.getRow(indexRow);
        return row != null ? row : sheet.createRow(indexRow);
    }
    public static Row insertRow(Sheet sheet, int startRow) {
        return insertRow(sheet,startRow,1);
    }
    public static Row insertRow(Sheet sheet, int startRow,int numRow) {
        int lastRow = sheet.getLastRowNum();
        if (lastRow < startRow) {
            sheet.createRow(startRow);
        }

        sheet.shiftRows(startRow, lastRow, numRow, true, true);
        sheet.createRow(startRow);
        Row row = sheet.getRow(startRow);
        return row;
    }

    public static Cell getOrCreateCell(Row row, int indexCol) {
        Cell cell = row.getCell(indexCol);
        return cell != null ? cell : row.createCell(indexCol);
    }

    public static Cell getOrCreateCell(Sheet sheet, int indexRow, int indexCol) {
        Row row = getOrCreateRow(sheet, indexRow);
        return getOrCreateCell(row, indexCol);
    }

    public static void addValidationData(Sheet sheet, List<String> listValue, int firstRow, int lastRow, int firstCol, int lastCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidation dstDataValidation = helper.createValidation(helper.createExplicitListConstraint(listValue.toArray(new String[listValue.size()])),
                addressList);
        //        dstDataValidation.createPromptBox("prompt header", "prompt content");
        dstDataValidation.setShowErrorBox(true);
//        dstDataValidation.setShowPromptBox(true);
        dstDataValidation.setEmptyCellAllowed(false);
        sheet.addValidationData(dstDataValidation);
    }

    public static int makeHeader(int colGroupStart, CellStyle styleGroup, CellStyle styleHeader,
                                 List<HeaderDTO> headerDTOList, String groupName, Sheet sheet) {
        return makeHeader(colGroupStart, styleGroup, styleHeader, headerDTOList, groupName, sheet, 0, 1,2);
    }

    public static int makeHeader(int colGroupStart, CellStyle styleGroup, CellStyle styleHeader,
                                 List<HeaderDTO> headerDTOList, String groupName, Sheet sheet, int rowGroupIndex,
                                 int rowHeaderIndex, int rowDefaultValueIndex) {
//        return makeHeader(colGroupStart,colGroupStart,styleGroup, styleHeader, headerDTOList, groupName, sheet);
//    }
//    public static int makeHeader(int colGroupStart,int colHeaderStart, CellStyle styleGroup, CellStyle styleHeader, List<HeaderDTO> headerDTOList, String groupName, Sheet sheet){

        Font redFont = sheet.getWorkbook().createFont();
        redFont.setColor(Font.COLOR_RED);
        redFont.setBold(true);
        redFont.setFontName("Calibri");
        //header
        Row rowHeader = ExcelCommonUtils.getOrCreateRow(sheet, rowHeaderIndex);
        Row rowDefaultValue = ExcelCommonUtils.getOrCreateRow(sheet, rowDefaultValueIndex);
        int startCol = colGroupStart;
        for (HeaderDTO headerDTO : headerDTOList) {
            sheet.setColumnWidth(startCol, headerDTO.getColumnWith());
            int col = startCol++;
            Cell cell = rowHeader.createCell(col);
            cell.setCellValue(headerDTO.getName());
            cell.setCellStyle(styleHeader);
            if (headerDTO.isRequired()) {
                XSSFRichTextString value = new XSSFRichTextString(headerDTO.getName() + "*");
                value.applyFont(headerDTO.getName().length(), headerDTO.getName().length() + 1, redFont);
                cell.setCellValue(value);
            }
            if (headerDTO.getDefaultValue() != null) {
                Cell defaultValueCell = rowDefaultValue.createCell(col);
                defaultValueCell.setCellValue(headerDTO.getDefaultValue());
            }
            if (CollectionUtils.isEmpty(headerDTO.getSubHeader()) && rowHeaderIndex + 1 < rowDefaultValueIndex) {
                sheet.addMergedRegion(new CellRangeAddress(rowHeaderIndex, rowDefaultValueIndex - 1, col, col));
            } else if (CollectionUtils.isNotEmpty(headerDTO.getSubHeader())) {
                System.out.println(String.format("Have subData: firstRow %s, lastRow %s, int firstCol %s, int lastCol %s,",rowHeaderIndex, rowHeaderIndex, col, (col + headerDTO.getSubHeader().size()-1)));
                sheet.addMergedRegion(new CellRangeAddress(rowHeaderIndex, rowHeaderIndex, col, (col + headerDTO.getSubHeader().size()-1)));
                startCol = startCol + (headerDTO.getSubHeader().size()-1);
                Row rowSubHeader = ExcelCommonUtils.getOrCreateRow(sheet, rowHeaderIndex+1);
                int colSubHeader = col;
                for (HeaderDTO childHeader : headerDTO.getSubHeader()) {
                    sheet.setColumnWidth(colSubHeader, headerDTO.getColumnWith());
                    Cell cellSubHeader = rowSubHeader.createCell(colSubHeader++);
                    cellSubHeader.setCellValue(childHeader.getName());
                    cellSubHeader.setCellStyle(styleHeader);
                }
            }

        }
        Row row = ExcelCommonUtils.getOrCreateRow(sheet, rowGroupIndex);
        Cell cell = row.createCell(colGroupStart);
        int endcol = startCol;
        System.out.println(String.format("%s, %s", colGroupStart, endcol - 1));
        //Merged region must contain 2 or more cells
        if (endcol-1 > colGroupStart)
            sheet.addMergedRegion(new CellRangeAddress(rowGroupIndex, rowGroupIndex, colGroupStart, endcol - 1));
        cell.setCellStyle(styleGroup);
        cell.setCellValue(groupName);
        return endcol;
    }

    public static Cell mergedRegionAndSetVal(Sheet sheet, int startRow, int endRow, int startCol, int endCol, CellStyle styleGroup,
                                             String value) {
        Cell cell = getOrCreateCell(sheet, startRow, startCol);
        //update style
        for(int r=startRow;r<=endRow;r++){
            for(int c=startCol;c<=endCol;c++){
                getOrCreateCell(sheet,r,c).setCellStyle(styleGroup);
            }
        }
        if (!(startRow == endRow && startCol == endCol))
            sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, startCol, endCol));

        cell.setCellStyle(styleGroup);
        cell.setCellValue(value);
        return cell;
    }

    public static int writeCellValue(int indexRow, int indexCol, List<HeaderDTO> list, Long objectId, Map<Long, Map<Long, List<CustomFieldValue>>> mapValueCustomField, CellStyle cellStyle, Sheet sheet) {
        return writeCellValue(indexRow, indexCol, list, mapValueCustomField.containsKey(objectId) ? mapValueCustomField.get(objectId) : new HashMap<>(), cellStyle, sheet);
    }

    public static int writeCellValue(int indexRow, int indexCol, List<HeaderDTO> list, Map<Long, List<CustomFieldValue>> customFieldOneObject, CellStyle cellStyle, Sheet sheet) {
        for (HeaderDTO headerDTO : list) {
            try {
                Cell cell = getOrCreateCell(sheet, indexRow, indexCol++);
                cell.setCellStyle(cellStyle);
                if (customFieldOneObject.containsKey(Long.valueOf(headerDTO.getCode()))) {
                    List<CustomFieldValue> valueList = customFieldOneObject.get(Long.valueOf(headerDTO.getCode()));
                    if (valueList != null && !valueList.isEmpty()) {
                        cell.setCellValue(valueList.get(0).getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return indexCol;
    }

    public static void main(String[] args) throws IOException {

        String path = "D:\\success.xlsx";
        String sheetName = "sheetlist";

        XSSFWorkbook wb = null;
        XSSFSheet sheetlist = null;

        File inputFile = new File(path);
        if (inputFile.exists()) {
            wb = new XSSFWorkbook(new FileInputStream(path));
        } else {
            wb = new XSSFWorkbook();// excel file object
        }

        if (wb.getSheet(sheetName) == null) {
            sheetlist = wb.createSheet(sheetName);// Worksheet object
        } else {
            sheetlist = wb.getSheet(sheetName);// Worksheet object
        }

        DataValidationHelper helper = sheetlist.getDataValidationHelper();

        List<XSSFDataValidation> dataValidations = sheetlist.getDataValidations();
        for (XSSFDataValidation dv : dataValidations) {
            // existing verification
        }

        //
        CellRangeAddressList dstAddrList = new CellRangeAddressList(0, 500, 0, 0);// Rule one cell range
        String[] textlist = {"List 1", "List 2", "List 3", "List 4", "List 5"};
        DataValidation dstDataValidation = helper.createValidation(helper.createExplicitListConstraint(textlist),
                dstAddrList);
//        dstDataValidation.createPromptBox("prompt header", "prompt content");
        dstDataValidation.setShowErrorBox(true);
//        dstDataValidation.setShowPromptBox(true);
        dstDataValidation.setEmptyCellAllowed(false);
        sheetlist.addValidationData(dstDataValidation);

        CellRangeAddressList dstAddrList2 = new CellRangeAddressList(0, 500, 1, 1);// Rule two cell range
        DataValidationConstraint dvc = helper.createNumericConstraint(DVConstraint.ValidationType.INTEGER,
                DVConstraint.OperatorType.BETWEEN, "0", "9999999999");
        DataValidation dstDataValidation2 = helper.createValidation(dvc, dstAddrList2);
        dstDataValidation2.createErrorBox("Fill in the wrong!", "Can only fill in the  ");
        dstDataValidation2.setEmptyCellAllowed(false);
        dstDataValidation2.setShowErrorBox(true);

        sheetlist.addValidationData(dstDataValidation2);

        FileOutputStream out = new FileOutputStream(path);
        wb.write(out);
        out.close();
    }

    public static Cell writeCellValue(Row row, int indexCol, CellStyle cellStyle,
                                      String value) {
        Cell cell = getOrCreateCell(row, indexCol);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(value != null ? value : "");
        return cell;
    }

    public static Cell writeCellValue(Sheet sheet, int indexRow, int indexCol, CellStyle cellStyle,
                                      String value) {
        Row row = getOrCreateRow(sheet, indexRow);
        return writeCellValue(row, indexCol, cellStyle, value);
    }
    public static void removeRageCell(Sheet sheet,int startRow,int endRow,int startCol, int endCol){
        for(int r=startRow;r<=endRow;r++){
            for (int c=startCol;c<=endCol;c++){
                Row row=getOrCreateRow(sheet, r);
                Cell cell=getOrCreateCell(row,c);
                row.removeCell(cell);
            }
        }
    }


    public static Object getCellContentRow(int c, Row row) {
        Cell cell = row.getCell(c);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getRichStringCellValue().getString();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.FORMULA) {
            return cell.getCellFormula();
        } else return "";


    }
    public static String getCellStrContent(Row row,int c) {

        String temp = getCellContentRow(c, row).toString().trim();
        if (temp.endsWith(".0") && getOrCreateCell(row,c).getCellType() == CellType.NUMERIC) {
            return temp.substring(0, temp.length() - 2);
        }
        return temp;
    }

    final static int NUM_COL_CHECK_EXIST =6;
    public static boolean existData(Row row, Integer maxColToCheck) {
        boolean exist = false;
        if (row != null && row.getLastCellNum() > 0) {
            for (int i = 0; i < row.getLastCellNum() && (maxColToCheck==null || i < maxColToCheck); i++) {
                if (row.getCell(i) != null && !StringUtils.isEmpty(row.getCell(i).toString())) {
                    return true;
                }
                ;
            }
        }
        return exist;
    }

    public static Map<Integer, String> getRowValueAsMap(Row row) {
        Map<Integer, String> map = new HashMap<>();
        if (row != null && row.getLastCellNum() > 0) {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                if (row.getCell(i) == null || StringUtils.isEmpty(row.getCell(i).toString())) {
                    continue;
                }
                map.put(i, ExcelCommonUtils.getCellStrContent(row, i).trim());
            }
        }
        return map;
    }

    public static List<Pair<Integer, Map<Integer, String>>> readSheetValue(Sheet worksheet, int startRow, int startCol, int endCol, int endRow) {
        List<Pair<Integer, Map<Integer, String>>> pairList = new ArrayList<>();
        for (int i = startRow; i <= worksheet.getLastRowNum() && i <= endRow; i++) {
            Row row = worksheet.getRow(i);
            if (!existData(row, NUM_COL_CHECK_EXIST)) continue;
            pairList.add(Pair.of(i, getRowValueAsMap(row)));
        }

        return pairList;
    }

    public static int getNbOfMergedRegions(Sheet sheet, int row)
    {
        int count = 0;
        for(int i = 0; i < sheet.getNumMergedRegions(); ++i)
        {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstRow() <= row && range.getLastRow() >= row)
                ++count;
        }
        return count;
    }
    public static int getNbOfMergedRegionsColInRow(Sheet sheet, int row)
    {
        int count = 0;
        for(int i = 0; i < sheet.getNumMergedRegions(); ++i)
        {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstRow() <= row && range.getLastRow() >= row && range.getFirstColumn()< range.getLastColumn())
                ++count;
        }
        return count;
    }
    public static void bindDataToCell(String value,Row row,int indexCell,CellStyle style){
        Cell cell = row.createCell(indexCell);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static CellStyle cloneCellStyleFromOtherSheet(Sheet sourceSheet,Sheet targetSheet, int rowIndex, int colIndex) {
//        try {
        CellStyle cellStyle = targetSheet.getWorkbook().createCellStyle();
        Row row = sourceSheet.getRow(rowIndex);
        try {
            cellStyle.cloneStyleFrom(row.getCell(colIndex).getCellStyle());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

//        cellStyle.setWrapText(true);
        return cellStyle;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return createCellStyleValue(sheet.getWorkbook());
    }
}
