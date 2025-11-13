/**
 * Authentication module - Main aggregator
 * 
 * This module exports all authentication functionality and initializes
 * authentication state on page load.
 * @module auth
 */

import * as storage from './storage.js';
import * as api from './api.js';
import * as ui from './ui.js';

// Re-export all functions for convenience
export { storage, api, ui };

// Export commonly used functions directly
export const {
    getToken,
    getCurrentUser,
    isAuthenticated,
    clearAuth
} = storage;

export const {
    login,
    register,
    logout,
    validateToken,
    authenticatedFetch
} = api;

export const {
    updateNavigation,
    initializeNavigation,
    hasRole,
    isAdmin
} = ui;

// Create a global Auth object for backward compatibility
const Auth = {
    // Storage methods
    getToken: storage.getToken,
    getCurrentUser: storage.getCurrentUser,
    isAuthenticated: storage.isAuthenticated,
    hasRole: ui.hasRole,
    isAdmin: ui.isAdmin,
    logout: api.logout,
    fetch: api.authenticatedFetch,
    validateToken: api.validateToken,
    updateUser: storage.setCurrentUser,
    getWebSocketUrl: (baseUrl) => {
        const token = storage.getToken();
        if (token) {
            const separator = baseUrl.includes('?') ? '&' : '?';
            return baseUrl + separator + 'token=' + encodeURIComponent(token);
        }
        return baseUrl;
    }
};

// Make Auth available globally for backward compatibility
if (typeof window !== 'undefined') {
    window.Auth = Auth;
}

// Auto-validate token on page load if authenticated
if (typeof document !== 'undefined') {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            ui.initializeNavigation();
            
            // Auto-validate token if authenticated
            if (storage.isAuthenticated()) {
                api.validateToken().catch(err => {
                    console.error('[AUTH] Token validation failed:', err);
                });
            }
        });
    } else {
        // DOM already loaded
        ui.initializeNavigation();
        
        if (storage.isAuthenticated()) {
            api.validateToken().catch(err => {
                console.error('[AUTH] Token validation failed:', err);
            });
        }
    }
}

export default Auth;

