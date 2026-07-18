package io.github.rudradave1.proguardlint.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeedsParserTest {

    @Test
    fun `parses class-level seeds correctly`() {
        val lines = listOf(
            "com.mycompany.app.MainActivity",
            "com.mycompany.payment.Gatekeeper",
            "com.mycompany.crypto.KeyStore"
        )

        val result = SeedsParser.parse(lines)

        assertEquals(3, result.size)
        assertEquals("com.mycompany.app.MainActivity", result[0].fullyQualifiedName)
        assertEquals("com.mycompany.crypto", result[2].packageName)
        assertEquals("KeyStore", result[2].className)
    }

    @Test
    fun `skips method-level lines`() {
        val lines = listOf(
            "com.mycompany.app.MainActivity",
            "com.mycompany.app.MainActivity: void onCreate()",
            "com.mycompany.app.MainActivity: java.lang.String getData()"
        )

        val result = SeedsParser.parse(lines)

        assertEquals(1, result.size)
        assertEquals("com.mycompany.app.MainActivity", result[0].fullyQualifiedName)
    }

    @Test
    fun `skips blank and comment lines`() {
        val lines = listOf(
            "",
            "# This is a comment",
            "   ",
            "com.mycompany.app.MainActivity"
        )

        val result = SeedsParser.parse(lines)

        assertEquals(1, result.size)
    }

    @Test
    fun `parses from file fixture`() {
        val content = this::class.java.classLoader
            .getResource("sample-seeds.txt")!!
            .readText()
        val lines = content.lines()

        val result = SeedsParser.parse(lines)

        // 8 lines total, 2 method-level skipped -> 6 class seeds
        assertEquals(6, result.size)
    }

    @Test
    fun `returns empty for no class-type lines`() {
        val lines = listOf(
            "# header comment",
            "com.foo.Bar: void method()"
        )

        val result = SeedsParser.parse(lines)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `handles class line with trailing colon`() {
        val lines = listOf(
            "com.foo.Bar:",
            "com.foo.Baz:"
        )

        val result = SeedsParser.parse(lines)

        assertEquals(2, result.size)
        assertEquals("Bar", result[0].className)
        assertEquals("Baz", result[1].className)
    }

    @Test
    fun `returns empty for empty input`() {
        assertTrue(SeedsParser.parse(emptyList()).isEmpty())
    }
}
