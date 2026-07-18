package io.github.rudradave1.proguardlint.model

/**
 * A single line from seeds.txt — a class or method ProGuard was told to keep.
 * Line format: `com.foo.Bar` or `com.foo.Bar: void method()`
 */
data class SeedsEntry(
    val fullyQualifiedName: String,
    val packageName: String,
    val className: String
)
