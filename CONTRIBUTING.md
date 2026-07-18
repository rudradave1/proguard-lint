# Contributing to ProGuardLint

## Development

```bash
./gradlew plugin:test
```

All tests use mock file fixtures. No Android SDK or real project needed.

## Project structure

```
plugin/src/main/kotlin/.../proguardlint/
├── ProguardLintPlugin.kt   # Plugin entry, extension, task
├── parser/                  # seeds.txt and mapping.txt parsing
├── model/                   # Data classes
└── reporter/                # Console + JSON output
```

## Code style

- Follow Kotlin coding conventions
- Keep parser functions pure — no side effects, return results
- No external dependencies beyond stdlib + compileOnly AGP
- Test violations use descriptive `assertThat(...)` messages

## Testing

Run tests before opening a PR:

```bash
./gradlew plugin:test
```

Add tests for new parsing rules or reporter output.

## Pull request process

1. Open an issue first describing the change
2. Link the PR to the issue
3. Ensure CI passes
