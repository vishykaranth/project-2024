# Observability Logs: Structured Logging, Log Aggregation, Search

## Overview

Logs are one of the three pillars of observability (Metrics, Logs, Traces). They provide detailed, timestamped records of events that occur in a system. Structured logging, log aggregation, and search capabilities are essential for effective observability in distributed systems.

## Logs in Observability

```
┌─────────────────────────────────────────────────────────┐
│              Logs Role in Observability                 │
└─────────────────────────────────────────────────────────┘

Logs provide:
├─ Event Records
│  └─ What happened and when
│
├─ Context Information
│  └─ Who, what, where, why
│
├─ Debugging Information
│  └─ Stack traces, error details
│
└─ Audit Trail
   └─ Compliance and security
```

## Structured Logging

### What is Structured Logging?

Structured logging uses a consistent, machine-readable format (typically JSON) instead of free-form text. This enables easier parsing, searching, and analysis.

```
┌─────────────────────────────────────────────────────────┐
│              Unstructured vs Structured                 │
└─────────────────────────────────────────────────────────┘

Unstructured:
"2024-01-15 10:30:00 ERROR User login failed for user@example.com"

Structured (JSON):
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "ERROR",
  "message": "User login failed",
  "user": "user@example.com",
  "service": "auth-service",
  "trace_id": "abc123"
}
```

### Structured Log Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Structured Log Benefits                   │
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
├─ Machine-Readable
│  └─ Easy to process and aggregate
│
└─ Filterable
   └─ Filter by any field
```

### Structured Log Format

**JSON Example:**

```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "ERROR",
  "logger": "com.example.AuthService",
  "message": "Authentication failed",
  "service": "auth-service",
  "version": "1.2.3",
  "environment": "production",
  "trace_id": "abc123def456",
  "span_id": "ghi789",
  "user_id": "user123",
  "ip_address": "192.168.1.100",
  "error": {
    "type": "AuthenticationException",
    "message": "Invalid credentials",
    "stack_trace": "..."
  },
  "context": {
    "request_id": "req-123",
    "session_id": "sess-456",
    "correlation_id": "corr-789"
  }
}
```

## Log Levels

```
┌─────────────────────────────────────────────────────────┐
│              Log Level Hierarchy                        │
└─────────────────────────────────────────────────────────┘

TRACE  ← Most verbose (development only)
  │
  ▼
DEBUG  ← Debug information (development)
  │
  ▼
INFO   ← General information (production default)
  │
  ▼
WARN   ← Warning messages (needs attention)
  │
  ▼
ERROR  ← Error conditions (needs investigation)
  │
  ▼
FATAL  ← Critical failures (application stops)
```

### Log Level Usage

**TRACE:**
- Very detailed information
- Usually only in development
- Function entry/exit
- Variable values

**DEBUG:**
- Detailed information for debugging
- Development and troubleshooting
- Not typically in production

**INFO:**
- General informational messages
- Normal application flow
- Business events
- Production default

**WARN:**
- Warning messages
- Something unexpected but handled
- Deprecated features
- Performance concerns

**ERROR:**
- Error conditions
- Exceptions caught
- Failed operations
- Needs investigation

**FATAL:**
- Critical failures
- Application cannot continue
- System-level errors
- Requires immediate attention

## Log Aggregation

### What is Log Aggregation?

Log aggregation collects logs from multiple sources into a central location for analysis, search, and monitoring.

```
┌─────────────────────────────────────────────────────────┐
│              Log Aggregation Flow                       │
└─────────────────────────────────────────────────────────┘

Service A ──┐
            │
Service B ──┼──► Log Shipper ──► Log Aggregator ──► Storage
            │      (Filebeat)      (Logstash)      (Elasticsearch)
Service C ──┘
```

### Aggregation Patterns

**Pattern 1: Direct Shipping**
```
Application → Log File → Filebeat → Elasticsearch
```

**Pattern 2: Centralized Processing**
```
Application → Log File → Filebeat → Logstash → Elasticsearch
```

**Pattern 3: Message Queue**
```
Application → Log File → Filebeat → Kafka → Logstash → Elasticsearch
```

## Log Search

### Search Capabilities

```
┌─────────────────────────────────────────────────────────┐
│              Log Search Features                        │
└─────────────────────────────────────────────────────────┘

├─ Full-Text Search
│  └─ Search across all log content
│
├─ Field-Based Search
│  └─ Search specific fields
│
├─ Time-Range Search
│  └─ Filter by timestamp
│
├─ Boolean Queries
│  └─ AND, OR, NOT operations
│
├─ Regex Search
│  └─ Pattern matching
│
└─ Aggregation
   └─ Group, count, analyze
```

### Search Examples

**Elasticsearch Query:**

```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "level": "ERROR" }},
        { "range": { "timestamp": { "gte": "now-1h" }}}
      ],
      "filter": [
        { "term": { "service": "api-service" }}
      ]
    }
  },
  "sort": [{ "timestamp": "desc" }],
  "size": 100
}
```

**Kibana Query:**

```
level:ERROR AND service:api-service AND timestamp:[now-1h TO now]
```

**Splunk Query:**

```
index=main level=ERROR service=api-service earliest=-1h
| stats count by error_type
| sort -count
```

## Log Context

### Contextual Information

Effective logs include context that helps understand what happened:

```
┌─────────────────────────────────────────────────────────┐
│              Log Context Fields                         │
└─────────────────────────────────────────────────────────┘

├─ Request Context
│  ├─ request_id
│  ├─ user_id
│  ├─ session_id
│  └─ correlation_id
│
├─ Service Context
│  ├─ service_name
│  ├─ service_version
│  ├─ instance_id
│  └─ environment
│
├─ Trace Context
│  ├─ trace_id
│  ├─ span_id
│  └─ parent_span_id
│
└─ Business Context
   ├─ transaction_id
   ├─ order_id
   └─ customer_id
```

### Correlation IDs

Correlation IDs help track requests across services:

```
┌─────────────────────────────────────────────────────────┐
│              Correlation ID Flow                        │
└─────────────────────────────────────────────────────────┘

Request arrives with correlation_id: abc123
    │
    ├─► API Gateway (logs with abc123)
    │
    ├─► Auth Service (logs with abc123)
    │
    ├─► Payment Service (logs with abc123)
    │
    └─► Notification Service (logs with abc123)

All logs can be searched by correlation_id: abc123
```

## Log Retention

### Retention Policies

```
┌─────────────────────────────────────────────────────────┐
│              Log Retention Strategy                     │
└─────────────────────────────────────────────────────────┘

Hot Storage (Recent):
├─ Duration: 7-30 days
├─ Fast access
└─ Full search capability

Warm Storage (Older):
├─ Duration: 30-90 days
├─ Slower access
└─ Limited search

Cold Storage (Archive):
├─ Duration: 90+ days
├─ Very slow access
└─ Compliance/audit only
```

### Retention Configuration

**Elasticsearch Index Lifecycle:**

```json
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "7d"
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "allocate": {
            "number_of_replicas": 0
          }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

## Log Analysis

### Common Analysis Patterns

**1. Error Analysis:**
```
Search: level:ERROR
Group by: error_type
Count: errors per type
Time range: last 24 hours
```

**2. Performance Analysis:**
```
Search: duration_ms > 1000
Group by: service, endpoint
Aggregate: avg, p95, p99
Time range: last hour
```

**3. User Activity:**
```
Search: event:user_action
Group by: user_id, action_type
Count: actions per user
Time range: last 7 days
```

### Log Aggregation Queries

**Error Rate by Service:**

```json
{
  "size": 0,
  "aggs": {
    "errors_by_service": {
      "terms": {
        "field": "service.keyword"
      },
      "aggs": {
        "error_count": {
          "filter": {
            "term": { "level": "ERROR" }
          }
        }
      }
    }
  }
}
```

## Best Practices

### 1. Structured Format
- Use JSON format
- Consistent field names
- Include timestamps
- Add context fields

### 2. Appropriate Log Levels
- Use INFO for normal flow
- Use ERROR for failures
- Use DEBUG sparingly in production
- Use WARN for warnings

### 3. Include Context
- Add correlation IDs
- Include user information
- Add request IDs
- Include service information

### 4. Avoid Sensitive Data
- Don't log passwords
- Don't log credit cards
- Don't log SSNs
- Mask sensitive fields

### 5. Performance
- Use async logging
- Batch log shipping
- Avoid logging in hot paths
- Use appropriate log levels

## Summary

Observability Logs:
- **Purpose**: Detailed, timestamped records of system events
- **Format**: Structured logging (JSON) for better analysis
- **Aggregation**: Centralized collection from multiple sources
- **Search**: Powerful query capabilities for analysis

**Key Concepts:**
- Structured Logging: Machine-readable format
- Log Levels: TRACE, DEBUG, INFO, WARN, ERROR, FATAL
- Log Aggregation: Centralized collection
- Context: Correlation IDs, request IDs, trace IDs
- Retention: Hot, warm, cold storage strategies

**Best Practices:**
- Use structured format (JSON)
- Appropriate log levels
- Include context information
- Avoid sensitive data
- Optimize for performance
