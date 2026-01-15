# Tracing Tools: Jaeger, Zipkin, AWS X-Ray

## Overview

Tracing Tools provide distributed tracing capabilities to track requests as they flow through distributed systems. They help identify performance bottlenecks, understand service dependencies, and debug issues across microservices architectures.

## Tracing Tools Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Tracing Tools Architecture                 │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Traces via SDK/Agent)
    │
    ▼
┌──────────────┐
│  Tracing     │  ← Collects traces
│  Collector   │
└──────┬───────┘
       │
       │ (Span data)
       │
       ▼
┌──────────────┐
│  Trace       │  ← Stores traces
│  Storage     │
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  Trace       │  ← Queries traces
│  Query       │
└──────┬───────┘
       │
       │ (Visualization)
       │
       ▼
┌──────────────┐
│  Trace UI    │  ← Visualizes traces
└──────────────┘
```

## Jaeger

### Overview

Jaeger is an open-source distributed tracing system originally built by Uber. It helps monitor and troubleshoot microservices-based distributed systems.

### Jaeger Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Jaeger Components                         │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Traces via OpenTelemetry/Client)
    │
    ▼
┌──────────────┐
│  Jaeger      │  ← Receives traces
│  Agent       │
└──────┬───────┘
       │
       │ (gRPC)
       │
       ▼
┌──────────────┐
│  Jaeger      │  ← Processes traces
│  Collector   │
└──────┬───────┘
       │
       │ (Writes)
       │
       ▼
┌──────────────┐
│  Storage     │  ← Elasticsearch, Cassandra, etc.
└──────┬───────┘
       │
       │ (Reads)
       │
       ▼
┌──────────────┐
│  Jaeger      │  ← Queries traces
│  Query       │
└──────┬───────┘
       │
       │ (UI)
       │
       ▼
┌──────────────┐
│  Jaeger UI   │  ← Visualizes traces
└──────────────┘
```

### Jaeger Features

**1. Distributed Tracing:**
- End-to-end request tracking
- Service dependency mapping
- Latency analysis
- Error tracking

**2. Sampling:**
- Constant sampling
- Probabilistic sampling
- Rate limiting
- Adaptive sampling

**3. Storage Backends:**
- Elasticsearch
- Cassandra
- In-memory (development)
- Kafka (streaming)

**4. UI Features:**
- Trace search
- Timeline visualization
- Service map
- Comparison view

### Jaeger Instrumentation

**Java Example:**

```java
import io.jaegertracing.Configuration;
import io.opentracing.Tracer;

Tracer tracer = Configuration.fromEnv("my-service")
    .getTracer();

Span span = tracer.buildSpan("processPayment")
    .withTag("payment.amount", 100.0)
    .start();

try (Scope scope = tracer.scopeManager().activate(span)) {
    // Business logic
    processPayment();
    span.setTag("payment.status", "success");
} catch (Exception e) {
    span.setTag("error", true);
    span.log(Collections.singletonMap("error", e));
    throw e;
} finally {
    span.finish();
}
```

### Jaeger Query

**Search Traces:**
- Service name
- Operation name
- Tags
- Time range
- Duration

**Example Query:**
```
Service: payment-service
Operation: processPayment
Tags: error=true
Time Range: Last 1 hour
Min Duration: 1000ms
```

## Zipkin

### Overview

Zipkin is a distributed tracing system that helps gather timing data needed to troubleshoot latency problems in microservice architectures.

### Zipkin Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Zipkin Components                          │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (HTTP/JSON, Kafka, Scribe)
    │
    ▼
┌──────────────┐
│  Zipkin      │  ← Receives spans
│  Collector   │
└──────┬───────┘
       │
       │ (Stores)
       │
       ▼
┌──────────────┐
│  Storage     │  ← In-memory, MySQL, Elasticsearch, etc.
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  Zipkin      │  ← Queries traces
│  Query API   │
└──────┬───────┘
       │
       │ (UI)
       │
       ▼
┌──────────────┐
│  Zipkin UI   │  ← Visualizes traces
└──────────────┘
```

### Zipkin Data Model

**Span:**

```json
{
  "traceId": "abc123",
  "id": "def456",
  "parentId": "ghi789",
  "name": "processPayment",
  "timestamp": 1705312200000000,
  "duration": 150000,
  "localEndpoint": {
    "serviceName": "payment-service",
    "ipv4": "192.168.1.100"
  },
  "tags": {
    "http.method": "POST",
    "http.status_code": "200"
  },
  "annotations": [
    {
      "timestamp": 1705312200000000,
      "value": "Payment processed"
    }
  ]
}
```

### Zipkin Features

**1. Multiple Transport Options:**
- HTTP/JSON
- Kafka
- Scribe
- gRPC

**2. Storage Options:**
- In-memory (development)
- MySQL
- Elasticsearch
- Cassandra

**3. UI Features:**
- Trace search
- Timeline view
- Dependency graph
- Service map

### Zipkin Integration

**Spring Boot Example:**

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Configuration:**

```yaml
# application.yml
spring:
  zipkin:
    base-url: http://zipkin:9411
    sender:
      type: web
  sleuth:
    sampler:
      probability: 1.0
```

## AWS X-Ray

### Overview

AWS X-Ray is a distributed tracing service that helps developers analyze and debug distributed applications, including those built using microservices architectures.

### AWS X-Ray Architecture

```
┌─────────────────────────────────────────────────────────┐
│              AWS X-Ray Components                      │
└─────────────────────────────────────────────────────────┘

AWS Services/Applications
    │
    │ (X-Ray SDK)
    │
    ▼
┌──────────────┐
│  X-Ray       │  ← Collects traces
│  Daemon      │
└──────┬───────┘
       │
       │ (Trace segments)
       │
       ▼
┌──────────────┐
│  X-Ray       │  ← Stores traces
│  Service     │
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  X-Ray       │  ← Visualizes traces
│  Console     │
└──────────────┘
```

### AWS X-Ray Features

**1. Service Map:**
- Visual service dependencies
- Request flow visualization
- Error rate indicators
- Latency indicators

**2. Trace Analysis:**
- End-to-end traces
- Segment details
- Subsegment breakdown
- Annotations and metadata

**3. AWS Integration:**
- Lambda functions
- API Gateway
- EC2 instances
- ECS containers
- Elastic Beanstalk

**4. Sampling Rules:**
- Configurable sampling
- Rule-based sampling
- Reservoir and rate

### AWS X-Ray Instrumentation

**Java Example:**

```java
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;

AWSXRay.beginSegment("payment-service");

Subsegment subsegment = AWSXRay.beginSubsegment("processPayment");
subsegment.putMetadata("amount", 100.0);
subsegment.putAnnotation("currency", "USD");

try {
    // Business logic
    processPayment();
    subsegment.putAnnotation("status", "success");
} catch (Exception e) {
    subsegment.addException(e);
    throw e;
} finally {
    AWSXRay.endSubsegment();
    AWSXRay.endSegment();
}
```

### AWS X-Ray Sampling

**Sampling Rules:**

```json
{
  "version": 2,
  "rules": [
    {
      "description": "Sample all requests",
      "priority": 10000,
      "fixed_rate": 1.0,
      "reservoir_size": 1,
      "service_name": "*",
      "service_type": "*"
    },
    {
      "description": "Sample 10% of requests",
      "priority": 1000,
      "fixed_rate": 0.1,
      "reservoir_size": 1,
      "service_name": "*",
      "service_type": "*"
    }
  ]
}
```

## Tool Comparison

| Feature | Jaeger | Zipkin | AWS X-Ray |
|---------|--------|--------|-----------|
| **License** | Open Source | Open Source | AWS Service |
| **Storage** | Elasticsearch, Cassandra | Multiple options | AWS managed |
| **AWS Integration** | Manual | Manual | Native |
| **UI** | Excellent | Good | Good |
| **Sampling** | Advanced | Basic | Configurable |
| **Cost** | Free | Free | Pay per trace |
| **Use Case** | Kubernetes, Microservices | Microservices | AWS workloads |

## Best Practices

### 1. Sampling Strategy
- Don't trace 100% in production
- Use adaptive sampling
- Sample based on error rate
- Balance cost vs. visibility

### 2. Span Naming
- Use consistent naming
- Include operation type
- Be descriptive
- Follow conventions

### 3. Tags and Annotations
- Add relevant context
- Include business identifiers
- Don't add sensitive data
- Use standard tag names

### 4. Error Handling
- Always record errors
- Include error details
- Set error status
- Add error tags

### 5. Performance
- Use async exporters
- Batch span exports
- Limit span size
- Monitor overhead

## Summary

Tracing Tools:
- **Purpose**: Track requests across distributed systems
- **Jaeger**: Open-source, flexible storage, excellent UI
- **Zipkin**: Open-source, multiple transports, simple setup
- **AWS X-Ray**: AWS-native, integrated with AWS services

**Key Features:**
- Distributed Tracing: End-to-end request tracking
- Service Maps: Visual dependency graphs
- Latency Analysis: Identify bottlenecks
- Error Tracking: Error propagation
- Sampling: Control trace volume

**Best Practices:**
- Implement sampling
- Use consistent naming
- Add relevant tags
- Handle errors properly
- Monitor performance overhead
