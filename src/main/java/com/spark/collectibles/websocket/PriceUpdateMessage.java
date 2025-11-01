package com.spark.collectibles.websocket;

/**
 * Message class for WebSocket price updates
 * Simple POJO for sending price update notifications to connected clients
 */
public class PriceUpdateMessage {
    private String type;
    private String productId;
    private Double oldPrice;
    private Double newPrice;
    private String currency;
    
    public PriceUpdateMessage() {
    }
    
    public PriceUpdateMessage(String type, String productId, Double oldPrice, 
                             Double newPrice, String currency) {
        this.type = type;
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.currency = currency;
    }
    
    // Getters and Setters
    public String getType() { 
        return type; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }
    
    public String getProductId() { 
        return productId; 
    }
    
    public void setProductId(String productId) { 
        this.productId = productId; 
    }
    
    public Double getOldPrice() { 
        return oldPrice; 
    }
    
    public void setOldPrice(Double oldPrice) { 
        this.oldPrice = oldPrice; 
    }
    
    public Double getNewPrice() { 
        return newPrice; 
    }
    
    public void setNewPrice(Double newPrice) { 
        this.newPrice = newPrice; 
    }
    
    public String getCurrency() { 
        return currency; 
    }
    
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }
}

