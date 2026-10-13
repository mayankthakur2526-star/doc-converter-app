package com.converter.service;

import com.converter.dto.ConvertedDocument;
import com.converter.dto.DocumentFormat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentConversionService {

    private final PdfConversionService pdfConversionService;
    private final DocxConversionService docxConversionService;
    private final PptxConversionService pptxConversionService;

    public DocumentConversionService(PdfConversionService pdfConversionService,
                                     DocxConversionService docxConversionService,
                                     PptxConversionService pptxConversionService) {
        this.pdfConversionService = pdfConversionService;
        this.docxConversionService = docxConversionService;
        this.pptxConversionService = pptxConversionService;
    }

    public ConvertedDocument convertText(String text, String formatStr, String customTitle) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text content cannot be empty.");
        }

        DocumentFormat format = DocumentFormat.fromString(formatStr);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String defaultTitle = (customTitle != null && !customTitle.isBlank()) ? customTitle.trim() : "Converted Document";
        String baseName = sanitizeFilename(defaultTitle).replaceAll("\\s+", "_");
        if (baseName.isEmpty()) {
            baseName = "document_" + timestamp;
        } else {
            baseName = baseName + "_" + timestamp;
        }

        byte[] bytes;
        switch (format) {
            case PDF -> bytes = pdfConversionService.convertTextToPdf(text, customTitle);
            case DOCX -> bytes = docxConversionService.convertTextToDocx(text, customTitle);
            case PPTX -> bytes = pptxConversionService.convertTextToPptx(text, customTitle);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }

        String filename = baseName + "." + format.getExtension();
        return new ConvertedDocument(bytes, filename, format.getMimeType());
    }

    public ConvertedDocument convertImage(MultipartFile file, String formatStr) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a valid, non-empty image file.");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (!isValidImage(contentType, originalFilename)) {
            throw new IllegalArgumentException("Invalid file type. Please upload an image file (PNG, JPG, JPEG, WEBP, GIF, BMP).");
        }

        DocumentFormat format = DocumentFormat.fromString(formatStr);
        byte[] imageBytes = file.getBytes();

        String rawBaseName = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
                : "image";
        String safeBaseName = sanitizeFilename(rawBaseName).replaceAll("\\s+", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseName = (safeBaseName.isEmpty() ? "image" : safeBaseName) + "_" + timestamp;

        byte[] bytes;
        switch (format) {
            case PDF -> bytes = pdfConversionService.convertImageToPdf(imageBytes, originalFilename);
            case DOCX -> bytes = docxConversionService.convertImageToDocx(imageBytes, originalFilename, contentType);
            case PPTX -> bytes = pptxConversionService.convertImageToPptx(imageBytes, originalFilename, contentType);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }

        String filename = baseName + "." + format.getExtension();
        return new ConvertedDocument(bytes, filename, format.getMimeType());
    }

    private boolean isValidImage(String contentType, String filename) {
        if (contentType != null) {
            String mime = contentType.toLowerCase();
            if (mime.startsWith("image/")) {
                return true;
            }
        }
        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp");
        }
        return false;
    }

    private String sanitizeFilename(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
