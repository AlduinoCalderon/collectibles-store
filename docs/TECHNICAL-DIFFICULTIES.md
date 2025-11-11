# Dificultades Técnicas Encontradas y Estrategias de Solución

## Resumen

Este documento describe las principales dificultades técnicas encontradas durante la implementación del módulo de autenticación y las estrategias utilizadas para resolverlas.

## 1. Migración de Almacenamiento en Memoria a Base de Datos

### Dificultad
El `UserService` original utilizaba un `ConcurrentHashMap` para almacenar usuarios en memoria. Era necesario migrar a persistencia en base de datos manteniendo la compatibilidad con el código existente.

### Estrategia de Solución
- **Patrón Repository**: Implementamos el patrón Repository siguiendo el mismo patrón usado en `ProductRepository`
- **Interfaz común**: Creamos `UserRepository` como interfaz, permitiendo cambiar la implementación sin afectar el servicio
- **Implementación MySQL**: Creamos `MySQLUserRepository` que implementa todas las operaciones usando prepared statements
- **Mocks en tests**: Actualizamos los tests para usar mocks del repositorio, permitiendo testear la lógica de negocio sin necesidad de base de datos

### Resultado
✅ Migración exitosa sin romper la API existente. Los tests ahora son más rápidos y aislados.

---

## 2. Generación y Validación de Tokens JWT

### Dificultad
Implementar un sistema robusto de generación y validación de tokens JWT que:
- Genere tokens seguros
- Valide tokens correctamente
- Maneje tokens expirados
- Soporte el prefijo "Bearer" en headers

### Estrategia de Solución
- **Biblioteca Auth0**: Utilizamos `java-jwt` de Auth0, una biblioteca madura y bien mantenida
- **Secret dinámico**: Implementamos generación de secret desde "Hola mundo" usando SHA-256 cuando no se proporciona `JWT_SECRET`
- **Validación robusta**: Creamos método `validateToken()` que:
  - Maneja tokens null/vacíos
  - Remueve prefijo "Bearer" automáticamente
  - Valida firma y expiración
  - Verifica que el usuario existe y está activo
- **Manejo de errores**: Capturamos `JWTVerificationException` y retornamos null en lugar de lanzar excepciones

### Resultado
✅ Sistema de autenticación JWT funcional y seguro. Los tokens se generan correctamente y la validación es robusta.

---

## 3. Hash de Contraseñas con BCrypt

### Dificultad
Implementar hash seguro de contraseñas que:
- Nunca exponga contraseñas en texto plano
- Use un algoritmo probado y seguro
- Permita verificación eficiente
- Genere hashes únicos para la misma contraseña (sal aleatorio)

### Estrategia de Solución
- **BCrypt**: Elegimos BCrypt por ser el estándar de la industria
- **Biblioteca jbcrypt**: Utilizamos `org.mindrot:jbcrypt` que es ligera y no requiere dependencias adicionales
- **Rondas configurables**: Implementamos soporte para `BCRYPT_ROUNDS` (default: 10)
- **Nunca exponer hash**: El hash de contraseña nunca se retorna en respuestas JSON
- **Verificación segura**: Método `verifyPassword()` que maneja nulls y excepciones

### Resultado
✅ Contraseñas hasheadas correctamente. Cada hash es único gracias al sal aleatorio de BCrypt.

---

## 4. Protección de Rutas con Filtros

### Dificultad
Implementar un sistema de filtros que:
- Proteja rutas específicas
- Permita rutas públicas
- Soporte control de acceso basado en roles
- Maneje errores de autenticación correctamente

### Estrategia de Solución
- **AuthFilter**: Creamos clase `AuthFilter` con métodos:
  - `requireAuth()`: Requiere token válido
  - `requireRole()`: Requiere rol específico
  - `requireAnyRole()`: Requiere uno de varios roles
- **Integración con Spark**: Usamos `before()` de Spark para aplicar filtros antes de las rutas
- **Manejo de errores**: Usamos `halt()` de Spark para detener la ejecución y retornar códigos HTTP apropiados (401, 403)
- **Contexto de usuario**: Almacenamos el usuario autenticado en `request.attribute()` para uso en handlers

### Resultado
✅ Sistema de protección de rutas funcional. Las rutas públicas siguen accesibles y las protegidas requieren autenticación.

---

## 5. Testing con Mocks y Dependencias

### Dificultad
Crear tests unitarios completos que:
- No dependan de base de datos real
- Testen casos edge y manejo de errores
- Alcanzen 90% de cobertura
- Sean rápidos y confiables

### Estrategia de Solución
- **Mockito**: Utilizamos Mockito para mockear dependencias (`UserRepository`, `AuthService`)
- **Tests aislados**: Cada test es independiente y no afecta otros tests
- **Cobertura completa**: Creamos tests para:
  - Casos exitosos
  - Casos de error (null, vacío, inválido)
  - Edge cases (whitespace, caracteres especiales, strings largos)
  - Validaciones de seguridad
- **JaCoCo**: Configuramos JaCoCo para generar reportes de cobertura
- **Tests de integración**: Separamos tests unitarios de tests de integración

### Resultado
✅ Suite de tests completa con alta cobertura. Los tests son rápidos y confiables.

---

## 6. Compatibilidad hacia Atrás

### Dificultad
Asegurar que los cambios no rompan funcionalidad existente:
- Rutas públicas deben seguir funcionando
- WebSocket debe seguir funcionando
- API de productos debe seguir accesible

### Estrategia de Solución
- **Rutas públicas**: Mantenemos todas las rutas GET de productos como públicas
- **Protección selectiva**: Solo protegemos métodos POST/PUT/DELETE
- **Sin cambios en modelos**: Los modelos existentes no se modifican (excepto User que ya existía)
- **Tests de compatibilidad**: Verificamos que rutas públicas funcionen sin autenticación

### Resultado
✅ Compatibilidad hacia atrás mantenida. Todas las funcionalidades existentes siguen funcionando.

---

## 7. Manejo de Errores y Validaciones

### Dificultad
Implementar validaciones robustas que:
- Prevengan datos inválidos
- Proporcionen mensajes de error claros
- Manejen edge cases
- Protejan contra inyección SQL

### Estrategia de Solución
- **Validación en múltiples capas**:
  - Validación en `AuthService` (negocio)
  - Validación en `UserService` (negocio)
  - Validación en repositorio (datos)
- **Prepared Statements**: Todos los queries usan prepared statements para prevenir SQL injection
- **Mensajes de error claros**: Retornamos mensajes específicos (409 para duplicados, 401 para no autorizado, etc.)
- **Validación de entrada**: Validamos username, email, password antes de procesar

### Resultado
✅ Sistema robusto de validación y manejo de errores. Protección contra inyección SQL implementada.

---

## 8. Configuración de Entorno

### Dificultad
Manejar configuración que:
- Funcione en desarrollo y producción
- Use variables de entorno
- Tenga valores por defecto seguros
- Genere secret JWT desde "Hola mundo"

### Estrategia de Solución
- **Variables de entorno**: Priorizamos variables de entorno sobre valores hardcodeados
- **Generación de secret**: Cuando `JWT_SECRET` no está configurado, generamos uno desde "Hola mundo" usando SHA-256
- **Valores por defecto**: Configuramos valores por defecto razonables (24 horas para JWT, 10 rondas para BCrypt)
- **Logging**: Registramos cuando se usan valores por defecto para alertar en producción

### Resultado
✅ Configuración flexible que funciona en todos los entornos. Secret JWT generado desde "Hola mundo" cuando no se proporciona.

---

## 9. Estructura de Tests y Cobertura

### Dificultad
Organizar tests para:
- Alcanzar 90% de cobertura
- Mantener tests mantenibles
- Testear casos edge
- Generar reportes de cobertura

### Estrategia de Solución
- **Organización por clase**: Un test class por clase a testear
- **Tests descriptivos**: Usamos `@DisplayName` para nombres descriptivos
- **Given-When-Then**: Estructuramos tests con patrón Given-When-Then
- **Cobertura de edge cases**: Testeamos null, vacío, valores inválidos, strings largos, caracteres especiales
- **JaCoCo**: Configuramos JaCoCo en `pom.xml` para generar reportes automáticamente

### Resultado
✅ Suite de tests bien organizada con alta cobertura. Reportes de cobertura generados automáticamente.

---

## 10. Documentación y Limpieza

### Dificultad
Mantener documentación:
- Orientada al usuario final
- Específica para implementación
- Sin documentación técnica interna
- Actualizada con cambios

### Estrategia de Solución
- **Documentación de usuario**: Mantenemos solo documentación orientada al usuario
- **Eliminación de docs técnicas**: Removemos documentación interna de desarrollo
- **Guías de uso**: Creamos guías específicas de cómo usar la API
- **Ejemplos prácticos**: Incluimos ejemplos de curl y casos de uso

### Resultado
✅ Documentación limpia y orientada al usuario. Sin documentación técnica interna expuesta.

---

## Lecciones Aprendidas

1. **Patrón Repository**: Facilita testing y cambios de implementación
2. **Mocks en tests**: Permiten tests rápidos y aislados
3. **Validación en múltiples capas**: Asegura robustez del sistema
4. **BCrypt para passwords**: Estándar de la industria por buenas razones
5. **JWT para autenticación**: Ideal para APIs REST stateless
6. **Prepared statements**: Esencial para seguridad SQL
7. **Tests comprehensivos**: Inversión que paga dividendos
8. **Compatibilidad hacia atrás**: Crítica para sistemas en producción

---

## Conclusión

La implementación del módulo de autenticación presentó varios desafíos técnicos, pero todos fueron resueltos exitosamente usando patrones establecidos, bibliotecas probadas y buenas prácticas de desarrollo. El resultado es un sistema robusto, seguro y bien testeado.

