package omni.engine;

import java.util.HashMap;
import java.util.StringTokenizer;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 1 / Lab 1–4 : String & StringBuffer Operations
 *                      — StringTokenizer parsing of metadata strings
 *                      — Vowel counting for naming-integrity checks
 *                      — Character-frequency map (HashMap, Unit 4)
 *                      — StringBuffer dynamic status-card builder
 *  Unit 3 / Lab 7–9 : ArrayIndexOutOfBoundsException handling
 *  Unit 4 / Lab 11–15: HashMap<Character, Integer>
 * ═══════════════════════════════════════════════════════════════════════════ */
public class MetadataParser {

    /* ─────────────────────────────────────────────────────────────────────
     *  Parse metadata string "FILENAME#FORMAT#FILESIZE" via StringTokenizer
     * ───────────────────────────────────────────────────────────────────── */
    public static String[] parseMetadata(String metadata) {
        String[] result = new String[3];
        try {
            /* Unit 1 : StringTokenizer usage */
            StringTokenizer tokenizer = new StringTokenizer(metadata, "#");
            int index = 0;
            while (tokenizer.hasMoreTokens() && index < 3) {
                result[index] = tokenizer.nextToken().trim();
                index++;
            }

            /* Unit 3 / Lab 7–9 : Deliberate bounds check */
            if (result[0] == null || result[1] == null || result[2] == null) {
                throw new ArrayIndexOutOfBoundsException(
                        "Incomplete metadata — expected 3 tokens, got " + index);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("[MetadataParser] Token parse error: " + e.getMessage());
            if (result[0] == null) result[0] = "unknown";
            if (result[1] == null) result[1] = "unknown";
            if (result[2] == null) result[2] = "0";
        }
        return result;
    }

    /* ─────────────────────────────────────────────────────────────────────
     *  Count vowels in a filename — naming integrity metric
     *  Unit 1 / Lab 1–4 : Wrapper class Integer (auto-boxing)
     * ───────────────────────────────────────────────────────────────────── */
    public static Integer countVowels(String fileName) {
        Integer count = 0;                                // auto-boxing int -> Integer
        String vowels = "aeiouAEIOU";
        for (int i = 0; i < fileName.length(); i++) {
            if (vowels.indexOf(fileName.charAt(i)) != -1) {
                count++;                                  // auto-unbox, increment, re-box
            }
        }
        return count;
    }

    /* ─────────────────────────────────────────────────────────────────────
     *  Character frequency map — Unit 4 / Lab 11–15 : HashMap
     * ───────────────────────────────────────────────────────────────────── */
    public static HashMap<Character, Integer> charFrequency(String fileName) {
        HashMap<Character, Integer> freqMap = new HashMap<>();
        for (char c : fileName.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);   // auto-boxing
        }
        return freqMap;
    }

    /* ─────────────────────────────────────────────────────────────────────
     *  Build formatted status card — Unit 1 / Lab 1–4 : StringBuffer
     * ───────────────────────────────────────────────────────────────────── */
    public static String buildStatusCard(String fileName, String srcFmt,
                                         String tgtFmt, double score) {
        StringBuffer sb = new StringBuffer();
        sb.append("+========================================+\n");
        sb.append("|       CONVERSION STATUS CARD           |\n");
        sb.append("+========================================+\n");
        sb.append("| File   : ").append(pad(fileName, 28)).append("|\n");
        sb.append("| Source : ").append(pad(srcFmt.toUpperCase(), 28)).append("|\n");
        sb.append("| Target : ").append(pad(tgtFmt.toUpperCase(), 28)).append("|\n");
        sb.append("| Score  : ").append(pad(String.format("%.1f%%", score), 28)).append("|\n");
        sb.append("| Vowels : ").append(pad(String.valueOf(countVowels(fileName)), 28)).append("|\n");
        sb.append("+========================================+");
        return sb.toString();
    }

    /** Right-pad a string with spaces to width. */
    private static String pad(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        StringBuffer buf = new StringBuffer(s);
        while (buf.length() < width) buf.append(' ');
        return buf.toString();
    }
}
