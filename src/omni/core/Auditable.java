package omni.core;

/* ═══════════════════════════════════════════════════════════════════════════
 *  Unit 2 / Lab 5–6 : Interfaces — Audit logging contract
 * ═══════════════════════════════════════════════════════════════════════════ */
public interface Auditable {

    /**
     * Produce a formatted audit-log entry for this entity.
     *
     * @return audit string suitable for persistence or display
     */
    String auditLog();
}
