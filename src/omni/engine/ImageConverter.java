package omni.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import omni.core.ConversionRecord;
import omni.core.FileConversionException;
import omni.core.InvalidFormatException;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 3 / Lab 7–9 : Multithreading — Background worker thread
 *                      Implements Runnable, uses Thread.sleep(600) to
 *                      simulate pipeline stages, and pushes UI updates
 *                      back to the Swing EDT via SwingUtilities.invokeLater.
 *  Unit 3 / Lab 7–9 : Custom Exceptions (InvalidFormatException,
 *                      FileConversionException), ArithmeticException
 *                      handling for efficiency-ratio division.
 *  Bidirectional Conversion Engine:
 *  - Image \u2192 Image (PNG \u2194 JPG \u2194 BMP)
 *  - Image \u2192 PDF
 *  - Image \u2192 DOC
 *  - PDF \u2192 Images (PNG, JPG, BMP)
 *  - DOC \u2192 Images (PNG, JPG, BMP)
 * ═══════════════════════════════════════════════════════════════════════════ */
public class ImageConverter implements Runnable {

    /* ── Callback contract for Swing GUI updates ── */
    public interface ConversionCallback {
        void onProgress(int percent, String message);
        void onComplete(ConversionRecord record);
        void onError(String message);
    }

    private final List<File>         sourceFiles;
    private final String             targetFormat;
    private final File               outputDir;
    private final boolean            mergeMultiPage;
    private final ConversionCallback callback;

    public ImageConverter(File sourceFile, String targetFormat,
                          File outputDir, ConversionCallback callback) {
        this(Collections.singletonList(sourceFile), targetFormat, outputDir, true, callback);
    }

    public ImageConverter(List<File> sourceFiles, String targetFormat,
                          File outputDir, boolean mergeMultiPage,
                          ConversionCallback callback) {
        this.sourceFiles    = (sourceFiles != null) ? sourceFiles : new ArrayList<>();
        this.targetFormat   = (targetFormat != null) ? targetFormat.toLowerCase().trim() : "";
        this.outputDir      = outputDir;
        this.mergeMultiPage = mergeMultiPage;
        this.callback       = callback;
    }

    @Override
    public void run() {
        try {
            if (sourceFiles.isEmpty()) {
                throw new FileConversionException("No source files selected.");
            }

            // ── Stage 1 : Validate Target Format ──
            progress(10, "Validating target format '" + targetFormat.toUpperCase() + "'...");
            Thread.sleep(600);
            FormatValidator.validate(targetFormat);

            // Determine if source is Document (PDF) or Image
            String firstExt = getExtension(sourceFiles.get(0).getName());

            if (firstExt.equals("pdf") && FormatValidator.isImageFormat(targetFormat)) {
                // PDF -> Images (PNG, JPG, BMP)
                runPdfToImages();
            } else if (sourceFiles.size() == 1) {
                // Single Image -> Image / PDF
                runSingleFile(sourceFiles.get(0));
            } else {
                // Multiple Images -> Multi-page PDF / Batch Images
                runMultiFiles();
            }

        } catch (InvalidFormatException e) {
            SwingUtilities.invokeLater(() -> callback.onError("Invalid format: " + e.getMessage()));
        } catch (FileConversionException e) {
            SwingUtilities.invokeLater(() -> callback.onError("Conversion failed: " + e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SwingUtilities.invokeLater(() -> callback.onError("Conversion interrupted"));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> callback.onError("Unexpected error: " + e.getMessage()));
        }
    }

    /* ── Pipeline: PDF \u2192 Images ────────────────────────────────────────── */
    private void runPdfToImages() throws Exception {
        progress(25, "Parsing PDF stream and extracting visual assets...");
        Thread.sleep(600);

        List<File> generated = new ArrayList<>();
        long originalTotal = 0;
        long newTotal = 0;

        for (File pdfFile : sourceFiles) {
            originalTotal += pdfFile.length();
            String base = removeExtension(pdfFile.getName());
            List<BufferedImage> extracted = PdfExtractor.extractImages(pdfFile);

            if (extracted.isEmpty()) {
                throw new FileConversionException("No visual assets found in PDF: " + pdfFile.getName());
            }

            progress(55, "Transcoding " + extracted.size() + " extracted page(s) to " + targetFormat.toUpperCase() + "...");
            Thread.sleep(600);

            for (int i = 0; i < extracted.size(); i++) {
                File out = new File(outputDir, base + "_page" + (i + 1) + "." + targetFormat);
                convertImage(extracted.get(i), targetFormat, out);
                generated.add(out);
                newTotal += out.length();
            }
        }

        progress(85, "Computing efficiency metrics...");
        Thread.sleep(600);
        Double score = calculateEfficiency(originalTotal, newTotal);

        ConversionRecord record = new ConversionRecord(
                sourceFiles.get(0).getName(), originalTotal,
                "pdf", targetFormat, score);
        record.setOutputFiles(generated);
        record.setRemarks("Extracted " + generated.size() + " image(s) from PDF to " + targetFormat.toUpperCase());

        progress(100, "PDF to " + targetFormat.toUpperCase() + " conversion complete!");
        SwingUtilities.invokeLater(() -> callback.onComplete(record));
    }

    /* ── Pipeline: Single Image Conversion ──────────────────────────────── */
    private void runSingleFile(File sourceFile) throws Exception {
        progress(25, "Reading source image '" + sourceFile.getName() + "'...");
        Thread.sleep(600);

        BufferedImage image = ImageIO.read(sourceFile);
        if (image == null) {
            throw new FileConversionException("Cannot decode image: " + sourceFile.getName());
        }

        long   originalSize = sourceFile.length();
        String srcFormat    = getExtension(sourceFile.getName());
        String baseName     = removeExtension(sourceFile.getName());

        progress(50, "Transforming to " + targetFormat.toUpperCase() + "...");
        Thread.sleep(600);

        File outputFile;
        if (FormatValidator.isImageFormat(targetFormat)) {
            outputFile = new File(outputDir, baseName + "." + targetFormat);
            convertImage(image, targetFormat, outputFile);
        } else if (targetFormat.equals("pdf")) {
            outputFile = new File(outputDir, baseName + ".pdf");
            PdfGenerator.generatePdf(image, outputFile.getAbsolutePath());
        } else {
            throw new FileConversionException("Unhandled target format: " + targetFormat);
        }

        progress(80, "Computing compression efficiency...");
        Thread.sleep(600);

        long newSize = outputFile.length();
        Double efficiency = calculateEfficiency(originalSize, newSize);

        ConversionRecord record = new ConversionRecord(
                sourceFile.getName(), originalSize,
                srcFormat, targetFormat, efficiency);
        record.setOutputFile(outputFile);
        record.setRemarks("Output: " + outputFile.getName() + " (" + newSize + " bytes)");

        progress(100, "Conversion complete!");
        SwingUtilities.invokeLater(() -> callback.onComplete(record));
    }

    /* ── Pipeline: Multiple Images Conversion ───────────────────────────── */
    private void runMultiFiles() throws Exception {
        int count = sourceFiles.size();
        progress(20, "Loading " + count + " source images...");
        Thread.sleep(600);

        List<BufferedImage> images = new ArrayList<>();
        List<File> generatedFiles  = new ArrayList<>();
        long totalOriginalSize     = 0;

        for (File f : sourceFiles) {
            totalOriginalSize += f.length();
            BufferedImage img = ImageIO.read(f);
            if (img == null) {
                throw new FileConversionException("Cannot decode: " + f.getName());
            }
            images.add(img);
        }

        // Case A: Merged Multi-page PDF
        if (targetFormat.equals("pdf") && mergeMultiPage) {
            progress(50, "Assembling " + count + "-page native PDF document...");
            Thread.sleep(600);

            File outputFile = new File(outputDir, "ImageConverter_Output.pdf");
            PdfGenerator.generateMultiPagePdf(images, outputFile.getAbsolutePath());
            generatedFiles.add(outputFile);

            long totalNewSize = outputFile.length();
            Double efficiency = calculateEfficiency(totalOriginalSize, totalNewSize);

            ConversionRecord record = new ConversionRecord(
                    "[" + count + " images combined]", totalOriginalSize,
                    "multi", "pdf", efficiency);
            record.setOutputFile(outputFile);
            record.setOutputFiles(generatedFiles);
            record.setRemarks("Merged " + count + " images \u2192 " + outputFile.getName() + " (" + totalNewSize + " bytes)");

            progress(100, "Multi-page PDF generated successfully!");
            SwingUtilities.invokeLater(() -> callback.onComplete(record));
            return;
        }

        // Case B: Batch conversion to individual files
        long totalNewSize = 0;
        for (int i = 0; i < count; i++) {
            File sf = sourceFiles.get(i);
            BufferedImage img = images.get(i);
            String base = removeExtension(sf.getName());
            int pct = 30 + (int) (((double) (i + 1) / count) * 50);
            progress(pct, "Converting (" + (i + 1) + "/" + count + "): " + sf.getName());

            File out;
            if (targetFormat.equals("pdf")) {
                out = new File(outputDir, base + ".pdf");
                PdfGenerator.generatePdf(img, out.getAbsolutePath());
            } else {
                out = new File(outputDir, base + "." + targetFormat);
                convertImage(img, targetFormat, out);
            }
            generatedFiles.add(out);
            totalNewSize += out.length();
        }

        Double efficiency = calculateEfficiency(totalOriginalSize, totalNewSize);
        ConversionRecord record = new ConversionRecord(
                "[" + count + " files batch]", totalOriginalSize,
                "multi", targetFormat, efficiency);
        record.setOutputFiles(generatedFiles);
        record.setRemarks("Batch converted " + count + " files to " + targetFormat.toUpperCase());

        progress(100, "All " + count + " files converted successfully!");
        SwingUtilities.invokeLater(() -> callback.onComplete(record));
    }

    /* ── Efficiency Metric (Unit 3 : ArithmeticException) ─────────────── */
    private Double calculateEfficiency(long originalSize, long newSize) {
        Double efficiency = 0.0;
        try {
            if (originalSize == 0L) {
                throw new ArithmeticException("Division by zero: original file size is 0 bytes");
            }
            efficiency = ((double) (originalSize - newSize) / (double) originalSize) * 100.0;
            if (efficiency < 0.0)   efficiency = 0.0;
            if (efficiency > 100.0) efficiency = 100.0;
        } catch (ArithmeticException ae) {
            System.err.println("[ImageConverter] Efficiency calculation error: " + ae.getMessage());
            efficiency = 0.0;
        }
        return efficiency;
    }

    private void progress(int pct, String msg) {
        SwingUtilities.invokeLater(() -> callback.onProgress(pct, msg));
    }

    private void convertImage(BufferedImage image, String fmt, File output) throws IOException {
        BufferedImage canvas = image;
        if (fmt.equals("jpg") || fmt.equals("jpeg")) {
            canvas = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
        String ioFormat = (fmt.equals("jpg") || fmt.equals("jpeg")) ? "jpeg" : fmt;
        if (!ImageIO.write(canvas, ioFormat, output)) {
            throw new IOException("ImageIO.write failed for format: " + fmt);
        }
    }

    private static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(dot + 1).toLowerCase() : "bin";
    }

    private static String removeExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }
}
