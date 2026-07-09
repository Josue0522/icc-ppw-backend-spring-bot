# Práctica 11: Autenticación y Autorización con JWT

## 1. Tema

Frameworks Backend: Spring Boot – Seguridad con Spring Security y JSON Web Tokens (JWT).

En esta práctica se implementó un sistema completo de autenticación y autorización *stateless* para la API REST del proyecto `fundamentos01`. Hasta este punto, todos los endpoints estaban completamente abiertos: cualquier persona podía consultar, crear, modificar o eliminar recursos sin ningún control de acceso.

Se incorporó **Spring Security** junto con **JWT (JSON Web Tokens)** para verificar la identidad de los usuarios (autenticación) y controlar qué pueden hacer una vez identificados (autorización), sin depender de sesiones en el servidor.

---

## 2. Objetivo

Implementar autenticación basada en tokens usando:

- Spring Security (`spring-boot-starter-security`)
- JWT con la librería `jjwt` (`io.jsonwebtoken`)
- `BCryptPasswordEncoder` para el hash seguro de contraseñas
- Relación `ManyToMany` entre usuarios y roles
- Filtros personalizados de autenticación (`OncePerRequestFilter`)
- Manejo centralizado de errores de autenticación (`AuthenticationEntryPoint`)
- Endpoints públicos de registro e inicio de sesión
- Protección automática del resto de la API

---

## 3. Problema identificado

Antes de esta práctica, endpoints como:

```txt
GET  /api/products
POST /api/products
DELETE /api/users/{id}
```

eran accesibles por cualquier cliente, sin importar si estaba identificado o no. En una aplicación real esto es inaceptable: se necesita saber **quién** hace la petición y **qué permisos** tiene antes de dejarlo operar sobre los datos.

---

## 4. Estrategia de implementación

Se optó por un esquema **stateless** (sin sesiones HTTP), ideal para APIs REST:

- **JWT** en lugar de sesiones basadas en cookies: cada petición incluye su propio token, el servidor no guarda estado.
- **Tabla separada de roles** (`RoleEntity`) en vez de un simple campo de texto en el usuario, permitiendo asignar múltiples roles y escalar a futuro.
- **BCrypt** para el hash de contraseñas, con *salt* aleatorio automático.
- **Filtro de autenticación** (`JwtAuthenticationFilter`) que valida el token en cada request antes de llegar al controlador.

---

## 5. Estructura de paquetes creada

```txt
security/
├── config/
│   ├── JwtProperties.java
│   ├── SecurityConfig.java
│   └── SecurityDataInitializer.java
├── controllers/
│   └── AuthController.java
├── dtos/
│   ├── AuthResponseDto.java
│   ├── LoginRequestDto.java
│   └── RegisterRequestDto.java
├── entities/
│   └── RoleEntity.java
├── enums/
│   └── RoleName.java
├── filters/
│   ├── JwtAuthenticationEntryPoint.java
│   └── JwtAuthenticationFilter.java
├── repositories/
│   └── RoleRepository.java
├── services/
│   ├── AuthService.java
│   ├── UserDetailsImpl.java
│   └── UserDetailsServiceImpl.java
└── utils/
    └── JwtUtil.java
```

Adicionalmente se modificó:

- `users/entity/UserEntity.java` → se agregó la relación `@ManyToMany` con `RoleEntity`.
- `users/repository/UserRepository.java` → se agregaron los métodos `findByEmailAndDeletedFalse` y `existsByEmail`.

---

## 6. Dependencias agregadas

Archivo: `build.gradle.kts`

```kotlin
dependencies {
    // ============== SEGURIDAD: Spring Security + JWT ==============
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Jackson 2 clásico (Spring Boot 4.1 usa Jackson 3 por defecto)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
```

> **Nota técnica:** este proyecto usa Spring Boot 4.1, que adoptó Jackson 3 (`tools.jackson`) como serializador JSON por defecto. Como la librería `jjwt-jackson` y nuestro manejador de errores dependen de las clases clásicas de Jackson 2 (`com.fasterxml.jackson`), fue necesario declararlas explícitamente como dependencia de compilación.

---

## 7. Configuración de JWT

Archivo: `src/main/resources/application.yaml`

```yaml
jwt:
  secret: ${JWT_SECRET:mySecretKeyForJWT2024MustBeAtLeast256BitsLongForHS256Algorithm}
  expiration: 1800000          # 30 minutos
  refresh-expiration: 604800000  # 7 días
  issuer: fundamentos01-api
  header: Authorization
  prefix: "Bearer "
```

En producción, el secreto debe sobrescribirse mediante la variable de entorno `JWT_SECRET` y nunca dejarse hardcodeado.

---

## 8. Modelo de datos: roles

Se creó la entidad `RoleEntity`, relacionada `ManyToMany` con `UserEntity` a través de una tabla intermedia `user_roles`, generada automáticamente por JPA.

```txt
roles
├── id
├── name        (ROLE_USER | ROLE_ADMIN)
├── description
├── created_at
└── updated_at

user_roles
├── user_id
└── role_id
```

Al iniciar la aplicación, `SecurityDataInitializer` crea automáticamente los roles base (`ROLE_USER`, `ROLE_ADMIN`) si aún no existen en la base de datos.

---

## 9. Endpoints de autenticación

Con el `context-path: /api` configurado en el proyecto, los endpoints quedan expuestos así:

| Método | Endpoint             | Acceso   | Descripción                                  |
|--------|-----------------------|----------|-----------------------------------------------|
| POST   | `/api/auth/register`  | Público  | Crea un usuario nuevo con `ROLE_USER` y devuelve un JWT |
| POST   | `/api/auth/login`     | Público  | Valida credenciales y devuelve un JWT         |

### Ejemplo de registro

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Test User",
  "email": "test@test.com",
  "password": "Password123"
}
```

Respuesta `201 Created`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "Test User",
  "email": "test@test.com",
  "roles": ["ROLE_USER"]
}
```

### Ejemplo de login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@test.com",
  "password": "Password123"
}
```

Respuesta `200 OK`: mismo formato que el registro.

---

## 10. Protección del resto de la API

Todo endpoint fuera de `/auth/**` requiere autenticación. El token debe enviarse en el header `Authorization`:

```http
GET /api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

| Escenario                          | Resultado           |
|-------------------------------------|----------------------|
| Sin token                           | `401 Unauthorized`  |
| Token expirado o modificado         | `401 Unauthorized`  |
| Token válido                        | `200 OK` (continúa al controlador) |

Los errores de autenticación son manejados por `JwtAuthenticationEntryPoint`, devolviendo un JSON consistente con el resto de la API:

```json
{
  "timestamp": "2026-07-08T19:06:17.415137",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token de autenticación inválido o no proporcionado. Debe incluir un token válido en el header Authorization: Bearer <token>",
  "path": "/api/products"
}
```

---

## 11. Flujo general de autenticación

```txt
Cliente                                Servidor
  |  POST /auth/login                     |
  |  { email, password }                  |
  |--------------------------------------->|
  |                    AuthenticationManager valida credenciales (BCrypt)
  |                    JwtUtil genera token firmado (HS256)
  |  { token, userId, roles }             |
  |<---------------------------------------|
  |                                        |
  |  GET /api/products                     |
  |  Authorization: Bearer <token>         |
  |--------------------------------------->|
  |                    JwtAuthenticationFilter valida el token
  |                    Carga el usuario y sus roles
  |                    Establece el SecurityContext
  |  200 OK con los datos                 |
  |<---------------------------------------|
```

---

## 12. Pruebas realizadas

Se creó una colección en **Bruno** con los siguientes requests:

1. **Register** → `POST {{base_url}}/auth/register` → `201 Created`
2. **Login** → `POST {{base_url}}/auth/login` → `200 OK`, guarda el token en una variable de entorno mediante script post-respuesta
3. **Get Products (sin token)** → `GET {{base_url}}/products` → `401 Unauthorized`
4. **Get Products (con token)** → `GET {{base_url}}/products` con `Authorization: Bearer {{token}}` → `200 OK`

![Registro exitoso](assets/11-register-response.png)
![Login exitoso](assets/11-login-response.png)
![Endpoint protegido sin token](assets/11-unauthorized.png)
![Endpoint protegido con token](assets/11-authorized.png)

---