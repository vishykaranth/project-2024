# Kafka In-Depth: Part 5 - Operations, Monitoring & Best Practices

## Table of Contents
1. [Kafka Operations](#kafka-operations)
2. [Monitoring & Metrics](#monitoring--metrics)
3. [Performance Tuning](#performance-tuning)
4. [Security](#security)
5. [Best Practices](#best-practices)
6. [Troubleshooting](#troubleshooting)
7. [Capacity Planning](#capacity-planning)

---

## Kafka Operations

### Cluster Management

```
┌─────────────────────────────────────────────────────────┐
│         Cluster Management                            │
└─────────────────────────────────────────────────────────┘

Key Operations:
├─ Broker management
├─ Topic management
├─ Partition management
├─ Replication management
└─ Configuration management
```

### Topic Management

```bash
# Create topic
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic orders \
  --partitions 3 \
  --replication-factor 3 \
  --config retention.ms=604800000 \
  --config segment.bytes=1073741824

# List topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Describe topic
kafka-topics.sh --describe \
  --bootstrap-server localhost:9092 \
  --topic orders

# Alter topic
kafka-topics.sh --alter \
  --bootstrap-server localhost:9092 \
  --topic orders \
  --partitions 6

# Delete topic
kafka-topics.sh --delete \
  --bootstrap-server localhost:9092 \
  --topic orders
```

### Partition Management

```
┌─────────────────────────────────────────────────────────┐
│         Partition Operations                          │
└─────────────────────────────────────────────────────────┘

Partition Reassignment:
├─ Move partitions between brokers
├─ Balance load
└─ Handle broker failures

Reassignment Process:
1. Generate reassignment plan
2. Execute reassignment
3. Verify completion
4. Remove old replicas
```

### Partition Reassignment

```bash
# Generate reassignment plan
kafka-reassign-partitions.sh \
  --bootstrap-server localhost:9092 \
  --topics-to-move-json-file topics-to-move.json \
  --broker-list 1,2,3 \
  --generate

# Execute reassignment
kafka-reassign-partitions.sh \
  --bootstrap-server localhost:9092 \
  --reassignment-json-file reassignment.json \
  --execute

# Verify reassignment
kafka-reassign-partitions.sh \
  --bootstrap-server localhost:9092 \
  --reassignment-json-file reassignment.json \
  --verify
```

### Consumer Group Management

```bash
# List consumer groups
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# Describe consumer group
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group order-processors \
  --describe

# Reset offsets
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group order-processors \
  --topic orders \
  --reset-offsets \
  --to-earliest \
  --execute

# Delete consumer group
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group order-processors \
  --delete
```

---

## Monitoring & Metrics

### Key Metrics to Monitor

```
┌─────────────────────────────────────────────────────────┐
│         Key Metrics                                   │
└─────────────────────────────────────────────────────────┘

Broker Metrics:
├─ Request rate (requests/sec)
├─ Request latency (p50, p95, p99)
├─ Network I/O (bytes/sec)
├─ Disk I/O (bytes/sec)
├─ CPU usage (%)
├─ Memory usage (%)
└─ Disk space usage (%)

Topic Metrics:
├─ Message rate (messages/sec)
├─ Byte rate (bytes/sec)
├─ Partition count
├─ Replication factor
├─ Under-replicated partitions
└─ Leader elections

Consumer Metrics:
├─ Lag (messages behind)
├─ Consumption rate (messages/sec)
├─ Fetch latency
└─ Commit rate
```

### JMX Metrics

```
┌─────────────────────────────────────────────────────────┐
│         Important JMX Metrics                        │
└─────────────────────────────────────────────────────────┘

Broker Metrics:
├─ kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec
├─ kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec
├─ kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec
├─ kafka.network:type=RequestMetrics,name=TotalTimeMs
└─ kafka.controller:type=KafkaController,name=ActiveControllerCount

Topic Metrics:
├─ kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec,topic=<topic>
├─ kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=<topic>
└─ kafka.log:type=Log,name=Size,topic=<topic>,partition=<partition>

Consumer Metrics:
├─ kafka.consumer:type=consumer-fetch-manager-metrics,client-id=<client-id>
└─ kafka.consumer:type=consumer-coordinator-metrics,client-id=<client-id>
```

### Monitoring Tools

```
┌─────────────────────────────────────────────────────────┐
│         Monitoring Tools                              │
└─────────────────────────────────────────────────────────┘

1. Kafka Manager / CMAK
   ├─ Web UI for cluster management
   ├─ Topic and consumer group monitoring
   └─ Partition reassignment

2. Confluent Control Center
   ├─ Enterprise monitoring
   ├─ Schema registry UI
   └─ Connect monitoring

3. Prometheus + Grafana
   ├─ Metrics collection
   ├─ Visualization
   └─ Alerting

4. Burrow (LinkedIn)
   ├─ Consumer lag monitoring
   └─ Alerting on lag

5. Kafka Exporter
   ├─ Exports JMX metrics to Prometheus
   └─ Standard Prometheus format
```

### Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka-broker-1:9092']
    metrics_path: /metrics
    scrape_interval: 15s
```

### Grafana Dashboards

```
┌─────────────────────────────────────────────────────────┐
│         Grafana Dashboard Metrics                    │
└─────────────────────────────────────────────────────────┘

Key Panels:
├─ Message rate (messages/sec)
├─ Byte rate (bytes/sec)
├─ Request latency (p50, p95, p99)
├─ Consumer lag
├─ Under-replicated partitions
├─ Disk usage
└─ Network I/O
```

---

## Performance Tuning

### Producer Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Producer Performance Tuning                   │
└─────────────────────────────────────────────────────────┘

Key Settings:

1. Batch Size
   batch.size = 32768 (32KB)
   ├─ Larger batches = higher throughput
   └─ Trade-off: increased latency

2. Linger Time
   linger.ms = 10
   ├─ Wait for batch to fill
   └─ Balance latency vs throughput

3. Compression
   compression.type = snappy
   ├─ Reduces network bandwidth
   └─ CPU overhead

4. Buffer Memory
   buffer.memory = 67108864 (64MB)
   ├─ Total memory for buffering
   └─ Blocks if full

5. Acks
   acks = 1
   ├─ Faster than acks=all
   └─ Less durable

6. Max In-Flight Requests
   max.in.flight.requests.per.connection = 5
   ├─ Parallel requests
   └─ Requires idempotence for > 1
```

### Consumer Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Performance Tuning                   │
└─────────────────────────────────────────────────────────┘

Key Settings:

1. Fetch Size
   fetch.min.bytes = 1024
   fetch.max.bytes = 52428800 (50MB)
   ├─ Larger fetches = better throughput
   └─ More memory usage

2. Fetch Wait Time
   fetch.max.wait.ms = 500
   ├─ Wait for data
   └─ Balance latency vs throughput

3. Max Poll Records
   max.poll.records = 500
   ├─ Process in batches
   └─ Control processing time

4. Session Timeout
   session.timeout.ms = 30000
   ├─ Failure detection
   └─ Balance responsiveness vs stability

5. Heartbeat Interval
   heartbeat.interval.ms = 3000
   ├─ Keep-alive messages
   └─ Should be < session.timeout.ms / 3
```

### Broker Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Broker Performance Tuning                     │
└─────────────────────────────────────────────────────────┘

Key Settings:

1. Number of Replica Fetchers
   num.replica.fetchers = 4
   ├─ Parallel replication
   └─ Higher = faster replication

2. Replica Fetch Size
   replica.fetch.max.bytes = 1048576 (1MB)
   ├─ Larger fetches = better throughput
   └─ More memory usage

3. Socket Send/Receive Buffer
   socket.send.buffer.bytes = 102400
   socket.receive.buffer.bytes = 102400
   ├─ Network buffer size
   └─ Higher = better throughput

4. Number of Network Threads
   num.network.threads = 8
   ├─ Handle network requests
   └─ Scale with CPU cores

5. Number of I/O Threads
   num.io.threads = 8
   ├─ Handle disk I/O
   └─ Scale with disk count
```

### Topic Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Topic Performance Tuning                     │
└─────────────────────────────────────────────────────────┘

Key Settings:

1. Number of Partitions
   ├─ More partitions = more parallelism
   ├─ Limit: ~4000 per broker
   └─ Consider consumer parallelism

2. Segment Size
   segment.bytes = 1073741824 (1GB)
   ├─ Larger segments = fewer files
   └─ Longer retention per segment

3. Segment Time
   segment.ms = 604800000 (7 days)
   ├─ Time-based rolling
   └─ Works with size-based

4. Retention
   retention.ms = 604800000 (7 days)
   retention.bytes = -1 (unlimited)
   ├─ How long to keep messages
   └─ Balance storage vs requirements

5. Compression
   compression.type = producer
   ├─ Use producer compression
   └─ Or broker compression (snappy, lz4)
```

---

## Security

### Security Features

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Security Features                      │
└─────────────────────────────────────────────────────────┘

1. Authentication
   ├─ SASL (PLAIN, SCRAM, GSSAPI)
   ├─ SSL/TLS client certificates
   └─ OAuth

2. Authorization
   ├─ ACLs (Access Control Lists)
   ├─ RBAC (Role-Based Access Control)
   └─ Custom authorizers

3. Encryption
   ├─ Encryption in transit (SSL/TLS)
   ├─ Encryption at rest (external)
   └─ End-to-end encryption

4. Audit Logging
   ├─ Track access
   └─ Compliance
```

### SASL Authentication

```properties
# Broker configuration
listeners=SASL_SSL://localhost:9093
security.inter.broker.protocol=SASL_SSL
sasl.mechanism.inter.broker.protocol=SCRAM-SHA-256
sasl.enabled.mechanisms=SCRAM-SHA-256

# Client configuration
security.protocol=SASL_SSL
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required \
  username="admin" \
  password="admin-secret";
```

### ACL Configuration

```bash
# Create ACL
kafka-acls.sh \
  --bootstrap-server localhost:9092 \
  --add \
  --allow-principal User:producer \
  --operation Write \
  --topic orders

# List ACLs
kafka-acls.sh \
  --bootstrap-server localhost:9092 \
  --list

# Delete ACL
kafka-acls.sh \
  --bootstrap-server localhost:9092 \
  --remove \
  --allow-principal User:producer \
  --operation Write \
  --topic orders
```

### SSL/TLS Configuration

```properties
# Broker SSL configuration
listeners=SSL://localhost:9093
ssl.keystore.location=/var/private/ssl/kafka.server.keystore.jks
ssl.keystore.password=test1234
ssl.key.password=test1234
ssl.truststore.location=/var/private/ssl/kafka.server.truststore.jks
ssl.truststore.password=test1234

# Client SSL configuration
security.protocol=SSL
ssl.truststore.location=/var/private/ssl/kafka.client.truststore.jks
ssl.truststore.password=test1234
```

---

## Best Practices

### Topic Design

```
┌─────────────────────────────────────────────────────────┐
│         Topic Design Best Practices                   │
└─────────────────────────────────────────────────────────┘

1. Naming Conventions
   ├─ Use descriptive names
   ├─ Include domain/team
   └─ Example: orders-events, user-actions

2. Partition Count
   ├─ Start with 3-6 partitions
   ├─ Scale based on throughput
   └─ Consider consumer parallelism

3. Replication Factor
   ├─ Minimum 3 for production
   ├─ Higher for critical data
   └─ Balance durability vs cost

4. Retention Policy
   ├─ Set based on requirements
   ├─ Consider compliance
   └─ Monitor disk usage

5. Compression
   ├─ Enable compression
   ├─ Use snappy or lz4
   └─ Balance CPU vs storage
```

### Producer Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Producer Best Practices                       │
└─────────────────────────────────────────────────────────┘

1. Use Idempotent Producer
   ├─ Enable idempotence
   ├─ Prevents duplicates
   └─ Required for exactly-once

2. Batch Messages
   ├─ Use batching
   ├─ Tune batch.size and linger.ms
   └─ Balance latency vs throughput

3. Handle Errors
   ├─ Implement retries
   ├─ Handle retriable vs non-retriable
   └─ Log failures

4. Use Async Sends
   ├─ Better throughput
   ├─ Use callbacks for errors
   └─ Monitor completion

5. Monitor Producer Metrics
   ├─ Record send rate
   ├─ Error rate
   └─ Latency
```

### Consumer Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Best Practices                      │
└─────────────────────────────────────────────────────────┘

1. Commit Offsets Carefully
   ├─ Commit after processing
   ├─ Handle failures
   └─ Use manual commits for exactly-once

2. Handle Rebalancing
   ├─ Implement rebalance listeners
   ├─ Save state on partition revocation
   └─ Restore state on assignment

3. Monitor Consumer Lag
   ├─ Track lag metrics
   ├─ Alert on high lag
   └─ Scale consumers if needed

4. Process in Batches
   ├─ Use max.poll.records
   ├─ Process efficiently
   └─ Control processing time

5. Handle Duplicates
   ├─ Idempotent processing
   ├─ Handle at-least-once semantics
   └─ Use exactly-once when needed
```

### Cluster Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Cluster Best Practices                       │
└─────────────────────────────────────────────────────────┘

1. Broker Configuration
   ├─ Use multiple brokers (3+)
   ├─ Distribute across racks
   └─ Monitor broker health

2. Replication
   ├─ Use replication factor 3
   ├─ Monitor under-replicated partitions
   └─ Ensure ISR health

3. Zookeeper/KRaft
   ├─ Use odd number of nodes (3, 5, 7)
   ├─ Monitor health
   └─ Backup configuration

4. Monitoring
   ├─ Comprehensive monitoring
   ├─ Alert on critical metrics
   └─ Regular health checks

5. Capacity Planning
   ├─ Plan for growth
   ├─ Monitor disk usage
   └─ Scale proactively
```

---

## Troubleshooting

### Common Issues

```
┌─────────────────────────────────────────────────────────┐
│         Common Issues & Solutions                     │
└─────────────────────────────────────────────────────────┘

1. High Consumer Lag
   Causes:
   ├─ Slow processing
   ├─ Insufficient consumers
   └─ Network issues
   
   Solutions:
   ├─ Increase consumers
   ├─ Optimize processing
   └─ Check network

2. Under-Replicated Partitions
   Causes:
   ├─ Broker failures
   ├─ Network issues
   └─ Disk failures
   
   Solutions:
   ├─ Check broker health
   ├─ Verify network
   └─ Replace failed brokers

3. Leader Elections
   Causes:
   ├─ Broker failures
   ├─ Network partitions
   └─ Zookeeper issues
   
   Solutions:
   ├─ Investigate broker failures
   ├─ Check network connectivity
   └─ Verify Zookeeper health

4. Disk Space Issues
   Causes:
   ├─ High retention
   ├─ High message rate
   └─ Insufficient disk
   
   Solutions:
   ├─ Reduce retention
   ├─ Add more disk
   └─ Archive old data
```

### Diagnostic Commands

```bash
# Check consumer lag
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group order-processors \
  --describe

# Check under-replicated partitions
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --under-replicated-partitions

# Check broker logs
tail -f /var/log/kafka/server.log

# Check Zookeeper
echo stat | nc localhost 2181

# Check disk usage
df -h /var/kafka-logs
```

---

## Capacity Planning

### Capacity Planning Factors

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Planning                            │
└─────────────────────────────────────────────────────────┘

Key Factors:
├─ Message rate (messages/sec)
├─ Message size (bytes)
├─ Retention period (time)
├─ Replication factor
└─ Number of topics/partitions

Storage Calculation:
Storage = (Message Rate × Message Size × Retention Time × Replication Factor)

Example:
- 1M messages/sec
- 1KB per message
- 7 days retention
- Replication factor 3

Storage = 1M × 1KB × 604800s × 3
        = 1.8 TB
```

### Scaling Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Strategies                           │
└─────────────────────────────────────────────────────────┘

1. Horizontal Scaling
   ├─ Add more brokers
   ├─ Redistribute partitions
   └─ Scale consumers

2. Vertical Scaling
   ├─ Increase broker resources
   ├─ More CPU, memory, disk
   └─ Limited by hardware

3. Partition Scaling
   ├─ Increase partition count
   ├─ More parallelism
   └─ Consider consumer count

4. Consumer Scaling
   ├─ Add more consumers
   ├─ Better parallelism
   └─ Up to partition count
```

---

## Summary

### Key Takeaways

1. **Operations**: Proper topic, partition, and consumer group management
2. **Monitoring**: Comprehensive metrics for brokers, topics, and consumers
3. **Performance**: Tune producers, consumers, brokers, and topics
4. **Security**: Authentication, authorization, and encryption
5. **Best Practices**: Follow guidelines for topics, producers, consumers, and clusters
6. **Troubleshooting**: Common issues and diagnostic approaches
7. **Capacity Planning**: Plan for growth and scale appropriately

### Complete Kafka Knowledge

You now have comprehensive knowledge of:
- Kafka fundamentals and architecture
- Storage and replication internals
- Producers and consumers
- Stream processing and advanced features
- Operations, monitoring, and best practices

This knowledge enables you to:
- Design Kafka-based systems
- Implement producers and consumers
- Build stream processing applications
- Operate and monitor Kafka clusters
- Troubleshoot issues
- Plan for scale
