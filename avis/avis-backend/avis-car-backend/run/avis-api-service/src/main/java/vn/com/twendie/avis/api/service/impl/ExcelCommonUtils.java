package vn.com.twendie.avis.api.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

public class ExcelCommonUtils {

    public static CellStyle cloneCellStyle(Sheet sheet, int rowIndex, int colIndex) {
//        try {
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        Row row = sheet.getRow(rowIndex);
        cellStyle.cloneStyleFrom(row.getCell(colIndex).getCellStyle());
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

}
