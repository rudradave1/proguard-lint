#!/bin/bash
set -eu

REPORT_FILE="build/reports/proguard-lint/proguard-lint-report.json"

if [ ! -f "$REPORT_FILE" ]; then
  echo "No report file found at $REPORT_FILE -- skipping PR comment"
  exit 0
fi

PASSED=$(jq -r '.passed' "$REPORT_FILE")
VIOLATIONS=$(jq -r '.violationCount' "$REPORT_FILE")
SEEDS=$(jq -r '.totalSeedsChecked' "$REPORT_FILE")
ZONES=$(jq -r '.dangerZonesChecked' "$REPORT_FILE")
DURATION=$(jq -r '.elapsedMs' "$REPORT_FILE")

build_body() {
  if [ "$PASSED" = "true" ]; then
    echo "## :lock: ProGuardLint Audit"
    echo ""
    echo "**All danger zones properly obfuscated.**"
    echo ""
    echo "| Metric | Value |"
    echo "|--------|-------|"
    echo "| Seeds checked | ${SEEDS} |"
    echo "| Danger zones | ${ZONES} |"
    echo "| Duration | ${DURATION}ms |"
  else
    echo "## :lock: ProGuardLint Audit"
    echo ""
    echo "**${VIOLATIONS} un-obfuscated class(es) found in danger zones.**"
    echo ""
    while IFS= read -r line; do
      echo "- ${line}"
    done < <(jq -r '.violations[] | "`\(.fullyQualifiedName)` -- matches danger zone `\(.packagePrefix)`"' "$REPORT_FILE")
    echo ""
    echo "| Metric | Value |"
    echo "|--------|-------|"
    echo "| Seeds checked | ${SEEDS} |"
    echo "| Danger zones | ${ZONES} |"
    echo "| Violations | ${VIOLATIONS} |"
    echo "| Duration | ${DURATION}ms |"
  fi
}

BODY=$(build_body)

COMMENT_ID=$(gh api "/repos/${GITHUB_REPOSITORY}/issues/${GITHUB_EVENT_NUMBER}/comments" \
  --jq '.[] | select(.body | startswith("## :lock: ProGuardLint Audit")) | .id' | head -1)

if [ -n "$COMMENT_ID" ]; then
  gh api "/repos/${GITHUB_REPOSITORY}/issues/comments/${COMMENT_ID}" \
    -X PATCH \
    -f body="$BODY" > /dev/null
  echo "Updated existing PR comment (id: $COMMENT_ID)"
else
  gh api "/repos/${GITHUB_REPOSITORY}/issues/${GITHUB_EVENT_NUMBER}/comments" \
    -X POST \
    -f body="$BODY" > /dev/null
  echo "Posted new PR comment"
fi
