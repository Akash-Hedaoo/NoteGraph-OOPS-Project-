#!/bin/bash
# ============================================
#  NoteGraph Java UI — Build and Run Script
#  Requires: Java 17+ (JDK)
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${SCRIPT_DIR}/src"
LIB_DIR="${SCRIPT_DIR}/lib"
OUT_DIR="${SCRIPT_DIR}/out"

echo ""
echo "============================================"
echo "  NoteGraph Desktop Client"
echo "============================================"
echo ""
echo "[1/2] Compiling Java sources..."

mkdir -p "${OUT_DIR}"

# Find all .java files and wrap paths in quotes to handle spaces in directory names
find "${SRC_DIR}" -name "*.java" | sed 's/.*/"&"/' > "${SCRIPT_DIR}/sources.txt"

# Compile
javac -d "${OUT_DIR}" -cp "${LIB_DIR}/*" @"${SCRIPT_DIR}/sources.txt"
if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Compilation failed!"
    rm -f "${SCRIPT_DIR}/sources.txt"
    exit 1
fi

rm -f "${SCRIPT_DIR}/sources.txt"

echo "[OK] Compilation successful."
echo ""
echo "[2/2] Launching NoteGraph..."
echo "      Make sure the Spring Boot backend is running on port 8080."
echo ""

java -cp "${OUT_DIR}:${LIB_DIR}/*" com.notegraph.ui.NoteGraphApp
