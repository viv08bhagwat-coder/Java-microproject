package omni.core;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 2 / Lab 5–6 : Multilevel Inheritance — Level 1 (Abstract Base)
 *  Unit 1 / Lab 1–4 : Wrapper Classes — Long for fileSizeBytes
 * ═══════════════════════════════════════════════════════════════════════════ */
public abstract class FileItem {

    protected String fileName;
    protected Long   fileSizeBytes;   // Wrapper class Long (Unit 1)

    /* ── Constructor (Unit 1 / Lab 1–4) ── */
    public FileItem(String fileName, Long fileSizeBytes) {
        this.fileName      = fileName;
        this.fileSizeBytes = fileSizeBytes;      // auto-boxing demo
    }

    /* ── Accessors ── */
    public String getFileName()              { return fileName; }
    public Long   getFileSizeBytes()         { return fileSizeBytes; }
    public void   setFileSizeBytes(Long sz)  { this.fileSizeBytes = sz; }

    /**
     * Subclass-specific description of this file item.
     */
    public abstract String describe();

    @Override
    public String toString() {
        return fileName + " (" + fileSizeBytes + " bytes)";
    }
}
