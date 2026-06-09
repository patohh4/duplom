#!/bin/bash

# MindDoc Application Launcher
# This script builds and runs the MindDoc application

echo "🧠 MindDoc - Mental Health Support Application"
echo "=============================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F\" '/version/ {print $2}')
echo "✓ Java version detected: $JAVA_VERSION"

# Clean and build
echo ""
echo "Building MindDoc..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "✓ Build successful!"
echo ""
echo "Launching MindDoc..."

# Run JavaFX app with Maven plugin (ensures JavaFX modules are on module-path)
mvn -q javafx:run

echo ""
echo "Thank you for using MindDoc!"
