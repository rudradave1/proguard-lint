# Development guide

## Build

```bash
./gradlew plugin:test
```

## Release

1. Tag the release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

2. The release workflow publishes to the Gradle Plugin Portal and
   creates a GitHub Release.

### Prerequisites

Set the following secrets in the GitHub repository:

- `GRADLE_PUBLISH_KEY`
- `GRADLE_PUBLISH_SECRET`

These are generated from the [Gradle Plugin Portal](https://plugins.gradle.org).

## Project structure

```
proguard-lint/
├── plugin/                    # Gradle plugin module
│   └── src/
│       ├── main/kotlin/.../   # Source: plugin, parser, reporter
│       └── test/              # Unit tests (mock file fixtures)
├── action/                    # GitHub Action
│   ├── action.yml
│   └── comment.sh
├── .github/workflows/
│   ├── ci.yml                 # Test on push/PR
│   └── release.yml            # Publish on tag
└── README.md
```
