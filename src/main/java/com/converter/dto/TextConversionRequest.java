package com.converter.dto;

public class TextConversionRequest {
    private String text;
    private String format;
    private String title;

    public TextConversionRequest() {
    }

    public TextConversionRequest(String text, String format, String title) {
        this.text = text;
        this.format = format;
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
