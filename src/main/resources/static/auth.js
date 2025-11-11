/**
 * Authentication utility functions
 * Handles token storage, retrieval, and authenticated API requests
 */

const Auth = {
    /**
     * Get the stored authentication token
     * @returns {string|null} JWT token or null if not logged in
     */
    getToken: function() {
        return localStorage.getItem('authToken');
    },
    
    /**
     * Get the current user from localStorage
     * @returns {object|null} User object or null if not logged in
     */
    getCurrentUser: function() {
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (e) {
                console.error('Error parsing current user:', e);
                return null;
            }
        }
        return null;
    },
    
    /**
     * Check if user is authenticated
     * @returns {boolean} True if user has a valid token
     */
    isAuthenticated: function() {
        return this.getToken() !== null;
    },
    
    /**
     * Check if user has a specific role
     * @param {string} role - Role to check (ADMIN, CUSTOMER, etc.)
     * @returns {boolean} True if user has the role
     */
    hasRole: function(role) {
        const user = this.getCurrentUser();
        return user && user.role === role;
    },
    
    /**
     * Check if user is admin
     * @returns {boolean} True if user is ADMIN
     */
    isAdmin: function() {
        return this.hasRole('ADMIN');
    },
    
    /**
     * Logout user by clearing stored data
     */
    logout: function() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
        window.location.href = '/login';
    },
    
    /**
     * Make an authenticated fetch request
     * Automatically includes the JWT token in the Authorization header
     * @param {string} url - Request URL
     * @param {object} options - Fetch options (method, headers, body, etc.)
     * @returns {Promise<Response>} Fetch response
     */
    fetch: function(url, options = {}) {
        const token = this.getToken();
        
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
    },
    
    /**
     * Validate token by checking with server
     * @returns {Promise<boolean>} True if token is valid
     */
    validateToken: async function() {
        const token = this.getToken();
        if (!token) {
            return false;
        }
        
        try {
            const response = await this.fetch('/api/auth/me');
            if (response.ok) {
                const user = await response.json();
                localStorage.setItem('currentUser', JSON.stringify(user));
                console.log('[AUTH] Token validated successfully, user:', user.username);
                return true;
            } else {
                // Token is invalid, but don't auto-logout (let user decide)
                console.warn('[AUTH] Token validation failed, status:', response.status);
                // Only clear if it's a 401 (unauthorized), not for other errors
                if (response.status === 401) {
                    localStorage.removeItem('authToken');
                    localStorage.removeItem('currentUser');
                }
                return false;
            }
        } catch (error) {
            console.error('[AUTH] Error validating token:', error);
            // Don't clear token on network errors, might be temporary
            return false;
        }
    },
    
    /**
     * Update current user data in localStorage
     * @param {object} user - Updated user object
     */
    updateUser: function(user) {
        if (user) {
            localStorage.setItem('currentUser', JSON.stringify(user));
            console.log('[AUTH] User data updated:', user.username);
        }
    },
    
    /**
     * Get WebSocket URL with authentication token
     * @param {string} baseUrl - Base WebSocket URL (e.g., '/ws/prices')
     * @returns {string} WebSocket URL with token as query parameter
     */
    getWebSocketUrl: function(baseUrl) {
        const token = this.getToken();
        if (token) {
            const separator = baseUrl.includes('?') ? '&' : '?';
            return baseUrl + separator + 'token=' + encodeURIComponent(token);
        }
        return baseUrl;
    }
};

// Auto-validate token on page load if authenticated
if (Auth.isAuthenticated()) {
    Auth.validateToken().catch(err => {
        console.error('Token validation failed:', err);
    });
}

