#!/bin/bash
# ============================================
#  NoteGraph Spring Boot Backend — Run Script
#  Requires: Java 17+ (JDK), Maven Wrapper
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

echo ""
echo "============================================"
echo "  NoteGraph Backend  (Spring Boot)"
echo "============================================"
echo ""
echo "Starting server on http://localhost:8080 ..."
echo ""

chmod +x mvnw
./mvnw spring-boot:run

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Backend failed to start. Check logs above."
    read -p "Press Enter to continue..."
fi
