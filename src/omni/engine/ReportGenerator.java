package omni.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* ═══════════════════════════════════════════════════════════════════════════
 *  OmniFormat Report Generator
 *  Generates an official, human-readable, multi-page technical report in
 *  standard PDF format using native Java AWT & PdfGenerator.
 * ═══════════════════════════════════════════════════════════════════════════ */
public class ReportGenerator {

    private static final int PAGE_W = 1224; // 150 DPI US Letter Width
    private static final int PAGE_H = 1584; // 150 DPI US Letter Height

    private static final Color COLOR_PRIMARY = new Color(20, 30, 45);
    private static final Color COLOR_ACCENT  = new Color(0, 150, 136);
    private static final Color COLOR_TEXT    = new Color(30, 35, 45);
    private static final Color COLOR_MUTED   = new Color(100, 110, 125);
    private static final Color COLOR_CARD    = new Color(245, 247, 250);
    private static final Color COLOR_BORDER  = new Color(215, 222, 232);

    public static void generateReportPdf(String outputPath) throws Exception {
        List<BufferedImage> pages = new ArrayList<>();

        pages.add(createPage1());
        pages.add(createPage2());
        pages.add(createPage3());
        pages.add(createPage4());
        pages.add(createPage5());

        PdfGenerator.generateMultiPagePdf(pages, outputPath);
    }

    /* ── PAGE 1: Title, Executive Summary & Problem Statement ─────────── */
    private static BufferedImage createPage1() {
        BufferedImage img = newPage();
        Graphics2D g = setupGraphics(img);

        drawHeader(g, 1, 5, "Executive Summary & Problem Statement");

        int y = 140;

        // Title Box
        g.setColor(COLOR_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g.drawString("Image Converter Workstation", 80, y);
        y += 35;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g.setColor(COLOR_ACCENT);
        g.drawString("Universal Image & PDF Converter, Content Summarizer & Deep Analyzer", 80, y);
        y += 30;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.setColor(COLOR_MUTED);
        g.drawString("Course: Core Java (CSE00130) \u2022 3rd-Year B.Tech CSE \u2022 MIT-WPU Pune", 80, y);
        y += 45;

        // Section: Problem Statement in Human Words
        drawCardHeader(g, 80, y, 1064, "1. What Problem Does This Project Solve? (In Simple Words)");
        y += 40;
        String[] problemText = {
            "Students and professionals often need to change image formats (like PNG to JPG or BMP), combine",
            "multiple pictures into a single printable PDF, or inspect and extract pages from existing PDF files.",
            "Popular online websites like iLovePDF force users to upload their personal or sensitive photos to cloud servers,",
            "which risks privacy, requires internet access, and often adds watermarks.",
            "",
            "Image Converter solves this by providing a 100% offline desktop tool that runs entirely on your own computer.",
            "It converts images in both directions, generates real ISO PDF files without any external libraries,",
            "tracks compression efficiency, and provides a rich Content Summarizer and Deep Asset Analyzer."
        };
        y = drawParagraph(g, 95, y, problemText);
        y += 30;

        // Section: Key Capabilities
        drawCardHeader(g, 80, y, 1064, "2. Key Features & Bidirectional Capabilities");
        y += 40;
        String[] features = {
            "\u2022 Image to Image: Converts seamlessly between PNG, JPG, and BMP with automatic alpha-channel handling.",
            "\u2022 Image to PDF: Builds native ISO PDF 1.4 files from scratch via raw byte streams (no external libraries needed).",
            "\u2022 PDF to Images: Reads any PDF and extracts visual pages directly back into standalone PNG/JPG files.",
            "\u2022 Asset Analyzer Engine: Deep structural diagnostic inspection of pixel matrices, color depth, and PDF streams.",
            "\u2022 Natural Content Summarizer: Evaluates 'What It Has' in human words, extracting scene types and document subjects.",
            "\u2022 Live Console & Diagnostics: Real-time execution logs with vowel counters and StringBuffer status cards.",
            "\u2022 JDBC Persistence: Logs every conversion with its efficiency score into a database using PreparedStatements."
        };
        y = drawBulletPoints(g, 95, y, features);

        drawFooter(g, 1, 5);
        g.dispose();
        return img;
    }

    /* ── PAGE 2: File-by-File Walkthrough ──────────────────────────────── */
    private static BufferedImage createPage2() {
        BufferedImage img = newPage();
        Graphics2D g = setupGraphics(img);

        drawHeader(g, 2, 5, "Codebase Architecture & File-by-File Guide");
        int y = 140;

        drawCardHeader(g, 80, y, 1064, "3. What Each Code File Does (Plain English Walkthrough)");
        y += 40;

        String[][] files = {
            {"Transformable.java", "omni.core", "Functional interface with transform() defining the contract for converters."},
            {"Auditable.java", "omni.core", "Interface with auditLog() ensuring every transaction produces an audit string."},
            {"FileItem.java", "omni.core", "Abstract base class (Level 1 inheritance) storing fileName and fileSizeBytes."},
            {"ConvertedItem.java", "omni.core", "Intermediate class (Level 2) extending FileItem; adds source & target formats."},
            {"ConversionRecord.java", "omni.core", "Leaf class (Level 3) implementing both interfaces; has 2 overloaded constructors."},
            {"InvalidFormatException.java", "omni.core", "Custom checked exception thrown if a user passes an unsupported file extension."},
            {"FileConversionException.java", "omni.core", "Custom checked exception thrown when file decoding, reading, or writing fails."},
            {"FormatValidator.java", "omni.engine", "Uses a HashSet to perform instant O(1) format checks for PNG, JPG, BMP, PDF."},
            {"MetadataParser.java", "omni.engine", "Uses StringTokenizer for file info, counts vowels, and builds status cards."},
            {"AnalyzerEngine.java", "omni.engine", "Deep Content Summarizer & Asset Analyzer for images & up to 10 PDF pages."},
            {"PdfGenerator.java", "omni.engine", "Writes raw PDF 1.4 byte streams (Catalog, Pages, xref table) without 3rd-party libraries."},
            {"PdfExtractor.java", "omni.engine", "Pulls visual pages and figures out of PDF files and turns them into image files."},
            {"ReportGenerator.java", "omni.engine", "Programmatic technical report builder rendering multi-page academic PDF documentation."},
            {"ImageConverter.java", "omni.engine", "Background worker thread (Runnable) that runs conversions without freezing the UI."},
            {"ConversionDB.java", "omni.storage", "Embedded JDBC database managing CRUD logs using standard PreparedStatement."},
            {"ConverterFrame.java", "omni.ui", "Main dark desktop window featuring the Asset Analyzer tab and conversion controls."},
            {"PdfPreviewDialog.java", "omni.ui", "In-app page previewer rendering authentic US Letter sheets directly in Swing."}
        };

        y = drawTable(g, 80, y, new String[]{"File Name", "Package", "Purpose & Mechanics"},
                new int[]{240, 140, 684}, files);

        drawFooter(g, 2, 5);
        g.dispose();
        return img;
    }

    /* ── PAGE 3: Core Java Concepts Used ──────────────────────────────── */
    private static BufferedImage createPage3() {
        BufferedImage img = newPage();
        Graphics2D g = setupGraphics(img);

        drawHeader(g, 3, 5, "Academic Syllabus Alignment (Units 1 to 5)");
        int y = 140;

        drawCardHeader(g, 80, y, 1064, "4. Core Java & OOP Concepts Used (Lab by Lab)");
        y += 40;

        String[][] units = {
            {"Unit 1 (Labs 1–4)", "Tokens, Arrays, Strings,\nConstructors, Wrapper Classes",
             "• Constructor Overloading in ConversionRecord (minimal 2-arg vs full 5-arg).\n" +
             "• StringTokenizer parsing metadata strings formatted as 'NAME#FORMAT#SIZE'.\n" +
             "• Wrapper Classes: explicit auto-boxing of sizes (Long), scores (Double), vowels (Integer).\n" +
             "• StringBuffer for dynamic HTML/RTF generation and formatted telemetry cards."},

            {"Unit 2 (Labs 5–6)", "Inheritance, Interfaces\n& Package Structure",
             "• 3-Level Multilevel Hierarchy: FileItem -> ConvertedItem -> ConversionRecord.\n" +
             "• Multiple Interface Implementation: ConversionRecord implements Transformable and Auditable.\n" +
             "• Modular package design: omni.core, omni.engine, omni.storage, and omni.ui."},

            {"Unit 3 (Labs 7–9)", "Exception Handling &\nMultithreading",
             "• Custom Exceptions: InvalidFormatException and FileConversionException.\n" +
             "• Defensive arithmetic handling: catches ArithmeticException if original file size is 0 bytes.\n" +
             "• Multithreading: ImageConverter implements Runnable on a background worker thread with\n" +
             "  Thread.sleep(600) to keep the Swing Event Dispatch Thread (EDT) completely responsive."},

            {"Unit 4 (Labs 11–15)", "Java 8 Lambdas &\nModern Collections",
             "• Predicate<ConversionRecord> filters high-efficiency conversions (> 70.0% score).\n" +
             "• Consumer<ConversionRecord> prints structured audit cards; Supplier<String> supplies timestamps.\n" +
             "• Collections: ArrayList (history), HashSet (O(1) format validation), HashMap (frequency)."},

            {"Unit 5 (Lab 16)", "JDBC CRUD Persistence\n& PreparedStatement",
             "• Full CRUD operations: Create (insert record), Read (SELECT WHERE efficiency_score > 70.0),\n" +
             "  Update (updateRemarks), and Delete (deleteAll).\n" +
             "• Uses standard java.sql.PreparedStatement and java.sql.ResultSet."}
        };

        y = drawCardBlocks(g, 80, y, units);

        drawFooter(g, 3, 5);
        g.dispose();
        return img;
    }

    /* ── PAGE 4: Bidirectional Pipeline & Image Summary Tab ───────────── */
    private static BufferedImage createPage4() {
        BufferedImage img = newPage();
        Graphics2D g = setupGraphics(img);

        drawHeader(g, 4, 5, "Bidirectional Pipeline & Image Summary Tab");
        int y = 140;

        drawCardHeader(g, 80, y, 1064, "5. How Bidirectional Conversion Works");
        y += 40;

        String[] pipeInfo = {
            "\u2022 Image to PDF (Raw Stream Writing): The engine reads images, converts them to clean RGB JPEG data,",
            "  and builds a valid PDF 1.4 byte stream with page catalog, dimensions, and cross-reference table.",
            "",
            "\u2022 Image to Word DOC (Rich Text RTF): Rather than raw HTML that confuses Notepad, the engine builds a true",
            "  RTF document where images are embedded as hex-encoded picture blips. It opens natively in WordPad and Word.",
            "",
            "\u2022 PDF to Images (Extraction Pipeline): PdfExtractor searches the PDF binary stream for exact stream lengths",
            "  and JPEG markers (0xFF 0xD8 to 0xFF 0xD9), pulling every page out as a standalone high-resolution image.",
            "",
            "\u2022 DOC to Images (Extraction Pipeline): DocExtractor inspects the Word document, decodes the hex or Base64",
            "  picture blocks, and restores the original visual images."
        };
        y = drawParagraph(g, 95, y, pipeInfo);
        y += 35;

        drawCardHeader(g, 80, y, 1064, "6. The Dedicated Image Summary Tab");
        y += 40;

        String[] summaryInfo = {
            "The Image Summary tab provides an asset inspector that reveals deep technical metadata:",
            "\u2022 Resolution & Dimensions: Exact Width \u00D7 Height in pixels (e.g. 1920 \u00D7 1080 px).",
            "\u2022 Aspect Ratio: Precise proportional ratio (e.g. 1.33:1, 1.78:1) calculated dynamically.",
            "\u2022 Color Depth & Model: Identifies RGB 24-bit Truecolor or ARGB 32-bit with transparency channel.",
            "\u2022 File Size Metrics: Accurate byte counts and formatted Kilobytes/Megabytes display.",
            "\u2022 Naming Integrity & Vowels: Vowel count (Unit 1 Lab requirement) and unique character frequency.",
            "\u2022 Live Thumbnail: Interactive thumbnail preview of the selected queued asset."
        };
        y = drawParagraph(g, 95, y, summaryInfo);

        drawFooter(g, 4, 5);
        g.dispose();
        return img;
    }

    /* ── PAGE 5: Viva Voce Defense Guide ──────────────────────────────── */
    private static BufferedImage createPage5() {
        BufferedImage img = newPage();
        Graphics2D g = setupGraphics(img);

        drawHeader(g, 5, 5, "Viva Voce Examination Defense Guide");
        int y = 140;

        drawCardHeader(g, 80, y, 1064, "7. Top 5 Viva Questions & Human Answers for High Marks");
        y += 40;

        String[][] qa = {
            {"Q1: How do you generate a PDF without any external library like iText?",
             "Answer: We write standard PDF 1.4 byte structures directly using ByteArrayOutputStream. We manually " +
             "construct the PDF header (%PDF-1.4), Catalog object, Pages dictionary, Page object with MediaBox, " +
             "Content stream with image drawing operators, and embed the JPEG bytes under /DCTDecode. Finally, we " +
             "calculate exact byte positions to write the xref cross-reference table and trailer."},

            {"Q2: Why did Word documents previously open in Notepad and how did you solve it?",
             "Answer: Earlier, the file was HTML saved with a .doc extension. Windows systems without Microsoft Office " +
             "fell back to Notepad, which displayed the raw HTML tags. We fixed this by generating authentic Rich Text " +
             "Format (RTF) documents with embedded hex picture blips. This opens universally in Windows WordPad and Word " +
             "showing the real visual picture without any raw text."},

            {"Q3: How does your code prevent the Swing GUI from freezing during conversion?",
             "Answer: Heavy file conversions run on an asynchronous worker thread via ImageConverter, which implements " +
             "Runnable. We simulate realistic processing stages with Thread.sleep(600). Whenever the progress bar or status " +
             "needs updating, we pass a Runnable back to the Swing Event Dispatch Thread using SwingUtilities.invokeLater()."},

            {"Q4: How do you extract images from an existing PDF file?",
             "Answer: PdfExtractor reads the raw PDF binary bytes and looks for stream dictionaries with /Length or " +
             "scans for JPEG markers (Start-of-Image 0xFF 0xD8 and End-of-Image 0xFF 0xD9). It takes the extracted byte " +
             "arrays and decodes them into BufferedImage objects via ImageIO, saving each page as a standalone image."},

            {"Q5: Explain how JDBC CRUD persistence works in your project.",
             "Answer: ConversionDB implements standard JDBC interfaces (Connection, PreparedStatement, ResultSet) " +
             "using dynamic proxies over an in-memory transactional storage list. It supports full CRUD: insertRecord " +
             "(Create), readHighEfficiency executing 'SELECT * WHERE efficiency_score > 70.0' (Read), updateRemarks " +
             "(Update), and deleteAll (Delete)." }
        };

        y = drawQaBlocks(g, 80, y, qa);

        drawFooter(g, 5, 5);
        g.dispose();
        return img;
    }

    /* ── Drawing Helpers ────────────────────────────────────────────────── */
    private static BufferedImage newPage() {
        return new BufferedImage(PAGE_W, PAGE_H, BufferedImage.TYPE_INT_RGB);
    }

    private static Graphics2D setupGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, PAGE_W, PAGE_H);
        return g;
    }

    private static void drawHeader(Graphics2D g, int pageNum, int totalPages, String subtitle) {
        g.setColor(COLOR_PRIMARY);
        g.fillRect(0, 0, PAGE_W, 85);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("OmniFormat Studio \u2014 Academic Project Report", 80, 42);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(new Color(200, 210, 225));
        g.drawString(subtitle, 80, 68);

        g.drawString("Page " + pageNum + " of " + totalPages, PAGE_W - 170, 50);
    }

    private static void drawFooter(Graphics2D g, int pageNum, int totalPages) {
        g.setColor(COLOR_BORDER);
        g.drawLine(80, PAGE_H - 70, PAGE_W - 80, PAGE_H - 70);

        g.setColor(COLOR_MUTED);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("MIT-WPU Pune \u2022 Course CSE00130 \u2022 Core Java Microproject", 80, PAGE_H - 45);
        g.drawString("Page " + pageNum + " of " + totalPages, PAGE_W - 170, PAGE_H - 45);
    }

    private static void drawCardHeader(Graphics2D g, int x, int y, int w, String title) {
        g.setColor(COLOR_CARD);
        g.fillRoundRect(x, y, w, 32, 6, 6);
        g.setColor(COLOR_BORDER);
        g.drawRoundRect(x, y, w, 32, 6, 6);

        g.setColor(COLOR_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString(title, x + 15, y + 21);
    }

    private static int drawParagraph(Graphics2D g, int x, int y, String[] lines) {
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(COLOR_TEXT);
        for (String line : lines) {
            if (line.isEmpty()) {
                y += 12;
            } else {
                g.drawString(line, x, y);
                y += 22;
            }
        }
        return y;
    }

    private static int drawBulletPoints(Graphics2D g, int x, int y, String[] bullets) {
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(COLOR_TEXT);
        for (String b : bullets) {
            g.drawString(b, x, y);
            y += 24;
        }
        return y;
    }

    private static int drawTable(Graphics2D g, int x, int y, String[] headers, int[] colWidths, String[][] rows) {
        int rowH = 26;
        int totalW = 0;
        for (int w : colWidths) totalW += w;

        // Table Header
        g.setColor(COLOR_PRIMARY);
        g.fillRect(x, y, totalW, rowH);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        int curX = x;
        for (int i = 0; i < headers.length; i++) {
            g.drawString(headers[i], curX + 8, y + 18);
            curX += colWidths[i];
        }
        y += rowH;

        // Table Rows
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int r = 0; r < rows.length; r++) {
            g.setColor((r % 2 == 0) ? Color.WHITE : COLOR_CARD);
            g.fillRect(x, y, totalW, rowH);
            g.setColor(COLOR_BORDER);
            g.drawRect(x, y, totalW, rowH);

            curX = x;
            for (int c = 0; c < rows[r].length; c++) {
                g.setColor(c == 0 ? new Font("Segoe UI", Font.BOLD, 11).getStyle() == 1 ? COLOR_PRIMARY : COLOR_TEXT : COLOR_TEXT);
                g.drawString(rows[r][c], curX + 8, y + 18);
                curX += colWidths[c];
            }
            y += rowH;
        }
        return y;
    }

    private static int drawCardBlocks(Graphics2D g, int x, int y, String[][] blocks) {
        int blockW = 1064;
        for (String[] blk : blocks) {
            int blockH = 110;
            g.setColor(COLOR_CARD);
            g.fillRoundRect(x, y, blockW, blockH, 8, 8);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(x, y, blockW, blockH, 8, 8);

            // Left Unit badge
            g.setColor(COLOR_PRIMARY);
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.drawString(blk[0], x + 16, y + 26);

            g.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            g.setColor(COLOR_ACCENT);
            String[] subLines = blk[1].split("\n");
            int subY = y + 44;
            for (String s : subLines) {
                g.drawString(s, x + 16, subY);
                subY += 16;
            }

            // Right description
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.setColor(COLOR_TEXT);
            String[] descLines = blk[2].split("\n");
            int descY = y + 24;
            for (String d : descLines) {
                g.drawString(d, x + 240, descY);
                descY += 18;
            }

            y += blockH + 12;
        }
        return y;
    }

    private static int drawQaBlocks(Graphics2D g, int x, int y, String[][] qa) {
        int blockW = 1064;
        for (String[] item : qa) {
            String question = item[0];
            String answer = item[1];

            g.setColor(COLOR_CARD);
            g.fillRoundRect(x, y, blockW, 115, 6, 6);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(x, y, blockW, 115, 6, 6);

            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.setColor(COLOR_ACCENT);
            g.drawString(question, x + 14, y + 22);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.setColor(COLOR_TEXT);

            // Word wrap answer in block
            int lineY = y + 42;
            int maxChars = 125;
            int start = 0;
            while (start < answer.length()) {
                int end = Math.min(start + maxChars, answer.length());
                if (end < answer.length() && answer.charAt(end) != ' ') {
                    int lastSpace = answer.lastIndexOf(' ', end);
                    if (lastSpace > start) end = lastSpace;
                }
                g.drawString(answer.substring(start, end).trim(), x + 14, lineY);
                lineY += 18;
                start = end;
                while (start < answer.length() && answer.charAt(start) == ' ') start++;
            }

            y += 125;
        }
        return y;
    }

    public static void main(String[] args) {
        String target = (args != null && args.length > 0) ? args[0] : "PROJECT_REPORT.pdf";
        try {
            System.out.println("[..] Generating multi-page academic PDF report: " + target);
            generateReportPdf(target);
            System.out.println("[OK] Successfully generated: " + new File(target).getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[FAIL] Report generation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

