# Image Converter Workstation — Technical Project Report
**Universal Image & PDF Converter, Content Summarizer & Deep Analyzer**

---

## 1. Project Overview & Problem Statement

### 1.1 Context & Course Details
* **Course Title:** Core Java & Object-Oriented Engineering (Course Code: `CSE00130`)
* **Institution:** Department of Computer Science & Engineering, MIT-WPU, Pune
* **Target Audience:** 3rd-Year Computer Science Engineering Capstone Evaluation

### 1.2 Problem Statement
Users frequently need to transform visual assets across raster formats (PNG $\leftrightarrow$ JPG $\leftrightarrow$ BMP), bundle multiple pictures into portable printable documents (`.pdf`), inspect what files contain without opening slow third-party viewers, and extract embedded images back from documents without exposing private photos to untrusted third-party cloud tools (e.g., iLovePDF). 

**Image Converter** delivers an entirely offline, high-performance desktop workstation written in pure standard Java (JDK 8+) with zero external dependencies (no Apache POI, no iText, no Maven/Gradle required). It tracks conversion compression ratios, processes tasks asynchronously on background worker threads without freezing the Swing GUI, persists audit transactions via a standard JDBC persistence layer, and features an integrated **Content Summarizer & Deep Asset Analyzer**.

---

## 2. System Architecture & Package Hierarchy

Image Converter is structured into four distinct, loosely coupled packages adhering to standard object-oriented architectural patterns:

```
omni/
├── core/                  # Core domain models, contracts & custom exceptions
│   ├── Transformable.java         [Unit 2 / Unit 4: Functional Interface]
│   ├── Auditable.java             [Unit 2: Second Interface Contract]
│   ├── FileItem.java              [Unit 2: Abstract Base Class - Level 1]
│   ├── ConvertedItem.java         [Unit 2: Mid-level Inheritance - Level 2]
│   ├── ConversionRecord.java      [Unit 1/2: Leaf Record - Level 3]
│   ├── InvalidFormatException.java[Unit 3: Custom Checked Exception]
│   └── FileConversionException.java [Unit 3: Custom Checked Exception]
│
├── engine/                # Pipeline encoders, decoders, and background threads
│   ├── FormatValidator.java       [Unit 4: O(1) HashSet Validation]
│   ├── MetadataParser.java        [Unit 1/4: StringTokenizer, StringBuffer, HashMap]
│   ├── AnalyzerEngine.java        [Unit 1/3/4: Content Summarizer & Deep Analyzer]
│   ├── PdfGenerator.java          [Unit 3: Native %PDF-1.4 Byte Stream Generator]
│   ├── PdfExtractor.java          [Unit 3: Native PDF to Image Extractor]
│   ├── ReportGenerator.java       [Unit 3/5: Multi-page Academic PDF Generator]
│   └── ImageConverter.java        [Unit 3: Runnable Background Worker Thread]
│
├── storage/               # Relational Database Persistence Layer
│   └── ConversionDB.java          [Unit 5 / Lab 16: JDBC CRUD Engine]
│
└── ui/                    # Modern Swing Graphical User Interface
    ├── ConverterFrame.java        [Unit 3/4/5: Desktop Workstation GUI]
    └── PdfPreviewDialog.java      [In-App Visual US Letter Page Viewer]
```

---

## 3. Detailed Content Summarizer & Deep Asset Analyzer

The workstation features an integrated two-tier inspection engine that evaluates images and PDF documents (analyzing up to 10 pages at a time):

### 3.1 Detailed Image Analyzer & Content Summarizer
* **Content Summarizer ("What It Has"):**
  - **Visual Scene Classification:** Identifies whether the asset is a UI Screenshot, Technical Diagram, Natural Photography, Vector Graphic with Transparency, or Scanned Document.
  - **Layout & Structure:** Detects geometric panels, borders, UI controls, or organic photographic gradients.
  - **Color Palette & Atmosphere:** Extracts the primary color spectrum, average exposure, and dominant color hex swatches (`#RRGGBB`).
  - **Transparency & Canvas:** Reports alpha channel activity and transparent cutout areas.
  - **Sharpness & Edge Density:** Evaluates high-frequency contours and text-like transitions.
* **Deep Asset Diagnostics (Analyzer):**
  - Pixel dimensions ($W \times H$) and Total Megapixels ($MP$).
  - Aspect ratio and framing classification (16:9 widescreen, 4:3 standard, 1:1 square, 9:16 mobile portrait).
  - Bit depth (24-bit Truecolor, 32-bit ARGB with alpha transparency).
  - Exposure luminance ($0\text{--}255$), RMS dynamic contrast range, and color temperature.
  - File size on disk and compression density (bits per pixel).

### 3.2 Detailed PDF Analyzer & Content Summarizer
* **Content Summarizer ("What It Has"):**
  - **Document Scope & Type:** Total page count and evaluated sample (up to 10 pages).
  - **Primary Subject Matter:** Automatically discovers document title, headings, or topics.
  - **Discovered Headings & Sections:** Extracts chapters, modules, and units (e.g., `Unit 3: Streams`).
  - **Core Conceptual Themes:** Ranks high-value domain vocabulary while stripping stop-words (`the`, `is`, `at`, `which`).
  - **Visual Figure Streams:** Counts embedded diagrams, charts, and JPEG/lossless graphics.
  - **Reading Profile:** Extracted total word count, words per page, and estimated reading time ($W / 200 \text{ wpm}$).
* **Deep Document Diagnostics (Analyzer):**
  - PDF Specification version (`PDF 1.4 ISO-32000`).
  - Page geometry / paper size (`A4 (595 × 842 pt)` or `US Letter (612 × 792 pt)`).
  - Embedded font resources detected (`/BaseFont /Helvetica`, `/Times-Roman`, etc.).
  - Total word and character counts.
  - Metadata dictionary properties (Title, Author, Producer).

---

## 4. Supported Conversion Pipelines

1. **Image to Image (Bidirectional):**
   - Lossless PNG $\leftrightarrow$ Compressed JPG $\leftrightarrow$ Bitmap BMP.
   - Preserves aspect ratios and handles alpha-to-opaque background fills.
2. **Image(s) to PDF:**
   - Single Image $\rightarrow$ Native printable PDF 1.4 document.
   - Batch Images $\rightarrow$ Multi-page merged PDF document.
3. **PDF to Images:**
   - Extracts every visual page or embedded graphic stream from a PDF back into standalone PNG, JPG, or BMP files.

---

## 5. Course Syllabus Alignment (Units 1 to 5)

| Syllabus Unit | Core Java Concepts Implemented | Specific Source Files |
| :--- | :--- | :--- |
| **Unit 1: Java Basics & String Handling** | `StringTokenizer`, `StringBuffer`, character loops, vowel counting, immutable string formatting. | `MetadataParser.java`, `AnalyzerEngine.java` |
| **Unit 2: OOP & Interfaces** | Abstract classes (`FileItem`), 3-level class inheritance, multiple interface contracts (`Transformable`, `Auditable`), method overriding. | `FileItem.java`, `ConvertedItem.java`, `ConversionRecord.java`, `Transformable.java`, `Auditable.java` |
| **Unit 3: Exceptions & Multithreading** | Custom checked exceptions (`InvalidFormatException`, `FileConversionException`), exception chaining, `try-catch-finally`, `Runnable` background worker thread, `Thread.sleep` simulated stages, byte I/O streams (`InflaterInputStream`). | `InvalidFormatException.java`, `FileConversionException.java`, `ImageConverter.java`, `PdfGenerator.java`, `PdfExtractor.java` |
| **Unit 4: Collections & Generics** | `ArrayList<ConversionRecord>`, `HashMap<String, Integer>` keyword ranking and format counting, `HashSet<String>` $O(1)$ format validator, Functional Interfaces (`Predicate`, `Consumer`, `Supplier`), Lambda expressions. | `FormatValidator.java`, `AnalyzerEngine.java`, `ConverterFrame.java` |
| **Unit 5: Swing GUI & JDBC Persistence** | `JFrame`, `JSplitPane`, `JTabbedPane`, `GridBagLayout`, `BoxLayout`, `JTable`, `DefaultTableModel`, `JProgressBar`, `JEditorPane`, JDBC `Connection`, `PreparedStatement`, `ResultSet`, SQL CRUD transactions. | `ConverterFrame.java`, `PdfPreviewDialog.java`, `ConversionDB.java` |

---

## 6. Compilation & Execution Instructions

```bat
:: Compile the entire project
compile.bat

:: Launch the workstation GUI
run.bat

:: Or launch directly with Java
java -cp out omni.ui.ConverterFrame
```
