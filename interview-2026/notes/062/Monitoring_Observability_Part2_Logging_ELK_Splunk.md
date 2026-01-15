# Logging: ELK Stack, Splunk, Centralized Logging

## Overview

Logging is the process of recording events, messages, and data points that occur during application execution. Centralized logging aggregates logs from multiple sources into a single location, making it easier to search, analyze, and monitor system behavior across distributed systems.

## Logging Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Centralized Logging Architecture           │
└─────────────────────────────────────────────────────────┘

┌──────────┐  ┌──────────┐  ┌──────────┐
│ Service  │  │ Service  │  │ Service  │
│   A      │  │   B      │  │   C      │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │            │            │
     │            │            │
     └────────────┼────────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Log Shippers   │  ← Filebeat, Fluentd, Logstash
         │  (Agents)       │
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Log Aggregator │  ← Logstash, Fluentd
         │  (Processing)   │
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Log Storage    │  ← Elasticsearch, Splunk
         │  (Search Engine) │
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Visualization  │  ← Kibana, Splunk UI
         │  (Dashboards)   │
         └─────────────────┘
```

## ELK Stack

### Overview

ELK Stack is a collection of three open-source tools:
- **Elasticsearch**: Search and analytics engine
- **Logstash**: Log processing pipeline
- **Kibana**: Visualization and exploration

### ELK Stack Architecture

```
┌─────────────────────────────────────────────────────────┐
│              ELK Stack Components                       │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Logs)
    │
    ▼
┌──────────────┐
│  Filebeat    │  ← Lightweight log shipper
│  (Shipper)   │
└──────┬───────┘
       │
       │ (Structured logs)
       │
       ▼
┌──────────────┐
│  Logstash    │  ← Log processing pipeline
│  (Processor) │
└──────┬───────┘
       │
       │ (Indexed data)
       │
       ▼
┌──────────────┐
│ Elasticsearch│  ← Search and storage
│  (Storage)   │
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│   Kibana     │  ← Visualization
│  (UI)        │
└──────────────┘
```

### 1. Elasticsearch

**Overview**: Distributed search and analytics engine built on Apache Lucene.

**Key Features:**
- Full-text search
- Real-time indexing
- Horizontal scalability
- RESTful API
- JSON document storage

**Elasticsearch Structure:**

```
┌─────────────────────────────────────────────────────────┐
│              Elasticsearch Hierarchy                    │
└─────────────────────────────────────────────────────────┘

Cluster
    │
    ├─► Node 1
    │   └─► Index: logs-2024-01
    │       └─► Shard 1, Shard 2
    │
    ├─► Node 2
    │   └─► Index: logs-2024-01
    │       └─► Shard 1 (replica), Shard 2 (replica)
    │
    └─► Node 3
        └─► Index: logs-2024-02
            └─► Shard 1, Shard 2
```

**Document Structure:**

```json
{
  "@timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "message": "User login successful",
  "service": "auth-service",
  "user_id": "12345",
  "ip_address": "192.168.1.100",
  "duration_ms": 45
}
```

**Query Example:**

```json
GET /logs-2024-01/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "level": "ERROR" }},
        { "range": { "@timestamp": { "gte": "now-1h" }}}
      ]
    }
  },
  "sort": [{ "@timestamp": "desc" }]
}
```

### 2. Logstash

**Overview**: Server-side data processing pipeline that ingests data from multiple sources, transforms it, and sends it to Elasticsearch.

**Logstash Pipeline:**

```
┌─────────────────────────────────────────────────────────┐
│              Logstash Pipeline Stages                    │
└─────────────────────────────────────────────────────────┘

Input
    │
    ▼
┌──────────────┐
│  Parse Logs   │  ← Extract fields, parse formats
│  (Filter)     │
└──────┬────────┘
       │
       ▼
┌──────────────┐
│  Transform   │  ← Enrich, modify, add fields
│  (Filter)     │
└──────┬────────┘
       │
       ▼
┌──────────────┐
│  Output      │  ← Send to Elasticsearch
│  (Output)     │
└──────────────┘
```

**Logstash Configuration:**

```ruby
# logstash.conf
input {
  beats {
    port => 5044
  }
}

filter {
  # Parse JSON logs
  if [fields][log_type] == "json" {
    json {
      source => "message"
    }
  }
  
  # Parse Apache logs
  if [fields][log_type] == "apache" {
    grok {
      match => { "message" => "%{COMBINEDAPACHELOG}" }
    }
  }
  
  # Add timestamp
  date {
    match => [ "timestamp", "ISO8601" ]
  }
  
  # Add geoip
  geoip {
    source => "clientip"
  }
  
  # Remove sensitive data
  mutate {
    remove_field => [ "password", "ssn" ]
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "logs-%{+YYYY.MM.dd}"
  }
  
  # Also output to file for debugging
  file {
    path => "/var/log/logstash/output.log"
  }
}
```

### 3. Kibana

**Overview**: Visualization and exploration tool for Elasticsearch data.

**Kibana Features:**

```
┌─────────────────────────────────────────────────────────┐
│              Kibana Capabilities                         │
└─────────────────────────────────────────────────────────┘

├─ Discover
│  ├─ Search logs
│  ├─ Filter by fields
│  └─ View raw documents
│
├─ Visualize
│  ├─ Create charts
│  ├─ Build graphs
│  └─ Design visualizations
│
├─ Dashboard
│  ├─ Combine visualizations
│  ├─ Real-time updates
│  └─ Share dashboards
│
├─ Dev Tools
│  ├─ Query editor
│  ├─ Console for Elasticsearch
│  └─ API testing
│
└─ Management
   ├─ Index management
   ├─ Index patterns
   └─ Saved objects
```

**Kibana Dashboard Example:**

```
┌─────────────────────────────────────────────────────┐
│  Dashboard: Application Logs                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────────────────────────────────┐  │
│  │  Log Level Distribution (Pie Chart)         │  │
│  │  INFO: 70% | WARN: 20% | ERROR: 10%        │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
│  ┌─────────────────────────────────────────────┐  │
│  │  Error Rate Over Time (Line Chart)          │  │
│  │  [Graph showing errors per hour]            │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
│  ┌─────────────────────────────────────────────┐  │
│  │  Recent Errors (Data Table)                 │  │
│  │  [Table with error logs]                    │  │
│  └─────────────────────────────────────────────┘  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Filebeat (Log Shipper)

**Overview**: Lightweight log shipper that forwards logs to Logstash or Elasticsearch.

**Filebeat Configuration:**

```yaml
# filebeat.yml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /var/log/app/*.log
    fields:
      log_type: application
      service: api-service
    fields_under_root: false

output.logstash:
  hosts: ["logstash:5044"]

# Optional: Processors
processors:
  - add_host_metadata:
      when.not.contains.tags: forwarded
```

## Splunk

### Overview

Splunk is a commercial platform for searching, monitoring, and analyzing machine-generated data. It's known for its powerful search capabilities and enterprise features.

### Splunk Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Splunk Architecture                        │
└─────────────────────────────────────────────────────────┘

┌──────────┐  ┌──────────┐  ┌──────────┐
│ Service  │  │ Service  │  │ Service  │
│   A      │  │   B      │  │   C      │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │            │            │
     │            │            │
     └────────────┼────────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Universal      │  ← Splunk Universal Forwarder
         │  Forwarder      │
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Indexer        │  ← Splunk Indexer
         │  (Storage)      │
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Search Head    │  ← Splunk Search Head
         │  (Query Engine) │
         └────────┬─────────┘
                  │
                  ▼
         ┌─────────────────┐
         │  Splunk Web     │  ← User Interface
         │  (UI)            │
         └─────────────────┘
```

### Splunk Components

**1. Universal Forwarder**
- Lightweight agent
- Forwards logs to indexers
- No indexing capability
- Low resource usage

**2. Indexer**
- Receives and indexes data
- Stores in indexes
- Creates searchable data
- Handles search requests

**3. Search Head**
- Coordinates searches
- Distributes queries
- Aggregates results
- Manages dashboards

### Splunk Search Language (SPL)

**Basic Searches:**

```spl
# Simple search
index=main error

# Time range
index=main error earliest=-1h

# Field filtering
index=main status=500

# Multiple conditions
index=main status=500 OR status=404

# Field extraction
index=main | stats count by status

# Aggregation
index=main | stats avg(response_time) by service

# Timechart
index=main | timechart count by status
```

**Advanced Searches:**

```spl
# Error rate calculation
index=main 
| stats count as total, 
        count(eval(status>=500)) as errors 
| eval error_rate = (errors/total)*100

# Top errors
index=main status>=500 
| stats count by error_message 
| sort -count 
| head 10

# Correlation
index=main 
| transaction user_id maxspan=5m 
| stats count by user_id
```

### Splunk vs ELK Stack

| Feature | Splunk | ELK Stack |
|---------|--------|-----------|
| **License** | Commercial | Open Source |
| **Cost** | Expensive | Free (self-hosted) |
| **Ease of Use** | Easy | Moderate |
| **Search Language** | SPL (powerful) | Query DSL (flexible) |
| **Scalability** | Excellent | Good |
| **Support** | Enterprise | Community |
| **Use Case** | Enterprise | Startups/SMBs |

## Centralized Logging Patterns

### Pattern 1: Direct Shipping

```
Application → Log File → Filebeat → Elasticsearch
```

**Pros:**
- Simple setup
- Low latency
- Direct path

**Cons:**
- No processing
- Limited transformation

### Pattern 2: Log Aggregation

```
Application → Log File → Filebeat → Logstash → Elasticsearch
```

**Pros:**
- Processing capability
- Transformation
- Multiple outputs

**Cons:**
- More components
- Higher latency

### Pattern 3: Sidecar Pattern (Kubernetes)

```
┌─────────────────────────────────────────────────────────┐
│              Kubernetes Sidecar Pattern                  │
└─────────────────────────────────────────────────────────┘

Pod
├─ Application Container
│  └─ Writes logs to stdout/stderr
│
└─ Logging Sidecar Container
   └─ Filebeat/Fluentd
      └─ Ships logs to central system
```

## Structured Logging

### Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Structured Logging Benefits                 │
└─────────────────────────────────────────────────────────┘

├─ Searchable
│  └─ Query by fields, not text parsing
│
├─ Parseable
│  └─ Easy to extract and analyze
│
├─ Consistent
│  └─ Standard format across services
│
└─ Machine-Readable
   └─ Easy to process and aggregate
```

### JSON Logging Example

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "ERROR",
  "service": "payment-service",
  "trace_id": "abc123",
  "span_id": "def456",
  "message": "Payment processing failed",
  "error": {
    "type": "PaymentGatewayException",
    "message": "Connection timeout",
    "stack_trace": "..."
  },
  "context": {
    "user_id": "12345",
    "order_id": "67890",
    "amount": 99.99
  }
}
```

### Log Levels

```
┌─────────────────────────────────────────────────────────┐
│              Log Level Hierarchy                        │
└─────────────────────────────────────────────────────────┘

TRACE  ← Most verbose (development)
  │
  ▼
DEBUG  ← Debug information
  │
  ▼
INFO   ← General information (default)
  │
  ▼
WARN   ← Warning messages
  │
  ▼
ERROR  ← Error conditions
  │
  ▼
FATAL  ← Critical failures (application stops)
```

## Best Practices

### 1. Structured Logging
- Use JSON format
- Include context fields
- Consistent field names
- Avoid sensitive data

### 2. Log Levels
- Use appropriate levels
- DEBUG for development
- INFO for production
- ERROR for failures

### 3. Context Information
- Include request IDs
- Add user information
- Track correlation IDs
- Add timing information

### 4. Log Retention
- Define retention policies
- Archive old logs
- Balance cost vs. need
- Comply with regulations

### 5. Performance
- Async logging
- Batch shipping
- Avoid logging in hot paths
- Use appropriate log levels

## Summary

Centralized Logging:
- **Purpose**: Aggregate and analyze logs from multiple sources
- **ELK Stack**: Elasticsearch (storage), Logstash (processing), Kibana (visualization)
- **Splunk**: Commercial platform with powerful search
- **Patterns**: Direct shipping, aggregation, sidecar

**Key Components:**
- Log Shippers: Filebeat, Fluentd, Universal Forwarder
- Log Processors: Logstash, Fluentd
- Storage: Elasticsearch, Splunk Indexer
- Visualization: Kibana, Splunk Web

**Best Practices:**
- Structured logging (JSON)
- Appropriate log levels
- Include context information
- Define retention policies
- Optimize for performance
