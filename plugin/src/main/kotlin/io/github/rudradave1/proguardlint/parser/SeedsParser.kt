package io.github.rudradave1.proguardlint.parser

import io.github.rudradave1.proguardlint.model.SeedsEntry

/**
 * Parses seeds.txt line-by-line.
 *
 * seeds.txt lists every class/method ProGuard was instructed to keep
 * via -keep rules. Each line is a fully qualified name.
 *
 * Lines that are blank, comments (#), or method signatures (containing
 * parentheses or colons after the class name) are skipped — we only
 * care about class-level seeds.
 */
object SeedsParser {

    fun parse(lines: List<String>): List<SeedsEntry> {
        return lines
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#") }
            // Skip member-level lines (fields and methods contain ": ").
            // Only class-level seeds matter for obfuscation audit.
            .filterNot { it.contains(": ") }
            .mapNotNull { line -> parseClassLine(line) }
    }

    private fun parseClassLine(line: String): SeedsEntry? {
        // mapping.txt/usage.txt class lines end with ':'
        // pure class name lines from seeds.txt don't
        val clean = line.removeSuffix(":").trim()
        if (clean.isEmpty()) return null

        val lastDot = clean.lastIndexOf('.')
        if (lastDot == -1) return null // no package — skip

        val packageName = clean.substring(0, lastDot)
        val className = clean.substring(lastDot + 1)
        return SeedsEntry(
            fullyQualifiedName = clean,
            packageName = packageName,
            className = className
        )
    }
}
