# IAM Implementation Answers - Part 7: gRPC Implementation (Questions 31-35)

## Question 31: Walk me through your gRPC service implementation for authorization.

### Answer

### gRPC Service Implementation

#### 1. **gRPC Service Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         gRPC Service Architecture                     │
└─────────────────────────────────────────────────────────┘

Components:
├─ Protocol Buffer definitions (.proto)
├─ gRPC service implementation
├─ Service registration
└─ Client stubs
```

#### 2. **Protocol Buffer Definition**

```protobuf
// authorization.proto
syntax = "proto3";

package com.example.iam.authorization;

service AuthorizationService {
  // Unary RPC: Single request, single response
  rpc CheckPermission(AuthorizationRequest) returns (AuthorizationResponse);
  
  // Server streaming: Single request, multiple responses
  rpc StreamPermissions(AuthorizationRequest) returns (stream AuthorizationResponse);
  
  // Client streaming: Multiple requests, single response
  rpc BatchCheck(stream AuthorizationRequest) returns (BatchAuthorizationResponse);
  
  // Bidirectional streaming: Multiple requests, multiple responses
  rpc StreamBatchCheck(stream AuthorizationRequest) returns (stream AuthorizationResponse);
}

message AuthorizationRequest {
  string user_id = 1;
  string resource = 2;
  string action = 3;
  map<string, string> context = 4; // Additional context
}

message AuthorizationResponse {
  string user_id = 1;
  string resource = 2;
  string action = 3;
  bool allowed = 4;
  string reason = 5;
  int64 timestamp = 6;
}

message BatchAuthorizationResponse {
  repeated AuthorizationResponse results = 1;
  int32 total_count = 2;
  int32 allowed_count = 3;
  int32 denied_count = 4;
}
```

#### 3. **gRPC Service Implementation**

```java
@GrpcService
public class AuthorizationGrpcService extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {
    
    private final PermissionService permissionService;
    
    /**
     * Unary RPC: Single permission check
     */
    @Override
    public void checkPermission(
            AuthorizationRequest request,
            StreamObserver<AuthorizationResponse> responseObserver) {
        
        try {
            // Evaluate permission
            PermissionResult result = permissionService.evaluate(
                request.getUserId(),
                request.getResource(),
                request.getAction()
            );
            
            // Build response
            AuthorizationResponse response = AuthorizationResponse.newBuilder()
                .setUserId(request.getUserId())
                .setResource(request.getResource())
                .setAction(request.getAction())
                .setAllowed(result == PermissionResult.ALLOW)
                .setReason(result == PermissionResult.ALLOW ? "Permission granted" : "Permission denied")
                .setTimestamp(System.currentTimeMillis())
                .build();
            
            // Send response
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Error checking permission: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }
}
```

#### 4. **Server Configuration**

```java
@Configuration
public class GrpcServerConfiguration {
    @Bean
    public GrpcServerFactory grpcServerFactory() {
        NettyServerBuilder builder = NettyServerBuilder
            .forPort(9090)
            .maxInboundMessageSize(4 * 1024 * 1024) // 4MB
            .maxInboundMetadataSize(8192) // 8KB
            .permitKeepAliveTime(30, TimeUnit.SECONDS)
            .permitKeepAliveWithoutCalls(true);
        
        return new NettyGrpcServerFactory(builder);
    }
}
```

---

## Question 32: What gRPC patterns did you use (unary, streaming, bidirectional)?

### Answer

### gRPC Patterns

#### 1. **Unary RPC**

```java
/**
 * Unary RPC: Single request, single response
 * Use case: Simple permission checks
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
    
    responseObserver.onNext(AuthorizationResponse.newBuilder()
        .setAllowed(result == PermissionResult.ALLOW)
        .build());
    responseObserver.onCompleted();
}
```

#### 2. **Server Streaming**

```java
/**
 * Server streaming: Single request, multiple responses
 * Use case: Stream all permissions for a user
 */
@Override
public void streamPermissions(
        AuthorizationRequest request,
        StreamObserver<AuthorizationResponse> responseObserver) {
    
    List<Permission> permissions = permissionService.getUserPermissions(
        request.getUserId()
    );
    
    for (Permission perm : permissions) {
        AuthorizationResponse response = AuthorizationResponse.newBuilder()
            .setUserId(request.getUserId())
            .setResource(perm.getResource())
            .setAction(perm.getAction())
            .setAllowed(true)
            .build();
        
        responseObserver.onNext(response);
    }
    
    responseObserver.onCompleted();
}
```

#### 3. **Client Streaming**

```java
/**
 * Client streaming: Multiple requests, single response
 * Use case: Batch permission checks
 */
@Override
public StreamObserver<AuthorizationRequest> batchCheck(
        StreamObserver<BatchAuthorizationResponse> responseObserver) {
    
    return new StreamObserver<AuthorizationRequest>() {
        private final List<AuthorizationResponse> results = new ArrayList<>();
        
        @Override
        public void onNext(AuthorizationRequest request) {
            PermissionResult result = permissionService.evaluate(
                request.getUserId(),
                request.getResource(),
                request.getAction()
            );
            
            results.add(AuthorizationResponse.newBuilder()
                .setUserId(request.getUserId())
                .setResource(request.getResource())
                .setAction(request.getAction())
                .setAllowed(result == PermissionResult.ALLOW)
                .build());
        }
        
        @Override
        public void onError(Throwable t) {
            responseObserver.onError(t);
        }
        
        @Override
        public void onCompleted() {
            BatchAuthorizationResponse response = BatchAuthorizationResponse.newBuilder()
                .addAllResults(results)
                .setTotalCount(results.size())
                .setAllowedCount((int) results.stream().filter(AuthorizationResponse::getAllowed).count())
                .setDeniedCount((int) results.stream().filter(r -> !r.getAllowed()).count())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    };
}
```

#### 4. **Bidirectional Streaming**

```java
/**
 * Bidirectional streaming: Multiple requests, multiple responses
 * Use case: Real-time permission checks
 */
@Override
public StreamObserver<AuthorizationRequest> streamBatchCheck(
        StreamObserver<AuthorizationResponse> responseObserver) {
    
    return new StreamObserver<AuthorizationRequest>() {
        @Override
        public void onNext(AuthorizationRequest request) {
            // Process each request immediately
            PermissionResult result = permissionService.evaluate(
                request.getUserId(),
                request.getResource(),
                request.getAction()
            );
            
            // Send response immediately
            responseObserver.onNext(AuthorizationResponse.newBuilder()
                .setUserId(request.getUserId())
                .setResource(request.getResource())
                .setAction(request.getAction())
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
```

---

## Question 33: How did you handle gRPC error handling and status codes?

### Answer

### gRPC Error Handling

#### 1. **gRPC Status Codes**

```java
/**
 * gRPC error handling with appropriate status codes
 */
@GrpcService
public class AuthorizationGrpcService extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {
    
    @Override
    public void checkPermission(
            AuthorizationRequest request,
            StreamObserver<AuthorizationResponse> responseObserver) {
        
        try {
            // Validate request
            if (request.getUserId().isEmpty()) {
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("User ID is required")
                        .asRuntimeException()
                );
                return;
            }
            
            // Business logic
            PermissionResult result = permissionService.evaluate(
                request.getUserId(),
                request.getResource(),
                request.getAction()
            );
            
            responseObserver.onNext(AuthorizationResponse.newBuilder()
                .setAllowed(result == PermissionResult.ALLOW)
                .build());
            responseObserver.onCompleted();
            
        } catch (PermissionNotFoundException e) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("Permission not found: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        } catch (UnauthorizedException e) {
            responseObserver.onError(
                Status.PERMISSION_DENIED
                    .withDescription("Unauthorized: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }
}
```

#### 2. **Error Mapping**

```java
/**
 * Map exceptions to gRPC status codes
 */
@Component
public class GrpcExceptionMapper {
    
    public StatusException mapException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .withCause(e)
                .asException();
        }
        
        if (e instanceof PermissionNotFoundException) {
            return Status.NOT_FOUND
                .withDescription(e.getMessage())
                .withCause(e)
                .asException();
        }
        
        if (e instanceof UnauthorizedException) {
            return Status.PERMISSION_DENIED
                .withDescription(e.getMessage())
                .withCause(e)
                .asException();
        }
        
        if (e instanceof RateLimitException) {
            return Status.RESOURCE_EXHAUSTED
                .withDescription(e.getMessage())
                .withCause(e)
                .asException();
        }
        
        // Default to INTERNAL
        return Status.INTERNAL
            .withDescription("Internal server error")
            .withCause(e)
            .asException();
    }
}
```

---

## Question 34: What's the advantage of gRPC over REST for authorization?

### Answer

### gRPC Advantages

#### 1. **Performance Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         gRPC vs REST Performance                      │
└─────────────────────────────────────────────────────────┘

gRPC Advantages:
├─ Binary protocol (Protocol Buffers)
│   └─ Smaller payload size (30-50% smaller)
├─ HTTP/2 multiplexing
│   └─ Multiple requests over single connection
├─ Streaming support
│   └─ Real-time bidirectional communication
├─ Strong typing
│   └─ Compile-time type checking
└─ Code generation
    └─ Type-safe client/server code

Performance:
├─ Latency: 20-30% lower
├─ Throughput: 2-3x higher
└─ CPU usage: 20-30% lower
```

#### 2. **Code Example**

```java
/**
 * gRPC performance benefits
 */
// gRPC: Type-safe, generated code
AuthorizationRequest request = AuthorizationRequest.newBuilder()
    .setUserId("user123")
    .setResource("trade")
    .setAction("read")
    .build();

AuthorizationResponse response = stub.checkPermission(request);
boolean allowed = response.getAllowed(); // Type-safe

// REST: Manual JSON parsing
String json = "{\"userId\":\"user123\",\"resource\":\"trade\",\"action\":\"read\"}";
// Manual parsing, no type safety
```

---

## Question 35: How did you ensure gRPC service reliability and performance?

### Answer

### gRPC Reliability & Performance

#### 1. **Reliability Mechanisms**

```java
/**
 * gRPC reliability configuration
 */
@Configuration
public class GrpcReliabilityConfig {
    @Bean
    public NettyServerBuilder serverBuilder() {
        return NettyServerBuilder.forPort(9090)
            // Connection management
            .maxConnectionIdle(30, TimeUnit.SECONDS)
            .maxConnectionAge(300, TimeUnit.SECONDS)
            .maxConnectionAgeGrace(5, TimeUnit.SECONDS)
            
            // Keep-alive
            .permitKeepAliveTime(30, TimeUnit.SECONDS)
            .permitKeepAliveWithoutCalls(true)
            
            // Timeouts
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            
            // Message limits
            .maxInboundMessageSize(4 * 1024 * 1024) // 4MB
            .maxInboundMetadataSize(8192); // 8KB
    }
}
```

#### 2. **Client Configuration**

```java
/**
 * gRPC client configuration for reliability
 */
@Configuration
public class GrpcClientConfig {
    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9090)
            .usePlaintext() // Use TLS in production
            .maxInboundMessageSize(4 * 1024 * 1024)
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .enableRetry()
            .maxRetryAttempts(3)
            .build();
    }
}
```

#### 3. **Performance Optimization**

```java
/**
 * Performance optimizations
 */
@GrpcService
public class OptimizedAuthorizationService {
    private final ExecutorService executorService;
    
    public OptimizedAuthorizationService() {
        // Dedicated thread pool for gRPC
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
        );
    }
    
    @Override
    public void checkPermission(
            AuthorizationRequest request,
            StreamObserver<AuthorizationResponse> responseObserver) {
        
        // Execute in dedicated thread pool
        executorService.submit(() -> {
            try {
                PermissionResult result = permissionService.evaluate(
                    request.getUserId(),
                    request.getResource(),
                    request.getAction()
                );
                
                responseObserver.onNext(AuthorizationResponse.newBuilder()
                    .setAllowed(result == PermissionResult.ALLOW)
                    .build());
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(e);
            }
        });
    }
}
```

---

## Summary

Part 7 covers questions 31-35 on gRPC Implementation:

31. **gRPC Service Implementation**: Protocol buffers, service implementation, configuration
32. **gRPC Patterns**: Unary, server streaming, client streaming, bidirectional
33. **Error Handling**: Status codes, error mapping, exception handling
34. **gRPC Advantages**: Performance, type safety, streaming
35. **Reliability & Performance**: Connection management, timeouts, optimization

Key techniques:
- Protocol Buffer definitions
- Multiple gRPC patterns
- Comprehensive error handling
- Performance optimization
- Reliability configuration
