package omni.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Image-to-PDF Conversion Pipeline
 *  — Writes a native, standard-compliant single- or multi-page PDF
 *    (%PDF-1.4 header, xref table, page streams, trailer, EOF marker)
 *    using raw byte-stream output. No external PDF library is needed.
 *  — Universal viewer compatibility: standard US Letter geometry,
 *    /DCTDecode JPEG streams, 20-byte CRLF xref entries, /ProcSet.
 *  Unit 3 / Lab 7–9 : Exception handling, try-with-resources
 * ═══════════════════════════════════════════════════════════════════════════ */
public class PdfGenerator {

    /**
     * Generate a single-page PDF containing the supplied image.
     */
    public static void generatePdf(BufferedImage image,
                                   String outputPath) throws IOException {
        generateMultiPagePdf(Collections.singletonList(image), outputPath);
    }

    /**
     * Generate a multi-page PDF containing multiple images in sequence.
     */
    public static void generateMultiPagePdf(List<BufferedImage> images,
                                            String outputPath) throws IOException {
        if (images == null || images.isEmpty()) {
            throw new IOException("Cannot generate PDF: no images provided.");
        }

        int numPages = images.size();
        // Object IDs layout:
        // 1: Catalog
        // 2: Pages
        // For each page i (0 <= i < numPages):
        //   pageObj    = 3 + 3 * i
        //   contentObj = 4 + 3 * i
        //   imageObj   = 5 + 3 * i
        int totalObjs = 2 + 3 * numPages;
        int[] offsets = new int[totalObjs + 1];

        ByteArrayOutputStream pdf = new ByteArrayOutputStream(images.size() * 65536 + 4096);

        // ── %PDF-1.4 header + binary marker ──
        writeLatin(pdf, "%PDF-1.4\n");
        pdf.write(new byte[] { '%', (byte) 0xE2, (byte) 0xE3,
                               (byte) 0xCF, (byte) 0xD3, '\n' });

        // ── Object 1 : Catalog ──
        offsets[1] = pdf.size();
        writeLatin(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n\n");

        // ── Object 2 : Pages ──
        offsets[2] = pdf.size();
        StringBuffer kids = new StringBuffer();
        for (int i = 0; i < numPages; i++) {
            kids.append((3 + 3 * i)).append(" 0 R ");
        }
        writeLatin(pdf, "2 0 obj\n<< /Type /Pages /Kids [" + kids.toString().trim()
                + "] /Count " + numPages + " >>\nendobj\n\n");

        // ── Write each Page, its Content Stream, and Image XObject ──
        for (int i = 0; i < numPages; i++) {
            BufferedImage image = images.get(i);

            // Re-encode to JPEG (strip alpha channel with white background)
            BufferedImage rgb = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(image, 0, 0, null);
            g.dispose();

            ByteArrayOutputStream jpgBuf = new ByteArrayOutputStream();
            if (!ImageIO.write(rgb, "jpeg", jpgBuf)) {
                throw new IOException("JPEG encoding failed for page " + (i + 1));
            }
            byte[] imgBytes = jpgBuf.toByteArray();
            int imgW = image.getWidth();
            int imgH = image.getHeight();

            int pageObj    = 3 + 3 * i;
            int contentObj = 4 + 3 * i;
            int imageObj   = 5 + 3 * i;

            // Geometry (US Letter: 612 x 792 pt, 36 pt margin)
            double pageW = 612.0, pageH = 792.0, margin = 36.0;
            double maxW  = pageW - 2 * margin;
            double maxH  = pageH - 2 * margin;
            double scale = Math.min(maxW / imgW, maxH / imgH);
            double w     = imgW * scale;
            double h     = imgH * scale;
            double x     = (pageW - w) / 2.0;
            double y     = (pageH - h) / 2.0;

            // Page Object (with /ProcSet and local /Img0 XObject)
            offsets[pageObj] = pdf.size();
            writeLatin(pdf,
                    pageObj + " 0 obj\n<< /Type /Page /Parent 2 0 R " +
                    "/MediaBox [0 0 612 792] " +
                    "/Contents " + contentObj + " 0 R " +
                    "/Resources << /ProcSet [/PDF /ImageC] /XObject << /Img0 " + imageObj + " 0 R >> >> " +
                    ">>\nendobj\n\n");

            // Content Stream (Draw Image)
            String drawCmd = String.format(
                    Locale.US,
                    "q\n%.4f 0 0 %.4f %.4f %.4f cm\n/Img0 Do\nQ", w, h, x, y);
            byte[] drawBytes = drawCmd.getBytes("US-ASCII");

            offsets[contentObj] = pdf.size();
            writeLatin(pdf, contentObj + " 0 obj\n<< /Length " + drawBytes.length + " >>\nstream\n");
            pdf.write(drawBytes);
            writeLatin(pdf, "\nendstream\nendobj\n\n");

            // Image XObject (JPEG / DCTDecode)
            offsets[imageObj] = pdf.size();
            writeLatin(pdf,
                    imageObj + " 0 obj\n<< /Type /XObject /Subtype /Image " +
                    "/Width " + imgW + " /Height " + imgH + " " +
                    "/ColorSpace /DeviceRGB /BitsPerComponent 8 " +
                    "/Filter /DCTDecode /Length " + imgBytes.length +
                    " >>\nstream\n");
            pdf.write(imgBytes);
            writeLatin(pdf, "\nendstream\nendobj\n\n");
        }

        // ── Cross-reference table (strict 20-byte CRLF entries) ──
        int xrefStart = pdf.size();
        writeLatin(pdf, "xref\n0 " + (totalObjs + 1) + "\n");
        writeLatin(pdf, "0000000000 65535 f \r\n");
        for (int k = 1; k <= totalObjs; k++) {
            writeLatin(pdf, String.format(Locale.US, "%010d 00000 n \r\n", offsets[k]));
        }

        // ── Trailer ──
        writeLatin(pdf, "trailer\n<< /Size " + (totalObjs + 1) + " /Root 1 0 R >>\n");
        writeLatin(pdf, "startxref\n" + xrefStart + "\n%%EOF\n");

        /* ── Flush to disk ── */
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            pdf.writeTo(fos);
        }
    }

    /** Write an ASCII / Latin-1 string to the PDF output buffer. */
    private static void writeLatin(ByteArrayOutputStream out,
                                   String text) throws IOException {
        out.write(text.getBytes("ISO-8859-1"));
    }
}
