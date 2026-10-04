package io.github.rudradave1.proguardlint

import io.github.rudradave1.proguardlint.parser.DangerZoneChecker
import io.github.rudradave1.proguardlint.parser.MappingParser
import io.github.rudradave1.proguardlint.parser.SeedsParser

fun main() {
    val classCount = 10_000
    val zoneCount = 10
    val mappingLines = (0 until classCount).map { i ->
        val pkg = if (i % 50 == 0) "com.mycompany.payment" else "com.example.pkg${i % 100}"
        "$pkg.Class$i -> a.b.${(i % 52).toChar()}${i / 52}:"
    }
    val seedsLines = (0 until classCount).map { i ->
        val pkg = if (i % 50 == 0) "com.mycompany.payment" else "com.example.pkg${i % 100}"
        "$pkg.Class$i"
    }
    val zones = (0 until zoneCount).map { "com.mycompany.payment" } + listOf("com.sensitive")

    val runs = 20
    val times = (1..runs).map {
        val start = System.nanoTime()
        val seeds = SeedsParser.parse(seedsLines)
        MappingParser.parse(mappingLines)
        DangerZoneChecker.check(seeds, zones)
        (System.nanoTime() - start) / 1_000_000
    }
    val mean = times.average()
    val sorted = times.sorted()
    val p95 = sorted[(sorted.size * 0.95).toInt()]
    println("ProGuardLint audit benchmark ($classCount classes, $zoneCount danger zones, $runs runs)")
    println("mean: ${"%.1f".format(mean)}ms  p95: ${p95}ms  min: ${sorted.first()}ms  max: ${sorted.last()}ms")
}
