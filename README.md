# ProGuardLint

[![CI](https://github.com/rudradave1/proguard-lint/actions/workflows/ci.yml/badge.svg)](https://github.com/rudradave1/proguard-lint/actions/workflows/ci.yml)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.rudradave1.proguardlint?color=blue)](https://plugins.gradle.org/plugin/io.github.rudradave1.proguardlint)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Audit ProGuard and R8 obfuscation quality on every release build.

Parses `mapping.txt` and `seeds.txt` — no APK, no decompiler, no Docker.
Runs in under 100ms as a Gradle plugin task.

## Why

No existing tool analyses obfuscation output for security compliance.
Alternatives either skip it entirely (SonarQube, Detekt, Android Lint) or
require full APK decompilation at 2–5 minutes per run (MobSF).

ProGuardLint reads the mapping files that R8 already produces and flags
packages that are shipping without obfuscation, in under a second.

## Usage

Apply the plugin in your app or library module:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.rudradave1.proguardlint") version "0.1.0"
}

proguardLint {
    dangerZones = listOf("com.mycompany.payment", "com.mycompany.auth")
    failOnError = true   // default: fail build on violations
}
```

Run the audit:

```bash
./gradlew proguardLintRelease
```

### Example output

```
> Task :app:proguardLintRelease FAILED

  ProGuardLint — 2 violation(s)
  ─────────────────────────────
  com.mycompany.payment.Gatekeeper       danger zone: com.mycompany.payment
  com.mycompany.auth.SessionManager      danger zone: com.mycompany.auth

  FAIL: 2 class(es) in danger zones are not obfuscated.
  Fix: Check -keep rules in proguard-rules.pro for blanket
       keep rules covering these packages.
```

A JSON report is written to `build/reports/proguard-lint/proguard-lint-report.json`
for CI consumption.

### Config reference

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `dangerZones` | `List<String>` | `[]` | Package prefixes to flag if un-obfuscated |
| `failOnError` | `Boolean` | `true` | Whether violations fail the build |

Set `failOnError = false` to run in warning-only mode during evaluation.

## GitHub Action

```yaml
- uses: rudradave1/proguard-lint@v0.1.0
  with:
    danger-zones: "com.mycompany.payment,com.mycompany.auth"
    fail-on-error: "true"
```

The action runs the audit and posts a formatted Markdown comment on each PR
with the result. It uses `${{ github.token }}` — no additional secrets needed.

## How it works

1. R8 emits `mapping.txt` and `seeds.txt` after each release build
2. ProGuardLint reads `seeds.txt` — the list of every class ProGuard was
   told to keep via `-keep` rules
3. Each kept class is checked against your configured `dangerZones`
4. If a class in a danger zone was kept (not obfuscated), it is reported
   as a violation

The check runs automatically in `proguardLintRelease`, wired via the
AGP `SingleArtifact.OBFUSCATION_MAPPING_FILE` API. No manual task
dependency is needed.

## License

MIT
