# Container Orchestration Patterns - Part 3: Adapter Pattern

## 🔌 Adapter Pattern: Service Normalization

---

## 1. Adapter Pattern Overview

### Basic Adapter Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Adapter Pattern Architecture                    │
└─────────────────────────────────────────────────────────────┘

    Main Application/Service
    │
    │ Expects standard interface
    │ (e.g., REST API, standard format)
    │
    ▼
┌─────────────────────────────────────┐
│         Application Pod             │
│                                     │
│  ┌──────────────┐  ┌─────────────┐ │
│  │              │  │             │ │
│  │  Main App    │  │  Adapter    │ │
│  │  Container   │  │  Container  │ │
│  │              │  │             │ │
│  │  Standard    │  │  Transforms │ │
│  │  Interface   │  │  Protocol/  │ │
│  │              │  │  Format     │ │
│  └──────┬───────┘  └──────┬──────┘ │
│         │                 │        │
│         │  Standard       │        │
│         │  Format         │        │
│         └─────────►────────┘        │
│                  │                  │
│         ┌────────▼────────┐        │
│         │  Shared Network │        │
│         │  / Volume       │        │
│         └──────────────────┘        │
└─────────────────────────────────────┘
              │
              │ Adapted format
              ▼
    ┌─────────────────────┐
    │  Legacy/External     │
    │  Service            │
    │  (Different format)│
    └─────────────────────┘

Key Characteristics:
- Adapter translates between incompatible interfaces
- Main app uses standard interface
- Adapter handles transformation
- Enables integration with legacy systems
- Protocol/format normalization
```

### Adapter Pattern Purpose
```
┌─────────────────────────────────────────────────────────────┐
│              Adapter Pattern Purpose                         │
└─────────────────────────────────────────────────────────────┘

Problem:
    Modern App          Legacy System
    │                   │
    │ REST API           │ SOAP API
    │ JSON format        │ XML format
    │ HTTP/2             │ HTTP/1.0
    │                    │
    │  Incompatible!      │
    └────────────────────┘

Solution:
    Modern App          Adapter          Legacy System
    │                   │                │
    │ REST API ────────►│ Transform ────►│ SOAP API
    │ JSON format       │ JSON→XML       │ XML format
    │ HTTP/2            │ HTTP/2→HTTP/1.0│ HTTP/1.0
    │                   │                │
    │  Compatible!      │                │
    └───────────────────┴────────────────┴┘

Adapter Functions:
- Protocol translation
- Data format conversion
- Interface normalization
- API versioning
- Legacy system integration
```

---

## 2. Protocol Adapter

### Protocol Translation
```
┌─────────────────────────────────────────────────────────────┐
│              Protocol Adapter                                │
└─────────────────────────────────────────────────────────────┘

Modern Application
    │
    │ gRPC Request
    │ (Protocol Buffers)
    │
    ▼
┌──────────────────────────────────┐
│  Protocol Adapter                │
│                                  │
│  ┌──────────┐  ┌──────────┐     │
│  │  gRPC    │  │  HTTP    │     │
│  │  Handler │  │  Client  │     │
│  └────┬─────┘  └────┬─────┘     │
│       │             │            │
│  ┌────▼─────────────▼─────┐    │
│  │  Protocol Translator   │    │
│  │  - gRPC → HTTP         │    │
│  │  - Protobuf → JSON    │    │
│  │  - HTTP/2 → HTTP/1.1  │    │
│  └────┬──────────────────┘    │
└───────┼──────────────────────────┘
        │
        │ HTTP Request (JSON)
        ▼
┌──────────────────────────────────┐
│  Legacy Service                  │
│                                  │
│  - HTTP/1.1                      │
│  - JSON format                    │
│  - REST API                       │
└──────────────────────────────────┘

Supported Translations:
- gRPC ↔ HTTP/REST
- HTTP/2 ↔ HTTP/1.1
- WebSocket ↔ HTTP
- GraphQL ↔ REST
- SOAP ↔ REST
```

### Protocol Adapter Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Protocol Adapter Flow                          │
└─────────────────────────────────────────────────────────────┘

Step 1: Modern App Request
    ┌──────────┐
    │ Modern   │
    │ App      │
    │          │
    │ gRPC     │
    │ (Protobuf)│
    └────┬─────┘
         │
         │ gRPC call
         ▼
Step 2: Adapter Receives
    ┌──────────┐
    │ Protocol │
    │ Adapter  │
    │          │
    │ Receives │
    │ gRPC     │
    └────┬─────┘
         │
         │ Translates
         ▼
Step 3: Transform Protocol
    ┌──────────┐
    │ Protocol │
    │ Adapter  │
    │          │
    │ - gRPC → │
    │   HTTP   │
    │ - Protobuf│
    │   → JSON │
    └────┬─────┘
         │
         │ HTTP Request (JSON)
         ▼
Step 4: Legacy Service
    ┌──────────┐
    │ Legacy   │
    │ Service  │
    │          │
    │ Processes│
    │ HTTP     │
    └────┬─────┘
         │
         │ HTTP Response (JSON)
         ▼
Step 5: Adapter Transforms Back
    ┌──────────┐
    │ Protocol │
    │ Adapter  │
    │          │
    │ - HTTP → │
    │   gRPC   │
    │ - JSON → │
    │   Protobuf│
    └────┬─────┘
         │
         │ gRPC Response
         ▼
    ┌──────────┐
    │ Modern   │
    │ App      │
    └──────────┘
```

---

## 3. Data Format Adapter

### Format Conversion
```
┌─────────────────────────────────────────────────────────────┐
│              Data Format Adapter                             │
└─────────────────────────────────────────────────────────────┘

Modern Application
    │
    │ JSON Data
    │ {
    │   "userId": 123,
    │   "name": "John"
    │ }
    │
    ▼
┌──────────────────────────────────┐
│  Format Adapter                  │
│                                  │
│  ┌──────────┐  ┌──────────┐     │
│  │  JSON    │  │  XML     │     │
│  │  Parser  │  │  Builder │     │
│  └────┬─────┘  └────┬─────┘     │
│       │             │            │
│  ┌────▼─────────────▼─────┐    │
│  │  Format Converter       │    │
│  │  - JSON → XML          │    │
│  │  - Field mapping       │    │
│  │  - Type conversion     │    │
│  │  - Schema transform    │    │
│  └────┬──────────────────┘    │
└───────┼──────────────────────────┘
        │
        │ XML Data
        │ <user>
        │   <id>123</id>
        │   <name>John</name>
        │ </user>
        ▼
┌──────────────────────────────────┐
│  Legacy Service                  │
│                                  │
│  - XML format                    │
│  - SOAP protocol                 │
│  - Old schema                    │
└──────────────────────────────────┘

Supported Conversions:
- JSON ↔ XML
- JSON ↔ CSV
- Protobuf ↔ JSON
- Avro ↔ JSON
- Custom formats
```

### Format Adapter Example
```
┌─────────────────────────────────────────────────────────────┐
│              JSON to XML Adapter                             │
└─────────────────────────────────────────────────────────────┘

Input (JSON):
{
  "user": {
    "id": 123,
    "name": "John Doe",
    "email": "john@example.com",
    "active": true
  }
}

Adapter Processing:
1. Parse JSON structure
2. Map fields to XML elements
3. Convert types (boolean → string)
4. Apply schema rules
5. Generate XML

Output (XML):
<user>
  <id>123</id>
  <name>John Doe</name>
  <email>john@example.com</email>
  <active>true</active>
</user>

Field Mappings:
- JSON nested objects → XML nested elements
- JSON arrays → XML repeated elements
- JSON primitives → XML text content
- Type conversions as needed
```

---

## 4. API Version Adapter

### API Versioning Adapter
```
┌─────────────────────────────────────────────────────────────┐
│              API Version Adapter                             │
└─────────────────────────────────────────────────────────────┘

Client (v2 API)
    │
    │ POST /api/v2/users
    │ {
    │   "firstName": "John",
    │   "lastName": "Doe"
    │ }
    │
    ▼
┌──────────────────────────────────┐
│  API Version Adapter             │
│                                  │
│  ┌──────────┐  ┌──────────┐     │
│  │  v2      │  │  v1      │     │
│  │  Parser  │  │  Builder │     │
│  └────┬─────┘  └────┬─────┘     │
│       │             │            │
│  ┌────▼─────────────▼─────┐    │
│  │  Version Transformer   │    │
│  │  - v2 → v1 mapping    │    │
│  │  - Field renaming      │    │
│  │  - Structure change    │    │
│  └────┬──────────────────┘    │
└───────┼──────────────────────────┘
        │
        │ v1 API format
        │ POST /api/v1/users
        │ {
        │   "first_name": "John",
        │   "last_name": "Doe"
        │ }
        ▼
┌──────────────────────────────────┐
│  Legacy Service (v1 API)         │
│                                  │
│  - Old API version               │
│  - Different field names         │
│  - Different structure           │
└──────────────────────────────────┘

Version Adaptations:
- Field name mapping (camelCase ↔ snake_case)
- Structure transformation
- Default value handling
- Deprecated field removal
- New field addition
```

### API Version Mapping
```
┌─────────────────────────────────────────────────────────────┐
│              API Version Mapping                             │
└─────────────────────────────────────────────────────────────┘

v2 Request Format:
{
  "userId": 123,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "metadata": {
    "createdAt": "2024-01-01",
    "tags": ["premium", "active"]
  }
}

Adapter Transformation:
1. Rename fields:
   - userId → user_id
   - firstName → first_name
   - lastName → last_name

2. Flatten structure:
   - metadata.createdAt → created_at
   - metadata.tags → tags (array)

3. Add defaults:
   - status: "active" (if missing)

v1 Request Format (to Legacy):
{
  "user_id": 123,
  "first_name": "John",
  "last_name": "Doe",
  "email": "john@example.com",
  "created_at": "2024-01-01",
  "tags": ["premium", "active"],
  "status": "active"
}
```

---

## 5. Legacy System Adapter

### Legacy Integration
```
┌─────────────────────────────────────────────────────────────┐
│              Legacy System Adapter                          │
└─────────────────────────────────────────────────────────────┘

Modern Microservice
    │
    │ REST API call
    │ (Standard format)
    │
    ▼
┌──────────────────────────────────┐
│  Legacy System Adapter          │
│                                  │
│  ┌──────────┐  ┌──────────┐     │
│  │  Modern  │  │  Legacy  │     │
│  │  Interface│  │  Interface│    │
│  └────┬─────┘  └────┬─────┘     │
│       │             │            │
│  ┌────▼─────────────▼─────┐    │
│  │  Legacy Adapter        │    │
│  │  - Protocol convert   │    │
│  │  - Format convert     │    │
│  │  - Error handling     │    │
│  │  - Retry logic        │    │
│  │  - Timeout handling   │    │
│  └────┬──────────────────┘    │
└───────┼──────────────────────────┘
        │
        │ Legacy protocol/format
        ▼
┌──────────────────────────────────┐
│  Legacy System                  │
│                                  │
│  - Mainframe                    │
│  - SOAP service                 │
│  - Old database                 │
│  - Proprietary protocol         │
└──────────────────────────────────┘

Adapter Handles:
- Protocol translation
- Data format conversion
- Error code mapping
- Authentication translation
- Session management
- Transaction handling
```

### Legacy System Integration Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Legacy Integration Flow                         │
└─────────────────────────────────────────────────────────────┘

Step 1: Modern Service Request
    ┌──────────┐
    │ Modern   │
    │ Service  │
    │          │
    │ REST API │
    │ JSON     │
    └────┬─────┘
         │
         │ HTTP POST /api/users
         ▼
Step 2: Adapter Receives
    ┌──────────┐
    │ Legacy   │
    │ Adapter  │
    │          │
    │ Validates│
    │ request  │
    └────┬─────┘
         │
         │ Transforms
         ▼
Step 3: Protocol Conversion
    ┌──────────┐
    │ Legacy   │
    │ Adapter  │
    │          │
    │ - REST → │
    │   SOAP   │
    │ - JSON → │
    │   XML    │
    │ - HTTP → │
    │   Custom │
    └────┬─────┘
         │
         │ SOAP Request (XML)
         ▼
Step 4: Legacy System
    ┌──────────┐
    │ Legacy   │
    │ System   │
    │          │
    │ Processes│
    │ SOAP     │
    └────┬─────┘
         │
         │ SOAP Response (XML)
         ▼
Step 5: Adapter Transforms
    ┌──────────┐
    │ Legacy   │
    │ Adapter  │
    │          │
    │ - SOAP → │
    │   REST   │
    │ - XML →  │
    │   JSON   │
    │ - Error │
    │   mapping│
    └────┬─────┘
         │
         │ REST Response (JSON)
         ▼
    ┌──────────┐
    │ Modern   │
    │ Service  │
    └──────────┘
```

---

## 6. Database Adapter

### Database Protocol Adapter
```
┌─────────────────────────────────────────────────────────────┐
│              Database Adapter                                │
└─────────────────────────────────────────────────────────────┘

Application
    │
    │ SQL Query
    │ (Standard SQL)
    │
    ▼
┌──────────────────────────────────┐
│  Database Adapter                │
│                                  │
│  ┌──────────┐  ┌──────────┐     │
│  │  SQL     │  │  NoSQL   │     │
│  │  Parser  │  │  Query   │     │
│  └────┬─────┘  └────┬─────┘     │
│       │             │            │
│  ┌────▼─────────────▼─────┐    │
│  │  Query Translator     │    │
│  │  - SQL → NoSQL        │    │
│  │  - Relational → Doc   │    │
│  │  - Join → Aggregation │    │
│  └────┬──────────────────┘    │
└───────┼──────────────────────────┘
        │
        │ NoSQL Query
        ▼
┌──────────────────────────────────┐
│  MongoDB / Cassandra             │
│                                  │
│  - Document store                │
│  - Different query language      │
│  - Different data model          │
└──────────────────────────────────┘

Adapter Functions:
- SQL to NoSQL query translation
- Relational to document mapping
- Join operations to aggregation
- Transaction handling
- Schema adaptation
```

### SQL to NoSQL Translation
```
┌─────────────────────────────────────────────────────────────┐
│              SQL to MongoDB Adapter                          │
└─────────────────────────────────────────────────────────────┘

SQL Query:
SELECT name, email 
FROM users 
WHERE age > 25 
ORDER BY name 
LIMIT 10

Adapter Translation:
1. Parse SQL query
2. Map to MongoDB operations
3. Convert WHERE clause
4. Convert ORDER BY
5. Convert LIMIT

MongoDB Query:
db.users.find(
  { age: { $gt: 25 } },
  { name: 1, email: 1 }
).sort({ name: 1 }).limit(10)

Mapping:
- SELECT → find() projection
- FROM → collection name
- WHERE → find() filter
- ORDER BY → sort()
- LIMIT → limit()
```

---

## 7. Kubernetes Implementation

### Adapter Pod YAML
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: app-with-adapter
spec:
  containers:
  # Main Application Container
  - name: app
    image: myapp:latest
    ports:
    - containerPort: 8080
    env:
    - name: BACKEND_URL
      value: "http://localhost:9090"
    # App uses standard REST API
  
  # Adapter Container
  - name: adapter
    image: adapter:latest
    ports:
    - containerPort: 9090
      name: adapter
    env:
    - name: LEGACY_SERVICE_URL
      value: "http://legacy-service:8080"
    - name: ADAPTER_MODE
      value: "REST_TO_SOAP"
    # Adapter translates REST to SOAP
```

### Adapter Configuration
```yaml
# Adapter Configuration
adapter:
  mode: REST_TO_SOAP
  mappings:
    # Protocol mapping
    protocol:
      input: REST
      output: SOAP
    
    # Format mapping
    format:
      input: JSON
      output: XML
    
    # Field mappings
    fields:
      - source: userId
        target: user_id
        type: integer
      - source: firstName
        target: first_name
        type: string
      - source: lastName
        target: last_name
        type: string
    
    # Endpoint mapping
    endpoints:
      - source: /api/v1/users
        target: /soap/UserService
        method: POST
```

---

## 8. Real-World Examples

### Example 1: REST to SOAP Adapter
```
┌─────────────────────────────────────────────────────────────┐
│              REST to SOAP Adapter                           │
└─────────────────────────────────────────────────────────────┘

Modern Service
    │
    │ POST /api/users
    │ {
    │   "name": "John",
    │   "email": "john@example.com"
    │ }
    ▼
┌──────────────────┐
│  REST→SOAP      │
│  Adapter         │
│                  │
│  Transforms:     │
│  - REST → SOAP   │
│  - JSON → XML    │
│  - HTTP → SOAP   │
└────────┬─────────┘
         │
         │ SOAP Request
         ▼
┌──────────────────┐
│  Legacy SOAP     │
│  Service         │
│                  │
│  <soap:Envelope> │
│    <soap:Body>   │
│      <user>      │
│        <name>    │
│        <email>   │
│      </user>     │
│    </soap:Body>  │
│  </soap:Envelope>│
└──────────────────┘
```

### Example 2: GraphQL to REST Adapter
```
┌─────────────────────────────────────────────────────────────┐
│              GraphQL to REST Adapter                        │
└─────────────────────────────────────────────────────────────┘

GraphQL Client
    │
    │ GraphQL Query
    │ {
    │   user(id: 123) {
    │     name
    │     email
    │   }
    │ }
    ▼
┌──────────────────┐
│  GraphQL→REST    │
│  Adapter         │
│                  │
│  Transforms:     │
│  - GraphQL → REST│
│  - Query → GET   │
│  - Mutation → POST│
└────────┬─────────┘
         │
         │ REST Request
         │ GET /api/users/123
         ▼
┌──────────────────┐
│  REST API        │
│  Service         │
│                  │
│  Returns JSON    │
└──────────────────┘
```

---

## 9. Best Practices

### Adapter Pattern Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

1. Single Responsibility
   - One adapter per transformation
   - Clear transformation rules
   - Focused functionality

2. Idempotency
   - Same input → same output
   - Reversible transformations
   - Error handling

3. Configuration-Driven
   - Externalize mapping rules
   - Use ConfigMaps/Secrets
   - Support dynamic updates

4. Error Handling
   - Map error codes
   - Provide meaningful errors
   - Log transformation failures

5. Performance
   - Cache transformations
   - Batch operations
   - Efficient parsing

6. Testing
   - Unit test transformations
   - Integration tests
   - Edge case handling

7. Observability
   - Log transformations
   - Metrics for adapters
   - Distributed tracing

8. Versioning
   - Support multiple versions
   - Backward compatibility
   - Migration support
```

---

## 10. Comparison: Sidecar vs Ambassador vs Adapter

### Pattern Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Pattern Comparison                              │
└─────────────────────────────────────────────────────────────┘

Sidecar Pattern:
    Main App ──► Sidecar (Enhances)
    │              │
    │              │ Logging/Monitoring
    └──────────────┘
    
    Purpose: Enhance main app functionality
    Communication: Internal (localhost)
    Use Case: Logging, monitoring, helper services

Ambassador Pattern:
    Main App ──► Ambassador ──► External Service
    │              │
    │              │ Proxy/Routing
    └──────────────┘
    
    Purpose: Proxy external communication
    Communication: External (network)
    Use Case: Database proxy, API gateway, routing

Adapter Pattern:
    Main App ──► Adapter ──► Legacy Service
    │              │
    │              │ Transform
    └──────────────┘
    
    Purpose: Transform incompatible interfaces
    Communication: Protocol/format conversion
    Use Case: Legacy integration, format conversion
```

### When to Use Each Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Pattern Selection Guide                         │
└─────────────────────────────────────────────────────────────┘

Use Sidecar When:
✅ Need to add functionality without code changes
✅ Cross-cutting concerns (logging, monitoring)
✅ Helper services for main app
✅ Lifecycle coupling needed

Use Ambassador When:
✅ Need to proxy external communication
✅ Complex routing requirements
✅ Connection management needed
✅ Load balancing required

Use Adapter When:
✅ Need to integrate incompatible systems
✅ Protocol/format conversion required
✅ Legacy system integration
✅ API versioning needed
```

---

## Key Takeaways

### Adapter Pattern Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Key Takeaways                                   │
└─────────────────────────────────────────────────────────────┘

✅ Use Adapter Pattern When:
   - Integrating incompatible systems
   - Protocol translation needed
   - Format conversion required
   - Legacy system integration
   - API versioning

❌ Avoid Adapter Pattern When:
   - Systems are already compatible
   - Direct integration possible
   - Performance is critical
   - Simple use cases

Common Adapter Use Cases:
1. REST to SOAP conversion
2. JSON to XML transformation
3. Database protocol translation
4. Legacy system integration
5. API versioning
6. Protocol translation (gRPC ↔ HTTP)
```

---

## Complete Pattern Summary

### All Three Patterns Together
```
┌─────────────────────────────────────────────────────────────┐
│              Complete Pattern Overview                       │
└─────────────────────────────────────────────────────────────┘

Sidecar Pattern:
    ┌──────────┐
    │ Main App │
    └────┬─────┘
         │
    ┌────▼─────┐
    │ Sidecar  │ (Enhances)
    │ - Logging│
    │ - Monitor│
    └──────────┘

Ambassador Pattern:
    ┌──────────┐
    │ Main App │
    └────┬─────┘
         │
    ┌────▼─────┐
    │Ambassador│ (Proxies)
    │ - Route  │
    │ - Balance│
    └────┬─────┘
         │
    External Service

Adapter Pattern:
    ┌──────────┐
    │ Main App │
    └────┬─────┘
         │
    ┌────▼─────┐
    │ Adapter  │ (Transforms)
    │ - Convert│
    │ - Translate│
    └────┬─────┘
         │
    Legacy Service
```

---

**This completes all 3 parts of Container Orchestration Patterns!**

**Summary:**
- **Part 1: Sidecar Pattern** - Logging, monitoring, and proxy sidecars
- **Part 2: Ambassador Pattern** - Service proxy and routing
- **Part 3: Adapter Pattern** - Service normalization and transformation

All patterns are essential for building resilient, scalable containerized applications! 🚀

