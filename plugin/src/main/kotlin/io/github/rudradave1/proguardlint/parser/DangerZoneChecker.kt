package io.github.rudradave1.proguardlint.parser

import io.github.rudradave1.proguardlint.model.Violation
import io.github.rudradave1.proguardlint.model.SeedsEntry

/**
 * Checks a list of seeds entries against user-configured danger zones.
 *
 * A violation occurs when any class in seeds.txt has a package prefix
 * matching a user-specified danger zone — meaning that package ships
 * un-obfuscated in the APK due to a blanket -keep rule.
 */
object DangerZoneChecker {

    fun check(
        seeds: List<SeedsEntry>,
        dangerZones: List<String>
    ): List<Violation> {
        if (dangerZones.isEmpty()) return emptyList()

        return seeds
            .filter { seedsEntry ->
                dangerZones.any { zone ->
                    seedsEntry.packageName == zone ||
                    seedsEntry.packageName.startsWith("$zone.")
                }
            }
            .map { entry ->
                Violation(
                    packagePrefix = dangerZones.first { entry.packageName == it || entry.packageName.startsWith("$it.") },
                    className = entry.className,
                    fullyQualifiedName = entry.fullyQualifiedName
                )
            }
    }
}
