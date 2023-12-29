package com.stg.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Service
@Slf4j
public class HTMLtoPDFUtils {

    private static final String UTF_8 = "UTF-8";

    @Autowired
    private ObjectMapper objectMapper;

//    public static void main(String[] args) throws IOException, DocumentException {
//        try {
//            PDFThymeleafExample thymeleaf2Pdf = new PDFThymeleafExample();
//            String html = thymeleaf2Pdf.parseThymeleafTemplate("test");
////            System.out.println(html);
//            thymeleaf2Pdf.generatePdfFileFromHtml(html);

//        Map<String, Object> attributeMap = new HashMap<>();
//        attributeMap.put(UserComboAttribute.MIC_FEE_YEAR, "15");
//        PDFThymeleafExample thymeleaf2Pdf = new PDFThymeleafExample();
//        thymeleaf2Pdf.generatePdfFromHtml("test", attributeMap);

//        BigDecimal ONE_MILLION = BigDecimal.valueOf(1000000L);
//        BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000L);
//        BigDecimal test = BigDecimal.valueOf(46666.67);
//        String rs = "";
//        if(test.compareTo(ONE_MILLION) > 0){
//            int scale = 1;
//            if(test.remainder(ONE_MILLION).compareTo(BigDecimal.ZERO) == 0){
//                scale = 0;
//            }
//            rs = test.divide(ONE_MILLION, scale, RoundingMode.HALF_UP).toString() + " triệu";
//        } else {
//            rs = test.divide(ONE_THOUSAND, 0, RoundingMode.HALF_UP).toString() + " nghìn";
//        }
//        System.out.println(rs);

//        } catch(Exception ex){
//            ex.printStackTrace();
//        }

//    }

//    public void generatePdfFileFromHtml(String html) throws IOException, DocumentException {
//
//        String outputFolder = "/home/tool/thymeleaf234.pdf";
//        OutputStream outputStream = new FileOutputStream(outputFolder);
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocumentFromString(html);
//        renderer.layout();
//        renderer.createPDF(outputStream);
//
//        outputStream.close();
//    }

    public byte[] generatePdfFromHtml(String template, Map<String, Object> attributes) throws IOException, DocumentException {
        String renderedHtmlContent = parseThymeleafTemplate(template, attributes);
        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont("fonts/Intelligent Design - AvertaStdCY-RegularItalic.otf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        renderer.getFontResolver().addFont("fonts/OpenSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

        String baseUrl = FileSystems
                .getDefault()
                .getPath("src", "main", "resources")
                .toUri()
                .toURL()
                .toString();
        log.info("baseUrl: " + baseUrl);
        renderer.setDocumentFromString(convertToXhtml(renderedHtmlContent), baseUrl);
        renderer.layout();

        // And finally, we create the PDF:
//        OutputStream outputStream = new FileOutputStream(OUTPUT_FILE);
//        renderer.createPDF(outputStream);
//        outputStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

//        ITextRenderer renderer = new ITextRenderer();
//        renderer.getFontResolver().addFont("fonts/Intelligent Design - AvertaStdCY-RegularItalic.otf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

//        renderer.setDocumentFromString(html);
//        renderer.layout();
        renderer.createPDF(outputStream);
        byte[] pdfAsBytes = outputStream.toByteArray();
        outputStream.close();
        return pdfAsBytes;
    }

    public static String parseThymeleafTemplate(String template, Map<String, Object> attributes) throws UnsupportedEncodingException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setCharacterEncoding(UTF_8);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("data", attributes);
//        log.info("attributes.toString()" + attributes.toString());
        return templateEngine.process(template, context);
    }

    public static String convertToXhtml(String html) throws UnsupportedEncodingException {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(UTF_8);
        tidy.setOutputEncoding(UTF_8);
        tidy.setXHTML(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString(UTF_8);
    }

    public Map<String, Object> getAttributesFromString(String attributes){
        Map<String, Object> attributeMap = new HashMap<>();
        try {
            attributeMap = objectMapper.readValue(attributes, Map.class);
        } catch (JsonProcessingException e) {
            log.error("getAttributesFromString: " + e.getMessage());
        }

        return attributeMap;
    }


}