# Use OpenJDK 11 as base image
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file
COPY target/collectibles-store-1.0.0.jar app.jar

# Expose port
EXPOSE 4567

# Set environment variables with defaults
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=collectibles_store
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=password
ENV APP_PORT=4567
ENV APP_ENV=production
ENV LOG_LEVEL=INFO
ENV DB_MAX_CONNECTIONS=10
ENV DB_MIN_CONNECTIONS=2
ENV DB_CONNECTION_TIMEOUT=30000
ENV API_BASE_PATH=/api

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:4567/api/products || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]


