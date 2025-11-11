# Authentication Guide

## Overview

The Collectibles Store API now includes JWT-based authentication for secure access to protected endpoints. This guide explains how to use the authentication system.

## Quick Start

### 1. Register a New User

```bash
curl -X POST http://localhost:4567/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "password123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

**Response** (201 Created):
```json
{
  "user": {
    "id": "user1",
    "username": "admin",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2. Login

```bash
curl -X POST http://localhost:4567/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "password123"
  }'
```

**Response** (200 OK):
```json
{
  "user": { ... },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Use Token for Protected Routes

```bash
curl -X POST http://localhost:4567/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "name": "New Product",
    "description": "Product description",
    "price": 99.99,
    "currency": "USD",
    "category": "Collectibles"
  }'
```

## Environment Variables

### Required for Production

```bash
JWT_SECRET=your-secret-key-here-minimum-32-characters-very-important
```

### Optional

```bash
JWT_EXPIRATION_HOURS=24  # Default: 24 hours
BCRYPT_ROUNDS=10         # Default: 10 (4-31 range)
```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login and get JWT token | No |
| GET | `/api/auth/me` | Get current user info | Yes |
| POST | `/api/auth/logout` | Logout (client-side) | No |

### Protected Endpoints (Require ADMIN Role)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create product |
| PUT | `/api/products/:id` | Update product |
| DELETE | `/api/products/:id` | Delete product |
| POST | `/api/products/:id/restore` | Restore product |
| DELETE | `/api/products/:id/hard` | Hard delete product |
| GET | `/api/users` | Get all users |
| GET | `/api/users/:id` | Get user by ID |
| PUT | `/api/users/:id` | Update user |
| DELETE | `/api/users/:id` | Delete user |

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products |
| GET | `/api/products/:id` | Get product by ID |
| GET | `/api/products/search` | Search products |
| GET | `/api/products/category/:category` | Get products by category |

## User Roles

- **ADMIN**: Full access to all endpoints, can manage products and users
- **CUSTOMER**: Regular user, can browse products
- **MODERATOR**: Reserved for future use

## Security Features

- **Password Hashing**: All passwords are hashed using BCrypt (never stored plaintext)
- **JWT Tokens**: Stateless authentication using JSON Web Tokens
- **Token Expiration**: Tokens expire after 24 hours (configurable)
- **Role-Based Access**: Different roles have different permissions
- **SQL Injection Protection**: All queries use prepared statements

## Database

The `users` table is automatically created when the application starts via Flyway migration `V3__Create_users_table.sql`. The migration uses `CREATE TABLE IF NOT EXISTS`, so it's safe to run on existing databases.

## Testing

See `AUTHENTICATION-TESTING-GUIDE.md` for comprehensive testing instructions.

## Troubleshooting

### "JWT_SECRET not set"
**Solution**: Set the `JWT_SECRET` environment variable to a strong random string (minimum 32 characters).

### "Authentication required"
**Solution**: Include the JWT token in the `Authorization` header: `Authorization: Bearer <token>`

### "Invalid or expired token"
**Solution**: 
- Check that the token is correctly formatted
- Verify the token hasn't expired (default: 24 hours)
- Login again to get a new token

### "Insufficient permissions"
**Solution**: Verify your user has the required role (ADMIN for protected routes).

## Production Deployment

1. Set `JWT_SECRET` environment variable in Render.com dashboard
2. Set `JWT_EXPIRATION_HOURS` if different from default (24)
3. Database migration will run automatically on deployment
4. Register first admin user via `/api/auth/register` endpoint

## Support

For detailed testing instructions, see `AUTHENTICATION-TESTING-GUIDE.md`.  
For technical implementation details, see `TECHNICAL-DIFFICULTIES-AND-SOLUTIONS.md`.

