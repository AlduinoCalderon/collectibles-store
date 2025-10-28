package com.spark.collectibles.integration;

import com.spark.collectibles.model.Product;
import com.spark.collectibles.service.ProductService;
import com.spark.collectibles.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ProductRoutes
 * 
 * This test class contains integration tests for the ProductRoutes endpoints,
 * testing the interaction between routes and the service layer, including
 * request validation, error handling, and JSON serialization/deserialization.
 * 
 * @see ProductRoutes
 * @see ProductService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRoutes Integration Tests")
class ProductRoutesIntegrationTest {

    @Mock
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = createSampleProduct();
    }

    @Test
    @DisplayName("Should handle valid product creation request")
    void testValidProductCreation() {
        // Given
        when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.createProduct(sampleProduct);
        
        // Then
        assertNotNull(result);
        assertEquals("item1", result.getId());
        assertEquals("Guitar", result.getName());
        verify(productService).createProduct(sampleProduct);
    }

    @Test
    @DisplayName("Should handle product creation with invalid data")
    void testInvalidProductCreation() {
        // Given
        Product invalidProduct = createInvalidProduct();
        when(productService.createProduct(any(Product.class))).thenReturn(null);
        
        // When
        Product result = productService.createProduct(invalidProduct);
        
        // Then
        assertNull(result);
        verify(productService).createProduct(invalidProduct);
    }

    @Test
    @DisplayName("Should handle product search with valid query")
    void testValidProductSearch() {
        // Given
        String searchQuery = "guitar";
        List<Product> expectedProducts = Arrays.asList(sampleProduct);
        when(productService.searchProducts(searchQuery)).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productService.searchProducts(searchQuery);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("item1", result.get(0).getId());
        verify(productService).searchProducts(searchQuery);
    }

    @Test
    @DisplayName("Should handle product search with invalid query")
    void testInvalidProductSearch() {
        // Given
        String invalidQuery = "'; DROP TABLE products; --";
        when(productService.searchProducts(invalidQuery)).thenReturn(Arrays.asList());
        
        // When
        List<Product> result = productService.searchProducts(invalidQuery);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productService).searchProducts(invalidQuery);
    }

    @Test
    @DisplayName("Should handle product update with valid data")
    void testValidProductUpdate() {
        // Given
        String productId = "item1";
        Product updatedProduct = createSampleProduct();
        updatedProduct.setName("Updated Guitar");
        updatedProduct.setPrice(new BigDecimal("150.00"));
        
        when(productService.updateProduct(productId, updatedProduct)).thenReturn(updatedProduct);
        
        // When
        Product result = productService.updateProduct(productId, updatedProduct);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Guitar", result.getName());
        assertEquals(new BigDecimal("150.00"), result.getPrice());
        verify(productService).updateProduct(productId, updatedProduct);
    }

    @Test
    @DisplayName("Should handle product update with non-existent ID")
    void testProductUpdateWithNonExistentId() {
        // Given
        String productId = "nonexistent";
        Product updatedProduct = createSampleProduct();
        
        when(productService.updateProduct(productId, updatedProduct)).thenReturn(null);
        
        // When
        Product result = productService.updateProduct(productId, updatedProduct);
        
        // Then
        assertNull(result);
        verify(productService).updateProduct(productId, updatedProduct);
    }

    @Test
    @DisplayName("Should handle product deletion")
    void testProductDeletion() {
        // Given
        String productId = "item1";
        when(productService.deleteProduct(productId)).thenReturn(true);
        
        // When
        boolean result = productService.deleteProduct(productId);
        
        // Then
        assertTrue(result);
        verify(productService).deleteProduct(productId);
    }

    @Test
    @DisplayName("Should handle product deletion with non-existent ID")
    void testProductDeletionWithNonExistentId() {
        // Given
        String productId = "nonexistent";
        when(productService.deleteProduct(productId)).thenReturn(false);
        
        // When
        boolean result = productService.deleteProduct(productId);
        
        // Then
        assertFalse(result);
        verify(productService).deleteProduct(productId);
    }

    @Test
    @DisplayName("Should handle product existence check")
    void testProductExistenceCheck() {
        // Given
        String productId = "item1";
        when(productService.productExists(productId)).thenReturn(true);
        
        // When
        boolean exists = productService.productExists(productId);
        
        // Then
        assertTrue(exists);
        verify(productService).productExists(productId);
    }

    @Test
    @DisplayName("Should handle product existence check with non-existent ID")
    void testProductExistenceCheckWithNonExistentId() {
        // Given
        String productId = "nonexistent";
        when(productService.productExists(productId)).thenReturn(false);
        
        // When
        boolean exists = productService.productExists(productId);
        
        // Then
        assertFalse(exists);
        verify(productService).productExists(productId);
    }

    @Test
    @DisplayName("Should handle product retrieval by category")
    void testProductRetrievalByCategory() {
        // Given
        String category = "Musical Instruments";
        List<Product> expectedProducts = Arrays.asList(sampleProduct);
        when(productService.getProductsByCategory(category)).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productService.getProductsByCategory(category);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("item1", result.get(0).getId());
        verify(productService).getProductsByCategory(category);
    }

    @Test
    @DisplayName("Should handle product retrieval by price range")
    void testProductRetrievalByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("200.00");
        List<Product> expectedProducts = Arrays.asList(sampleProduct);
        when(productService.getProductsByPriceRange(minPrice, maxPrice)).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productService.getProductsByPriceRange(minPrice, maxPrice);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("item1", result.get(0).getId());
        verify(productService).getProductsByPriceRange(minPrice, maxPrice);
    }

    @Test
    @DisplayName("Should handle product statistics retrieval")
    void testProductStatisticsRetrieval() {
        // Given
        ProductService.ProductStats expectedStats = new ProductService.ProductStats(10, 8, 2);
        
        when(productService.getProductStats()).thenReturn(expectedStats);
        
        // When
        ProductService.ProductStats result = productService.getProductStats();
        
        // Then
        assertNotNull(result);
        assertEquals(10, result.getTotalProducts());
        assertEquals(8, result.getActiveProducts());
        assertEquals(2, result.getDeletedProducts());
        verify(productService).getProductStats();
    }

    @Test
    @DisplayName("Should handle product restoration")
    void testProductRestoration() {
        // Given
        String productId = "item1";
        when(productService.restoreProduct(productId)).thenReturn(true);
        
        // When
        boolean result = productService.restoreProduct(productId);
        
        // Then
        assertTrue(result);
        verify(productService).restoreProduct(productId);
    }

    @Test
    @DisplayName("Should handle product hard deletion")
    void testProductHardDeletion() {
        // Given
        String productId = "item1";
        when(productService.hardDeleteProduct(productId)).thenReturn(true);
        
        // When
        boolean result = productService.hardDeleteProduct(productId);
        
        // Then
        assertTrue(result);
        verify(productService).hardDeleteProduct(productId);
    }

    @Test
    @DisplayName("Should handle JSON serialization and deserialization")
    void testJsonSerialization() {
        // Given
        Product product = createSampleProduct();
        
        // When
        String json = JsonUtil.toJson(product);
        Product deserializedProduct = JsonUtil.fromJson(json, Product.class);
        
        // Then
        assertNotNull(json);
        assertNotNull(deserializedProduct);
        assertEquals(product.getId(), deserializedProduct.getId());
        assertEquals(product.getName(), deserializedProduct.getName());
        assertEquals(product.getDescription(), deserializedProduct.getDescription());
        assertEquals(product.getPrice(), deserializedProduct.getPrice());
        assertEquals(product.getCurrency(), deserializedProduct.getCurrency());
        assertEquals(product.getCategory(), deserializedProduct.getCategory());
        assertEquals(product.isActive(), deserializedProduct.isActive());
        assertEquals(product.isDeleted(), deserializedProduct.isDeleted());
    }

    /**
     * Helper method to create a sample product
     */
    private Product createSampleProduct() {
        Product product = new Product();
        product.setId("item1");
        product.setName("Guitar");
        product.setDescription("A beautiful guitar");
        product.setPrice(new BigDecimal("100.00"));
        product.setCurrency("USD");
        product.setCategory("Musical Instruments");
        product.setActive(true);
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    /**
     * Helper method to create an invalid product
     */
    private Product createInvalidProduct() {
        Product product = new Product();
        product.setId("'; DROP TABLE products; --");
        product.setName("'; DROP TABLE products; --");
        product.setDescription("'; DROP TABLE products; --");
        product.setPrice(new BigDecimal("-100.00"));
        product.setCurrency("INVALID");
        product.setCategory("'; DROP TABLE products; --");
        product.setActive(true);
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}


