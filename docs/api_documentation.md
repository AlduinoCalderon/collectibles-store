# Collectibles Store API Documentation

## 🎯 API Overview

The Collectibles Store API is a RESTful e-commerce API built with Java and the Spark framework, designed to manage collectible items with PostgreSQL database integration. The API follows REST principles and implements proper route grouping for better organization.

## 🏗️ Architecture & Route Grouping

### API Base Structure
```
Base URL: http://localhost:4567
API Version: v1
Base Path: /api/v1
```

### Route Grouping Strategy
All product-related endpoints are grouped under `/api/v1/products` for better organization and maintainability:

```
/api/v1/products/          - Main product collection
/api/v1/products/:id       - Individual product operations
/api/v1/products/search    - Search functionality
/api/v1/products/category  - Category-based operations
/api/v1/products/price-range - Price-based filtering
```

## 📋 API Endpoints

### Core Product Operations

#### 1. Get All Products
**Endpoint:** `GET /api/v1/products`  
**Description:** Retrieve a list of all products with their name and price  
**Response:** Array of product objects

```json
[
  {
    "id": "item1",
    "name": "Gorra autografiada por Peso Pluma",
    "description": "Una gorra autografiada por el famoso Peso Pluma.",
    "price": 621.34,
    "currency": "USD",
    "category": "Autographed Items",
    "isActive": true,
    "isDeleted": false,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Product by ID
**Endpoint:** `GET /api/v1/products/:id`  
**Description:** Retrieve a specific product by ID with full description  
**Parameters:** `id` (string) - Product identifier  
**Response:** Product object

```json
{
  "id": "item1",
  "name": "Gorra autografiada por Peso Pluma",
  "description": "Una gorra autografiada por el famoso Peso Pluma.",
  "price": 621.34,
  "currency": "USD",
  "category": "Autographed Items",
  "isActive": true,
  "isDeleted": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Product
**Endpoint:** `POST /api/v1/products`  
**Description:** Create a new product  
**Request Body:** Product object (without ID)  
**Response:** Created product object (201 Created)

```json
{
  "name": "New Collectible Item",
  "description": "Description of the new item",
  "price": 299.99,
  "currency": "USD",
  "category": "Collectibles"
}
```

#### 4. Update Product
**Endpoint:** `PUT /api/v1/products/:id`  
**Description:** Update an existing product  
**Parameters:** `id` (string) - Product identifier  
**Request Body:** Updated product object  
**Response:** Updated product object

#### 5. Delete Product (Soft Delete)
**Endpoint:** `DELETE /api/v1/products/:id`  
**Description:** Soft delete a product (marks as deleted but preserves data)  
**Parameters:** `id` (string) - Product identifier  
**Response:** Status message

```json
{
  "message": "Product deleted successfully",
  "success": true
}
```

#### 6. Check Product Exists
**Endpoint:** `OPTIONS /api/v1/products/:id`  
**Description:** Check whether a product with the given ID exists  
**Parameters:** `id` (string) - Product identifier  
**Response:** Status message

```json
{
  "message": "Product exists",
  "success": true
}
```

### Advanced Product Operations

#### 7. Search Products
**Endpoint:** `GET /api/v1/products/search?q=query`  
**Description:** Search products by name or description  
**Parameters:** `q` (string) - Search query  
**Response:** Array of matching products

#### 8. Get Products by Category
**Endpoint:** `GET /api/v1/products/category/:category`  
**Description:** Get all products in a specific category  
**Parameters:** `category` (string) - Category name  
**Response:** Array of products in the category

#### 9. Get Products by Price Range
**Endpoint:** `GET /api/v1/products/price-range?min=minPrice&max=maxPrice`  
**Description:** Get products within a price range  
**Parameters:** 
- `min` (decimal) - Minimum price
- `max` (decimal) - Maximum price  
**Response:** Array of products in the price range

#### 10. Get Active Products
**Endpoint:** `GET /api/v1/products/active`  
**Description:** Get only active (non-deleted) products  
**Response:** Array of active products

#### 11. Get Product Statistics
**Endpoint:** `GET /api/v1/products/stats`  
**Description:** Get product statistics  
**Response:** Statistics object

```json
{
  "totalProducts": 7,
  "activeProducts": 7,
  "deletedProducts": 0
}
```

#### 12. Restore Product
**Endpoint:** `POST /api/v1/products/:id/restore`  
**Description:** Restore a soft-deleted product  
**Parameters:** `id` (string) - Product identifier  
**Response:** Status message

#### 13. Hard Delete Product
**Endpoint:** `DELETE /api/v1/products/:id/hard`  
**Description:** Permanently delete a product  
**Parameters:** `id` (string) - Product identifier  
**Response:** Status message

## 🔧 Configuration

### Environment Variables
The API uses environment variables for configuration:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=collectibles_store
DB_USERNAME=postgres
DB_PASSWORD=password

# Application Configuration
APP_PORT=4567
APP_ENV=development
LOG_LEVEL=INFO

# Database Connection Pool
DB_MAX_CONNECTIONS=10
DB_MIN_CONNECTIONS=2
DB_CONNECTION_TIMEOUT=30000

# API Configuration
API_VERSION=v1
API_BASE_PATH=/api
```

### Database Schema
The API uses PostgreSQL with the following main table:

```sql
CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    category VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);
```

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Git

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/collectibles-store.git
   cd collectibles-store
   ```

2. **Set up PostgreSQL database**
   ```bash
   # Create database
   createdb collectibles_store
   
   # Or using psql
   psql -U postgres -c "CREATE DATABASE collectibles_store;"
   ```

3. **Configure environment variables**
   ```bash
   # Set environment variables or update application.properties
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=collectibles_store
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   ```

4. **Build and run the application**
   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="com.spark.collectibles.Application"
   ```

5. **Verify the API is running**
   ```bash
   curl http://localhost:4567/api/v1/products
   ```

## 📝 API Usage Examples

### Basic CRUD Operations

#### Create a Product
```bash
curl -X POST http://localhost:4567/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Guitarra autografiada por Coldplay",
    "description": "Una guitarra eléctrica autografiada por la popular banda británica Coldplay",
    "price": 458.91,
    "currency": "USD",
    "category": "Musical Instruments"
  }'
```

#### Get All Products
```bash
curl -X GET http://localhost:4567/api/v1/products
```

#### Get Product by ID
```bash
curl -X GET http://localhost:4567/api/v1/products/item1
```

#### Update a Product
```bash
curl -X PUT http://localhost:4567/api/v1/products/item1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gorra autografiada por Peso Pluma - Limited Edition",
    "description": "Una gorra autografiada por el famoso Peso Pluma. Edición limitada.",
    "price": 750.00,
    "currency": "USD",
    "category": "Autographed Items"
  }'
```

#### Delete a Product (Soft Delete)
```bash
curl -X DELETE http://localhost:4567/api/v1/products/item1
```

#### Check if Product Exists
```bash
curl -X OPTIONS http://localhost:4567/api/v1/products/item1
```

### Advanced Operations

#### Search Products
```bash
curl -X GET "http://localhost:4567/api/v1/products/search?q=guitarra"
```

#### Get Products by Category
```bash
curl -X GET http://localhost:4567/api/v1/products/category/Autographed%20Items
```

#### Get Products by Price Range
```bash
curl -X GET "http://localhost:4567/api/v1/products/price-range?min=500&max=800"
```

#### Get Product Statistics
```bash
curl -X GET http://localhost:4567/api/v1/products/stats
```

#### Restore a Deleted Product
```bash
curl -X POST http://localhost:4567/api/v1/products/item1/restore
```

## 🔒 Error Handling

The API returns appropriate HTTP status codes and error messages:

### Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "message": "Error description"
}
```

### Common Error Scenarios
1. **Invalid Product ID**: Returns 400 with "Product ID is required"
2. **Product Not Found**: Returns 404 with "Product not found"
3. **Invalid JSON**: Returns 400 with "Invalid JSON format"
4. **Duplicate Product**: Returns 409 with "Product already exists"
5. **Server Error**: Returns 500 with "Internal server error"

## 🏗️ SOLID Principles Implementation

### Single Responsibility Principle (SRP)
- Each class has a single responsibility
- `ProductService` handles business logic
- `ProductRepository` handles data access
- `ProductRoutes` handles HTTP routing

### Open/Closed Principle (OCP)
- Repository interface allows for different implementations
- Service layer is open for extension, closed for modification

### Liskov Substitution Principle (LSP)
- PostgreSQL repository can be substituted for any repository implementation
- All implementations follow the same contract

### Interface Segregation Principle (ISP)
- Repository interface is focused and specific
- No unnecessary dependencies

### Dependency Inversion Principle (DIP)
- High-level modules depend on abstractions (interfaces)
- Low-level modules implement interfaces

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Coverage
- Unit tests for service layer
- Integration tests for repository layer
- API endpoint testing

## 📊 Performance Considerations

### Database Optimization
- Indexes on frequently queried columns
- Connection pooling with HikariCP
- Prepared statements for security and performance

### Caching Strategy
- Database connection pooling
- Query result caching (future enhancement)

### Monitoring
- Comprehensive logging with Logback
- Database connection pool monitoring
- Request/response logging

## 🔮 Future Enhancements

### Planned Features
1. **Authentication & Authorization**: JWT-based authentication
2. **Rate Limiting**: API rate limiting and throttling
3. **Caching**: Redis integration for better performance
4. **File Upload**: Image upload for products
5. **Pagination**: Paginated responses for large datasets
6. **WebSocket**: Real-time updates for price changes
7. **Search**: Advanced search with Elasticsearch
8. **Analytics**: Product analytics and reporting

### Technical Improvements
1. **Docker**: Containerization support
2. **Kubernetes**: Orchestration and scaling
3. **Monitoring**: Application performance monitoring
4. **Documentation**: OpenAPI/Swagger documentation
5. **CI/CD**: Automated testing and deployment

## 📞 Support

For support and questions:
- GitHub Issues: [Create an issue](https://github.com/yourusername/collectibles-store/issues)
- Documentation: [API Documentation](docs/api_documentation.md)
- Email: support@collectibles-store.com

---

**API Version**: v1  
**Last Updated**: 2024-01-15  
**Maintainer**: Collectibles Store Development Team
