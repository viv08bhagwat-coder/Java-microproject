package omni.core;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 2 / Lab 5–6 : Interfaces — Functional contract for transformation
 *  Unit 4 / Lab 11–15: @FunctionalInterface annotation (Java 8 feature)
 * ═══════════════════════════════════════════════════════════════════════════ */
@FunctionalInterface
public interface Transformable {

    /**
     * Perform or describe the transformation operation.
     *
     * @return human-readable description of the transformation
     * @throws Exception if transformation cannot proceed
     */
    String transform() throws Exception;
}
