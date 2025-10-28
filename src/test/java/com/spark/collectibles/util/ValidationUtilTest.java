package com.spark.collectibles.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Test class for ValidationUtil
 */
@DisplayName("ValidationUtil Tests")
class ValidationUtilTest {

    @Test
    @DisplayName("Should validate valid product ID")
    void testValidId() {
        assertTrue(ValidationUtil.isValidId("item1"));
        assertTrue(ValidationUtil.isValidId("product-123"));
        assertTrue(ValidationUtil.isValidId("item_456"));
        assertTrue(ValidationUtil.isValidId("a".repeat(50)));
    }

    @Test
    @DisplayName("Should reject invalid product ID")
    void testInvalidId() {
        assertFalse(ValidationUtil.isValidId(null));
        assertFalse(ValidationUtil.isValidId(""));
        assertFalse(ValidationUtil.isValidId(" "));
        assertFalse(ValidationUtil.isValidId("item with spaces"));
        assertFalse(ValidationUtil.isValidId("item@special"));
        assertFalse(ValidationUtil.isValidId("a".repeat(51)));
        assertFalse(ValidationUtil.isValidId("item'; DROP TABLE products; --"));
    }

    @Test
    @DisplayName("Should validate valid product name")
    void testValidName() {
        assertTrue(ValidationUtil.isValidName("Guitar"));
        assertTrue(ValidationUtil.isValidName("Guitar - Special Edition"));
        assertTrue(ValidationUtil.isValidName("Item (Limited)"));
        assertTrue(ValidationUtil.isValidName("a".repeat(255)));
    }

    @Test
    @DisplayName("Should reject invalid product name")
    void testInvalidName() {
        assertFalse(ValidationUtil.isValidName(null));
        assertFalse(ValidationUtil.isValidName(""));
        assertFalse(ValidationUtil.isValidName(" "));
        assertFalse(ValidationUtil.isValidName("a".repeat(256)));
        assertFalse(ValidationUtil.isValidName("Item<script>alert('xss')</script>"));
        assertFalse(ValidationUtil.isValidName("Item'; DROP TABLE products; --"));
    }

    @Test
    @DisplayName("Should validate valid product description")
    void testValidDescription() {
        assertTrue(ValidationUtil.isValidDescription("A beautiful guitar"));
        assertTrue(ValidationUtil.isValidDescription("A".repeat(2000)));
        assertTrue(ValidationUtil.isValidDescription("Guitar with special features and details"));
    }

    @Test
    @DisplayName("Should reject invalid product description")
    void testInvalidDescription() {
        assertFalse(ValidationUtil.isValidDescription(null));
        assertFalse(ValidationUtil.isValidDescription(""));
        assertFalse(ValidationUtil.isValidDescription(" "));
        assertFalse(ValidationUtil.isValidDescription("A".repeat(2001)));
        assertFalse(ValidationUtil.isValidDescription("Description<script>alert('xss')</script>"));
        assertFalse(ValidationUtil.isValidDescription("Description'; DROP TABLE products; --"));
    }

    @Test
    @DisplayName("Should validate valid price")
    void testValidPrice() {
        assertTrue(ValidationUtil.isValidPrice(new BigDecimal("10.50")));
        assertTrue(ValidationUtil.isValidPrice(new BigDecimal("0.01")));
        assertTrue(ValidationUtil.isValidPrice(new BigDecimal("999999.99")));
    }

    @Test
    @DisplayName("Should reject invalid price")
    void testInvalidPrice() {
        assertFalse(ValidationUtil.isValidPrice(null));
        assertFalse(ValidationUtil.isValidPrice(new BigDecimal("0")));
        assertFalse(ValidationUtil.isValidPrice(new BigDecimal("-10.50")));
        assertFalse(ValidationUtil.isValidPrice(new BigDecimal("1000000.00")));
    }

    @Test
    @DisplayName("Should validate valid currency")
    void testValidCurrency() {
        assertTrue(ValidationUtil.isValidCurrency("USD"));
        assertTrue(ValidationUtil.isValidCurrency("EUR"));
        assertTrue(ValidationUtil.isValidCurrency("GBP"));
        assertTrue(ValidationUtil.isValidCurrency("usd")); // Should be case insensitive
    }

    @Test
    @DisplayName("Should reject invalid currency")
    void testInvalidCurrency() {
        assertFalse(ValidationUtil.isValidCurrency(null));
        assertFalse(ValidationUtil.isValidCurrency(""));
        assertFalse(ValidationUtil.isValidCurrency(" "));
        assertFalse(ValidationUtil.isValidCurrency("US"));
        assertFalse(ValidationUtil.isValidCurrency("USDD"));
        assertFalse(ValidationUtil.isValidCurrency("123"));
    }

    @Test
    @DisplayName("Should validate valid category")
    void testValidCategory() {
        assertTrue(ValidationUtil.isValidCategory("Musical Instruments"));
        assertTrue(ValidationUtil.isValidCategory("Clothing"));
        assertTrue(ValidationUtil.isValidCategory("Electronics-2024"));
        assertTrue(ValidationUtil.isValidCategory("a".repeat(100)));
        assertTrue(ValidationUtil.isValidCategory(null)); // Category is optional
        assertTrue(ValidationUtil.isValidCategory("")); // Category is optional
    }

    @Test
    @DisplayName("Should reject invalid category")
    void testInvalidCategory() {
        assertFalse(ValidationUtil.isValidCategory("a".repeat(101)));
        assertFalse(ValidationUtil.isValidCategory("Category<script>alert('xss')</script>"));
        assertFalse(ValidationUtil.isValidCategory("Category'; DROP TABLE products; --"));
    }

    @Test
    @DisplayName("Should validate valid search query")
    void testValidSearchQuery() {
        assertTrue(ValidationUtil.isValidSearchQuery("guitar"));
        assertTrue(ValidationUtil.isValidSearchQuery("electric guitar"));
        assertTrue(ValidationUtil.isValidSearchQuery("guitar-2024"));
        assertTrue(ValidationUtil.isValidSearchQuery("a".repeat(100)));
    }

    @Test
    @DisplayName("Should reject invalid search query")
    void testInvalidSearchQuery() {
        assertFalse(ValidationUtil.isValidSearchQuery(null));
        assertFalse(ValidationUtil.isValidSearchQuery(""));
        assertFalse(ValidationUtil.isValidSearchQuery(" "));
        assertFalse(ValidationUtil.isValidSearchQuery("a".repeat(101)));
        assertFalse(ValidationUtil.isValidSearchQuery("guitar<script>alert('xss')</script>"));
        assertFalse(ValidationUtil.isValidSearchQuery("guitar'; DROP TABLE products; --"));
    }

    @Test
    @DisplayName("Should validate valid price range")
    void testValidPriceRange() {
        assertTrue(ValidationUtil.isValidPriceRange(
            new BigDecimal("10.00"), new BigDecimal("100.00")));
        assertTrue(ValidationUtil.isValidPriceRange(
            new BigDecimal("50.00"), new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("Should reject invalid price range")
    void testInvalidPriceRange() {
        assertFalse(ValidationUtil.isValidPriceRange(null, new BigDecimal("100.00")));
        assertFalse(ValidationUtil.isValidPriceRange(new BigDecimal("10.00"), null));
        assertFalse(ValidationUtil.isValidPriceRange(
            new BigDecimal("100.00"), new BigDecimal("10.00")));
        assertFalse(ValidationUtil.isValidPriceRange(
            new BigDecimal("0"), new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Should detect SQL injection patterns")
    void testSqlInjectionDetection() {
        assertTrue(ValidationUtil.containsSqlInjection("'; DROP TABLE products; --"));
        assertTrue(ValidationUtil.containsSqlInjection("' OR '1'='1"));
        assertTrue(ValidationUtil.containsSqlInjection("1' UNION SELECT * FROM users --"));
        assertTrue(ValidationUtil.containsSqlInjection("admin'--"));
        assertTrue(ValidationUtil.containsSqlInjection("1' OR 1=1 --"));
        assertTrue(ValidationUtil.containsSqlInjection("'; INSERT INTO products VALUES ('hack', 'hack', 0); --"));
        assertTrue(ValidationUtil.containsSqlInjection("1' AND 1=1 --"));
        assertTrue(ValidationUtil.containsSqlInjection("' OR 1=1 #"));
        assertTrue(ValidationUtil.containsSqlInjection("1' OR '1'='1' --"));
    }

    @Test
    @DisplayName("Should not detect false positives for SQL injection")
    void testNoFalsePositives() {
        assertFalse(ValidationUtil.containsSqlInjection("guitar"));
        assertFalse(ValidationUtil.containsSqlInjection("electric guitar"));
        assertFalse(ValidationUtil.containsSqlInjection("guitar-2024"));
        assertFalse(ValidationUtil.containsSqlInjection("Musical Instruments"));
        assertFalse(ValidationUtil.containsSqlInjection("USD"));
        assertFalse(ValidationUtil.containsSqlInjection("item1"));
        assertFalse(ValidationUtil.containsSqlInjection("A beautiful guitar"));
        assertFalse(ValidationUtil.containsSqlInjection(null));
    }

    @Test
    @DisplayName("Should sanitize strings correctly")
    void testSanitizeString() {
        assertEquals("guitar", ValidationUtil.sanitizeString("guitar"));
        assertEquals("guitar", ValidationUtil.sanitizeString("  guitar  "));
        assertEquals("guitar", ValidationUtil.sanitizeString("guitar'; DROP TABLE products; --"));
        assertEquals("guitar", ValidationUtil.sanitizeString("guitar\""));
        assertEquals("guitar", ValidationUtil.sanitizeString("guitar\\"));
        assertEquals("guitar", ValidationUtil.sanitizeString("guitar\n"));
        assertEquals("guitar", ValidationUtil.sanitizeString("guitar\t"));
        assertNull(ValidationUtil.sanitizeString(null));
    }

    @Test
    @DisplayName("Should validate and sanitize ID correctly")
    void testValidateAndSanitizeId() {
        assertEquals("item1", ValidationUtil.validateAndSanitizeId("item1"));
        assertEquals("item1", ValidationUtil.validateAndSanitizeId("  item1  "));
        assertNull(ValidationUtil.validateAndSanitizeId("item1'; DROP TABLE products; --"));
        assertNull(ValidationUtil.validateAndSanitizeId("item with spaces"));
        assertNull(ValidationUtil.validateAndSanitizeId(null));
    }

    @Test
    @DisplayName("Should validate and sanitize name correctly")
    void testValidateAndSanitizeName() {
        assertEquals("Guitar", ValidationUtil.validateAndSanitizeName("Guitar"));
        assertEquals("Guitar", ValidationUtil.validateAndSanitizeName("  Guitar  "));
        assertNull(ValidationUtil.validateAndSanitizeName("Guitar'; DROP TABLE products; --"));
        assertNull(ValidationUtil.validateAndSanitizeName("Guitar<script>alert('xss')</script>"));
        assertNull(ValidationUtil.validateAndSanitizeName(null));
    }

    @Test
    @DisplayName("Should validate and sanitize description correctly")
    void testValidateAndSanitizeDescription() {
        assertEquals("A beautiful guitar", ValidationUtil.validateAndSanitizeDescription("A beautiful guitar"));
        assertEquals("A beautiful guitar", ValidationUtil.validateAndSanitizeDescription("  A beautiful guitar  "));
        assertNull(ValidationUtil.validateAndSanitizeDescription("A beautiful guitar'; DROP TABLE products; --"));
        assertNull(ValidationUtil.validateAndSanitizeDescription("A beautiful guitar<script>alert('xss')</script>"));
        assertNull(ValidationUtil.validateAndSanitizeDescription(null));
    }

    @Test
    @DisplayName("Should validate and sanitize category correctly")
    void testValidateAndSanitizeCategory() {
        assertEquals("Musical Instruments", ValidationUtil.validateAndSanitizeCategory("Musical Instruments"));
        assertEquals("Musical Instruments", ValidationUtil.validateAndSanitizeCategory("  Musical Instruments  "));
        assertNull(ValidationUtil.validateAndSanitizeCategory("Musical Instruments'; DROP TABLE products; --"));
        assertNull(ValidationUtil.validateAndSanitizeCategory("Musical Instruments<script>alert('xss')</script>"));
        assertNull(ValidationUtil.validateAndSanitizeCategory(null));
        assertNull(ValidationUtil.validateAndSanitizeCategory(""));
    }

    @Test
    @DisplayName("Should validate and sanitize search query correctly")
    void testValidateAndSanitizeSearchQuery() {
        assertEquals("guitar", ValidationUtil.validateAndSanitizeSearchQuery("guitar"));
        assertEquals("guitar", ValidationUtil.validateAndSanitizeSearchQuery("  guitar  "));
        assertNull(ValidationUtil.validateAndSanitizeSearchQuery("guitar'; DROP TABLE products; --"));
        assertNull(ValidationUtil.validateAndSanitizeSearchQuery("guitar<script>alert('xss')</script>"));
        assertNull(ValidationUtil.validateAndSanitizeSearchQuery(null));
    }
}


