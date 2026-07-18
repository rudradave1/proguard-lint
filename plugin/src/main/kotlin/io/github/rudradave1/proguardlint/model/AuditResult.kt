package io.github.rudradave1.proguardlint.model

data class AuditResult(
    val violations: List<Violation>,
    val totalSeedsChecked: Int,
    val dangerZonesChecked: Int,
    val elapsedMs: Long
) {
    val passed: Boolean get() = violations.isEmpty()
}
