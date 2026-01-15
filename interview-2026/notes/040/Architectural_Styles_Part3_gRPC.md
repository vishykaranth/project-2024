# Architectural Styles - In Depth (Part 3: gRPC)

## 🚀 gRPC: Protocol Buffers, Streaming, and Service Definitions

---

## 1. gRPC Fundamentals

### What is gRPC?
```
┌─────────────────────────────────────────────────────────────┐
│              gRPC Overview                                   │
└─────────────────────────────────────────────────────────────┘

gRPC (gRPC Remote Procedure Calls) is a high-performance,
open-source RPC framework developed by Google.

Key Features:
1. Protocol Buffers: Binary serialization
2. HTTP/2: Multiplexing, streaming
3. Language Agnostic: Multiple language support
4. Strongly Typed: Contract-first approach
5. Streaming: Unary, server, client, bidirectional
6. Performance: Faster than REST/JSON
```

### gRPC Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              gRPC Architecture                              │
└─────────────────────────────────────────────────────────────┘

    Client (Java/Python/Go/etc.)
    │
    │ gRPC Call (HTTP/2)
    │ Protocol Buffers
    │
    ▼
┌─────────────────────────────────────┐
│      gRPC Client Stub               │
│  ┌──────────────────────────────┐  │
│  │  Generated from .proto       │  │
│  │  - Serialization             │  │
│  │  - Network calls            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ HTTP/2
    │
    ▼
┌─────────────────────────────────────┐
│      gRPC Server                    │
│  ┌──────────────────────────────┐  │
│  │  Service Implementation      │  │
│  │  - Business Logic            │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │  gRPC Server Stub            │  │
│  │  - Deserialization           │  │
│  │  - Method routing            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │
    ▼
┌─────────────────────────────────────┐
│      Data Sources                   │
│  ┌──────────┐  ┌──────────┐        │
│  │ Database │  │   APIs   │        │
│  └──────────┘  └──────────┘        │
└─────────────────────────────────────┘
```

---

## 2. Protocol Buffers

### .proto File Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Protocol Buffer Definition                      │
└─────────────────────────────────────────────────────────────┘

syntax = "proto3";

package user.v1;

// Service Definition
service UserService {
  // Unary RPC
  rpc GetUser(GetUserRequest) returns (GetUserResponse);
  
  // Server Streaming
  rpc ListUsers(ListUsersRequest) returns (stream User);
  
  // Client Streaming
  rpc CreateUsers(stream CreateUserRequest) returns (CreateUsersResponse);
  
  // Bidirectional Streaming
  rpc ChatUsers(stream ChatMessage) returns (stream ChatMessage);
}

// Message Definitions
message User {
  int32 id = 1;
  string name = 2;
  string email = 3;
  int32 age = 4;
}

message GetUserRequest {
  int32 id = 1;
}

message GetUserResponse {
  User user = 1;
}

message ListUsersRequest {
  int32 page = 1;
  int32 page_size = 2;
}

message CreateUserRequest {
  string name = 1;
  string email = 2;
  int32 age = 3;
}

message CreateUsersResponse {
  repeated User users = 1;
  int32 count = 2;
}
```

### Protocol Buffer Features
```
┌─────────────────────────────────────────────────────────────┐
│              Protocol Buffer Features                       │
└─────────────────────────────────────────────────────────────┘

Field Numbers:
- Unique identifier for each field
- Used for serialization
- Cannot be changed once used

Field Types:
- Scalar: int32, int64, string, bool, bytes, float, double
- Enums: enum UserRole { ADMIN = 0; USER = 1; }
- Nested: message within message
- Repeated: Arrays/lists
- Maps: Key-value pairs
- Oneof: Mutually exclusive fields

Versioning:
- Fields can be added (use new numbers)
- Fields can be removed (reserved keyword)
- Fields can be deprecated
```

### Serialization Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Serialization Formats                           │
└─────────────────────────────────────────────────────────────┘

JSON:
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com"
}
Size: ~60 bytes
Human-readable: ✓
Performance: Slow

Protocol Buffers:
Binary format
Size: ~30 bytes (50% smaller)
Human-readable: ✗
Performance: Fast

XML:
<user>
  <id>123</id>
  <name>John Doe</name>
  <email>john@example.com</email>
</user>
Size: ~100 bytes
Human-readable: ✓
Performance: Very slow
```

---

## 3. gRPC Service Definitions

### Unary RPC
```
┌─────────────────────────────────────────────────────────────┐
│              Unary RPC (Request-Response)                    │
└─────────────────────────────────────────────────────────────┘

Service Definition:
┌─────────────────────────────────────┐
│ service UserService {                │
│   rpc GetUser(GetUserRequest)       │
│       returns (GetUserResponse);    │
│ }                                   │
└─────────────────────────────────────┘

Client Call:
┌─────────────────────────────────────┐
│ GetUserRequest request =             │
│   GetUserRequest.newBuilder()       │
│     .setId(123)                      │
│     .build();                        │
│                                     │
│ GetUserResponse response =          │
│   stub.getUser(request);             │
│                                     │
│ User user = response.getUser();     │
└─────────────────────────────────────┘

Server Implementation:
┌─────────────────────────────────────┐
│ @Override                           │
│ public void getUser(                │
│     GetUserRequest request,          │
│     StreamObserver<GetUserResponse> responseObserver) {
│   int id = request.getId();         │
│   User user = userRepository.findById(id);
│                                     │
│   GetUserResponse response =         │
│     GetUserResponse.newBuilder()     │
│       .setUser(user)                │
│       .build();                      │
│                                     │
│   responseObserver.onNext(response);
│   responseObserver.onCompleted();   │
│ }                                    │
└─────────────────────────────────────┘

Flow:
Client ────► Request ────► Server
Client ◄──── Response ◄──── Server
```

### Server Streaming RPC
```
┌─────────────────────────────────────────────────────────────┐
│              Server Streaming                                │
└─────────────────────────────────────────────────────────────┘

Service Definition:
┌─────────────────────────────────────┐
│ service UserService {                │
│   rpc ListUsers(ListUsersRequest)    │
│       returns (stream User);         │
│ }                                   │
└─────────────────────────────────────┘

Client Call:
┌─────────────────────────────────────┐
│ ListUsersRequest request =            │
│   ListUsersRequest.newBuilder()       │
│     .setPage(1)                       │
│     .setPageSize(10)                  │
│     .build();                          │
│                                     │
│ Iterator<User> users =               │
│   stub.listUsers(request);           │
│                                     │
│ while (users.hasNext()) {            │
│   User user = users.next();         │
│   System.out.println(user.getName());
│ }                                    │
└─────────────────────────────────────┘

Server Implementation:
┌─────────────────────────────────────┐
│ @Override                           │
│ public void listUsers(               │
│     ListUsersRequest request,        │
│     StreamObserver<User> responseObserver) {
│   List<User> users =                  │
│     userRepository.findAll(           │
│       request.getPage(),              │
│       request.getPageSize()           │
│     );                                │
│                                     │
│   for (User user : users) {         │
│     responseObserver.onNext(user);   │
│   }                                  │
│                                     │
│   responseObserver.onCompleted();   │
│ }                                    │
└─────────────────────────────────────┘

Flow:
Client ────► Request ────► Server
Client ◄──── User 1 ◄──── Server
Client ◄──── User 2 ◄──── Server
Client ◄──── User 3 ◄──── Server
Client ◄──── Done ◄──── Server
```

### Client Streaming RPC
```
┌─────────────────────────────────────────────────────────────┐
│              Client Streaming                                │
└─────────────────────────────────────────────────────────────┘

Service Definition:
┌─────────────────────────────────────┐
│ service UserService {                │
│   rpc CreateUsers(stream CreateUserRequest)
│       returns (CreateUsersResponse);
│ }                                   │
└─────────────────────────────────────┘

Client Call:
┌─────────────────────────────────────┐
│ StreamObserver<CreateUsersResponse> responseObserver =
│   new StreamObserver<CreateUsersResponse>() {
│     @Override
│     public void onNext(CreateUsersResponse response) {
│       System.out.println("Created: " + response.getCount());
│     }
│     // ... onError, onCompleted
│   };
│                                     │
│ StreamObserver<CreateUserRequest> requestObserver =
│   stub.createUsers(responseObserver);
│                                     │
│ requestObserver.onNext(createUser1);
│ requestObserver.onNext(createUser2);
│ requestObserver.onNext(createUser3);
│ requestObserver.onCompleted();      │
└─────────────────────────────────────┘

Server Implementation:
┌─────────────────────────────────────┐
│ @Override                           │
│ public StreamObserver<CreateUserRequest> createUsers(
│     StreamObserver<CreateUsersResponse> responseObserver) {
│   return new StreamObserver<CreateUserRequest>() {
│     List<User> createdUsers = new ArrayList<>();
│                                     │
│     @Override
│     public void onNext(CreateUserRequest request) {
│       User user = userRepository.create(request);
│       createdUsers.add(user);        │
│     }                                │
│                                     │
│     @Override
│     public void onCompleted() {    │
│       CreateUsersResponse response = │
│         CreateUsersResponse.newBuilder()
│           .addAllUsers(createdUsers)
│           .setCount(createdUsers.size())
│           .build();
│       responseObserver.onNext(response);
│       responseObserver.onCompleted();
│     }                                │
│   };                                 │
│ }                                    │
└─────────────────────────────────────┘

Flow:
Client ────► User 1 ────► Server
Client ────► User 2 ────► Server
Client ────► User 3 ────► Server
Client ────► Done ────► Server
Client ◄──── Response ◄──── Server
```

### Bidirectional Streaming RPC
```
┌─────────────────────────────────────────────────────────────┐
│              Bidirectional Streaming                         │
└─────────────────────────────────────────────────────────────┘

Service Definition:
┌─────────────────────────────────────┐
│ service ChatService {                │
│   rpc Chat(stream ChatMessage)      │
│       returns (stream ChatMessage);  │
│ }                                   │
└─────────────────────────────────────┘

Client Call:
┌─────────────────────────────────────┐
│ StreamObserver<ChatMessage> responseObserver =
│   new StreamObserver<ChatMessage>() {
│     @Override
│     public void onNext(ChatMessage message) {
│       System.out.println("Received: " + message.getText());
│     }
│   };
│                                     │
│ StreamObserver<ChatMessage> requestObserver =
│   stub.chat(responseObserver);
│                                     │
│ requestObserver.onNext(message1);
│ requestObserver.onNext(message2);
│ // Can receive messages while sending
└─────────────────────────────────────┘

Server Implementation:
┌─────────────────────────────────────┐
│ @Override                           │
│ public StreamObserver<ChatMessage> chat(
│     StreamObserver<ChatMessage> responseObserver) {
│   return new StreamObserver<ChatMessage>() {
│     @Override
│     public void onNext(ChatMessage message) {
│       // Process message
│       ChatMessage reply = processMessage(message);
│       responseObserver.onNext(reply);
│     }                                │
│   };                                 │
│ }                                    │
└─────────────────────────────────────┘

Flow:
Client ────► Message 1 ────► Server
Client ◄──── Reply 1 ◄──── Server
Client ────► Message 2 ────► Server
Client ◄──── Reply 2 ◄──── Server
(Full duplex communication)
```

---

## 4. gRPC vs REST

### Performance Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              gRPC vs REST Performance                       │
└─────────────────────────────────────────────────────────────┘

Latency:
REST/JSON:    ~50ms
gRPC:         ~10ms (5x faster)

Throughput:
REST/JSON:    ~1000 req/s
gRPC:         ~5000 req/s (5x higher)

Payload Size:
REST/JSON:    100 bytes
gRPC/PB:      50 bytes (50% smaller)

Features:
REST:         HTTP/1.1, Text-based, Browser-friendly
gRPC:         HTTP/2, Binary, Streaming, Strong typing
```

### Use Case Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use gRPC vs REST                       │
└─────────────────────────────────────────────────────────────┘

Use gRPC when:
✅ Microservices communication
✅ High-performance requirements
✅ Real-time streaming needed
✅ Strong typing required
✅ Internal APIs
✅ Mobile apps (smaller payloads)

Use REST when:
✅ Public APIs
✅ Browser clients
✅ Simple CRUD operations
✅ Human-readable format needed
✅ Wide compatibility required
✅ Web applications
```

---

## 5. gRPC Interceptors

### Interceptor Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              gRPC Interceptors                               │
└─────────────────────────────────────────────────────────────┘

Request Flow:
Client
  │
  ▼
Client Interceptor (Auth, Logging)
  │
  ▼
Server Interceptor (Auth, Logging)
  │
  ▼
Service Implementation

Example - Authentication Interceptor:
┌─────────────────────────────────────┐
│ public class AuthInterceptor implements ServerInterceptor {
│   @Override
│   public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
│       ServerCall<ReqT, RespT> call,
│       Metadata headers,
│       ServerCallHandler<ReqT, RespT> next) {
│     String token = headers.get(Metadata.Key.of("authorization", ...));
│     if (!isValid(token)) {
│       call.close(Status.UNAUTHENTICATED, new Metadata());
│       return new ServerCall.Listener<ReqT>() {};
│     }
│     return next.startCall(call, headers);
│   }
│ }                                    │
└─────────────────────────────────────┘
```

---

## 6. Error Handling

### gRPC Status Codes
```
┌─────────────────────────────────────────────────────────────┐
│              gRPC Status Codes                              │
└─────────────────────────────────────────────────────────────┘

OK:                  Success
CANCELLED:           Operation cancelled
UNKNOWN:             Unknown error
INVALID_ARGUMENT:    Invalid argument
DEADLINE_EXCEEDED:   Timeout
NOT_FOUND:           Resource not found
ALREADY_EXISTS:      Resource already exists
PERMISSION_DENIED:   Permission denied
UNAUTHENTICATED:     Authentication required
RESOURCE_EXHAUSTED:  Resource exhausted
FAILED_PRECONDITION: Precondition failed
ABORTED:             Operation aborted
OUT_OF_RANGE:        Out of range
UNIMPLEMENTED:       Not implemented
INTERNAL:            Internal error
UNAVAILABLE:         Service unavailable
DATA_LOSS:           Data loss
```

### Error Handling Example
```
┌─────────────────────────────────────────────────────────────┐
│              Error Handling                                  │
└─────────────────────────────────────────────────────────────┘

Server:
┌─────────────────────────────────────┐
│ @Override                           │
│ public void getUser(GetUserRequest request,
│     StreamObserver<GetUserResponse> responseObserver) {
│   try {
│     User user = userRepository.findById(request.getId());
│     if (user == null) {
│       responseObserver.onError(
│         Status.NOT_FOUND
│           .withDescription("User not found")
│           .asRuntimeException()
│       );
│       return;
│     }
│     responseObserver.onNext(response);
│     responseObserver.onCompleted();
│   } catch (Exception e) {
│     responseObserver.onError(
│       Status.INTERNAL
│         .withDescription(e.getMessage())
│         .asRuntimeException()
│     );
│   }
│ }                                    │
└─────────────────────────────────────┘

Client:
┌─────────────────────────────────────┐
│ try {
│   GetUserResponse response = stub.getUser(request);
│ } catch (StatusRuntimeException e) {
│   Status status = e.getStatus();
│   if (status.getCode() == Status.Code.NOT_FOUND) {
│     // Handle not found
│   }
│ }                                    │
└─────────────────────────────────────┘
```

---

## 7. gRPC Best Practices

### Service Design
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

✅ Do:
- Use meaningful service and method names
- Keep messages focused and small
- Use streaming for large datasets
- Implement proper error handling
- Use interceptors for cross-cutting concerns
- Version your services
- Document with comments

❌ Don't:
- Don't create overly large messages
- Don't ignore errors
- Don't mix concerns in services
- Don't use unary when streaming is better
- Don't expose internal details
```

### Performance Tips
```
┌─────────────────────────────────────────────────────────────┐
│              Performance Optimization                        │
└─────────────────────────────────────────────────────────────┘

1. Use streaming for large data
2. Implement connection pooling
3. Use keepalive pings
4. Batch small requests
5. Use compression
6. Implement proper timeout handling
7. Use load balancing
8. Monitor and optimize message sizes
```

---

## Key Takeaways

### gRPC Advantages
```
✅ High performance (HTTP/2, binary)
✅ Strong typing (Protocol Buffers)
✅ Streaming support
✅ Language agnostic
✅ Built-in error handling
✅ Code generation
✅ Efficient serialization
```

### gRPC Use Cases
```
✅ Microservices communication
✅ Real-time applications
✅ High-throughput systems
✅ Mobile applications
✅ Internal APIs
✅ Service-to-service communication
```

---

**Next: Part 4 will cover SOA (Service-Oriented Architecture).**

