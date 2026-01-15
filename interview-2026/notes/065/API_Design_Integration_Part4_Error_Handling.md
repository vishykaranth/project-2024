# Error Handling: HTTP Status Codes, Error Responses, Error Formats

## Overview

Proper error handling is crucial for API usability. It provides clear feedback to clients about what went wrong and how to fix it. Well-designed error responses improve developer experience and reduce support burden.

## HTTP Status Codes

### Status Code Categories

```
┌─────────────────────────────────────────────────────────┐
│              HTTP Status Code Categories                 │
└─────────────────────────────────────────────────────────┘

1xx - Informational
├─ 100 Continue
├─ 101 Switching Protocols
└─ 102 Processing

2xx - Success
├─ 200 OK
├─ 201 Created
├─ 202 Accepted
├─ 204 No Content
└─ 206 Partial Content

3xx - Redirection
├─ 301 Moved Permanently
├─ 302 Found
├─ 304 Not Modified
└─ 307 Temporary Redirect

4xx - Client Error
├─ 400 Bad Request
├─ 401 Unauthorized
├─ 403 Forbidden
├─ 404 Not Found
├─ 409 Conflict
├─ 422 Unprocessable Entity
└─ 429 Too Many Requests

5xx - Server Error
├─ 500 Internal Server Error
├─ 502 Bad Gateway
├─ 503 Service Unavailable
└─ 504 Gateway Timeout
```

### Common Status Codes for APIs

#### 200 OK
Request succeeded.

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 123,
  "name": "John Doe"
}
```

#### 201 Created
Resource created successfully.

```http
HTTP/1.1 201 Created
Location: /api/v1/users/123
Content-Type: application/json

{
  "id": 123,
  "name": "John Doe",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### 204 No Content
Success with no response body.

```http
HTTP/1.1 204 No Content
```

#### 400 Bad Request
Client error - malformed request.

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Request body is malformed",
    "details": "JSON syntax error at line 3"
  }
}
```

#### 401 Unauthorized
Authentication required or failed.

```http
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer
Content-Type: application/json

{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required"
  }
}
```

#### 403 Forbidden
Authorization failed - insufficient permissions.

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "error": {
    "code": "FORBIDDEN",
    "message": "You don't have permission to access this resource"
  }
}
```

#### 404 Not Found
Resource not found.

```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "error": {
    "code": "NOT_FOUND",
    "message": "User with ID 123 not found"
  }
}
```

#### 409 Conflict
Resource conflict (e.g., duplicate).

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "error": {
    "code": "CONFLICT",
    "message": "User with email already exists"
  }
}
```

#### 422 Unprocessable Entity
Validation error.

```http
HTTP/1.1 422 Unprocessable Entity
Content-Type: application/json

{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "errors": [
      {
        "field": "email",
        "message": "Email is required"
      },
      {
        "field": "password",
        "message": "Password must be at least 8 characters"
      }
    ]
  }
}
```

#### 429 Too Many Requests
Rate limit exceeded.

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 60
Content-Type: application/json

{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Rate limit exceeded. Try again in 60 seconds"
  }
}
```

#### 500 Internal Server Error
Server error.

```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "An internal error occurred"
  }
}
```

## Error Response Formats

### Standard Error Format

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": "Additional details (optional)",
    "timestamp": "2024-01-15T10:30:00Z",
    "path": "/api/v1/users/123",
    "requestId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### Validation Error Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "errors": [
      {
        "field": "email",
        "message": "Email is required",
        "rejectedValue": null
      },
      {
        "field": "password",
        "message": "Password must be at least 8 characters",
        "rejectedValue": "123"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### RFC 7807 Problem Details

```json
{
  "type": "https://api.example.com/problems/validation-error",
  "title": "Validation Error",
  "status": 422,
  "detail": "The request contains invalid data",
  "instance": "/api/v1/users",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

## Error Handling Implementation

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .code("NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(404).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .errors(ex.getErrors())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(422).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An internal error occurred")
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(500).body(error);
    }
}
```

### Error Response Model

```java
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String details;
    private Instant timestamp;
    private String path;
    private String requestId;
    private List<FieldError> errors;
    
    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
```

### Custom Exceptions

```java
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;
    private final String identifier;
    
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s with ID %s not found", resource, identifier));
        this.resource = resource;
        this.identifier = identifier;
    }
}

public class ValidationException extends RuntimeException {
    private final List<FieldError> errors;
    
    public ValidationException(List<FieldError> errors) {
        super("Validation failed");
        this.errors = errors;
    }
}
```

## Error Handling Best Practices

### 1. Use Appropriate Status Codes

```java
// ✅ GOOD
if (user == null) {
    throw new ResourceNotFoundException("User", id);
    // Returns 404
}

// ❌ BAD
if (user == null) {
    throw new RuntimeException("User not found");
    // Returns 500 (wrong!)
}
```

### 2. Provide Clear Error Messages

```json
// ✅ GOOD
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Email is required",
    "field": "email"
  }
}

// ❌ BAD
{
  "error": "Error"
}
```

### 3. Include Request ID for Debugging

```java
String requestId = UUID.randomUUID().toString();
MDC.put("requestId", requestId);

ErrorResponse error = ErrorResponse.builder()
    .requestId(requestId)
    .message("Error occurred")
    .build();
```

### 4. Don't Expose Internal Details

```java
// ❌ BAD: Exposes internal details
{
  "error": {
    "message": "NullPointerException at UserService.java:123"
  }
}

// ✅ GOOD: Generic message for production
{
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "An internal error occurred"
  }
}
```

### 5. Use Consistent Error Format

```java
// All errors follow same structure
{
  "error": {
    "code": "...",
    "message": "...",
    "timestamp": "..."
  }
}
```

## Error Code Design

### Error Code Structure

```
┌─────────────────────────────────────────────────────────┐
│              Error Code Naming Convention               │
└─────────────────────────────────────────────────────────┘

Format: CATEGORY_SUB_CATEGORY

Examples:
├─ VALIDATION_EMAIL_REQUIRED
├─ VALIDATION_PASSWORD_TOO_SHORT
├─ AUTHENTICATION_INVALID_TOKEN
├─ AUTHENTICATION_TOKEN_EXPIRED
├─ AUTHORIZATION_INSUFFICIENT_PERMISSIONS
├─ RESOURCE_USER_NOT_FOUND
├─ RESOURCE_USER_ALREADY_EXISTS
└─ RATE_LIMIT_EXCEEDED
```

### Error Code Categories

```java
public enum ErrorCategory {
    VALIDATION,      // 422 - Input validation errors
    AUTHENTICATION,  // 401 - Auth failures
    AUTHORIZATION,   // 403 - Permission issues
    NOT_FOUND,       // 404 - Resource not found
    CONFLICT,        // 409 - Resource conflicts
    RATE_LIMIT,      // 429 - Rate limiting
    INTERNAL_ERROR   // 500 - Server errors
}
```

## Error Handling Patterns

### Pattern 1: Result Type

```java
public class Result<T> {
    private final T data;
    private final Error error;
    private final boolean success;
    
    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }
    
    public static <T> Result<T> error(Error error) {
        return new Result<>(null, error, false);
    }
}
```

### Pattern 2: Exception Mapping

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex) {
    List<FieldError> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> FieldError.builder()
            .field(error.getField())
            .message(error.getDefaultMessage())
            .rejectedValue(error.getRejectedValue())
            .build())
        .collect(Collectors.toList());
    
    return ResponseEntity.status(422)
        .body(ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Validation failed")
            .errors(errors)
            .build());
}
```

## Summary

Error Handling:
- **HTTP Status Codes**: Use appropriate codes (200, 201, 400, 401, 404, 422, 500)
- **Error Formats**: Consistent structure with code, message, details
- **Validation Errors**: Field-level errors with clear messages
- **Error Codes**: Categorized, descriptive codes

**Best Practices:**
- Use appropriate status codes
- Provide clear error messages
- Include request ID for debugging
- Don't expose internal details
- Use consistent error format

**Remember**: Good error handling improves developer experience and reduces support burden!
