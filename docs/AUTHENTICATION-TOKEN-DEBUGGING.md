# Authentication Token Debugging Guide

## Problem: Token Loss After Login

### Symptoms
- User successfully logs in
- Token is saved to localStorage
- After redirect, token appears to be lost
- User is shown login/register options instead of user menu
- Console shows token validation failures

### Root Cause Analysis

The issue occurs when:
1. Login is successful and token is saved
2. Page redirects to home page (`/`)
3. Home page loads but token validation fails or token is not found
4. Navigation shows login/register instead of user menu

### Debugging Steps

#### 1. Check Browser Console Logs

Look for `[AUTH]` prefixed messages in the browser console:

**During Login:**
```
[AUTH] Sending login request for: username
[AUTH] Login response status: 200
[AUTH] Login successful, saving token to localStorage
[AUTH] Token length: 200
[AUTH] Token saved verification - Token exists: true
[AUTH] Redirecting to home page in 1 second...
[AUTH] Executing redirect now...
[AUTH] Token before redirect: EXISTS
```

**After Redirect:**
```
[AUTH] DOMContentLoaded - Initializing navigation
[AUTH] Navigation check - Token: EXISTS
[AUTH] Navigation check - User: EXISTS
[AUTH] Parsed user from localStorage: username
[AUTH] Showing user menu for: username
```

**If Token is Lost:**
```
[AUTH] Navigation check - Token: MISSING
[AUTH] Navigation check - User: MISSING
[AUTH] No token found, showing login/register
```

#### 2. Check Backend Logs

Look for authentication-related logs in the server console:

**Successful Login:**
```
POST /api/auth/login - Login attempt from IP: 127.0.0.1
POST /api/auth/login - Attempting login for: username from IP: 127.0.0.1
AuthService.login() called for: username
AuthService.login() - Login successful for user: username (ID: user1), token generated
POST /api/auth/login - Successful login for user: username (ID: user1) from IP: 127.0.0.1
POST /api/auth/login - Token generated, length: 200
AuthResponse created - User: username, Token exists: true
```

**Token Validation:**
```
GET /api/auth/me - Token validation request from IP: 127.0.0.1
AuthService.validateToken() called
AuthService.validateToken() - Token signature valid, user ID: user1
GET /api/auth/me - Valid token for user: username (ID: user1) from IP: 127.0.0.1
```

#### 3. Verify localStorage

Open browser DevTools → Application → Local Storage → Check:
- `authToken` should exist and contain a JWT token
- `currentUser` should exist and contain user JSON

**Manual Check:**
```javascript
// In browser console:
localStorage.getItem('authToken')  // Should return token string
localStorage.getItem('currentUser')  // Should return JSON string
```

#### 4. Check for Errors

**Common Errors:**

1. **404 on /static/auth.js**
   - Symptom: `GET /static/auth.js net::ERR_ABORTED 404`
   - Solution: Verify static files are configured correctly
   - Check: `Application.java` has `staticFiles.location("/static")`

2. **502 Bad Gateway**
   - Symptom: `POST /api/auth/login 502 (Bad Gateway)`
   - Cause: Server error or timeout
   - Solution: Check server logs, verify database connection

3. **401 Unauthorized**
   - Symptom: `GET /api/auth/me 401 (Unauthorized)`
   - Cause: Token expired or invalid
   - Solution: Token may have expired, user needs to login again

4. **Non-JSON Response**
   - Symptom: `SyntaxError: Unexpected token '<', "<!DOCTYPE "...`
   - Cause: Server returning HTML error page instead of JSON
   - Solution: Check server error handling, verify API routes

### Debugging Commands

#### Frontend Console Commands

```javascript
// Check authentication status
console.log('Token:', localStorage.getItem('authToken'));
console.log('User:', localStorage.getItem('currentUser'));

// Manually validate token
fetch('/api/auth/me', {
    headers: { 'Authorization': 'Bearer ' + localStorage.getItem('authToken') }
}).then(r => r.json()).then(console.log);

// Check if Auth object exists
console.log('Auth object:', typeof Auth !== 'undefined' ? 'EXISTS' : 'MISSING');
```

#### Backend Logging

Enable DEBUG logging in `logback.xml`:
```xml
<logger name="com.spark.collectibles" level="DEBUG"/>
```

### Common Issues and Solutions

#### Issue 1: Token Not Saved After Login

**Symptoms:**
- Login succeeds but token is missing after redirect
- Console shows "Token saved verification - Token exists: false"

**Debugging:**
1. Check if `data.token` exists in login response
2. Verify localStorage.setItem is being called
3. Check for browser storage restrictions (private mode, etc.)

**Solution:**
- Verify response contains token: `console.log('Response data:', data)`
- Check browser storage settings
- Verify no browser extensions blocking localStorage

#### Issue 2: Token Lost During Redirect

**Symptoms:**
- Token exists before redirect
- Token missing after redirect
- Different domain/subdomain issue

**Debugging:**
1. Check if redirect changes domain
2. Verify localStorage is domain-specific
3. Check for cross-origin issues

**Solution:**
- Ensure redirect stays on same domain
- Check for subdomain changes (www vs non-www)
- Verify CORS settings if using different ports

#### Issue 3: auth.js Not Loading

**Symptoms:**
- `GET /static/auth.js 404 (Not Found)`
- `Auth object: MISSING` in console
- Navigation doesn't update

**Debugging:**
1. Verify file exists: `src/main/resources/static/auth.js`
2. Check static file configuration
3. Verify file is included in build

**Solution:**
- Check `Application.java` static file configuration
- Verify file is in correct location
- Rebuild application if needed

#### Issue 4: Token Validation Fails

**Symptoms:**
- Token exists in localStorage
- `/api/auth/me` returns 401
- Backend logs show "Invalid or expired token"

**Debugging:**
1. Check token format in localStorage
2. Verify token hasn't expired
3. Check JWT_SECRET matches

**Solution:**
- Verify token is complete (not truncated)
- Check token expiration time
- Ensure JWT_SECRET is consistent

### Testing Checklist

- [ ] Login successful - token saved to localStorage
- [ ] Token persists after page reload
- [ ] Token persists after redirect
- [ ] Navigation shows user menu when authenticated
- [ ] Token validation works via `/api/auth/me`
- [ ] auth.js loads successfully
- [ ] No console errors related to authentication
- [ ] Backend logs show successful authentication
- [ ] Token remains valid for expected duration

### Log Analysis Workflow

1. **Start Application** - Check for startup errors
2. **Open Browser Console** - Enable verbose logging
3. **Attempt Login** - Watch for `[AUTH]` messages
4. **Check localStorage** - Verify token and user data
5. **Navigate to Home** - Watch navigation update
6. **Check Backend Logs** - Verify server-side authentication
7. **Reload Page** - Verify token persistence

### Expected Log Flow

**Successful Authentication Flow:**
```
Frontend:
[AUTH] Sending login request
[AUTH] Login response status: 200
[AUTH] Login successful, saving token
[AUTH] Token saved verification - Token exists: true
[AUTH] Redirecting to home page
[AUTH] DOMContentLoaded - Initializing navigation
[AUTH] Navigation check - Token: EXISTS
[AUTH] Showing user menu for: username

Backend:
POST /api/auth/login - Login attempt
AuthService.login() - Login successful
POST /api/auth/login - Successful login
GET /api/auth/me - Token validation request
GET /api/auth/me - Valid token for user
```

### Next Steps

If token loss persists after following this guide:
1. Check browser compatibility (localStorage support)
2. Verify no browser extensions interfering
3. Check for JavaScript errors blocking execution
4. Verify server is returning correct response format
5. Check network tab for failed requests
6. Review server logs for authentication errors

