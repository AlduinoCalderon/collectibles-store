/**
 * API utilities for authentication
 * 
 * Handles HTTP requests to authentication endpoints.
 * @module auth/api
 */

import { getToken, setToken, setCurrentUser, clearAuth } from './storage.js';

/**
 * Make an authenticated fetch request
 * Automatically includes the JWT token in the Authorization header
 * @param {string} url - Request URL
 * @param {object} options - Fetch options (method, headers, body, etc.)
 * @returns {Promise<Response>} Fetch response
 */
export function authenticatedFetch(url, options = {}) {
    const token = getToken();
    
    // Set default headers
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    // Add Authorization header if token exists
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Merge with existing options
    const fetchOptions = {
        ...options,
        headers: headers
    };
    
    return fetch(url, fetchOptions);
}

/**
 * Register a new user
 * @param {string} username - Username
 * @param {string} email - Email address
 * @param {string} password - Password
 * @param {string} firstName - First name (optional)
 * @param {string} lastName - Last name (optional)
 * @param {string} role - User role (optional, defaults to CUSTOMER)
 * @returns {Promise<object>} Response containing user and token
 * @throws {Error} If registration fails
 */
export async function register(username, email, password, firstName = '', lastName = '', role = 'CUSTOMER') {
    console.log('[AUTH] Sending registration request for:', username);
    
    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username,
            email,
            password,
            firstName,
            lastName,
            role
        })
    });
    
    const data = await response.json();
    
    if (!response.ok) {
        console.error('[AUTH] Registration failed:', data.message);
        throw new Error(data.message || 'Registration failed');
    }
    
    console.log('[AUTH] Registration successful for:', username);
    
    // Save token and user
    if (data.token) {
        setToken(data.token);
    }
    if (data.user) {
        setCurrentUser(data.user);
    }
    
    return data;
}

/**
 * Login with username/email and password
 * @param {string} usernameOrEmail - Username or email address
 * @param {string} password - Password
 * @returns {Promise<object>} Response containing user and token
 * @throws {Error} If login fails
 */
export async function login(usernameOrEmail, password) {
    console.log('[AUTH] Sending login request for:', usernameOrEmail);
    
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            usernameOrEmail,
            password
        })
    });
    
    const data = await response.json();
    
    if (!response.ok) {
        console.error('[AUTH] Login failed:', data.message);
        throw new Error(data.message || 'Login failed');
    }
    
    console.log('[AUTH] Login successful for:', usernameOrEmail);
    
    // Save token and user
    if (data.token) {
        setToken(data.token);
    }
    if (data.user) {
        setCurrentUser(data.user);
    }
    
    return data;
}

/**
 * Logout user (client-side only, clears localStorage)
 */
export function logout() {
    console.log('[AUTH] Logging out user');
    clearAuth();
    window.location.href = '/login';
}

/**
 * Validate token by checking with server
 * @returns {Promise<boolean>} True if token is valid
 */
export async function validateToken() {
    const token = getToken();
    if (!token) {
        return false;
    }
    
    try {
        const response = await authenticatedFetch('/api/auth/me');
        if (response.ok) {
            const user = await response.json();
            setCurrentUser(user);
            console.log('[AUTH] Token validated successfully, user:', user.username);
            return true;
        } else {
            // Token is invalid, but don't auto-logout (let user decide)
            console.warn('[AUTH] Token validation failed, status:', response.status);
            // Only clear if it's a 401 (unauthorized), not for other errors
            if (response.status === 401) {
                clearAuth();
            }
            return false;
        }
    } catch (error) {
        console.error('[AUTH] Error validating token:', error);
        // Don't clear token on network errors, might be temporary
        return false;
    }
}

/**
 * Get current user info from server
 * @returns {Promise<object|null>} User object or null if not authenticated
 */
export async function getCurrentUserInfo() {
    const token = getToken();
    if (!token) {
        return null;
    }
    
    try {
        const response = await authenticatedFetch('/api/auth/me');
        if (response.ok) {
            const user = await response.json();
            setCurrentUser(user);
            return user;
        }
        return null;
    } catch (error) {
        console.error('[AUTH] Error getting current user info:', error);
        return null;
    }
}

