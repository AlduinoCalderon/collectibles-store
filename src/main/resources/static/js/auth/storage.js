/**
 * Storage utilities for authentication
 * 
 * Handles localStorage operations for authentication tokens and user data.
 * @module auth/storage
 */

/**
 * Get the stored authentication token from localStorage
 * @returns {string|null} JWT token or null if not found
 */
export function getToken() {
    return localStorage.getItem('authToken');
}

/**
 * Save authentication token to localStorage
 * @param {string} token - JWT token to save
 */
export function setToken(token) {
    if (token) {
        localStorage.setItem('authToken', token);
    }
}

/**
 * Remove authentication token from localStorage
 */
export function removeToken() {
    localStorage.removeItem('authToken');
}

/**
 * Get the current user from localStorage
 * @returns {object|null} User object or null if not found
 */
export function getCurrentUser() {
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
        try {
            return JSON.parse(userStr);
        } catch (e) {
            console.error('[AUTH] Error parsing current user:', e);
            return null;
        }
    }
    return null;
}

/**
 * Save current user to localStorage
 * @param {object} user - User object to save
 */
export function setCurrentUser(user) {
    if (user) {
        localStorage.setItem('currentUser', JSON.stringify(user));
    }
}

/**
 * Remove current user from localStorage
 */
export function removeCurrentUser() {
    localStorage.removeItem('currentUser');
}

/**
 * Check if user is authenticated (has token)
 * @returns {boolean} True if user has a valid token
 */
export function isAuthenticated() {
    return getToken() !== null;
}

/**
 * Clear all authentication data from localStorage
 */
export function clearAuth() {
    removeToken();
    removeCurrentUser();
}

