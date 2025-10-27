#!/bin/bash

echo "Starting Collectibles Store API..."
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

echo "Building project..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo
echo "Starting application..."
echo "API will be available at: http://localhost:4567"
echo "Press Ctrl+C to stop the application"
echo

mvn exec:java -Dexec.mainClass="com.spark.collectibles.Application"
