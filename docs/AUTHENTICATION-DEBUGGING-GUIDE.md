# Authentication Debugging Guide

## Issue: Automatic Redirection with Invalid Tokens

### Problem Description

When users attempted to access the login or register pages, they were automatically redirected to the home page (`/`) even when they should have been able to see the authentication forms. This occurred when an invalid or expired JWT token was stored in `localStorage`.

### Root Cause

The login and register templates (`login.mustache` and `register.mustache`) were checking for the presence of a token in `localStorage` without validating whether the token was actually valid. The code was:

```javascript
// OLD CODE (BUGGY)
if (localStorage.getItem('authToken')) {
    window.location.href = '/';
}
```

This caused the following issues:
1. **Expired tokens**: Tokens that had expired were still present in `localStorage`, causing automatic redirection
2. **Invalid tokens**: Corrupted or invalid tokens would trigger redirection
3. **User confusion**: Users couldn't access login/register pages even after their session expired

### Solution Implemented

The fix validates the token with the server before redirecting:

```javascript
// NEW CODE (FIXED)
(async function() {
    const token = localStorage.getItem('authToken');
    if (token) {
        console.log('[AUTH] Token found in localStorage, validating...');
        try {
            const response = await fetch('/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                const user = await response.json();
                console.log('[AUTH] Token is valid, user:', user.username);
                localStorage.setItem('currentUser', JSON.stringify(user));
                window.location.href = '/';
            } else {
                console.log('[AUTH] Token validation failed, clearing localStorage');
                localStorage.removeItem('authToken');
                localStorage.removeItem('currentUser');
                // Stay on login page
            }
        } catch (error) {
            console.error('[AUTH] Error validating token:', error);
            localStorage.removeItem('authToken');
            localStorage.removeItem('currentUser');
        }
    } else {
        console.log('[AUTH] No token found, staying on login page');
    }
})();
```

### Enhanced Logging

Detailed logging has been added throughout the authentication flow to help debug similar issues in the future:

#### Backend Logging

**AuthRoutes.java** - All authentication endpoints now log:
- Client IP address
- Request details (username, email, etc.)
- Success/failure status
- Error details

**AuthService.java** - Service methods log:
- Method entry with parameters
- Step-by-step validation process
- User lookup results
- Token generation/validation results
- Error conditions

#### Frontend Logging

Console logs with `[AUTH]` prefix for:
- Token validation attempts
- Token validation results
- localStorage operations
- Redirection decisions

### Testing

#### Backend Tests

Created `AuthServiceTokenValidationTest.java` to test:
- Expired token handling
- Invalid signature handling
- User not found scenarios
- Inactive user scenarios
- Token validation failure scenarios

#### Manual Testing Steps

1. **Test with expired token**:
   - Login and wait for token to expire (or manually expire it)
   - Try to access `/login` or `/register`
   - Expected: Should stay on login/register page, not redirect

2. **Test with invalid token**:
   - Manually set invalid token in localStorage: `localStorage.setItem('authToken', 'invalid-token')`
   - Try to access `/login` or `/register`
   - Expected: Should clear localStorage and stay on page

3. **Test with valid token**:
   - Login successfully
   - Try to access `/login` or `/register`
   - Expected: Should redirect to home page

4. **Test with no token**:
   - Clear localStorage: `localStorage.clear()`
   - Try to access `/login` or `/register`
   - Expected: Should stay on login/register page

### Log Analysis

When debugging authentication issues, check the following logs:

#### Backend Logs

Look for patterns like:
```
POST /api/auth/login - Login attempt from IP: 127.0.0.1
AuthService.login() called for: username
AuthService.login() - User found: username (ID: user1), checking if active
AuthService.login() - Login successful for user: username (ID: user1), token generated
```

Or error patterns:
```
GET /api/auth/me - Invalid or expired token from IP: 127.0.0.1
AuthService.validateToken() - JWT verification failed: Token has expired
```

#### Frontend Console

Look for `[AUTH]` prefixed messages:
```
[AUTH] Token found in localStorage, validating...
[AUTH] Token validation failed, clearing localStorage
```

### Common Issues and Solutions

#### Issue: User redirected even with invalid token

**Symptom**: User is redirected to home page even when token is invalid

**Solution**: Check that the new validation code is in place. The token should be validated via `/api/auth/me` before redirecting.

#### Issue: Token validation fails but user stays on page

**Symptom**: Token validation returns 401 but user is not redirected

**Solution**: This is expected behavior. Invalid tokens should clear localStorage and allow user to stay on login/register page.

#### Issue: Logs show token validation but no user data

**Symptom**: Backend logs show successful token validation but frontend doesn't receive user data

**Solution**: Check CORS settings and ensure `Authorization` header is being sent correctly.

### Prevention

To prevent similar issues in the future:

1. **Always validate tokens server-side** before making authentication decisions
2. **Never trust client-side token presence** as proof of authentication
3. **Clear invalid tokens immediately** from localStorage
4. **Use detailed logging** to track authentication flow
5. **Test edge cases** including expired tokens, invalid tokens, and missing tokens

### Related Files

- `src/main/resources/templates/login.mustache` - Login page with token validation
- `src/main/resources/templates/register.mustache` - Register page with token validation
- `src/main/java/com/spark/collectibles/routes/AuthRoutes.java` - Authentication API endpoints with enhanced logging
- `src/main/java/com/spark/collectibles/service/AuthService.java` - Authentication service with detailed logging
- `src/test/java/com/spark/collectibles/service/AuthServiceTokenValidationTest.java` - Token validation tests

### References

- [Authentication Guide](AUTHENTICATION-GUIDE.md) - General authentication documentation
- [Authentication Testing Guide](AUTHENTICATION-TESTING-GUIDE.md) - Testing procedures
- [Technical Difficulties and Solutions](TECHNICAL-DIFFICULTIES-AND-SOLUTIONS.md) - Other authentication-related issues

