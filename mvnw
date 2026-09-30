#!/usr/bin/env sh
set -eu
MAVEN_VERSION="3.9.16"
ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
MAVEN_HOME="$ROOT_DIR/.mvn/apache-maven-$MAVEN_VERSION"
MAVEN_TGZ="$ROOT_DIR/.mvn/apache-maven-$MAVEN_VERSION-bin.tar.gz"
if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  echo "Downloading Apache Maven $MAVEN_VERSION..."
  curl -fsSL "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz" -o "$MAVEN_TGZ"
  tar -xzf "$MAVEN_TGZ" -C "$ROOT_DIR/.mvn"
  rm -f "$MAVEN_TGZ"
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
