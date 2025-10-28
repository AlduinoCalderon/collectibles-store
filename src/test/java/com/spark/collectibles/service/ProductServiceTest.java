package com.spark.collectibles.service;

import com.spark.collectibles.model.Product;
import com.spark.collectibles.repository.ProductRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService
 * 
 * This test class contains comprehensive unit tests for the ProductService class,
 * covering all CRUD operations, validation logic, and edge cases using Mockito
 * for mocking dependencies.
 * 
 * @see ProductService
 * @see ProductRepository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Use constructor injection with the mock repository
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("Should get all products successfully")
    void testGetAllProducts() {
        // Given
        List<Product> expectedProducts = Arrays.asList(
            createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00")),
            createSampleProduct("item2", "Piano", "A grand piano", new BigDecimal("500.00"))
        );
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // When
        List<Product> actualProducts = productService.getAllProducts();

        // Then
        assertNotNull(actualProducts);
        assertEquals(2, actualProducts.size());
        assertEquals("item1", actualProducts.get(0).getId());
        assertEquals("item2", actualProducts.get(1).getId());
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById() {
        // Given
        String productId = "item1";
        Product expectedProduct = createSampleProduct(productId, "Guitar", "A beautiful guitar", new BigDecimal("100.00"));
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        // When
        Product actualProduct = productService.getProductById(productId);

        // Then
        assertNotNull(actualProduct);
        assertEquals(productId, actualProduct.getId());
        assertEquals("Guitar", actualProduct.getName());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should return null when product not found")
    void testGetProductByIdNotFound() {
        // Given
        String productId = "nonexistent";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Product actualProduct = productService.getProductById(productId);

        // Then
        assertNull(actualProduct);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() {
        // Given
        Product product = createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00"));
        when(productRepository.existsById(product.getId())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        Product createdProduct = productService.createProduct(product);

        // Then
        assertNotNull(createdProduct);
        assertEquals("item1", createdProduct.getId());
        assertEquals("Guitar", createdProduct.getName());
        verify(productRepository).existsById(product.getId());
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should return null when creating existing product")
    void testCreateProductAlreadyExists() {
        // Given
        Product product = createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00"));
        when(productRepository.existsById(product.getId())).thenReturn(true);

        // When
        Product createdProduct = productService.createProduct(product);

        // Then
        assertNull(createdProduct);
        verify(productRepository).existsById(product.getId());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() {
        // Given
        String productId = "item1";
        Product existingProduct = createSampleProduct(productId, "Guitar", "A beautiful guitar", new BigDecimal("100.00"));
        Product updatedProduct = createSampleProduct(productId, "Guitar Updated", "A beautiful guitar updated", new BigDecimal("150.00"));
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findAll()).thenReturn(Arrays.asList(existingProduct));
        when(productRepository.update(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.updateProduct(productId, updatedProduct);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Guitar Updated", result.getName());
        verify(productRepository).findById(productId);
        verify(productRepository).update(any(Product.class));
    }

    @Test
    @DisplayName("Should return null when updating non-existent product")
    void testUpdateProductNotFound() {
        // Given
        String productId = "nonexistent";
        Product product = createSampleProduct(productId, "Guitar", "A beautiful guitar", new BigDecimal("100.00"));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Product updatedProduct = productService.updateProduct(productId, product);

        // Then
        assertNull(updatedProduct);
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() {
        // Given
        String productId = "item1";
        when(productRepository.softDeleteById(productId)).thenReturn(true);

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertTrue(result);
        verify(productRepository).softDeleteById(productId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent product")
    void testDeleteProductNotFound() {
        // Given
        String productId = "nonexistent";
        when(productRepository.softDeleteById(productId)).thenReturn(false);

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertFalse(result);
        verify(productRepository).softDeleteById(productId);
    }

    @Test
    @DisplayName("Should check if product exists")
    void testProductExists() {
        // Given
        String productId = "item1";
        when(productRepository.existsById(productId)).thenReturn(true);

        // When
        boolean exists = productService.productExists(productId);

        // Then
        assertTrue(exists);
        verify(productRepository).existsById(productId);
    }

    @Test
    @DisplayName("Should search products successfully")
    void testSearchProducts() {
        // Given
        String query = "guitar";
        List<Product> expectedProducts = Arrays.asList(
            createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00"))
        );
        when(productRepository.search(query)).thenReturn(expectedProducts);

        // When
        List<Product> actualProducts = productService.searchProducts(query);

        // Then
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals("item1", actualProducts.get(0).getId());
        verify(productRepository).search(query);
    }

    @Test
    @DisplayName("Should get products by category successfully")
    void testGetProductsByCategory() {
        // Given
        String category = "Musical Instruments";
        List<Product> expectedProducts = Arrays.asList(
            createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00"))
        );
        when(productRepository.findByCategory(category)).thenReturn(expectedProducts);

        // When
        List<Product> actualProducts = productService.getProductsByCategory(category);

        // Then
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals("item1", actualProducts.get(0).getId());
        verify(productRepository).findByCategory(category);
    }

    @Test
    @DisplayName("Should get products by price range successfully")
    void testGetProductsByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("200.00");
        List<Product> expectedProducts = Arrays.asList(
            createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00"))
        );
        when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(expectedProducts);

        // When
        List<Product> actualProducts = productService.getProductsByPriceRange(minPrice, maxPrice);

        // Then
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals("item1", actualProducts.get(0).getId());
        verify(productRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    @DisplayName("Should get active products successfully")
    void testGetActiveProducts() {
        // Given
        List<Product> expectedProducts = Arrays.asList(
            createSampleProduct("item1", "Guitar", "A beautiful guitar", new BigDecimal("100.00"))
        );
        when(productRepository.findActive()).thenReturn(expectedProducts);

        // When
        List<Product> actualProducts = productService.getActiveProducts();

        // Then
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals("item1", actualProducts.get(0).getId());
        verify(productRepository).findActive();
    }

    @Test
    @DisplayName("Should get product statistics successfully")
    void testGetProductStats() {
        // Given
        when(productRepository.count()).thenReturn(10L);
        when(productRepository.countActive()).thenReturn(8L);

        // When
        ProductService.ProductStats stats = productService.getProductStats();

        // Then
        assertNotNull(stats);
        assertEquals(10, stats.getTotalProducts());
        assertEquals(8, stats.getActiveProducts());
        assertEquals(2, stats.getDeletedProducts());
        verify(productRepository).count();
        verify(productRepository).countActive();
    }

    @Test
    @DisplayName("Should restore product successfully")
    void testRestoreProduct() {
        // Given
        String productId = "item1";
        when(productRepository.restoreById(productId)).thenReturn(true);

        // When
        boolean result = productService.restoreProduct(productId);

        // Then
        assertTrue(result);
        verify(productRepository).restoreById(productId);
    }

    @Test
    @DisplayName("Should return false when restoring non-existent product")
    void testRestoreProductNotFound() {
        // Given
        String productId = "nonexistent";
        when(productRepository.restoreById(productId)).thenReturn(false);

        // When
        boolean result = productService.restoreProduct(productId);

        // Then
        assertFalse(result);
        verify(productRepository).restoreById(productId);
    }

    @Test
    @DisplayName("Should hard delete product successfully")
    void testHardDeleteProduct() {
        // Given
        String productId = "item1";
        when(productRepository.hardDeleteById(productId)).thenReturn(true);

        // When
        boolean result = productService.hardDeleteProduct(productId);

        // Then
        assertTrue(result);
        verify(productRepository).hardDeleteById(productId);
    }

    @Test
    @DisplayName("Should return false when hard deleting non-existent product")
    void testHardDeleteProductNotFound() {
        // Given
        String productId = "nonexistent";
        when(productRepository.hardDeleteById(productId)).thenReturn(false);

        // When
        boolean result = productService.hardDeleteProduct(productId);

        // Then
        assertFalse(result);
        verify(productRepository).hardDeleteById(productId);
    }

    /**
     * Helper method to create a sample product
     */
    private Product createSampleProduct(String id, String name, String description, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCurrency("USD");
        product.setCategory("Musical Instruments");
        product.setActive(true);
        product.setDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}


