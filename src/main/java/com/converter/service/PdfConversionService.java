package com.converter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfConversionService {

    private static final Logger log = LoggerFactory.getLogger(PdfConversionService.class);

    private static final float MARGIN = 50f;
    private static final float FONT_SIZE = 11f;
    private static final float TITLE_FONT_SIZE = 18f;
    private static final float LEADING = 15f;
    private static final float TITLE_LEADING = 24f;

    public byte[] convertTextToPdf(String text, String title) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float contentWidth = pageWidth - (2 * MARGIN);
            float currentY = pageHeight - MARGIN;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Draw Title if provided
            if (title != null && !title.trim().isEmpty()) {
                String safeTitle = sanitizeForWinAnsi(title.trim());
                contentStream.beginText();
                contentStream.setFont(boldFont, TITLE_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, currentY);
                contentStream.showText(safeTitle);
                contentStream.endText();
                currentY -= (TITLE_LEADING + 10f);
            }

            // Process text content
            String sanitizedText = sanitizeForWinAnsi(text != null ? text : "");
            String[] rawParagraphs = sanitizedText.split("\\R");

            for (String rawPara : rawParagraphs) {
                if (rawPara.trim().isEmpty()) {
                    currentY -= (LEADING * 0.7f);
                    if (currentY < MARGIN) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = pageHeight - MARGIN;
                    }
                    continue;
                }

                List<String> wrappedLines = wrapText(rawPara, regularFont, FONT_SIZE, contentWidth);
                for (String line : wrappedLines) {
                    if (currentY < MARGIN + LEADING) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        currentY = pageHeight - MARGIN;
                    }

                    contentStream.beginText();
                    contentStream.setFont(regularFont, FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN, currentY);
                    contentStream.showText(line);
                    contentStream.endText();

                    currentY -= LEADING;
                }
                currentY -= 4f; // Paragraph spacing
            }

            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public byte[] convertImageToPdf(byte[] imageBytes, String originalFilename) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, imageBytes, originalFilename);

            float imgWidth = pdImage.getWidth();
            float imgHeight = pdImage.getHeight();

            // Create A4 page
            PDRectangle pageSize = PDRectangle.A4;
            // If image is landscape oriented, use landscape page
            if (imgWidth > imgHeight) {
                pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            }

            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();

            float maxWidth = pageWidth - (2 * MARGIN);
            float maxHeight = pageHeight - (2 * MARGIN);

            // Scale to fit while maintaining aspect ratio
            float scale = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);
            if (scale > 1.0f) {
                scale = 1.0f; // Maintain original size if smaller than page
            }

            float drawWidth = imgWidth * scale;
            float drawHeight = imgHeight * scale;

            // Center image on page
            float x = (pageWidth - drawWidth) / 2.0f;
            float y = (pageHeight - drawHeight) / 2.0f;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, x, y, drawWidth, drawHeight);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            float lineWidth = (font.getStringWidth(candidate) / 1000f) * fontSize;

            if (lineWidth <= maxWidth) {
                currentLine = new StringBuilder(candidate);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word itself is longer than maxWidth, break word
                    lines.add(word);
                    currentLine = new StringBuilder();
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Standard PDF Type1 fonts only support WinAnsiEncoding. Replace unsupported Unicode characters.
     */
    private String sanitizeForWinAnsi(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '\t') {
                sb.append("    ");
            } else if (c == '\n' || c == '\r' || (c >= 32 && c <= 126) || (c >= 160 && c <= 255)) {
                sb.append(c);
            } else if (c == '’' || c == '‘') {
                sb.append('\'');
            } else if (c == '“' || c == '”') {
                sb.append('"');
            } else if (c == '—' || c == '–') {
                sb.append('-');
            } else if (c == '…') {
                sb.append("...");
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
