package com.stg.utils.csv;

import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.errors.UploadFileException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OpenCsvUtil {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char NO_QUOTE_CHARACTER = '\u0000';

    public static void setResponseExcel(String pattern, String fileName, HttpServletResponse response) {
        DateTimeFormatter formatCSV = DateTimeFormatter.ofPattern(pattern);
        String filePath = fileName + LocalDateTime.now().format(formatCSV) + ".xlsx";
        response.addHeader("file_name", filePath);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
    }

    public static void setResponseCSV(String pattern, String fileName, HttpServletResponse response) {
        DateTimeFormatter formatCSV = DateTimeFormatter.ofPattern(pattern);
        String filePath = fileName + LocalDateTime.now().format(formatCSV) + ".csv";
        response.addHeader("file_name", filePath);
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCsvUtil.class);

    public static List<Object> parseCsvFile(InputStream is, List<Object> headers) throws IOException {
        String[] myArray = new String[headers.size()];
        String[] CSV_HEADER = headers.toArray(myArray);
        try (Reader fileReader = new InputStreamReader(is)) {
            ColumnPositionMappingStrategy<Object> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(Object.class);
            mappingStrategy.setColumnMapping(CSV_HEADER);
            CsvToBean<Object> csvToBean = new CsvToBeanBuilder<>(fileReader)
                    .withMappingStrategy(mappingStrategy)
                    .withSkipLines(1)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<Object> objects = csvToBean.parse();
            return objects;
        }
    }

    @SuppressWarnings("unchecked")
    public static void write(Writer writer, List data, Class clazz) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        CustomMappingStrategy mappingStrategy = new CustomMappingStrategy();
        mappingStrategy.setType(clazz);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer)
                .withMappingStrategy(mappingStrategy)
//                .withSeparator(DEFAULT_SEPARATOR)
//                .withQuotechar(NO_QUOTE_CHARACTER)
                .withApplyQuotesToAll(false)
                .build();
        beanToCsv.write(data);
        LOGGER.info("Write data success, "+data.size());
    }

    public static void buildCSVFileUTF8(HttpServletResponse response, List<List<String>> dataCSV) throws IOException {
        CSVFormat format = CSVFormat.EXCEL.withHeader("\ufeff")
                .withFirstRecordAsHeader().withIgnoreEmptyLines(true);
        ServletOutputStream os = response.getOutputStream();
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(byteArray), format)) {
            for (List<String> data : dataCSV) {
                csvPrinter.printRecord(data);
            }
            csvPrinter.flush();
            os.write(byteArray.toByteArray() , 0, byteArray.toByteArray().length);
        } catch (IOException e) {
            throw new UploadFileException("fail to export data to CSV file: " + e.getMessage());
        }
    }

}
