# Contributing to Collectibles Store API

Thank you for your interest in contributing to the Collectibles Store API! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Branching Strategy](#branching-strategy)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Code Style](#code-style)
- [Testing Requirements](#testing-requirements)
- [Documentation](#documentation)

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect different viewpoints and experiences

## Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/your-username/collectibles-store.git
   cd collectibles-store
   ```
3. **Set up development environment**
   - Install Java 17+
   - Install Maven 3.6+
   - Install MySQL 8+
   - Install Node.js 18+ (for frontend tests)
   - Set up environment variables (see README.md)

## Development Workflow

### 1. Create a Feature Branch

Always create a new branch for your work:

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
# or
git checkout -b docs/documentation-update
```

### 2. Make Your Changes

- Write clean, well-documented code
- Follow the code style guidelines
- Write tests for new functionality
- Update documentation as needed

### 3. Test Your Changes

```bash
# Run backend tests
mvn test

# Run frontend tests
npm test

# Run with coverage
mvn clean test jacoco:report
npm test -- --coverage
```

### 4. Commit Your Changes

Follow the commit message guidelines below.

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a pull request on GitHub.

## Branching Strategy

### Branch Naming Conventions

Use descriptive branch names with prefixes:

- `feature/` - New features
  - Example: `feature/user-profile-page`
  - Example: `feature/email-notifications`
  
- `fix/` - Bug fixes
  - Example: `fix/auth-token-expiration`
  - Example: `fix/product-search-bug`
  
- `docs/` - Documentation updates
  - Example: `docs/readme-update`
  - Example: `docs/api-documentation`
  
- `refactor/` - Code refactoring
  - Example: `refactor/auth-service`
  - Example: `refactor/database-layer`
  
- `test/` - Test improvements
  - Example: `test/integration-tests`
  - Example: `test/coverage-improvement`

### Branch Structure

```
main (production-ready)
  └── develop (integration branch)
       ├── feature/user-authentication
       ├── feature/product-search
       ├── fix/login-bug
       └── docs/readme-update
```

## Commit Guidelines

### Commit Message Format

Use atomic commits with clear, descriptive messages:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Commit Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks (dependencies, build config, etc.)

### Commit Scope (Optional)

- `auth`: Authentication-related
- `product`: Product-related
- `api`: API endpoints
- `frontend`: Frontend JavaScript
- `backend`: Backend Java
- `db`: Database-related
- `ci`: CI/CD related

### Examples

**Good commits:**
```
feat(auth): add JWT token refresh endpoint

Adds POST /api/auth/refresh endpoint to allow users to refresh
their JWT tokens without re-authenticating.

Closes #123
```

```
fix(product): handle null category in product search

Fixes NullPointerException when searching products with null category.
Added null check before category filtering.

Fixes #456
```

```
docs(readme): update installation instructions

Updates README with Node.js 18+ requirement and npm install step
for frontend tests.
```

**Bad commits:**
```
fix stuff
```

```
WIP
```

```
changes
```

### Atomic Commits

Make small, focused commits. Each commit should:
- Address a single concern
- Be self-contained and testable
- Have a clear, descriptive message

**Example of good atomic commits:**
```
feat(auth): add password validation utility

test(auth): add tests for password validation

docs(auth): document password requirements
```

## Pull Request Process

### Before Submitting

- [ ] Code follows style guidelines
- [ ] All tests pass (`mvn test` and `npm test`)
- [ ] Code coverage maintained or improved
- [ ] Documentation updated
- [ ] No merge conflicts with target branch
- [ ] Commit messages follow guidelines

### PR Checklist

When creating a pull request, ensure:

- [ ] **Title**: Clear, descriptive title
- [ ] **Description**: 
  - What changes were made
  - Why the changes were needed
  - How to test the changes
  - Screenshots (if UI changes)
- [ ] **Tests**: All tests pass
- [ ] **Coverage**: Coverage report included
- [ ] **Documentation**: Updated README/docs if needed
- [ ] **Breaking Changes**: Documented if any

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests pass locally
```

### Review Process

1. **Automated Checks**: CI/CD pipeline runs tests and checks
2. **Code Review**: At least one maintainer reviews the code
3. **Feedback**: Address any review comments
4. **Approval**: Once approved, maintainer merges the PR

## Code Style

### Java

- Follow Java naming conventions
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Add Javadoc for public classes and methods
- Use meaningful variable and method names

**Example:**
```java
/**
 * Service for managing product operations
 * 
 * @author Your Name
 */
public class ProductService {
    /**
     * Create a new product
     * 
     * @param product Product to create
     * @param user User creating the product
     * @return Created product
     * @throws ProductValidationException if product data is invalid
     */
    public Product createProduct(Product product, User user) {
        // Implementation
    }
}
```

### JavaScript

- Use ES6+ features
- Use 2 spaces for indentation
- Use JSDoc for all exported functions
- Use meaningful variable and function names

**Example:**
```javascript
/**
 * Register a new user
 * 
 * @param {string} username - Username
 * @param {string} email - Email address
 * @param {string} password - Password
 * @returns {Promise<object>} Response containing user and token
 * @throws {Error} If registration fails
 */
export async function register(username, email, password) {
    // Implementation
}
```

### Code Formatting

Run formatters before committing:

```bash
# Java (if configured)
mvn formatter:format

# JavaScript (if configured)
npm run format
```

## Testing Requirements

### Backend Tests

- Write unit tests for all new methods
- Write integration tests for new endpoints
- Maintain minimum 70% code coverage
- All tests must pass before PR

```bash
mvn test
mvn clean test jacoco:report
```

### Frontend Tests

- Write unit tests for all new functions
- Test edge cases and error handling
- All tests must pass before PR

```bash
npm test
npm test -- --coverage
```

### Test Naming

- Test methods should be descriptive
- Use `@DisplayName` for JUnit tests
- Use descriptive `describe` blocks for Jest tests

## Documentation

### Code Documentation

- **Javadoc**: All public classes and methods
- **JSDoc**: All exported functions
- **Inline Comments**: For complex logic

### Documentation Updates

- Update README.md for user-facing changes
- Update API documentation for endpoint changes
- Update CONTRIBUTING.md for workflow changes
- Add examples for new features

## Questions?

If you have questions:
- Open an issue for discussion
- Check existing issues and PRs
- Review the codebase for examples

Thank you for contributing! 🎉

