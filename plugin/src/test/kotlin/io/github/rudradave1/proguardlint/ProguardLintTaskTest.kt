package io.github.rudradave1.proguardlint

import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProguardLintTaskTest {

    private val project: Project = ProjectBuilder.builder().build()

    private fun taskWith(
        mapping: String?,
        seeds: String?,
        failOnError: Boolean = true,
        dangerZones: List<String> = listOf("com.mycompany.payment")
    ): ProguardLintTask {
        val t = project.tasks.create("lint", ProguardLintTask::class.java)
        val tmp = File(project.layout.buildDirectory.get().asFile, "tmp-fixtures").apply { mkdirs() }
        val mappingFile = File(tmp, "mapping.txt")
        val seedsFile = File(tmp, "seeds.txt")
        mapping?.let { mappingFile.writeText(it) }
        seeds?.let { seedsFile.writeText(it) }
        t.mappingFile.set(mappingFile)
        t.seedsFile.set(seedsFile)
        t.dangerZones.set(dangerZones)
        t.failOnError.set(failOnError)
        t.reportDir.set(project.layout.buildDirectory.dir("reports"))
        return t
    }

    @Test
    fun `fails with clear message when seeds file is missing`() {
        val t = taskWith(mapping = "a -> b:", seeds = null)
        val e = assertFailsWith<GradleException> { t.audit() }
        assertTrue(e.message!!.contains("seeds.txt not found"), e.message)
    }

    @Test
    fun `fails with clear message when mapping file is missing`() {
        val t = taskWith(mapping = null, seeds = "com.a.B")
        val e = assertFailsWith<GradleException> { t.audit() }
        assertTrue(e.message!!.contains("mapping.txt not found"), e.message)
    }

    @Test
    fun `writes json report on successful audit`() {
        val t = taskWith(
            mapping = "com.mycompany.payment.Gatekeeper -> a.b:",
            seeds = "com.mycompany.payment.Gatekeeper",
            failOnError = false
        )
        t.audit()
        val report = File(project.layout.buildDirectory.get().asFile, "reports/proguard-lint-report.json")
        assertTrue(report.exists(), "expected report: ${report.absolutePath}, saw: ${report.parentFile?.listFiles()?.toList()}")
    }
}
