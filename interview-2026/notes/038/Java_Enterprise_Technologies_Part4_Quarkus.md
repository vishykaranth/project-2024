# Java Enterprise Technologies - In-Depth Diagrams (Part 4: Quarkus)

## ⚡ Quarkus - Cloud-Native Java Framework

---

## 1. Quarkus Architecture

### Quarkus Platform Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Quarkus Architecture                           │
└─────────────────────────────────────────────────────────────┘

Application Layer
┌─────────────────────────────────────┐
│  ┌──────────┐  ┌──────────┐        │
│  │ REST     │  │ GraphQL │        │
│  │ Services │  │ Services│        │
│  └──────────┘  └──────────┘        │
│  ┌──────────┐  ┌──────────┐        │
│  │ Messaging│  │ WebSocket│        │
│  │ Services │  │ Services │        │
│  └──────────┘  └──────────┘        │
└─────────────────────────────────────┘
    │
    │ Uses
    ▼
┌─────────────────────────────────────┐
│  Quarkus Framework                  │
│  ┌──────────────────────────────┐  │
│  │ Build-Time Optimization      │  │
│  │  ├─ Bytecode Analysis        │  │
│  │  ├─ Dead Code Elimination    │  │
│  │  └─ Reflection Registration   │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │ CDI (Contexts & DI)          │  │
│  │  ├─ Build-time DI            │  │
│  │  └─ Runtime DI               │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │ Reactive Engine               │  │
│  │  ├─ Vert.x                    │  │
│  │  └─ Mutiny                    │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Runs on
    ▼
┌─────────────────────────────────────┐
│  JVM or GraalVM Native Image        │
└─────────────────────────────────────┘
```

### Build-Time vs Runtime
```
┌─────────────────────────────────────────────────────────────┐
│              Build-Time Optimization                        │
└─────────────────────────────────────────────────────────────┘

Build Phase
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐   │
│  │ Bytecode Analysis            │   │
│  │  ├─ Scan annotations         │   │
│  │  ├─ Find dependencies        │   │
│  │  └─ Build metadata           │   │
│  └──────────────────────────────┘   │
│        │                             │
│        ▼                             │
│  ┌──────────────────────────────┐   │
│  │ Dead Code Elimination         │   │
│  │  ├─ Remove unused classes     │   │
│  │  ├─ Remove unused methods     │   │
│  │  └─ Optimize bytecode         │   │
│  └──────────────────────────────┘   │
│        │                             │
│        ▼                             │
│  ┌──────────────────────────────┐   │
│  │ Reflection Registration        │   │
│  │  ├─ Register classes          │   │
│  │  ├─ Register methods          │   │
│  │  └─ Generate metadata          │   │
│  └──────────────────────────────┘   │
│        │                             │
│        ▼                             │
│  ┌──────────────────────────────┐   │
│  │ Native Image Generation        │   │
│  │  ├─ AOT Compilation            │   │
│  │  ├─ Substrate VM               │   │
│  │  └─ Executable binary           │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
    │
    │ Produces
    ▼
Runtime Phase
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐   │
│  │ Fast Startup                 │   │
│  │  ├─ No class loading         │   │
│  │  ├─ No reflection overhead   │   │
│  │  └─ Pre-compiled code        │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │ Low Memory Footprint         │   │
│  │  ├─ Only used classes         │   │
│  │  └─ Optimized heap           │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │ High Throughput               │   │
│  │  ├─ Optimized bytecode       │   │
│  │  └─ Efficient execution      │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## 2. GraalVM Native Images

### Native Image Compilation
```
┌─────────────────────────────────────────────────────────────┐
│              GraalVM Native Image Process                   │
└─────────────────────────────────────────────────────────────┘

Java Source Code
    │
    │ javac
    ▼
┌─────────────────────────────────────┐
│  Java Bytecode (.class files)       │
└─────────────────────────────────────┘
    │
    │ native-image
    ▼
┌─────────────────────────────────────┐
│  Static Analysis                    │
│  ┌──────────────────────────────┐  │
│  │  ├─ Reachability Analysis    │  │
│  │  ├─ Find entry points         │  │
│  │  └─ Build call graph           │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │
    ▼
┌─────────────────────────────────────┐
│  Ahead-of-Time (AOT) Compilation    │
│  ┌──────────────────────────────┐  │
│  │  ├─ Compile to machine code   │  │
│  │  ├─ Optimize                  │  │
    │  └─ Link native libraries     │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │
    ▼
┌─────────────────────────────────────┐
│  Native Executable                  │
│  ┌──────────────────────────────┐  │
│  │  ├─ Standalone binary          │  │
│  │  ├─ No JVM needed              │  │
│  │  ├─ Fast startup (~10ms)       │  │
│  │  └─ Low memory (~50MB)        │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Native Image vs JVM
```
┌─────────────────────────────────────────────────────────────┐
│              Native Image vs JVM                             │
└─────────────────────────────────────────────────────────────┘

JVM Execution
┌─────────────────────────────────────┐
│  Application JAR                    │
│        │                            │
│        ▼                            │
│  JVM (java -jar app.jar)            │
│  ┌──────────────────────────────┐  │
│  │  ├─ Class loading            │  │  ← Runtime
│  │  ├─ JIT compilation           │  │  ← Runtime
│  │  ├─ Garbage collection       │  │  ← Runtime
│  │  └─ Reflection               │  │  ← Runtime
│  └──────────────────────────────┘  │
│        │                            │
│        ▼                            │
│  Startup: 1-3 seconds               │
│  Memory: 200-500 MB                  │
│  Peak Performance: High              │
└─────────────────────────────────────┘

Native Image Execution
┌─────────────────────────────────────┐
│  Native Executable                  │
│        │                            │
│        ▼                            │
│  Substrate VM                       │
│  ┌──────────────────────────────┐  │
│  │  ├─ Pre-compiled code         │  │  ← Build-time
│  │  ├─ No class loading          │  │  ← Build-time
│  │  ├─ No JIT needed             │  │  ← Build-time
│  │  └─ Minimal reflection         │  │  ← Build-time
│  └──────────────────────────────┘  │
│        │                            │
│        ▼                            │
│  Startup: 10-50 ms                  │
│  Memory: 50-100 MB                   │
│  Peak Performance: Very High         │
└─────────────────────────────────────┘
```

### Reflection Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Reflection in Native Images                     │
└─────────────────────────────────────────────────────────────┘

Problem:
┌─────────────────────────────────────┐
│  // This won't work in native image │
│  Class<?> clazz =                   │
│    Class.forName("com.example.User");│
│  Object obj = clazz.newInstance();  │
└─────────────────────────────────────┘

Solution: Reflection Configuration
┌─────────────────────────────────────┐
│  META-INF/native-image/            │
│    reflect-config.json              │
│  ┌──────────────────────────────┐  │
│  │ [{                            │  │
│  │   "name": "com.example.User", │  │
│  │   "methods": [                │  │
│  │     {"name": "<init>"}        │  │
│  │   ],                          │  │
│  │   "fields": [                 │  │
│  │     {"name": "id"}            │  │
│  │   ]                           │  │
│  │ }]                            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘

Quarkus Auto-Generation:
┌─────────────────────────────────────┐
│  Build-time analysis automatically   │
│  generates reflect-config.json       │
│  for:                                │
│  - @Entity classes                   │
│  - JSON serialization classes        │
│  - CDI beans                         │
│  - REST endpoints                    │
└─────────────────────────────────────┘
```

---

## 3. Quarkus CDI

### Build-Time CDI
```
┌─────────────────────────────────────────────────────────────┐
│              Quarkus CDI (Build-Time)                       │
└─────────────────────────────────────────────────────────────┘

Build Phase
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Bean Discovery                │  │
│  │  ├─ Scan @ApplicationScoped │  │
│  │  ├─ Scan @RequestScoped      │  │
│  │  └─ Scan @Dependent          │  │
│  └──────────────────────────────┘  │
│        │                            │
│        ▼                            │
│  ┌──────────────────────────────┐  │
│  │ Dependency Graph Building     │  │
│  │  ├─ Resolve @Inject           │  │
│  │  ├─ Find @Produces            │  │
│  │  └─ Validate cycles           │  │
│  └──────────────────────────────┘  │
│        │                            │
│        ▼                            │
│  ┌──────────────────────────────┐  │
│  │ Proxy Generation              │  │
│  │  ├─ Generate bytecode         │  │
│  │  ├─ Create proxies            │  │
│  │  └─ Optimize                   │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Produces metadata
    ▼
Runtime Phase
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Fast Bean Resolution          │  │
│  │  ├─ Pre-built graph           │  │
│  │  ├─ No scanning              │  │
│  │  └─ Direct lookup             │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │ Instant Injection             │  │
│  │  ├─ Proxies ready             │  │
│  │  └─ No runtime generation     │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

### CDI Bean Scopes
```
┌─────────────────────────────────────────────────────────────┐
│              Quarkus CDI Scopes                              │
└─────────────────────────────────────────────────────────────┘

@ApplicationScoped (Singleton)
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Created once at startup       │  │
│  │ Shared across all requests    │  │
│  │ Thread-safe required          │  │
│  │                               │  │
│  │ @ApplicationScoped            │  │
│  │ public class ConfigService {  │  │
│  │     // Singleton              │  │
│  │ }                             │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘

@RequestScoped
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Created per HTTP request       │  │
│  │ Destroyed after request       │  │
│  │ Not thread-safe needed        │  │
│  │                               │  │
│  │ @RequestScoped                │  │
│  │ public class RequestContext {  │  │
│  │     // Per request            │  │
│  │ }                             │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘

@Singleton (Alternative)
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Jakarta @Singleton            │  │
│  │ Similar to @ApplicationScoped │  │
│  │ Eager initialization          │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

---

## 4. Quarkus REST (JAX-RS)

### REST Endpoint Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Quarkus REST Endpoint                          │
└─────────────────────────────────────────────────────────────┘

@Path("/users")
@ApplicationScoped
public class UserResource {
    
    @Inject
    UserService userService;
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") Long id) {
        return userService.findById(id);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(User user) {
        User created = userService.create(user);
        return Response.status(201)
            .entity(created)
            .build();
    }
}
```

### Request Processing Flow
```
┌─────────────────────────────────────────────────────────────┐
│              REST Request Flow                              │
└─────────────────────────────────────────────────────────────┘

HTTP Request
    │
    │ GET /users/123
    ▼
┌─────────────────────────────────────┐
│  Vert.x HTTP Server                 │
│  ┌──────────────────────────────┐  │
│  │ Receives request              │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Routes to
    ▼
┌─────────────────────────────────────┐
│  JAX-RS Router                      │
│  ┌──────────────────────────────┐  │
│  │ Match @Path                  │  │
│  │ Match HTTP method            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Instantiates
    ▼
┌─────────────────────────────────────┐
│  CDI Container                     │
│  ┌──────────────────────────────┐  │
│  │ Create bean instance         │  │
│  │ Inject dependencies          │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Calls
    ▼
┌─────────────────────────────────────┐
│  Resource Method                   │
│  ┌──────────────────────────────┐  │
│  │ getUser(@PathParam("id"))    │  │
│  │ Business logic               │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Returns
    ▼
┌─────────────────────────────────────┐
│  Message Body Writer                │
│  ┌──────────────────────────────┐  │
│  │ Serialize to JSON            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │
    ▼
HTTP Response (JSON)
```

---

## 5. Quarkus Reactive

### Reactive Programming Model
```
┌─────────────────────────────────────────────────────────────┐
│              Quarkus Reactive Stack                        │
└─────────────────────────────────────────────────────────────┘

Application Layer
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Mutiny API                    │  │
│  │  ├─ Uni<T> (0-1 item)        │  │
│  │  └─ Multi<T> (0-N items)     │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Built on
    ▼
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Vert.x                       │  │
│  │  ├─ Event Loop               │  │
│  │  ├─ Non-blocking I/O        │  │
│  │  └─ Reactive Streams         │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │ Uses
    ▼
┌─────────────────────────────────────┐
│  ┌──────────────────────────────┐  │
│  │ Reactive Drivers              │  │
│  │  ├─ Reactive PostgreSQL      │  │
│  │  ├─ Reactive MySQL            │  │
│  │  ├─ Reactive MongoDB          │  │
│  │  └─ Kafka Reactive            │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Mutiny API
```
┌─────────────────────────────────────────────────────────────┐
│              Mutiny Reactive Types                          │
└─────────────────────────────────────────────────────────────┘

Uni<T> (Single Item)
┌─────────────────────────────────────┐
│  Uni<User> user =                    │
│    userService.findById(id);        │
│                                     │
│  user                               │
│    .onItem().transform(u -> u.name) │
│    .onFailure().recover("Unknown")  │
│    .subscribe().with(               │
│      name -> System.out.println(name)│
│    );                                │
└─────────────────────────────────────┘

Multi<T> (Multiple Items)
┌─────────────────────────────────────┐
│  Multi<User> users =                 │
│    userService.findAll();            │
│                                     │
│  users                              │
│    .filter(u -> u.active)           │
│    .map(u -> u.name)                │
│    .collect().asList()              │
│    .subscribe().with(               │
│      names -> process(names)         │
│    );                                │
└─────────────────────────────────────┘

Chaining Operations
┌─────────────────────────────────────┐
│  Uni<String> result =                │
│    fetchUser(id)                     │
│      .onItem().transformToUni(      │
│        user -> fetchProfile(user.id) │
│      )                               │
│      .onItem().transform(           │
│        profile -> profile.data       │
│      )                               │
│      .onFailure().retry().atMost(3);│
└─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Quarkus Features
```
Build-Time Optimization: Bytecode analysis, dead code elimination
Native Images: GraalVM AOT compilation
Fast Startup: 10-50ms vs 1-3s (JVM)
Low Memory: 50-100MB vs 200-500MB (JVM)
```

### GraalVM Native Images
```
AOT Compilation: Machine code at build time
Substrate VM: Minimal runtime
Reflection: Requires configuration
Fast Startup: No JVM overhead
```

### CDI
```
Build-Time DI: Pre-computed dependency graph
Runtime DI: Fast bean resolution
Scopes: @ApplicationScoped, @RequestScoped
```

### Reactive
```
Mutiny: Uni<T>, Multi<T>
Vert.x: Event loop, non-blocking I/O
Reactive Drivers: Database, messaging
```

---

**Next: Part 5 will cover Micronaut in depth.**

