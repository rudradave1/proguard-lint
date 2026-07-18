package io.github.rudradave1.proguardlint.parser

import io.github.rudradave1.proguardlint.model.MappingEntry

/**
 * Parses mapping.txt line-by-line.
 *
 * mapping.txt maps original -> obfuscated names. Key pattern:
 *   `com.foo.Bar -> a.a.a:`   (class mapping)
 *   `    int x -> b`          (member mapping, indented)
 *
 * For MVP we use this to confirm which classes were actually obfuscated
 * vs kept as-is (original name == obfuscated name).
 */
object MappingParser {

    fun parse(lines: List<String>): List<MappingEntry> {
        return lines
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .mapNotNull { line -> parseLine(line) }
    }

    private fun parseLine(line: String): MappingEntry? {
        // Class mapping: `com.foo.Original -> x.y.z:`
        val arrowIndex = line.indexOf(" -> ")
        if (arrowIndex == -1) return null

        val beforeArrow = line.substring(0, arrowIndex).trim()
        val afterArrow = line.substring(arrowIndex + 4).trimEnd(':').trim()

        if (beforeArrow.isEmpty() || afterArrow.isEmpty()) return null

        val isMember = line.startsWith("    ") || line.startsWith("\t")
        return MappingEntry(
            originalName = beforeArrow,
            obfuscatedName = afterArrow,
            isClassMember = isMember
        )
    }
}
