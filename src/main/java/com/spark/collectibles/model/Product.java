package com.spark.collectibles.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Product model class representing a collectible item in the e-commerce store
 * 
 * This class contains all the necessary fields for product management
 * with proper validation and soft delete functionality.
 */
public class Product {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("price")
    private BigDecimal price;
    
    @SerializedName("currency")
    private String currency;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("isActive")
    private boolean isActive;
    
    @SerializedName("isDeleted")
    private boolean isDeleted;
    
    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    
    @SerializedName("updatedAt")
    private LocalDateTime updatedAt;
    
    @SerializedName("deletedAt")
    private LocalDateTime deletedAt;
    
    // Default constructor
    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.isDeleted = false;
        this.currency = "USD";
    }
    
    // Constructor with required fields
    public Product(String id, String name, String description, BigDecimal price) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }
    
    // Constructor with all fields
    public Product(String id, String name, String description, BigDecimal price, 
                   String currency, String category) {
        this(id, name, description, price);
        this.currency = currency;
        this.category = category;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    /**
     * Validates the product data
     * @return true if product data is valid, false otherwise
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               price != null && price.compareTo(BigDecimal.ZERO) > 0 &&
               currency != null && !currency.trim().isEmpty();
    }
    
    /**
     * Updates the updatedAt timestamp
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Performs soft delete by setting isDeleted to true and deletedAt timestamp
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
        touch();
    }
    
    /**
     * Restores a soft-deleted product
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.isActive = true;
        touch();
    }
    
    /**
     * Gets formatted price string
     * @return formatted price with currency
     */
    public String getFormattedPrice() {
        return String.format("$%.2f %s", price, currency);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
