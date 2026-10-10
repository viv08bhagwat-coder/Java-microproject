package omni.core;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 3 / Lab 7–9 : Custom Exception — thrown when an unsupported
 *                      conversion format is requested.
 * ═══════════════════════════════════════════════════════════════════════════ */
public class InvalidFormatException extends Exception {

    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
