# Test Runner Script with MySQL Detection and Clear Summary
# This script detects MySQL availability and runs appropriate tests

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Collectibles Store - Test Runner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is available
$mavenAvailable = $false
try {
    $null = Get-Command mvn -ErrorAction Stop
    $mavenAvailable = $true
    $mavenVersion = (mvn -version | Select-Object -First 1)
    Write-Host "[OK] Maven found: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Maven not found. Please install Maven to run tests." -ForegroundColor Red
    Write-Host ""
    Write-Host "To install Maven:" -ForegroundColor Yellow
    Write-Host "  - Download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    Write-Host "  - Or use: winget install Apache.Maven" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Check MySQL availability
$mysqlAvailable = $false
$mysqlHost = $env:DB_HOST
if (-not $mysqlHost) { $mysqlHost = "localhost" }
$mysqlPort = $env:DB_PORT
if (-not $mysqlPort) { $mysqlPort = "3306" }

Write-Host "Checking MySQL availability..." -ForegroundColor Yellow
Write-Host "  Host: $mysqlHost" -ForegroundColor Gray
Write-Host "  Port: $mysqlPort" -ForegroundColor Gray

try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connection = $tcpClient.BeginConnect($mysqlHost, $mysqlPort, $null, $null)
    $wait = $connection.AsyncWaitHandle.WaitOne(2000, $false)
    
    if ($wait) {
        $tcpClient.EndConnect($connection)
        $mysqlAvailable = $true
        $tcpClient.Close()
        Write-Host "[OK] MySQL is available at $mysqlHost`:$mysqlPort" -ForegroundColor Green
    } else {
        $tcpClient.Close()
        Write-Host "[SKIP] MySQL is NOT available at $mysqlHost`:$mysqlPort" -ForegroundColor Yellow
        Write-Host "  (This is OK for local unit tests)" -ForegroundColor Gray
    }
} catch {
    Write-Host "[SKIP] MySQL is NOT available at $mysqlHost`:$mysqlPort" -ForegroundColor Yellow
    Write-Host "  (This is OK for local unit tests)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Running Tests" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Determine which tests to run
if ($mysqlAvailable) {
    Write-Host "Running ALL tests (unit + integration)..." -ForegroundColor Green
    Write-Host "  - Unit tests [OK]" -ForegroundColor Gray
    Write-Host "  - Integration tests [OK] (MySQL available)" -ForegroundColor Gray
    Write-Host ""
    
    $testCommand = "mvn clean test -Dtest=`"**/*Test`" jacoco:report"
    $includeIntegration = $true
} else {
    Write-Host "Running UNIT tests only (integration tests excluded)..." -ForegroundColor Yellow
    Write-Host "  - Unit tests [OK]" -ForegroundColor Gray
    Write-Host "  - Integration tests [SKIP] (MySQL not available)" -ForegroundColor Gray
    Write-Host ""
    
    $testCommand = "mvn clean test jacoco:report"
    $includeIntegration = $false
}

Write-Host "Command: $testCommand" -ForegroundColor Gray
Write-Host ""

# Run tests
$startTime = Get-Date
Invoke-Expression $testCommand
$testExitCode = $LASTEXITCODE
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Parse test results
$surefireReports = Get-ChildItem -Path "target\surefire-reports" -Filter "*.txt" -ErrorAction SilentlyContinue

$totalTests = 0
$totalFailures = 0
$totalErrors = 0
$totalSkipped = 0
$testClasses = @()

if ($surefireReports) {
    foreach ($report in $surefireReports) {
        $content = Get-Content $report.FullName -Raw
        
        if ($content -match "Tests run:\s+(\d+),\s+Failures:\s+(\d+),\s+Errors:\s+(\d+),\s+Skipped:\s+(\d+)") {
            $tests = [int]$matches[1]
            $failures = [int]$matches[2]
            $errors = [int]$matches[3]
            $skipped = [int]$matches[4]
            
            $totalTests += $tests
            $totalFailures += $failures
            $totalErrors += $errors
            $totalSkipped += $skipped
            
            $testName = $report.BaseName -replace "com\.spark\.collectibles\.", ""
            $testClasses += @{
                Name = $testName
                Tests = $tests
                Failures = $failures
                Errors = $errors
                Skipped = $skipped
            }
        }
    }
}

# Display summary
Write-Host "Test Execution:" -ForegroundColor White
Write-Host "  Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor Gray
if ($testExitCode -eq 0) {
    Write-Host "  Exit Code: $testExitCode" -ForegroundColor Green
} else {
    Write-Host "  Exit Code: $testExitCode" -ForegroundColor Red
}
Write-Host ""

if ($totalTests -gt 0) {
    Write-Host "Results:" -ForegroundColor White
    if ($totalFailures -eq 0 -and $totalErrors -eq 0) {
        Write-Host "  Total Tests: $totalTests" -ForegroundColor Green
    } else {
        Write-Host "  Total Tests: $totalTests" -ForegroundColor Yellow
    }
    
    if ($totalFailures -gt 0) {
        Write-Host "  Failures: $totalFailures" -ForegroundColor Red
    }
    if ($totalErrors -gt 0) {
        Write-Host "  Errors: $totalErrors" -ForegroundColor Red
    }
    if ($totalSkipped -gt 0) {
        Write-Host "  Skipped: $totalSkipped" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Test Classes:" -ForegroundColor White
    foreach ($testClass in $testClasses) {
        if ($testClass.Failures -eq 0 -and $testClass.Errors -eq 0) {
            $status = "[PASS]"
            $color = "Green"
        } else {
            $status = "[FAIL]"
            $color = "Red"
        }
        Write-Host "  $status $($testClass.Name): $($testClass.Tests) tests" -ForegroundColor $color
        if ($testClass.Failures -gt 0) {
            Write-Host "      Failures: $($testClass.Failures)" -ForegroundColor Red
        }
        if ($testClass.Errors -gt 0) {
            Write-Host "      Errors: $($testClass.Errors)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "[WARNING] No test results found" -ForegroundColor Yellow
    Write-Host "  Check target/surefire-reports/ for detailed output" -ForegroundColor Gray
}

Write-Host ""

# Integration tests info
if (-not $includeIntegration) {
    Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow
    Write-Host "  Integration Tests Not Run" -ForegroundColor Yellow
    Write-Host "═══════════════════════════════════════" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Integration tests were excluded because MySQL is not available." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Integration tests that were skipped:" -ForegroundColor White
    Write-Host "  - UserServiceIntegrationTest" -ForegroundColor Gray
    Write-Host "  - AuthRoutesIntegrationTest" -ForegroundColor Gray
    Write-Host "  - ProductRoutesIntegrationTest" -ForegroundColor Gray
    Write-Host "  - (Any other *IntegrationTest classes)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "To run integration tests locally:" -ForegroundColor White
    Write-Host "  1. Start MySQL on $mysqlHost`:$mysqlPort" -ForegroundColor Cyan
    Write-Host "  2. Set environment variables:" -ForegroundColor Cyan
    Write-Host "     - DB_HOST=$mysqlHost" -ForegroundColor Gray
    Write-Host "     - DB_PORT=$mysqlPort" -ForegroundColor Gray
    Write-Host "     - DB_NAME=collectibles_store_test" -ForegroundColor Gray
    Write-Host "     - DB_USERNAME=root" -ForegroundColor Gray
    Write-Host "     - DB_PASSWORD=your_password" -ForegroundColor Gray
    Write-Host "  3. Run: mvn test -Dtest=`"**/*Test`"" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Note: In GitHub Actions CI, integration tests run automatically" -ForegroundColor Gray
    Write-Host "      because MySQL is provided as a service." -ForegroundColor Gray
    Write-Host ""
}

# Coverage report info
$jacocoReport = "target\site\jacoco\index.html"
if (Test-Path $jacocoReport) {
    Write-Host "═══════════════════════════════════════" -ForegroundColor Green
    Write-Host "  Coverage Report Generated" -ForegroundColor Green
    Write-Host "═══════════════════════════════════════" -ForegroundColor Green
    Write-Host ""
    Write-Host "Coverage report available at:" -ForegroundColor White
    Write-Host "  $((Get-Item $jacocoReport).FullName)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "To view the report:" -ForegroundColor White
    Write-Host "  start target\site\jacoco\index.html" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "[WARNING] Coverage report not generated" -ForegroundColor Yellow
    Write-Host "  This may happen if no tests were executed" -ForegroundColor Gray
    Write-Host ""
}

# Final status
Write-Host "========================================" -ForegroundColor Cyan
if ($testExitCode -eq 0) {
    Write-Host "  [SUCCESS] Tests Completed Successfully" -ForegroundColor Green
} else {
    Write-Host "  [FAILURE] Some Tests Failed" -ForegroundColor Red
    Write-Host "  Check target/surefire-reports/ for details" -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

exit $testExitCode

