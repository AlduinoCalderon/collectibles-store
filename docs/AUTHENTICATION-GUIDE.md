# Guía de Autenticación - Collectibles Store API

## Introducción

Esta guía explica cómo usar el sistema de autenticación de la Collectibles Store API. El sistema utiliza JWT (JSON Web Tokens) para autenticación y control de acceso basado en roles.

## Configuración del Entorno

### Variables de Entorno Requeridas

Para producción, configure estas variables en Render.com:

```bash
JWT_SECRET=tu-secret-key-aqui-minimo-32-caracteres
JWT_EXPIRATION_HOURS=24
BCRYPT_ROUNDS=10
```

**⚠️ IMPORTANTE**: En producción, `JWT_SECRET` debe ser una cadena aleatoria fuerte. Si no se configura, se generará automáticamente desde "Hola mundo" usando SHA-256.

## Endpoints de Autenticación

### 1. Registrar Usuario

**POST** `/api/auth/register`

Registra un nuevo usuario en el sistema.

**Request Body:**
```json
{
  "username": "usuario123",
  "email": "usuario@example.com",
  "password": "password123",
  "firstName": "Nombre",
  "lastName": "Apellido",
  "role": "CUSTOMER"
}
```

**Roles disponibles:**
- `ADMIN` - Administrador (puede gestionar productos y usuarios)
- `CUSTOMER` - Cliente (usuario regular)
- `MODERATOR` - Moderador (reservado para uso futuro)

**Response (201 Created):**
```json
{
  "user": {
    "id": "user1",
    "username": "usuario123",
    "email": "usuario@example.com",
    "firstName": "Nombre",
    "lastName": "Apellido",
    "role": "CUSTOMER",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Ejemplo con curl:**
```bash
curl -X POST https://collectibles-store-09ew.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario123",
    "email": "usuario@example.com",
    "password": "password123",
    "firstName": "Nombre",
    "lastName": "Apellido",
    "role": "CUSTOMER"
  }'
```

### 2. Iniciar Sesión

**POST** `/api/auth/login`

Inicia sesión y obtiene un token JWT.

**Request Body:**
```json
{
  "usernameOrEmail": "usuario123",
  "password": "password123"
}
```

**Nota**: Puede usar username o email en `usernameOrEmail`.

**Response (200 OK):**
```json
{
  "user": {
    "id": "user1",
    "username": "usuario123",
    "email": "usuario@example.com",
    ...
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Ejemplo con curl:**
```bash
curl -X POST https://collectibles-store-09ew.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "usuario123",
    "password": "password123"
  }'
```

### 3. Obtener Usuario Actual

**GET** `/api/auth/me`

Obtiene la información del usuario autenticado actual.

**Headers requeridos:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": "user1",
  "username": "usuario123",
  "email": "usuario@example.com",
  "firstName": "Nombre",
  "lastName": "Apellido",
  "role": "CUSTOMER",
  "isActive": true,
  ...
}
```

**Ejemplo con curl:**
```bash
curl -X GET https://collectibles-store-09ew.onrender.com/api/auth/me \
  -H "Authorization: Bearer <tu-token-jwt>"
```

### 4. Cerrar Sesión

**POST** `/api/auth/logout`

Cierra sesión (el token se elimina del lado del cliente).

**Response (200 OK):**
```json
{
  "message": "Logged out successfully",
  "success": true
}
```

## Uso de Tokens JWT

### Cómo Usar el Token

Una vez que obtiene un token del endpoint de login o registro, debe incluirlo en todas las peticiones a rutas protegidas:

```
Authorization: Bearer <tu-token-jwt>
```

### Ejemplo: Crear Producto (Requiere ADMIN)

```bash
curl -X POST https://collectibles-store-09ew.onrender.com/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <tu-token-jwt>" \
  -d '{
    "name": "Producto Nuevo",
    "description": "Descripción del producto",
    "price": 99.99,
    "currency": "USD",
    "category": "Collectibles"
  }'
```

### Expiración del Token

Los tokens JWT expiran después de 24 horas (configurable con `JWT_EXPIRATION_HOURS`). Cuando un token expira, debe iniciar sesión nuevamente para obtener un nuevo token.

## Rutas Protegidas

### Rutas que Requieren Autenticación ADMIN

- `POST /api/products` - Crear producto
- `PUT /api/products/:id` - Actualizar producto
- `DELETE /api/products/:id` - Eliminar producto
- `POST /api/products/:id/restore` - Restaurar producto eliminado
- `DELETE /api/products/:id/hard` - Eliminación permanente
- `GET /api/users` - Listar usuarios
- `GET /api/users/:id` - Obtener usuario
- `PUT /api/users/:id` - Actualizar usuario
- `DELETE /api/users/:id` - Eliminar usuario

### Rutas Públicas (No Requieren Autenticación)

- `GET /api/products` - Listar productos
- `GET /api/products/:id` - Obtener producto
- `GET /api/products/search?q=query` - Buscar productos
- `GET /api/products/category/:category` - Productos por categoría
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión

## Códigos de Error

### 401 Unauthorized
- Token no proporcionado
- Token inválido
- Token expirado

**Solución**: Inicie sesión nuevamente para obtener un nuevo token.

### 403 Forbidden
- Usuario no tiene el rol requerido (ej: CUSTOMER intentando crear producto)

**Solución**: Use una cuenta con rol ADMIN.

### 409 Conflict
- Usuario o email ya existe al registrar

**Solución**: Use un username o email diferente.

## Seguridad

### Contraseñas
- Las contraseñas se hashean con BCrypt antes de almacenarse
- Nunca se retornan en respuestas de la API
- Mínimo 6 caracteres requeridos

### Tokens JWT
- Firmados con secret key
- Incluyen información de usuario y rol
- Expiran automáticamente

### Protección SQL Injection
- Todos los queries usan prepared statements
- Parámetros validados antes de ejecutar

## Ejemplos Completos

### Flujo Completo: Registrar, Login, Crear Producto

```bash
# 1. Registrar usuario ADMIN
curl -X POST https://collectibles-store-09ew.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'

# Guardar el token de la respuesta
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 2. Crear producto (requiere ADMIN)
curl -X POST https://collectibles-store-09ew.onrender.com/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Guitarra Autografiada",
    "description": "Guitarra autografiada por artista famoso",
    "price": 500.00,
    "currency": "USD",
    "category": "Musical Instruments"
  }'
```

## Soporte

Para problemas o preguntas:
- Revise los códigos de error arriba
- Verifique que el token no haya expirado
- Asegúrese de usar el rol correcto para la operación

---

**Última actualización**: Enero 2024  
**Versión API**: 1.0.0

