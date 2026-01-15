# gRPC: Protocol Buffers, Streaming, Service Definitions

## Overview

gRPC is a high-performance, open-source RPC (Remote Procedure Call) framework developed by Google. It uses Protocol Buffers for serialization and HTTP/2 for transport, making it efficient for microservices communication.

## What is gRPC?

```
┌─────────────────────────────────────────────────────────┐
│              gRPC Architecture                           │
└─────────────────────────────────────────────────────────┘

Client                    Server
    │                         │
    │─── RPC Call ───────────►│
    │   (Protocol Buffer)     │
    │                         │
    │◄── Response ────────────│
    │   (Protocol Buffer)     │
    │                         │
```

## gRPC vs REST

```
┌─────────────────────────────────────────────────────────┐
│         gRPC vs REST                                     │
└─────────────────────────────────────────────────────────┘

gRPC:
├─ Binary protocol (Protocol Buffers)
├─ HTTP/2 transport
├─ Strongly typed
├─ Streaming support
├─ High performance
└─ Language agnostic

REST:
├─ Text protocol (JSON/XML)
├─ HTTP/1.1 transport
├─ Loosely typed
├─ Request/Response only
├─ Lower performance
└─ Web-friendly
```

## Protocol Buffers

### What are Protocol Buffers?

Protocol Buffers (protobuf) are Google's language-neutral, platform-neutral mechanism for serializing structured data. They're more efficient than JSON/XML.

### .proto File

```protobuf
syntax = "proto3";

package com.example.user;

// User service definition
service UserService {
  rpc GetUser(GetUserRequest) returns (User);
  rpc CreateUser(CreateUserRequest) returns (User);
  rpc ListUsers(ListUsersRequest) returns (ListUsersResponse);
  rpc UpdateUser(UpdateUserRequest) returns (User);
  rpc DeleteUser(DeleteUserRequest) returns (DeleteUserResponse);
}

// Request messages
message GetUserRequest {
  string id = 1;
}

message CreateUserRequest {
  string name = 1;
  string email = 2;
  string password = 3;
}

message ListUsersRequest {
  int32 page = 1;
  int32 page_size = 2;
}

message UpdateUserRequest {
  string id = 1;
  string name = 2;
  string email = 3;
}

message DeleteUserRequest {
  string id = 1;
}

// Response messages
message User {
  string id = 1;
  string name = 2;
  string email = 3;
  string created_at = 4;
}

message ListUsersResponse {
  repeated User users = 1;
  int32 total = 2;
  int32 page = 3;
  int32 page_size = 4;
}

message DeleteUserResponse {
  bool success = 1;
  string message = 2;
}
```

### Protocol Buffer Types

```protobuf
// Scalar types
string
int32, int64
uint32, uint64
float, double
bool
bytes

// Complex types
message User {
  string id = 1;
  string name = 2;
}

repeated User users = 1;  // Array/list
map<string, User> userMap = 2;  // Map
```

## gRPC Service Types

### 1. Unary RPC

Simple request-response pattern.

```protobuf
service UserService {
  rpc GetUser(GetUserRequest) returns (User);
}
```

```java
// Server implementation
@Override
public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
    User user = userService.getUser(request.getId());
    responseObserver.onNext(user);
    responseObserver.onCompleted();
}

// Client call
User user = blockingStub.getUser(GetUserRequest.newBuilder()
    .setId("123")
    .build());
```

### 2. Server Streaming

Server sends multiple responses to a single request.

```protobuf
service UserService {
  rpc ListUsers(ListUsersRequest) returns (stream User);
}
```

```java
// Server implementation
@Override
public void listUsers(ListUsersRequest request, StreamObserver<User> responseObserver) {
    List<User> users = userService.getAllUsers();
    users.forEach(user -> {
        responseObserver.onNext(user);
    });
    responseObserver.onCompleted();
}

// Client call
Iterator<User> users = blockingStub.listUsers(ListUsersRequest.newBuilder()
    .setPage(1)
    .setPageSize(10)
    .build());
while (users.hasNext()) {
    User user = users.next();
    // Process user
}
```

### 3. Client Streaming

Client sends multiple requests, server responds once.

```protobuf
service UserService {
  rpc CreateUsers(stream CreateUserRequest) returns (CreateUsersResponse);
}
```

```java
// Server implementation
@Override
public StreamObserver<CreateUserRequest> createUsers(StreamObserver<CreateUsersResponse> responseObserver) {
    return new StreamObserver<CreateUserRequest>() {
        List<User> createdUsers = new ArrayList<>();
        
        @Override
        public void onNext(CreateUserRequest request) {
            User user = userService.createUser(request);
            createdUsers.add(user);
        }
        
        @Override
        public void onError(Throwable t) {
            responseObserver.onError(t);
        }
        
        @Override
        public void onCompleted() {
            CreateUsersResponse response = CreateUsersResponse.newBuilder()
                .addAllUsers(createdUsers)
                .setCount(createdUsers.size())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    };
}

// Client call
StreamObserver<CreateUserRequest> requestObserver = asyncStub.createUsers(
    new StreamObserver<CreateUsersResponse>() {
        @Override
        public void onNext(CreateUsersResponse response) {
            // Handle response
        }
        
        @Override
        public void onError(Throwable t) {
            // Handle error
        }
        
        @Override
        public void onCompleted() {
            // Stream completed
        }
    }
);

// Send multiple requests
requestObserver.onNext(CreateUserRequest.newBuilder()
    .setName("User 1")
    .setEmail("user1@example.com")
    .build());
requestObserver.onNext(CreateUserRequest.newBuilder()
    .setName("User 2")
    .setEmail("user2@example.com")
    .build());
requestObserver.onCompleted();
```

### 4. Bidirectional Streaming

Both client and server send multiple messages.

```protobuf
service UserService {
  rpc Chat(stream ChatMessage) returns (stream ChatMessage);
}
```

```java
// Server implementation
@Override
public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
    return new StreamObserver<ChatMessage>() {
        @Override
        public void onNext(ChatMessage message) {
            // Process message and respond
            ChatMessage response = processMessage(message);
            responseObserver.onNext(response);
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

## gRPC Implementation (Java)

### Server Implementation

```java
@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        try {
            com.example.entity.User entity = userService.getUser(request.getId());
            User user = User.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setEmail(entity.getEmail())
                .setCreatedAt(entity.getCreatedAt().toString())
                .build();
            
            responseObserver.onNext(user);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
    
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            com.example.entity.User entity = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
            );
            
            User user = User.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setEmail(entity.getEmail())
                .setCreatedAt(entity.getCreatedAt().toString())
                .build();
            
            responseObserver.onNext(user);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException());
        }
    }
}
```

### Client Implementation

```java
@Service
public class UserGrpcClient {
    
    private final UserServiceGrpc.UserServiceBlockingStub blockingStub;
    private final UserServiceGrpc.UserServiceStub asyncStub;
    
    public UserGrpcClient(ManagedChannel channel) {
        this.blockingStub = UserServiceGrpc.newBlockingStub(channel);
        this.asyncStub = UserServiceGrpc.newStub(channel);
    }
    
    public User getUser(String id) {
        GetUserRequest request = GetUserRequest.newBuilder()
            .setId(id)
            .build();
        
        return blockingStub.getUser(request);
    }
    
    public User createUser(String name, String email, String password) {
        CreateUserRequest request = CreateUserRequest.newBuilder()
            .setName(name)
            .setEmail(email)
            .setPassword(password)
            .build();
        
        return blockingStub.createUser(request);
    }
}
```

## gRPC Configuration

### Server Configuration

```yaml
# application.yml
grpc:
  server:
    port: 9090
```

### Client Configuration

```java
@Configuration
public class GrpcClientConfig {
    
    @Bean
    public ManagedChannel userServiceChannel() {
        return ManagedChannelBuilder.forAddress("localhost", 9090)
            .usePlaintext()  // For development only
            .build();
    }
}
```

## gRPC Advantages

```
┌─────────────────────────────────────────────────────────┐
│         gRPC Advantages                                 │
└─────────────────────────────────────────────────────────┘

├─ Performance
│  ├─ Binary protocol (smaller payloads)
│  ├─ HTTP/2 (multiplexing)
│  └─ Efficient serialization
│
├─ Strong Typing
│  └─ Protocol buffer schemas
│
├─ Streaming
│  └─ Real-time communication
│
├─ Language Agnostic
│  └─ Works with many languages
│
└─ Code Generation
   └─ Auto-generate client/server code
```

## gRPC Challenges

```
┌─────────────────────────────────────────────────────────┐
│         gRPC Challenges                                  │
└─────────────────────────────────────────────────────────┘

├─ Browser Support
│  └─ Requires gRPC-Web for browsers
│
├─ Human Readability
│  └─ Binary format (harder to debug)
│
├─ Learning Curve
│  └─ Different from REST
│
└─ Tooling
   └─ Less tooling than REST
```

## gRPC-Web

### For Browser Clients

```protobuf
// Enable gRPC-Web
syntax = "proto3";
package example;

import "google/api/annotations.proto";

service UserService {
  rpc GetUser(GetUserRequest) returns (User) {
    option (google.api.http) = {
      get: "/v1/users/{id}"
    };
  }
}
```

## Best Practices

### 1. Use Meaningful Message Names

```protobuf
// ✅ GOOD
message GetUserRequest { }
message CreateUserRequest { }

// ❌ BAD
message Request { }
message Response { }
```

### 2. Version Your Services

```protobuf
package com.example.user.v1;

service UserServiceV1 {
  rpc GetUser(GetUserRequest) returns (User);
}

package com.example.user.v2;

service UserServiceV2 {
  rpc GetUser(GetUserRequest) returns (UserV2);
}
```

### 3. Handle Errors Properly

```java
try {
    // Business logic
} catch (UserNotFoundException e) {
    responseObserver.onError(Status.NOT_FOUND
        .withDescription("User not found")
        .asRuntimeException());
} catch (Exception e) {
    responseObserver.onError(Status.INTERNAL
        .withDescription("Internal error")
        .asRuntimeException());
}
```

### 4. Use Streaming for Large Data

```protobuf
// Use streaming for large datasets
rpc ListUsers(ListUsersRequest) returns (stream User);
```

### 5. Implement Timeouts

```java
User user = blockingStub
    .withDeadlineAfter(5, TimeUnit.SECONDS)
    .getUser(request);
```

## Summary

gRPC:
- **Protocol Buffers**: Efficient binary serialization
- **Service Types**: Unary, Server Streaming, Client Streaming, Bidirectional
- **Benefits**: High performance, strong typing, streaming support
- **Use Cases**: Microservices, real-time communication, high-performance APIs

**Key Features:**
- Protocol Buffer schemas
- HTTP/2 transport
- Streaming support
- Code generation
- Language agnostic

**Best Practices:**
- Use meaningful message names
- Version services
- Handle errors properly
- Use streaming for large data
- Implement timeouts

**Remember**: gRPC is ideal for high-performance microservices communication with strong typing and streaming capabilities!
