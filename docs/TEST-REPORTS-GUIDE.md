# Test Reports Guide

This guide explains where to find test reports locally and in GitHub Actions.

## Local Test Execution

### Running Tests Locally

#### Unit Tests Only (Recommended for Development)
```bash
# Run all unit tests (excludes integration tests)
mvn test

# Run tests with coverage report generation
mvn clean test jacoco:report
```

#### Integration Tests (Requires MySQL)
```bash
# Run only integration tests
mvn test -Dtest=*IntegrationTest

# Run all tests including integration
mvn test -Dtest="**/*Test"
```

### Where to Find Local Reports

#### 1. Surefire Test Reports (Text/XML)
**Location:** `target/surefire-reports/`

Contains:
- `TEST-*.xml` - Test execution results in XML format
- `*.txt` - Test execution summary in text format

**View:** Open `TEST-com.spark.collectibles.*.xml` files with a text editor or browser.

#### 2. JaCoCo Coverage Report (HTML)
**Location:** `target/site/jacoco/index.html`

Contains:
- Line coverage percentage
- Branch coverage percentage
- Method coverage percentage
- Class coverage percentage
- Detailed coverage by package/class

**View:** Open `target/site/jacoco/index.html` in your web browser.

#### 3. JaCoCo Coverage Data (Binary)
**Location:** `target/jacoco.exec`

Contains raw coverage data in binary format (used to generate HTML reports).

### Generating Reports Manually

If reports weren't generated automatically:

```bash
# Generate JaCoCo HTML report from existing jacoco.exec
mvn jacoco:report

# Generate both test and coverage reports
mvn clean test jacoco:report
```

## GitHub Actions Reports

### Where to Find Reports in GitHub

1. **Go to your repository on GitHub**
2. **Click on "Actions" tab**
3. **Click on the latest workflow run**
4. **Click on "Backend Tests (Java)" job**
5. **Scroll down to "Artifacts" section**

### Available Artifacts

#### 1. JaCoCo Coverage Report
**Artifact Name:** `jacoco-coverage-report`

**Location in workflow:** `target/site/jacoco/`

**Contains:**
- HTML coverage report (same as local)
- Open `index.html` to view in browser

**Download:**
1. Click on the artifact
2. Download the ZIP file
3. Extract and open `index.html` in browser

#### 2. Jest Coverage Report (Frontend)
**Artifact Name:** `jest-coverage-report`

**Location in workflow:** `coverage/`

**Contains:**
- HTML coverage report for JavaScript tests
- Open `coverage/index.html` to view in browser

#### 3. SpotBugs Report
**Artifact Name:** `spotbugs-report`

**Location in workflow:** `target/spotbugsXml.xml`

**Contains:**
- Static code analysis findings in XML format

#### 4. Javadoc
**Artifact Name:** `javadoc`

**Location in workflow:** `target/site/apidocs/`

**Contains:**
- API documentation generated from code comments

### Viewing Coverage in GitHub Actions

After a workflow run:
1. Check the "Summary" tab for test results
2. Check "Artifacts" section for downloadable reports
3. Download and open HTML reports in your browser

### Coverage Badge (Optional)

You can add a coverage badge to your README using services like:
- [Codecov](https://codecov.io/)
- [Coveralls](https://coveralls.io/)
- [Shields.io](https://shields.io/)

## Troubleshooting

### Reports Not Generated?

1. **Check if tests ran:**
   ```bash
   ls target/surefire-reports/
   ```

2. **Check if JaCoCo executed:**
   ```bash
   ls target/jacoco.exec
   ```

3. **Regenerate reports:**
   ```bash
   mvn clean test jacoco:report
   ```

### Integration Tests Failing?

Integration tests require a MySQL database. They are excluded by default in `pom.xml`.

**Solutions:**
1. Start MySQL locally
2. Run only unit tests: `mvn test`
3. Configure test database in `application.properties` or environment variables

### No Artifacts in GitHub Actions?

1. Check if workflow completed (even with failures, artifacts may be uploaded with `if: always()`)
2. Check if file paths in workflow are correct
3. Check workflow logs for upload errors

## Quick Commands Reference

```bash
# Run unit tests only
mvn test

# Run with coverage
mvn clean test jacoco:report

# View coverage report (after generation)
# Windows:
start target/site/jacoco/index.html
# Mac:
open target/site/jacoco/index.html
# Linux:
xdg-open target/site/jacoco/index.html

# Run integration tests (requires MySQL)
mvn test -Dtest=*IntegrationTest

# Skip tests
mvn clean package -DskipTests
```

