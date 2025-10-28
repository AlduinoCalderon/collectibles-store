package com.spark.collectibles.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility class for input validation and sanitization
 * 
 * This class provides methods to validate and sanitize user inputs
 * to prevent SQL injection and ensure data integrity.
 */
public class ValidationUtil {
    
    // Regex patterns for validation
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,50}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,()]{1,255}$");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_]{1,100}$");
    private static final Pattern SEARCH_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,()]{1,100}$");
    
    // SQL injection patterns to detect
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("(?i).*(union\\s+select|insert\\s+into|update\\s+.*\\s+set|delete\\s+from|drop\\s+table|create\\s+table|alter\\s+table|exec\\s*\\(|execute\\s*\\().*"),
        Pattern.compile("(?i).*\\b(script|javascript|vbscript|onload|onerror|onclick)\\b.*"),
        Pattern.compile("(?i).*(or|and)\\s+['\"0-9].*['\"0-9]\\s*=\\s*['\"0-9].*"),
        Pattern.compile("(?i).*'\\s*(or|and)\\s+['\"0-9].*['\"0-9]\\s*=\\s*['\"0-9].*"),
        Pattern.compile("(?i).*'\\s+(or|and)\\s+.*\\s*=\\s*.*\\s*--.*"),
        Pattern.compile("(?i).*'\\s+(or|and)\\s+.*\\s*=\\s*.*\\s*#.*"),
        Pattern.compile("(?i).*';\\s*(drop|delete|insert|update|select|union).*"),
        Pattern.compile("(?i).*'\\s*--.*"),
        Pattern.compile("(?i).*'.*#.*"),
        Pattern.compile("(?i).*(drop|delete|insert|update)\\s.*--.*")
    };
    
    /**
     * Validate product ID
     * @param id Product ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return ID_PATTERN.matcher(id.trim()).matches() && !containsSqlInjection(id);
    }
    
    /**
     * Validate product name
     * @param name Product name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 255 && 
               NAME_PATTERN.matcher(trimmed).matches() && 
               !containsSqlInjection(trimmed);
    }
    
    /**
     * Validate product description
     * @param description Product description to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }
        String trimmed = description.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 2000 && 
               !containsSqlInjection(trimmed);
    }
    
    /**
     * Validate price
     * @param price Price to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPrice(BigDecimal price) {
        if (price == null) {
            return false;
        }
        return price.compareTo(BigDecimal.ZERO) > 0 && 
               price.compareTo(new BigDecimal("999999.99")) <= 0;
    }
    
    /**
     * Validate currency code
     * @param currency Currency code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        return CURRENCY_PATTERN.matcher(currency.trim().toUpperCase()).matches();
    }
    
    /**
     * Validate category
     * @param category Category to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return true; // Category is optional
        }
        String trimmed = category.trim();
        return trimmed.length() <= 100 && 
               CATEGORY_PATTERN.matcher(trimmed).matches() && 
               !containsSqlInjection(trimmed);
    }
    
    /**
     * Validate search query
     * @param query Search query to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        String trimmed = query.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 100 && 
               SEARCH_PATTERN.matcher(trimmed).matches() && 
               !containsSqlInjection(trimmed);
    }
    
    /**
     * Validate price range parameters
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return true if valid, false otherwise
     */
    public static boolean isValidPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            return false;
        }
        return isValidPrice(minPrice) && isValidPrice(maxPrice) && 
               minPrice.compareTo(maxPrice) <= 0;
    }
    
    /**
     * Check if input contains SQL injection patterns
     * @param input Input string to check
     * @return true if contains SQL injection, false otherwise
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }
        
        String lowerInput = input.toLowerCase();
        
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(lowerInput).matches()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Sanitize string input by removing potentially dangerous characters
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove null bytes and control characters
        String sanitized = input.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        // Remove common SQL injection characters and patterns
        sanitized = sanitized.replaceAll("[';\"\\\\]", "");
        
        // Remove SQL keywords and dangerous patterns (case insensitive)
        sanitized = sanitized.replaceAll("(?i)\\s*(DROP|DELETE|INSERT|UPDATE|SELECT|UNION|EXEC|EXECUTE|SCRIPT|JAVASCRIPT|--|/\\*|\\*/|<|>).*", "");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }
    
    /**
     * Validate and sanitize product ID
     * @param id Product ID to validate and sanitize
     * @return Sanitized ID if valid, null otherwise
     */
    public static String validateAndSanitizeId(String id) {
        if (id == null) {
            return null;
        }
        
        // Check for SQL injection BEFORE sanitizing
        if (containsSqlInjection(id)) {
            return null;
        }
        
        String sanitized = sanitizeString(id);
        return isValidId(sanitized) ? sanitized : null;
    }
    
    /**
     * Validate and sanitize product name
     * @param name Product name to validate and sanitize
     * @return Sanitized name if valid, null otherwise
     */
    public static String validateAndSanitizeName(String name) {
        if (name == null) {
            return null;
        }
        
        // Check for SQL injection BEFORE sanitizing
        if (containsSqlInjection(name)) {
            return null;
        }
        
        String sanitized = sanitizeString(name);
        return isValidName(sanitized) ? sanitized : null;
    }
    
    /**
     * Validate and sanitize product description
     * @param description Product description to validate and sanitize
     * @return Sanitized description if valid, null otherwise
     */
    public static String validateAndSanitizeDescription(String description) {
        if (description == null) {
            return null;
        }
        
        // Check for SQL injection BEFORE sanitizing
        if (containsSqlInjection(description)) {
            return null;
        }
        
        String sanitized = sanitizeString(description);
        return isValidDescription(sanitized) ? sanitized : null;
    }
    
    /**
     * Validate and sanitize category
     * @param category Category to validate and sanitize
     * @return Sanitized category if valid, null otherwise
     */
    public static String validateAndSanitizeCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        
        // Check for SQL injection BEFORE sanitizing
        if (containsSqlInjection(category)) {
            return null;
        }
        
        String sanitized = sanitizeString(category);
        return isValidCategory(sanitized) ? sanitized : null;
    }
    
    /**
     * Validate and sanitize search query
     * @param query Search query to validate and sanitize
     * @return Sanitized query if valid, null otherwise
     */
    public static String validateAndSanitizeSearchQuery(String query) {
        if (query == null) {
            return null;
        }
        
        // Check for SQL injection BEFORE sanitizing
        if (containsSqlInjection(query)) {
            return null;
        }
        
        String sanitized = sanitizeString(query);
        return isValidSearchQuery(sanitized) ? sanitized : null;
    }
}


