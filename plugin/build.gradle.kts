plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.3.1"
    kotlin("jvm") version "2.0.21"
}

gradlePlugin {
    website.set("https://github.com/rudradave1/proguard-lint")
    vcsUrl.set("https://github.com/rudradave1/proguard-lint.git")

    plugins {
        create("proguardlint") {
            id = "io.github.rudradave1.proguardlint"
            implementationClass = "io.github.rudradave1.proguardlint.ProguardLintPlugin"
            version = "0.1.0"
            displayName = "ProGuardLint"
            description = "Lightning-fast Gradle plugin that audits ProGuard/R8 obfuscation quality from mapping.txt and seeds.txt"
            tags.set(listOf("android", "proguard", "r8", "obfuscation", "security", "lint"))
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.0.0")
    testImplementation(kotlin("test"))  // <-- Remove kotlin-test-junit5
}

kotlin {
    jvmToolchain(17)
}