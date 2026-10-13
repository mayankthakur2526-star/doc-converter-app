# DocMorph - Universal Document Converter

DocMorph is a full-stack Java web application built with **Spring Boot 3.3**, **Apache PDFBox 3.0**, **Apache POI 5.3**, and a modern **Tailwind CSS** Single Page Application (SPA) frontend.

It converts typed/pasted text and uploaded images into standard document formats:
- **PDF** (`.pdf`) via Apache PDFBox
- **Word Document** (`.docx`) via Apache POI OOXML
- **PowerPoint Presentation** (`.pptx`) via Apache POI OOXML

All conversions are processed **purely in-memory** (`ByteArrayOutputStream`), ensuring high performance and zero ephemeral file leaks on disk.

---

## Features

### 1. Text to Document
- **Rich Editor**: Type or paste plain text with multi-line paragraphs and bullet notes.
- **Optional Header/Title**: Custom title formatted as an executive heading in generated files.
- **Intelligent Formatting**:
  - **PDF Engine**: Automatic word-wrapping algorithm and page-break calculations across multiple A4 pages.
  - **Word (DOCX) Engine**: Paragraph spacing, custom typography, and styled title runs.
  - **PowerPoint (PPTX) Engine**: Automatic slide chunking across 16:9 widescreen slides.
- **Productivity Tools**: Live word & character counters, "Load Sample Text" prefill, and `Ctrl + Enter` shortcut.

### 2. Image to Document
- **Drag-and-Drop Dropzone**: Drag files directly into the browser or browse via file picker.
- **Accepted Formats**: PNG, JPG, JPEG, WEBP, GIF, BMP (up to 25MB).
- **Instant Image Preview**: Displays thumbnail, natural dimensions (`W × H px`), and file size before conversion.
- **Proportional Scaling**: Automatically fits images into printable page/slide dimensions while preserving aspect ratio.

### 3. Error Handling & Feedback
- Backend validation with structured JSON diagnostics (`GlobalExceptionHandler`).
- Dismissible in-panel alerts and floating toast notifications on the frontend.
- Live session history tracking recent document conversions.

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend Framework** | Spring Boot 3.3.4 (Java 17 or higher) |
| **PDF Generation** | Apache PDFBox 3.0.3 |
| **DOCX & PPTX Generation** | Apache POI & POI-OOXML 5.3.0 |
| **Templating** | Thymeleaf (Single Page Application container) |
| **Frontend Styling** | Tailwind CSS (CDN) + Custom Glassmorphism CSS |
| **Frontend Logic** | Vanilla JavaScript (Fetch API, Blob Stream handling, Drag & Drop) |
| **Build Tool** | Apache Maven 3.8+ |

---

## Project Structure

```text
doc-converter-app/
├── pom.xml                                  # Maven dependencies and build configuration
├── run.bat                                  # Windows CMD quick-launch script
├── run.ps1                                  # Windows PowerShell quick-launch script
├── src/
│   ├── main/
│   │   ├── java/com/converter/
│   │   │   ├── DocConverterApplication.java # Spring Boot main entrypoint
│   │   │   ├── controller/
│   │   │   │   ├── ConversionController.java# REST endpoints for text and image conversion
│   │   │   │   └── WebController.java       # Controller serving index.html
│   │   │   ├── dto/
│   │   │   │   ├── ConvertedDocument.java   # Wrapper for byte payload, filename, & MIME
│   │   │   │   ├── DocumentFormat.java      # Enum (PDF, DOCX, PPTX)
│   │   │   │   ├── ErrorResponse.java       # Structured error DTO
│   │   │   │   └── TextConversionRequest.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java# RestControllerAdvice for 400, 413, 500 errors
│   │   │   └── service/
│   │   │       ├── DocumentConversionService.java # Unified conversion coordinator
│   │   │       ├── PdfConversionService.java      # Apache PDFBox logic
│   │   │       ├── DocxConversionService.java     # Apache POI Word logic
│   │   │       └── PptxConversionService.java     # Apache POI PowerPoint logic
│   │   └── resources/
│   │       ├── application.properties       # Port (8085) and multipart upload settings
│   │       ├── static/
│   │       │   ├── css/style.css            # Dark theme, glassmorphism, animations
│   │       │   └── js/app.js                # UI logic, state, drag & drop, download handling
│   │       └── templates/
│   │           └── index.html               # Thymeleaf single page interface
│   └── test/
│       └── java/com/converter/
│           └── DocConverterApplicationTests.java # 9 automated unit/integration tests
```

---

## Getting Started

### Prerequisites
- **Java SE Development Kit**: Java 17 or higher (`java -version`)
- **Apache Maven**: Version 3.8 or higher (`mvn -version`)

### 1. Clone or Open the Project
```bash
cd doc-converter-app
```

### 2. Run the Application

#### Option A: Using Helper Scripts (Windows)
Double-click `run.bat` or run:
```powershell
.\run.ps1
```

#### Option B: Using Maven
```bash
mvn spring-boot:run
```

#### Option C: Build and Run JAR
```bash
mvn clean package -DskipTests
java -jar target/doc-converter-app-1.0.0.jar
```

### 3. Open in Browser
Navigate to:
```
http://localhost:8085
```
*(Default configured port is `8085` in `application.properties` to avoid conflict with standard services).*

---

## REST API Reference

### 1. Convert Text to Document
**Endpoint**: `POST /api/convert/text`  
**Consumes**: `application/json` or `application/x-www-form-urlencoded` / `multipart/form-data`  
**Produces**: Downloadable binary stream with `Content-Disposition: attachment`

#### JSON Request Example:
```bash
curl -X POST "http://localhost:8085/api/convert/text" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Quarterly Report",
    "text": "Executive highlights and achievements.\n- Item 1\n- Item 2",
    "format": "PDF"
  }' \
  --output report.pdf
```

Supported formats: `PDF`, `DOCX`, `PPTX` (case-insensitive).

---

### 2. Convert Image to Document
**Endpoint**: `POST /api/convert/image`  
**Consumes**: `multipart/form-data`  
**Produces**: Downloadable binary stream with `Content-Disposition: attachment`

#### Multipart Form Request Example:
```bash
curl -X POST "http://localhost:8085/api/convert/image" \
  -F "file=@chart.png" \
  -F "format=DOCX" \
  --output chart_document.docx
```

Supported formats: `PDF`, `DOCX`, `PPTX`.

---

## Running Automated Tests

To execute the test suite (verifying all document converters and validation behaviors):

```bash
mvn test
```

All 9 integration and unit tests will run:
- Spring Boot Application Context loading
- Text to PDF conversion
- Text to DOCX conversion
- Text to PPTX conversion
- Image to PDF embedding
- Image to DOCX embedding
- Image to PPTX embedding
- Empty text validation (HTTP 400 check)
- Invalid format rejection (HTTP 400 check)

---

## License

This project is licensed under the Apache License 2.0.
