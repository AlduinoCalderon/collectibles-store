# Solution Explanation: Collectibles Store API

## 🎯 Problem Statement

Based on the Spanish instructions provided, the requirements were to:

1. **Create a RESTful API** that implements the required functionality for an online store
2. **Use proper route grouping** for better organization
3. **Implement specific endpoints**:
   - Return a list of articles with name and price, plus an ID
   - Given an arbitrary ID, return the description of the indicated article
4. **Focus on products** (not users) for an e-commerce "chop bacon" store
5. **Implement all CRUD operations** (GET, POST, PUT, DELETE) with RESTful design
6. **Enable soft deletion** logic
7. **Work with PostgreSQL database** with configuration from environment files
8. **Follow SOLID principles** and best practices

## 🏗️ Solution Architecture

### Route Grouping Strategy

The solution implements a well-organized route structure following REST principles:

```
Base URL: http://localhost:4567
API Version: v1
Base Path: /api/v1

Product Routes Group:
├── /api/v1/products                    # Main product collection
├── /api/v1/products/:id               # Individual product operations
├── /api/v1/products/search            # Search functionality
├── /api/v1/products/category/:category # Category-based operations
├── /api/v1/products/price-range       # Price-based filtering
├── /api/v1/products/active            # Active products only
├── /api/v1/products/stats             # Product statistics
└── /api/v1/products/:id/restore       # Restore soft-deleted products
```

### Key Design Decisions

#### 1. **RESTful API Design**
- **GET /api/v1/products** - Returns list of all products with name and price (as requested)
- **GET /api/v1/products/:id** - Returns specific product with full description (as requested)
- **POST /api/v1/products** - Creates new products
- **PUT /api/v1/products/:id** - Updates existing products
- **DELETE /api/v1/products/:id** - Soft deletes products
- **OPTIONS /api/v1/products/:id** - Checks if product exists

#### 2. **Route Grouping Benefits**
- **Logical Organization**: All product-related endpoints under `/api/v1/products`
- **Scalability**: Easy to add new product-related endpoints
- **Maintainability**: Clear separation of concerns
- **API Versioning**: Built-in versioning support with `/api/v1`
- **Consistency**: Uniform URL structure across all endpoints

#### 3. **Database Integration**
- **PostgreSQL**: Robust, production-ready database
- **Connection Pooling**: HikariCP for optimal performance
- **Migrations**: Flyway for schema management
- **Environment Configuration**: Flexible configuration management

#### 4. **SOLID Principles Implementation**

**Single Responsibility Principle (SRP)**:
- `ProductService` - Handles business logic only
- `ProductRepository` - Handles data access only
- `ProductRoutes` - Handles HTTP routing only
- `DatabaseConnectionManager` - Handles database connections only

**Open/Closed Principle (OCP)**:
- Repository interface allows different implementations
- Service layer is extensible without modification

**Liskov Substitution Principle (LSP)**:
- PostgreSQL repository can be substituted for any repository implementation
- All implementations follow the same contract

**Interface Segregation Principle (ISP)**:
- Focused, specific interfaces
- No unnecessary dependencies

**Dependency Inversion Principle (DIP)**:
- High-level modules depend on abstractions
- Low-level modules implement interfaces

## 🔧 Technical Implementation

### 1. **Product Model**
Based on the provided `items.json` structure:

```java
public class Product {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String category;
    private boolean isActive;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
```

### 2. **Soft Delete Implementation**
- **Soft Delete**: Products are marked as deleted but data is preserved
- **Restore Functionality**: Soft-deleted products can be restored
- **Hard Delete**: Permanent deletion option available
- **Filtering**: Active products are filtered by default

### 3. **Database Schema**
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

### 4. **Environment Configuration**
- **Flexible Configuration**: Environment variables with fallback to properties file
- **Database Settings**: Configurable database connection parameters
- **Application Settings**: Port, logging, and API configuration
- **Connection Pool**: Configurable connection pool settings

## 📋 API Endpoints Implementation

### Core Requirements Fulfilled

#### 1. **List of Articles with Name and Price**
**Endpoint**: `GET /api/v1/products`
**Response**: Array of products with name, price, and ID
```json
[
  {
    "id": "item1",
    "name": "Gorra autografiada por Peso Pluma",
    "price": 621.34,
    "currency": "USD"
  }
]
```

#### 2. **Product Description by ID**
**Endpoint**: `GET /api/v1/products/:id`
**Response**: Complete product information including description
```json
{
  "id": "item1",
  "name": "Gorra autografiada por Peso Pluma",
  "description": "Una gorra autografiada por el famoso Peso Pluma.",
  "price": 621.34,
  "currency": "USD"
}
```

### Additional RESTful Operations

#### 3. **Create Product**
**Endpoint**: `POST /api/v1/products`
**Purpose**: Add new products to the store

#### 4. **Update Product**
**Endpoint**: `PUT /api/v1/products/:id`
**Purpose**: Modify existing product information

#### 5. **Delete Product (Soft Delete)**
**Endpoint**: `DELETE /api/v1/products/:id`
**Purpose**: Remove products while preserving data

#### 6. **Check Product Exists**
**Endpoint**: `OPTIONS /api/v1/products/:id`
**Purpose**: Verify product existence

### Advanced E-commerce Features

#### 7. **Search Products**
**Endpoint**: `GET /api/v1/products/search?q=query`
**Purpose**: Find products by name or description

#### 8. **Category Filtering**
**Endpoint**: `GET /api/v1/products/category/:category`
**Purpose**: Get products by category

#### 9. **Price Range Filtering**
**Endpoint**: `GET /api/v1/products/price-range?min=min&max=max`
**Purpose**: Filter products by price range

#### 10. **Product Statistics**
**Endpoint**: `GET /api/v1/products/stats`
**Purpose**: Get store statistics

## 🚀 Usage Examples

### Basic Operations

#### Get All Products (Name and Price)
```bash
curl -X GET http://localhost:4567/api/v1/products
```

#### Get Product Description by ID
```bash
curl -X GET http://localhost:4567/api/v1/products/item1
```

#### Create New Product
```bash
curl -X POST http://localhost:4567/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Collectible Item",
    "description": "Description of the new item",
    "price": 299.99,
    "currency": "USD",
    "category": "Collectibles"
  }'
```

#### Update Product
```bash
curl -X PUT http://localhost:4567/api/v1/products/item1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product Name",
    "description": "Updated description",
    "price": 399.99,
    "currency": "USD",
    "category": "Updated Category"
  }'
```

#### Soft Delete Product
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

#### Restore Deleted Product
```bash
curl -X POST http://localhost:4567/api/v1/products/item1/restore
```

## 🏆 Benefits of This Solution

### 1. **RESTful Design**
- Clean, intuitive API endpoints
- Proper HTTP methods and status codes
- Consistent response formats
- Easy to understand and use

### 2. **Route Grouping**
- Logical organization of endpoints
- Scalable architecture
- Easy maintenance and extension
- Clear API structure

### 3. **Database Integration**
- Production-ready PostgreSQL database
- Connection pooling for performance
- Automated schema migrations
- Soft delete functionality

### 4. **SOLID Principles**
- Clean, maintainable code
- Easy to test and extend
- Proper separation of concerns
- Flexible architecture

### 5. **Environment Configuration**
- Flexible configuration management
- Easy deployment across environments
- Secure credential management
- Configurable settings

### 6. **E-commerce Ready**
- Product management functionality
- Search and filtering capabilities
- Category organization
- Price range filtering
- Statistics and analytics

## 📊 Technical Specifications

### Performance
- **Connection Pooling**: HikariCP for optimal database performance
- **Indexed Queries**: Database indexes on frequently queried columns
- **Prepared Statements**: SQL injection protection and performance
- **Efficient Queries**: Optimized database queries

### Security
- **Input Validation**: Comprehensive data validation
- **SQL Injection Protection**: Prepared statements
- **Error Handling**: Secure error messages
- **CORS Support**: Cross-origin resource sharing

### Maintainability
- **Clean Architecture**: SOLID principles implementation
- **Comprehensive Logging**: Detailed logging for debugging
- **Documentation**: Extensive API documentation
- **Testing**: Unit tests for core functionality

## 🎯 Conclusion

This solution successfully addresses all the requirements:

1. ✅ **RESTful API** with proper CRUD operations
2. ✅ **Route grouping** for better organization
3. ✅ **Required endpoints** for listing products and getting descriptions
4. ✅ **Product-focused** e-commerce functionality
5. ✅ **Soft deletion** logic implemented
6. ✅ **PostgreSQL integration** with environment configuration
7. ✅ **SOLID principles** and best practices followed

The API is production-ready, well-documented, and provides a solid foundation for a collectibles e-commerce store. The route grouping makes it easy to understand and extend, while the SOLID architecture ensures maintainability and scalability.

---

**Solution Status**: ✅ **COMPLETE** - All requirements fulfilled  
**Architecture**: RESTful API with proper route grouping  
**Database**: PostgreSQL with soft delete functionality  
**Principles**: SOLID design principles implemented  
**Documentation**: Comprehensive API documentation provided
