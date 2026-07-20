# ProGuardLint

[![CI](https://github.com/rudradave1/proguard-lint/actions/workflows/ci.yml/badge.svg)](https://github.com/rudradave1/proguard-lint/actions/workflows/ci.yml)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.rudradave1.proguardlint?color=blue)](https://plugins.gradle.org/plugin/io.github.rudradave1.proguardlint)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Lightning fast Gradle plugin that audits ProGuard/R8 obfuscation quality from mapping.txt and seeds.txt.
No APK, no decompiler, no Docker. Runs in <250ms as part of your release build.

## Why?
Most tools analyze obfuscation by decompiling the APK, which is slow (2-5 minutes) and heavy.
ProGuardLint is different. It reads the mapping files that R8 already produces. It flags packages that are shipping without obfuscation in under a second.

## Example Output
![ProGuardLint Screenshot](docs/screenshot.png)

## Usage
Apply the plugin in your app or library module:

```kotlin
plugins {
    id("io.github.rudradave1.proguardlint") version "0.1.0"
}

proguardLint {
    dangerZones = listOf("com.mycompany.payment", "com.mycompany.auth")
    failOnError = true
}
```

Run the audit:
```bash
./gradlew proguardLintRelease
```

## GitHub Action
```yaml
- uses: rudradave1/proguard-lint@v0.1.0
  with:
    danger-zones: "com.mycompany.payment,com.mycompany.auth"
    fail-on-error: "true"
```
The action posts a comment on your pull request with the audit result.

## How It Works
1. R8 emits mapping.txt and seeds.txt after release builds.
2. ProGuardLint reads seeds.txt (the list of classes kept by your -keep rules).
3. If a class in a danger zone was kept, it flags a violation.

No APK is needed. The check runs automatically in proguardLintRelease.

## License
MIT