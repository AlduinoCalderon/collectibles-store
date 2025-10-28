# Multi-stage build for Maven project
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-11 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file from build stage
COPY --from=build /app/target/collectibles-store-1.0.0.jar app.jar

# Expose port
EXPOSE 4567

# Set environment variables with defaults (MySQL configuration)
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=collectibles_store
ENV DB_USERNAME=root
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



