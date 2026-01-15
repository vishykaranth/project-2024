# APM: Application Performance Monitoring, New Relic, Datadog

## Overview

Application Performance Monitoring (APM) is a practice of monitoring and managing the performance and availability of software applications. APM tools provide comprehensive observability into application behavior, performance metrics, and user experience.

## What is APM?

```
┌─────────────────────────────────────────────────────────┐
│              APM Scope                                 │
└─────────────────────────────────────────────────────────┘

APM monitors:
├─ Application Performance
│  ├─ Response times
│  ├─ Throughput
│  └─ Error rates
│
├─ Infrastructure
│  ├─ Server metrics
│  ├─ Container metrics
│  └─ Cloud resources
│
├─ User Experience
│  ├─ Real user monitoring (RUM)
│  ├─ Synthetic monitoring
│  └─ User sessions
│
└─ Business Metrics
   ├─ Transactions
   ├─ Revenue
   └─ User behavior
```

## APM Architecture

```
┌─────────────────────────────────────────────────────────┐
│              APM System Architecture                   │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Instrumentation)
    │
    ▼
┌──────────────┐
│  APM Agent   │  ← Collects metrics, traces, logs
│  (SDK)       │
└──────┬───────┘
       │
       │ (Telemetry Data)
       │
       ▼
┌──────────────┐
│  APM         │  ← Processes and stores
│  Platform    │
└──────┬───────┘
       │
       │ (Analysis)
       │
       ▼
┌──────────────┐
│  APM UI      │  ← Dashboards, alerts, analysis
└──────────────┘
```

## APM Capabilities

### 1. Application Metrics

```
┌─────────────────────────────────────────────────────────┐
│              Application Metrics                      │
└─────────────────────────────────────────────────────────┘

├─ Response Time
│  ├─ Average
│  ├─ Percentiles (p50, p95, p99)
│  └─ By endpoint
│
├─ Throughput
│  ├─ Requests per second
│  ├─ Transactions per second
│  └─ Operations per second
│
├─ Error Rate
│  ├─ Error percentage
│  ├─ Error count
│  └─ Error types
│
└─ Resource Usage
   ├─ CPU
   ├─ Memory
   └─ Database connections
```

### 2. Distributed Tracing

```
┌─────────────────────────────────────────────────────────┐
│              Distributed Tracing in APM                 │
└─────────────────────────────────────────────────────────┘

├─ End-to-End Traces
│  └─ Complete request journey
│
├─ Service Map
│  └─ Service dependencies
│
├─ Span Analysis
│  └─ Individual operation details
│
└─ Error Tracking
   └─ Error propagation
```

### 3. Real User Monitoring (RUM)

```
┌─────────────────────────────────────────────────────────┐
│              Real User Monitoring                      │
└─────────────────────────────────────────────────────────┘

├─ Page Load Times
│  ├─ Time to first byte
│  ├─ DOM content loaded
│  └─ Page fully loaded
│
├─ User Sessions
│  ├─ Session duration
│  ├─ Page views
│  └─ User journey
│
├─ Browser Metrics
│  ├─ Browser type
│  ├─ Device type
│  └─ Geographic location
│
└─ JavaScript Errors
   ├─ Error count
   ├─ Error types
   └─ Affected users
```

### 4. Database Monitoring

```
┌─────────────────────────────────────────────────────────┐
│              Database Monitoring                        │
└─────────────────────────────────────────────────────────┘

├─ Query Performance
│  ├─ Slow queries
│  ├─ Query execution time
│  └─ Query frequency
│
├─ Connection Pool
│  ├─ Active connections
│  ├─ Idle connections
│  └─ Connection wait time
│
└─ Database Health
   ├─ CPU usage
   ├─ Memory usage
   └─ Disk I/O
```

## New Relic

### Overview

New Relic is a cloud-based APM platform that provides full-stack observability for applications, infrastructure, and user experience.

### New Relic Architecture

```
┌─────────────────────────────────────────────────────────┐
│              New Relic Components                       │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (New Relic Agent)
    │
    ▼
┌──────────────┐
│  New Relic   │  ← Cloud platform
│  Platform    │
└──────┬───────┘
       │
       ├─► APM (Application Performance)
       ├─► Infrastructure Monitoring
       ├─► Browser Monitoring (RUM)
       ├─► Synthetics
       └─► Logs
```

### New Relic Features

**1. APM:**
- Application performance monitoring
- Transaction tracing
- Error tracking
- Database monitoring

**2. Infrastructure:**
- Server monitoring
- Container monitoring
- Cloud monitoring
- Network monitoring

**3. Browser (RUM):**
- Real user monitoring
- Page load performance
- JavaScript error tracking
- User session replay

**4. Synthetics:**
- Uptime monitoring
- API monitoring
- Browser monitoring
- Alerting

**5. Logs:**
- Centralized logging
- Log aggregation
- Log search
- Log analysis

### New Relic Query Language (NRQL)

```sql
-- Average response time
SELECT average(duration) 
FROM Transaction 
WHERE appName = 'MyApp' 
FACET name 
SINCE 1 hour ago

-- Error rate
SELECT percentage(count(*), WHERE error IS true) 
FROM Transaction 
WHERE appName = 'MyApp' 
SINCE 1 hour ago

-- Top slow queries
SELECT average(duration) 
FROM Transaction 
WHERE appName = 'MyApp' 
FACET name 
ORDER BY average(duration) DESC 
LIMIT 10
```

### New Relic Instrumentation

**Java Example:**

```java
// Add dependency
// com.newrelic.agent.java:newrelic-agent:7.0.0

// Auto-instrumentation (no code changes)
// Just add agent to JVM:
// -javaagent:newrelic.jar

// Manual instrumentation
import com.newrelic.api.agent.*;

@Trace
public void processPayment(PaymentRequest request) {
    NewRelic.setTransactionName("Payment", "processPayment");
    NewRelic.addCustomParameter("amount", request.getAmount());
    
    try {
        // Business logic
        process(request);
        NewRelic.recordMetric("Payment/Success", 1);
    } catch (Exception e) {
        NewRelic.noticeError(e);
        NewRelic.recordMetric("Payment/Error", 1);
        throw e;
    }
}
```

## Datadog

### Overview

Datadog is a monitoring and analytics platform that provides infrastructure monitoring, APM, log management, and security monitoring.

### Datadog Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Datadog Components                         │
└─────────────────────────────────────────────────────────┘

Applications/Infrastructure
    │
    │ (Datadog Agent)
    │
    ▼
┌──────────────┐
│  Datadog     │  ← Cloud platform
│  Platform    │
└──────┬───────┘
       │
       ├─► APM
       ├─► Infrastructure Monitoring
       ├─► Logs
       ├─► Synthetics
       └─► Security Monitoring
```

### Datadog Features

**1. APM:**
- Distributed tracing
- Service map
- Performance monitoring
- Error tracking

**2. Infrastructure:**
- Server monitoring
- Container monitoring
- Kubernetes monitoring
- Cloud integrations

**3. Logs:**
- Log management
- Log search
- Log analytics
- Log pipelines

**4. Synthetics:**
- API tests
- Browser tests
- Uptime monitoring
- Alerting

**5. Security:**
- Security monitoring
- Threat detection
- Compliance monitoring

### Datadog Instrumentation

**Java Example:**

```java
// Add dependency
// com.datadoghq:dd-trace-api:1.0.0

// Auto-instrumentation
// Add agent to JVM:
// -javaagent:dd-java-agent.jar

// Manual instrumentation
import datadog.trace.api.Trace;

@Trace
public void processPayment(PaymentRequest request) {
    Span span = GlobalTracer.get().activeSpan();
    if (span != null) {
        span.setTag("payment.amount", request.getAmount());
        span.setTag("payment.currency", request.getCurrency());
    }
    
    try {
        process(request);
    } catch (Exception e) {
        if (span != null) {
            span.setError(true);
            span.log(Collections.singletonMap("error", e));
        }
        throw e;
    }
}
```

### Datadog Query Language

```
# Average response time
avg:trace.http.request.duration{service:api-service}

# Error rate
sum:trace.http.request.errors{service:api-service} / 
sum:trace.http.request.hits{service:api-service} * 100

# P95 latency
p95:trace.http.request.duration{service:api-service}
```

## APM Tool Comparison

| Feature | New Relic | Datadog |
|---------|-----------|---------|
| **APM** | Excellent | Excellent |
| **Infrastructure** | Good | Excellent |
| **Logs** | Good | Excellent |
| **RUM** | Excellent | Good |
| **Pricing** | Per GB | Per host/GB |
| **Ease of Use** | Easy | Moderate |
| **Custom Dashboards** | Good | Excellent |
| **Alerting** | Good | Excellent |

## APM Best Practices

### 1. Instrumentation
- Use auto-instrumentation when possible
- Add manual instrumentation for custom code
- Instrument all critical paths
- Include business context

### 2. Sampling
- Don't trace 100% of requests
- Use adaptive sampling
- Sample based on error rate
- Balance cost vs. visibility

### 3. Alerting
- Set up alerts for key metrics
- Use appropriate thresholds
- Avoid alert fatigue
- Create runbooks

### 4. Dashboards
- Create custom dashboards
- Focus on business metrics
- Include SLOs/SLIs
- Share with stakeholders

### 5. Performance
- Monitor APM overhead
- Optimize instrumentation
- Use sampling
- Review regularly

## Summary

APM Tools:
- **Purpose**: Comprehensive application performance monitoring
- **Capabilities**: Metrics, traces, logs, RUM, infrastructure
- **Tools**: New Relic, Datadog (leading platforms)
- **Benefits**: Full-stack observability, faster troubleshooting

**Key Features:**
- Application Performance: Response times, throughput, errors
- Distributed Tracing: End-to-end request visibility
- Real User Monitoring: User experience metrics
- Infrastructure Monitoring: Server, container, cloud metrics
- Log Management: Centralized logging and analysis

**Best Practices:**
- Use auto-instrumentation
- Implement sampling
- Set up alerting
- Create dashboards
- Monitor performance overhead
