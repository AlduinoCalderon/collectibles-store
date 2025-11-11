package com.spark.collectibles.websocket;

import com.google.gson.Gson;
import com.spark.collectibles.service.AuthService;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * WebSocket handler for real-time price updates
 * Uses Jetty WebSocket API (included with Spark)
 * Requires authentication token in query parameter
 */
@WebSocket
public class PriceWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(PriceWebSocketHandler.class);
    private static final Gson gson = new Gson();
    private static AuthService authService;
    
    // Store all active WebSocket sessions
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    
    /**
     * Set the AuthService instance for token validation
     * This should be called during application initialization
     */
    public static void setAuthService(AuthService authService) {
        PriceWebSocketHandler.authService = authService;
    }
    
    /**
     * Called when a WebSocket client connects
     */
    @OnWebSocketConnect
    public void connected(Session session) {
        // Validate authentication token from query parameters
        String token = session.getUpgradeRequest().getParameterMap().get("token") != null 
            ? session.getUpgradeRequest().getParameterMap().get("token").get(0) 
            : null;
        
        if (token == null || token.trim().isEmpty()) {
            logger.warn("WebSocket connection rejected: No token provided");
            session.close(1008, "Authentication required");
            return;
        }
        
        // Validate token if AuthService is available
        if (authService != null) {
            if (authService.validateToken(token) == null) {
                logger.warn("WebSocket connection rejected: Invalid token");
                session.close(1008, "Invalid or expired token");
                return;
            }
        }
        
        sessions.add(session);
        logger.info("WebSocket client connected (authenticated). Total connections: {}", sessions.size());
    }
    
    /**
     * Called when a WebSocket client disconnects
     */
    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
        logger.info("WebSocket client disconnected. Total connections: {}", sessions.size());
    }
    
    /**
     * Called when a message is received from a WebSocket client
     */
    @OnWebSocketMessage
    public void message(Session session, String message) {
        logger.debug("Received message from client: {}", message);
        // Can handle client messages here if needed
    }
    
    /**
     * Called when a WebSocket error occurs
     */
    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket error: {}", error.getMessage(), error);
    }
    
    /**
     * Broadcast price update to all connected clients
     * This method is static so it can be called from anywhere in the application
     */
    public static void broadcastPriceUpdate(PriceUpdateMessage message) {
        if (sessions.isEmpty()) {
            logger.debug("No active WebSocket connections to broadcast to");
            return;
        }
        
        String jsonMessage = gson.toJson(message);
        int sentCount = 0;
        int failedCount = 0;
        
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendString(jsonMessage);
                    sentCount++;
                } catch (IOException e) {
                    logger.error("Error sending WebSocket message", e);
                    failedCount++;
                }
            } else {
                // Clean up closed sessions
                sessions.remove(session);
            }
        }
        
        logger.info("Broadcasted price update to {}/{} clients ({} failed)", 
                   sentCount, sessions.size(), failedCount);
    }
    
    /**
     * Get the number of active connections
     */
    public static int getActiveConnections() {
        return sessions.size();
    }
}

