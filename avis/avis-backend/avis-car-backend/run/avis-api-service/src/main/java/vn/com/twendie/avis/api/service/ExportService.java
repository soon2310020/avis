package vn.com.twendie.avis.api.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import vn.com.twendie.avis.api.model.export.ExportConfig;
import vn.com.twendie.avis.api.model.response.WorkbookWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import static vn.com.twendie.avis.api.model.export.ExportConfig.DEFAULT_EXPORT_CONFIG;

public interface ExportService {

    Workbook export(Collection<?> items, Workbook workbook, ExportConfig exportConfig);

    WorkbookWrapper exportToWrapper(Collection<?> items, Workbook workbook, ExportConfig exportConfig);

    Workbook export(Object item, Workbook workbook, int keyColumnIndex, int lastRowSearching) throws IOException;

    default Workbook export(Collection<?> items, Workbook workbook) {
        return export(items, workbook, DEFAULT_EXPORT_CONFIG);
    }

    default Workbook export(Collection<?> items, InputStream inputStream, ExportConfig exportConfig) throws IOException {
        return export(items, new XSSFWorkbook(inputStream), exportConfig);
    }

    default WorkbookWrapper exportToWrapper(Collection<?> items, InputStream inputStream, ExportConfig exportConfig) throws IOException {
        return exportToWrapper(items, new XSSFWorkbook(inputStream), exportConfig);
    }

    default Workbook export(Collection<?> items, InputStream inputStream) throws IOException {
        return export(items, inputStream, DEFAULT_EXPORT_CONFIG);
    }

    default Workbook export(Collection<?> items, String templateFileName) throws IOException {
        return export(items, new FileInputStream(templateFileName));
    }

    default void exportToStream(Collection<?> items, OutputStream outputStream, Workbook workbook, ExportConfig exportConfig) throws IOException {
        workbook = export(items, workbook, exportConfig);
        workbook.write(outputStream);
        workbook.close();
    }

    default void exportToStream(Collection<?> items, OutputStream outputStream, Workbook workbook) throws IOException {
        exportToStream(items, outputStream, workbook, DEFAULT_EXPORT_CONFIG);
    }

    default void exportToStream(Collection<?> items, OutputStream outputStream, InputStream inputStream, ExportConfig exportConfig) throws IOException {
        exportToStream(items, outputStream, new XSSFWorkbook(inputStream), exportConfig);
    }

    default void exportToStream(Collection<?> items, OutputStream outputStream, InputStream inputStream) throws IOException {
        exportToStream(items, outputStream, inputStream, DEFAULT_EXPORT_CONFIG);
    }

    default void exportToStream(Collection<?> items, OutputStream outputStream, String templateFileName) throws IOException {
        exportToStream(items, outputStream, new FileInputStream(templateFileName));
    }

    default void exportToStream(OutputStream outputStream, Workbook workbook) throws IOException{
        workbook.write(outputStream);
        workbook.close();
    }

    Workbook export(Object item, Workbook workbook, int keyColumnIndex, int firstRowSearching, int lastRowSearching) throws IOException;
}
