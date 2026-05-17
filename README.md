# Todo List Backend

This is the Spring Boot backend for the multi-platform Todo List system.
It is built with Spring Boot 3.x, Spring Data MongoDB, and session-based authentication.

## Features

- MongoDB persistence for Todo and User entities
- Tenant-based data isolation using `tenantId`
- Session-based authentication with login, registration, and logout
- Standardized JSON responses with `status_code`, `message`, `data`, and `timestamp`
- CRUD operations for todos
- Section/group filtering for todos
- Separate frontend-aware endpoint support for Angular, React, and generic clients
- HTTPS-ready configuration via `application.yml`

## Project Structure

- `pom.xml` - Maven dependencies and build configuration
- `src/main/java/com/example/todolist`
  - `config` - Security and application configuration
  - `controller` - Auth and Todo REST controllers
  - `dto` - Request/response DTOs and standardized API response wrapper
  - `model` - MongoDB documents and enums
  - `repository` - Spring Data MongoDB repositories
  - `service` - Business logic and tenant-aware filtering
  - `security` - Security entry point and authentication handling
- `src/main/resources/application.yml` - Application configuration

## Running Locally

1. Install Java 17 and Maven.
2. Start MongoDB locally or update `spring.data.mongodb.uri` in `src/main/resources/application.yml`.
3. Run:

```bash
mvn spring-boot:run
```

4. The application is configured to run on HTTPS port `8443` by default.
5. On first startup, the application will automatically create sample users and todos for testing.

## Logging

The application includes comprehensive request/response logging to help with debugging and monitoring:

### Request/Response Logging
- **RequestLoggingInterceptor**: Logs all incoming requests and outgoing responses
- Captures: HTTP method, URI, headers (excluding sensitive data), request body, response status, response body
- Masks sensitive data like passwords, tokens, and authorization headers
- Provides reasons for error responses (400 Bad Request, 401 Unauthorized, etc.)

### Business Logic Logging
- **AuthController**: Logs authentication attempts, successes, failures, and reasons
- **TodoController**: Logs CRUD operations with tenant context and operation details
- **DataInitializer**: Logs sample data creation

### Log Levels
- `INFO`: General operations, authentication events, CRUD operations
- `DEBUG`: Detailed operation data, request/response bodies
- `WARN`: Authentication failures, client errors (4xx)
- `ERROR`: Server errors (5xx), exceptions

### Log Configuration
Logging levels can be adjusted in `application.yml`:
```yaml
logging:
  level:
    com.example.todolist: INFO
    com.example.todolist.config.RequestLoggingInterceptor: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```

### Sample Log Output
```
2026-05-10 22:44:05 REQUEST: POST /api/auth/login | Remote: 127.0.0.1 | Body: {"username":"admin","password":"***"}
2026-05-10 22:44:05 RESPONSE: POST /api/auth/login | Status: 200 | Body: {"statusCode":200,"message":"Login successful"}
2026-05-10 22:44:05 Login successful for username: admin with tenant: default-tenant
```

## Sample Data

The application includes a `DataInitializer` that creates sample data on first startup:

### Sample Users
- **admin** / admin123 (admin@example.com)
- **john_doe** / password123 (john.doe@example.com)  
- **jane_smith** / password123 (jane.smith@example.com)

### Sample Todos
- Complete Project Documentation (Work, High Priority, In Progress)
- Review Code Changes (Work, Medium Priority, Pending)
- Grocery Shopping (Personal, Low Priority, Pending)
- Database Optimization (Work, High Priority, Completed)
- Exercise Routine (Personal, Medium Priority, In Progress)

All sample data uses the tenant ID `default-tenant`.

## API Endpoints

### Authentication
- `POST /api/auth/register` - register a new user
- `POST /api/auth/login` - login and create a session
- `POST /api/auth/logout` - invalidate the session

### Todos
- `GET /api/todo` - get all todos for the current tenant
- `GET /api/todo/{id}` - get a todo by id
- `POST /api/todo` - create a new todo
- `PUT /api/todo/{id}` - update an existing todo
- `DELETE /api/todo/{id}` - delete a todo
- `GET /api/todo/sections` - get all sections for the tenant
- `GET /api/todo/section/{section}` - get todos by section

### Frontend-specific sample endpoints
- `GET /api/angular/todo` - Angular-specific route
- `GET /api/react/todo` - React-specific route
- `GET /api/todo` with header `X-Frontend-Client` - frontend-aware response

## Notes

- Ensure a valid SSL keystore is provided for HTTPS in production.
- The backend currently uses MongoDB for persistence and can be extended to support additional frontend-specific payload mappings.
