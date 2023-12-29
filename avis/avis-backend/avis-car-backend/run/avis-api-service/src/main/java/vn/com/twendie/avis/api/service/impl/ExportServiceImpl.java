package vn.com.twendie.avis.api.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.javatuples.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.ReflectUtils;
import vn.com.twendie.avis.api.mapping.ValueMapping;
import vn.com.twendie.avis.api.model.export.ExportConfig;
import vn.com.twendie.avis.api.model.response.WorkbookWrapper;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.service.ExportPdfService;
import vn.com.twendie.avis.api.service.ExportService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    private final ReflectUtils reflectUtils;


    public ExportServiceImpl(ReflectUtils reflectUtils) {
        this.reflectUtils = reflectUtils;
    }

    @Override
    public Workbook export(Collection<?> items, Workbook workbook, ExportConfig exportConfig) {
        Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
        Row keyRow = sheet.getRow(exportConfig.getKeyRowIndex());
        Row sampleRow = sheet.getRow(exportConfig.getSampleRowIndex());

        setSheetName(sheet, exportConfig.getSheetName());

        AtomicInteger rowIndex = new AtomicInteger(exportConfig.getSampleRowIndex());

        if (!CollectionUtils.isEmpty(items)) {

            sheet.shiftRows(exportConfig.getSampleRowIndex(), sheet.getLastRowNum(), items.size());

            items.forEach(item -> {
                Row row = getOrCreateRow(sheet, rowIndex.getAndIncrement());
                if (exportConfig.isCopyRowHeight()) {
                    row.setHeight(sampleRow.getHeight());
                }
                keyRow.cellIterator().forEachRemaining(keyCell -> {
                    int columnIndex = keyCell.getColumnIndex();
                    Cell sampleCell = sampleRow.getCell(columnIndex);
                    Cell cell = getOrCreateCell(row, columnIndex);
                    cell.setCellStyle(sampleCell.getCellStyle());
                    String cellValue = keyCell.getStringCellValue();
                    if (StringUtils.isNotBlank(cellValue)) {
                        String[] fieldChain;
                        if (cellValue.contains("[")) {
                            fieldChain = getFieldChainWithCondition(cellValue);
                        } else {
                            fieldChain = cellValue.split("\\.");
                        }
                        setCellValue(cell, getValue(item, fieldChain, cell.getRowIndex()), sampleCell);
                    } else if (Objects.nonNull(exportConfig.getColumnIndex())
                            && columnIndex == exportConfig.getColumnIndex()) {
                        cell.setCellValue(rowIndex.get() - exportConfig.getSampleRowIndex());
                    }
                });
            });
        }

        deleteRow(keyRow);
        deleteRow(sampleRow);

        return workbook;
    }

    @Override
    public WorkbookWrapper exportToWrapper(Collection<?> items, Workbook workbook, ExportConfig exportConfig) {
        Workbook workbook1 = export(items, workbook, exportConfig);
        return WorkbookWrapper.builder()
                .workbook(workbook1)
                .lastRowIndexWithData(CollectionUtils.isEmpty(items) ? exportConfig.getSampleRowIndex() - 2
                        : exportConfig.getSampleRowIndex() + items.size() - 2)
                .build();
    }

    @Override
    public Workbook export(Object item, Workbook workbook, int keyColumnIndex, int lastRowSearching) throws IOException {
        return export(item, workbook, keyColumnIndex, 0, lastRowSearching);
    }

    @Override
    public Workbook export(Object item, Workbook workbook, int keyColumnIndex, int firstRowSearching, int lastRowSearching) throws IOException {
        Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
        for (int i = firstRowSearching; i <= lastRowSearching; i++) {
            Row row = getOrCreateRow(sheet, i);
            Cell cell = getOrCreateCell(row, keyColumnIndex);
            String cellValue = cell.getStringCellValue();
            if (StringUtils.isNotBlank(cellValue)) {
                String[] fieldChain = cellValue.split("\\.");
                setCellValue(cell, getValue(item, fieldChain, cell.getRowIndex()), cell);
            }
        }

        return workbook;
    }

    private String[] getFieldChainWithCondition(String cellValue) {
        Pattern pattern = Pattern.compile("^((?<property>[.\\w]+)\\.)*(?<condition>\\w+\\[[.\\w]+=\\w+])\\.(?<field>[.\\w]+)$");
        Matcher m = pattern.matcher(cellValue);
        if (m.find()) {
            List<String> fieldChains = new ArrayList<>();

            if (Objects.nonNull(m.group("property"))) {
                Collections.addAll(fieldChains, m.group("property").split("\\."));
            }

            fieldChains.add(m.group("condition"));
            Collections.addAll(fieldChains, m.group("field").split("\\."));
            return fieldChains.toArray(new String[0]);
        } else {
            return null;
        }
    }

    private Object getValue(Object object, String[] fieldChain, Integer rowIndex) {
        if (Objects.isNull(object)) {
            return null;
        }
        try {
            Object value;
            ValueMapping<?> valueMapping;
            Pair<String, Pair<String, Object>> pair = parsePropertyStr(fieldChain[0]);
            String property = pair.getValue0();
            Pair<String, Object> condition = pair.getValue1();
            Field field = reflectUtils.getField(object.getClass(), property);
            if (Objects.nonNull(field)) {
                value = field.get(object);
                valueMapping = ValueMapping.getValueMapping(field);
            } else {
                Method method = reflectUtils.getMethod(object.getClass(), property);
                if (Objects.nonNull(method)) {
                    value = method.invoke(object);
                    valueMapping = ValueMapping.getValueMapping(method);
                } else {
                    throw new NotFoundException("Not found property: " + property + " in class: " + object.getClass());
                }
            }
            if (Objects.nonNull(condition) && value instanceof Collection) {
                value = filterCollection((Collection<?>) value, condition, rowIndex);
            }
            if (fieldChain.length > 1) {
                value = getValue(value, Arrays.copyOfRange(fieldChain, 1, fieldChain.length), rowIndex);
            }
            return valueMapping.map(value);
        } catch (Exception e) {
            log.error("Error when get value of field chain: {}, {}, at row: {}",
                    String.join(".", fieldChain),
                    ExceptionUtils.getRootCauseMessage(e),
                    rowIndex);
            return null;
        }
    }

    private Pair<String, Pair<String, Object>> parsePropertyStr(String propertyStr) {
        Pattern pattern = Pattern.compile("^(?<property>\\w+)\\[(?<key>[.\\w]+)=(?<value>\\w+)]$");
        Matcher m = pattern.matcher(propertyStr);
        if (m.find()) {
            return Pair.with(m.group("property"),
                    Pair.with(m.group("key"), m.group("value")));
        } else {
            return Pair.with(propertyStr, null);
        }
    }

    private Object filterCollection(Collection<?> collection, Pair<String, Object> condition, Integer rowIndex) {
        String[] fieldChain = condition.getValue0().split("\\.");
        Object value = condition.getValue1();
        return collection.stream()
                .filter(item -> Objects.equals(value, getValue(item, fieldChain, rowIndex)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("Not found item satisfy condition %s=%s", condition.getValue0(), value)));
    }

    private void setCellValue(Cell cell, Object value, Cell sampleCell) {
        if (Objects.nonNull(value)) {
            switch (sampleCell.getCellType()) {
                case NUMERIC:
                    try {
                        cell.setCellValue(new BigDecimal(String.valueOf(value)).doubleValue());
                        break;
                    } catch (Exception e) {}
                default:
                    cell.setCellValue(String.valueOf(value));
            }
        } else {
            cell.setBlank();
        }
    }

    private Row getOrCreateRow(Sheet sheet, int index) {
        if (sheet.getPhysicalNumberOfRows() > index
                && Objects.nonNull(sheet.getRow(index))) {
            return sheet.getRow(index);
        } else {
            return sheet.createRow(index);
        }
    }

    private Cell getOrCreateCell(Row row, int index) {
        if (row.getPhysicalNumberOfCells() > index) {
            return Objects.isNull(row.getCell(index)) ? row.createCell(index)
                    : row.getCell(index);
        } else {
            return row.createCell(index);
        }
    }

    private void deleteRow(Row row) {
        Sheet sheet = row.getSheet();
        sheet.shiftRows(row.getRowNum() + 1, sheet.getPhysicalNumberOfRows(), -1);
    }

    private void setSheetName(Sheet sheet, String sheetName) {
        if (Objects.nonNull(sheetName)) {
            Workbook workbook = sheet.getWorkbook();
            int sheetIndex = workbook.getSheetIndex(sheet);
            workbook.setSheetName(sheetIndex, sheetName);
        }
    }

}
