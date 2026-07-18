package io.github.rudradave1.proguardlint.reporter

import io.github.rudradave1.proguardlint.model.AuditResult
import io.github.rudradave1.proguardlint.model.Violation
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class ConsoleReporterTest {

    private val passResult = AuditResult(
        violations = emptyList(),
        totalSeedsChecked = 120,
        dangerZonesChecked = 2,
        elapsedMs = 45
    )

    private val failResult = AuditResult(
        violations = listOf(
            Violation("com.app.payment", "Gatekeeper", "com.app.payment.Gatekeeper"),
            Violation("com.app.crypto", "KeyStore", "com.app.crypto.KeyStore")
        ),
        totalSeedsChecked = 120,
        dangerZonesChecked = 2,
        elapsedMs = 48
    )

    @Test
    fun `pass summary contains PASS`() {
        val s = ConsoleReporter.summary(passResult)
        assertContains(s, "PASS")
        assertContains(s, "120")
        assertContains(s, "2")
    }

    @Test
    fun `fail summary contains FAIL and violations`() {
        val s = ConsoleReporter.summary(failResult)
        assertContains(s, "FAIL")
        assertContains(s, "2 violation")
        assertContains(s, "com.app.payment.Gatekeeper")
        assertContains(s, "com.app.crypto.KeyStore")
    }

    @Test
    fun `json report success`() {
        val json = ConsoleReporter.jsonReport(passResult)
        assertContains(json, "\"passed\": true")
        assertContains(json, "\"violationCount\": 0")
    }

    @Test
    fun `json report failure`() {
        val json = ConsoleReporter.jsonReport(failResult)
        assertContains(json, "\"passed\": false")
        assertContains(json, "\"violationCount\": 2")
        assertContains(json, "com.app.payment.Gatekeeper")
    }

    @Test
    fun `markdown comment for passing`() {
        val md = ConsoleReporter.markdownComment(passResult)
        assertContains(md, "obfuscated")
        assertContains(md, "120")
    }

    @Test
    fun `markdown comment for failing`() {
        val md = ConsoleReporter.markdownComment(failResult)
        assertContains(md, "un-obfuscated")
        assertContains(md, "com.app.payment.Gatekeeper")
    }

    @Test
    fun `build failure message lists violations`() {
        val msg = ConsoleReporter.buildFailureMessage(failResult)
        assertContains(msg, "2 un-obfuscated")
        assertContains(msg, "com.app.payment.Gatekeeper")
        assertContains(msg, "failOnError = false")
    }
}
