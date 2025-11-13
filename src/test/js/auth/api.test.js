/**
 * Unit tests for API module
 */

import {
    login,
    register,
    logout,
    validateToken,
    authenticatedFetch,
    getCurrentUserInfo
} from '../../../main/resources/static/js/auth/api.js';
import * as storage from '../../../main/resources/static/js/auth/storage.js';

// Mock storage module
jest.mock('../../../main/resources/static/js/auth/storage.js', () => ({
    getToken: jest.fn(),
    setToken: jest.fn(),
    setCurrentUser: jest.fn(),
    clearAuth: jest.fn()
}));

describe('API Module', () => {
    beforeEach(() => {
        global.fetch = jest.fn();
        jest.clearAllMocks();
        console.log = jest.fn();
        console.error = jest.fn();
        console.warn = jest.fn();
    });

    describe('authenticatedFetch', () => {
        test('should include Authorization header when token exists', async () => {
            storage.getToken.mockReturnValue('test-token');
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => ({})
            });

            await authenticatedFetch('/api/test');

            expect(fetch).toHaveBeenCalledWith('/api/test', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer test-token'
                }
            });
        });

        test('should not include Authorization header when token does not exist', async () => {
            storage.getToken.mockReturnValue(null);
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => ({})
            });

            await authenticatedFetch('/api/test');

            expect(fetch).toHaveBeenCalledWith('/api/test', {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
        });

        test('should merge custom headers', async () => {
            storage.getToken.mockReturnValue('test-token');
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => ({})
            });

            await authenticatedFetch('/api/test', {
                headers: {
                    'X-Custom-Header': 'custom-value'
                }
            });

            expect(fetch).toHaveBeenCalledWith('/api/test', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer test-token',
                    'X-Custom-Header': 'custom-value'
                }
            });
        });
    });

    describe('login', () => {
        test('should call login endpoint with correct data', async () => {
            const mockResponse = {
                user: { id: 'user1', username: 'test' },
                token: 'test-token'
            };
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockResponse
            });

            const result = await login('test', 'password123');

            expect(fetch).toHaveBeenCalledWith('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    usernameOrEmail: 'test',
                    password: 'password123'
                })
            });

            expect(storage.setToken).toHaveBeenCalledWith('test-token');
            expect(storage.setCurrentUser).toHaveBeenCalledWith(mockResponse.user);
            expect(result).toEqual(mockResponse);
        });

        test('should throw error on failed login', async () => {
            fetch.mockResolvedValueOnce({
                ok: false,
                json: async () => ({ message: 'Invalid credentials' })
            });

            await expect(login('test', 'wrong')).rejects.toThrow('Invalid credentials');
        });
    });

    describe('register', () => {
        test('should call register endpoint with correct data', async () => {
            const mockResponse = {
                user: { id: 'user1', username: 'newuser' },
                token: 'test-token'
            };
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockResponse
            });

            const result = await register('newuser', 'new@example.com', 'password123', 'New', 'User', 'ADMIN');

            expect(fetch).toHaveBeenCalledWith('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: 'newuser',
                    email: 'new@example.com',
                    password: 'password123',
                    firstName: 'New',
                    lastName: 'User',
                    role: 'ADMIN'
                })
            });

            expect(storage.setToken).toHaveBeenCalledWith('test-token');
            expect(storage.setCurrentUser).toHaveBeenCalledWith(mockResponse.user);
            expect(result).toEqual(mockResponse);
        });

        test('should throw error on failed registration', async () => {
            fetch.mockResolvedValueOnce({
                ok: false,
                json: async () => ({ message: 'Username already exists' })
            });

            await expect(register('existing', 'test@example.com', 'password')).rejects.toThrow('Username already exists');
        });
    });

    describe('validateToken', () => {
        test('should return true for valid token', async () => {
            storage.getToken.mockReturnValue('valid-token');
            const mockUser = { id: 'user1', username: 'test' };
            
            fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockUser
            });

            const result = await validateToken();

            expect(fetch).toHaveBeenCalledWith('/api/auth/me', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer valid-token'
                }
            });

            expect(storage.setCurrentUser).toHaveBeenCalledWith(mockUser);
            expect(result).toBe(true);
        });

        test('should return false and clear auth on 401', async () => {
            storage.getToken.mockReturnValue('invalid-token');
            
            fetch.mockResolvedValueOnce({
                ok: false,
                status: 401
            });

            const result = await validateToken();

            expect(result).toBe(false);
            expect(storage.clearAuth).toHaveBeenCalled();
        });

        test('should return false if no token', async () => {
            storage.getToken.mockReturnValue(null);

            const result = await validateToken();

            expect(result).toBe(false);
            expect(fetch).not.toHaveBeenCalled();
        });
    });

    describe('logout', () => {
        test('should clear auth and redirect', () => {
            // Mock window.location
            delete window.location;
            window.location = { href: '' };

            logout();

            expect(storage.clearAuth).toHaveBeenCalled();
            expect(window.location.href).toBe('/login');
        });
    });
});

