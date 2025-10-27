package com.spark.collectibles.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Utility class for JSON serialization and deserialization
 * 
 * This class provides methods to convert objects to JSON and vice versa
 * using Gson with proper configuration for the application.
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    
    // Configured Gson instance with proper date/time handling
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    /**
     * Convert an object to JSON string
     * @param object Object to convert
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        try {
            return gson.toJson(object);
        } catch (Exception e) {
            logger.error("Error converting object to JSON", e);
            return "{\"error\": \"Failed to serialize object\"}";
        }
    }
    
    /**
     * Convert JSON string to object of specified class
     * @param json JSON string
     * @param clazz Target class
     * @param <T> Type parameter
     * @return Object of specified class
     * @throws JsonSyntaxException if JSON is invalid
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing JSON: {}", json, e);
            throw e;
        }
    }
    
    /**
     * Check if a string is valid JSON
     * @param json JSON string to validate
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    /**
     * Get the configured Gson instance
     * @return Gson instance
     */
    public static Gson getGson() {
        return gson;
    }
}
