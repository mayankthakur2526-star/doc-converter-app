package com.converter.service;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class DocxConversionService {

    private static final Logger log = LoggerFactory.getLogger(DocxConversionService.class);

    public byte[] convertTextToDocx(String text, String title) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {

            // Title paragraph
            if (title != null && !title.trim().isEmpty()) {
                XWPFParagraph titleParagraph = document.createParagraph();
                titleParagraph.setAlignment(ParagraphAlignment.CENTER);
                titleParagraph.setSpacingAfter(240); // 12pt

                XWPFRun titleRun = titleParagraph.createRun();
                titleRun.setText(title.trim());
                titleRun.setBold(true);
                titleRun.setFontSize(20);
                titleRun.setFontFamily("Segoe UI");
                titleRun.setColor("1E293B"); // Slate-800
            }

            // Body paragraphs
            String[] rawParagraphs = (text != null ? text : "").split("\\R");
            for (String rawPara : rawParagraphs) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setSpacingAfter(120); // 6pt spacing
                paragraph.setSpacingLineRule(org.apache.poi.xwpf.usermodel.LineSpacingRule.AUTO);

                XWPFRun run = paragraph.createRun();
                run.setText(rawPara);
                run.setFontSize(11);
                run.setFontFamily("Calibri");
                run.setColor("334155"); // Slate-700
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] convertImageToDocx(byte[] imageBytes, String originalFilename, String mimeType) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            paragraph.setSpacingBefore(120);
            paragraph.setSpacingAfter(120);

            XWPFRun run = paragraph.createRun();

            int pictureType = resolvePoiPictureType(mimeType, originalFilename);

            // Read image dimensions
            BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(imageBytes));
            int origWidth = (bimg != null) ? bimg.getWidth() : 600;
            int origHeight = (bimg != null) ? bimg.getHeight() : 400;

            // Maximum width in points (standard A4 page text width is ~468 pt)
            double maxPointsW = 468.0;
            double maxPointsH = 650.0;

            double scale = Math.min(maxPointsW / origWidth, maxPointsH / origHeight);
            if (scale > 1.0) {
                scale = 1.0;
            }

            int finalWidthPt = (int) Math.round(origWidth * scale);
            int finalHeightPt = (int) Math.round(origHeight * scale);

            String safeFileName = (originalFilename != null && !originalFilename.isBlank()) ? originalFilename : "image";
            try (ByteArrayInputStream is = new ByteArrayInputStream(imageBytes)) {
                run.addPicture(is, pictureType, safeFileName, Units.toEMU(finalWidthPt), Units.toEMU(finalHeightPt));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();
        }
    }

    private int resolvePoiPictureType(String mimeType, String filename) {
        String lowerName = (filename != null) ? filename.toLowerCase() : "";
        String lowerMime = (mimeType != null) ? mimeType.toLowerCase() : "";

        if (lowerMime.contains("png") || lowerName.endsWith(".png")) {
            return Document.PICTURE_TYPE_PNG;
        } else if (lowerMime.contains("jpeg") || lowerMime.contains("jpg") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return Document.PICTURE_TYPE_JPEG;
        } else if (lowerMime.contains("gif") || lowerName.endsWith(".gif")) {
            return Document.PICTURE_TYPE_GIF;
        } else if (lowerMime.contains("bmp") || lowerName.endsWith(".bmp")) {
            return Document.PICTURE_TYPE_BMP;
        } else {
            // Default to PNG
            return Document.PICTURE_TYPE_PNG;
        }
    }
}
