# Todo List Backend Technical Specification

## 1. System Infrastructure

- **Base URL (local dev):** `http://localhost:8080`
- **Protocol:** HTTP by default for local development; HTTPS when `SSL_ENABLED=true`
- **Port:** `8080` (default local) / `8443` (when SSL enabled)
- **Spring Boot:** `3.3.3`
- **Persistence:** MongoDB via Spring Data MongoDB
- **Authentication:** JWT bearer token
- **Configuration:** externalized via environment variables

> Production configuration is externalized. The application reads MongoDB settings from `MONGODB_URI` and SSL settings from `SSL_*` environment variables.

### 1.1 Externalized Configuration

- `MONGODB_URI` — MongoDB connection string
- `SSL_ENABLED` — enable or disable TLS (`true` / `false`)
- `SSL_KEY_STORE` — SSL keystore location
- `SSL_KEY_STORE_PASSWORD` — keystore password
- `SSL_KEY_STORE_TYPE` — keystore type, e.g. `PKCS12`
- `SSL_KEY_ALIAS` — SSL key alias

> The backend does not hardcode a MongoDB URI in production configuration. It requires `MONGODB_URI` to be set externally.

### 1.2 Security Model

- **JWT bearer token authentication** using Spring Security stateless filter chain
- **Login:** `POST /api/auth/login` returns a JWT token
- **Logout:** client-side token discard; no server session invalidation required
- **Token header:** `Authorization: Bearer <token>`
- **CSRF protection:** disabled for stateless token-based API requests
- **Form login and HTTP Basic:** disabled
- **Request channel security:** HTTPS required for all endpoints when SSL is enabled
- **Rate limiting:** failed login attempts are throttled and return `429 Too Many Requests`

### 1.3 Tenant Isolation

- Users belong to a tenant identified by `tenantId`
- `tenantId` is required for registration and login
- After login, tenant context is determined from the authenticated user
- Todo operations are tenant-scoped and do not accept `tenantId` in request bodies

### 1.4 Required Headers

For authenticated API calls:

- `Content-Type: application/json`
- `Accept: application/json`
- `Cookie: JSESSIONID={sessionId}`
- `Cookie: XSRF-TOKEN={token}` for CSRF-protected requests
- `X-XSRF-TOKEN: {token}` for state-changing requests (`POST`, `PUT`, `DELETE`)
- `X-Frontend-Client: Angular|React` for frontend-specific route selection

---

## 2. Validation Rules and Error Handling

### 2.1 Validation annotations

The backend uses `@Valid` on request bodies and the following validation constraints:

#### AuthRequest
- `username` — `@NotBlank`, `@Size(min = 3, max = 100)`
- `password` — `@NotBlank`, `@Size(min = 8, max = 128)`, `@Pattern(...)`
  - password policy: must contain at least one uppercase letter, one lowercase letter, one digit, and one special character
- `tenantId` — `@NotBlank`, `@Size(min = 3, max = 100)`

#### TodoRequest
- `topic` — `@NotBlank`, `@Size(max = 200)`
- `summaryPoints` — `@NotBlank`, `@Size(max = 1000)`
- `status` — `@NotNull` and must be one of `PENDING`, `IN_PROGRESS`, `COMPLETED`
- `priority` — `@NotNull` and must be one of `LOW`, `MEDIUM`, `HIGH`
- `section` — `@NotBlank`, `@Size(max = 100)`

### 2.2 Validation error response

Validation failures return `400 Bad Request` with a field-level error payload.

#### Validation error example

```json
{
  "status_code": 400,
  "message": "Validation failed",
  "data": {
    "errors": [
      {
        "field": "password",
        "message": "Password must contain uppercase, lowercase, digit, and special character"
      },
      {
        "field": "topic",
        "message": "must not be blank"
      }
    ]
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

### 2.3 Error conventions

- `400 Bad Request` — validation failure, malformed JSON, or invalid enum values
- `401 Unauthorized` — missing or invalid session, invalid login
- `403 Forbidden` — access denied or CSRF validation failed
- `404 Not Found` — missing resource
- `429 Too Many Requests` — login throttling triggered
- `500 Internal Server Error` — unexpected server error

---

## 3. Response Envelope

All endpoints use the shared JSON wrapper `ApiResponse<T>` with these attributes:

- `status_code`: HTTP-style status integer
- `message`: human-readable result
- `data`: payload object or array, omitted when `null`
- `timestamp`: ISO-8601 UTC timestamp

### 3.1 Example success response

```json
{
  "status_code": 200,
  "message": "Todo created",
  "data": {
    "id": "643af3b1d8c5e76a2f3f9d9a",
    "tenantId": "tenant-123",
    "topic": "Write spec",
    "summaryPoints": "Define backend contract for UI",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "section": "Planning",
    "createdAt": "2026-05-10T12:34:56.789Z",
    "updatedAt": "2026-05-10T12:34:56.789Z"
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

### 3.2 Example page response

```json
{
  "status_code": 200,
  "message": "Todos retrieved",
  "data": {
    "content": [
      {
        "id": "643af3b1d8c5e76a2f3f9d9a",
        "tenantId": "tenant-abc",
        "topic": "Write spec",
        "summaryPoints": "Define backend contract for UI",
        "status": "IN_PROGRESS",
        "priority": "HIGH",
        "section": "Planning",
        "createdAt": "2026-05-10T12:34:56.789Z",
        "updatedAt": "2026-05-10T12:34:56.789Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

### 3.3 Example array response

For backward compatibility, clients can request a plain array payload using `?array=true`.

```json
{
  "status_code": 200,
  "message": "Todos retrieved",
  "data": [
    {
      "id": "643af3b1d8c5e76a2f3f9d9a",
      "tenantId": "tenant-abc",
      "topic": "Write spec",
      "summaryPoints": "Define backend contract for UI",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "section": "Planning",
      "createdAt": "2026-05-10T12:34:56.789Z",
      "updatedAt": "2026-05-10T12:34:56.789Z"
    }
  ],
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

---

## 4. API Endpoint Catalog

### 4.1 Authentication Endpoints

#### Register

- **Method:** `POST`
- **URL:** `/api/auth/register`
- **Headers:**
  - `Content-Type: application/json`
  - `Accept: application/json`
- **Request body:**

```json
{
  "username": "alice",
  "password": "Secret123!",
  "tenantId": "tenant-abc"
}
```

- **Success response:** `200 OK`

```json
{
  "status_code": 200,
  "message": "Registration successful",
  "data": {
    "username": "alice",
    "tenantId": "tenant-abc"
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

- **Note:** registration does not create a session cookie.

#### CSRF token retrieval

- **Method:** `GET`
- **URL:** `/api/auth/csrf`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}` (optional)

- **Success response:** `200 OK`

```json
{
  "status_code": 200,
  "message": "CSRF token generated",
  "data": {
    "csrfToken": "abcdef123456"
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

- **Note:** the backend also returns `Set-Cookie: XSRF-TOKEN=...`.

#### Login

- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Headers:**
  - `Content-Type: application/json`
  - `Accept: application/json`
- **Request body:**

```json
{
  "username": "alice",
  "password": "Secret123!",
  "tenantId": "tenant-abc"
}
```

- **Success response:** `200 OK`

```json
{
  "status_code": 200,
  "message": "Login successful",
  "data": {
    "username": "alice",
    "tenantId": "tenant-abc"
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

- **Important:** response includes `Set-Cookie: JSESSIONID=...` and `Set-Cookie: XSRF-TOKEN=...`.
- **Failed login:** returns `401 Unauthorized`.
- **Throttled login:** returns `429 Too Many Requests` after repeated failed attempts.

#### Logout

- **Method:** `POST`
- **URL:** `/api/auth/logout`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`
- **Request body:** none
- **Success response:** `200 OK`

```json
{
  "status_code": 200,
  "message": "Logout successful",
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

---

## 5. CSRF Frontend Flow

### 5.1 Flow summary

1. Call `POST /api/auth/login` with credentials.
2. Store cookies: `JSESSIONID` and `XSRF-TOKEN`.
3. Request `GET /api/auth/csrf` to retrieve `csrfToken`.
4. Send state-changing requests with `Cookie: JSESSIONID`, `Cookie: XSRF-TOKEN`, and `X-XSRF-TOKEN.`

### 5.2 Curl examples

Login and store cookies:

```bash
curl -i -c cookies.txt -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Secret123!","tenantId":"tenant-abc"}' \
  https://localhost:8443/api/auth/login
```

Fetch CSRF token:

```bash
curl -i -b cookies.txt \
  https://localhost:8443/api/auth/csrf
```

Create a Todo with CSRF token:

```bash
curl -i -b cookies.txt \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: abcdef123456" \
  -d '{"topic":"Write spec","summaryPoints":"Define backend contract","status":"PENDING","priority":"MEDIUM","section":"Planning"}' \
  https://localhost:8443/api/todo
```

---

## 6. Data Models & Type Definitions

### 6.1 User model

```json
{
  "id": "string",
  "username": "string",
  "password": "string",
  "tenantId": "string"
}
```

#### User field types

| Field | Type | Description |
| --- | --- | --- |
| `id` | string | MongoDB document id |
| `username` | string | unique login name |
| `password` | string | bcrypt hashed password in storage |
| `tenantId` | string | tenant identifier |

### 6.2 Todo model

```json
{
  "id": "string",
  "tenantId": "string",
  "topic": "string",
  "summaryPoints": "string",
  "status": "PENDING|IN_PROGRESS|COMPLETED",
  "priority": "LOW|MEDIUM|HIGH",
  "section": "string",
  "createdAt": "ISO-8601 string",
  "updatedAt": "ISO-8601 string"
}
```

#### Todo field types

| Field | Type | Description |
| --- | --- | --- |
| `id` | string | Todo identifier |
| `tenantId` | string | tenant context |
| `topic` | string | title |
| `summaryPoints` | string | detail text |
| `status` | string enum | workflow state |
| `priority` | string enum | urgency level |
| `section` | string | grouping label |
| `createdAt` | string | ISO-8601 creation timestamp |
| `updatedAt` | string | ISO-8601 last update timestamp |

### 6.3 Enum values

#### Status values

- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`

#### Priority values

- `LOW`
- `MEDIUM`
- `HIGH`

---

## 7. API Endpoint Catalog

### 7.1 Todo CRUD Endpoints

#### Get all todos

- **Method:** `GET`
- **URL:** `/api/todo`
- **Query parameters:**
  - `page` (optional, default `0`)
  - `size` (optional, default `20`)
  - `sort` (optional, e.g. `createdAt,desc`)
  - `array` (optional, `true` for plain list output)
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`

##### Paginated response example

```json
{
  "status_code": 200,
  "message": "Todos retrieved",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

##### Array response example

```json
{
  "status_code": 200,
  "message": "Todos retrieved",
  "data": [ ... ],
  "timestamp": "2026-05-10T12:34:56.789Z"
}
```

#### Get todo by id

- **Method:** `GET`
- **URL:** `/api/todo/{id}`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`

#### Create todo

- **Method:** `POST`
- **URL:** `/api/todo`
- **Headers:**
  - `Content-Type: application/json`
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`
  - `X-XSRF-TOKEN: {token}`
- **Request body:**

```json
{
  "topic": "Write spec",
  "summaryPoints": "Define backend contract for UI",
  "status": "PENDING",
  "priority": "MEDIUM",
  "section": "Planning"
}
```

#### Update todo

- **Method:** `PUT`
- **URL:** `/api/todo/{id}`
- **Headers:**
  - `Content-Type: application/json`
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`
  - `X-XSRF-TOKEN: {token}`
- **Request body:** same shape as create

#### Delete todo

- **Method:** `DELETE`
- **URL:** `/api/todo/{id}`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`
  - `X-XSRF-TOKEN: {token}`

---

## 8. Section and Frontend-Specific Endpoints

#### Get sections

- **Method:** `GET`
- **URL:** `/api/todo/sections`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`

#### Get todos by section

- **Method:** `GET`
- **URL:** `/api/todo/section/{section}`
- **Query parameters:** `page`, `size`, `sort`, `array`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`

#### Angular-specific endpoint

- **Method:** `GET`
- **URL:** `/api/angular/todo`
- **Query parameters:** `page`, `size`, `sort`, `array`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`

#### React-specific endpoint

- **Method:** `GET`
- **URL:** `/api/react/todo`
- **Query parameters:** `page`, `size`, `sort`, `array`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`

#### Generic frontend-aware endpoint

- **Method:** `GET`
- **URL:** `/api/todo`
- **Headers:**
  - `Accept: application/json`
  - `Cookie: JSESSIONID={sessionId}`
  - `X-Frontend-Client: Angular|React`

---

## 9. Password Policy

Passwords must satisfy these rules:

- Minimum length: `8` characters
- Maximum length: `128` characters
- At least one uppercase character
- At least one lowercase character
- At least one digit
- At least one special character
- Stored hashed with bcrypt

---

## 10. Important Integration Notes

- `tenantId` is required only for registration and login
- `Todo` create/update payloads must not include `tenantId`
- `status` and `priority` must be exact enum values
- `?sort=createdAt,desc` is supported on pageable endpoints
- `?array=true` returns backward-compatible plain arrays
- MongoDB connection is externalized via `MONGODB_URI`
- CSRF requires both cookie and `X-XSRF-TOKEN` header for state-changing requests

---

## 11. Example Requests

### Register

```bash
curl -X POST https://localhost:8443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Secret123!","tenantId":"tenant-abc"}'
```

### Login

```bash
curl -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"Secret123!","tenantId":"tenant-abc"}'
```

### Paginated todos sorted by createdAt desc

```bash
curl https://localhost:8443/api/todo?page=0&size=20&sort=createdAt,desc \
  -H "Accept: application/json" \
  -b cookies.txt
```

### Backward-compatible todo array

```bash
curl https://localhost:8443/api/todo?array=true \
  -H "Accept: application/json" \
  -b cookies.txt
```
