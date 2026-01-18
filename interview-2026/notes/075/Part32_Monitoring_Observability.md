# Part 32: Monitoring & Observability - Quick Revision

## Three Pillars

- **Logs**: Event records, debugging, audit trails; structured logging (JSON)
- **Metrics**: Numerical measurements, time-series data; counters, gauges, histograms
- **Traces**: Request flow across services; distributed tracing, span correlation

## Monitoring Tools

- **Prometheus**: Metrics collection, time-series database, PromQL query language
- **Grafana**: Visualization, dashboards, alerting, supports multiple data sources
- **ELK Stack**: Elasticsearch (search), Logstash (processing), Kibana (visualization)
- **Jaeger/Zipkin**: Distributed tracing, request flow visualization

## Key Metrics

- **Latency**: Response time (p50, p95, p99 percentiles)
- **Throughput**: Requests per second (RPS)
- **Error Rate**: Percentage of failed requests
- **Availability**: Uptime percentage (99.9% = 8.76hrs downtime/year)
- **Resource Usage**: CPU, memory, disk, network utilization

## Alerting

- **Threshold-Based**: Alert when metric exceeds threshold
- **Anomaly Detection**: Detect unusual patterns, machine learning-based
- **Alert Fatigue**: Avoid too many alerts, prioritize critical, use alerting rules

## APM (Application Performance Monitoring)

- **Purpose**: Monitor application performance, identify bottlenecks
- **Features**: Code-level insights, database query analysis, transaction tracing
- **Tools**: New Relic, Datadog, AppDynamics, Elastic APM
