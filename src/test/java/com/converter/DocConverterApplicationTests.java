package com.converter;

import com.converter.dto.ConvertedDocument;
import com.converter.service.DocumentConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocConverterApplicationTests {

    @Autowired
    private DocumentConversionService conversionService;

    @Test
    void contextLoads() {
        assertNotNull(conversionService);
    }

    @Test
    void testTextToPdf() throws Exception {
        String sampleText = "Hello World!\nThis is a test of PDF conversion with multiple lines.\nDocMorph generates clean PDF files.";
        ConvertedDocument doc = conversionService.convertText(sampleText, "PDF", "Test Document");

        assertNotNull(doc);
        assertNotNull(doc.getContent());
        assertTrue(doc.getContent().length > 0);
        assertTrue(doc.getFilename().endsWith(".pdf"));
        assertEquals("application/pdf", doc.getContentType());
    }

    @Test
    void testTextToDocx() throws Exception {
        String sampleText = "Hello World!\nThis is a test of DOCX conversion with multiple paragraphs.";
        ConvertedDocument doc = conversionService.convertText(sampleText, "DOCX", "Test Report");

        assertNotNull(doc);
        assertNotNull(doc.getContent());
        assertTrue(doc.getContent().length > 0);
        assertTrue(doc.getFilename().endsWith(".docx"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", doc.getContentType());
    }

    @Test
    void testTextToPptx() throws Exception {
        String sampleText = "Slide line 1\nSlide line 2\nSlide line 3";
        ConvertedDocument doc = conversionService.convertText(sampleText, "PPTX", "Sample Presentation");

        assertNotNull(doc);
        assertNotNull(doc.getContent());
        assertTrue(doc.getContent().length > 0);
        assertTrue(doc.getFilename().endsWith(".pptx"));
        assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation", doc.getContentType());
    }

    @Test
    void testImageToPdf() throws Exception {
        byte[] testPng = createSamplePngImage(200, 150);
        MockMultipartFile file = new MockMultipartFile("file", "chart.png", "image/png", testPng);

        ConvertedDocument doc = conversionService.convertImage(file, "PDF");
        assertNotNull(doc);
        assertTrue(doc.getContent().length > 0);
        assertTrue(doc.getFilename().endsWith(".pdf"));
    }

    @Test
    void testImageToDocx() throws Exception {
        byte[] testPng = createSamplePngImage(200, 150);
        MockMultipartFile file = new MockMultipartFile("file", "banner.png", "image/png", testPng);

        ConvertedDocument doc = conversionService.convertImage(file, "DOCX");
        assertNotNull(doc);
        assertTrue(doc.getContent().length > 0);
        assertTrue(doc.getFilename().endsWith(".docx"));
    }

    @Test
    void testImageToPptx() throws Exception {
        byte[] testPng = createSamplePngImage(200, 150);
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", testPng);

        ConvertedDocument doc = conversionService.convertImage(file, "PPTX");
        assertNotNull(doc);
        assertTrue(doc.getContent().length > 0);
        assertTrue(doc.getFilename().endsWith(".pptx"));
    }

    @Test
    void testEmptyTextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertText("   ", "PDF", "Empty Title");
        });
    }

    @Test
    void testInvalidFormatThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertText("Valid text", "TXT", "Title");
        });
    }

    private byte[] createSamplePngImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(99, 102, 241));
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawString("DocMorph Test", 20, height / 2);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}
