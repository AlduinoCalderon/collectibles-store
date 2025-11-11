# Technical Difficulties and Solutions

## Overview

This document describes the main technical difficulties encountered during the implementation of the authentication system and the strategies used to solve them. This promotes reflection and documentation of technical learning.

## 1. JWT Secret Management

### Difficulty
Initially, the JWT secret was hardcoded or used a default value that could be exposed in the codebase. This is a critical security issue, especially for production deployments.

### Solution
- Implemented environment variable-based configuration through `EnvironmentConfig`
- Created a deterministic development secret generated from "Hello World" phrase using SHA-256 hashing
- Added validation to require `JWT_SECRET` environment variable in production mode
- Ensured no secrets are hardcoded in the codebase
- All JWT operations now use `EnvironmentConfig.getJwtSecret()` which enforces proper secret management

### Learning
- Never hardcode secrets in source code
- Use environment variables for all sensitive configuration
- Provide safe defaults for development while enforcing strict requirements for production
- Use cryptographic hashing to generate deterministic but secure-looking development secrets

## 2. Password Hash Exposure in API Responses

### Difficulty
The `User` model needed to store password hashes for authentication, but these should never be returned in API responses. Initially, password hashes were being serialized in JSON responses.

### Solution
- Added `passwordHash` field to `User` model without `@SerializedName` annotation
- Created separate repository methods: regular queries exclude password hash, authentication queries include it
- Implemented `findByUsernameWithPassword()` and `findByEmailWithPassword()` methods that return password hash only for authentication
- Ensured all API responses explicitly set `passwordHash` to null before serialization
- Added validation in tests to verify password hash is never exposed

### Learning
- Separate data access concerns: authentication queries vs. regular queries
- Never trust default serialization for sensitive data
- Explicitly null out sensitive fields before API responses
- Test that sensitive data is never exposed

## 3. Mocking MySQLUserRepository for Unit Tests

### Difficulty
`AuthService` uses `MySQLUserRepository` which requires database connections. Unit tests should not depend on database infrastructure. The repository has methods like `findByUsernameWithPassword()` that are not in the interface.

### Solution
- Created a test constructor in `AuthService` that accepts `UserRepository` interface and JWT configuration
- Used Mockito to mock `MySQLUserRepository` directly in tests
- Created test-specific methods that return `Optional<User>` with password hash for authentication tests
- Separated unit tests (using mocks) from integration tests (using real database)

### Learning
- Design for testability: allow dependency injection
- Use interfaces to enable mocking
- Separate unit tests (fast, isolated) from integration tests (slower, require infrastructure)
- Mock concrete classes when necessary, but prefer interfaces

## 4. Spark Framework Filter Testing

### Difficulty
Spark's `before()` filters use `halt()` which throws exceptions to stop request processing. Testing these filters requires understanding Spark's exception handling mechanism.

### Solution
- Created `AuthFilter` class that returns Spark `Filter` instances
- Used Mockito to mock `Request` and `Response` objects
- Expected exceptions in tests when authentication fails (since `halt()` throws)
- Verified that filters call `authService.validateToken()` appropriately
- Used `assertDoesNotThrow()` for successful authentication paths

### Learning
- Understand framework-specific behavior (Spark's `halt()` throws exceptions)
- Test both success and failure paths
- Mock framework objects (`Request`, `Response`) to isolate filter logic
- Verify interactions with dependencies (e.g., `authService`)

## 5. Database Migration Ordering

### Difficulty
The users table migration (`V3__Create_users_table.sql`) needs to run after products table migration. Flyway uses version numbers, but we needed to ensure proper ordering and handle cases where the database might already exist.

### Solution
- Used Flyway versioning: `V1__Create_products_table.sql`, `V2__Insert_sample_products.sql`, `V3__Create_users_table.sql`
- Used `CREATE TABLE IF NOT EXISTS` to handle existing tables gracefully
- Ensured migration scripts are idempotent
- Tested migrations on fresh databases and existing databases

### Learning
- Use proper versioning for database migrations
- Make migrations idempotent when possible
- Test migrations on both fresh and existing databases
- Document migration dependencies

## 6. BCrypt Performance in Tests

### Difficulty
BCrypt with default 10 rounds takes ~100-200ms per hash, making unit tests slow when hashing passwords multiple times.

### Solution
- Added `BCRYPT_ROUNDS` environment variable configuration
- Used lower rounds (4) in test environment for faster execution
- Maintained production default of 10 rounds for security
- Documented the trade-off between security and performance

### Learning
- Balance test performance with security requirements
- Use environment-specific configuration for test vs. production
- Document performance considerations
- Lower security settings are acceptable in test environments, but never in production

## 7. JWT Token Validation Edge Cases

### Difficulty
JWT token validation needed to handle various edge cases: null tokens, empty tokens, tokens with "Bearer " prefix, expired tokens, tokens for non-existent users, tokens for inactive users.

### Solution
- Implemented comprehensive validation in `AuthService.validateToken()`
- Added checks for null/empty tokens before processing
- Stripped "Bearer " prefix if present
- Validated token signature and expiration
- Verified user exists and is active after token validation
- Created unit tests for all edge cases

### Learning
- Always validate input before processing
- Handle common variations (e.g., "Bearer " prefix)
- Validate not just token format, but also business rules (user exists, user is active)
- Test all edge cases systematically

## 8. Role-Based Access Control Implementation

### Difficulty
Implementing role-based access control required checking user roles after authentication, but Spark filters execute before route handlers, making it challenging to pass user context.

### Solution
- Used Spark's request attributes to store authenticated user: `request.attribute("currentUser", user)`
- Created `requireRole()` and `requireAnyRole()` filter methods
- Filters first authenticate (set user in attributes), then check roles
- Route handlers can access user via `request.attribute("currentUser")`
- Applied filters using Spark's `before()` method with path patterns

### Learning
- Use framework features (request attributes) to pass data between filters and handlers
- Chain filters: authentication first, then authorization
- Design filter API to be composable and reusable
- Document how filters interact with route handlers

## 9. Integration Test Server Lifecycle

### Difficulty
Integration tests need a running Spark server, but starting/stopping the server for each test is slow and can cause port conflicts.

### Solution
- Used `@BeforeAll` to start server once for all tests
- Used static flag to prevent multiple server starts
- Set test-specific environment variables before server start
- Used `@AfterAll` to stop server after all tests
- Created `IntegrationTestExtension` for reusable test setup

### Learning
- Optimize test execution time by sharing expensive setup
- Use static flags to prevent duplicate initialization
- Separate test configuration from production configuration
- Create reusable test infrastructure

## 10. Test Coverage Requirements

### Difficulty
Achieving 90% code coverage required testing all code paths, including error cases, edge cases, and exception handling.

### Solution
- Created comprehensive unit tests for all service methods
- Tested both success and failure paths
- Added tests for edge cases (null inputs, empty strings, invalid data)
- Used code coverage tools (JaCoCo) to identify untested code
- Created integration tests for end-to-end scenarios
- Documented test coverage goals and actual coverage achieved

### Learning
- Aim for high test coverage, but focus on meaningful tests
- Test error paths as much as success paths
- Use coverage tools to identify gaps
- Balance unit tests (fast, isolated) with integration tests (realistic, comprehensive)

## Summary

The main challenges were:
1. **Security**: Ensuring no secrets in code, no sensitive data exposure
2. **Testability**: Making code testable without database dependencies
3. **Framework Integration**: Understanding Spark's request/response lifecycle
4. **Edge Cases**: Handling all possible input variations and error conditions
5. **Performance**: Balancing test speed with security requirements

The solutions emphasized:
- Environment-based configuration
- Dependency injection for testability
- Comprehensive error handling
- Systematic testing of edge cases
- Clear separation between unit and integration tests

