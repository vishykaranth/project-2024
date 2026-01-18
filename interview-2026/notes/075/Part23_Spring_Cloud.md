# Part 23: Spring Cloud - Quick Revision

## Service Discovery

- **Eureka Server**: Service registry, health monitoring
- **Eureka Client**: Register and discover services
- **Service Registration**: Services register on startup
- **Health Checks**: Remove unhealthy services from registry

## API Gateway

- **Spring Cloud Gateway**: Reactive, non-blocking
- **Routing**: Route requests to services based on path, headers
- **Filtering**: Add/remove headers, modify requests/responses
- **Load Balancing**: Client-side load balancing

## Circuit Breaker

- **Resilience4j**: Modern circuit breaker library
- **States**: Closed (normal), Open (failing), Half-Open (testing)
- **Fallback**: Provide fallback response when circuit is open
- **Configuration**: Failure threshold, timeout, retry

## Configuration Management

- **Spring Cloud Config Server**: Centralized configuration
- **Git Backend**: Store configuration in Git repository
- **Refresh**: @RefreshScope to reload configuration
- **Encryption**: Encrypt sensitive configuration values

## Distributed Tracing

- **Spring Cloud Sleuth**: Add trace IDs to requests
- **Zipkin Integration**: Send traces to Zipkin
- **Trace Correlation**: Track requests across services
