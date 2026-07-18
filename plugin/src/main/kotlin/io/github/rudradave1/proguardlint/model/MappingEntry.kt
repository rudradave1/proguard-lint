package io.github.rudradave1.proguardlint.model

/**
 * Single entry from mapping.txt — maps original symbol to obfuscated name.
 * Line format: `com.foo.Bar -> a.a.a:`
 */
data class MappingEntry(
    val originalName: String,
    val obfuscatedName: String,
    val isClassMember: Boolean = false
)
