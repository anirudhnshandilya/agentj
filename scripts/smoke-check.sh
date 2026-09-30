#!/usr/bin/env bash
set -euo pipefail
command -v java >/dev/null || { echo "Java is required"; exit 1; }
java -version
for f in pom.xml README.md LICENSE SECURITY.md CONTRIBUTING.md ARCHITECTURE.md; do test -f "$f"; done
count=$(find . -name '*.java' | wc -l)
test "$count" -ge 20
echo "AgentJ layout OK ($count Java sources)"
echo "Run: mvn test"
