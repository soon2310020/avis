package vn.com.twendie.avis.api.service;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.StringReader;

public abstract class ExportPdfService {

    private String xhtml;

    public abstract void buildXhtml(String xhtml);

    public void setXhtml(String xhtml) {
        this.xhtml = xhtml;
    }

    public void export(OutputStream outputStream, int width, int height) throws Exception {
        build(outputStream, width, height, null);
    }

    public void export(OutputStream outputStream, int width, int height, String pathToImage) throws Exception {
        build(outputStream, width, height, pathToImage);
    }

    private void build(OutputStream outputStream, int width, int height, String pathImage) throws Exception {
        Document document = convertHtmlToDocument(xhtml);
        float MARGIN = 19.05f; // 0.75 inch

//         Java2DRenderer.NO_HEIGHT = -1 (it's private)
        // if used the required height is computed
        Java2DRenderer renderer = new Java2DRenderer(document, width, height);
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        renderer.setRenderingHints(hints);

        BufferedImage img = renderer.getImage();

        // Adjusts height based on required width
        int widthImg = img.getWidth();
        int heightImg = (int) Math.round(widthImg * 1191.0F / 842.0F); // A3

        int page = img.getHeight() / heightImg;
        if (img.getHeight() % heightImg != 0) {
            page++;
        }
        renderer = new Java2DRenderer(document, img.getWidth(), heightImg * page);
        img = renderer.getImage();
        if (pathImage != null) {
            img = addLogo(img, pathImage);
        }
        com.lowagie.text.Document pdf = new com.lowagie.text.Document();

        pdf.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
        PdfWriter.getInstance(pdf, outputStream);
        pdf.open();

        int heightSplit = img.getHeight() / page;


        for(int i = 0; i < page; i++) {
            com.lowagie.text.Image pdfImage = com.lowagie.text.Image.getInstance(img.getSubimage(0, heightSplit * i, img.getWidth(), img.getHeight() / page), Color.WHITE);
            Rectangle ps = pdf.getPageSize();
            pdfImage.scaleAbsolute(ps.getWidth() - pdf.leftMargin() - pdf.rightMargin(), ps.getHeight() - pdf.topMargin() - pdf.bottomMargin());
            pdf.add(pdfImage);
            pdf.newPage();
        }

        pdf.close();

    }

    private BufferedImage addLogo(BufferedImage image, String pathToLogo) throws Exception {
        BufferedImage results = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        BufferedImage logo = ImageIO.read(new ClassPathResource(pathToLogo).getInputStream());
        Graphics2D graphics = results.createGraphics();
        graphics.drawImage(image, null, 0, 0);
        graphics.drawImage(logo, null, 0, 0);
        graphics.dispose();
        return results;
    }


    private org.w3c.dom.Document convertHtmlToDocument(String xhtml) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xhtml));
        return db.parse(is);
    }

}
