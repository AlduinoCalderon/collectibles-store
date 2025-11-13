/**
 * Unit tests for UI module
 */

import {
    updateNavigation,
    hasRole,
    isAdmin,
    initializeNavigation,
    showError,
    showSuccess
} from '../../../main/resources/static/js/auth/ui.js';
import * as storage from '../../../main/resources/static/js/auth/storage.js';

// Mock storage module
jest.mock('../../../main/resources/static/js/auth/storage.js', () => ({
    getCurrentUser: jest.fn(),
    isAuthenticated: jest.fn()
}));

describe('UI Module', () => {
    beforeEach(() => {
        document.body.innerHTML = `
            <div id="nav-auth">
                <a href="/login">Login</a>
                <a href="/register">Register</a>
            </div>
            <div id="nav-user" style="display: none;">
                <span id="nav-username"></span>
                <div id="nav-user-menu">
                    <a href="/profile" class="admin-only">Profile</a>
                    <a href="/admin/products" class="admin-only">Admin</a>
                </div>
            </div>
        `;
        jest.clearAllMocks();
        console.log = jest.fn();
    });

    describe('updateNavigation', () => {
        test('should show user menu for authenticated user', () => {
            const mockUser = { id: 'user1', username: 'testuser', role: 'ADMIN' };
            storage.isAuthenticated.mockReturnValue(true);
            storage.getCurrentUser.mockReturnValue(mockUser);

            updateNavigation(mockUser);

            const navAuth = document.getElementById('nav-auth');
            const navUser = document.getElementById('nav-user');
            const navUsername = document.getElementById('nav-username');

            expect(navAuth.style.display).toBe('none');
            expect(navUser.style.display).toBe('block');
            expect(navUsername.textContent).toBe('testuser');
        });

        test('should show login/register for unauthenticated user', () => {
            storage.isAuthenticated.mockReturnValue(false);
            storage.getCurrentUser.mockReturnValue(null);

            updateNavigation();

            const navAuth = document.getElementById('nav-auth');
            const navUser = document.getElementById('nav-user');

            expect(navAuth.style.display).toBe('block');
            expect(navUser.style.display).toBe('none');
        });

        test('should show admin links for ADMIN role', () => {
            const mockUser = { id: 'user1', username: 'admin', role: 'ADMIN' };
            storage.isAuthenticated.mockReturnValue(true);
            storage.getCurrentUser.mockReturnValue(mockUser);

            updateNavigation(mockUser);

            const adminLinks = document.querySelectorAll('.admin-only');
            adminLinks.forEach(link => {
                expect(link.style.display).toBe('block');
            });
        });

        test('should hide admin links for CUSTOMER role', () => {
            const mockUser = { id: 'user1', username: 'customer', role: 'CUSTOMER' };
            storage.isAuthenticated.mockReturnValue(true);
            storage.getCurrentUser.mockReturnValue(mockUser);

            updateNavigation(mockUser);

            const adminLinks = document.querySelectorAll('.admin-only');
            adminLinks.forEach(link => {
                expect(link.style.display).toBe('none');
            });
        });

        test('should handle missing navigation elements gracefully', () => {
            document.body.innerHTML = '';
            storage.isAuthenticated.mockReturnValue(true);

            // Should not throw
            expect(() => updateNavigation()).not.toThrow();
        });
    });

    describe('hasRole', () => {
        test('should return true if user has role', () => {
            const mockUser = { role: 'ADMIN' };
            storage.getCurrentUser.mockReturnValue(mockUser);

            expect(hasRole('ADMIN')).toBe(true);
        });

        test('should return false if user does not have role', () => {
            const mockUser = { role: 'CUSTOMER' };
            storage.getCurrentUser.mockReturnValue(mockUser);

            expect(hasRole('ADMIN')).toBe(false);
        });

        test('should return false if no user', () => {
            storage.getCurrentUser.mockReturnValue(null);

            expect(hasRole('ADMIN')).toBe(false);
        });
    });

    describe('isAdmin', () => {
        test('should return true for ADMIN role', () => {
            const mockUser = { role: 'ADMIN' };
            storage.getCurrentUser.mockReturnValue(mockUser);

            expect(isAdmin()).toBe(true);
        });

        test('should return false for CUSTOMER role', () => {
            const mockUser = { role: 'CUSTOMER' };
            storage.getCurrentUser.mockReturnValue(mockUser);

            expect(isAdmin()).toBe(false);
        });
    });

    describe('showError', () => {
        test('should display error message', () => {
            const container = document.createElement('div');
            document.body.appendChild(container);

            showError('Test error', container);

            expect(container.innerHTML).toContain('Test error');
            expect(container.querySelector('.alert-error')).not.toBeNull();
        });

        test('should auto-hide error after 5 seconds', (done) => {
            jest.useFakeTimers();
            const container = document.createElement('div');
            document.body.appendChild(container);

            showError('Test error', container);

            expect(container.innerHTML).not.toBe('');

            jest.advanceTimersByTime(5000);

            setTimeout(() => {
                expect(container.innerHTML).toBe('');
                jest.useRealTimers();
                done();
            }, 100);
        });
    });

    describe('showSuccess', () => {
        test('should display success message', () => {
            const container = document.createElement('div');
            document.body.appendChild(container);

            showSuccess('Success!', container);

            expect(container.innerHTML).toContain('Success!');
            expect(container.querySelector('.alert-success')).not.toBeNull();
        });

        test('should handle missing container gracefully', () => {
            expect(() => showSuccess('Success!', null)).not.toThrow();
        });
    });
});

