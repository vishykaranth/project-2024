# Messaging Patterns - Complete Guide (Part 2: Publish-Subscribe)

## 📢 Publish-Subscribe: Topic-Based Messaging

---

## 1. Basic Publish-Subscribe Architecture

### Fundamental Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Publish-Subscribe Pattern                       │
└─────────────────────────────────────────────────────────────┘

    Producer 1              Producer 2
         │                       │
         │                       │
         └───────────┬───────────┘
                     │
                     ▼
              ┌──────────┐
              │  Topic   │  ← Topic/Exchange
              │ "orders" │
              └────┬─────┘
                   │
         ┌─────────┼─────────┐
         │         │         │
         ▼         ▼         ▼
    Consumer 1  Consumer 2  Consumer 3
    (Subscriber) (Subscriber) (Subscriber)
    
Key Characteristics:
- One message → Multiple consumers
- Messages are NOT consumed (copied to all)
- All subscribers receive the message
- Decoupled: Publishers don't know subscribers
- Dynamic: Subscribers can join/leave anytime
```

### Message Broadcasting
```
┌─────────────────────────────────────────────────────────────┐
│              Message Broadcasting                            │
└─────────────────────────────────────────────────────────────┘

Step 1: Publisher sends message to topic
    Publisher
         │
         │ Message: {event: "OrderCreated", orderId: 123}
         │
         ▼
    ┌──────────┐
    │  Topic   │
    │ "orders" │
    └────┬─────┘
         │
         │ Message copied to all subscribers
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   S1   S2   S3
   
Step 2: All subscribers receive message
    S1: Receives copy of message
    S2: Receives copy of message
    S3: Receives copy of message
    
Step 3: Message remains in topic
    ┌──────────┐
    │  Topic   │  [Message still available]
    │ "orders" │  (for new subscribers)
    └──────────┘
```

---

## 2. Topic Structure and Routing

### Hierarchical Topics
```
┌─────────────────────────────────────────────────────────────┐
│              Hierarchical Topic Structure                    │
└─────────────────────────────────────────────────────────────┘

Root Topic
    │
    ├─── orders
    │     ├─── orders.created
    │     ├─── orders.updated
    │     └─── orders.cancelled
    │
    ├─── payments
    │     ├─── payments.processed
    │     ├─── payments.failed
    │     └─── payments.refunded
    │
    └─── inventory
          ├─── inventory.low
          ├─── inventory.out
          └─── inventory.restocked

Subscription Patterns:
- orders.* → All order events
- orders.created → Only order creation
- *.failed → All failure events
- payments.* → All payment events
```

### Wildcard Subscriptions
```
┌─────────────────────────────────────────────────────────────┐
│              Wildcard Subscriptions                          │
└─────────────────────────────────────────────────────────────┘

Topic: orders.created
Topic: orders.updated
Topic: orders.cancelled
Topic: payments.processed

Subscriber 1: "orders.*"
    Receives:
    ✅ orders.created
    ✅ orders.updated
    ✅ orders.cancelled
    ❌ payments.processed

Subscriber 2: "*.created"
    Receives:
    ✅ orders.created
    ❌ orders.updated
    ❌ orders.cancelled
    ❌ payments.processed

Subscriber 3: "*.*"
    Receives:
    ✅ orders.created
    ✅ orders.updated
    ✅ orders.cancelled
    ✅ payments.processed
```

---

## 3. Exchange Types (RabbitMQ Model)

### Direct Exchange
```
┌─────────────────────────────────────────────────────────────┐
│              Direct Exchange                                │
└─────────────────────────────────────────────────────────────┘

    Publisher
         │
         │ Routing Key: "orders.created"
         │
         ▼
    ┌──────────┐
    │ Direct   │
    │ Exchange │
    └────┬─────┘
         │
         │ Exact match routing
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   (orders.created) (orders.updated) (orders.cancelled)
   
Binding:
- Queue 1: routing_key = "orders.created"
- Queue 2: routing_key = "orders.updated"
- Queue 3: routing_key = "orders.cancelled"

Message with "orders.created" → Only Queue 1
```

### Topic Exchange
```
┌─────────────────────────────────────────────────────────────┐
│              Topic Exchange                                 │
└─────────────────────────────────────────────────────────────┘

    Publisher
         │
         │ Routing Key: "orders.created.high"
         │
         ▼
    ┌──────────┐
    │  Topic   │
    │ Exchange │
    └────┬─────┘
         │
         │ Pattern matching
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   (orders.*) (*.created.*) (orders.created.*)
   
Bindings:
- Queue 1: "orders.*" → Matches ✅
- Queue 2: "*.created.*" → Matches ✅
- Queue 3: "orders.created.*" → Matches ✅

All three queues receive the message
```

### Fanout Exchange
```
┌─────────────────────────────────────────────────────────────┐
│              Fanout Exchange                                 │
└─────────────────────────────────────────────────────────────┘

    Publisher
         │
         │ Message (no routing key needed)
         │
         ▼
    ┌──────────┐
    │  Fanout  │
    │ Exchange │
    └────┬─────┘
         │
         │ Broadcast to all bound queues
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
All queues receive message regardless of routing key
Use case: Broadcast notifications
```

### Headers Exchange
```
┌─────────────────────────────────────────────────────────────┐
│              Headers Exchange                                │
└─────────────────────────────────────────────────────────────┘

    Publisher
         │
         │ Headers: {type: "order", priority: "high"}
         │
         ▼
    ┌──────────┐
    │ Headers  │
    │ Exchange │
    └────┬─────┘
         │
         │ Match based on headers
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   (type=order) (priority=high) (type=order AND priority=high)
   
Matching:
- Queue 1: x-match=any, type=order → Matches ✅
- Queue 2: x-match=any, priority=high → Matches ✅
- Queue 3: x-match=all, type=order, priority=high → Matches ✅
```

---

## 4. Subscription Models

### Durable vs Non-Durable Subscriptions
```
┌─────────────────────────────────────────────────────────────┐
│              Subscription Durability                        │
└─────────────────────────────────────────────────────────────┘

Non-Durable Subscription:
    Subscriber connects
         │
         │ Receives messages while connected
         │
         ▼
    Subscriber disconnects
         │
         │ Messages published during disconnect: LOST
         │
         ▼
    Subscriber reconnects
         │
         │ Missed messages not received
         │

Durable Subscription:
    Subscriber connects (durable)
         │
         │ Subscription persists
         │
         ▼
    Subscriber disconnects
         │
         │ Messages stored in subscription queue
         │
         ▼
    ┌──────────┐
    │  Queue   │  [Message 1]
    │          │  [Message 2]
    │          │  [Message 3]
    └──────────┘
         │
         │ Subscriber reconnects
         │
         ▼
    Receives all missed messages
```

### Active vs Passive Subscriptions
```
┌─────────────────────────────────────────────────────────────┐
│              Active vs Passive Subscriptions                  │
└─────────────────────────────────────────────────────────────┘

Active Subscription (Push):
    Topic ──► Push ──► Subscriber
                │
                │ Immediate delivery
                │
                ▼
            Subscriber processes
    
Passive Subscription (Pull):
    Subscriber ──► Pull ──► Topic
                      │
                      │ On-demand retrieval
                      │
                      ▼
                  Subscriber processes
                  
Comparison:
- Push: Real-time, requires active connection
- Pull: Polling-based, can miss messages
```

---

## 5. Message Filtering

### Content-Based Filtering
```
┌─────────────────────────────────────────────────────────────┐
│              Content-Based Filtering                        │
└─────────────────────────────────────────────────────────────┘

    Publisher
         │
         │ Message: {
         │   event: "OrderCreated",
         │   amount: 1000,
         │   region: "US"
         │ }
         │
         ▼
    ┌──────────┐
    │  Topic   │
    └────┬─────┘
         │
         │ Filter: amount > 500 AND region = "US"
         │
         ▼
    ┌──────────┐
    │ Filter   │
    │ Engine   │
    └────┬─────┘
         │
         │ Matches filter
         │
         ▼
    Subscriber receives message

Filter: amount <= 500 OR region != "US"
    Subscriber does NOT receive message
```

### Selector-Based Filtering (JMS)
```
┌─────────────────────────────────────────────────────────────┐
│              JMS Selectors                                  │
└─────────────────────────────────────────────────────────────┘

Message Properties:
    event = "OrderCreated"
    amount = 1000
    region = "US"
    priority = "high"

Subscriber 1 Selector:
    "amount > 500 AND region = 'US'"
    ✅ Receives message

Subscriber 2 Selector:
    "priority = 'high'"
    ✅ Receives message

Subscriber 3 Selector:
    "amount < 100"
    ❌ Does NOT receive message

Subscriber 4 Selector:
    "event = 'OrderCancelled'"
    ❌ Does NOT receive message
```

---

## 6. Real-World Examples

### Event-Driven Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              E-Commerce Event System                        │
└─────────────────────────────────────────────────────────────┘

    Order Service
         │
         │ Publish: "orders.created"
         │
         ▼
    ┌──────────┐
    │  Topic   │
    │ "events" │
    └────┬─────┘
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   ES   IS   NS
   
Event Store (ES):
    - Stores all events
    - Event sourcing

Inventory Service (IS):
    - Updates inventory
    - Reserves items

Notification Service (NS):
    - Sends email
    - Sends SMS
```

### Microservices Communication
```
┌─────────────────────────────────────────────────────────────┐
│              Microservices Event Bus                        │
└─────────────────────────────────────────────────────────────┘

    Service A          Service B          Service C
         │                 │                 │
         │                 │                 │
         └─────────┬───────┴─────────────────┘
                   │
                   ▼
            ┌──────────┐
            │  Event   │
            │   Bus    │
            └────┬─────┘
                 │
        ┌────────┼────────┐
        │        │        │
        ▼        ▼        ▼
       S1       S2       S3
       
Benefits:
- Loose coupling
- Scalability
- Event-driven
- Real-time updates
```

---

## 7. Implementation Examples

### Java (JMS Topic)
```java
// Publisher
ConnectionFactory factory = new ActiveMQConnectionFactory();
Connection connection = factory.createConnection();
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
Topic topic = session.createTopic("orders.created");
MessageProducer producer = session.createProducer(topic);

TextMessage message = session.createTextMessage("Order: 12345");
producer.publish(message);

// Subscriber
MessageConsumer consumer = session.createConsumer(topic);
consumer.setMessageListener(new MessageListener() {
    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            System.out.println("Received: " + textMessage.getText());
        }
    }
});
```

### Python (RabbitMQ Topic Exchange)
```python
# Publisher
import pika
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()
channel.exchange_declare(exchange='orders', exchange_type='topic')

routing_key = 'orders.created'
message = 'Order: 12345'
channel.basic_publish(exchange='orders', routing_key=routing_key, body=message)

# Subscriber
def callback(ch, method, properties, body):
    print(f"Received: {body}")

channel.queue_declare(queue='order_processor')
channel.queue_bind(exchange='orders', queue='order_processor', routing_key='orders.*')
channel.basic_consume(queue='order_processor', on_message_callback=callback, auto_ack=True)
channel.start_consuming()
```

### Kafka (Topic)
```java
// Producer
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);
ProducerRecord<String, String> record = new ProducerRecord<>("orders", "key", "Order: 12345");
producer.send(record);

// Consumer
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "order-processors");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Collections.singletonList("orders"));

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.println("Received: " + record.value());
    }
}
```

---

## Key Characteristics Summary

### Publish-Subscribe Messaging
```
✅ One-to-Many: One message → Multiple consumers
✅ Decoupling: Publishers don't know subscribers
✅ Dynamic: Subscribers can join/leave anytime
✅ Broadcasting: All subscribers receive message
✅ Topic-Based: Categorized message routing
✅ Filtering: Content-based or selector-based
✅ Durable Subscriptions: Don't miss messages
✅ Wildcards: Pattern-based subscriptions
```

### When to Use
```
✅ Event Broadcasting: Notify all interested parties
✅ Event-Driven Architecture: Loose coupling
✅ Real-Time Updates: Live data distribution
✅ Microservices: Service communication
✅ Logging: Centralized log distribution
✅ Monitoring: Metrics and alerts
✅ News Feeds: Content distribution
```

### When NOT to Use
```
❌ Task Distribution: Need one consumer per message
❌ Guaranteed Order: Need strict ordering
❌ Point-to-Point: One-to-one communication
❌ Request-Reply: Need response back
```

---

**Next: Part 3 will cover Request-Reply (Synchronous messaging patterns).**

