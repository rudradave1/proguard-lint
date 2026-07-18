package io.github.rudradave1.proguardlint.model

data class Violation(
    val packagePrefix: String,
    val className: String,
    val fullyQualifiedName: String
)
