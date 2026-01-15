# API Documentation: OpenAPI, Swagger, Postman

## Overview

API Documentation is essential for developers to understand and use APIs effectively. This guide covers three key tools: OpenAPI (specification), Swagger (tooling), and Postman (API testing and documentation).

## API Documentation Ecosystem

```
┌─────────────────────────────────────────────────────────┐
│         API Documentation Tools                        │
└─────────────────────────────────────────────────────────┘

OpenAPI Specification
    │
    ├─► Swagger UI (Visualization)
    ├─► Swagger Editor (Editing)
    ├─► Swagger Codegen (Code Generation)
    └─► Postman (Testing & Documentation)
```

## 1. OpenAPI Specification

### What is OpenAPI?

OpenAPI (formerly Swagger) is a specification for describing RESTful APIs. It provides a standard format for API documentation.

### OpenAPI Structure

```yaml
openapi: 3.0.0
info:
  title: User Management API
  version: 1.0.0
  description: API for managing users

servers:
  - url: https://api.example.com/v1
    description: Production server

paths:
  /users:
    get:
      summary: Get all users
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
  
  /users/{id}:
    get:
      summary: Get user by ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: User found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
        email:
          type: string
      required:
        - id
        - name
        - email
```

### OpenAPI Components

#### 1. Info Section
```yaml
info:
  title: API Title
  version: 1.0.0
  description: API description
  contact:
    name: API Support
    email: support@example.com
  license:
    name: MIT
```

#### 2. Servers
```yaml
servers:
  - url: https://api.example.com/v1
    description: Production
  - url: https://staging-api.example.com/v1
    description: Staging
```

#### 3. Paths
```yaml
paths:
  /users:
    get:
      summary: Get users
      operationId: getUsers
      tags:
        - Users
      responses:
        '200':
          description: Success
```

#### 4. Components
```yaml
components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
  parameters:
    userId:
      name: id
      in: path
      required: true
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
```

## 2. Swagger

### What is Swagger?

Swagger is a set of tools built around the OpenAPI specification for API development, documentation, and testing.

### Swagger Tools

#### 1. Swagger UI

**Purpose**: Interactive API documentation

**Features:**
- Visual API documentation
- Try-it-out functionality
- Request/response examples
- Schema visualization

**Setup:**
```java
// Spring Boot
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User API")
                .version("1.0.0"));
    }
}
```

**Access:**
- URL: `http://localhost:8080/swagger-ui.html`
- Interactive documentation
- Test endpoints directly

#### 2. Swagger Editor

**Purpose**: Edit OpenAPI specifications

**Features:**
- Live preview
- Validation
- Auto-completion
- Export to various formats

**Usage:**
- Online: editor.swagger.io
- Local: Docker container
- VS Code extension

#### 3. Swagger Codegen

**Purpose**: Generate client/server code from OpenAPI spec

**Supported Languages:**
- Java, Python, JavaScript
- Go, C#, Ruby
- And many more

**Usage:**
```bash
# Generate Java client
swagger-codegen generate \
  -i api.yaml \
  -l java \
  -o ./generated-client

# Generate Spring Boot server
swagger-codegen generate \
  -i api.yaml \
  -l spring \
  -o ./generated-server
```

### Swagger Integration (Spring Boot)

```java
// pom.xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>

// Configuration
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Management API")
                .version("1.0.0")
                .description("API for managing users"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://api.example.com").description("Production")
            ));
    }
}
```

### Swagger Annotations

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management API")
public class UserController {
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve list of all users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public User getUser(
        @Parameter(description = "User ID", required = true) @PathVariable Long id
    ) {
        return userService.getUserById(id);
    }
}
```

## 3. Postman

### What is Postman?

Postman is a platform for API development, testing, and documentation. It provides tools for creating, testing, and documenting APIs.

### Postman Features

#### 1. API Testing
```
┌─────────────────────────────────────────────────────────┐
│         Postman Request Builder                        │
└─────────────────────────────────────────────────────────┘

Request:
├─ Method: GET, POST, PUT, DELETE
├─ URL: https://api.example.com/users
├─ Headers: Content-Type, Authorization
├─ Body: JSON, XML, form-data
└─ Tests: Assertions, scripts
```

#### 2. Collections
- Organize requests
- Share with team
- Version control
- Environment variables

#### 3. Documentation
- Auto-generated docs
- Custom descriptions
- Examples
- Interactive documentation

#### 4. Testing
- Pre-request scripts
- Test scripts
- Assertions
- Automated testing

### Postman Setup

#### 1. Create Collection
```
New → Collection
Name: User API
Description: User management endpoints
```

#### 2. Add Requests
```
GET /users
POST /users
GET /users/{id}
PUT /users/{id}
DELETE /users/{id}
```

#### 3. Configure Environment
```json
{
  "baseUrl": "https://api.example.com",
  "apiKey": "your-api-key",
  "token": "bearer-token"
}
```

### Postman Examples

#### GET Request
```
Method: GET
URL: {{baseUrl}}/users
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json
```

#### POST Request
```
Method: POST
URL: {{baseUrl}}/users
Headers:
  Authorization: Bearer {{token}}
  Content-Type: application/json
Body (raw JSON):
{
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### Test Script
```javascript
// Postman test script
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has users array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});

pm.test("User has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData[0]).to.have.property('id');
    pm.expect(jsonData[0]).to.have.property('name');
    pm.expect(jsonData[0]).to.have.property('email');
});
```

### Postman Documentation

#### Auto-Generate Documentation
1. Create collection
2. Add descriptions to requests
3. Add examples
4. Publish documentation
5. Share link with team

#### Custom Documentation
```markdown
# User Management API

## Get All Users
Retrieves a list of all users in the system.

### Request
- Method: GET
- Endpoint: /users

### Response
- Status: 200 OK
- Body: Array of user objects
```

## 4. Integration Workflow

### Complete API Documentation Flow

```
┌─────────────────────────────────────────────────────────┐
│         API Documentation Workflow                      │
└─────────────────────────────────────────────────────────┘

1. Define API (OpenAPI Spec)
    │
    ▼
2. Generate Code (Swagger Codegen)
    │
    ▼
3. Implement API
    │
    ▼
4. Generate Documentation (Swagger UI)
    │
    ▼
5. Test API (Postman)
    │
    ▼
6. Publish Documentation
```

### Best Practices

#### 1. Keep Documentation Updated
- Update with code changes
- Version documentation
- Review regularly

#### 2. Provide Examples
- Request examples
- Response examples
- Error examples

#### 3. Document Errors
- Error codes
- Error messages
- Error handling

#### 4. Include Authentication
- Auth methods
- Token usage
- Security requirements

#### 5. Use Standards
- Follow OpenAPI spec
- Consistent naming
- Standard HTTP methods

## 5. Tool Comparison

| Feature | OpenAPI | Swagger | Postman |
|---------|---------|---------|---------|
| **Type** | Specification | Tools | Platform |
| **Documentation** | Spec file | UI | Interactive |
| **Testing** | No | Limited | Yes |
| **Code Generation** | No | Yes | No |
| **Cost** | Free | Free/Paid | Free/Paid |

## Summary

API Documentation:
- **OpenAPI**: Specification standard for REST APIs
- **Swagger**: Tools for API development and documentation
- **Postman**: Platform for API testing and documentation

**Key Components:**
- OpenAPI specification (YAML/JSON)
- Swagger UI (interactive docs)
- Postman collections (testing)
- Code generation (Swagger Codegen)

**Best Practices:**
- Keep documentation updated
- Provide examples
- Document errors
- Include authentication
- Use standards

**Remember**: Good API documentation is essential for API adoption and developer experience!
