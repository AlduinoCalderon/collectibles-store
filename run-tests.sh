#!/bin/bash
# Test Runner Script with MySQL Detection and Clear Summary
# This script detects MySQL availability and runs appropriate tests

echo "========================================"
echo "  Collectibles Store - Test Runner"
echo "========================================"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Maven not found. Please install Maven to run tests."
    echo ""
    echo "To install Maven:"
    echo "  - Download from: https://maven.apache.org/download.cgi"
    echo "  - Or use: brew install maven (macOS)"
    echo "  - Or use: apt-get install maven (Ubuntu/Debian)"
    exit 1
fi

MAVEN_VERSION=$(mvn -version | head -n 1)
echo "[OK] Maven found: $MAVEN_VERSION"
echo ""

# Check MySQL availability
MYSQL_AVAILABLE=false
MYSQL_HOST=${DB_HOST:-localhost}
MYSQL_PORT=${DB_PORT:-3306}

echo "Checking MySQL availability..."
echo "  Host: $MYSQL_HOST"
echo "  Port: $MYSQL_PORT"

if command -v nc &> /dev/null; then
    # Use netcat if available
    if nc -z -w2 "$MYSQL_HOST" "$MYSQL_PORT" 2>/dev/null; then
        MYSQL_AVAILABLE=true
        echo "[OK] MySQL is available at $MYSQL_HOST:$MYSQL_PORT"
    else
        echo "[SKIP] MySQL is NOT available at $MYSQL_HOST:$MYSQL_PORT"
        echo "  (This is OK for local unit tests)"
    fi
elif command -v timeout &> /dev/null; then
    # Use timeout with bash's built-in TCP connection
    if timeout 2 bash -c "cat < /dev/null > /dev/tcp/$MYSQL_HOST/$MYSQL_PORT" 2>/dev/null; then
        MYSQL_AVAILABLE=true
        echo "[OK] MySQL is available at $MYSQL_HOST:$MYSQL_PORT"
    else
        echo "[SKIP] MySQL is NOT available at $MYSQL_HOST:$MYSQL_PORT"
        echo "  (This is OK for local unit tests)"
    fi
else
    # Fallback: try to connect using a simple test
    echo "[SKIP] Cannot check MySQL availability (nc or timeout not available)"
    echo "  Assuming MySQL is not available for safety"
fi

echo ""
echo "========================================"
echo "  Running Tests"
echo "========================================"
echo ""

# Determine which tests to run
if [ "$MYSQL_AVAILABLE" = true ]; then
    echo "Running ALL tests (unit + integration)..."
    echo "  - Unit tests [OK]"
    echo "  - Integration tests [OK] (MySQL available)"
    echo ""
    
    TEST_COMMAND="mvn clean test -Dtest=\"**/*Test\" jacoco:report"
    INCLUDE_INTEGRATION=true
else
    echo "Running UNIT tests only (integration tests excluded)..."
    echo "  - Unit tests [OK]"
    echo "  - Integration tests [SKIP] (MySQL not available)"
    echo ""
    
    TEST_COMMAND="mvn clean test jacoco:report"
    INCLUDE_INTEGRATION=false
fi

echo "Command: $TEST_COMMAND"
echo ""

# Run tests
START_TIME=$(date +%s)
eval $TEST_COMMAND
TEST_EXIT_CODE=$?
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "========================================"
echo "  Test Summary"
echo "========================================"
echo ""

# Parse test results
SUREFIRE_REPORTS_DIR="target/surefire-reports"
TOTAL_TESTS=0
TOTAL_FAILURES=0
TOTAL_ERRORS=0
TOTAL_SKIPPED=0

if [ -d "$SUREFIRE_REPORTS_DIR" ]; then
    while IFS= read -r -d '' report; do
        if [ -f "$report" ]; then
            # Extract test results from .txt files
            if RESULTS=$(grep -E "Tests run:\s*[0-9]+,\s*Failures:\s*[0-9]+,\s*Errors:\s*[0-9]+,\s*Skipped:\s*[0-9]+" "$report" 2>/dev/null); then
                TESTS=$(echo "$RESULTS" | sed -E 's/.*Tests run:\s*([0-9]+).*/\1/')
                FAILURES=$(echo "$RESULTS" | sed -E 's/.*Failures:\s*([0-9]+).*/\1/')
                ERRORS=$(echo "$RESULTS" | sed -E 's/.*Errors:\s*([0-9]+).*/\1/')
                SKIPPED=$(echo "$RESULTS" | sed -E 's/.*Skipped:\s*([0-9]+).*/\1/')
                
                TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
                TOTAL_FAILURES=$((TOTAL_FAILURES + FAILURES))
                TOTAL_ERRORS=$((TOTAL_ERRORS + ERRORS))
                TOTAL_SKIPPED=$((TOTAL_SKIPPED + SKIPPED))
            fi
        fi
    done < <(find "$SUREFIRE_REPORTS_DIR" -name "*.txt" -print0 2>/dev/null)
fi

# Display summary
echo "Test Execution:"
echo "  Duration: ${DURATION} seconds"
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "  Exit Code: $TEST_EXIT_CODE"
else
    echo "  Exit Code: $TEST_EXIT_CODE"
fi
echo ""

if [ $TOTAL_TESTS -gt 0 ]; then
    echo "Results:"
    if [ $TOTAL_FAILURES -eq 0 ] && [ $TOTAL_ERRORS -eq 0 ]; then
        echo "  Total Tests: $TOTAL_TESTS"
    else
        echo "  Total Tests: $TOTAL_TESTS"
    fi
    
    if [ $TOTAL_FAILURES -gt 0 ]; then
        echo "  Failures: $TOTAL_FAILURES"
    fi
    if [ $TOTAL_ERRORS -gt 0 ]; then
        echo "  Errors: $TOTAL_ERRORS"
    fi
    if [ $TOTAL_SKIPPED -gt 0 ]; then
        echo "  Skipped: $TOTAL_SKIPPED"
    fi
else
    echo "[WARNING] No test results found"
    echo "  Check target/surefire-reports/ for detailed output"
fi

echo ""

# Integration tests info
if [ "$INCLUDE_INTEGRATION" = false ]; then
    echo "========================================"
    echo "  Integration Tests Not Run"
    echo "========================================"
    echo ""
    echo "Integration tests were excluded because MySQL is not available."
    echo ""
    echo "Integration tests that were skipped:"
    echo "  - UserServiceIntegrationTest"
    echo "  - AuthRoutesIntegrationTest"
    echo "  - ProductRoutesIntegrationTest"
    echo "  - (Any other *IntegrationTest classes)"
    echo ""
    echo "To run integration tests locally:"
    echo "  1. Start MySQL on $MYSQL_HOST:$MYSQL_PORT"
    echo "  2. Set environment variables:"
    echo "     - export DB_HOST=$MYSQL_HOST"
    echo "     - export DB_PORT=$MYSQL_PORT"
    echo "     - export DB_NAME=collectibles_store_test"
    echo "     - export DB_USERNAME=root"
    echo "     - export DB_PASSWORD=your_password"
    echo "  3. Run: mvn test -Dtest=\"**/*Test\""
    echo ""
fi

# Coverage report info
JACOCO_REPORT="target/site/jacoco/index.html"
if [ -f "$JACOCO_REPORT" ]; then
    echo "========================================"
    echo "  Coverage Report Generated"
    echo "========================================"
    echo ""
    echo "Coverage report available at:"
    echo "  $(readlink -f "$JACOCO_REPORT" 2>/dev/null || echo "$(pwd)/$JACOCO_REPORT")"
    echo ""
    echo "To view the report:"
    echo "  - Linux: xdg-open target/site/jacoco/index.html"
    echo "  - macOS: open target/site/jacoco/index.html"
    echo ""
else
    echo "[WARNING] Coverage report not generated"
    echo "  This may happen if no tests were executed"
    echo ""
fi

# Final status
echo "========================================"
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "  [SUCCESS] Tests Completed Successfully"
else
    echo "  [FAILURE] Some Tests Failed"
    echo "  Check target/surefire-reports/ for details"
fi
echo "========================================"
echo ""

exit $TEST_EXIT_CODE

