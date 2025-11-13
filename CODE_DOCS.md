# Code Documentation Guidelines

This document outlines the documentation standards for the Collectibles Store API project.

## Table of Contents

- [Overview](#overview)
- [Java Documentation (Javadoc)](#java-documentation-javadoc)
- [JavaScript Documentation (JSDoc)](#javascript-documentation-jsdoc)
- [Generating Documentation](#generating-documentation)
- [Best Practices](#best-practices)

## Overview

All public APIs, classes, and methods should be documented. Documentation helps:
- Understand code purpose and usage
- Generate API documentation automatically
- Improve code maintainability
- Onboard new developers

## Java Documentation (Javadoc)

### When to Document

- **Always document**: Public classes, public methods, public fields
- **Consider documenting**: Protected methods, complex private methods
- **Don't document**: Obvious getters/setters, simple private methods

### Javadoc Format

```java
/**
 * Brief description (one sentence).
 * 
 * Detailed description if needed (multiple sentences).
 * Can include paragraphs, lists, and code examples.
 * 
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType Description of when exception is thrown
 * @since Version when added
 * @author Author name (optional)
 * @see RelatedClass#method()
 */
```

### Class Documentation

```java
/**
 * Service for handling user authentication and authorization
 * 
 * This service provides methods for user registration, login, JWT token generation,
 * and password hashing using BCrypt. All passwords are hashed before storage
 * and never stored in plaintext.
 * 
 * @author Your Name
 * @since 1.0.0
 */
public class AuthService {
    // ...
}
```

### Method Documentation

```java
/**
 * Register a new user
 * 
 * Validates input, checks for duplicate username/email, hashes the password
 * using BCrypt, creates the user in the database, and generates a JWT token.
 * 
 * Password requirements:
 * - Minimum 6 characters
 * - Will be hashed using BCrypt (10 rounds by default)
 * 
 * @param username Username (must be unique, non-empty)
 * @param email Email address (must be unique, valid format)
 * @param password Plain text password (minimum 6 characters)
 * @param firstName First name (optional, can be empty)
 * @param lastName Last name (optional, can be empty)
 * @param role User role (defaults to CUSTOMER if null)
 * @return AuthResult containing user and JWT token, or null if registration fails
 *         (e.g., duplicate username/email, invalid input)
 * @throws IllegalArgumentException if username or email format is invalid
 */
public AuthResult register(String username, String email, String password, 
                          String firstName, String lastName, User.UserRole role) {
    // Implementation
}
```

### Field Documentation

```java
/**
 * JWT algorithm for token generation and validation
 * 
 * Uses HMAC256 with secret from environment configuration.
 */
private final Algorithm jwtAlgorithm;
```

### Parameter Documentation

- Use `@param` for all parameters
- Describe what the parameter is, not just its type
- Mention constraints, defaults, or special values

```java
/**
 * @param token JWT token string (with or without "Bearer " prefix)
 * @param maxAge Maximum age in seconds (0 = no limit, -1 = use default)
 */
```

### Return Value Documentation

- Use `@return` to describe what the method returns
- Mention special cases (null, empty, etc.)
- Describe the structure if returning complex objects

```java
/**
 * @return User object if token is valid and user is active, null otherwise
 *         (invalid signature, expired, user not found, or inactive)
 */
```

### Exception Documentation

- Use `@throws` for all checked exceptions
- Use `@throws` for important unchecked exceptions
- Describe when the exception is thrown

```java
/**
 * @throws ProductNotFoundException if product with given ID does not exist
 * @throws DatabaseException if database operation fails
 * @throws IllegalArgumentException if product data is invalid
 */
```

### Code Examples

Include examples in documentation when helpful:

```java
/**
 * Create a new product
 * 
 * Example:
 * <pre>{@code
 * Product product = new Product();
 * product.setName("Guitar");
 * product.setPrice(500.00);
 * Product created = productService.createProduct(product, currentUser);
 * }</pre>
 * 
 * @param product Product to create
 * @return Created product with generated ID
 */
```

### Inline Comments

Use inline comments for:
- Complex algorithms
- Non-obvious business logic
- Workarounds for bugs
- Performance optimizations

```java
// Use BCrypt with 10 rounds for balance between security and performance
// (each additional round doubles computation time)
int rounds = EnvironmentConfig.getBcryptRounds();
```

## JavaScript Documentation (JSDoc)

### When to Document

- **Always document**: Exported functions, classes, modules
- **Consider documenting**: Complex internal functions
- **Don't document**: Obvious utility functions, simple getters

### JSDoc Format

```javascript
/**
 * Brief description (one sentence).
 * 
 * Detailed description if needed (multiple sentences).
 * 
 * @param {Type} paramName - Description of parameter
 * @returns {Type} Description of return value
 * @throws {ErrorType} Description of when error is thrown
 * @example
 * // Code example
 * const result = functionName(param);
 */
```

### Function Documentation

```javascript
/**
 * Register a new user
 * 
 * Sends registration request to API, validates response, and stores
 * authentication token and user data in localStorage.
 * 
 * @param {string} username - Username (must be unique, non-empty)
 * @param {string} email - Email address (must be unique, valid format)
 * @param {string} password - Password (minimum 6 characters)
 * @param {string} [firstName=''] - First name (optional)
 * @param {string} [lastName=''] - Last name (optional)
 * @param {string} [role='CUSTOMER'] - User role (optional, defaults to CUSTOMER)
 * @returns {Promise<object>} Response containing user and token
 * @throws {Error} If registration fails (duplicate username/email, invalid input)
 * 
 * @example
 * try {
 *   const result = await register('john', 'john@example.com', 'password123');
 *   console.log('User registered:', result.user.username);
 * } catch (error) {
 *   console.error('Registration failed:', error.message);
 * }
 */
export async function register(username, email, password, firstName = '', lastName = '', role = 'CUSTOMER') {
    // Implementation
}
```

### Type Annotations

Use JSDoc type annotations:

```javascript
/**
 * @param {string} url - Request URL
 * @param {object} options - Fetch options
 * @param {string} [options.method='GET'] - HTTP method
 * @param {object} [options.headers={}] - Request headers
 * @param {object} [options.body] - Request body
 * @returns {Promise<Response>} Fetch response
 */
```

### Module Documentation

```javascript
/**
 * Storage utilities for authentication
 * 
 * Handles localStorage operations for authentication tokens and user data.
 * All functions are pure and do not have side effects beyond localStorage.
 * 
 * @module auth/storage
 */
```

### Class Documentation

```javascript
/**
 * Represents a user in the system
 * 
 * @class
 * @property {string} id - User ID
 * @property {string} username - Username
 * @property {string} email - Email address
 * @property {string} role - User role (ADMIN, CUSTOMER)
 */
class User {
    /**
     * Create a new User instance
     * 
     * @param {object} data - User data
     * @param {string} data.id - User ID
     * @param {string} data.username - Username
     * @param {string} data.email - Email address
     */
    constructor(data) {
        // Implementation
    }
}
```

## Generating Documentation

### Java (Javadoc)

Generate Javadoc:

```bash
mvn javadoc:javadoc
```

View documentation:
- Open `target/site/apidocs/index.html` in your browser

### JavaScript (JSDoc)

If using JSDoc tool:

```bash
npm install -g jsdoc
jsdoc src/main/resources/static/js -d docs/jsdoc
```

## Best Practices

### Do's

✅ **Be concise but complete**
- One sentence summary
- Detailed description when needed
- Include examples for complex APIs

✅ **Document behavior, not implementation**
- Focus on what the method does, not how
- Document side effects
- Document preconditions and postconditions

✅ **Keep documentation up to date**
- Update docs when code changes
- Remove outdated documentation
- Review docs during code review

✅ **Use consistent style**
- Follow project conventions
- Use same terminology throughout
- Be consistent with formatting

### Don'ts

❌ **Don't state the obvious**
```java
// Bad
/**
 * Gets the username
 * @return the username
 */
public String getUsername() { ... }

// Good
/**
 * Get the username
 * @return Username of the user
 */
public String getUsername() { ... }
```

❌ **Don't copy method signature**
```java
// Bad
/**
 * @param username the username
 */

// Good
/**
 * @param username Username (must be unique, 3-50 characters)
 */
```

❌ **Don't use vague descriptions**
```java
// Bad
/**
 * Processes the data
 */

// Good
/**
 * Validates and normalizes product data, then saves to database
 */
```

### Documentation Checklist

Before submitting code:

- [ ] All public classes have class-level Javadoc/JSDoc
- [ ] All public methods have method-level documentation
- [ ] All parameters documented with `@param`
- [ ] Return values documented with `@return`
- [ ] Exceptions documented with `@throws`
- [ ] Complex logic has inline comments
- [ ] Examples included for non-trivial APIs
- [ ] Documentation is accurate and up-to-date

## Examples

### Complete Java Example

```java
/**
 * Service for managing product operations
 * 
 * Provides business logic for product CRUD operations, validation,
 * and authorization checks. All product modifications require ADMIN role.
 * 
 * @author Your Name
 * @since 1.0.0
 */
public class ProductService {
    private final ProductRepository productRepository;
    
    /**
     * Create a new product
     * 
     * Validates product data, checks user permissions, generates product ID,
     * and saves to database. Only ADMIN users can create products.
     * 
     * @param product Product to create (must have name, description, price)
     * @param user User creating the product (must be ADMIN)
     * @return Created product with generated ID and timestamps
     * @throws ProductValidationException if product data is invalid
     * @throws UnauthorizedException if user is not ADMIN
     * @throws DatabaseException if database operation fails
     * 
     * @since 1.0.0
     */
    public Product createProduct(Product product, User user) {
        // Implementation
    }
}
```

### Complete JavaScript Example

```javascript
/**
 * API utilities for authentication
 * 
 * Handles HTTP requests to authentication endpoints and manages
 * authentication state in localStorage.
 * 
 * @module auth/api
 */

/**
 * Login with username/email and password
 * 
 * Sends login request to API, validates credentials, and stores
 * authentication token and user data in localStorage on success.
 * 
 * @param {string} usernameOrEmail - Username or email address
 * @param {string} password - Password
 * @returns {Promise<object>} Response containing user and token
 * @throws {Error} If login fails (invalid credentials, network error)
 * 
 * @example
 * try {
 *   const result = await login('john', 'password123');
 *   console.log('Logged in as:', result.user.username);
 * } catch (error) {
 *   console.error('Login failed:', error.message);
 * }
 */
export async function login(usernameOrEmail, password) {
    // Implementation
}
```

## Additional Resources

- [Oracle Javadoc Guide](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
- [JSDoc Documentation](https://jsdoc.app/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)

