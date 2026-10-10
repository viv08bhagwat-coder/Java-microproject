package omni.core;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 2 / Lab 5–6 : Multilevel Inheritance — Level 3 (Leaf)
 *                      Implements BOTH Transformable and Auditable.
 *  Unit 1 / Lab 1–4 : Constructor Overloading, Wrapper Classes
 *                      (Double for efficiencyScore, Integer for counts)
 * ═══════════════════════════════════════════════════════════════════════════ */
public class ConversionRecord extends ConvertedItem
                              implements Transformable, Auditable {

    private Double      efficiencyScore;   // Wrapper class Double  (Unit 1)
    private String      remarks;
    private String      timestamp;
    private File        outputFile;        // Converted target file on disk
    private List<File>  outputFiles = new ArrayList<>(); // All generated files

    /* ═══════════════════════════════════════════════════════════════════════
     *  Unit 1 / Lab 1–4 : Constructor Overloading
     * ═══════════════════════════════════════════════════════════════════════ */

    /** Minimal constructor — only file name and target format required. */
    public ConversionRecord(String fileName, String targetFormat) {
        super(fileName, 0L, "unknown", targetFormat);       // auto-boxing 0 -> Long
        this.efficiencyScore = 0.0;                         // auto-boxing double -> Double
        this.remarks         = "";
        this.timestamp       = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** Full constructor — all fields supplied. */
    public ConversionRecord(String fileName, Long size,
                            String srcFmt, String tgtFmt, Double score) {
        super(fileName, size, srcFmt, tgtFmt);
        this.efficiencyScore = score;                       // Wrapper class usage
        this.remarks         = "";
        this.timestamp       = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /* ── Accessors ── */
    public Double  getEfficiencyScore()             { return efficiencyScore; }
    public void    setEfficiencyScore(Double score)  { this.efficiencyScore = score; }
    public String  getRemarks()                      { return remarks; }
    public void    setRemarks(String remarks)        { this.remarks = remarks; }
    public String  getTimestamp()                     { return timestamp; }
    public void    setTimestamp(String ts)            { this.timestamp = ts; }
    public File    getOutputFile()                   { return outputFile; }
    public void    setOutputFile(File outputFile)    {
        this.outputFile = outputFile;
        if (outputFile != null && !this.outputFiles.contains(outputFile)) {
            this.outputFiles.add(outputFile);
        }
    }
    public List<File> getOutputFiles()               { return outputFiles; }
    public void    setOutputFiles(List<File> files)  {
        this.outputFiles = (files != null) ? files : new ArrayList<>();
        if (this.outputFiles != null && !this.outputFiles.isEmpty()) {
            this.outputFile = this.outputFiles.get(0);
        }
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  Unit 2 / Lab 5–6 : Interface Implementation — Transformable
     * ═══════════════════════════════════════════════════════════════════════ */
    @Override
    public String transform() {
        return String.format("Transforming '%s' from %s to %s  (Efficiency: %.1f%%)",
                fileName, sourceFormat, targetFormat, efficiencyScore);
    }

    /* ═══════════════════════════════════════════════════════════════════════
     *  Unit 2 / Lab 5–6 : Interface Implementation — Auditable
     * ═══════════════════════════════════════════════════════════════════════ */
    @Override
    public String auditLog() {
        return String.format("[AUDIT] %s | %s | %s\u2192%s | %.1f%% | %s",
                timestamp, fileName, sourceFormat, targetFormat,
                efficiencyScore, remarks);
    }

    @Override
    public String describe() {
        return String.format("%s [%s\u2192%s] Score=%.1f%%",
                fileName, sourceFormat, targetFormat, efficiencyScore);
    }

    @Override
    public String toString() {
        return describe();
    }
}
