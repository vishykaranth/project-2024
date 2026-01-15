# Observability Metrics: Quantitative Measurements, KPIs, SLIs

## Overview

Observability Metrics are quantitative measurements that provide insights into system behavior, business performance, and user experience. They form one of the three pillars of observability (Metrics, Logs, Traces) and are essential for understanding system health and making data-driven decisions.

## Metrics in Observability

```
┌─────────────────────────────────────────────────────────┐
│              Observability Metrics Role                 │
└─────────────────────────────────────────────────────────┘

Metrics provide:
├─ Quantitative Measurements
│  └─ Numerical values over time
│
├─ System Health Indicators
│  └─ Performance, availability, errors
│
├─ Business Intelligence
│  └─ KPIs, user behavior, revenue
│
└─ Service Level Indicators (SLIs)
   └─ Measurable aspects of service quality
```

## Types of Observability Metrics

### 1. System Metrics

**Infrastructure Metrics:**
- CPU usage
- Memory usage
- Disk I/O
- Network traffic
- Container metrics

**Application Metrics:**
- Request rate
- Response time
- Error rate
- Throughput
- Queue depth

### 2. Business Metrics

**User Metrics:**
- Active users
- New signups
- Feature usage
- Conversion rate
- Retention rate

**Revenue Metrics:**
- Revenue per user
- Transaction volume
- Average order value
- Churn rate

### 3. Service Level Metrics (SLIs)

**Availability:**
- Uptime percentage
- Error rate
- Success rate

**Latency:**
- Response time (p50, p95, p99)
- Time to first byte
- End-to-end latency

**Throughput:**
- Requests per second
- Transactions per second
- Data processed per second

## KPIs (Key Performance Indicators)

### What are KPIs?

KPIs are metrics that measure progress toward business objectives. They are critical metrics that directly impact business success.

```
┌─────────────────────────────────────────────────────────┐
│              KPI Characteristics                         │
└─────────────────────────────────────────────────────────┘

KPIs are:
├─ Strategic
│  └─ Aligned with business goals
│
├─ Measurable
│  └─ Quantifiable and trackable
│
├─ Actionable
│  └─ Can influence outcomes
│
└─ Time-bound
   └─ Measured over specific periods
```

### Technical KPIs

**Performance KPIs:**
- Average response time
- 95th percentile latency
- Throughput (requests/sec)
- Error rate
- Availability (uptime %)

**Example:**
```
KPI: API Response Time
├─ Target: < 200ms (p95)
├─ Current: 180ms
├─ Trend: ↘️ Improving
└─ Status: ✅ Meeting target
```

### Business KPIs

**User Engagement:**
- Daily Active Users (DAU)
- Monthly Active Users (MAU)
- Session duration
- Page views per session

**Revenue:**
- Monthly Recurring Revenue (MRR)
- Customer Acquisition Cost (CAC)
- Lifetime Value (LTV)
- Churn rate

**Example:**
```
KPI: Monthly Recurring Revenue
├─ Target: $1M
├─ Current: $950K
├─ Trend: ↗️ Growing
└─ Status: ⚠️ Below target
```

## SLIs (Service Level Indicators)

### What are SLIs?

SLIs are quantitative measures of a service's behavior from the user's perspective. They measure specific aspects of service quality.

```
┌─────────────────────────────────────────────────────────┐
│              SLI Components                             │
└─────────────────────────────────────────────────────────┘

SLI = Good Events / Total Events

Example:
├─ Availability SLI
│  └─ Successful requests / Total requests
│
├─ Latency SLI
│  └─ Requests under threshold / Total requests
│
└─ Error Rate SLI
   └─ Successful requests / Total requests
```

### Common SLIs

**1. Availability SLI**

```
Availability = (Successful Requests / Total Requests) × 100%

Example:
├─ Total requests: 1,000,000
├─ Successful: 999,500
├─ Failed: 500
└─ Availability: 99.95%
```

**2. Latency SLI**

```
Latency SLI = (Requests under threshold / Total requests) × 100%

Example:
├─ Total requests: 1,000,000
├─ Under 200ms: 950,000
├─ Over 200ms: 50,000
└─ Latency SLI: 95%
```

**3. Error Rate SLI**

```
Error Rate = (Error Requests / Total Requests) × 100%

Example:
├─ Total requests: 1,000,000
├─ Errors: 1,000
└─ Error Rate: 0.1%
```

## SLOs (Service Level Objectives)

### What are SLOs?

SLOs are target values for SLIs. They define the level of service quality a team commits to providing.

```
┌─────────────────────────────────────────────────────────┐
│              SLI → SLO Relationship                      │
└─────────────────────────────────────────────────────────┘

SLI: Measured value
    │
    │ (Compare against)
    │
    ▼
SLO: Target value
    │
    │ (If violated)
    │
    ▼
SLA: Contractual agreement
```

### SLO Examples

**Availability SLO:**
```
SLO: 99.9% availability
├─ SLI: Current availability = 99.95%
├─ Status: ✅ Meeting SLO
└─ Error Budget: 0.1% remaining
```

**Latency SLO:**
```
SLO: 95% of requests < 200ms
├─ SLI: 96% of requests < 200ms
├─ Status: ✅ Meeting SLO
└─ Error Budget: 1% remaining
```

**Error Rate SLO:**
```
SLO: Error rate < 0.1%
├─ SLI: Current error rate = 0.05%
├─ Status: ✅ Meeting SLO
└─ Error Budget: 0.05% remaining
```

## Error Budgets

### Concept

Error budgets represent the acceptable amount of service degradation. When error budget is exhausted, new features are paused to focus on reliability.

```
┌─────────────────────────────────────────────────────────┐
│              Error Budget Calculation                   │
└─────────────────────────────────────────────────────────┘

Error Budget = 100% - SLO

Example:
├─ SLO: 99.9% availability
├─ Error Budget: 0.1% downtime
├─ Monthly Budget: 43.2 minutes
└─ Used: 20 minutes
    └─ Remaining: 23.2 minutes
```

### Error Budget Policy

```
┌─────────────────────────────────────────────────────────┐
│              Error Budget Policy                        │
└─────────────────────────────────────────────────────────┘

Error Budget Status:
├─ > 50% remaining
│  └─ Normal operations, can deploy features
│
├─ 25-50% remaining
│  └─ Caution, reduce deployment frequency
│
├─ < 25% remaining
│  └─ Warning, focus on reliability
│
└─ Exhausted
   └─ Freeze new features, focus on reliability
```

## Metric Categories

### Golden Signals

The four golden signals of monitoring:

```
┌─────────────────────────────────────────────────────────┐
│              Four Golden Signals                        │
└─────────────────────────────────────────────────────────┘

1. Latency
   └─ Time to serve a request
   └─ Focus on p95, p99

2. Traffic
   └─ Demand on the system
   └─ Requests per second

3. Errors
   └─ Rate of failed requests
   └─ Error percentage

4. Saturation
   └─ How "full" the system is
   └─ CPU, memory, queue depth
```

### RED Method

**Rate, Errors, Duration:**

```
┌─────────────────────────────────────────────────────────┐
│              RED Metrics                                │
└─────────────────────────────────────────────────────────┘

R - Rate
   └─ Requests per second
   └─ Transactions per second

E - Errors
   └─ Error rate
   └─ Failed requests

D - Duration
   └─ Response time
   └─ Latency percentiles
```

### USE Method

**Utilization, Saturation, Errors:**

```
┌─────────────────────────────────────────────────────────┐
│              USE Metrics                                │
└─────────────────────────────────────────────────────────┘

U - Utilization
   └─ Percentage of resource busy
   └─ CPU, memory, disk usage

S - Saturation
   └─ Degree of queuing
   └─ Queue length, wait time

E - Errors
   └─ Error count
   └─ Failed operations
```

## Metric Collection

### Instrumentation

**Application Metrics:**

```java
// Counter: Total requests
Counter requestsTotal = Counter.build()
    .name("http_requests_total")
    .labelNames("method", "status")
    .help("Total HTTP requests")
    .register();

// Histogram: Request latency
Histogram requestDuration = Histogram.build()
    .name("http_request_duration_seconds")
    .help("HTTP request duration")
    .labelNames("method")
    .register();

// Gauge: Active connections
Gauge activeConnections = Gauge.build()
    .name("active_connections")
    .help("Active connections")
    .register();
```

### Metric Export

**Prometheus Format:**

```
# HELP http_requests_total Total HTTP requests
# TYPE http_requests_total counter
http_requests_total{method="GET",status="200"} 12345
http_requests_total{method="POST",status="200"} 6789
http_requests_total{method="GET",status="500"} 12

# HELP http_request_duration_seconds HTTP request duration
# TYPE http_request_duration_seconds histogram
http_request_duration_seconds_bucket{le="0.1"} 10000
http_request_duration_seconds_bucket{le="0.5"} 15000
http_request_duration_seconds_bucket{le="1.0"} 18000
```

## Metric Analysis

### Aggregation

**Time-based Aggregation:**
- Average over time
- Sum over time
- Min/Max over time
- Rate of change

**Label-based Aggregation:**
- Sum by service
- Average by instance
- Count by status

### Percentiles

```
┌─────────────────────────────────────────────────────────┐
│              Percentile Analysis                        │
└─────────────────────────────────────────────────────────┘

p50 (Median): 100ms
├─ 50% of requests faster
└─ 50% of requests slower

p95: 500ms
├─ 95% of requests faster
└─ 5% of requests slower

p99: 1000ms
├─ 99% of requests faster
└─ 1% of requests slower
```

## Best Practices

### 1. Choose Right Metrics
- Focus on business value
- Measure what matters
- Avoid metric overload

### 2. Set Appropriate Targets
- Based on user expectations
- Aligned with business goals
- Realistic and achievable

### 3. Monitor Trends
- Track over time
- Identify patterns
- Detect anomalies early

### 4. Use Percentiles
- Don't rely on averages
- Use p95, p99 for latency
- Understand tail behavior

### 5. Combine Metrics
- Don't look at metrics in isolation
- Correlate with other signals
- Use metrics, logs, and traces together

## Summary

Observability Metrics:
- **Purpose**: Quantitative measurements of system and business performance
- **Types**: System metrics, Business metrics, SLIs
- **KPIs**: Key metrics aligned with business objectives
- **SLIs**: Measurable aspects of service quality
- **SLOs**: Target values for SLIs

**Key Concepts:**
- Golden Signals: Latency, Traffic, Errors, Saturation
- RED Method: Rate, Errors, Duration
- USE Method: Utilization, Saturation, Errors
- Error Budgets: Acceptable service degradation

**Best Practices:**
- Choose metrics that matter
- Set appropriate targets
- Monitor trends
- Use percentiles
- Combine with other observability signals
