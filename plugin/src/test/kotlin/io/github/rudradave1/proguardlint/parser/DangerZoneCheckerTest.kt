package io.github.rudradave1.proguardlint.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DangerZoneCheckerTest {

    private val sampleSeeds = listOf(
        SeedsParser.parse(
            """
            com.mycompany.app.MainActivity
            com.mycompany.payment.Gatekeeper
            com.mycompany.payment.Transaction
            com.mycompany.crypto.KeyStore
            com.mycompany.analytics.Tracker
            """.trimIndent().lines()
        )
    ).flatten()

    @Test
    fun `flags classes in danger zones`() {
        val violations = DangerZoneChecker.check(
            sampleSeeds,
            listOf("com.mycompany.payment", "com.mycompany.crypto")
        )

        assertEquals(3, violations.size)
        val packages = violations.map { it.packagePrefix }.toSet()
        assertEquals(setOf("com.mycompany.payment", "com.mycompany.crypto"), packages)
    }

    @Test
    fun `allows classes outside danger zones`() {
        val violations = DangerZoneChecker.check(
            sampleSeeds,
            listOf("com.mycompany.internal")
        )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `returns empty for empty danger zones`() {
        val violations = DangerZoneChecker.check(sampleSeeds, emptyList())

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `returns empty for no seeds`() {
        val violations = DangerZoneChecker.check(emptyList(), listOf("com.app"))

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `matches prefix correctly avoids accidental substring match`() {
        val seeds = listOf(
            SeedsParser.parse(
                """
                com.mycompany.paymentservice.Pay
                com.mycompany.payment.Gatekeeper
                """.trimIndent().lines()
            )
        ).flatten()

        val violations = DangerZoneChecker.check(
            seeds,
            listOf("com.mycompany.payment")
        )

        // Should NOT match "com.mycompany.paymentservice"
        assertEquals(1, violations.size)
        assertEquals("com.mycompany.payment.Gatekeeper", violations[0].fullyQualifiedName)
    }

    @Test
    fun `from fixture file matches expected`() {
        val content = this::class.java.classLoader
            .getResource("sample-seeds.txt")!!
            .readText()
        val seeds = SeedsParser.parse(content.lines())

        val violations = DangerZoneChecker.check(
            seeds,
            listOf("com.mycompany.payment", "com.mycompany.crypto")
        )

        assertEquals(3, violations.size)
        val classes = violations.map { it.className }.toSet()
        assertEquals(setOf("Gatekeeper", "Transaction", "KeyStore"), classes)
    }

    @Test
    fun `from clean fixture returns no violations`() {
        val content = this::class.java.classLoader
            .getResource("clean-seeds.txt")!!
            .readText()
        val seeds = SeedsParser.parse(content.lines())

        val violations = DangerZoneChecker.check(
            seeds,
            listOf("com.mycompany.payment", "com.mycompany.crypto")
        )

        assertTrue(violations.isEmpty())
    }
}
