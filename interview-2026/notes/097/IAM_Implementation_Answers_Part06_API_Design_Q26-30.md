# IAM Implementation Answers - Part 6: API Design (Questions 26-30)

## Question 26: You "designed and developed RESTful APIs and gRPC services for external authorization." Explain your API design approach.

### Answer

### API Design Approach

#### 1. **API Design Principles**

```
┌─────────────────────────────────────────────────────────┐
│         API Design Principles                          │
└─────────────────────────────────────────────────────────┘

Principles:
├─ RESTful design
├─ Resource-oriented
├─ Stateless
├─ Versioning
├─ Security
└─ Documentation
```

#### 2. **RESTful API Design**

```java
@RestController
@RequestMapping("/api/v1/authorization")
public class AuthorizationController {
    
    /**
     * Check permission
     * GET /api/v1/authorization/check?userId=user123&resource=trade&action=read
     */
    @GetMapping("/check")
    public ResponseEntity<AuthorizationResponse> checkPermission(
            @RequestParam String userId,
            @RequestParam String resource,
            @RequestParam String action) {
        
        PermissionResult result = permissionService.evaluate(userId, resource, action);
        
        return ResponseEntity.ok(new AuthorizationResponse(
            userId,
            resource,
            action,
            result
        ));
    }
    
    /**
     * Batch check permissions
     * POST /api/v1/authorization/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchAuthorizationResponse> batchCheck(
            @RequestBody BatchAuthorizationRequest request) {
        
        Map<String, PermissionResult> results = permissionService
            .evaluateBatch(request.getUserId(), request.getPermissions());
        
        return ResponseEntity.ok(new BatchAuthorizationResponse(results));
    }
}
```

#### 3. **Resource-Oriented Design**

```java
/**
 * Resource-oriented API design
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/permissions")
public class UserPermissionController {
    
    /**
     * Get all permissions for user
     * GET /api/v1/users/{userId}/permissions
     */
    @GetMapping
    public ResponseEntity<List<Permission>> getUserPermissions(
            @PathVariable String userId) {
        List<Permission> permissions = permissionService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Create permission for user
     * POST /api/v1/users/{userId}/permissions
     */
    @PostMapping
    public ResponseEntity<Permission> createPermission(
            @PathVariable String userId,
            @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
            userId,
            request.getResource(),
            request.getAction()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }
    
    /**
     * Delete permission
     * DELETE /api/v1/users/{userId}/permissions/{permissionId}
     */
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(
            @PathVariable String userId,
            @PathVariable String permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Question 27: Why did you choose both REST and gRPC? What are the use cases for each?

### Answer

### REST vs gRPC Use Cases

#### 1. **Technology Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         REST vs gRPC Comparison                       │
└─────────────────────────────────────────────────────────┘

REST:
├─ Use cases:
│   ├─ External APIs
│   ├─ Web integration
│   ├─ Human-readable
│   └─ Browser support
├─ Pros:
│   ├─ Simple
│   ├─ Standard HTTP
│   ├─ Easy debugging
│   └─ Wide support
└─ Cons:
    ├─ Text-based (larger payload)
    └─ Slower than gRPC

gRPC:
├─ Use cases:
│   ├─ Internal services
│   ├─ High performance
│   ├─ Streaming
│   └─ Microservices
├─ Pros:
│   ├─ Binary protocol (faster)
│   ├─ Streaming support
│   ├─ Strong typing
│   └─ Code generation
└─ Cons:
    ├─ Complex setup
    └─ Limited browser support
```

#### 2. **REST Use Cases**

```java
/**
 * REST API for external clients
 */
@RestController
@RequestMapping("/api/v1/authorization")
public class ExternalAuthorizationAPI {
    
    /**
     * Use REST for:
     * - External clients (web, mobile)
     * - Simple integration
     * - Human-readable responses
     */
    @GetMapping("/check")
    public AuthorizationResponse checkPermission(
            @RequestParam String userId,
            @RequestParam String resource,
            @RequestParam String action) {
        // REST is better for:
        // - External clients
        // - Web browsers
        // - Simple integration
        return permissionService.evaluate(userId, resource, action);
    }
}
```

#### 3. **gRPC Use Cases**

```java
/**
 * gRPC for internal high-performance services
 */
@GrpcService
public class AuthorizationGrpcService extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {
    
    /**
     * Use gRPC for:
     * - Internal service-to-service
     * - High performance requirements
     * - Streaming capabilities
     * - Strong typing
     */
    @Override
    public void checkPermission(
            AuthorizationRequest request,
            StreamObserver<AuthorizationResponse> responseObserver) {
        
        PermissionResult result = permissionService.evaluate(
            request.getUserId(),
            request.getResource(),
            request.getAction()
        );
        
        AuthorizationResponse response = AuthorizationResponse.newBuilder()
            .setUserId(request.getUserId())
            .setResource(request.getResource())
            .setAction(request.getAction())
            .setAllowed(result == PermissionResult.ALLOW)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    /**
     * Streaming for batch operations
     */
    @Override
    public StreamObserver<AuthorizationRequest> batchCheck(
            StreamObserver<AuthorizationResponse> responseObserver) {
        
        return new StreamObserver<AuthorizationRequest>() {
            @Override
            public void onNext(AuthorizationRequest request) {
                // Process each request
                PermissionResult result = permissionService.evaluate(
                    request.getUserId(),
                    request.getResource(),
                    request.getAction()
                );
                
                responseObserver.onNext(AuthorizationResponse.newBuilder()
                    .setAllowed(result == PermissionResult.ALLOW)
                    .build());
            }
            
            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }
            
            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
```

---

## Question 28: How did you design the REST API for authorization?

### Answer

### REST API Design for Authorization

#### 1. **API Structure**

```
┌─────────────────────────────────────────────────────────┐
│         REST API Structure                             │
└─────────────────────────────────────────────────────────┘

Endpoints:
├─ GET /api/v1/authorization/check
├─ POST /api/v1/authorization/batch
├─ GET /api/v1/users/{userId}/permissions
├─ POST /api/v1/users/{userId}/permissions
└─ DELETE /api/v1/users/{userId}/permissions/{id}
```

#### 2. **Authorization Check Endpoint**

```java
@RestController
@RequestMapping("/api/v1/authorization")
public class AuthorizationController {
    
    /**
     * Single permission check
     */
    @GetMapping("/check")
    public ResponseEntity<AuthorizationResponse> checkPermission(
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String resource,
            @RequestParam @NotBlank String action) {
        
        PermissionResult result = permissionService.evaluate(userId, resource, action);
        
        return ResponseEntity.ok(new AuthorizationResponse(
            userId,
            resource,
            action,
            result,
            Instant.now()
        ));
    }
    
    /**
     * Batch permission check
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchAuthorizationResponse> batchCheck(
            @Valid @RequestBody BatchAuthorizationRequest request) {
        
        Map<String, PermissionResult> results = permissionService.evaluateBatch(
            request.getUserId(),
            request.getPermissions()
        );
        
        return ResponseEntity.ok(new BatchAuthorizationResponse(results));
    }
}
```

#### 3. **Request/Response Models**

```java
/**
 * Request models
 */
public class AuthorizationRequest {
    @NotBlank
    private String userId;
    
    @NotBlank
    private String resource;
    
    @NotBlank
    private String action;
    
    // Getters and setters
}

public class BatchAuthorizationRequest {
    @NotBlank
    private String userId;
    
    @NotEmpty
    private List<PermissionRequest> permissions;
    
    // Getters and setters
}

/**
 * Response models
 */
public class AuthorizationResponse {
    private String userId;
    private String resource;
    private String action;
    private PermissionResult result;
    private Instant timestamp;
    
    // Getters and setters
}

public class BatchAuthorizationResponse {
    private Map<String, PermissionResult> results;
    private Instant timestamp;
    
    // Getters and setters
}
```

---

## Question 29: What's your approach to API versioning for the IAM system?

### Answer

### API Versioning Strategy

#### 1. **Versioning Approach**

```
┌─────────────────────────────────────────────────────────┐
│         API Versioning Strategy                        │
└─────────────────────────────────────────────────────────┘

Versioning Methods:
├─ URL versioning (/api/v1/, /api/v2/)
├─ Header versioning (Accept: application/vnd.api.v1+json)
├─ Query parameter (?version=1)
└─ Chosen: URL versioning (simplest)
```

#### 2. **URL Versioning Implementation**

```java
@RestController
@RequestMapping("/api/v1/authorization")
public class AuthorizationV1Controller {
    // V1 implementation
}

@RestController
@RequestMapping("/api/v2/authorization")
public class AuthorizationV2Controller {
    // V2 implementation with improvements
}
```

#### 3. **Version Management**

```java
/**
 * API version management
 */
@Configuration
public class ApiVersioningConfig {
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        ApiVersionRequestMappingHandlerMapping mapping = 
            new ApiVersionRequestMappingHandlerMapping();
        mapping.setOrder(0);
        return mapping;
    }
}

/**
 * Version annotation
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    String value();
}
```

---

## Question 30: How did you document the APIs (OpenAPI, Swagger)?

### Answer

### API Documentation

#### 1. **OpenAPI/Swagger Documentation**

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("IAM Authorization API")
                .version("v1")
                .description("Identity and Access Management Authorization API")
                .contact(new Contact()
                    .name("IAM Team")
                    .email("iam@example.com")))
            .servers(List.of(
                new Server().url("https://api.example.com").description("Production"),
                new Server().url("https://api-staging.example.com").description("Staging")
            ))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

#### 2. **API Documentation Annotations**

```java
@RestController
@RequestMapping("/api/v1/authorization")
@Tag(name = "Authorization", description = "Authorization API endpoints")
public class AuthorizationController {
    
    @Operation(
        summary = "Check permission",
        description = "Check if a user has permission to perform an action on a resource"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission check result",
            content = @Content(schema = @Schema(implementation = AuthorizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        )
    })
    @GetMapping("/check")
    public ResponseEntity<AuthorizationResponse> checkPermission(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Resource name", required = true)
            @RequestParam String resource,
            @Parameter(description = "Action name", required = true)
            @RequestParam String action) {
        // Implementation
    }
}
```

---

## Summary

Part 6 covers questions 26-30 on API Design:

26. **API Design Approach**: RESTful design, resource-oriented, stateless
27. **REST vs gRPC**: Use cases, comparison, when to use each
28. **REST API Design**: Endpoints, request/response models
29. **API Versioning**: URL versioning, version management
30. **API Documentation**: OpenAPI/Swagger, annotations

Key techniques:
- RESTful API design principles
- Strategic use of REST and gRPC
- Comprehensive API documentation
- API versioning strategy
- Resource-oriented design
