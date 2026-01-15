# Metrics Tools: Prometheus, InfluxDB, CloudWatch Metrics

## Overview

Metrics Tools provide collection, storage, and analysis of time-series metrics data. They enable monitoring of system performance, application behavior, and business KPIs through quantitative measurements over time.

## Metrics Tools Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Metrics Tools Architecture                  │
└─────────────────────────────────────────────────────────┘

Applications/Infrastructure
    │
    │ (Metrics)
    │
    ▼
┌──────────────┐
│  Metrics     │  ← Collects metrics
│  Collectors  │
└──────┬───────┘
       │
       │ (Time-series data)
       │
       ▼
┌──────────────┐
│  Time-Series │  ← Stores metrics
│  Database    │
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  Query       │  ← Analyzes metrics
│  Engine      │
└──────┬───────┘
       │
       │ (Visualization)
       │
       ▼
┌──────────────┐
│  Dashboard   │  ← Visualizes metrics
│  Tools       │
└──────────────┘
```

## Prometheus

### Overview

Prometheus is an open-source monitoring and alerting toolkit designed for reliability and scalability. It uses a pull-based model to collect metrics and provides a powerful query language (PromQL).

### Prometheus Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Prometheus Components                      │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Exposes /metrics)
    │
    ▼
┌──────────────┐
│  Prometheus  │  ← Scrapes metrics
│  Server      │
└──────┬───────┘
       │
       ├─► Scraper (Pull)
       ├─► Storage (TSDB)
       ├─► Query Engine (PromQL)
       └─► Alertmanager
```

### Prometheus Data Model

**Metric Types:**

```
┌─────────────────────────────────────────────────────────┐
│              Prometheus Metric Types                     │
└─────────────────────────────────────────────────────────┘

├─ Counter
│  └─ Monotonically increasing (requests_total)
│
├─ Gauge
│  └─ Can go up or down (cpu_usage_percent)
│
├─ Histogram
│  └─ Distribution of values (request_duration_seconds)
│
└─ Summary
   └─ Quantiles over time (request_duration_seconds)
```

### PromQL Examples

```promql
# Simple query
http_requests_total

# Filter by labels
http_requests_total{method="GET", status="200"}

# Rate calculation
rate(http_requests_total[5m])

# Aggregation
sum(http_requests_total) by (method)

# Percentile
histogram_quantile(0.95, 
  rate(http_request_duration_seconds_bucket[5m])
)
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
```

## InfluxDB

### Overview

InfluxDB is a time-series database designed for high-write and query loads. It's optimized for metrics, events, and real-time analytics.

### InfluxDB Architecture

```
┌─────────────────────────────────────────────────────────┐
│              InfluxDB Components                        │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Metrics via Telegraf/Client)
    │
    ▼
┌──────────────┐
│  InfluxDB    │  ← Stores time-series data
│  Server      │
└──────┬───────┘
       │
       ├─► Storage Engine
       ├─► Query Engine (InfluxQL/Flux)
       └─► HTTP API
```

### InfluxDB Data Model

**Measurement, Tags, Fields:**

```
┌─────────────────────────────────────────────────────────┐
│              InfluxDB Data Structure                     │
└─────────────────────────────────────────────────────────┘

Measurement: cpu_usage
Tags: host=server1, region=us-east
Fields: usage=75.5, idle=24.5
Timestamp: 2024-01-15T10:30:00Z

Example:
cpu_usage,host=server1,region=us-east usage=75.5,idle=24.5 1705312200000000000
```

### InfluxQL Examples

```sql
-- Select all data
SELECT * FROM cpu_usage

-- Filter by time
SELECT * FROM cpu_usage WHERE time > now() - 1h

-- Aggregation
SELECT mean(usage) FROM cpu_usage 
WHERE time > now() - 1h 
GROUP BY time(5m), host

-- Multiple fields
SELECT mean(usage), max(usage) FROM cpu_usage
WHERE time > now() - 1h
GROUP BY host
```

### Flux Query Language

```flux
// Flux example
from(bucket: "metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "cpu_usage")
  |> filter(fn: (r) => r.host == "server1")
  |> aggregateWindow(every: 5m, fn: mean)
```

### InfluxDB Configuration

**Telegraf Configuration:**

```toml
# telegraf.conf
[[inputs.cpu]]
  percpu = true
  totalcpu = true

[[inputs.mem]]

[[outputs.influxdb]]
  urls = ["http://influxdb:8086"]
  database = "metrics"
```

## AWS CloudWatch Metrics

### Overview

CloudWatch Metrics is AWS's monitoring service that collects and tracks metrics for AWS resources and custom applications.

### CloudWatch Metrics Architecture

```
┌─────────────────────────────────────────────────────────┐
│              CloudWatch Metrics Architecture            │
└─────────────────────────────────────────────────────────┘

AWS Resources/Applications
    │
    │ (Metrics)
    │
    ▼
┌──────────────┐
│  CloudWatch   │  ← Collects metrics
│  Agent        │
└──────┬───────┘
       │
       │ (Metric data points)
       │
       ▼
┌──────────────┐
│  CloudWatch  │  ← Stores metrics
│  Metrics      │
└──────┬───────┘
       │
       ├─► Namespaces
       ├─► Metric Names
       └─► Dimensions
```

### CloudWatch Metrics Structure

**Namespace, Metric Name, Dimensions:**

```
┌─────────────────────────────────────────────────────────┐
│              CloudWatch Metrics Structure                │
└─────────────────────────────────────────────────────────┘

Namespace: AWS/EC2
Metric Name: CPUUtilization
Dimensions:
  - InstanceId: i-1234567890abcdef0
  - InstanceType: t2.micro

Data Points:
  - Timestamp: 2024-01-15T10:30:00Z
  - Value: 75.5
  - Unit: Percent
```

### CloudWatch Metrics Types

**1. Standard Resolution:**
- 1-minute granularity
- 15 months retention
- Free tier available

**2. High Resolution:**
- 1-second granularity
- 3 hours retention
- Paid service

**3. Custom Metrics:**
- Application-specific
- Custom namespaces
- Flexible dimensions

### CloudWatch Metrics API

**Put Metric:**

```bash
aws cloudwatch put-metric-data \
  --namespace MyApp \
  --metric-name RequestCount \
  --value 100 \
  --unit Count \
  --dimensions InstanceId=i-123,Environment=prod
```

**Get Metric Statistics:**

```bash
aws cloudwatch get-metric-statistics \
  --namespace AWS/EC2 \
  --metric-name CPUUtilization \
  --dimensions Name=InstanceId,Value=i-123 \
  --start-time 2024-01-15T00:00:00Z \
  --end-time 2024-01-15T23:59:59Z \
  --period 3600 \
  --statistics Average,Maximum,Minimum
```

## Tool Comparison

| Feature | Prometheus | InfluxDB | CloudWatch Metrics |
|---------|-----------|----------|-------------------|
| **License** | Open Source | Open Source/Commercial | AWS Service |
| **Model** | Pull | Push/Pull | Push |
| **Query Language** | PromQL | InfluxQL/Flux | CloudWatch API |
| **Scalability** | Good | Excellent | Excellent |
| **AWS Integration** | Manual | Manual | Native |
| **Cost** | Free | Free/Paid | Pay per metric |
| **Use Case** | Kubernetes, Microservices | IoT, High-frequency | AWS workloads |

## Best Practices

### 1. Metric Naming
- Use consistent conventions
- Include units
- Use underscores
- Be descriptive

### 2. Label Cardinality
- Avoid high-cardinality labels
- Don't use unique IDs as labels
- Use labels for grouping

### 3. Retention
- Define retention policies
- Archive old data
- Balance cost vs. need

### 4. Aggregation
- Pre-aggregate when possible
- Use appropriate intervals
- Store at multiple resolutions

### 5. Alerting
- Set meaningful thresholds
- Use percentiles
- Avoid alert fatigue

## Summary

Metrics Tools:
- **Purpose**: Collect, store, and analyze time-series metrics
- **Prometheus**: Open-source, pull-based, PromQL
- **InfluxDB**: Time-series database, InfluxQL/Flux
- **CloudWatch Metrics**: AWS-native metrics service

**Key Features:**
- Time-Series Storage: Optimized for temporal data
- Query Languages: PromQL, InfluxQL, Flux, CloudWatch API
- Aggregation: Sum, average, percentiles
- Visualization: Grafana, Chronograf, CloudWatch Dashboards

**Best Practices:**
- Consistent naming conventions
- Appropriate label cardinality
- Defined retention policies
- Pre-aggregation when possible
- Meaningful alerting thresholds
