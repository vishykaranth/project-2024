# Distributed Tracing: OpenTelemetry, Jaeger, Zipkin

## Overview

Distributed Tracing is a method of observing requests as they flow through distributed systems. It tracks requests across multiple services, providing visibility into system behavior, performance bottlenecks, and error propagation in microservices architectures.

## What is Distributed Tracing?

Distributed tracing follows a single request (trace) as it travels through multiple services, recording timing and metadata at each step (span).

```
┌─────────────────────────────────────────────────────────┐
│              Distributed Tracing Concept                 │
└─────────────────────────────────────────────────────────┘

User Request
    │
    ▼
┌──────────┐
│  API     │  ← Trace starts here
│ Gateway  │
└────┬─────┘
     │
     ├─► Service A (50ms)
     │   └─► Database Query (30ms)
     │
     ├─► Service B (100ms)
     │   ├─► External API (80ms)
     │   └─► Cache Lookup (20ms)
     │
     └─► Service C (75ms)
         └─► Message Queue (25ms)
```

## Trace and Span

### Trace

A trace represents the entire request journey through the system. It contains all spans related to a single request.

```
┌─────────────────────────────────────────────────────────┐
│              Trace Structure                            │
└─────────────────────────────────────────────────────────┘

Trace ID: abc123
├─ Span 1: API Gateway (root)
│  ├─ Span 2: Service A
│  │  └─ Span 3: Database Query
│  │
│  ├─ Span 4: Service B
│  │  ├─ Span 5: External API
│  │  └─ Span 6: Cache Lookup
│  │
│  └─ Span 7: Service C
│     └─ Span 8: Message Queue
```

### Span

A span represents a single operation within a trace. It contains:
- Operation name
- Start/end timestamps
- Tags (key-value pairs)
- Logs (events)
- References to parent/child spans

```
┌─────────────────────────────────────────────────────────┐
│              Span Structure                            │
└─────────────────────────────────────────────────────────┘

Span:
├─ Trace ID: abc123
├─ Span ID: def456
├─ Parent Span ID: ghi789 (optional)
├─ Operation Name: "processPayment"
├─ Start Time: 2024-01-15T10:30:00.000Z
├─ End Time: 2024-01-15T10:30:00.150Z
├─ Duration: 150ms
├─ Tags:
│  ├─ service.name: "payment-service"
│  ├─ http.method: "POST"
│  ├─ http.status_code: 200
│  └─ error: false
└─ Logs:
   ├─ Event: "Payment processed"
   └─ Event: "Database updated"
```

## OpenTelemetry

### Overview

OpenTelemetry is an open-source observability framework that provides a unified set of APIs, SDKs, and tools for instrumenting, generating, collecting, and exporting telemetry data (traces, metrics, logs).

### OpenTelemetry Architecture

```
┌─────────────────────────────────────────────────────────┐
│              OpenTelemetry Architecture                 │
└─────────────────────────────────────────────────────────┘

Application
    │
    │ (Instrumentation)
    │
    ▼
┌─────────────────┐
│ OpenTelemetry   │
│ SDK             │
└────────┬────────┘
         │
         │ (Telemetry Data)
         │
         ▼
┌─────────────────┐
│ OpenTelemetry   │
│ Collector       │
└────────┬────────┘
         │
         │ (Processed Data)
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│ Jaeger │ │Zipkin  │
└────────┘ └────────┘
```

### OpenTelemetry Components

**1. API**
- Defines interfaces
- Language-agnostic
- Vendor-neutral

**2. SDK**
- Language-specific implementation
- Provides instrumentation
- Manages spans and traces

**3. Collector**
- Receives telemetry data
- Processes and transforms
- Exports to backends

**4. Instrumentation Libraries**
- Auto-instrumentation
- Framework-specific
- Zero-code changes

### OpenTelemetry Instrumentation

#### Manual Instrumentation (Java)

```java
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;

public class PaymentService {
    private static final Tracer tracer = 
        OpenTelemetry.getGlobalTracerProvider()
            .get("payment-service");
    
    public void processPayment(PaymentRequest request) {
        Span span = tracer.spanBuilder("processPayment")
            .setAttribute("payment.amount", request.getAmount())
            .setAttribute("payment.currency", request.getCurrency())
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Business logic
            validatePayment(request);
            chargeCard(request);
            updateOrder(request);
            
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

#### Auto-Instrumentation (Java)

```bash
# Add Java agent
java -javaagent:opentelemetry-javaagent.jar \
     -Dotel.service.name=payment-service \
     -Dotel.exporter.otlp.endpoint=http://collector:4317 \
     -jar app.jar
```

**Benefits:**
- Zero code changes
- Automatic instrumentation
- Framework support (Spring, HTTP, JDBC)

### OpenTelemetry Collector

**Configuration:**

```yaml
# otel-collector-config.yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 1s
    send_batch_size: 1024
  
  resource:
    attributes:
      - key: environment
        value: production
        action: upsert

exporters:
  jaeger:
    endpoint: jaeger:14250
    tls:
      insecure: true
  
  zipkin:
    endpoint: http://zipkin:9411/api/v2/spans

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch, resource]
      exporters: [jaeger, zipkin]
```

## Jaeger

### Overview

Jaeger is an open-source distributed tracing system originally built by Uber. It helps monitor and troubleshoot microservices-based distributed systems.

### Jaeger Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Jaeger Architecture                        │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Traces via OpenTelemetry/Client Libraries)
    │
    ▼
┌──────────────┐
│  Jaeger     │
│  Agent      │  ← Runs on each host
│  (UDP)      │
└──────┬──────┘
       │
       │ (gRPC)
       │
       ▼
┌──────────────┐
│  Jaeger     │
│  Collector  │  ← Receives and processes traces
└──────┬──────┘
       │
       │ (Writes)
       │
       ▼
┌──────────────┐
│  Storage     │  ← Elasticsearch, Cassandra, etc.
│  Backend     │
└──────┬──────┘
       │
       │ (Reads)
       │
       ▼
┌──────────────┐
│  Jaeger      │
│  Query       │  ← Query service
└──────┬───────┘
       │
       │ (UI)
       │
       ▼
┌──────────────┐
│  Jaeger UI   │  ← Web interface
└──────────────┘
```

### Jaeger Components

**1. Jaeger Agent**
- Runs on each host
- Receives spans via UDP
- Batches and forwards to collector
- Lightweight daemon

**2. Jaeger Collector**
- Receives traces from agents
- Validates and processes
- Stores in backend
- Handles sampling

**3. Jaeger Query**
- Retrieves traces from storage
- Provides query API
- Powers UI

**4. Jaeger UI**
- Web interface
- Search and visualize traces
- Analyze performance
- Debug issues

### Jaeger Sampling

```
┌─────────────────────────────────────────────────────────┐
│              Sampling Strategies                       │
└─────────────────────────────────────────────────────────┘

├─ Constant Sampling
│  └─ Sample all traces (100%) or none (0%)
│
├─ Probabilistic Sampling
│  └─ Sample X% of traces randomly
│
├─ Rate Limiting Sampling
│  └─ Sample up to N traces per second
│
└─ Adaptive Sampling
   └─ Adjusts based on traffic patterns
```

**Configuration:**

```yaml
# Jaeger sampling configuration
sampling:
  default_strategy:
    type: probabilistic
    param: 0.1  # Sample 10% of traces
```

### Jaeger UI Features

**1. Search**
- Find traces by service
- Filter by tags
- Time range selection
- Operation filtering

**2. Trace View**
- Timeline visualization
- Span details
- Service dependencies
- Error highlighting

**3. Comparison**
- Compare traces
- Performance analysis
- Identify regressions

## Zipkin

### Overview

Zipkin is a distributed tracing system that helps gather timing data needed to troubleshoot latency problems in microservice architectures.

### Zipkin Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Zipkin Architecture                        │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (HTTP/JSON, Kafka, Scribe)
    │
    ▼
┌──────────────┐
│  Zipkin      │
│  Collector   │  ← Receives spans
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
│  Zipkin      │
│  Query API   │
└──────┬───────┘
       │
       │ (UI)
       │
       ▼
┌──────────────┐
│  Zipkin UI   │
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

### Zipkin Integration

**Spring Boot Example:**

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
            .additionalInterceptors(
                new TracingClientHttpRequestInterceptor()
            )
            .build();
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
      probability: 1.0  # 100% sampling for development
```

## Trace Visualization

### Timeline View

```
┌─────────────────────────────────────────────────────────┐
│              Trace Timeline View                        │
└─────────────────────────────────────────────────────────┘

Service Timeline:
─────────────────────────────────────────────────────────
API Gateway     [████████████████████████] 200ms
  ├─ Auth       [████████] 50ms
  ├─ Payment    [████████████████] 100ms
  │   ├─ DB     [████] 20ms
  │   └─ Cache  [██] 10ms
  └─ Notification [████] 30ms
─────────────────────────────────────────────────────────
Total Duration: 200ms
```

### Service Map

```
┌─────────────────────────────────────────────────────────┐
│              Service Dependency Map                      │
└─────────────────────────────────────────────────────────┘

        ┌──────────┐
        │   API    │
        │ Gateway  │
        └────┬─────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
┌──────┐ ┌──────┐ ┌──────┐
│ Auth │ │Payment│ │Notify│
└──┬───┘ └──┬───┘ └──────┘
   │        │
   │    ┌───┴───┐
   │    │       │
   ▼    ▼       ▼
┌────┐ ┌────┐ ┌────┐
│ DB │ │Cache│ │Queue│
└────┘ └────┘ └────┘
```

## Best Practices

### 1. Sampling
- Don't trace 100% in production
- Use adaptive sampling
- Sample based on error rate
- Balance cost vs. visibility

### 2. Span Naming
- Use consistent naming
- Include operation type
- Example: "http.get", "db.query", "cache.get"

### 3. Tags
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

Distributed Tracing:
- **Purpose**: Track requests across distributed systems
- **Concepts**: Traces (request journey), Spans (individual operations)
- **OpenTelemetry**: Unified observability framework
- **Tools**: Jaeger, Zipkin (tracing backends)

**Key Components:**
- Trace: Complete request journey
- Span: Individual operation
- Tags: Key-value metadata
- Logs: Events within spans

**Tools:**
- **OpenTelemetry**: Instrumentation framework
- **Jaeger**: Distributed tracing system
- **Zipkin**: Distributed tracing system

**Best Practices:**
- Implement sampling
- Use consistent naming
- Add relevant tags
- Handle errors properly
- Monitor performance overhead
