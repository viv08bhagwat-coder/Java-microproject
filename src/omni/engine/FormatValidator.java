package omni.engine;

import java.util.HashSet;
import omni.core.InvalidFormatException;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 4 / Lab 11–15 : Collections — HashSet for O(1) format validation
 * ═══════════════════════════════════════════════════════════════════════════ */
public class FormatValidator {

    /** Constant-time lookup set of supported formats. */
    private static final HashSet<String> ALLOWED_FORMATS = new HashSet<>();

    static {
        ALLOWED_FORMATS.add("jpg");
        ALLOWED_FORMATS.add("jpeg");
        ALLOWED_FORMATS.add("png");
        ALLOWED_FORMATS.add("bmp");
        ALLOWED_FORMATS.add("pdf");
    }

    /** @return true when the format string (case-insensitive) is supported. */
    public static boolean isValid(String format) {
        return format != null && ALLOWED_FORMATS.contains(format.toLowerCase().trim());
    }

    /**
     * Validates or throws.
     *
     * @throws InvalidFormatException when format is not in the allowed set
     */
    public static void validate(String format) throws InvalidFormatException {
        if (!isValid(format)) {
            throw new InvalidFormatException(
                    "Unsupported format: '" + format + "'. Allowed: " + ALLOWED_FORMATS);
        }
    }

    /** @return defensive copy of the allowed-format set. */
    public static HashSet<String> getAllowedFormats() {
        return new HashSet<>(ALLOWED_FORMATS);
    }

    /** @return true if the format is a raster image type (jpg / png / bmp). */
    public static boolean isImageFormat(String format) {
        if (format == null) return false;
        String f = format.toLowerCase().trim();
        return f.equals("jpg") || f.equals("jpeg") || f.equals("png") || f.equals("bmp");
    }

    /** @return true if the format is a document type (pdf). */
    public static boolean isDocumentFormat(String format) {
        if (format == null) return false;
        String f = format.toLowerCase().trim();
        return f.equals("pdf");
    }
}
