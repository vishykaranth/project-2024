# Observability Traces: Request Flows, Span Correlation, Latency Analysis

## Overview

Traces are the third pillar of observability (Metrics, Logs, Traces). They provide end-to-end visibility into request flows across distributed systems, showing how requests propagate through services, where time is spent, and how services interact.

## Traces in Observability

```
┌─────────────────────────────────────────────────────────┐
│              Traces Role in Observability               │
└─────────────────────────────────────────────────────────┘

Traces provide:
├─ Request Flow Visibility
│  └─ How requests flow through services
│
├─ Service Dependencies
│  └─ Which services call which
│
├─ Latency Analysis
│  └─ Where time is spent
│
└─ Error Propagation
   └─ How errors spread through system
```

## Request Flows

### Simple Request Flow

```
┌─────────────────────────────────────────────────────────┐
│              Simple Request Flow                        │
└─────────────────────────────────────────────────────────┘

User Request
    │
    ▼
┌──────────┐
│  API     │  ← Entry point
│ Gateway  │
└────┬─────┘
     │
     ├─► Service A
     │   └─► Database
     │
     └─► Service B
         └─► External API
```

### Complex Request Flow

```
┌─────────────────────────────────────────────────────────┐
│              Complex Request Flow                      │
└─────────────────────────────────────────────────────────┘

User Request
    │
    ▼
┌──────────┐
│  API     │
│ Gateway  │
└────┬─────┘
     │
     ├─► Auth Service
     │   └─► User DB
     │
     ├─► Product Service
     │   ├─► Product DB
     │   └─► Cache
     │
     ├─► Inventory Service
     │   └─► Inventory DB
     │
     ├─► Payment Service
     │   ├─► Payment DB
     │   └─► Payment Gateway (External)
     │
     └─► Notification Service
         └─► Message Queue
```

## Trace Structure

### Trace Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│              Trace Structure                            │
└─────────────────────────────────────────────────────────┘

Trace (Request Journey)
├─ Span 1: API Gateway (Root)
│  ├─ Span 2: Auth Service
│  │  └─ Span 3: User DB Query
│  │
│  ├─ Span 4: Product Service
│  │  ├─ Span 5: Product DB Query
│  │  └─ Span 6: Cache Lookup
│  │
│  ├─ Span 7: Payment Service
│  │  ├─ Span 8: Payment DB Query
│  │  └─ Span 9: External Payment Gateway
│  │
│  └─ Span 10: Notification Service
│     └─ Span 11: Message Queue Publish
```

### Span Details

```
┌─────────────────────────────────────────────────────────┐
│              Span Information                           │
└─────────────────────────────────────────────────────────┘

Span:
├─ Trace ID: abc123 (same for all spans in trace)
├─ Span ID: def456 (unique per span)
├─ Parent Span ID: ghi789 (links to parent)
├─ Operation Name: "processPayment"
├─ Service Name: "payment-service"
├─ Start Time: 2024-01-15T10:30:00.000Z
├─ End Time: 2024-01-15T10:30:00.150Z
├─ Duration: 150ms
├─ Tags:
│  ├─ http.method: "POST"
│  ├─ http.status_code: 200
│  ├─ db.type: "postgresql"
│  └─ error: false
└─ Logs:
   ├─ Event: "Payment validated"
   ├─ Event: "Database updated"
   └─ Event: "Notification sent"
```

## Span Correlation

### Parent-Child Relationships

```
┌─────────────────────────────────────────────────────────┐
│              Span Correlation                          │
└─────────────────────────────────────────────────────────┘

Trace ID: abc123

Span 1 (Root)
├─ Trace ID: abc123
├─ Span ID: span1
└─ Parent: null
    │
    ├─► Span 2 (Child)
    │   ├─ Trace ID: abc123
    │   ├─ Span ID: span2
    │   └─ Parent: span1
    │       │
    │       └─► Span 3 (Grandchild)
    │           ├─ Trace ID: abc123
    │           ├─ Span ID: span3
    │           └─ Parent: span2
    │
    └─► Span 4 (Child)
        ├─ Trace ID: abc123
        ├─ Span ID: span4
        └─ Parent: span1
```

### Trace Context Propagation

```
┌─────────────────────────────────────────────────────────┐
│              Context Propagation                        │
└─────────────────────────────────────────────────────────┘

Service A
    │
    │ Creates span
    │ trace_id: abc123
    │ span_id: span1
    │
    ▼
HTTP Request to Service B
    │
    │ Headers:
    │ traceparent: 00-abc123-span1-01
    │
    ▼
Service B
    │
    │ Receives trace context
    │ Creates child span
    │ trace_id: abc123 (same)
    │ span_id: span2
    │ parent_span_id: span1
    │
    ▼
HTTP Request to Service C
    │
    │ Headers:
    │ traceparent: 00-abc123-span2-01
    │
    ▼
Service C
    │
    │ Creates grandchild span
    │ trace_id: abc123 (same)
    │ span_id: span3
    │ parent_span_id: span2
```

## Latency Analysis

### Latency Breakdown

```
┌─────────────────────────────────────────────────────────┐
│              Latency Analysis                           │
└─────────────────────────────────────────────────────────┘

Total Request Time: 500ms
├─ API Gateway: 50ms
│  └─ Auth Check: 30ms
│
├─ Product Service: 200ms
│  ├─ Cache Lookup: 20ms (miss)
│  └─ Database Query: 180ms
│
├─ Payment Service: 200ms
│  ├─ Payment Processing: 150ms
│  └─ Database Update: 50ms
│
└─ Notification Service: 50ms
   └─ Message Queue: 50ms
```

### Identifying Bottlenecks

```
┌─────────────────────────────────────────────────────────┐
│              Bottleneck Identification                  │
└─────────────────────────────────────────────────────────┘

Service Latencies:
├─ API Gateway: 50ms (10%) ✓
├─ Product Service: 200ms (40%) ⚠️
├─ Payment Service: 200ms (40%) ⚠️
└─ Notification Service: 50ms (10%) ✓

Bottlenecks:
├─ Product Service DB Query: 180ms
│  └─ Action: Optimize query, add index
│
└─ Payment Processing: 150ms
   └─ Action: Optimize payment gateway call
```

### Latency Percentiles

```
┌─────────────────────────────────────────────────────────┐
│              Latency Percentiles                        │
└─────────────────────────────────────────────────────────┘

Service: Payment Service

p50 (Median): 150ms
├─ 50% of requests faster
└─ 50% of requests slower

p95: 500ms
├─ 95% of requests faster
└─ 5% of requests slower

p99: 1000ms
├─ 99% of requests faster
└─ 1% of requests slower (outliers)

Analysis:
├─ Most requests: Fast (150ms)
├─ Some requests: Slow (500ms)
└─ Few requests: Very slow (1000ms)
```

## Trace Visualization

### Timeline View

```
┌─────────────────────────────────────────────────────────┐
│              Trace Timeline                            │
└─────────────────────────────────────────────────────────┘

Time →
─────────────────────────────────────────────────────────
API Gateway     [████████████████████████] 200ms
  ├─ Auth       [████████] 50ms
  ├─ Product    [████████████████] 100ms
  │   ├─ DB     [████] 20ms
  │   └─ Cache  [██] 10ms
  └─ Payment    [████████] 50ms
─────────────────────────────────────────────────────────
Total: 200ms
```

### Service Map

```
┌─────────────────────────────────────────────────────────┐
│              Service Dependency Map                    │
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
│ Auth │ │Product│ │Payment│
└──┬───┘ └──┬───┘ └──┬───┘
   │        │        │
   │    ┌───┴───┐    │
   │    │       │    │
   ▼    ▼       ▼    ▼
┌────┐ ┌────┐ ┌────┐ ┌────┐
│User│ │Prod│ │Cache│ │Pay │
│ DB │ │ DB │ │     │ │Gate│
└────┘ └────┘ └────┘ └────┘
```

### Flame Graph

```
┌─────────────────────────────────────────────────────────┐
│              Flame Graph View                          │
└─────────────────────────────────────────────────────────┘

Width = Duration
Height = Call Stack

┌─────────────────────────────────────┐
│ API Gateway (200ms)                 │
├─────────────────────────────────────┤
│ ├─ Auth (50ms)                      │
│ ├─ Product (100ms)                  │
│ │  ├─ DB (20ms)                     │
│ │  └─ Cache (10ms)                  │
│ └─ Payment (50ms)                   │
└─────────────────────────────────────┘
```

## Error Propagation

### Error Flow

```
┌─────────────────────────────────────────────────────────┐
│              Error Propagation                         │
└─────────────────────────────────────────────────────────┘

Request Flow:
API Gateway
    │
    ├─► Auth Service ✓
    │
    ├─► Product Service ✓
    │
    ├─► Payment Service ✗ (Error)
    │   └─► Payment Gateway ✗ (Timeout)
    │
    └─► Notification Service (Not reached)

Trace shows:
├─ Error originated in Payment Gateway
├─ Propagated to Payment Service
└─ Request failed at Payment Service
```

### Error Tags in Traces

```
┌─────────────────────────────────────────────────────────┐
│              Error Information in Traces                │
└─────────────────────────────────────────────────────────┘

Span with Error:
├─ Tags:
│  ├─ error: true
│  ├─ error.type: "TimeoutException"
│  └─ error.message: "Connection timeout"
│
└─ Logs:
   ├─ Event: "Error occurred"
   ├─ Exception: "TimeoutException"
   └─ Stack Trace: "..."
```

## Trace Analysis

### Common Analysis Patterns

**1. Slow Request Analysis:**
```
Filter: duration > 1000ms
Group by: service
Sort by: duration (desc)
Identify: Slowest services
```

**2. Error Analysis:**
```
Filter: error = true
Group by: service, error_type
Count: errors per service
Identify: Most error-prone services
```

**3. Dependency Analysis:**
```
Extract: service dependencies
Build: dependency graph
Identify: Critical dependencies
```

**4. Performance Regression:**
```
Compare: Current vs. previous week
Metric: p95 latency
Identify: Services with regressions
```

## Best Practices

### 1. Consistent Naming
- Use standard operation names
- Include service name
- Use descriptive names

### 2. Add Context
- Include business identifiers
- Add user information
- Include request IDs
- Add correlation IDs

### 3. Error Handling
- Always record errors
- Include error details
- Set error status
- Add error tags

### 4. Sampling
- Don't trace 100%
- Use adaptive sampling
- Sample based on error rate
- Balance cost vs. visibility

### 5. Performance
- Use async exporters
- Batch span exports
- Limit span size
- Monitor overhead

## Summary

Observability Traces:
- **Purpose**: End-to-end visibility into request flows
- **Structure**: Traces contain spans with parent-child relationships
- **Analysis**: Latency breakdown, error propagation, dependency mapping
- **Visualization**: Timeline, service map, flame graphs

**Key Concepts:**
- Request Flows: How requests propagate through services
- Span Correlation: Parent-child relationships
- Latency Analysis: Where time is spent
- Error Propagation: How errors spread
- Service Dependencies: Which services call which

**Best Practices:**
- Consistent naming
- Add context information
- Proper error handling
- Implement sampling
- Monitor performance overhead
