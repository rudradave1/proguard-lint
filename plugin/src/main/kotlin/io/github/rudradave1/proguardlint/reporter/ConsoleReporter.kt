package io.github.rudradave1.proguardlint.reporter

import io.github.rudradave1.proguardlint.model.AuditResult
import io.github.rudradave1.proguardlint.model.Violation
import java.io.File

/**
 * Handles all output formatting:
 * - Pretty console table
 * - JSON report file (for CI tools to consume)
 * - Build failure / warning message
 */
object ConsoleReporter {

    private const val SEPARATOR = "═══════════════════════════════════════════════════════"

    fun summary(result: AuditResult): String {
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine(SEPARATOR)
        sb.appendLine("  ProGuardLint — Obfuscation Audit")
        sb.appendLine(SEPARATOR)

        if (result.passed) {
            sb.appendLine("  ✅ PASS — All danger zones properly obfuscated")
        } else {
            sb.appendLine("  ❌ FAIL — ${result.violations.size} violation(s) found")
            sb.appendLine()
            sb.appendLine("  Un-obfuscated classes (present in seeds.txt):")
            result.violations.forEach { v ->
                sb.appendLine("     • ${v.fullyQualifiedName}")
                sb.appendLine("       Danger zone: ${v.packagePrefix}")
            }
        }

        sb.appendLine()
        sb.appendLine("  Stats:")
        sb.appendLine("     Seeds checked:    ${result.totalSeedsChecked}")
        sb.appendLine("     Danger zones:     ${result.dangerZonesChecked}")
        sb.appendLine("     Duration:         ${result.elapsedMs}ms")
        sb.appendLine(SEPARATOR)
        return sb.toString()
    }

    fun jsonReport(result: AuditResult): String {
        return buildString {
            appendLine("{")
            appendLine("  \"passed\": ${result.passed},")
            appendLine("  \"violationCount\": ${result.violations.size},")
            appendLine("  \"totalSeedsChecked\": ${result.totalSeedsChecked},")
            appendLine("  \"dangerZonesChecked\": ${result.dangerZonesChecked},")
            appendLine("  \"elapsedMs\": ${result.elapsedMs},")
            appendLine("  \"violations\": [")
            result.violations.forEachIndexed { i, v ->
                appendLine("    {")
                appendLine("      \"packagePrefix\": \"${v.packagePrefix}\",")
                appendLine("      \"className\": \"${v.className}\",")
                appendLine("      \"fullyQualifiedName\": \"${v.fullyQualifiedName}\"")
                append("    }")
                if (i < result.violations.lastIndex) appendLine(",") else appendLine()
            }
            appendLine("  ]")
            append("}")
        }
    }

    fun writeJsonReport(result: AuditResult, outputDir: File) {
        outputDir.mkdirs()
        val file = File(outputDir, "proguard-lint-report.json")
        file.writeText(jsonReport(result))
    }

    fun markdownComment(result: AuditResult): String {
        val sb = StringBuilder()
        sb.appendLine("## 🔒 ProGuardLint Audit")

        if (result.passed) {
            sb.appendLine("")
            sb.appendLine("**✅ All danger zones properly obfuscated.**")
            sb.appendLine("")
            sb.appendLine("| Metric | Value |")
            sb.appendLine("|--------|-------|")
            sb.appendLine("| Seeds checked | ${result.totalSeedsChecked} |")
            sb.appendLine("| Danger zones | ${result.dangerZonesChecked} |")
            sb.appendLine("| Duration | ${result.elapsedMs}ms |")
        } else {
            sb.appendLine("")
            sb.appendLine("**🚨 ${result.violations.size} un-obfuscated class(es) found in danger zones.**")
            sb.appendLine("")
            for (v in result.violations) {
                sb.appendLine("- `${v.fullyQualifiedName}` — matches danger zone `${v.packagePrefix}`")
            }
            sb.appendLine("")
            sb.appendLine("| Metric | Value |")
            sb.appendLine("|--------|-------|")
            sb.appendLine("| Seeds checked | ${result.totalSeedsChecked} |")
            sb.appendLine("| Danger zones | ${result.dangerZonesChecked} |")
            sb.appendLine("| Violations | ${result.violations.size} |")
            sb.appendLine("| Duration | ${result.elapsedMs}ms |")
        }
        return sb.toString()
    }

    fun buildFailureMessage(result: AuditResult): String {
        return """
ProGuardLint: Build failed — ${result.violations.size} un-obfuscated class(es) found.

The following classes are present in seeds.txt (kept by -keep rules)
and match configured danger zones:

${result.violations.joinToString("\n") { "  • ${it.fullyQualifiedName} (zone: ${it.packagePrefix})" }}

Fix: Review your ProGuard/R8 -keep rules. Remove blanket keeps for
these packages or narrow the rules to only what's needed.

To run in warning-only mode, set:
  proguardLint { failOnError = false }
        """.trimIndent()
    }
}
