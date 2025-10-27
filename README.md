# Collectibles Store API

A RESTful e-commerce API for managing collectible items using Java and the Spark framework with PostgreSQL database integration. This project implements a modern web service architecture following SOLID principles with proper error handling, validation, and comprehensive documentation.

## 🚀 Project Overview

This project is part of the Digital NAO Backend Development pathway, focusing on implementing a web application for selling collectible items using Java and the Spark framework. The API provides comprehensive product management functionality with a clean, RESTful interface and proper route grouping for better organization.

### Key Features

- **RESTful API Design**: Clean, intuitive endpoints following REST principles with proper route grouping
- **Product Management**: Complete CRUD operations for collectible items with soft delete functionality
- **PostgreSQL Integration**: Robust database layer with connection pooling and migrations
- **Environment Configuration**: Flexible configuration management with .env support
- **SOLID Principles**: Clean architecture following SOLID design principles
- **Input Validation**: Comprehensive data validation and error handling
- **JSON API**: Full JSON support with proper serialization/deserialization
- **CORS Support**: Cross-origin resource sharing enabled for web clients
- **Database Migrations**: Automated schema management with Flyway
- **Connection Pooling**: High-performance database connections with HikariCP
- **Logging**: Comprehensive logging using Logback
- **Maven Build**: Standard Maven project structure with dependency management

## 📋 API Endpoints

### Product Management (Core Operations)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/products` | Retrieve all products with name and price |
| `GET` | `/api/v1/products/:id` | Retrieve a specific product by ID with description |
| `POST` | `/api/v1/products` | Create a new product |
| `PUT` | `/api/v1/products/:id` | Update an existing product |
| `DELETE` | `/api/v1/products/:id` | Soft delete a product |
| `OPTIONS` | `/api/v1/products/:id` | Check if a product exists |

### Advanced Product Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/products/search?q=query` | Search products by name or description |
| `GET` | `/api/v1/products/category/:category` | Get products by category |
| `GET` | `/api/v1/products/price-range?min=min&max=max` | Get products by price range |
| `GET` | `/api/v1/products/active` | Get active products only |
| `GET` | `/api/v1/products/stats` | Get product statistics |
| `POST` | `/api/v1/products/:id/restore` | Restore soft-deleted product |
| `DELETE` | `/api/v1/products/:id/hard` | Permanently delete a product |

## 🛠️ Technology Stack

- **Java 11**: Core programming language
- **Spark Framework 2.9.4**: Lightweight web framework for Java
- **PostgreSQL 12+**: Primary database
- **HikariCP 5.1.0**: High-performance connection pooling
- **Flyway 10.8.1**: Database migration management
- **Maven**: Dependency management and build automation
- **Gson 2.10.1**: JSON serialization/deserialization
- **Logback 1.4.14**: Logging framework
- **JUnit 5**: Testing framework

## 📦 Project Structure

```
collectibles-store/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── spark/
│   │   │           └── collectibles/
│   │   │               ├── Application.java                    # Main application class
│   │   │               ├── config/
│   │   │               │   └── EnvironmentConfig.java         # Environment configuration
│   │   │               ├── database/
│   │   │               │   ├── DatabaseConnectionManager.java # Database connection management
│   │   │               │   └── DatabaseMigrationManager.java  # Database migrations
│   │   │               ├── model/
│   │   │               │   └── Product.java                   # Product model
│   │   │               ├── repository/
│   │   │               │   ├── ProductRepository.java         # Repository interface
│   │   │               │   └── impl/
│   │   │               │       └── PostgreSQLProductRepository.java # PostgreSQL implementation
│   │   │               ├── service/
│   │   │               │   └── ProductService.java            # Business logic
│   │   │               ├── routes/
│   │   │               │   └── ProductRoutes.java             # API routes
│   │   │               └── util/
│   │   │                   ├── JsonUtil.java                  # JSON utilities
│   │   │                   └── LocalDateTimeAdapter.java      # Date/time serialization
│   │   └── resources/
│   │       ├── application.properties                         # Application configuration
│   │       ├── logback.xml                                   # Logging configuration
│   │       └── db/
│   │           └── migration/                                # Database migration scripts
│   │               ├── V1__Create_products_table.sql
│   │               └── V2__Insert_sample_products.sql
│   └── test/
│       └── java/                                             # Test classes
├── docs/                                                     # Project documentation
│   ├── api_documentation.md                                 # API documentation
│   ├── development_decisions.md                             # Architectural decisions
│   ├── github_setup_commands.md                             # GitHub setup guide
│   └── project_summary.md                                   # Project summary
├── pom.xml                                                  # Maven configuration
├── README.md                                                # This file
├── run.bat                                                  # Windows run script
└── run.sh                                                   # Unix/Linux run script
```

## 🚀 Getting Started

### Prerequisites

- **Java 11** or higher
- **Maven 3.6** or higher
- **PostgreSQL 12** or higher
- **Git** (for version control)

### Installation

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
   # Set environment variables (optional - defaults are in application.properties)
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=collectibles_store
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   ```

4. **Build the project**
   ```bash
   mvn clean compile
   ```

5. **Run the application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.spark.collectibles.Application"
   ```
   
   Or build and run the JAR:
   ```bash
   mvn clean package
   java -jar target/collectibles-store-1.0.0.jar
   ```

6. **Access the API**
   The API will be available at: `http://localhost:4567`

### Running Tests

```bash
mvn test
```

## 📖 API Usage Examples

### Create a Product

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

### Get All Products

```bash
curl -X GET http://localhost:4567/api/v1/products
```

### Get Product by ID

```bash
curl -X GET http://localhost:4567/api/v1/products/item1
```

### Update a Product

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

### Check if Product Exists

```bash
curl -X OPTIONS http://localhost:4567/api/v1/products/item1
```

### Delete a Product (Soft Delete)

```bash
curl -X DELETE http://localhost:4567/api/v1/products/item1
```

### Search Products

```bash
curl -X GET "http://localhost:4567/api/v1/products/search?q=guitarra"
```

### Get Products by Category

```bash
curl -X GET http://localhost:4567/api/v1/products/category/Autographed%20Items
```

### Get Products by Price Range

```bash
curl -X GET "http://localhost:4567/api/v1/products/price-range?min=500&max=800"
```

### Get Product Statistics

```bash
curl -X GET http://localhost:4567/api/v1/products/stats
```

## 🔧 Configuration

### Environment Variables
The API uses environment variables for configuration (with fallback to application.properties):

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

### Maven Configuration

The project uses Maven for dependency management. Key dependencies include:

- **Spark Core**: Web framework
- **PostgreSQL**: Database driver
- **HikariCP**: Connection pooling
- **Flyway**: Database migrations
- **Gson**: JSON processing
- **Logback**: Logging
- **JUnit 5**: Testing

## 📊 Response Format

All API responses follow a consistent JSON format:

### Success Response
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

### Error Response
```json
{
  "message": "Product not found"
}
```

## 🧪 Testing

The project includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## 📝 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Maintain consistent indentation (4 spaces)

### Error Handling
- Use appropriate HTTP status codes
- Provide meaningful error messages
- Log errors for debugging
- Validate input data

### API Design
- Follow RESTful principles
- Use consistent endpoint naming
- Provide clear response formats
- Include proper HTTP headers

## 🚀 Deployment

### Building for Production

```bash
mvn clean package -Pproduction
```

### Docker Support (Optional)

```dockerfile
FROM openjdk:11-jre-slim
COPY target/collectibles-store-1.0.0.jar app.jar
EXPOSE 4567
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 📚 Documentation

- [Project Backlog](docs/backlog.md) - User stories and requirements
- [Project Roadmap](docs/roadmap.md) - Development timeline and milestones
- [API Documentation](docs/api.md) - Detailed API reference

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Rafael**: Lead Developer
- **Ramón**: Client and Domain Expert
- **Sofía**: Technical Advisor

## 📞 Support

For support and questions, please contact:
- Email: support@collectibles-store.com
- GitHub Issues: [Create an issue](https://github.com/yourusername/collectibles-store/issues)

---

**Note**: This project is part of the Digital NAO Backend Development pathway and follows the requirements specified in the project documentation.
