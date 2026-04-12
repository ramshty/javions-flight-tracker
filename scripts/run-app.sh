#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="$ROOT_DIR/build"
CLASSES_DIR="$BUILD_DIR/classes"
SOURCES_LIST="$BUILD_DIR/sources.txt"

if [[ -z "${JAVAFX_LIB:-}" ]]; then
  cat >&2 <<'EOF'
Set JAVAFX_LIB to your JavaFX SDK lib directory before launching Javions.

Example:
  export JAVAFX_LIB="$HOME/sdk/javafx-sdk-21.0.2/lib"
EOF
  exit 1
fi

mkdir -p "$CLASSES_DIR"
find "$ROOT_DIR/src" -name '*.java' | sort > "$SOURCES_LIST"

javac \
  --module-path "$JAVAFX_LIB" \
  --add-modules javafx.controls,javafx.graphics \
  -d "$CLASSES_DIR" \
  @"$SOURCES_LIST"

cp -R "$ROOT_DIR/resources/." "$CLASSES_DIR/"

if [[ $# -eq 0 ]]; then
  set -- --sample
fi

java \
  --module-path "$JAVAFX_LIB" \
  --add-modules javafx.controls,javafx.graphics \
  -cp "$CLASSES_DIR" \
  javions.gui.Main \
  "$@"
