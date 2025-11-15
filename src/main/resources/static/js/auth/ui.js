/**
 * UI utilities for authentication
 * 
 * Handles DOM updates and navigation state based on authentication status.
 * @module auth/ui
 */

import { getCurrentUser, isAuthenticated } from './storage.js';

/**
 * Update navigation based on authentication status
 * @param {object} user - Current user object (optional)
 */
export function updateNavigation(user = null) {
    const navAuth = document.getElementById('nav-auth');
    const navUser = document.getElementById('nav-user');
    const navUserMenu = document.getElementById('nav-user-menu');
    const navUsername = document.getElementById('nav-username');
    
    if (!navAuth || !navUser) {
        return; // Navigation elements not found
    }
    
    const authenticated = isAuthenticated();
    const currentUser = user || getCurrentUser();
    
    if (authenticated && currentUser) {
        // Show user menu, hide login/register
        navAuth.style.display = 'none';
        navUser.style.display = 'block';
        
        if (navUsername) {
            navUsername.textContent = currentUser.username || 'User';
        }
        
        if (navUserMenu) {
            // Update user menu based on role
            const adminLinks = navUserMenu.querySelectorAll('.admin-only');
            if (currentUser.role === 'ADMIN') {
                adminLinks.forEach(link => {
                    link.style.display = 'block';
                });
            } else {
                adminLinks.forEach(link => {
                    link.style.display = 'none';
                });
            }
        }
        
        console.log('[AUTH] Navigation updated for authenticated user:', currentUser.username);
    } else {
        // Show login/register, hide user menu
        navAuth.style.display = 'block';
        navUser.style.display = 'none';
        
        console.log('[AUTH] Navigation updated for unauthenticated user');
    }
}

/**
 * Check if user has a specific role
 * @param {string} role - Role to check (ADMIN, CUSTOMER, etc.)
 * @returns {boolean} True if user has the role
 */
export function hasRole(role) {
    const user = getCurrentUser();
    return user ? user.role === role : false;
}

/**
 * Check if user is admin
 * @returns {boolean} True if user is ADMIN
 */
export function isAdmin() {
    return hasRole('ADMIN');
}

/**
 * Initialize navigation on page load
 */
export function initializeNavigation() {
    console.log('[AUTH] DOMContentLoaded - Initializing navigation');
    
    const token = localStorage.getItem('authToken');
    const user = getCurrentUser();
    
    console.log('[AUTH] Navigation check - Token:', token ? 'EXISTS' : 'MISSING');
    console.log('[AUTH] Navigation check - User:', user ? 'EXISTS' : 'MISSING');
    
    if (token && user) {
        console.log('[AUTH] Parsed user from localStorage:', user.username);
        updateNavigation(user);
    } else {
        console.log('[AUTH] No token found, showing login/register');
        updateNavigation();
    }
}

/**
 * Show error message to user
 * @param {string} message - Error message to display
 * @param {HTMLElement} container - Container element to show error in
 */
export function showError(message, container) {
    if (!container) {
        console.error('[AUTH] Error container not found');
        return;
    }
    
    container.innerHTML = `
        <div class="alert alert-error" role="alert">
            ${message}
        </div>
    `;
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

/**
 * Show success message to user
 * @param {string} message - Success message to display
 * @param {HTMLElement} container - Container element to show message in
 */
export function showSuccess(message, container) {
    if (!container) {
        console.log('[AUTH] Success:', message);
        return;
    }
    
    container.innerHTML = `
        <div class="alert alert-success" role="alert">
            ${message}
        </div>
    `;
    
    // Auto-hide after 3 seconds
    setTimeout(() => {
        container.innerHTML = '';
    }, 3000);
}

