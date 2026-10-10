package omni.core;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 3 / Lab 7–9 : Custom Exception — thrown when image I/O or
 *                      document generation fails at runtime.
 * ═══════════════════════════════════════════════════════════════════════════ */
public class FileConversionException extends Exception {

    public FileConversionException(String message) {
        super(message);
    }

    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
