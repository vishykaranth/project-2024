# Part 9: Microservices Patterns - Quick Revision

## Service Discovery

- **Client-Side**: Client queries service registry, routes directly to service
- **Server-Side**: Load balancer queries registry, routes to service
- **Registry**: Eureka, Consul, etcd, Kubernetes service discovery
- **Health Checks**: Monitor service health, remove unhealthy services

## API Gateway

- **Single Entry Point**: All client requests go through gateway
- **Functions**: Routing, authentication, rate limiting, protocol translation
- **Benefits**: Centralized cross-cutting concerns, simplified client
- **Examples**: Kong, AWS API Gateway, Zuul, Spring Cloud Gateway

## Circuit Breaker

- **Purpose**: Prevent cascading failures, fast failure
- **States**: Closed (normal), Open (failing), Half-Open (testing)
- **Implementation**: Resilience4j, Hystrix (deprecated)
- **Benefits**: Fail fast, automatic recovery, prevent resource exhaustion

## Service Communication

- **Synchronous**: REST, gRPC; simple, but coupling
- **Asynchronous**: Message queues, event streaming; decoupled, but complex
- **Service Mesh**: Istio, Linkerd; handle service-to-service communication

## Distributed Tracing

- **Purpose**: Track requests across services, identify bottlenecks
- **Tools**: Zipkin, Jaeger, AWS X-Ray
- **Correlation**: Trace IDs, span IDs, parent-child relationships

## Key Principles

- **Independent Deployment**: Deploy services independently
- **Technology Diversity**: Use different tech stacks per service
- **Fault Isolation**: Failure in one service doesn't cascade
- **Team Autonomy**: Each team owns their service
