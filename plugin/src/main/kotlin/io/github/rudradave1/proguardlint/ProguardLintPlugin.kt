package io.github.rudradave1.proguardlint

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import io.github.rudradave1.proguardlint.model.AuditResult
import io.github.rudradave1.proguardlint.parser.DangerZoneChecker
import io.github.rudradave1.proguardlint.parser.MappingParser
import io.github.rudradave1.proguardlint.parser.SeedsParser
import io.github.rudradave1.proguardlint.reporter.ConsoleReporter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class ProguardLintExtension {
    abstract val dangerZones: ListProperty<String>
    abstract val failOnError: Property<Boolean>
    abstract val runOnBuild: Property<Boolean>
}

@CacheableTask
abstract class ProguardLintTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mappingFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val seedsFile: RegularFileProperty

    @get:Input
    abstract val dangerZones: ListProperty<String>

    @get:Input
    abstract val failOnError: Property<Boolean>

    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @TaskAction
    fun audit() {
        val start = System.currentTimeMillis()

        val mapping = mappingFile.get().asFile
        if (!mapping.exists()) {
            throw GradleException("ProGuardLint: mapping.txt not found at ${mapping.absolutePath}. Is minifyEnabled = true for the release variant?")
        }
        val seedsTxt = seedsFile.get().asFile
        if (!seedsTxt.exists()) {
            throw GradleException("ProGuardLint: seeds.txt not found next to mapping.txt (${seedsTxt.absolutePath}). ProGuardLint needs R8's seeds.txt output.")
        }

        val mappingLines = mapping.readLines()
        val seedsLines = seedsTxt.readLines()

        val seeds = SeedsParser.parse(seedsLines)
        MappingParser.parse(mappingLines)

        val zones = dangerZones.getOrElse(emptyList())
        val violations = DangerZoneChecker.check(seeds, zones)

        val elapsed = System.currentTimeMillis() - start

        val result = AuditResult(
            violations = violations,
            totalSeedsChecked = seeds.size,
            dangerZonesChecked = zones.size,
            elapsedMs = elapsed
        )

        logger.lifecycle(ConsoleReporter.summary(result))
        ConsoleReporter.writeJsonReport(result, reportDir.get().asFile)

        if (violations.isNotEmpty()) {
            if (failOnError.getOrElse(true)) {
                throw GradleException(ConsoleReporter.buildFailureMessage(result))
            } else {
                logger.warn("ProGuardLint: ${violations.size} violation(s) found (warning-only mode)")
            }
        }
    }

    companion object {
        const val TASK_PREFIX = "proguardLint"
    }
}

class ProguardLintPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "proguardLint",
            ProguardLintExtension::class.java
        )
        extension.failOnError.convention(true)
        extension.dangerZones.convention(emptyList())
        extension.runOnBuild.convention(true)

        val androidComponents = project.extensions.findByType(
            AndroidComponentsExtension::class.java
        ) ?: return

        androidComponents.onVariants { variant ->
            val variantName = variant.name
            if (variantName != "release") return@onVariants

            val taskName = "${ProguardLintTask.TASK_PREFIX}${variantName.replaceFirstChar { it.uppercase() }}"

            project.tasks.register(taskName, ProguardLintTask::class.java) { t ->
                t.description = "Audit ProGuard/R8 obfuscation for $variantName variant"
                t.group = "verification"

                // Wire directly into AGP's artifact system — task auto-depends on R8
                val mappingFile = variant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE)
                t.mappingFile.set(mappingFile)

                // seeds.txt sits alongside mapping.txt — derive from same parent dir
                val seedsFile = project.layout.file(
                    mappingFile.map { f -> f.asFile.parentFile.resolve("seeds.txt") }
                )
                t.seedsFile.convention(seedsFile)

                t.dangerZones.set(extension.dangerZones)
                t.failOnError.set(extension.failOnError)
                t.reportDir.set(project.layout.buildDirectory.dir("reports/proguard-lint"))
            }

            if (extension.runOnBuild.get()) {
                project.tasks.matching { it.name == "assembleRelease" }.configureEach {
                    it.dependsOn(taskName)
                }
            }
        }
    }
}
