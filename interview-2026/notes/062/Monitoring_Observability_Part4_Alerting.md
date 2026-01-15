# Alerting: Alert Rules, Notification Channels, Escalation

## Overview

Alerting is the process of automatically detecting anomalies, threshold violations, or critical conditions in a system and notifying relevant stakeholders. Effective alerting ensures that teams are informed about issues promptly, enabling quick response and resolution.

## Alerting Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Alerting System Architecture               │
└─────────────────────────────────────────────────────────┘

Metrics/Logs/Traces
    │
    ▼
┌──────────────┐
│  Monitoring  │  ← Collects data
│  System      │
└──────┬───────┘
       │
       │ (Evaluates)
       │
       ▼
┌──────────────┐
│  Alert Rules │  ← Defines conditions
│  Engine      │
└──────┬───────┘
       │
       │ (Triggers)
       │
       ▼
┌──────────────┐
│  Alert       │  ← Manages alert state
│  Manager     │
└──────┬───────┘
       │
       │ (Routes)
       │
       ▼
┌──────────────┐
│ Notification │  ← Sends notifications
│  Channels    │
└──────┬───────┘
       │
       │ (Delivers)
       │
    ┌──┴──┐
    │     │
    ▼     ▼
┌─────┐ ┌─────┐
│Email│ │Slack│ │PagerDuty│ │SMS│
└─────┘ └─────┘ └─────────┘ └───┘
```

## Alert Rules

### Alert Rule Structure

```
┌─────────────────────────────────────────────────────────┐
│              Alert Rule Components                       │
└─────────────────────────────────────────────────────────┘

Alert Rule:
├─ Name: Descriptive alert name
├─ Condition: When to trigger
├─ Threshold: Value that triggers alert
├─ Duration: How long condition must persist
├─ Severity: Critical, Warning, Info
├─ Labels: Metadata for routing
└─ Annotations: Human-readable information
```

### Prometheus Alert Rules

**Example Alert Rule:**

```yaml
# alerts.yml
groups:
  - name: api_errors
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors/sec (threshold: 0.05)"
      
      - alert: HighLatency
        expr: histogram_quantile(0.95, 
              rate(http_request_duration_seconds_bucket[5m])) > 1.0
        for: 10m
        labels:
          severity: warning
          team: backend
        annotations:
          summary: "High latency detected"
          description: "95th percentile latency is {{ $value }}s (threshold: 1.0s)"
      
      - alert: ServiceDown
        expr: up{job="api-service"} == 0
        for: 1m
        labels:
          severity: critical
          team: infrastructure
        annotations:
          summary: "Service is down"
          description: "{{ $labels.instance }} is down"
```

### Alert Rule Types

**1. Threshold Alerts**
```yaml
# CPU usage above 80%
- alert: HighCPUUsage
  expr: cpu_usage_percent > 80
  for: 5m
```

**2. Rate Alerts**
```yaml
# Error rate increasing
- alert: IncreasingErrorRate
  expr: rate(http_errors_total[5m]) > 0.1
  for: 5m
```

**3. Absence Alerts**
```yaml
# No metrics received
- alert: NoMetricsReceived
  expr: absent(up{job="api-service"})
  for: 5m
```

**4. Anomaly Alerts**
```yaml
# Unusual pattern
- alert: AnomalousTraffic
  expr: http_requests_total > (avg_over_time(http_requests_total[1h]) * 2)
  for: 10m
```

## Alert States

```
┌─────────────────────────────────────────────────────────┐
│              Alert State Machine                        │
└─────────────────────────────────────────────────────────┘

Inactive
    │
    │ (Condition met)
    │
    ▼
Pending
    │
    │ (Duration elapsed)
    │
    ▼
Firing
    │
    │ (Condition no longer met)
    │
    ▼
Resolved
    │
    │ (Back to normal)
    │
    └───► Inactive
```

### Alert States Explained

**1. Inactive**
- Condition not met
- No alert triggered
- Normal state

**2. Pending**
- Condition met
- Waiting for duration
- Not yet firing

**3. Firing**
- Condition met for duration
- Alert active
- Notifications sent

**4. Resolved**
- Condition no longer met
- Alert cleared
- Resolution notification sent

## Notification Channels

### Channel Types

```
┌─────────────────────────────────────────────────────────┐
│              Notification Channels                      │
└─────────────────────────────────────────────────────────┘

├─ Email
│  ├─ Pros: Universal, detailed
│  └─ Cons: Can be ignored, delayed
│
├─ Slack/Teams
│  ├─ Pros: Real-time, team visibility
│  └─ Cons: Can be noisy
│
├─ PagerDuty/Opsgenie
│  ├─ Pros: On-call management, escalation
│  └─ Cons: Cost, complexity
│
├─ SMS
│  ├─ Pros: Immediate, reliable
│  └─ Cons: Cost, limited content
│
├─ Webhooks
│  ├─ Pros: Flexible, custom integrations
│  └─ Cons: Requires development
│
└─ Phone Calls
   ├─ Pros: Guaranteed delivery
   └─ Cons: Intrusive, expensive
```

### Alertmanager Configuration

```yaml
# alertmanager.yml
global:
  resolve_timeout: 5m
  slack_api_url: 'https://hooks.slack.com/services/...'

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default'
  routes:
    - match:
        severity: critical
      receiver: 'critical-alerts'
      continue: true
    
    - match:
        severity: warning
      receiver: 'warning-alerts'

receivers:
  - name: 'default'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: 'Alert: {{ .GroupLabels.alertname }}'
  
  - name: 'critical-alerts'
    pagerduty_configs:
      - service_key: 'xxx'
        description: '{{ .GroupLabels.alertname }}'
    
    slack_configs:
      - channel: '#alerts-critical'
        title: '🚨 Critical Alert'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
  
  - name: 'warning-alerts'
    slack_configs:
      - channel: '#alerts-warning'
        title: '⚠️ Warning'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

## Alert Routing

### Routing Rules

```
┌─────────────────────────────────────────────────────────┐
│              Alert Routing Logic                        │
└─────────────────────────────────────────────────────────┘

Alert Fired
    │
    ▼
Check Labels
    │
    ├─► severity: critical
    │   └─► Route to PagerDuty + Slack
    │
    ├─► severity: warning
    │   └─► Route to Slack
    │
    ├─► team: backend
    │   └─► Route to Backend Team
    │
    └─► team: infrastructure
        └─► Route to DevOps Team
```

### Routing Configuration

```yaml
route:
  routes:
    # Critical alerts go to on-call
    - match:
        severity: critical
      receiver: on-call
      continue: false
    
    # Backend team alerts
    - match:
        team: backend
      receiver: backend-team
    
    # Infrastructure alerts
    - match:
        team: infrastructure
      receiver: devops-team
    
    # Default route
    - receiver: default
```

## Alert Grouping

### Grouping Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Alert Grouping                             │
└─────────────────────────────────────────────────────────┘

Without Grouping:
├─ Alert: HighCPU on server1
├─ Alert: HighCPU on server2
├─ Alert: HighCPU on server3
└─ Alert: HighCPU on server4
   → 4 separate notifications

With Grouping:
└─ Alert: HighCPU (4 instances)
   ├─ server1
   ├─ server2
   ├─ server3
   └─ server4
   → 1 grouped notification
```

### Grouping Configuration

```yaml
route:
  group_by: ['alertname', 'cluster']
  group_wait: 10s      # Wait before sending
  group_interval: 5m    # Wait between groups
  repeat_interval: 12h  # Repeat if still firing
```

## Escalation Policies

### Escalation Chain

```
┌─────────────────────────────────────────────────────────┐
│              Escalation Policy                          │
└─────────────────────────────────────────────────────────┘

Level 1: On-Call Engineer (0-15 min)
    │
    │ (No acknowledgment)
    │
    ▼
Level 2: Team Lead (15-30 min)
    │
    │ (No acknowledgment)
    │
    ▼
Level 3: Engineering Manager (30-60 min)
    │
    │ (No acknowledgment)
    │
    ▼
Level 4: CTO (60+ min)
```

### PagerDuty Escalation

```json
{
  "escalation_policy": {
    "name": "Production Escalation",
    "escalation_rules": [
      {
        "escalation_delay_in_minutes": 0,
        "targets": [
          {
            "type": "user",
            "id": "on-call-engineer-id"
          }
        ]
      },
      {
        "escalation_delay_in_minutes": 15,
        "targets": [
          {
            "type": "user",
            "id": "team-lead-id"
          }
        ]
      },
      {
        "escalation_delay_in_minutes": 30,
        "targets": [
          {
            "type": "user",
            "id": "engineering-manager-id"
          }
        ]
      }
    ]
  }
}
```

## Alert Fatigue Prevention

### Strategies

```
┌─────────────────────────────────────────────────────────┐
│              Preventing Alert Fatigue                   │
└─────────────────────────────────────────────────────────┘

├─ Set Appropriate Thresholds
│  └─ Don't alert on every minor issue
│
├─ Use Alert Severity
│  ├─ Critical: Immediate action needed
│  ├─ Warning: Attention needed
│  └─ Info: Informational only
│
├─ Implement Alert Suppression
│  └─ Suppress during maintenance
│
├─ Group Related Alerts
│  └─ Reduce notification volume
│
├─ Use Alert Routing
│  └─ Route to right people
│
└─ Regular Alert Review
   └─ Remove unnecessary alerts
```

### Alert Suppression

```yaml
# Suppress alerts during maintenance
inhibit_rules:
  - source_match:
      severity: 'maintenance'
    target_match:
      severity: 'critical'
    equal: ['alertname', 'instance']
```

## Best Practices

### 1. Alert Naming
- Use descriptive names
- Include context
- Example: "HighErrorRate-API-Service"

### 2. Thresholds
- Set based on SLOs
- Use percentiles (p95, p99)
- Consider business impact

### 3. Duration
- Avoid instant alerts
- Use appropriate wait times
- Prevent false positives

### 4. Runbooks
- Document alert responses
- Include troubleshooting steps
- Link from alert annotations

### 5. Testing
- Test alert rules
- Verify notifications
- Validate escalation

## Alert Examples

### Infrastructure Alerts

```yaml
# High CPU usage
- alert: HighCPUUsage
  expr: cpu_usage_percent > 80
  for: 5m
  annotations:
    summary: "High CPU usage on {{ $labels.instance }}"
    runbook_url: "https://wiki/runbooks/high-cpu"

# Disk space low
- alert: LowDiskSpace
  expr: (node_filesystem_avail_bytes / node_filesystem_size_bytes) < 0.1
  for: 10m
  annotations:
    summary: "Disk space below 10% on {{ $labels.instance }}"
```

### Application Alerts

```yaml
# High error rate
- alert: HighErrorRate
  expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
  for: 5m
  annotations:
    summary: "Error rate above 5% for {{ $labels.service }}"

# High latency
- alert: HighLatency
  expr: histogram_quantile(0.95, 
        rate(http_request_duration_seconds_bucket[5m])) > 1.0
  for: 10m
  annotations:
    summary: "95th percentile latency above 1s"
```

### Business Alerts

```yaml
# Low transaction volume
- alert: LowTransactionVolume
  expr: rate(transactions_total[1h]) < 100
  for: 30m
  annotations:
    summary: "Transaction volume below expected threshold"
```

## Summary

Alerting:
- **Purpose**: Detect and notify about issues automatically
- **Components**: Alert rules, notification channels, escalation
- **States**: Inactive → Pending → Firing → Resolved
- **Tools**: Prometheus Alertmanager, PagerDuty, Opsgenie

**Key Concepts:**
- Alert Rules: Define when to alert
- Notification Channels: How to notify
- Escalation: When to escalate
- Grouping: Reduce noise
- Suppression: Prevent fatigue

**Best Practices:**
- Set appropriate thresholds
- Use severity levels
- Implement escalation
- Group related alerts
- Regular review and cleanup
