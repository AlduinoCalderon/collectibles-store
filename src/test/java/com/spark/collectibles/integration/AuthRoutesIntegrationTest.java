package com.spark.collectibles.integration;

import com.google.gson.Gson;
import com.spark.collectibles.Application;
import com.spark.collectibles.model.User;
import com.spark.collectibles.routes.AuthRoutes;
import com.spark.collectibles.service.AuthService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for authentication routes
 * 
 * Tests cover:
 * - User registration endpoint
 * - User login endpoint
 * - Token validation endpoint
 * - Protected route access
 * - Error handling
 */
@ExtendWith(IntegrationTestExtension.class)
@DisplayName("AuthRoutes Integration Tests")
class AuthRoutesIntegrationTest {
    
    private static final String BASE_URL = "http://localhost:4567";
    private static final Gson gson = new Gson();
    private static boolean serverStarted = false;
    
    @BeforeAll
    static void setUpServer() {
        if (!serverStarted) {
            // Set test environment variables
            System.setProperty("APP_ENV", "test");
            System.setProperty("JWT_SECRET", "test-jwt-secret-key-for-integration-testing-minimum-32-characters");
            System.setProperty("JWT_EXPIRATION_HOURS", "24");
            System.setProperty("BCRYPT_ROUNDS", "4"); // Lower rounds for faster tests
            
            // Start Spark server in test mode
            try {
                Application.main(new String[]{});
                Thread.sleep(2000); // Wait for server to start
                serverStarted = true;
            } catch (Exception e) {
                // Server might already be running
            }
        }
    }
    
    @AfterAll
    static void tearDownServer() {
        Spark.stop();
        serverStarted = false;
    }
    
    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() throws IOException {
        // Arrange
        String requestBody = "{\n" +
                "  \"username\": \"testuser\",\n" +
                "  \"email\": \"test@example.com\",\n" +
                "  \"password\": \"password123\",\n" +
                "  \"firstName\": \"Test\",\n" +
                "  \"lastName\": \"User\",\n" +
                "  \"role\": \"CUSTOMER\"\n" +
                "}";
        
        // Act
        HttpURLConnection connection = createConnection("POST", "/api/auth/register");
        sendRequest(connection, requestBody);
        
        // Assert
        int responseCode = connection.getResponseCode();
        String response = getResponse(connection);
        
        assertEquals(201, responseCode, "Expected 201 Created");
        assertTrue(response.contains("token"));
        assertTrue(response.contains("user"));
        assertTrue(response.contains("testuser"));
    }
    
    @Test
    @DisplayName("Should fail registration with duplicate username")
    void testRegisterUser_DuplicateUsername() throws IOException {
        // Arrange - Register first user
        String requestBody1 = "{\n" +
                "  \"username\": \"duplicateuser\",\n" +
                "  \"email\": \"first@example.com\",\n" +
                "  \"password\": \"password123\"\n" +
                "}";
        
        HttpURLConnection conn1 = createConnection("POST", "/api/auth/register");
        sendRequest(conn1, requestBody1);
        assertEquals(201, conn1.getResponseCode());
        
        // Try to register with same username
        String requestBody2 = "{\n" +
                "  \"username\": \"duplicateuser\",\n" +
                "  \"email\": \"second@example.com\",\n" +
                "  \"password\": \"password123\"\n" +
                "}";
        
        // Act
        HttpURLConnection conn2 = createConnection("POST", "/api/auth/register");
        sendRequest(conn2, requestBody2);
        
        // Assert
        assertEquals(409, conn2.getResponseCode(), "Expected 409 Conflict");
        String response = getResponse(conn2);
        assertTrue(response.contains("already exist") || response.contains("failed"));
    }
    
    @Test
    @DisplayName("Should fail registration with invalid email")
    void testRegisterUser_InvalidEmail() throws IOException {
        // Arrange
        String requestBody = "{\n" +
                "  \"username\": \"testuser2\",\n" +
                "  \"email\": \"invalid-email\",\n" +
                "  \"password\": \"password123\"\n" +
                "}";
        
        // Act
        HttpURLConnection connection = createConnection("POST", "/api/auth/register");
        sendRequest(connection, requestBody);
        
        // Assert
        assertEquals(400, connection.getResponseCode(), "Expected 400 Bad Request");
    }
    
    @Test
    @DisplayName("Should login successfully with username")
    void testLogin_WithUsername_Success() throws IOException {
        // Arrange - Register user first
        String registerBody = "{\n" +
                "  \"username\": \"loginuser\",\n" +
                "  \"email\": \"login@example.com\",\n" +
                "  \"password\": \"password123\"\n" +
                "}";
        
        HttpURLConnection registerConn = createConnection("POST", "/api/auth/register");
        sendRequest(registerConn, registerBody);
        assertEquals(201, registerConn.getResponseCode());
        
        // Act - Login
        String loginBody = "{\n" +
                "  \"usernameOrEmail\": \"loginuser\",\n" +
                "  \"password\": \"password123\"\n" +
                "}";
        
        HttpURLConnection loginConn = createConnection("POST", "/api/auth/login");
        sendRequest(loginConn, loginBody);
        
        // Assert
        assertEquals(200, loginConn.getResponseCode(), "Expected 200 OK");
        String response = getResponse(loginConn);
        assertTrue(response.contains("token"));
        assertTrue(response.contains("user"));
    }
    
    @Test
    @DisplayName("Should fail login with wrong password")
    void testLogin_WrongPassword() throws IOException {
        // Arrange - Register user first
        String registerBody = "{\n" +
                "  \"username\": \"wrongpassuser\",\n" +
                "  \"email\": \"wrongpass@example.com\",\n" +
                "  \"password\": \"correctpassword\"\n" +
                "}";
        
        HttpURLConnection registerConn = createConnection("POST", "/api/auth/register");
        sendRequest(registerConn, registerBody);
        assertEquals(201, registerConn.getResponseCode());
        
        // Act - Login with wrong password
        String loginBody = "{\n" +
                "  \"usernameOrEmail\": \"wrongpassuser\",\n" +
                "  \"password\": \"wrongpassword\"\n" +
                "}";
        
        HttpURLConnection loginConn = createConnection("POST", "/api/auth/login");
        sendRequest(loginConn, loginBody);
        
        // Assert
        assertEquals(401, loginConn.getResponseCode(), "Expected 401 Unauthorized");
        String response = getResponse(loginConn);
        assertTrue(response.contains("Invalid") || response.contains("password"));
    }
    
    @Test
    @DisplayName("Should get current user with valid token")
    void testGetCurrentUser_ValidToken() throws IOException {
        // Arrange - Register and login with unique identifiers
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String registerBody = "{\n" +
                "  \"username\": \"currentuser" + uniqueId + "\",\n" +
                "  \"email\": \"current" + uniqueId + "@example.com\",\n" +
                "  \"password\": \"password123\"\n" +
                "}";
        
        HttpURLConnection registerConn = createConnection("POST", "/api/auth/register");
        sendRequest(registerConn, registerBody);
        
        // Ensure registration was successful
        int responseCode = registerConn.getResponseCode();
        String registerResponse = getResponse(registerConn);
        assertEquals(201, responseCode, "Registration should succeed. Response code: " + responseCode + ", Response: " + registerResponse);
        
        assertTrue(registerResponse.contains("token"), "Response should contain token. Response: " + registerResponse);
        assertTrue(registerResponse.contains("user"), "Response should contain user. Response: " + registerResponse);
        
        // Extract token from response
        String token = extractToken(registerResponse);
        assertNotNull(token, "Token should be present in registration response. Response: " + registerResponse);
        
        // Act - Get current user
        HttpURLConnection meConn = createConnection("GET", "/api/auth/me");
        meConn.setRequestProperty("Authorization", "Bearer " + token);
        meConn.connect();
        
        // Assert
        assertEquals(200, meConn.getResponseCode(), "Expected 200 OK");
        String response = getResponse(meConn);
        assertTrue(response.contains("currentuser" + uniqueId), "Response should contain username");
        assertFalse(response.contains("passwordHash")); // Password hash should never be returned
    }
    
    @Test
    @DisplayName("Should fail to get current user without token")
    void testGetCurrentUser_NoToken() throws IOException {
        // Act
        HttpURLConnection connection = createConnection("GET", "/api/auth/me");
        connection.connect();
        
        // Assert
        assertEquals(401, connection.getResponseCode(), "Expected 401 Unauthorized");
    }
    
    @Test
    @DisplayName("Should fail to access protected route without token")
    void testProtectedRoute_NoToken() throws IOException {
        // Arrange
        String requestBody = "{\n" +
                "  \"name\": \"Test Product\",\n" +
                "  \"description\": \"Test\",\n" +
                "  \"price\": 99.99,\n" +
                "  \"currency\": \"USD\"\n" +
                "}";
        
        // Act
        HttpURLConnection connection = createConnection("POST", "/api/products");
        sendRequest(connection, requestBody);
        
        // Assert
        assertEquals(401, connection.getResponseCode(), "Expected 401 Unauthorized");
    }
    
    @Test
    @DisplayName("Should access protected route with valid ADMIN token")
    void testProtectedRoute_WithAdminToken() throws IOException {
        // Arrange - Register admin user with unique identifiers
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String registerBody = "{\n" +
                "  \"username\": \"adminuser" + uniqueId + "\",\n" +
                "  \"email\": \"admin" + uniqueId + "@example.com\",\n" +
                "  \"password\": \"password123\",\n" +
                "  \"role\": \"ADMIN\"\n" +
                "}";
        
        HttpURLConnection registerConn = createConnection("POST", "/api/auth/register");
        sendRequest(registerConn, registerBody);
        
        // Ensure registration was successful
        int responseCode = registerConn.getResponseCode();
        String registerResponse = getResponse(registerConn);
        assertEquals(201, responseCode, "Admin registration should succeed. Response code: " + responseCode + ", Response: " + registerResponse);
        
        assertTrue(registerResponse.contains("token"), "Response should contain token. Response: " + registerResponse);
        assertTrue(registerResponse.contains("user"), "Response should contain user. Response: " + registerResponse);
        
        String token = extractToken(registerResponse);
        assertNotNull(token, "Token should be present in registration response. Response: " + registerResponse);
        
        // Act - Create product with admin token
        String productBody = "{\n" +
                "  \"name\": \"Test Product\",\n" +
                "  \"description\": \"Test Description\",\n" +
                "  \"price\": 99.99,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"category\": \"Test\"\n" +
                "}";
        
        HttpURLConnection productConn = createConnection("POST", "/api/products");
        productConn.setRequestProperty("Authorization", "Bearer " + token);
        sendRequest(productConn, productBody);
        
        // Assert
        int responseCode = productConn.getResponseCode();
        assertTrue(responseCode == 201 || responseCode == 200, "Expected 201 Created or 200 OK");
    }
    
    // Helper methods
    private HttpURLConnection createConnection(String method, String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }
    
    private void sendRequest(HttpURLConnection connection, String body) throws IOException {
        if (body != null && !body.isEmpty()) {
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        }
        connection.connect();
    }
    
    private String getResponse(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() >= 400) {
            return new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    private String extractToken(String jsonResponse) {
        // Try to parse using Gson first
        try {
            com.google.gson.JsonObject jsonObject = gson.fromJson(jsonResponse, com.google.gson.JsonObject.class);
            if (jsonObject != null && jsonObject.has("token")) {
                return jsonObject.get("token").getAsString();
            }
        } catch (Exception e) {
            // Fall back to string parsing
        }
        
        // Fallback: Simple token extraction
        int tokenIndex = jsonResponse.indexOf("\"token\":\"");
        if (tokenIndex == -1) {
            // Try with spaces
            tokenIndex = jsonResponse.indexOf("\"token\" : \"");
            if (tokenIndex != -1) {
                tokenIndex += 2; // Skip " : "
            }
        }
        
        if (tokenIndex == -1) return null;
        
        // Find the start of the token value (after "token":" or "token" : ")
        int start = jsonResponse.indexOf("\"", tokenIndex);
        if (start == -1) return null;
        start = jsonResponse.indexOf("\"", start + 1); // Skip the opening quote
        if (start == -1) return null;
        start += 1; // Start after the quote
        
        // Find the end of the token value
        int end = jsonResponse.indexOf("\"", start);
        if (end == -1) return null;
        
        return jsonResponse.substring(start, end);
    }
}

/**
 * Extension to handle integration test setup/teardown
 */
class IntegrationTestExtension implements org.junit.jupiter.api.extension.BeforeAllCallback, 
                                          org.junit.jupiter.api.extension.AfterAllCallback {
    
    @Override
    public void beforeAll(org.junit.jupiter.api.extension.ExtensionContext context) {
        // Setup handled in test class
    }
    
    @Override
    public void afterAll(org.junit.jupiter.api.extension.ExtensionContext context) {
        // Cleanup handled in test class
    }
}






