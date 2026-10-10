package omni.core;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 2 / Lab 5–6 : Multilevel Inheritance — Level 2
 *                      Adds sourceFormat and targetFormat to FileItem.
 * ═══════════════════════════════════════════════════════════════════════════ */
public class ConvertedItem extends FileItem {

    protected String sourceFormat;
    protected String targetFormat;

    public ConvertedItem(String fileName, Long fileSizeBytes,
                         String sourceFormat, String targetFormat) {
        super(fileName, fileSizeBytes);
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    /* ── Accessors ── */
    public String getSourceFormat() { return sourceFormat; }
    public String getTargetFormat() { return targetFormat; }

    @Override
    public String describe() {
        return fileName + " [" + sourceFormat + " \u2192 " + targetFormat + "]";
    }
}
