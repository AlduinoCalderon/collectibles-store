# Collectibles Store API

A RESTful e-commerce API for managing collectible items using Java and the Spark framework with PostgreSQL database integration. This project implements a modern web service architecture following SOLID principles with proper error handling, validation, and comprehensive documentation.

## 🚀 Project Overview

This project is part of the Digital NAO Backend Development pathway, focusing on implementing a web application for selling collectible items using Java and the Spark framework. The API provides comprehensive product management functionality with a clean, RESTful interface and proper route grouping for better organization.

### Key Features

- **RESTful API Design**: Clean, intuitive endpoints following REST principles
- **Product Management**: Complete CRUD operations for collectible items with soft delete functionality
- **MySQL Integration**: Robust database layer with connection pooling and migrations
- **Environment Configuration**: Flexible configuration management with .env support
- **SOLID Principles**: Clean architecture following SOLID design principles
- **Input Validation**: Comprehensive data validation and error handling
- **JSON API**: Full JSON support with proper serialization/deserialization
- **CORS Support**: Cross-origin resource sharing enabled for web clients
- **Database Migrations**: Automated schema management with Flyway
- **Connection Pooling**: High-performance database connections with HikariCP
- **Logging**: Comprehensive logging using Logback
- **Maven Build**: Standard Maven project structure with dependency management
- **Docker Support**: Containerized deployment ready
- **API Documentation**: Interactive API docs with Scalar
- **CI/CD Pipeline**: GitHub Actions workflow for automated testing and deployment

## 📋 API Endpoints

### Product Management (Core Operations)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | Retrieve all products with name and price |
| `GET` | `/api/products/:id` | Retrieve a specific product by ID with description |
| `POST` | `/api/products` | Create a new product |
| `PUT` | `/api/products/:id` | Update an existing product |
| `DELETE` | `/api/products/:id` | Soft delete a product |
| `OPTIONS` | `/api/products/:id` | Check if a product exists |

### Advanced Product Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products/search?q=query` | Search products by name or description |
| `GET` | `/api/products/category/:category` | Get products by category |
| `GET` | `/api/products/price-range?min=min&max=max` | Get products by price range |
| `GET` | `/api/products/active` | Get active products only |
| `GET` | `/api/products/stats` | Get product statistics |
| `POST` | `/api/products/:id/restore` | Restore soft-deleted product |
| `DELETE` | `/api/products/:id/hard` | Permanently delete a product |

### Web Interfaces

| Page | Endpoint | Description |
|------|----------|-------------|
| `GET` | `/` | Home page |
| `GET` | `/products` | Browse products with filtering |
| `GET` | `/admin/products` | Admin product management (create mode) |
| `GET` | `/admin/products/:id` | Admin product management (edit mode) |

### WebSocket Endpoints

| Endpoint | Description |
|----------|-------------|
| `/ws/prices` | Real-time price updates (WebSocket connection) |

### API Documentation

- **Interactive API Docs**: Available at `/api/docs` (Scalar UI)
- **OpenAPI Specification**: Available at `/api/openapi.json`

## 🛠️ Technology Stack

- **Java 17**: Core programming language
- **Spark Framework 2.9.4**: Lightweight web framework for Java
- **MySQL 8+**: Primary database
- **HikariCP 5.1.0**: High-performance connection pooling
- **Flyway 10.8.1**: Database migration management
- **Maven**: Dependency management and build automation
- **Gson 2.10.1**: JSON serialization/deserialization
- **Logback 1.4.14**: Logging framework
- **Mustache**: Server-side templating engine for views
- **Jetty WebSocket**: Real-time communication (included in Spark)
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for tests
- **Docker**: Containerization
- **Scalar**: Interactive API documentation

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
│   │   │               │       └── PostgreSQLProductRepository.java # MySQL implementation
│   │   │               ├── service/
│   │   │               │   └── ProductService.java            # Business logic
│   │   │               ├── routes/
│   │   │               │   ├── ProductRoutes.java             # API routes
│   │   │               │   └── ViewRoutes.java                # View routes
│   │   │               ├── exception/
│   │   │               │   ├── CollectiblesException.java     # Base exception
│   │   │               │   ├── ProductNotFoundException.java  # Product not found
│   │   │               │   ├── ProductValidationException.java # Validation errors
│   │   │               │   ├── DuplicateProductException.java # Duplicate products
│   │   │               │   ├── DatabaseException.java         # Database errors
│   │   │               │   └── ExceptionHandler.java          # Exception handling
│   │   │               ├── websocket/
│   │   │               │   ├── PriceWebSocketHandler.java     # WebSocket handler
│   │   │               │   └── PriceUpdateMessage.java        # WebSocket messages
│   │   │               └── util/
│   │   │                   ├── JsonUtil.java                  # JSON utilities
│   │   │                   ├── ErrorHandler.java              # Error utilities
│   │   │                   ├── ValidationUtil.java            # Validation utilities
│   │   │                   └── LocalDateTimeAdapter.java      # Date/time serialization
│   │   └── resources/
│   │       ├── application.properties                         # Application configuration
│   │       ├── logback.xml                                   # Logging configuration
│   │       ├── templates/                                    # Mustache templates
│   │       │   ├── products.mustache                         # Product browsing page
│   │       │   ├── admin/
│   │       │   │   └── product-form.mustache                # Admin form
│   │       │   └── error.mustache                            # Error pages
│   │       └── db/
│   │           └── migration/                                # Database migration scripts
│   │               ├── V1__Create_products_table.sql
│   │               └── V2__Insert_sample_products.sql
│   └── test/
│       └── java/                                             # Test classes
├── docs/                                                     # Project documentation
│   ├── openapi.json                                         # OpenAPI specification
│   ├── backlog.md                                           # Project backlog
│   └── roadmap.md                                           # Development roadmap
│   └── project_gantt.html                                   # Project Gantt chart
├── .github/
│   └── workflows/
│       └── ci-cd.yml                                        # GitHub Actions workflow
├── pom.xml                                                  # Maven configuration
├── Dockerfile                                               # Docker configuration
├── README.md                                                # This file
├── run.bat                                                  # Windows run script
└── run.sh                                                   # Unix/Linux run script
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **MySQL 8** or higher
- **Docker** (optional, for containerized deployment)
- **Git** (for version control)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/collectibles-store.git
   cd collectibles-store
   ```

2. **Set up MySQL database**
   ```bash
   # Create database using MySQL client
   mysql -u root -p -e "CREATE DATABASE collectibles_store;"
   
   # Or login to MySQL and create database
   mysql -u root -p
   CREATE DATABASE collectibles_store;
   ```

3. **Configure environment variables**
   ```bash
   # Set environment variables (optional - defaults are in application.properties)
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=collectibles_store
   export DB_USERNAME=root
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
   - API: `http://localhost:4567`
   - Interactive Docs: `http://localhost:4567/api/docs`
   - OpenAPI Spec: `http://localhost:4567/api/openapi.json`

### Docker Deployment

1. **Build Docker image**
   ```bash
   docker build -t collectibles-store .
   ```

2. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Run standalone container**
   ```bash
   docker run -p 4567:4567 \
     -e DB_HOST=your-db-host \
     -e DB_PASSWORD=your-password \
     collectibles-store
   ```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## 📖 API Usage Examples

### Create a Product

```bash
curl -X POST http://localhost:4567/api/products \
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
curl -X GET http://localhost:4567/api/products
```

### Get Product by ID

```bash
curl -X GET http://localhost:4567/api/products/item1
```

### Update a Product

```bash
curl -X PUT http://localhost:4567/api/products/item1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gorra autografiada por Peso Pluma - Limited Edition",
    "description": "Una gorra autografiada por el famoso Peso Pluma. Edición limitada.",
    "price": 750.00,
    "currency": "USD",
    "category": "Autographed Items"
  }'
```

### Search Products

```bash
curl -X GET "http://localhost:4567/api/products/search?q=guitarra"
```

### Get Products by Category

```bash
curl -X GET http://localhost:4567/api/products/category/Autographed%20Items
```

### Get Products by Price Range

```bash
curl -X GET "http://localhost:4567/api/products/price-range?min=500&max=800"
```

## 🔧 Configuration

### Environment Variables
The API uses environment variables for configuration (with fallback to application.properties):

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=collectibles_store
DB_USERNAME=root
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
API_BASE_PATH=/api
```

### Database Schema
The API uses MySQL with the following main table:

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

## 🚀 Deployment

### Building for Production

```bash
mvn clean package -Pproduction
```

### Docker Support

The project uses a multi-stage Dockerfile that builds and runs the application:

```dockerfile
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/collectibles-store-1.0.0.jar app.jar
EXPOSE 4567
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Render Deployment

The project is configured for easy deployment on Render:

1. Connect your GitHub repository to Render
2. Set environment variables in Render dashboard
3. Deploy automatically via GitHub Actions

## 📚 Documentation

- [Project Backlog](docs/backlog.md) - User stories and requirements
- [Project Roadmap](docs/roadmap.md) - Development timeline and milestones
- [OpenAPI Specification](docs/openapi.json) - Complete API specification

## 🎯 Development Sprints

### Sprint 2: Exception Handling, Views & Templates ✅

**Completed Features:**
- **Exception Handling Module**: Custom exception hierarchy with centralized handling
  - `CollectiblesException` base class
  - `ProductNotFoundException`, `ProductValidationException`, `DuplicateProductException`
  - `DatabaseException` for database errors
  - `ExceptionHandler` for unified error management
  
- **Views and Templates**: Modern web interface using Mustache templates
  - Product browsing page with filtering (search, category, price range)
  - Admin form for product management (create/edit/delete)
  - Error pages with proper status code handling
  - Responsive, modern UI design
  
- **Web Form**: Full-featured admin interface
  - Real-time form submission using Fetch API
  - Client-side and server-side validation
  - Product list with inline edit/delete actions
  - Form auto-population for edit mode

**Technical Implementation:**
- Added Mustache template engine integration
- Implemented `ViewRoutes.java` for view handling
- Separate exception handling for API (JSON) vs Views (HTML)
- REST API integration with web forms

### Sprint 3: WebSocket Real-Time Updates ✅

**Completed Features:**
- **WebSocket Implementation**: Real-time bidirectional communication
  - `PriceWebSocketHandler` using Jetty WebSocket API
  - `PriceUpdateMessage` POJO for price notifications
  - WebSocket endpoint at `/ws/prices`
  - Broadcast updates to all connected clients
  
- **Real-Time Price Updates**: Automatic UI updates
  - Connection indicator (🟢 Connected / 🔴 Disconnected)
  - Price change animation with visual feedback
  - Automatic reconnection logic (10 attempts)
  - WebSocket integration in `ProductService.updateProduct()`

- **Admin UI Enhancements**:
  - Auto-generated product IDs (removed manual input)
  - Read-only ID display in edit mode
  - Advanced table filtering (search by name/ID, filter by category)
  - Improved user experience with instant filtering

**Technical Implementation:**
- Jetty WebSocket API (included in Spark framework)
- Static broadcast method for price updates
- Client-side JavaScript for WebSocket connection management
- Integration with existing product update workflow

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