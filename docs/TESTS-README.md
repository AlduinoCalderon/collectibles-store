# Testing Guide

This document provides comprehensive instructions for running and understanding tests in the Collectibles Store API project.

## Table of Contents

- [Overview](#overview)
- [Backend Testing](#backend-testing)
- [Frontend Testing](#frontend-testing)
- [Integration Testing](#integration-testing)
- [Code Coverage](#code-coverage)
- [Test Examples](#test-examples)
- [Troubleshooting](#troubleshooting)

## Overview

The project includes comprehensive test coverage for:
- **Backend (Java)**: Unit tests and integration tests using JUnit 5 and Mockito
- **Frontend (JavaScript)**: Unit tests using Jest
- **Code Coverage**: JaCoCo for Java, Jest coverage for JavaScript

## Backend Testing

### Running Tests

#### Run All Tests
```bash
mvn test
```

**Expected Output:**
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.spark.collectibles.service.AuthServiceTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.345 s
[INFO] Running com.spark.collectibles.service.ProductServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.234 s
[INFO] Running com.spark.collectibles.integration.AuthRoutesIntegrationTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.678 s
[INFO] Results:
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

#### Run Specific Test Class
```bash
# Run a specific test class
mvn test -Dtest=AuthServiceTest

# Run integration tests only
mvn test -Dtest=AuthRoutesIntegrationTest

# Run tests matching a pattern
mvn test -Dtest="*Auth*"
```

#### Run Tests with Coverage
```bash
mvn clean test jacoco:report
```

This will:
1. Clean previous build artifacts
2. Run all tests
3. Generate JaCoCo coverage report

**Coverage Report Location**: `target/site/jacoco/index.html`

Open the HTML file in your browser to view:
- Overall coverage percentage
- Coverage by package
- Coverage by class
- Line-by-line coverage highlighting

### Test Structure

#### Unit Tests

Unit tests are located in `src/test/java/` and follow this structure:

```
src/test/java/
├── service/
│   ├── AuthServiceTest.java
│   ├── AuthServiceTokenValidationTest.java
│   └── ProductServiceTest.java
├── routes/
│   └── ProductRoutesTest.java
└── util/
    └── AuthFilterTest.java
```

**Example Unit Test:**
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, "test-secret", 24);
    }
    
    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterUser() {
        // Test implementation
    }
}
```

#### Integration Tests

Integration tests are located in `src/test/java/integration/`:

```
src/test/java/integration/
├── AuthRoutesIntegrationTest.java
├── ProductRoutesIntegrationTest.java
└── IntegrationTestExtension.java
```

**Example Integration Test:**
```java
@ExtendWith(IntegrationTestExtension.class)
@DisplayName("AuthRoutes Integration Tests")
class AuthRoutesIntegrationTest {
    
    @Test
    @DisplayName("Should register user and return token")
    void testRegisterUser() throws IOException {
        // HTTP request to actual server
        HttpURLConnection conn = (HttpURLConnection) 
            new URL("http://localhost:4567/api/auth/register").openConnection();
        // ... test implementation
    }
}
```

### Test Categories

#### Authentication Tests

**AuthServiceTest** - Tests authentication service logic:
- User registration with validation
- User login with password verification
- JWT token generation
- Password hashing and verification
- Edge cases (duplicate users, invalid credentials)

**AuthServiceTokenValidationTest** - Tests token validation:
- Expired token handling
- Invalid signature handling
- User not found scenarios
- Inactive user scenarios

**AuthRoutesIntegrationTest** - Tests authentication endpoints:
- POST /api/auth/register
- POST /api/auth/login
- GET /api/auth/me
- Protected route access
- Error handling

#### Product Tests

**ProductServiceTest** - Tests product service logic:
- Product creation
- Product updates
- Product deletion (soft delete)
- Product search and filtering

**ProductRoutesIntegrationTest** - Tests product endpoints:
- GET /api/products
- POST /api/products (with authentication)
- PUT /api/products/:id
- DELETE /api/products/:id

## Frontend Testing

### Setup

First, install dependencies:
```bash
npm install
```

### Running Tests

#### Run All Frontend Tests
```bash
npm test
```

**Expected Output:**
```
PASS  src/test/js/auth/storage.test.js
  Storage Module
    ✓ should get token from localStorage (3 ms)
    ✓ should return null if token not found (1 ms)
    ✓ should save token to localStorage (2 ms)
    ✓ should remove token from localStorage (1 ms)

PASS  src/test/js/auth/api.test.js
  API Module
    ✓ should call login endpoint with correct data (5 ms)
    ✓ should call register endpoint with correct data (4 ms)
    ✓ should handle API errors correctly (3 ms)

PASS  src/test/js/auth/ui.test.js
  UI Module
    ✓ should update navigation for authenticated user (2 ms)
    ✓ should update navigation for unauthenticated user (1 ms)

Test Suites: 3 passed, 3 total
Tests:       12 passed, 12 total
Snapshots:   0 total
Time:        2.345 s
```

#### Run Tests in Watch Mode
```bash
npm test -- --watch
```

#### Run Tests with Coverage
```bash
npm test -- --coverage
```

**Coverage Report Location**: `coverage/lcov-report/index.html`

### Test Structure

Frontend tests are located in `src/test/js/`:

```
src/test/js/
└── auth/
    ├── api.test.js
    ├── storage.test.js
    └── ui.test.js
```

**Example Frontend Test:**
```javascript
describe('Storage Module', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    test('should get token from localStorage', () => {
        localStorage.setItem('authToken', 'test-token');
        expect(getToken()).toBe('test-token');
    });

    test('should return null if token not found', () => {
        expect(getToken()).toBeNull();
    });
});
```

## Integration Testing

### Manual Integration Testing

#### Test Registration

```bash
curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "admin@test.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "Admin",
    "role": "ADMIN"
  }'
```

**Expected Response** (201 Created):
```json
{
  "user": {
    "id": "user1",
    "username": "testadmin",
    "email": "admin@test.com",
    "firstName": "Test",
    "lastName": "Admin",
    "role": "ADMIN",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Test Login

```bash
curl -X POST http://localhost:4567/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testadmin",
    "password": "password123"
  }'
```

**Expected Response** (200 OK):
```json
{
  "user": {
    "id": "user1",
    "username": "testadmin",
    "email": "admin@test.com",
    "role": "ADMIN",
    "isActive": true
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Test Protected Route

```bash
# Save token from login response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:4567/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Test Product",
    "description": "Test description",
    "price": 99.99,
    "currency": "USD",
    "category": "Test"
  }'
```

**Expected Response** (201 Created):
```json
{
  "id": "item1",
  "name": "Test Product",
  "description": "Test description",
  "price": 99.99,
  "currency": "USD",
  "category": "Test",
  "isActive": true,
  "isDeleted": false
}
```

#### Test Without Token (Should Fail)

```bash
curl -X POST http://localhost:4567/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "Test description",
    "price": 99.99,
    "currency": "USD",
    "category": "Test"
  }'
```

**Expected Response** (401 Unauthorized):
```json
{
  "message": "Authentication required"
}
```

### Test Scenarios

#### Scenario 1: Register Duplicate Username
```bash
curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "different@test.com",
    "password": "password123"
  }'
```

**Expected**: 409 Conflict - "User registration failed. Username or email may already exist."

#### Scenario 2: Login with Wrong Password
```bash
curl -X POST http://localhost:4567/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testadmin",
    "password": "wrongpassword"
  }'
```

**Expected**: 401 Unauthorized - "Invalid username/email or password"

#### Scenario 3: Access Protected Route with Invalid Token
```bash
curl -X POST http://localhost:4567/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid-token" \
  -d '{...}'
```

**Expected**: 401 Unauthorized - "Invalid or expired token"

#### Scenario 4: Access Protected Route with Customer Role (Should Fail)
```bash
# Register as CUSTOMER
curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer",
    "email": "customer@test.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'

# Login as CUSTOMER
CUSTOMER_TOKEN="..."

# Try to create product (should fail)
curl -X POST http://localhost:4567/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{...}'
```

**Expected**: 403 Forbidden - "Insufficient permissions"

## Code Coverage

### Backend Coverage (JaCoCo)

#### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

#### View Coverage Report
Open `target/site/jacoco/index.html` in your browser.

The report shows:
- **Overall Coverage**: Percentage of code covered
- **Package Coverage**: Coverage by package
- **Class Coverage**: Coverage by class
- **Line Coverage**: Which lines are covered/uncovered

#### Coverage Goals

- **Minimum Coverage**: 70% overall
- **Critical Classes**: 90%+ (AuthService, ProductService, AuthRoutes)
- **Repository Classes**: 80%+
- **Utility Classes**: 70%+

### Frontend Coverage (Jest)

#### Generate Coverage Report
```bash
npm test -- --coverage
```

#### View Coverage Report
Open `coverage/lcov-report/index.html` in your browser.

The report shows:
- **Statement Coverage**: Percentage of statements executed
- **Branch Coverage**: Percentage of branches executed
- **Function Coverage**: Percentage of functions called
- **Line Coverage**: Percentage of lines executed

## Test Examples

### Backend Unit Test Example

```java
@Test
@DisplayName("Should hash password using BCrypt")
void testHashPassword() {
    AuthService authService = new AuthService(mockRepository, "secret", 24);
    String password = "password123";
    String hash = authService.hashPassword(password);
    
    assertNotNull(hash);
    assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    assertTrue(authService.verifyPassword(password, hash));
}
```

### Frontend Unit Test Example

```javascript
describe('API Module', () => {
    beforeEach(() => {
        global.fetch = jest.fn();
    });

    test('should call login endpoint with correct data', async () => {
        const mockResponse = {
            user: { id: 'user1', username: 'test' },
            token: 'test-token'
        };
        fetch.mockResolvedValueOnce({
            ok: true,
            json: async () => mockResponse
        });

        const result = await login('test', 'password123');

        expect(fetch).toHaveBeenCalledWith('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                usernameOrEmail: 'test',
                password: 'password123'
            })
        });
        expect(result).toEqual(mockResponse);
    });
});
```

## Troubleshooting

### Issue: Tests Fail with Database Connection Error

**Solution**: Ensure MySQL is running and database is created:
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS collectibles_store;"
```

### Issue: Integration Tests Fail

**Solution**: Ensure the application is not already running on port 4567:
```bash
# Kill any process on port 4567
lsof -ti:4567 | xargs kill -9
```

### Issue: Frontend Tests Fail with "Cannot find module"

**Solution**: Reinstall dependencies:
```bash
rm -rf node_modules package-lock.json
npm install
```

### Issue: Coverage Report Not Generated

**Solution**: Ensure tests pass first, then generate report:
```bash
mvn clean test
mvn jacoco:report
```

### Issue: Tests Timeout

**Solution**: Increase timeout in test configuration or check for hanging connections.

## Continuous Integration

Tests are automatically run in CI/CD pipeline (GitHub Actions) on:
- Every push to main branch
- Every pull request
- Scheduled health checks

See `.github/workflows/ci-cd.yml` for CI configuration.

## Best Practices

1. **Write tests before fixing bugs** - Reproduce the bug in a test first
2. **Keep tests independent** - Each test should be able to run in isolation
3. **Use descriptive test names** - Test names should describe what they test
4. **Test edge cases** - Don't just test the happy path
5. **Mock external dependencies** - Use mocks for database, network calls, etc.
6. **Maintain high coverage** - Aim for 70%+ overall coverage
7. **Run tests frequently** - Run tests before committing code
8. **Keep tests fast** - Unit tests should run in milliseconds

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Jest Documentation](https://jestjs.io/docs/getting-started)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

