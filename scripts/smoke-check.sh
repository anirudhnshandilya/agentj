#!/usr/bin/env bash
set -euo pipefail

command -v java >/dev/null || { echo "Java is required"; exit 1; }
java -version

required_files=(
  pom.xml
  README.md
  LICENSE
  SECURITY.md
  CONTRIBUTING.md
  ARCHITECTURE.md
  agentj-jev/pom.xml
  examples/hybrid-agent/pom.xml
)

for file in "${required_files[@]}"; do
  test -f "$file" || { echo "Missing required file: $file"; exit 1; }
done

count=$(find . -name '*.java' -not -path './target/*' | wc -l | tr -d ' ')
test "$count" -ge 25 || { echo "Expected at least 25 Java sources, found $count"; exit 1; }

grep -q 'agentj-jev' pom.xml || { echo "agentj-jev is missing from the reactor"; exit 1; }
grep -q 'examples/hybrid-agent' pom.xml || { echo "hybrid example is missing from the reactor"; exit 1; }

echo "AgentJ layout OK ($count Java sources)"
echo "Run: ./mvnw -B verify"
