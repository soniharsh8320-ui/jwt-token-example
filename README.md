# JWT Token Example (Spring Boot 3, Java 21)

This project is a Spring Boot 3 JWT authentication example with:
- user signup/signin APIs
- JWT generation and validation
- stateless Spring Security filter chain
- MySQL database with JPA and HikariCP pooling
- Swagger/OpenAPI with Bearer JWT authorization support
- Global exception handling with standardized error response
- JUnit + Mockito test coverage for JWT and service layer behavior
- Dockerfile for containerized deployment

## Tech Stack

- Java 21 (project target)
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- MySQL
- HikariCP
- JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- Lombok
- Springdoc OpenAPI UI dependency
- JUnit 5 + Mockito (Spring Boot Starter Test)

## Implemented Features

1. JWT Authentication
- Generate token on successful signin (`/api/v1/auth/signin`)
- Generate refreshed token pair via refresh endpoint (`/api/v1/auth/refresh`)
- Validate token in `AuthTokenFilter`
- Extract username from token in `JwtUtils`
- Stateless session management (`SessionCreationPolicy.STATELESS`)

2. User Registration and Login
- Signup endpoint stores user with encoded password (BCrypt)
- Signin endpoint authenticates via `AuthenticationManager` and returns JWT

3. Database Layer
- `User` entity mapped to `users` table
- `UserRepository` with:
  - `findByUsername(String username)`
  - `existsByUsername(String username)`

4. Security Configuration
- `@EnableWebSecurity`
- `SecurityFilterChain` with JWT filter before `UsernamePasswordAuthenticationFilter`
- Custom unauthorized handler (`AuthEntryPointJwt`)
- `DaoAuthenticationProvider` + `CustomUserDetailsService`
- Swagger/OpenAPI endpoints allowed in security config (`/swagger-ui/**`, `/v3/api-docs/**`)

5. Database Configuration
- MySQL datasource configured in `application.properties`
- Custom datasource bean in `spring.security.jwt.config.MySqlDataSourceConfig`
- Hikari pool settings configured via `spring.datasource.hikari.*`

6. API Documentation Configuration
- OpenAPI bearer scheme in `spring.security.jwt.config.OpenApiConfig`
- Class-level Swagger tags:
  - `Authentication APIs`
  - `Main APIs`
- Operation-level annotations on all controller endpoints

7. Error Handling
- Global exception advice in `spring.security.jwt.adviser.GlobalExceptionHandler`
- Standardized error payload:
  - `timestamp`, `status`, `error`, `message`, `path`
- Common API status codes:
  - `400` for invalid refresh request payload
  - `401` for invalid refresh/access token
  - `409` when signup username already exists

8. Service Layer Logging
- Added logs in `CustomUserDetailsService` for user lookup flow:
  - debug: load start and success
  - warn: user not found

9. Test Coverage
- `JwtUtilsTest` includes:
  - valid token verification (positive)
  - tampered token verification (negative)
  - malformed token verification (negative)
- `CustomUserDetailsServiceTest` (Mockito) includes:
  - user found path returns `UserDetails`
  - user not found path throws `UsernameNotFoundException`

## API Endpoints

Base path: `/api/v1`

### Public
- `GET /api/v1/welcome`
- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/signin`
- `POST /api/v1/auth/refresh`

### Protected (JWT required)
- `GET /api/v1/user`
- `GET /api/v1/special`

## Swagger Usage

1. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`
2. Generate token using `POST /api/v1/auth/signin`
3. Click **Authorize** and paste only `accessToken` value in `bearerAuth` (do not prefix `Bearer `)
4. Call protected endpoints (`/api/v1/user`, `/api/v1/special`)

## Request/Response Examples

### Signup
`POST /api/v1/auth/signup`

```json
{
  "username": "john",
  "password": "john123"
}
```

Response:
```text
User registered successfully!
```

### Signin
`POST /api/v1/auth/signin`

```json
{
  "username": "john",
  "password": "john123"
}
```

Response:
```json
{
  "accessToken": "<jwt-access-token>",
  "refreshToken": "<jwt-refresh-token>",
  "tokenType": "Bearer",
  "accessExpiresInSeconds": 900,
  "refreshExpiresInSeconds": 604800
}
```

### Refresh Token
`POST /api/v1/auth/refresh`

```json
{
  "refreshToken": "<jwt-refresh-token>"
}
```

Response:
```json
{
  "accessToken": "<jwt-access-token>",
  "refreshToken": "<jwt-refresh-token>",
  "tokenType": "Bearer",
  "accessExpiresInSeconds": 900,
  "refreshExpiresInSeconds": 604800
}
```

### Use Token

```http
Authorization: Bearer <jwt-token>
```

## Configuration

Defined in [application.properties](src/main/resources/application.properties):

- `jwt.secret`
- `jwt.access-expiration-seconds`
- `jwt.refresh-expiration-seconds`
- `jwt.header`
- `jwt.prefix`
- `jwt.issuer`
- `jwt.audience`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.driver-class-name`
- `spring.jpa.database-platform`
- `spring.datasource.hikari.idle-timeout`
- `spring.datasource.hikari.max-lifetime`
- `spring.datasource.hikari.maximum-pool-size`
- `spring.datasource.hikari.minimum-idle`

Default datasource values in the repo:
- `maximum-pool-size=5`
- `minimum-idle=1`

## How to Run

1. Build
```bash
mvn clean install
```

2. Ensure MySQL is running and database credentials are set (or use defaults from `application.properties`):
```bash
export DB_URL='jdbc:mysql://localhost:3306/jwtdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='harsh'
export DB_PASSWORD='harsh123'
export JWT_SECRET='your-very-strong-32+-char-secret'
```

3. Start app
```bash
mvn spring-boot:run
```

4. Test
```bash
mvn test
```

## Docker

1. Build JAR:
```bash
mvn -DskipTests package
```

2. Build image:
```bash
docker build -t jwt-token-example:latest .
```

3. Run container:
```bash
docker run --name jwt-token-example -p 8080:8080 \
  -e JWT_SECRET='your-very-strong-32+-char-secret' \
  -e DB_URL='jdbc:mysql://host.docker.internal:3306/jwtdb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
  -e DB_USERNAME='harsh' \
  -e DB_PASSWORD='harsh123' \
  jwt-token-example:latest
```

## Project Structure

```text
src/main/java/spring/security/jwt
├── adviser
│   ├── ApiErrorResponse.java
│   └── GlobalExceptionHandler.java
├── config
│   ├── MySqlDataSourceConfig.java
│   └── OpenApiConfig.java
├── controller
│   ├── AuthenticationController.java
│   └── MainController.java
├── dto
│   ├── AuthResponse.java
│   └── RefreshTokenRequest.java
├── entity
│   └── User.java
├── repository
│   └── UserRepository.java
├── security
│   ├── AuthEntryPointJwt.java
│   ├── AuthTokenFilter.java
│   ├── JwtUtils.java
│   └── WebSecurityConfig.java
└── services
    └── CustomUserDetailsService.java
```

## Important Fixes Applied

- Migrated datasource from H2 to MySQL runtime connector
- Added explicit MySQL datasource config class (`MySqlDataSourceConfig`)
- Added Hikari pool tuning properties in `application.properties`
- Added Swagger bearer auth configuration and endpoint tags/operations
- Added global exception handler with standard API error payload
- Moved `dto` and `adviser` packages to top-level under `spring.security.jwt`
- Fixed JWT header parsing NPE in `AuthTokenFilter.parseJwt()`
- Fixed JWT expiration handling to use numeric seconds (`long`) and convert to milliseconds
- Fixed `AuthenticationConfiguration` wiring in Spring Security config
- Added Lombok compiler annotation processing config in Maven
- Added Mockito-based unit tests for `CustomUserDetailsService`
- Added Dockerfile and `.dockerignore` for containerized execution

## Notes

- Keep `jwt.secret` strong and do not commit production secrets.
- Set JWT secret using environment variable before running:

```bash
export JWT_SECRET='your-very-strong-32+-char-secret'
```
- Ensure Docker daemon is running before `docker build`.
