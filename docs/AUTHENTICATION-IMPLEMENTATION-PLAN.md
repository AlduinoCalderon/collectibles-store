# Authentication & Database Migration Implementation Plan

## Overview

This document outlines the implementation plan for adding authentication/authorization to the Collectibles Store application and migrating the User model from in-memory storage to database persistence.

**Status**: 🔄 In Progress  
**Target Branch**: Local development branch (NOT main/master - production)  
**Production Safety**: All changes must be tested before merging to main/master

## Objectives

1. **Database Migration**: Create `users` table with authentication fields
2. **User Repository**: Implement database persistence for users (following ProductRepository pattern)
3. **Authentication System**: JWT-based authentication with BCrypt password hashing
4. **Route Protection**: Middleware to protect admin routes
5. **Testing**: Comprehensive tests to ensure production safety
6. **Documentation**: Update all documentation

## Architecture Decision: Authentication Approach

### Selected: JWT (JSON Web Tokens)

**Why JWT?**
- Stateless authentication (no server-side sessions)
- Scalable for horizontal scaling
- Works well with RESTful APIs
- Standard industry practice
- Lightweight and simple to implement

**Alternative Considered: Session-based**
- ❌ Requires session storage (Redis or in-memory)
- ❌ Not ideal for stateless REST APIs
- ❌ More complex deployment

### Password Hashing: BCrypt

- Industry standard for password hashing
- Built-in salting
- Configurable work factor (cost)
- Secure and battle-tested

## Implementation Steps

### Phase 1: Dependencies & Configuration ✅

1. **Add Dependencies to `pom.xml`**
   - JWT library: `java-jwt` (Auth0)
   - BCrypt: `jbcrypt` or use `spring-security-crypto` (BCrypt only)
   - Minimal dependencies to keep stack simple

### Phase 2: Database Migration ✅

2. **Create Migration: `V3__Create_users_table.sql`**
   - `id` VARCHAR(50) PRIMARY KEY
   - `username` VARCHAR(100) UNIQUE NOT NULL
   - `email` VARCHAR(255) UNIQUE NOT NULL
   - `password_hash` VARCHAR(255) NOT NULL (BCrypt hashes are 60 chars)
   - `first_name` VARCHAR(100)
   - `last_name` VARCHAR(100)
   - `role` ENUM('ADMIN', 'CUSTOMER', 'MODERATOR') DEFAULT 'CUSTOMER'
   - `is_active` BOOLEAN DEFAULT TRUE
   - `created_at` TIMESTAMP
   - `updated_at` TIMESTAMP
   - Indexes on `username`, `email` for fast lookups

### Phase 3: Repository Layer ✅

3. **Create `UserRepository` Interface**
   - Follow same pattern as `ProductRepository`
   - Methods: `findById`, `findByUsername`, `findByEmail`, `save`, `update`, `delete`, etc.

4. **Create `MySQLUserRepository` Implementation**
   - Follow `PostgreSQLProductRepository` pattern
   - Use prepared statements (SQL injection protection)
   - Map ResultSet to User objects
   - Handle password_hash field (never return in queries)

### Phase 4: Service Layer ✅

5. **Update `UserService`**
   - Replace in-memory `ConcurrentHashMap` with `UserRepository`
   - Add password hashing on user creation/update
   - Add user lookup by username/email for authentication
   - Never expose password_hash in responses

6. **Create `AuthService`**
   - `register(String username, String email, String password, ...)` → Returns User + JWT
   - `login(String username, String password)` → Returns User + JWT
   - `validateToken(String token)` → Returns User or null
   - `hashPassword(String password)` → Returns BCrypt hash
   - `verifyPassword(String password, String hash)` → Returns boolean

### Phase 5: Authentication Middleware ✅

7. **Create `AuthFilter` or `AuthMiddleware`**
   - Extract JWT from `Authorization: Bearer <token>` header
   - Validate token
   - Set user context in request attributes
   - Return 401 Unauthorized if invalid

8. **Create Route Protection**
   - `requireAuth()` → Requires valid JWT
   - `requireRole(UserRole role)` → Requires specific role
   - Apply to admin routes (`/admin/*`, `/api/products` POST/PUT/DELETE)

### Phase 6: API Routes ✅

9. **Create `AuthRoutes`**
   - `POST /api/auth/register` → Register new user
   - `POST /api/auth/login` → Login and get JWT
   - `POST /api/auth/logout` → Logout (client-side token removal)
   - `GET /api/auth/me` → Get current user info
   - `POST /api/auth/refresh` → Refresh JWT token (optional)

10. **Update `UserRoutes`**
    - Add authentication requirement for user management
    - Only admins can manage users
    - Users can view/update their own profile

### Phase 7: Application Integration ✅

11. **Update `Application.java`**
    - Initialize `UserService` with `UserRepository`
    - Initialize `AuthService`
    - Register `AuthRoutes`
    - Apply authentication filters to protected routes
    - Ensure backward compatibility (public routes still work)

### Phase 8: Testing ✅

12. **Unit Tests**
    - `AuthServiceTest`: Test password hashing, JWT generation/validation
    - `UserServiceTest`: Test user CRUD with database
    - `UserRepositoryTest`: Test database operations

13. **Integration Tests**
    - `AuthRoutesIntegrationTest`: Test login, register, protected routes
    - Test authentication failures (invalid token, expired token)
    - Test role-based access control

14. **Backward Compatibility Tests**
    - Verify existing product routes still work (public access)
    - Verify WebSocket still works
    - Verify admin UI works with authentication

### Phase 9: Documentation ✅

15. **Update Documentation**
    - Update `README.md` with authentication endpoints
    - Update `openapi.json` with auth endpoints
    - Create `AUTHENTICATION.md` guide
    - Update `TECHNICAL-ANALYSIS-REPORT.md`

## Security Considerations

### Password Security
- ✅ Passwords hashed with BCrypt (never stored plaintext)
- ✅ Password never returned in API responses
- ✅ Password validation (min length, complexity rules)

### JWT Security
- ✅ JWT signed with secret key (stored in environment variable)
- ✅ JWT expiration (e.g., 24 hours)
- ✅ HTTPS required in production (JWT in Authorization header)

### Route Protection
- ✅ Admin routes require authentication
- ✅ Role-based access control (ADMIN, MODERATOR, CUSTOMER)
- ✅ Public routes remain accessible (product browsing)

### SQL Injection Protection
- ✅ All queries use prepared statements
- ✅ Parameterized queries in UserRepository

## Database Schema

```sql
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role ENUM('ADMIN', 'CUSTOMER', 'MODERATOR') NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login and get JWT | No |
| POST | `/api/auth/logout` | Logout (client-side) | No |
| GET | `/api/auth/me` | Get current user | Yes |
| POST | `/api/auth/refresh` | Refresh JWT token | Yes |

### Protected User Management (Admin Only)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/users` | Get all users | Yes | ADMIN |
| GET | `/api/users/:id` | Get user by ID | Yes | ADMIN |
| PUT | `/api/users/:id` | Update user | Yes | ADMIN |
| DELETE | `/api/users/:id` | Delete user | Yes | ADMIN |

### Protected Product Management (Admin Only)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| POST | `/api/products` | Create product | Yes | ADMIN |
| PUT | `/api/products/:id` | Update product | Yes | ADMIN |
| DELETE | `/api/products/:id` | Delete product | Yes | ADMIN |

## Environment Variables

Add to `EnvironmentConfig.java` and deployment:

```bash
# JWT Configuration
JWT_SECRET=your-secret-key-here-min-32-characters
JWT_EXPIRATION_HOURS=24

# Optional: BCrypt work factor (default: 10)
BCRYPT_ROUNDS=10
```

## Testing Strategy

### 1. Unit Tests
- Test password hashing and verification
- Test JWT generation and validation
- Test user repository CRUD operations
- Test authentication service methods

### 2. Integration Tests
- Test registration flow
- Test login flow
- Test protected routes (with and without auth)
- Test role-based access control
- Test invalid token handling

### 3. Backward Compatibility Tests
- Verify public product routes work without auth
- Verify WebSocket connection works
- Verify existing functionality unchanged

### 4. Security Tests
- Test SQL injection protection
- Test password hash not exposed
- Test JWT validation
- Test unauthorized access attempts

## Deployment Checklist

Before merging to main/master (production):

- [ ] All tests passing
- [ ] Database migration tested on staging
- [ ] Backward compatibility verified
- [ ] Environment variables configured in Render
- [ ] JWT_SECRET set in production environment
- [ ] Documentation updated
- [ ] Code review completed
- [ ] Security review completed

## Rollback Plan

If issues arise in production:

1. **Database Rollback**: Flyway migration can be rolled back
2. **Code Rollback**: Revert to previous commit
3. **Feature Flag**: Add feature flag to disable auth if needed

## Timeline

- **Phase 1-2**: Dependencies & Database (1-2 hours)
- **Phase 3-4**: Repository & Service (2-3 hours)
- **Phase 5-6**: Auth Middleware & Routes (2-3 hours)
- **Phase 7**: Application Integration (1 hour)
- **Phase 8**: Testing (3-4 hours)
- **Phase 9**: Documentation (1-2 hours)

**Total Estimated Time**: 12-15 hours

## Success Criteria

✅ Users can register and login  
✅ JWT tokens are generated and validated  
✅ Protected routes require authentication  
✅ Role-based access control works  
✅ Password hashing is secure (BCrypt)  
✅ All existing functionality still works  
✅ Tests pass with >80% coverage  
✅ Documentation is updated  
✅ Production deployment successful

## Notes

- **Production Safety**: All changes are on local branch, tested before merge
- **Backward Compatibility**: Public routes remain accessible
- **Minimal Dependencies**: Only JWT and BCrypt libraries added
- **Security First**: Password hashing, SQL injection protection, JWT security

