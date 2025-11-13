/**
 * Unit tests for storage module
 */

import {
    getToken,
    setToken,
    removeToken,
    getCurrentUser,
    setCurrentUser,
    removeCurrentUser,
    isAuthenticated,
    clearAuth
} from '../../../main/resources/static/js/auth/storage.js';

describe('Storage Module', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    describe('Token Management', () => {
        test('should get token from localStorage', () => {
            localStorage.setItem('authToken', 'test-token');
            expect(getToken()).toBe('test-token');
        });

        test('should return null if token not found', () => {
            expect(getToken()).toBeNull();
        });

        test('should save token to localStorage', () => {
            setToken('new-token');
            expect(localStorage.getItem('authToken')).toBe('new-token');
        });

        test('should remove token from localStorage', () => {
            localStorage.setItem('authToken', 'test-token');
            removeToken();
            expect(localStorage.getItem('authToken')).toBeNull();
        });

        test('should not save null token', () => {
            setToken(null);
            expect(localStorage.getItem('authToken')).toBeNull();
        });
    });

    describe('User Management', () => {
        const mockUser = {
            id: 'user1',
            username: 'testuser',
            email: 'test@example.com',
            role: 'ADMIN'
        };

        test('should get current user from localStorage', () => {
            localStorage.setItem('currentUser', JSON.stringify(mockUser));
            const user = getCurrentUser();
            expect(user).toEqual(mockUser);
        });

        test('should return null if user not found', () => {
            expect(getCurrentUser()).toBeNull();
        });

        test('should handle invalid JSON gracefully', () => {
            localStorage.setItem('currentUser', 'invalid-json');
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            const user = getCurrentUser();
            expect(user).toBeNull();
            expect(consoleSpy).toHaveBeenCalled();
            consoleSpy.mockRestore();
        });

        test('should save user to localStorage', () => {
            setCurrentUser(mockUser);
            const stored = JSON.parse(localStorage.getItem('currentUser'));
            expect(stored).toEqual(mockUser);
        });

        test('should remove user from localStorage', () => {
            localStorage.setItem('currentUser', JSON.stringify(mockUser));
            removeCurrentUser();
            expect(localStorage.getItem('currentUser')).toBeNull();
        });

        test('should not save null user', () => {
            setCurrentUser(null);
            expect(localStorage.getItem('currentUser')).toBeNull();
        });
    });

    describe('Authentication Status', () => {
        test('should return true if token exists', () => {
            localStorage.setItem('authToken', 'test-token');
            expect(isAuthenticated()).toBe(true);
        });

        test('should return false if token does not exist', () => {
            expect(isAuthenticated()).toBe(false);
        });
    });

    describe('Clear Auth', () => {
        test('should clear all authentication data', () => {
            localStorage.setItem('authToken', 'test-token');
            localStorage.setItem('currentUser', JSON.stringify({ id: 'user1' }));
            
            clearAuth();
            
            expect(localStorage.getItem('authToken')).toBeNull();
            expect(localStorage.getItem('currentUser')).toBeNull();
        });
    });
});

