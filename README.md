# ProGuardLint

[![CI](https://github.com/rudradave1/proguard-lint/actions/workflows/ci.yml/badge.svg)](https://github.com/rudradave1/proguard-lint/actions/workflows/ci.yml)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.rudradave1.proguardlint?color=blue&label=pending)](https://plugins.gradle.org/plugin/io.github.rudradave1.proguardlint)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

**Lightning-fast Gradle plugin that audits ProGuard/R8 obfuscation quality from `mapping.txt` and `seeds.txt`.**
No APK, no decompiler, no Docker. Runs in **<250ms** as part of your release build.

---

## Why?
No existing tool audits obfuscation output for security compliance.
Alternatives either skip it (SonarQube, Detekt, Android Lint) or require full APK decompilation (MobSF, 2-5 min).

**ProGuardLint is 1000x faster** because it reads the files R8/ProGuard already generate.

---

## Example Output
![ProGuardLint Screenshot](docs/screenshot.png)

---

## Usage
Apply the plugin in your app or library module:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.rudradave1.proguardlint") version "0.1.0"
}

proguardLint {
    dangerZones = listOf("com.mycompany.payment", "com.mycompany.auth")
    failOnError = true  // default: fail build on violations
}
```

Run the audit:

```bash
./gradlew proguardLintRelease
```

---

## GitHub Action

```yaml
- uses: rudradave1/proguard-lint@v0.1.0
  with:
    danger-zones: "com.mycompany.payment,com.mycompany.auth"
    fail-on-error: "true"
```

The action posts a formatted Markdown comment on each PR with the audit result.

---

## How It Works

1. R8/ProGuard emits `mapping.txt` and `seeds.txt` after each release build.
2. ProGuardLint reads `seeds.txt` — the list of every class ProGuard was told to keep via `-keep` rules.
3. Each kept class is checked against your configured `dangerZones`.
4. If a class in a danger zone was kept (not obfuscated), it is reported as a violation.

**No APK needed.** The check runs automatically in `proguardLintRelease`, wired via AGP’s `SingleArtifact.OBFUSCATION_MAPPING_FILE` API.

---

## License

MIT