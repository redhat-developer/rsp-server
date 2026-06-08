#!/bin/bash
# Build script for javac-ls with tests and increased XML entity limits
# Fixes: JAXP00010003: The length of entity "[xml]" exceeds the "100,000" limit

export MAVEN_OPTS="-Djdk.xml.maxGeneralEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 -Djdk.xml.entityExpansionLimit=0"

cd "$(dirname "$0")"

echo "Building javac-ls with tests and increased XML parser limits..."
echo "MAVEN_OPTS: $MAVEN_OPTS"
echo ""

mvn clean install "$@"
