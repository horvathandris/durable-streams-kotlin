#!/usr/bin/env bash
set -euo pipefail

echo "Refreshing dependency locks for all subprojects..."

subprojects=$(./gradlew projects --quiet | grep -oE "Project '(:[^']+)'" | sed "s/Project '\\(:[^']*\\)'/\\1/")

for project in $subprojects; do
    echo "Locking $project..."
    ./gradlew "${project}:dependencies" --write-locks
done

echo "Done."