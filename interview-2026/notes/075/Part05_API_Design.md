# Part 5: API Design - Quick Revision

## REST Principles

- **Resource-Based URLs**: `/api/users/123` not `/api/getUser?id=123`
- **HTTP Methods**: GET (read), POST (create), PUT (update), DELETE (remove), PATCH (partial update)
- **Stateless**: Each request contains all information needed
- **Status Codes**: 200 (OK), 201 (Created), 400 (Bad Request), 401 (Unauthorized), 404 (Not Found), 500 (Server Error)

## API Styles

- **REST**: Resource-based, stateless, HTTP methods, JSON/XML
- **GraphQL**: Single endpoint, flexible queries, client specifies fields, solves over/under-fetching
- **gRPC**: Protocol buffers, HTTP/2, strong typing, efficient serialization, microservices

## API Best Practices

- **Idempotency**: Same request = same result; use idempotency keys
- **Versioning**: URL (`/api/v1/users`) or Header (`Accept: application/vnd.api+json;version=1`)
- **Rate Limiting**: Prevent abuse, return 429 Too Many Requests
- **Pagination**: Limit results, use cursor-based or offset-based
- **Error Handling**: Consistent error format, meaningful messages, proper status codes

## API Security

- **Authentication**: OAuth 2.0, JWT tokens, API keys
- **Authorization**: Role-based access control (RBAC), permissions
- **Input Validation**: Validate all inputs, prevent injection attacks
- **HTTPS**: Encrypt data in transit

## API Documentation

- **OpenAPI/Swagger**: Standard API documentation format
- **Interactive Testing**: Test APIs directly from documentation
- **Examples**: Provide request/response examples
