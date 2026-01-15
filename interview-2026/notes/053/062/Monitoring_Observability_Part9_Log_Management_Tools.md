# Log Management Tools: ELK, Splunk, CloudWatch Logs

## Overview

Log Management Tools provide centralized collection, storage, search, and analysis of logs from multiple sources. They are essential for troubleshooting, security monitoring, compliance, and gaining insights from application and infrastructure logs.

## Log Management Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Log Management Architecture                │
└─────────────────────────────────────────────────────────┘

Applications/Services
    │
    │ (Logs)
    │
    ▼
┌──────────────┐
│  Log         │  ← Collects logs
│  Collectors  │
└──────┬───────┘
       │
       │ (Processed logs)
       │
       ▼
┌──────────────┐
│  Log         │  ← Stores and indexes
│  Storage     │
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  Log         │  ← Search and analyze
│  Platform    │
└──────┬───────┘
       │
       │ (Visualization)
       │
       ▼
┌──────────────┐
│  Log UI      │  ← Dashboards, search
└──────────────┘
```

## ELK Stack

### Overview

ELK Stack (Elasticsearch, Logstash, Kibana) is a popular open-source log management solution. It's now often called Elastic Stack and may include Beats for log shipping.

### ELK Stack Components

**1. Elasticsearch**
- Distributed search and analytics engine
- Stores and indexes logs
- Provides RESTful API
- Horizontal scalability

**2. Logstash**
- Server-side data processing pipeline
- Ingests, transforms, and outputs logs
- Rich plugin ecosystem
- Flexible configuration

**3. Kibana**
- Visualization and exploration tool
- Web-based interface
- Dashboards and visualizations
- Query builder

**4. Beats (Filebeat)**
- Lightweight log shippers
- Forward logs to Logstash/Elasticsearch
- Low resource usage
- Easy to deploy

### ELK Stack Architecture

```
┌─────────────────────────────────────────────────────────┐
│              ELK Stack Flow                             │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Log files)
    │
    ▼
┌──────────────┐
│  Filebeat    │  ← Ships logs
└──────┬───────┘
       │
       │ (Raw logs)
       │
       ▼
┌──────────────┐
│  Logstash    │  ← Processes logs
└──────┬───────┘
       │
       │ (Structured logs)
       │
       ▼
┌──────────────┐
│ Elasticsearch│  ← Stores logs
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│   Kibana     │  ← Visualizes logs
└──────────────┘
```

### ELK Stack Configuration

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
  json {
    source => "message"
  }
  
  date {
    match => [ "timestamp", "ISO8601" ]
  }
  
  mutate {
    remove_field => [ "password" ]
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "logs-%{+YYYY.MM.dd}"
  }
}
```

**Kibana Query:**

```
level:ERROR AND service:api-service AND timestamp:[now-1h TO now]
```

## Splunk

### Overview

Splunk is a commercial platform for searching, monitoring, and analyzing machine-generated data. It's known for its powerful search language (SPL) and enterprise features.

### Splunk Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Splunk Architecture                        │
└─────────────────────────────────────────────────────────┘

Applications
    │
    │ (Logs)
    │
    ▼
┌──────────────┐
│  Universal   │  ← Forwards logs
│  Forwarder   │
└──────┬───────┘
       │
       │ (Indexed logs)
       │
       ▼
┌──────────────┐
│  Indexer     │  ← Stores logs
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  Search Head │  ← Queries logs
└──────┬───────┘
       │
       │ (UI)
       │
       ▼
┌──────────────┐
│  Splunk Web  │  ← Visualizes logs
└──────────────┘
```

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

# Aggregation
index=main | stats count by status

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

### Splunk Features

**1. Search:**
- Powerful SPL language
- Fast search performance
- Real-time search
- Saved searches

**2. Dashboards:**
- Custom dashboards
- Visualizations
- Real-time updates
- Sharing

**3. Alerts:**
- Alert rules
- Notification channels
- Escalation policies
- Alert actions

**4. Reports:**
- Scheduled reports
- Email delivery
- PDF export
- Custom formats

## AWS CloudWatch Logs

### Overview

CloudWatch Logs is AWS's log management service that provides centralized logging for AWS resources and applications.

### CloudWatch Logs Architecture

```
┌─────────────────────────────────────────────────────────┐
│              CloudWatch Logs Architecture               │
└─────────────────────────────────────────────────────────┘

AWS Resources/Applications
    │
    │ (Logs)
    │
    ▼
┌──────────────┐
│  CloudWatch   │  ← Collects logs
│  Logs Agent   │
└──────┬───────┘
       │
       │ (Log streams)
       │
       ▼
┌──────────────┐
│  Log Groups  │  ← Organizes logs
│  Log Streams  │
└──────┬───────┘
       │
       │ (Queries)
       │
       ▼
┌──────────────┐
│  CloudWatch    │  ← Queries logs
│  Logs         │
└──────┬───────┘
       │
       │ (Insights)
       │
       ▼
┌──────────────┐
│  CloudWatch  │  ← Visualizes logs
│  Console     │
└──────────────┘
```

### CloudWatch Logs Features

**1. Log Groups and Streams:**
- Log Groups: Container for log streams
- Log Streams: Sequence of log events
- Automatic organization
- Retention policies

**2. CloudWatch Logs Insights:**
- Query language for logs
- Fast queries
- Visualizations
- Saved queries

**3. Metric Filters:**
- Extract metrics from logs
- Create CloudWatch metrics
- Set up alarms
- Monitor trends

**4. Subscription Filters:**
- Stream logs to other services
- Real-time processing
- Lambda functions
- Kinesis streams

### CloudWatch Logs Insights Query

```sql
-- Error count
fields @timestamp, @message
| filter @message like /ERROR/
| stats count() by bin(5m)

-- Top errors
fields @message
| filter @message like /ERROR/
| parse @message /ERROR (?<error_type>\w+)/
| stats count() by error_type
| sort count desc
| limit 10

-- Response time analysis
fields @timestamp, @message
| parse @message /duration=(?<duration>\d+)/
| stats avg(duration), max(duration), min(duration) by bin(1m)
```

### CloudWatch Logs Configuration

**AWS CLI:**

```bash
# Create log group
aws logs create-log-group --log-group-name /aws/lambda/my-function

# Put log events
aws logs put-log-events \
  --log-group-name /aws/lambda/my-function \
  --log-stream-name stream1 \
  --log-events file://events.json

# Query logs
aws logs start-query \
  --log-group-name /aws/lambda/my-function \
  --start-time $(date -u -d '1 hour ago' +%s) \
  --end-time $(date -u +%s) \
  --query-string 'fields @timestamp, @message | filter @message like /ERROR/'
```

## Tool Comparison

| Feature | ELK Stack | Splunk | CloudWatch Logs |
|---------|----------|--------|-----------------|
| **License** | Open Source | Commercial | AWS Service |
| **Cost** | Free (self-hosted) | Expensive | Pay per GB |
| **Ease of Use** | Moderate | Easy | Easy |
| **Search Language** | Query DSL | SPL (powerful) | CloudWatch Insights |
| **Scalability** | Good | Excellent | Excellent |
| **AWS Integration** | Manual | Manual | Native |
| **Use Case** | Startups/SMBs | Enterprise | AWS workloads |

## Best Practices

### 1. Structured Logging
- Use JSON format
- Consistent field names
- Include context
- Avoid sensitive data

### 2. Log Retention
- Define retention policies
- Archive old logs
- Balance cost vs. need
- Comply with regulations

### 3. Indexing Strategy
- Index important fields
- Use appropriate analyzers
- Optimize for search
- Monitor index size

### 4. Query Optimization
- Use specific queries
- Limit time ranges
- Use filters early
- Cache frequent queries

### 5. Cost Management
- Set retention limits
- Use log sampling
- Archive old logs
- Monitor usage

## Summary

Log Management Tools:
- **Purpose**: Centralized collection, storage, and analysis of logs
- **ELK Stack**: Open-source (Elasticsearch, Logstash, Kibana)
- **Splunk**: Commercial platform with powerful SPL
- **CloudWatch Logs**: AWS-native log management

**Key Features:**
- Log Collection: Filebeat, Universal Forwarder, CloudWatch Agent
- Log Processing: Logstash, Splunk Indexer
- Log Storage: Elasticsearch, Splunk Indexer, CloudWatch
- Log Search: Kibana, Splunk Web, CloudWatch Insights

**Best Practices:**
- Use structured logging
- Define retention policies
- Optimize indexing
- Query efficiently
- Manage costs
