# API Gateways in System Design Interviews

## Overview

API Gateways are a critical component in microservices architectures and are frequently discussed in system design interviews. This guide covers API Gateway patterns, responsibilities, and implementation considerations.

## What is an API Gateway?

```
┌─────────────────────────────────────────────────────────┐
│              API Gateway Architecture                   │
└─────────────────────────────────────────────────────────┘

[Client Applications]
        │
        │ HTTP/HTTPS
        ▼
┌─────────────────┐
│  API Gateway    │  ← Single entry point
│  (Entry Point)  │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
[Service A] [Service B] [Service C]
```

**Definition:** An API Gateway is a single entry point that routes client requests to appropriate backend services, handles cross-cutting concerns, and provides a unified API interface.

## API Gateway Responsibilities

### 1. Request Routing

```
┌─────────────────────────────────────────────────────────┐
│         Request Routing                                 │
└─────────────────────────────────────────────────────────┘

Client Request:
GET /api/v1/users/123

API Gateway:
├─ Parse request
├─ Identify target service
└─ Route to User Service

Routing Rules:
├─ /api/v1/users/*     → User Service
├─ /api/v1/orders/*    → Order Service
└─ /api/v1/payments/*  → Payment Service
```

### 2. Authentication & Authorization

```
┌─────────────────────────────────────────────────────────┐
│         Authentication Flow                             │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ Request with Token
  ▼
API Gateway
  │
  ├─► Validate Token
  ├─► Extract User Info
  ├─► Check Permissions
  │
  └─► Forward to Service (with user context)
```

**Responsibilities:**
- Validate JWT tokens
- Extract user information
- Check authorization
- Inject user context into requests

### 3. Rate Limiting

```
┌─────────────────────────────────────────────────────────┐
│         Rate Limiting Strategy                          │
└─────────────────────────────────────────────────────────┘

Request
  │
  ▼
API Gateway
  │
  ├─► Check Rate Limit (per user/IP)
  │
  ├─► Within Limit? → Forward to Service
  │
  └─► Exceeded? → Return 429 Too Many Requests
```

**Strategies:**
- Token bucket algorithm
- Sliding window
- Fixed window
- Per-user or per-IP limits

### 4. Load Balancing

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing                                 │
└─────────────────────────────────────────────────────────┘

API Gateway
    │
    ├─► Service Instance 1
    ├─► Service Instance 2
    └─► Service Instance 3

Algorithms:
├─ Round Robin
├─ Least Connections
├─ Weighted Round Robin
└─ IP Hash
```

### 5. Request/Response Transformation

```
┌─────────────────────────────────────────────────────────┐
│         Request Transformation                          │
└─────────────────────────────────────────────────────────┘

Client Request:
GET /api/v1/users/123

API Gateway:
├─ Transform to internal format
├─ Add headers
└─ Route to: http://user-service:8080/users/123

Response Transformation:
├─ Aggregate multiple service responses
├─ Format for client
└─ Add metadata
```

### 6. Caching

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                               │
└─────────────────────────────────────────────────────────┘

Request
  │
  ▼
API Gateway
  │
  ├─► Check Cache
  │
  ├─► Cache Hit? → Return cached response
  │
  └─► Cache Miss? → Forward to Service → Cache response
```

### 7. Monitoring & Logging

```
┌─────────────────────────────────────────────────────────┐
│         Monitoring & Logging                            │
└─────────────────────────────────────────────────────────┘

API Gateway Logs:
├─ Request/Response logs
├─ Error logs
├─ Performance metrics
└─ User activity

Metrics:
├─ Request rate
├─ Error rate
├─ Latency (p50, p95, p99)
└─ Service health
```

## API Gateway Patterns

### 1. Simple API Gateway

```
┌─────────────────────────────────────────────────────────┐
│         Simple API Gateway Pattern                     │
└─────────────────────────────────────────────────────────┘

[Client] → [API Gateway] → [Backend Services]
```

**Use Case:** Small to medium applications

### 2. API Gateway with Service Mesh

```
┌─────────────────────────────────────────────────────────┐
│         API Gateway + Service Mesh                     │
└─────────────────────────────────────────────────────────┘

[Client] → [API Gateway] → [Service Mesh] → [Services]
```

**Use Case:** Large microservices architectures

### 3. Multiple API Gateways

```
┌─────────────────────────────────────────────────────────┐
│         Multiple API Gateways                          │
└─────────────────────────────────────────────────────────┘

[Mobile Client] → [Mobile API Gateway]
[Web Client]    → [Web API Gateway]
[Partner API]   → [Partner API Gateway]
```

**Use Case:** Different client types with different requirements

## API Gateway Implementation

### Technology Options

```
┌─────────────────────────────────────────────────────────┐
│         API Gateway Technologies                        │
└─────────────────────────────────────────────────────────┘

Open Source:
├─ Kong
├─ Zuul (Netflix)
├─ Spring Cloud Gateway
└─ Traefik

Cloud Services:
├─ AWS API Gateway
├─ Azure API Management
├─ Google Cloud Endpoints
└─ Apigee

Self-Hosted:
├─ NGINX
├─ HAProxy
└─ Envoy
```

### Example: Spring Cloud Gateway

```yaml
# Gateway Configuration
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## API Gateway Design Considerations

### 1. Single Point of Failure

```
┌─────────────────────────────────────────────────────────┐
│         High Availability                               │
└─────────────────────────────────────────────────────────┘

Solution:
├─ Multiple Gateway Instances
├─ Load Balancer in Front
└─ Health Checks

[Load Balancer]
    │
    ├─► [Gateway Instance 1]
    ├─► [Gateway Instance 2]
    └─► [Gateway Instance 3]
```

### 2. Performance

**Optimization Strategies:**
- Connection pooling
- Response caching
- Request batching
- Compression
- Async processing

### 3. Security

```
┌─────────────────────────────────────────────────────────┐
│         Security Layers                                 │
└─────────────────────────────────────────────────────────┘

API Gateway Security:
├─ SSL/TLS Termination
├─ Authentication (JWT, OAuth)
├─ Authorization (RBAC)
├─ Rate Limiting
├─ DDoS Protection
└─ Input Validation
```

### 4. Service Discovery

```
┌─────────────────────────────────────────────────────────┐
│         Service Discovery Integration                   │
└─────────────────────────────────────────────────────────┘

API Gateway
    │
    ├─► Service Registry (Consul, Eureka)
    │
    └─► Discover Service Instances
        │
        └─► Route to Available Instance
```

## Common Interview Questions

### Q1: Why use an API Gateway?
**Answer:**
- Single entry point simplifies client integration
- Centralized cross-cutting concerns (auth, rate limiting)
- Service abstraction (clients don't need to know service locations)
- Request/response transformation
- Monitoring and analytics

### Q2: How do you handle API Gateway failures?
**Answer:**
- Multiple gateway instances behind load balancer
- Health checks and automatic failover
- Circuit breakers to prevent cascading failures
- Graceful degradation

### Q3: How do you scale an API Gateway?
**Answer:**
- Horizontal scaling (add more instances)
- Load balancing
- Caching to reduce backend load
- Connection pooling
- Async processing

### Q4: API Gateway vs Service Mesh?
**Answer:**
- **API Gateway**: North-south traffic (client to services)
- **Service Mesh**: East-west traffic (service to service)
- Can be used together
- Gateway handles external traffic, mesh handles internal

## API Gateway vs Direct Service Access

```
┌─────────────────────────────────────────────────────────┐
│         Comparison                                      │
└─────────────────────────────────────────────────────────┘

Without API Gateway:
[Client] → [Service 1]
[Client] → [Service 2]
[Client] → [Service 3]
  │
  └─► Problems:
      ├─ Multiple endpoints to manage
      ├─ No centralized auth
      ├─ No rate limiting
      └─ Complex client logic

With API Gateway:
[Client] → [API Gateway] → [Services]
  │
  └─► Benefits:
      ├─ Single endpoint
      ├─ Centralized auth
      ├─ Rate limiting
      └─ Simplified client
```

## Best Practices

### 1. Keep Gateway Thin
- Don't put business logic in gateway
- Use for cross-cutting concerns only
- Delegate to services

### 2. Implement Circuit Breakers
- Prevent cascading failures
- Fail fast when services are down
- Provide fallback responses

### 3. Monitor Everything
- Request/response times
- Error rates
- Service health
- Rate limit violations

### 4. Version APIs
- Support multiple API versions
- Gradual migration
- Deprecation strategy

## Summary

API Gateways are essential for:
- **Single Entry Point**: Unified API interface
- **Cross-Cutting Concerns**: Auth, rate limiting, logging
- **Service Abstraction**: Hide internal architecture
- **Request Routing**: Route to appropriate services
- **Load Balancing**: Distribute traffic
- **Monitoring**: Centralized observability

**Key Takeaways:**
- Use API Gateway for north-south traffic
- Keep gateway focused on cross-cutting concerns
- Implement high availability
- Monitor performance and errors
- Consider service mesh for east-west traffic
