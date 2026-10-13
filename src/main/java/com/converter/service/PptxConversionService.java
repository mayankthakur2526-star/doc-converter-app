package com.converter.service;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PptxConversionService {

    private static final Logger log = LoggerFactory.getLogger(PptxConversionService.class);

    // Standard 16:9 presentation dimensions in points
    private static final int SLIDE_WIDTH = 960;
    private static final int SLIDE_HEIGHT = 540;

    public byte[] convertTextToPptx(String text, String title) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.setPageSize(new Dimension(SLIDE_WIDTH, SLIDE_HEIGHT));

            String[] rawLines = (text != null ? text : "").split("\\R");

            // We can break text into multiple slides if long (e.g. up to 10 lines per slide)
            int linesPerSlide = 10;
            int totalLines = rawLines.length;
            int slideCount = Math.max(1, (int) Math.ceil((double) totalLines / linesPerSlide));

            for (int s = 0; s < slideCount; s++) {
                XSLFSlide slide = ppt.createSlide();

                int startLine = s * linesPerSlide;
                int endLine = Math.min(startLine + linesPerSlide, totalLines);

                double topOffset = 40.0;

                // Title banner (if first slide or title present)
                if (title != null && !title.trim().isEmpty()) {
                    XSLFTextBox titleBox = slide.createTextBox();
                    titleBox.setAnchor(new Rectangle2D.Double(60, topOffset, SLIDE_WIDTH - 120, 50));

                    XSLFTextParagraph tp = titleBox.addNewTextParagraph();
                    XSLFTextRun tr = tp.addNewTextRun();
                    String slideTitle = title.trim() + (slideCount > 1 ? " (" + (s + 1) + "/" + slideCount + ")" : "");
                    tr.setText(slideTitle);
                    tr.setFontSize(24.0);
                    tr.setBold(true);
                    tr.setFontColor(new Color(15, 23, 42)); // Slate-900
                    topOffset += 60.0;
                } else if (slideCount > 1) {
                    XSLFTextBox titleBox = slide.createTextBox();
                    titleBox.setAnchor(new Rectangle2D.Double(60, topOffset, SLIDE_WIDTH - 120, 40));

                    XSLFTextParagraph tp = titleBox.addNewTextParagraph();
                    XSLFTextRun tr = tp.addNewTextRun();
                    tr.setText("Slide " + (s + 1));
                    tr.setFontSize(20.0);
                    tr.setBold(true);
                    tr.setFontColor(new Color(71, 85, 105)); // Slate-600
                    topOffset += 50.0;
                }

                // Content box
                XSLFTextBox contentBox = slide.createTextBox();
                double contentHeight = SLIDE_HEIGHT - topOffset - 40;
                contentBox.setAnchor(new Rectangle2D.Double(60, topOffset, SLIDE_WIDTH - 120, contentHeight));

                for (int i = startLine; i < endLine; i++) {
                    String line = rawLines[i];
                    XSLFTextParagraph p = contentBox.addNewTextParagraph();
                    p.setSpaceAfter(8.0);

                    XSLFTextRun r = p.addNewTextRun();
                    r.setText(line.isEmpty() ? " " : line);
                    r.setFontSize(16.0);
                    r.setFontColor(new Color(51, 65, 85)); // Slate-700
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ppt.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] convertImageToPptx(byte[] imageBytes, String originalFilename, String mimeType) throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            ppt.setPageSize(new Dimension(SLIDE_WIDTH, SLIDE_HEIGHT));
            XSLFSlide slide = ppt.createSlide();

            PictureData.PictureType picType = resolvePptxPictureType(mimeType, originalFilename);
            XSLFPictureData picData = ppt.addPicture(imageBytes, picType);

            // Read image dimensions
            BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(imageBytes));
            double origW = (bimg != null) ? bimg.getWidth() : 800;
            double origH = (bimg != null) ? bimg.getHeight() : 500;

            // Maximum bounds on slide
            double maxW = SLIDE_WIDTH - 80.0;
            double maxH = SLIDE_HEIGHT - 60.0;

            double scale = Math.min(maxW / origW, maxH / origH);
            if (scale > 1.0) {
                scale = 1.0;
            }

            double drawW = origW * scale;
            double drawH = origH * scale;

            double x = (SLIDE_WIDTH - drawW) / 2.0;
            double y = (SLIDE_HEIGHT - drawH) / 2.0;

            XSLFPictureShape shape = slide.createPicture(picData);
            shape.setAnchor(new Rectangle2D.Double(x, y, drawW, drawH));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ppt.write(baos);
            return baos.toByteArray();
        }
    }

    private PictureData.PictureType resolvePptxPictureType(String mimeType, String filename) {
        String lowerName = (filename != null) ? filename.toLowerCase() : "";
        String lowerMime = (mimeType != null) ? mimeType.toLowerCase() : "";

        if (lowerMime.contains("png") || lowerName.endsWith(".png")) {
            return PictureData.PictureType.PNG;
        } else if (lowerMime.contains("jpeg") || lowerMime.contains("jpg") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return PictureData.PictureType.JPEG;
        } else if (lowerMime.contains("gif") || lowerName.endsWith(".gif")) {
            return PictureData.PictureType.GIF;
        } else if (lowerMime.contains("bmp") || lowerName.endsWith(".bmp")) {
            return PictureData.PictureType.BMP;
        } else {
            return PictureData.PictureType.PNG;
        }
    }
}
