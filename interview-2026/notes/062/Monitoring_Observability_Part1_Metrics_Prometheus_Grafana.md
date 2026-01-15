# Metrics: Prometheus, Grafana, Time-Series Data

## Overview

Metrics are quantitative measurements collected over time that provide insights into system behavior, performance, and health. They are fundamental to monitoring and observability, allowing teams to track system performance, identify trends, and detect anomalies.

## What are Metrics?

Metrics are numerical values that represent system characteristics at specific points in time. They are collected continuously and stored as time-series data.

```
┌─────────────────────────────────────────────────────────┐
│              Metrics Characteristics                     │
└─────────────────────────────────────────────────────────┘

├─ Numerical Values
│  ├─ Counters: Incrementing values (requests, errors)
│  ├─ Gauges: Current values (CPU usage, memory)
│  ├─ Histograms: Distribution of values (latency)
│  └─ Summaries: Quantiles over time (percentiles)
│
├─ Time-Series Data
│  ├─ Timestamp: When measurement was taken
│  ├─ Value: The measurement itself
│  └─ Labels: Metadata (service, environment, etc.)
│
└─ Aggregation
   ├─ Sum, Average, Min, Max
   ├─ Percentiles (p50, p95, p99)
   └─ Rate of change
```

## Time-Series Data

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              Time-Series Data Structure                  │
└─────────────────────────────────────────────────────────┘

Metric Name: http_requests_total
Labels: {method="GET", status="200", service="api"}

Timeline:
┌──────────┬──────────┬──────────┬──────────┐
│ Time     │ Value    │ Labels   │          │
├──────────┼──────────┼──────────┼──────────┤
│ 10:00:00 │ 1000     │ GET/200  │          │
│ 10:00:01 │ 1005     │ GET/200  │          │
│ 10:00:02 │ 1012     │ GET/200  │          │
│ 10:00:03 │ 1018     │ GET/200  │          │
└──────────┴──────────┴──────────┴──────────┘
```

### Time-Series Database

```
┌─────────────────────────────────────────────────────────┐
│         Time-Series Database Architecture                │
└─────────────────────────────────────────────────────────┘

Applications
    │
    ▼
┌─────────────────┐
│ Metrics Export  │  ← Prometheus client libraries
│ (Instrumentation)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Metrics Scraper │  ← Prometheus scrapes endpoints
│ (Prometheus)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Time-Series DB  │  ← Stores metrics with timestamps
│ (Storage)       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Query Engine    │  ← PromQL queries
│ (PromQL)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Visualization   │  ← Grafana dashboards
│ (Grafana)       │
└─────────────────┘
```

## Prometheus

### Overview

Prometheus is an open-source monitoring and alerting toolkit designed for reliability and scalability. It collects metrics from configured targets at given intervals, stores them, and provides a query language (PromQL) to analyze the data.

### Prometheus Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Prometheus Architecture                     │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Prometheus Server                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────┐    ┌──────────────┐            │
│  │   Scraper    │───►│   Storage    │            │
│  │  (Pull)      │    │  (TSDB)      │            │
│  └──────────────┘    └──────────────┘            │
│         │                    │                    │
│         │                    ▼                    │
│         │            ┌──────────────┐              │
│         │            │  PromQL     │              │
│         │            │  Engine     │              │
│         │            └──────────────┘              │
│         │                    │                    │
│         │                    ▼                    │
│         │            ┌──────────────┐              │
│         │            │   HTTP API  │              │
│         │            │   /metrics  │              │
│         │            └──────────────┘              │
│         │                    │                    │
│         └────────────────────┘                    │
│                    │                               │
└────────────────────┼───────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Service  │  │ Service  │  │ Service  │
│   A      │  │   B      │  │   C      │
│ /metrics │  │ /metrics │  │ /metrics │
└──────────┘  └──────────┘  └──────────┘
```

### Prometheus Data Model

#### Metric Types

**1. Counter**
- Monotonically increasing value
- Resets to zero on restart
- Example: Total HTTP requests

```promql
http_requests_total{method="GET", status="200"}
```

**2. Gauge**
- Value that can go up or down
- Current state measurement
- Example: CPU usage, memory

```promql
cpu_usage_percent{instance="server1"}
memory_usage_bytes{instance="server1"}
```

**3. Histogram**
- Samples observations and counts them in buckets
- Tracks distribution of values
- Example: Request latency distribution

```promql
http_request_duration_seconds_bucket{le="0.1"}
http_request_duration_seconds_bucket{le="0.5"}
http_request_duration_seconds_bucket{le="1.0"}
```

**4. Summary**
- Similar to histogram but calculates quantiles
- Pre-calculated percentiles
- Example: Request latency percentiles

```promql
http_request_duration_seconds{quantile="0.5"}
http_request_duration_seconds{quantile="0.95"}
http_request_duration_seconds{quantile="0.99"}
```

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
  
  - job_name: 'api-service'
    static_configs:
      - targets: ['api:8080']
    metrics_path: '/metrics'
    scrape_interval: 10s
  
  - job_name: 'database'
    static_configs:
      - targets: ['db:5432']
```

### PromQL (Prometheus Query Language)

#### Basic Queries

```promql
# Select all HTTP requests
http_requests_total

# Filter by label
http_requests_total{method="GET"}

# Multiple label filters
http_requests_total{method="GET", status="200"}

# Rate of change (requests per second)
rate(http_requests_total[5m])

# Increase over time
increase(http_requests_total[1h])

# Average
avg(cpu_usage_percent)

# Sum across instances
sum(http_requests_total) by (method)
```

#### Advanced Queries

```promql
# 95th percentile latency
histogram_quantile(0.95, 
  rate(http_request_duration_seconds_bucket[5m])
)

# Error rate
rate(http_requests_total{status=~"5.."}[5m]) / 
rate(http_requests_total[5m])

# CPU usage above 80%
cpu_usage_percent > 80
```

## Grafana

### Overview

Grafana is an open-source analytics and visualization platform that connects to various data sources, including Prometheus, to create dashboards and alerts.

### Grafana Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Grafana Architecture                        │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Grafana Server                         │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────┐    ┌──────────────┐            │
│  │  Data Source │───►│   Query     │            │
│  │  Connectors  │    │   Builder    │            │
│  └──────────────┘    └──────────────┘            │
│         │                    │                    │
│         │                    ▼                    │
│         │            ┌──────────────┐              │
│         │            │ Visualization│              │
│         │            │   Engine    │              │
│         │            └──────────────┘              │
│         │                    │                    │
│         │                    ▼                    │
│         │            ┌──────────────┐              │
│         │            │  Dashboard  │              │
│         │            │   Renderer   │              │
│         │            └──────────────┘              │
│         │                    │                    │
│         └────────────────────┘                    │
│                    │                               │
└────────────────────┼───────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│Prometheus│  │InfluxDB  │  │CloudWatch│
│          │  │          │  │          │
└──────────┘  └──────────┘  └──────────┘
```

### Grafana Dashboard Components

```
┌─────────────────────────────────────────────────────────┐
│              Grafana Dashboard Layout                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│  Dashboard: API Service Monitoring                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────────────────────────────────┐  │
│  │  Row 1: Request Metrics                     │  │
│  ├─────────────────────────────────────────────┤  │
│  │  [Graph] HTTP Requests/sec                  │  │
│  │  [Stat]  Total Requests                     │  │
│  │  [Stat]  Error Rate                         │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
│  ┌─────────────────────────────────────────────┐  │
│  │  Row 2: Latency Metrics                      │  │
│  ├─────────────────────────────────────────────┤  │
│  │  [Graph] Response Time (p50, p95, p99)      │  │
│  │  [Heatmap] Latency Distribution              │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
│  ┌─────────────────────────────────────────────┐  │
│  │  Row 3: System Metrics                       │  │
│  ├─────────────────────────────────────────────┤  │
│  │  [Graph] CPU Usage                          │  │
│  │  [Graph] Memory Usage                       │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Grafana Panel Types

**1. Graph Panel**
- Time-series visualization
- Multiple series support
- Zoom and pan

**2. Stat Panel**
- Single value display
- Color thresholds
- Sparklines

**3. Table Panel**
- Tabular data
- Sortable columns
- Formatted values

**4. Heatmap Panel**
- Distribution visualization
- Time on X-axis
- Value buckets on Y-axis

**5. Gauge Panel**
- Current value display
- Min/max thresholds
- Color coding

### Grafana Query Example

```json
{
  "expr": "rate(http_requests_total[5m])",
  "legendFormat": "{{method}} {{status}}",
  "refId": "A"
}
```

## Metrics Collection Patterns

### Pull Model (Prometheus)

```
┌─────────────────────────────────────────────────────────┐
│              Pull Model Architecture                     │
└─────────────────────────────────────────────────────────┘

Prometheus Server
    │
    │ (Scrapes every 15s)
    │
    ▼
┌──────────┐
│ Service  │  ← Exposes /metrics endpoint
│ /metrics │
└──────────┘

Benefits:
✓ Service doesn't need to know about Prometheus
✓ Service can be down without affecting Prometheus
✓ Centralized configuration
```

### Push Model (Pushgateway)

```
┌─────────────────────────────────────────────────────────┐
│              Push Model Architecture                     │
└─────────────────────────────────────────────────────────┘

Short-lived Jobs
    │
    │ (Push on completion)
    │
    ▼
┌──────────────┐
│ Pushgateway  │  ← Receives metrics
└──────┬───────┘
       │
       │ (Scraped by Prometheus)
       │
       ▼
┌──────────────┐
│ Prometheus   │
└──────────────┘

Use Cases:
- Batch jobs
- Cron jobs
- Short-lived processes
```

## Instrumentation

### Application Instrumentation

```java
// Java Example with Prometheus Client
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

public class ApiController {
    // Counter metric
    private static final Counter requestsTotal = Counter.build()
        .name("http_requests_total")
        .help("Total HTTP requests")
        .labelNames("method", "status")
        .register();
    
    // Histogram metric
    private static final Histogram requestDuration = Histogram.build()
        .name("http_request_duration_seconds")
        .help("HTTP request duration")
        .labelNames("method")
        .register();
    
    public ResponseEntity<String> handleRequest() {
        Histogram.Timer timer = requestDuration.labels("GET").startTimer();
        try {
            // Process request
            requestsTotal.labels("GET", "200").inc();
            return ResponseEntity.ok("Success");
        } finally {
            timer.observeDuration();
        }
    }
}
```

### Exposing Metrics Endpoint

```java
// Spring Boot Example
@RestController
public class MetricsController {
    
    @GetMapping("/metrics")
    public String metrics() {
        return PrometheusRegistry.defaultRegistry.scrape();
    }
}
```

## Best Practices

### 1. Metric Naming
- Use consistent naming conventions
- Include units in metric names
- Use underscores, not dots or dashes
- Example: `http_requests_total`, `cpu_usage_percent`

### 2. Label Cardinality
- Avoid high-cardinality labels
- Don't use user IDs, IPs as labels
- Use labels for grouping, not unique identification

### 3. Metric Granularity
- Balance detail vs. storage
- Too fine: Storage bloat
- Too coarse: Missing insights

### 4. Retention Policy
- Define retention periods
- Archive old data
- Balance cost vs. need

### 5. Alerting Rules
- Set meaningful thresholds
- Avoid alert fatigue
- Use alerting best practices

## Summary

Metrics and Time-Series Data:
- **Purpose**: Quantitative measurements over time
- **Types**: Counters, Gauges, Histograms, Summaries
- **Storage**: Time-series databases optimized for temporal data
- **Tools**: Prometheus (collection/query), Grafana (visualization)

**Key Concepts:**
- Time-series data: Timestamp + Value + Labels
- Prometheus: Pull-based metrics collection
- PromQL: Powerful query language
- Grafana: Visualization and dashboards
- Instrumentation: Adding metrics to applications

**Best Practices:**
- Consistent naming conventions
- Appropriate label cardinality
- Balanced granularity
- Defined retention policies
- Meaningful alerting rules
