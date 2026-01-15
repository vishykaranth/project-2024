# API Documentation: OpenAPI/Swagger, API Contracts

## Overview

API Documentation is essential for API adoption and integration. It describes what an API does, how to use it, and what to expect. OpenAPI (formerly Swagger) is the industry standard for REST API documentation.

## Why API Documentation Matters

```
┌─────────────────────────────────────────────────────────┐
│         Importance of API Documentation                 │
└─────────────────────────────────────────────────────────┘

Benefits:
├─ Developer Onboarding
│  └─ Faster integration, less support
│
├─ Consistency
│  └─ Standardized format, clear contracts
│
├─ Testing
│  └─ Generate test cases, validate requests
│
├─ Code Generation
│  └─ Client SDKs, server stubs
│
└─ Collaboration
   └─ Shared understanding, contract-first design
```

## OpenAPI Specification

### What is OpenAPI?

OpenAPI (formerly Swagger) is a specification for describing REST APIs. It provides a standard format for API documentation that is both human-readable and machine-readable.

### OpenAPI Structure

```
┌─────────────────────────────────────────────────────────┐
│              OpenAPI Document Structure                  │
└─────────────────────────────────────────────────────────┘

openapi: 3.0.0
info:
  title: User API
  version: 1.0.0
  description: API for managing users

servers:
  - url: https://api.example.com/v1

paths:
  /users:
    get:
      summary: List users
      responses:
        '200':
          description: Success

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
        name:
          type: string
```

### OpenAPI 3.0 Components

```
┌─────────────────────────────────────────────────────────┐
│              OpenAPI 3.0 Key Components                │
└─────────────────────────────────────────────────────────┘

├─ info
│  └─ API metadata (title, version, description)
│
├─ servers
│  └─ Base URLs for API
│
├─ paths
│  └─ API endpoints and operations
│
├─ components
│  ├─ schemas (data models)
│  ├─ parameters (reusable parameters)
│  ├─ responses (reusable responses)
│  ├─ requestBodies (request payloads)
│  └─ securitySchemes (auth methods)
│
└─ security
   └─ Global security requirements
```

## OpenAPI Example

### Complete API Definition

```yaml
openapi: 3.0.0
info:
  title: User Management API
  version: 1.0.0
  description: |
    API for managing users in the system.
    Provides CRUD operations for user resources.
  contact:
    name: API Support
    email: support@example.com
  license:
    name: MIT
    url: https://opensource.org/licenses/MIT

servers:
  - url: https://api.example.com/v1
    description: Production server
  - url: https://staging-api.example.com/v1
    description: Staging server

paths:
  /users:
    get:
      summary: List all users
      description: Retrieve a list of all users
      operationId: listUsers
      tags:
        - Users
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 1
        - name: limit
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/User'
                  pagination:
                    $ref: '#/components/schemas/Pagination'
    
    post:
      summary: Create a new user
      operationId: createUser
      tags:
        - Users
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '400':
          description: Bad request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /users/{id}:
    get:
      summary: Get user by ID
      operationId: getUserById
      tags:
        - Users
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
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

components:
  schemas:
    User:
      type: object
      properties:
        id:
          type: integer
          example: 123
        name:
          type: string
          example: John Doe
        email:
          type: string
          format: email
          example: john@example.com
        createdAt:
          type: string
          format: date-time
          example: 2024-01-15T10:30:00Z
      required:
        - id
        - name
        - email

    CreateUserRequest:
      type: object
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 100
        email:
          type: string
          format: email
        password:
          type: string
          format: password
          minLength: 8
      required:
        - name
        - email
        - password

    Pagination:
      type: object
      properties:
        page:
          type: integer
        limit:
          type: integer
        total:
          type: integer
        totalPages:
          type: integer

    Error:
      type: object
      properties:
        code:
          type: string
        message:
          type: string
        timestamp:
          type: string
          format: date-time

  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

security:
  - bearerAuth: []
```

## Swagger UI

### What is Swagger UI?

Swagger UI is an interactive documentation interface that renders OpenAPI specifications. It allows developers to explore and test APIs directly from the browser.

### Swagger UI Features

```
┌─────────────────────────────────────────────────────────┐
│              Swagger UI Capabilities                    │
└─────────────────────────────────────────────────────────┘

├─ Interactive Documentation
│  ├─ Browse endpoints
│  ├─ View request/response schemas
│  └─ See examples
│
├─ Try It Out
│  ├─ Execute API calls
│  ├─ See real responses
│  └─ Test authentication
│
├─ Schema Explorer
│  ├─ View data models
│  ├─ Understand relationships
│  └─ See validation rules
│
└─ Code Generation
   ├─ Generate client SDKs
   ├─ Generate server stubs
   └─ Export to Postman
```

### Swagger UI Integration (Spring Boot)

```java
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Management API")
                .version("1.0.0")
                .description("API for managing users"))
            .addServersItem(new Server()
                .url("https://api.example.com/v1")
                .description("Production"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### Accessing Swagger UI

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/swagger-ui/index.html
```

## API Contracts

### What is an API Contract?

An API Contract is a formal agreement between API provider and consumer that defines:
- Request/response formats
- Data types and validation rules
- Error handling
- Authentication requirements

### Contract-First Development

```
┌─────────────────────────────────────────────────────────┐
│         Contract-First Development Flow                 │
└─────────────────────────────────────────────────────────┘

1. Design API Contract
   │
   ├─► Define endpoints
   ├─► Define schemas
   └─► Write OpenAPI spec
   │
   ▼
2. Review Contract
   │
   ├─► Stakeholder review
   ├─► Team review
   └─► Client review
   │
   ▼
3. Generate Code
   │
   ├─► Server stubs
   ├─► Client SDKs
   └─► Tests
   │
   ▼
4. Implement
   │
   ├─► Server implementation
   └─► Client integration
   │
   ▼
5. Validate
   │
   └─► Contract testing
```

### Contract Testing

Contract testing ensures that API implementations match the documented contract.

```java
// Contract Test Example
@SpringBootTest
@AutoConfigureMockMvc
public class UserApiContractTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetUser_shouldMatchContract() throws Exception {
        mockMvc.perform(get("/api/v1/users/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.email").exists())
            .andDo(document("get-user",
                responseFields(
                    fieldWithPath("id").description("User ID"),
                    fieldWithPath("name").description("User name"),
                    fieldWithPath("email").description("User email")
                )
            ));
    }
}
```

## Documentation Best Practices

### 1. Keep Documentation Up-to-Date

```yaml
# ✅ GOOD: Documented
/users:
  get:
    summary: List users
    parameters:
      - name: status
        in: query
        schema:
          type: string
          enum: [active, inactive]

# ❌ BAD: Missing documentation
/users:
  get: {}
```

### 2. Provide Examples

```yaml
responses:
  '200':
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/User'
        examples:
          userExample:
            value:
              id: 123
              name: John Doe
              email: john@example.com
```

### 3. Document Error Responses

```yaml
responses:
  '400':
    description: Bad request
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/Error'
        examples:
          validationError:
            value:
              code: VALIDATION_ERROR
              message: Email is required
              timestamp: 2024-01-15T10:30:00Z
```

### 4. Use Tags for Organization

```yaml
paths:
  /users:
    get:
      tags:
        - Users
  /orders:
    get:
      tags:
        - Orders
```

### 5. Document Authentication

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
    apiKey:
      type: apiKey
      in: header
      name: X-API-Key

security:
  - bearerAuth: []
  - apiKey: []
```

## Code Generation from OpenAPI

### Generate Server Stubs

```bash
# Generate Spring Boot server
openapi-generator generate \
  -i api.yaml \
  -g spring \
  -o server

# Generate Node.js server
openapi-generator generate \
  -i api.yaml \
  -g nodejs-express-server \
  -o server
```

### Generate Client SDKs

```bash
# Generate Java client
openapi-generator generate \
  -i api.yaml \
  -g java \
  -o client

# Generate TypeScript client
openapi-generator generate \
  -i api.yaml \
  -g typescript-axios \
  -o client
```

## API Documentation Tools

### Documentation Tools

| Tool | Type | Features |
|------|------|----------|
| **Swagger UI** | Interactive | Try it out, schema explorer |
| **ReDoc** | Static | Clean, responsive design |
| **Postman** | Testing | Import OpenAPI, collections |
| **Insomnia** | Testing | REST client, OpenAPI support |
| **Stoplight** | Design | Visual editor, mock server |

### Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Documentation Tools Comparison                  │
└─────────────────────────────────────────────────────────┘

Swagger UI:
├─ Pros: Interactive, try it out, widely used
└─ Cons: Can be cluttered, requires server

ReDoc:
├─ Pros: Clean design, responsive, static
└─ Cons: No try it out, less interactive

Postman:
├─ Pros: Testing, collections, collaboration
└─ Cons: Not just documentation tool
```

## Summary

API Documentation:
- **OpenAPI/Swagger**: Industry standard for REST API documentation
- **Swagger UI**: Interactive documentation interface
- **API Contracts**: Formal agreements between provider and consumer
- **Contract Testing**: Validates implementation matches contract

**Key Components:**
- OpenAPI specification (YAML/JSON)
- Interactive documentation
- Code generation
- Contract testing

**Best Practices:**
- Keep documentation up-to-date
- Provide examples
- Document all error responses
- Use tags for organization
- Document authentication

**Remember**: Good API documentation is essential for API adoption and reduces support burden!
