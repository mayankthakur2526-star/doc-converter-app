package com.converter.dto;

public enum DocumentFormat {
    PDF("pdf", "application/pdf"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

    private final String extension;
    private final String mimeType;

    DocumentFormat(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static DocumentFormat fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Target format cannot be empty. Supported formats: PDF, DOCX, PPTX");
        }
        String normalized = value.trim().toUpperCase();
        for (DocumentFormat format : values()) {
            if (format.name().equals(normalized)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported format '" + value + "'. Supported formats are: PDF, DOCX, PPTX");
    }
}
