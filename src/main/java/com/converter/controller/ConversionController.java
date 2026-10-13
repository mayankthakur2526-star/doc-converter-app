package com.converter.controller;

import com.converter.dto.ConvertedDocument;
import com.converter.dto.TextConversionRequest;
import com.converter.service.DocumentConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/convert")
public class ConversionController {

    private static final Logger log = LoggerFactory.getLogger(ConversionController.class);

    private final DocumentConversionService conversionService;

    public ConversionController(DocumentConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Converts raw text into PDF, DOCX, or PPTX.
     * Supports both JSON payload and multipart/form-data / form-urlencoded parameters.
     */
    @PostMapping(value = "/text", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<byte[]> convertTextJson(@RequestBody TextConversionRequest request) throws Exception {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null.");
        }
        return processTextConversion(request.getText(), request.getFormat(), request.getTitle());
    }

    @PostMapping(value = "/text", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<byte[]> convertTextForm(
            @RequestParam("text") String text,
            @RequestParam("format") String format,
            @RequestParam(value = "title", required = false) String title) throws Exception {
        return processTextConversion(text, format, title);
    }

    private ResponseEntity<byte[]> processTextConversion(String text, String format, String title) throws Exception {
        log.info("Converting text to format: {}, title: {}", format, title);
        ConvertedDocument document = conversionService.convertText(text, format, title);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getContentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(document.getFilename())
                        .build()
        );
        headers.setContentLength(document.getContent().length);
        headers.add("X-Document-Name", document.getFilename());
        headers.add("Access-Control-Expose-Headers", "Content-Disposition, X-Document-Name");

        return ResponseEntity.ok()
                .headers(headers)
                .body(document.getContent());
    }

    /**
     * Converts an uploaded image file into a document embedding that image (PDF, DOCX, or PPTX).
     */
    @PostMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<byte[]> convertImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format) throws Exception {
        log.info("Converting image '{}' ({}) to format: {}", 
                file.getOriginalFilename(), file.getContentType(), format);
        
        ConvertedDocument document = conversionService.convertImage(file, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getContentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(document.getFilename())
                        .build()
        );
        headers.setContentLength(document.getContent().length);
        headers.add("X-Document-Name", document.getFilename());
        headers.add("Access-Control-Expose-Headers", "Content-Disposition, X-Document-Name");

        return ResponseEntity.ok()
                .headers(headers)
                .body(document.getContent());
    }
}
