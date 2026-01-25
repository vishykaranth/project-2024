# RabbitMQ In-Depth: Part 5 - Best Practices & Operations

## Table of Contents
1. [Best Practices](#best-practices)
2. [Common Patterns](#common-patterns)
3. [Troubleshooting](#troubleshooting)
4. [Security](#security)
5. [Production Deployment](#production-deployment)
6. [Integration Examples](#integration-examples)

---

## Best Practices

### Connection Management

```
┌─────────────────────────────────────────────────────────┐
│         Connection Best Practices                    │
└─────────────────────────────────────────────────────────┘

1. Reuse Connections
   ├─ Create connection per application
   ├─ Reuse across requests
   └─ Avoid creating per message

2. Use Channels for Concurrency
   ├─ Multiple channels per connection
   ├─ Channels are lightweight
   └─ Enable parallel processing

3. Connection Pooling
   ├─ Pool connections in application
   ├─ Limit connection count
   └─ Monitor connection usage

4. Proper Cleanup
   ├─ Close channels when done
   ├─ Close connections on shutdown
   └─ Handle connection errors
```

### Queue Design

```
┌─────────────────────────────────────────────────────────┐
│         Queue Design Best Practices                  │
└─────────────────────────────────────────────────────────┘

1. Naming Conventions
   ├─ Use descriptive names
   ├─ Include domain/context
   └─ Example: "orders.create", "users.notifications"

2. Queue Properties
   ├─ Use durable queues for important data
   ├─ Set appropriate TTL
   ├─ Configure length limits
   └─ Use lazy queues for large backlogs

3. Queue Organization
   ├─ Separate queues by purpose
   ├─ Avoid single queue for everything
   └─ Group related queues

4. Dead Letter Queues
   ├─ Always configure DLQ
   ├─ Monitor DLQ for issues
   └─ Enable retry logic
```

### Message Design

```
┌─────────────────────────────────────────────────────────┐
│         Message Design Best Practices                │
└─────────────────────────────────────────────────────────┘

1. Message Size
   ├─ Keep messages small (< 1MB)
   ├─ Use references for large data
   └─ Consider chunking for large payloads

2. Message Format
   ├─ Use JSON for structured data
   ├─ Use Protocol Buffers for efficiency
   └─ Include version in message

3. Message Properties
   ├─ Set correlation_id for request/reply
   ├─ Use message_id for deduplication
   └─ Set appropriate TTL

4. Idempotency
   ├─ Design idempotent consumers
   ├─ Handle duplicate messages
   └─ Use unique message IDs
```

### Error Handling

```
┌─────────────────────────────────────────────────────────┐
│         Error Handling Best Practices                │
└─────────────────────────────────────────────────────────┘

1. Acknowledgment Strategy
   ├─ Always use manual ACK
   ├─ ACK after successful processing
   └─ NACK with requeue for retries

2. Retry Logic
   ├─ Implement exponential backoff
   ├─ Limit retry attempts
   └─ Send to DLQ after max retries

3. Dead Letter Queues
   ├─ Configure DLQ for all queues
   ├─ Monitor DLQ regularly
   └─ Implement DLQ processing

4. Error Logging
   ├─ Log all errors
   ├─ Include message context
   └─ Track error rates
```

---

## Common Patterns

### 1. Work Queue Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Work Queue Pattern                           │
└─────────────────────────────────────────────────────────┘

Use Case: Distribute tasks among workers

Architecture:
Producer → Queue → Workers (round-robin)

Implementation:
├─ Single queue
├─ Multiple consumers
├─ Fair distribution (prefetch=1)
└─ Manual acknowledgment
```

### Work Queue Example

```python
# Producer
channel.queue_declare(queue='tasks', durable=True)

for task in tasks:
    channel.basic_publish(
        exchange='',
        routing_key='tasks',
        body=json.dumps(task),
        properties=pika.BasicProperties(delivery_mode=2)
    )

# Consumer
channel.queue_declare(queue='tasks', durable=True)
channel.basic_qos(prefetch_count=1)  # Fair distribution

def process_task(channel, method, properties, body):
    task = json.loads(body)
    process(task)
    channel.basic_ack(delivery_tag=method.delivery_tag)

channel.basic_consume(queue='tasks', on_message_callback=process_task)
```

### 2. Pub/Sub Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Pub/Sub Pattern                              │
└─────────────────────────────────────────────────────────┘

Use Case: Broadcast messages to multiple subscribers

Architecture:
Publisher → Fanout Exchange → Multiple Queues → Subscribers

Implementation:
├─ Fanout exchange
├─ Each subscriber has own queue
└─ Broadcast to all
```

### Pub/Sub Example

```python
# Publisher
channel.exchange_declare(exchange='notifications', exchange_type='fanout')

message = json.dumps({'event': 'user.created', 'user_id': 123})
channel.basic_publish(exchange='notifications', routing_key='', body=message)

# Subscriber 1
channel.exchange_declare(exchange='notifications', exchange_type='fanout')
result = channel.queue_declare(queue='', exclusive=True)
queue_name = result.method.queue
channel.queue_bind(exchange='notifications', queue=queue_name)

channel.basic_consume(queue=queue_name, on_message_callback=handle_notification)

# Subscriber 2 (same pattern)
```

### 3. Routing Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Routing Pattern                              │
└─────────────────────────────────────────────────────────┘

Use Case: Route messages based on criteria

Architecture:
Producer → Direct/Topic Exchange → Queues (by routing key)

Implementation:
├─ Direct exchange (exact match)
├─ Topic exchange (pattern match)
└─ Routing keys determine destination
```

### Routing Example

```python
# Producer
channel.exchange_declare(exchange='orders', exchange_type='direct')

channel.basic_publish(
    exchange='orders',
    routing_key='create',
    body=json.dumps({'order_id': 123})
)

# Consumer (create orders)
channel.exchange_declare(exchange='orders', exchange_type='direct')
channel.queue_declare(queue='orders.create')
channel.queue_bind(exchange='orders', queue='orders.create', routing_key='create')

channel.basic_consume(queue='orders.create', on_message_callback=handle_create)
```

### 4. Request/Reply Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Request/Reply Pattern                         │
└─────────────────────────────────────────────────────────┘

Use Case: RPC-style communication

Architecture:
Client → Request Queue → Server → Reply Queue → Client

Implementation:
├─ Correlation ID for matching
├─ Reply-to queue per client
└─ Temporary queues for replies
```

### Request/Reply Example

```python
# Client
result = channel.queue_declare(queue='', exclusive=True)
callback_queue = result.method.queue

correlation_id = str(uuid.uuid4())

def on_response(channel, method, properties, body):
    if properties.correlation_id == correlation_id:
        response = json.loads(body)
        print(f"Response: {response}")

channel.basic_consume(queue=callback_queue, on_message_callback=on_response)

channel.basic_publish(
    exchange='',
    routing_key='rpc_queue',
    body=json.dumps({'request': 'data'}),
    properties=pika.BasicProperties(
        reply_to=callback_queue,
        correlation_id=correlation_id
    )
)

# Server
channel.queue_declare(queue='rpc_queue')

def on_request(channel, method, properties, body):
    request = json.loads(body)
    response = process_request(request)
    
    channel.basic_publish(
        exchange='',
        routing_key=properties.reply_to,
        body=json.dumps(response),
        properties=pika.BasicProperties(
            correlation_id=properties.correlation_id
        )
    )
    channel.basic_ack(delivery_tag=method.delivery_tag)

channel.basic_qos(prefetch_count=1)
channel.basic_consume(queue='rpc_queue', on_message_callback=on_request)
```

---

## Troubleshooting

### Common Issues

```
┌─────────────────────────────────────────────────────────┐
│         Common Issues & Solutions                    │
└─────────────────────────────────────────────────────────┘

1. Memory Issues
   Symptoms:
   ├─ Flow control activated
   ├─ Slow performance
   └─ Connection refused
   
   Solutions:
   ├─ Increase memory limit
   ├─ Reduce queue lengths
   ├─ Use lazy queues
   └─ Add more nodes

2. Disk Space Issues
   Symptoms:
   ├─ Disk space warnings
   ├─ Broker stops accepting messages
   └─ Log errors
   
   Solutions:
   ├─ Increase disk_free_limit
   ├─ Clean up old queues
   ├─ Archive old messages
   └─ Add more disk space

3. Connection Issues
   Symptoms:
   ├─ Connection refused
   ├─ Timeout errors
   └─ Too many connections
   
   Solutions:
   ├─ Reuse connections
   ├─ Increase connection limit
   ├─ Use connection pooling
   └─ Check network connectivity

4. Queue Backlog
   Symptoms:
   ├─ Growing queue length
   ├─ Slow processing
   └─ Consumer lag
   
   Solutions:
   ├─ Add more consumers
   ├─ Optimize consumer processing
   ├─ Increase prefetch
   └─ Scale horizontally
```

### Diagnostic Commands

```bash
# Check broker status
rabbitmqctl status

# List queues
rabbitmqctl list_queues name messages consumers

# List connections
rabbitmqctl list_connections

# Check memory usage
rabbitmqctl status | grep memory

# Check disk usage
rabbitmqctl status | grep disk

# List exchanges
rabbitmqctl list_exchanges

# Check bindings
rabbitmqctl list_bindings

# Purge queue
rabbitmqctl purge_queue queue_name

# Delete queue
rabbitmqctl delete_queue queue_name
```

---

## Security

### Authentication

```
┌─────────────────────────────────────────────────────────┐
│         Authentication                               │
└─────────────────────────────────────────────────────────┘

Methods:
├─ Username/Password (default)
├─ LDAP
├─ OAuth 2.0
└─ x509 certificates

User Management:
├─ Create users
├─ Set passwords
├─ Assign tags (administrator, monitoring, etc.)
└─ Manage permissions
```

### Authorization

```
┌─────────────────────────────────────────────────────────┐
│         Authorization (Permissions)                 │
└─────────────────────────────────────────────────────────┘

Permission Types:
├─ Configure: Create/delete exchanges/queues
├─ Write: Publish messages
└─ Read: Consume messages

Permission Scope:
├─ Virtual host level
├─ Resource level (exchange/queue)
└─ Pattern-based
```

### Security Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Security Best Practices                      │
└─────────────────────────────────────────────────────────┘

1. Change Default Credentials
   ├─ Remove guest user
   ├─ Use strong passwords
   └─ Rotate passwords regularly

2. Use Virtual Hosts
   ├─ Isolate applications
   ├─ Separate environments
   └─ Limit access

3. Limit Permissions
   ├─ Principle of least privilege
   ├─ Separate read/write permissions
   └─ Use specific resource permissions

4. Enable TLS/SSL
   ├─ Encrypt connections
   ├─ Use certificates
   └─ Secure management UI

5. Network Security
   ├─ Firewall rules
   ├─ VPN access
   └─ Private networks
```

### Security Configuration

```bash
# Create user
rabbitmqctl add_user admin secure_password

# Set tags
rabbitmqctl set_user_tags admin administrator

# Set permissions
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# Enable TLS
# Edit rabbitmq.conf
listeners.ssl.default = 5671
ssl_options.cacertfile = /path/to/ca_certificate.pem
ssl_options.certfile = /path/to/server_certificate.pem
ssl_options.keyfile = /path/to/server_key.pem
```

---

## Production Deployment

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Production Architecture                      │
└─────────────────────────────────────────────────────────┘

                    Load Balancer
                    (HAProxy)
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Node 1     │  │   Node 2     │  │   Node 3     │
│  (Disk)      │  │  (Disk)      │  │  (RAM)       │
│              │  │              │  │              │
│ Mirrored     │  │ Mirrored     │  │ Mirrored     │
│ Queues       │  │ Queues       │  │ Queues       │
└──────────────┘  └──────────────┘  └──────────────┘
```

### Production Checklist

```
┌─────────────────────────────────────────────────────────┐
│         Production Checklist                         │
└─────────────────────────────────────────────────────────┘

Infrastructure:
├─ Multiple nodes (3+)
├─ Load balancer
├─ Monitoring
└─ Backup strategy

Configuration:
├─ Durable queues
├─ Persistent messages
├─ Mirrored queues
└─ Dead letter queues

Security:
├─ TLS/SSL enabled
├─ Strong authentication
├─ Proper permissions
└─ Network security

Monitoring:
├─ Metrics collection
├─ Alerting configured
├─ Log aggregation
└─ Health checks

Operations:
├─ Backup procedures
├─ Disaster recovery
├─ Capacity planning
└─ Documentation
```

---

## Integration Examples

### Spring Boot Integration

```java
// Configuration
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable("orders").build();
    }
    
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange("orders");
    }
    
    @Bean
    public Binding ordersBinding() {
        return BindingBuilder
            .bind(ordersQueue())
            .to(ordersExchange())
            .with("create");
    }
}

// Producer
@Service
public class OrderProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void sendOrder(Order order) {
        rabbitTemplate.convertAndSend("orders", "create", order);
    }
}

// Consumer
@Component
public class OrderConsumer {
    @RabbitListener(queues = "orders")
    public void handleOrder(Order order) {
        processOrder(order);
    }
}
```

### Node.js Integration

```javascript
// Producer
const amqp = require('amqplib');

async function publishOrder(order) {
    const connection = await amqp.connect('amqp://localhost');
    const channel = await connection.createChannel();
    
    await channel.assertQueue('orders', { durable: true });
    channel.sendToQueue('orders', Buffer.from(JSON.stringify(order)), {
        persistent: true
    });
    
    await channel.close();
    await connection.close();
}

// Consumer
async function consumeOrders() {
    const connection = await amqp.connect('amqp://localhost');
    const channel = await connection.createChannel();
    
    await channel.assertQueue('orders', { durable: true });
    channel.prefetch(10);
    
    channel.consume('orders', (msg) => {
        const order = JSON.parse(msg.content.toString());
        processOrder(order);
        channel.ack(msg);
    });
}
```

---

## Summary

### Key Takeaways

1. **Best Practices**: Reuse connections, use channels, design idempotent consumers
2. **Common Patterns**: Work queue, pub/sub, routing, request/reply
3. **Troubleshooting**: Monitor memory, disk, connections, queues
4. **Security**: Authentication, authorization, TLS, network security
5. **Production**: Cluster, HA, monitoring, backup, documentation

### Complete RabbitMQ Knowledge

You now have comprehensive knowledge of:
- RabbitMQ fundamentals and architecture
- Exchanges, routing, and queues
- Message delivery and reliability
- Clustering and high availability
- Best practices and operations

This knowledge enables you to:
- Design RabbitMQ-based systems
- Implement producers and consumers
- Build reliable messaging solutions
- Operate and monitor RabbitMQ clusters
- Troubleshoot issues
- Deploy to production
