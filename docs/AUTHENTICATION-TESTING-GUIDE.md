# Authentication Testing Guide

## Quick Start Testing

### 1. Start the Application
```bash
mvn clean package
java -jar target/collectibles-store-1.0.0.jar
```

### 2. Set Environment Variables (if not using defaults)
```bash
export JWT_SECRET=test-secret-key-for-development-min-32-characters
export JWT_EXPIRATION_HOURS=24
```

### 3. Test Registration
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

### 4. Test Login
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
  "user": { ... },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 5. Test Protected Route (Create Product)
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
  ...
}
```

### 6. Test Protected Route Without Token (Should Fail)
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

### 7. Test Public Route (Should Work Without Token)
```bash
curl -X GET http://localhost:4567/api/products
```

**Expected Response** (200 OK):
```json
[
  {
    "id": "item1",
    "name": "Test Product",
    ...
  }
]
```

### 8. Test Get Current User
```bash
curl -X GET http://localhost:4567/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response** (200 OK):
```json
{
  "id": "user1",
  "username": "testadmin",
  "email": "admin@test.com",
  ...
}
```

## Test Scenarios

### Scenario 1: Register Duplicate Username
```bash
# Try to register with same username
curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "email": "different@test.com",
    "password": "password123"
  }'
```

**Expected**: 409 Conflict - "User registration failed. Username or email may already exist."

### Scenario 2: Login with Wrong Password
```bash
curl -X POST http://localhost:4567/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testadmin",
    "password": "wrongpassword"
  }'
```

**Expected**: 401 Unauthorized - "Invalid username/email or password"

### Scenario 3: Access Protected Route with Invalid Token
```bash
curl -X POST http://localhost:4567/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid-token" \
  -d '{...}'
```

**Expected**: 401 Unauthorized - "Invalid or expired token"

### Scenario 4: Access Protected Route with Customer Role (Should Fail)
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

## Database Verification

### Check Users Table
```sql
SELECT id, username, email, role, is_active, created_at 
FROM users;
```

### Verify Password Hash
```sql
SELECT id, username, password_hash 
FROM users 
WHERE username = 'testadmin';
```

**Expected**: Password hash should be a BCrypt hash (starts with `$2a$` or `$2b$`)

## Troubleshooting

### Issue: "JWT_SECRET not set"
**Solution**: Set environment variable `JWT_SECRET` to a strong random string

### Issue: "Authentication required" on public routes
**Solution**: Check that route protection is only applied to POST/PUT/DELETE methods

### Issue: "Invalid or expired token"
**Solution**: 
- Check that token is included in `Authorization: Bearer <token>` header
- Verify token hasn't expired (default: 24 hours)
- Regenerate token by logging in again

### Issue: "Insufficient permissions"
**Solution**: Verify user has ADMIN role for protected routes

## Automated Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -Dtest=AuthRoutesIntegrationTest
```

### Run All Tests
```bash
mvn test
```

## Performance Testing

### Test Password Hashing Performance
```bash
# BCrypt with 10 rounds should take ~100-200ms per hash
time curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### Test JWT Generation Performance
```bash
# JWT generation should be very fast (<1ms)
time curl -X POST http://localhost:4567/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{...}'
```

## Security Testing

### Test SQL Injection Protection
```bash
# Try SQL injection in username
curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin\"; DROP TABLE users; --",
    "email": "test@test.com",
    "password": "password123"
  }'
```

**Expected**: Should be handled safely (parameterized queries prevent SQL injection)

### Test Password Hash Not Exposed
```bash
# Register user
# Login user
# Get user info

# Verify password_hash is NOT in response
curl -X GET http://localhost:4567/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: Response should NOT contain `passwordHash` field

## Next Steps

1. ✅ Complete manual testing
2. ✅ Write automated tests
3. ✅ Test on staging environment
4. ✅ Deploy to production
5. ✅ Monitor logs and performance

