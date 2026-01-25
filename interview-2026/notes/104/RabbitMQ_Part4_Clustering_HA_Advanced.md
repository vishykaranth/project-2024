# RabbitMQ In-Depth: Part 4 - Clustering, HA & Advanced Features

## Table of Contents
1. [RabbitMQ Clustering](#rabbitmq-clustering)
2. [High Availability](#high-availability)
3. [Mirrored Queues](#mirrored-queues)
4. [Federation](#federation)
5. [Shovel](#shovel)
6. [Performance Tuning](#performance-tuning)
7. [Monitoring](#monitoring)

---

## RabbitMQ Clustering

### Cluster Overview

```
┌─────────────────────────────────────────────────────────┐
│         RabbitMQ Cluster Architecture                │
└─────────────────────────────────────────────────────────┘

Cluster Components:
├─ Multiple RabbitMQ nodes
├─ Shared Erlang cookie
├─ Shared state
└─ Distributed queues

Benefits:
├─ High availability
├─ Load distribution
├─ Horizontal scaling
└─ Fault tolerance
```

### Cluster Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Cluster Topology                             │
└─────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │   Node 1     │
                    │  (Disk)      │
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Node 2     │  │   Node 3     │  │   Node 4     │
│  (RAM)       │  │  (RAM)       │  │  (RAM)       │
└──────────────┘  └──────────────┘  └──────────────┘

Cluster Types:
├─ Disk Node: Metadata stored on disk
└─ RAM Node: Metadata stored in memory (faster)
```

### Cluster Setup

```bash
# Node 1 (Disk Node)
rabbitmq-server -detached
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app

# Node 2 (Join Cluster)
rabbitmq-server -detached
rabbitmqctl stop_app
rabbitmqctl join_cluster rabbit@node1
rabbitmqctl start_app

# Node 3 (RAM Node)
rabbitmq-server -detached
rabbitmqctl stop_app
rabbitmqctl join_cluster --ram rabbit@node1
rabbitmqctl start_app

# Verify Cluster Status
rabbitmqctl cluster_status
```

### Cluster Operations

```bash
# List cluster nodes
rabbitmqctl cluster_status

# Remove node from cluster
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app

# Change node type
rabbitmqctl stop_app
rabbitmqctl change_cluster_node_type disc  # or ram
rabbitmqctl start_app

# Forget node
rabbitmqctl forget_cluster_node rabbit@old-node
```

---

## High Availability

### HA Overview

```
┌─────────────────────────────────────────────────────────┐
│         High Availability Strategy                   │
└─────────────────────────────────────────────────────────┘

HA Components:
├─ Cluster (multiple nodes)
├─ Mirrored queues (replication)
├─ Load balancer (client access)
└─ Monitoring (health checks)

HA Levels:
├─ No HA: Single node (development)
├─ Basic HA: Cluster with mirrored queues
└─ Full HA: Cluster + LB + monitoring
```

### HA Architecture

```
┌─────────────────────────────────────────────────────────┐
│         HA Architecture                              │
└─────────────────────────────────────────────────────────┘

                    Load Balancer
                    (HAProxy/Nginx)
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Node 1     │  │   Node 2     │  │   Node 3     │
│  (Master)    │  │  (Mirror)    │  │  (Mirror)    │
│              │  │              │  │              │
│ Queue: orders│  │ Queue: orders│  │ Queue: orders│
│ (Master)     │  │ (Mirror)     │  │ (Mirror)     │
└──────────────┘  └──────────────┘  └──────────────┘

Failover:
├─ If Node 1 fails
├─ Node 2 or Node 3 becomes master
└─ Service continues
```

---

## Mirrored Queues

### Queue Mirroring Overview

```
┌─────────────────────────────────────────────────────────┐
│         Queue Mirroring                             │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Replicate queues across nodes
├─ High availability
└─ Fault tolerance

Mirroring Types:
├─ Classic Mirrored Queues (legacy)
└─ Quorum Queues (recommended)
```

### Classic Mirrored Queues

```
┌─────────────────────────────────────────────────────────┐
│         Classic Mirrored Queues                      │
└─────────────────────────────────────────────────────────┘

Queue: "orders"
│
├─ Master: Node 1
│   ├─ Handles all operations
│   └─ Primary copy
│
├─ Mirror: Node 2
│   ├─ Replicates from master
│   └─ Backup copy
│
└─ Mirror: Node 3
    ├─ Replicates from master
    └─ Backup copy

Failover:
├─ If master fails
├─ Oldest mirror becomes master
└─ Automatic promotion
```

### Mirrored Queue Configuration

```python
# Declare mirrored queue
channel.queue_declare(
    queue='orders',
    durable=True,
    arguments={
        'x-ha-policy': 'all'  # Mirror to all nodes
        # OR
        'x-ha-policy': 'nodes',
        'x-ha-nodes': ['rabbit@node1', 'rabbit@node2']
    }
)

# Using policies (recommended)
rabbitmqctl set_policy ha-orders "^orders$" '{"ha-mode":"all"}'
```

### Quorum Queues

```
┌─────────────────────────────────────────────────────────┐
│         Quorum Queues                                 │
└─────────────────────────────────────────────────────────┘

Features:
├─ Raft consensus algorithm
├─ Better performance
├─ Automatic leader election
└─ Recommended for new deployments

Configuration:
channel.queue_declare(
    queue='orders',
    durable=True,
    arguments={
        'x-queue-type': 'quorum'
    }
)
```

### Quorum Queue vs Classic Mirrored

```
┌─────────────────────────────────────────────────────────┐
│         Queue Type Comparison                        │
└─────────────────────────────────────────────────────────┘

                Classic Mirrored    Quorum
─────────────────────────────────────────────────────────
Consensus          Master-based      Raft
Performance          Good            Better
Leader Election    Manual           Automatic
Durability          Good             Excellent
Use Case        Legacy systems    New deployments
```

---

## Federation

### Federation Overview

```
┌─────────────────────────────────────────────────────────┐
│         Federation                                   │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Connect RabbitMQ brokers
├─ Replicate exchanges/queues
└─ WAN-friendly (works over internet)

Federation Types:
├─ Exchange Federation
├─ Queue Federation
└─ Shovel (point-to-point)
```

### Federation Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Federation Architecture                      │
└─────────────────────────────────────────────────────────┘

Broker A (Upstream)              Broker B (Downstream)
   │                                  │
   ├─ Exchange: "orders"              │
   │                                  │
   │                                  ├─ Exchange: "orders"
   │                                  │  (Federated)
   │                                  │
   │                                  │
   │◄─────────────────────────────────│ (Federation Link)
   │                                  │
   └─ Messages replicated─────────────┘
```

### Federation Setup

```bash
# Enable federation plugin
rabbitmq-plugins enable rabbitmq_federation
rabbitmq-plugins enable rabbitmq_federation_management

# Create upstream
rabbitmqctl set_parameter federation-upstream upstream-broker \
  '{"uri":"amqp://user:pass@broker-a:5672"}'

# Create policy
rabbitmqctl set_policy federate-orders "^orders$" \
  '{"federation-upstream-set":"all"}'
```

---

## Shovel

### Shovel Overview

```
┌─────────────────────────────────────────────────────────┐
│         Shovel                                       │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Move messages between brokers
├─ Point-to-point replication
└─ Bridge brokers

Use Cases:
├─ Migration between brokers
├─ Disaster recovery
└─ Broker bridging
```

### Shovel Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Shovel Architecture                          │
└─────────────────────────────────────────────────────────┘

Broker A                        Broker B
   │                               │
   ├─ Queue: "source"              │
   │                               │
   │                               │
   │───Shovel──────────────────────►│
   │   (moves messages)             │
   │                               │
   │                               ├─ Queue: "destination"
   │                               │
```

### Shovel Configuration

```bash
# Enable shovel plugin
rabbitmq-plugins enable rabbitmq_shovel
rabbitmq-plugins enable rabbitmq_shovel_management

# Create shovel
rabbitmqctl set_parameter shovel my-shovel \
  '{"src-uri":"amqp://user:pass@broker-a:5672", \
    "src-queue":"source", \
    "dest-uri":"amqp://user:pass@broker-b:5672", \
    "dest-queue":"destination"}'
```

---

## Performance Tuning

### Connection Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Connection Tuning                            │
└─────────────────────────────────────────────────────────┘

Key Settings:
├─ Connection pool size
├─ Channel multiplexing
├─ Heartbeat interval
└─ TCP keepalive

Best Practices:
├─ Reuse connections
├─ Use channels for concurrency
├─ Set appropriate heartbeat
└─ Monitor connection count
```

### Queue Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Queue Tuning                                 │
└─────────────────────────────────────────────────────────┘

Settings:
├─ Durable vs non-durable
├─ Lazy queues (disk-based)
├─ Queue length limits
└─ Message TTL

Performance Tips:
├─ Use lazy queues for large backlogs
├─ Set queue length limits
├─ Use TTL for time-sensitive messages
└─ Monitor queue depth
```

### Memory Management

```
┌─────────────────────────────────────────────────────────┐
│         Memory Management                            │
└─────────────────────────────────────────────────────────┘

Settings:
├─ vm_memory_high_watermark: 0.4 (40%)
├─ disk_free_limit: 2GB
└─ Memory calculation

Memory Calculation:
Total Memory × vm_memory_high_watermark = Limit

Example:
8GB RAM × 0.4 = 3.2GB limit

When limit reached:
├─ Flow control activated
├─ Publishers throttled
└─ Prevents memory exhaustion
```

### Disk I/O Tuning

```
┌─────────────────────────────────────────────────────────┐
│         Disk I/O Tuning                              │
└─────────────────────────────────────────────────────────┘

Settings:
├─ Use fast storage (SSD)
├─ Separate data and log directories
├─ Tune OS disk scheduler
└─ Monitor disk I/O

Best Practices:
├─ Use SSDs for production
├─ Separate disk for logs
├─ Tune filesystem (ext4/xfs)
└─ Monitor disk latency
```

---

## Monitoring

### Key Metrics

```
┌─────────────────────────────────────────────────────────┐
│         Key Metrics                                  │
└─────────────────────────────────────────────────────────┘

Broker Metrics:
├─ Memory usage
├─ Disk usage
├─ Connection count
├─ Channel count
└─ Message rates

Queue Metrics:
├─ Message count
├─ Consumer count
├─ Message rate (in/out)
├─ Unacknowledged messages
└─ Consumer utilization

Node Metrics:
├─ CPU usage
├─ Memory usage
├─ Disk I/O
└─ Network I/O
```

### Management UI

```
┌─────────────────────────────────────────────────────────┐
│         Management UI                                │
└─────────────────────────────────────────────────────────┘

URL: http://localhost:15672

Features:
├─ Overview dashboard
├─ Connections
├─ Channels
├─ Exchanges
├─ Queues
├─ Bindings
├─ Admin (users, permissions)
└─ Monitoring (rates, node stats)
```

### Monitoring Tools

```
┌─────────────────────────────────────────────────────────┐
│         Monitoring Tools                            │
└─────────────────────────────────────────────────────────┘

1. Management UI
   ├─ Built-in web interface
   └─ Real-time metrics

2. Prometheus
   ├─ Prometheus exporter plugin
   ├─ Time-series metrics
   └─ Grafana dashboards

3. RabbitMQ Exporter
   ├─ Exports metrics to Prometheus
   └─ Standard metrics format

4. Command Line
   ├─ rabbitmqctl commands
   └─ Scripting and automation
```

### Alerting

```
┌─────────────────────────────────────────────────────────┐
│         Alerting Scenarios                           │
└─────────────────────────────────────────────────────────┘

Critical Alerts:
├─ Memory usage > 80%
├─ Disk usage > 90%
├─ No consumers on critical queue
├─ Queue length > threshold
└─ Node down

Warning Alerts:
├─ Memory usage > 60%
├─ High message rate
├─ Slow consumer
└─ Connection count high
```

---

## Summary

### Key Takeaways

1. **Clustering**: Multiple nodes for HA and scalability
2. **High Availability**: Mirrored queues for fault tolerance
3. **Quorum Queues**: Recommended for new deployments
4. **Federation**: Connect brokers over WAN
5. **Shovel**: Move messages between brokers
6. **Performance**: Tune connections, queues, memory, disk
7. **Monitoring**: Track metrics, set up alerting

### Next Steps

In Part 5, we'll explore:
- Best practices
- Common patterns
- Troubleshooting
- Security
- Production deployment
