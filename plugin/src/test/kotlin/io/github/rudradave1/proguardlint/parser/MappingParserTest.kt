package io.github.rudradave1.proguardlint.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappingParserTest {

    @Test
    fun `parses class mappings correctly`() {
        val lines = listOf(
            "com.foo.Original -> x.y.z:"
        )

        val result = MappingParser.parse(lines)

        assertEquals(1, result.size)
        assertEquals("com.foo.Original", result[0].originalName)
        assertEquals("x.y.z", result[0].obfuscatedName)
        assertEquals(false, result[0].isClassMember)
    }

    @Test
    fun `detects member mappings by indentation`() {
        val lines = listOf(
            "com.foo.Original -> a.a.a:",
            "    int x -> b"
        )

        val result = MappingParser.parse(lines)

        assertEquals(2, result.size)
        assertTrue(result[1].isClassMember)
        assertEquals("int x", result[1].originalName)
    }

    @Test
    fun `parses from file fixture`() {
        val content = this::class.java.classLoader
            .getResource("sample-mapping.txt")!!
            .readText()
        val lines = content.lines()

        val result = MappingParser.parse(lines)

        // 8 lines: 6 class mappings + 2 member mappings
        assertEquals(8, result.size)
    }

    @Test
    fun `identifies un-obfuscated class`() {
        val lines = listOf(
            "com.mycompany.analytics.Tracker -> com.mycompany.analytics.Tracker:"
        )

        val result = MappingParser.parse(lines)

        assertEquals(1, result.size)
        assertEquals(result[0].originalName, result[0].obfuscatedName)
    }

    @Test
    fun `skips lines without arrow`() {
        val lines = listOf(
            "ProGuardPlugin",
            "some random text"
        )

        val result = MappingParser.parse(lines)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty for empty input`() {
        assertTrue(MappingParser.parse(emptyList()).isEmpty())
    }
}
