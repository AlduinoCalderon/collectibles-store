package com.spark.collectibles.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.spark.collectibles.model.User;
import com.spark.collectibles.repository.UserRepository;
import com.spark.collectibles.repository.impl.MySQLUserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Authentication service for handling user authentication and authorization
 * 
 * This service provides methods for user registration, login, JWT token generation,
 * and password hashing using BCrypt.
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final Algorithm jwtAlgorithm;
    private final int jwtExpirationHours;
    
    public AuthService() {
        this.userRepository = new MySQLUserRepository();
        this.jwtExpirationHours = getJwtExpirationHours();
        String jwtSecret = getJwtSecret();
        this.jwtAlgorithm = Algorithm.HMAC256(jwtSecret);
    }
    
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.jwtExpirationHours = getJwtExpirationHours();
        String jwtSecret = getJwtSecret();
        this.jwtAlgorithm = Algorithm.HMAC256(jwtSecret);
    }
    
    /**
     * Register a new user
     * @param username Username
     * @param email Email address
     * @param password Plain text password
     * @param firstName First name
     * @param lastName Last name
     * @param role User role (defaults to CUSTOMER if null)
     * @return AuthResult containing user and JWT token, or null if registration fails
     */
    public AuthResult register(String username, String email, String password, 
                               String firstName, String lastName, User.UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Registration attempted with empty username");
            return null;
        }
        
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            logger.warn("Registration attempted with invalid email: {}", email);
            return null;
        }
        
        if (password == null || password.length() < 6) {
            logger.warn("Registration attempted with weak password");
            return null;
        }
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            logger.warn("Registration attempted with existing username: {}", username);
            return null;
        }
        
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration attempted with existing email: {}", email);
            return null;
        }
        
        // Hash password
        String passwordHash = hashPassword(password);
        
        // Create user
        User user = new User();
        user.setId(generateUserId());
        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordHash);
        user.setFirstName(firstName != null ? firstName.trim() : "");
        user.setLastName(lastName != null ? lastName.trim() : "");
        user.setRole(role != null ? role : User.UserRole.CUSTOMER);
        user.setActive(true);
        user.touch();
        
        // Save user to database
        User createdUser = userRepository.create(user);
        if (createdUser == null) {
            logger.error("Failed to create user during registration: {}", username);
            return null;
        }
        
        // Generate JWT token
        String token = generateToken(createdUser);
        
        logger.info("User registered successfully: {}", username);
        return new AuthResult(createdUser, token);
    }
    
    /**
     * Login user with username and password
     * @param usernameOrEmail Username or email address
     * @param password Plain text password
     * @return AuthResult containing user and JWT token, or null if login fails
     */
    public AuthResult login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            logger.warn("Login attempted with empty username/email");
            return null;
        }
        
        if (password == null || password.isEmpty()) {
            logger.warn("Login attempted with empty password");
            return null;
        }
        
        // Find user by username or email (with password hash)
        MySQLUserRepository mysqlRepo = (MySQLUserRepository) userRepository;
        User user = null;
        
        // Try username first
        user = mysqlRepo.findByUsernameWithPassword(usernameOrEmail.trim()).orElse(null);
        
        // If not found, try email
        if (user == null) {
            user = mysqlRepo.findByEmailWithPassword(usernameOrEmail.trim().toLowerCase()).orElse(null);
        }
        
        if (user == null) {
            logger.warn("Login attempted with non-existent user: {}", usernameOrEmail);
            return null;
        }
        
        // Check if user is active
        if (!user.isActive()) {
            logger.warn("Login attempted with inactive user: {}", usernameOrEmail);
            return null;
        }
        
        // Verify password
        if (!verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Login attempted with incorrect password for user: {}", usernameOrEmail);
            return null;
        }
        
        // Generate JWT token
        String token = generateToken(user);
        
        // Return user without password hash
        user.setPasswordHash(null);
        
        logger.info("User logged in successfully: {}", usernameOrEmail);
        return new AuthResult(user, token);
    }
    
    /**
     * Validate JWT token and return user
     * @param token JWT token
     * @return User if token is valid, null otherwise
     */
    public User validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            JWTVerifier verifier = JWT.require(jwtAlgorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            
            // Extract user ID from token
            String userId = decodedJWT.getSubject();
            if (userId == null) {
                return null;
            }
            
            // Get user from database
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || !user.isActive()) {
                logger.warn("Token validated but user not found or inactive: {}", userId);
                return null;
            }
            
            return user;
            
        } catch (JWTVerificationException e) {
            logger.warn("JWT token verification failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Hash password using BCrypt
     * @param password Plain text password
     * @return BCrypt hash
     */
    public String hashPassword(String password) {
        int rounds = getBcryptRounds();
        return BCrypt.hashpw(password, BCrypt.gensalt(rounds));
    }
    
    /**
     * Verify password against BCrypt hash
     * @param password Plain text password
     * @param hash BCrypt hash
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
    
    /**
     * Generate JWT token for user
     * @param user User object
     * @return JWT token string
     */
    private String generateToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(jwtExpirationHours * 3600L);
            
            return JWT.create()
                    .withSubject(user.getId())
                    .withClaim("username", user.getUsername())
                    .withClaim("role", user.getRole().name())
                    .withIssuedAt(Date.from(now))
                    .withExpiresAt(Date.from(expiration))
                    .sign(jwtAlgorithm);
                    
        } catch (JWTCreationException e) {
            logger.error("Error generating JWT token for user: {}", user.getId(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
    
    /**
     * Generate unique user ID
     * @return Generated user ID
     */
    private String generateUserId() {
        long maxId = 0;
        try {
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                String id = u.getId();
                if (id != null && id.startsWith("user")) {
                    try {
                        String numPart = id.substring(4); // Skip "user"
                        long num = Long.parseLong(numPart);
                        if (num > maxId) {
                            maxId = num;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore IDs that don't match the pattern
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error generating user ID, using timestamp fallback", e);
            return "user" + System.currentTimeMillis();
        }
        
        return "user" + (maxId + 1);
    }
    
    /**
     * Get JWT secret from environment or use default
     * @return JWT secret
     */
    private String getJwtSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.trim().isEmpty()) {
            logger.warn("JWT_SECRET not set in environment, using default (NOT SECURE FOR PRODUCTION)");
            return "default-jwt-secret-change-in-production-min-32-characters";
        }
        return secret;
    }
    
    /**
     * Get JWT expiration hours from environment or use default
     * @return JWT expiration hours
     */
    private int getJwtExpirationHours() {
        String hoursStr = System.getenv("JWT_EXPIRATION_HOURS");
        if (hoursStr != null && !hoursStr.trim().isEmpty()) {
            try {
                return Integer.parseInt(hoursStr.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid JWT_EXPIRATION_HOURS, using default: 24");
            }
        }
        return 24; // Default: 24 hours
    }
    
    /**
     * Get BCrypt rounds from environment or use default
     * @return BCrypt rounds
     */
    private int getBcryptRounds() {
        String roundsStr = System.getenv("BCRYPT_ROUNDS");
        if (roundsStr != null && !roundsStr.trim().isEmpty()) {
            try {
                int rounds = Integer.parseInt(roundsStr.trim());
                if (rounds >= 4 && rounds <= 31) {
                    return rounds;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid BCRYPT_ROUNDS, using default: 10");
            }
        }
        return 10; // Default: 10 rounds (good balance of security and performance)
    }
    
    /**
     * Authentication result containing user and JWT token
     */
    public static class AuthResult {
        private final User user;
        private final String token;
        
        public AuthResult(User user, String token) {
            this.user = user;
            this.token = token;
        }
        
        public User getUser() {
            return user;
        }
        
        public String getToken() {
            return token;
        }
    }
}

