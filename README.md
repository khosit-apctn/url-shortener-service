# URL Shortener API

This repository contains the implementation for the Software Engineer Take-Home Assignment: **URL Shortener API**.

The service provides core URL shortening features similar to Bitly or TinyURL, including shortening long links, redirecting with HTTP 302, user authentication (JWT), and basic link management.

---

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.1.1 (Spring Security, Spring Data JPA, Bean Validation)
- **Database**: PostgreSQL 16
- **Migration**: Flyway
- **Authentication**: JWT (JJWT) + BCrypt password hashing
- **Testing**: JUnit 5, Mockito, AssertJ

---

## Features

- **Shorten URL**: Accepts long URLs, validates `http://` / `https://` format, and generates a 6-character Base62 code. Supports both anonymous and authenticated users.
- **Redirect**: Redirects clients from `GET /r/{shortCode}` to the original URL using HTTP 302. Returns 404 if not found or inactive.
- **Authentication**: Simple registration and login using email & password. Passwords are encrypted using BCrypt, and authenticated requests use JWT Bearer tokens.
- **URL Management**: Authenticated users can list all URLs they have created (`GET /api/urls`) and deactivate their own URLs (`DELETE /api/urls/{id}`).
- **Error Handling**: Centralized exception handler with consistent error responses.
- **camelCase JSON**: All response payloads use camelCase naming.

---

## Database Migrations (Flyway)

Database tables are managed automatically by Flyway upon application startup:

- `V1__createTableUsers.sql`: Creates `users` table.
- `V2__createTableShortUrls.sql`: Creates `short_urls` table with foreign key to `users`.

---

## Getting Started

### 1. Prerequisites
- Java 21
- Docker (for PostgreSQL)

### 2. Start PostgreSQL

Run PostgreSQL in Docker with this command:

```bash
docker run --name postgres-shortener -p 5432:5432 -e POSTGRES_DB=shortener_db -e POSTGRES_PASSWORD=postgres -d postgres:16-alpine
```

### 3. Run the Application

```bash
# Windows (PowerShell)
.\gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```

The application will start on `http://localhost:8080`. Flyway will run migrations automatically.

### 4. Run Tests

```bash
# Windows (PowerShell)
.\gradlew.bat test

# Linux / macOS
./gradlew test
```

---

## API Examples (cURL)

Base URL: `http://localhost:8080`

### 1. Register
`POST /api/register`

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "email": "user@example.com"
}
```

---

### 2. Login
`POST /api/login`

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "<JWT_TOKEN>",
  "tokenType": "Bearer",
  "email": "user@example.com"
}
```

---

### 3. Shorten URL (Anonymous)
`POST /api/shorten`

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "original_url": "https://example.com/some/very/long/link"
  }'
```

Response:
```json
{
  "shortUrl": "http://localhost:8080/r/abc123"
}
```

---

### 4. Shorten URL (Authenticated)
`POST /api/shorten`

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "original_url": "https://example.com/some/very/long/link"
  }'
```

Response:
```json
{
  "shortUrl": "http://localhost:8080/r/xyz789"
}
```

---

### 5. Redirect
`GET /r/{shortCode}`

```bash
curl -I http://localhost:8080/r/abc123
```

Response:
```http
HTTP/1.1 302 Found
Location: https://example.com/some/very/long/link
```

---

### 6. List My URLs
`GET /api/urls`

```bash
curl -X GET http://localhost:8080/api/urls \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

Response:
```json
[
  {
    "id": 1,
    "shortCode": "xyz789",
    "shortUrl": "http://localhost:8080/r/xyz789",
    "originalUrl": "https://example.com/some/very/long/link",
    "isActive": true
  }
]
```

---

### 7. Deactivate a URL
`DELETE /api/urls/{id}`

```bash
curl -X DELETE http://localhost:8080/api/urls/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

Response:
```http
HTTP/1.1 204 No Content
```

---

### Error Response Format

In case of error, the API returns a standard JSON structure:

```json
{
  "timestamp": "2026-09-06T14:00:00.000+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "Invalid URL format. Must be a valid HTTP or HTTPS URL"
  ]
}
```