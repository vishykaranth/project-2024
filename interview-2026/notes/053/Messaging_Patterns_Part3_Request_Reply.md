# Messaging Patterns - Complete Guide (Part 3: Request-Reply)

## 🔄 Request-Reply: Synchronous Messaging Patterns

---

## 1. Basic Request-Reply Pattern

### Fundamental Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Request-Reply Pattern                           │
└─────────────────────────────────────────────────────────────┘

    Client (Requestor)
         │
         │ Request: {operation: "getUser", id: 123}
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server consumes request
         │
         ▼
    Server (Replier)
         │
         │ Process request
         │ Generate response
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ Client receives response
         │
         ▼
    Client receives response
    
Key Characteristics:
- Synchronous-like behavior over async messaging
- Request and Response are correlated
- Client waits for response
- One request → One response
```

### Request-Reply Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Request-Reply Flow                             │
└─────────────────────────────────────────────────────────────┘

Step 1: Client sends request
    Client
         │
         │ Request: {id: 1, data: "getUser(123)"}
         │ Correlation ID: "corr-123"
         │ Reply-To: "response-queue-client-1"
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │  [Request with correlation ID]
    └──────────┘

Step 2: Server processes request
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server consumes
         │
         ▼
    Server
         │
         │ Process: getUser(123)
         │ Result: {id: 123, name: "John"}
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │  [Response with same correlation ID]
    └──────────┘

Step 3: Client receives response
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ Match by correlation ID
         │
         ▼
    Client receives response
```

---

## 2. Correlation ID Pattern

### Correlation Mechanism
```
┌─────────────────────────────────────────────────────────────┐
│              Correlation ID Pattern                         │
└─────────────────────────────────────────────────────────────┘

Request Message:
    {
        correlationId: "req-12345",
        replyTo: "client-response-queue",
        body: {operation: "getUser", id: 123}
    }

Response Message:
    {
        correlationId: "req-12345",  ← Same ID
        body: {id: 123, name: "John"}
    }

Client Matching:
    Client maintains map:
    {
        "req-12345": Future<Response>
    }
    
    When response arrives:
    - Extract correlationId
    - Find matching Future
    - Complete Future with response
```

### Multiple Concurrent Requests
```
┌─────────────────────────────────────────────────────────────┐
│              Concurrent Request Handling                     │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Request 1: corrId="req-1"
         │ Request 2: corrId="req-2"
         │ Request 3: corrId="req-3"
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server processes (may be out of order)
         │
         ▼
    Server
         │
         │ Response 2: corrId="req-2" (processed first)
         │ Response 1: corrId="req-1" (processed second)
         │ Response 3: corrId="req-3" (processed third)
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ Client matches by correlationId
         │
         ▼
    Client
         │
         │ Matches req-2 → Response 2
         │ Matches req-1 → Response 1
         │ Matches req-3 → Response 3
         │
         ▼
    All requests matched correctly
```

---

## 3. Temporary Reply Queue Pattern

### Dynamic Reply Queue
```
┌─────────────────────────────────────────────────────────────┐
│              Temporary Reply Queue                           │
└─────────────────────────────────────────────────────────────┘

Step 1: Client creates temporary queue
    Client
         │
         │ Create temporary queue: "temp-reply-queue-12345"
         │
         ▼
    ┌──────────┐
    │  Temp    │
    │  Queue   │  (Auto-deleted when client disconnects)
    └──────────┘

Step 2: Client sends request with reply-to
    Client
         │
         │ Request:
         │   replyTo: "temp-reply-queue-12345"
         │   correlationId: "req-1"
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └──────────┘

Step 3: Server sends response to reply-to queue
    Server
         │
         │ Response:
         │   correlationId: "req-1"
         │   destination: "temp-reply-queue-12345"
         │
         ▼
    ┌──────────┐
    │  Temp    │
    │  Queue   │  [Response]
    └────┬─────┘
         │
         │ Client receives from temp queue
         │
         ▼
    Client receives response
```

### Shared vs Temporary Queues
```
┌─────────────────────────────────────────────────────────────┐
│              Queue Types for Replies                         │
└─────────────────────────────────────────────────────────────┘

Temporary Queue (Per Request):
    Client 1 ──► Request ──► Server ──► Temp Queue 1 ──► Client 1
    Client 2 ──► Request ──► Server ──► Temp Queue 2 ──► Client 2
    Client 3 ──► Request ──► Server ──► Temp Queue 3 ──► Client 3
    
    Pros:
    ✅ Isolation: Each client has own queue
    ✅ No message routing needed
    ✅ Automatic cleanup
    
    Cons:
    ❌ More queues to manage
    ❌ Overhead for queue creation

Shared Queue (All Clients):
    Client 1 ──┐
    Client 2 ──┼──► Request ──► Server ──► Shared Queue ──┐
    Client 3 ──┘                                            │
                                                              │
    Client 1 ◄──────────────────────────────────────────────┘
    Client 2 ◄──────────────────────────────────────────────┘
    Client 3 ◄──────────────────────────────────────────────┘
    
    Pros:
    ✅ Single queue for all responses
    ✅ Less overhead
    
    Cons:
    ❌ Need correlation ID matching
    ❌ All clients receive all responses (filter needed)
```

---

## 4. Request-Reply Variants

### Synchronous Request-Reply
```
┌─────────────────────────────────────────────────────────────┐
│              Synchronous Request-Reply                      │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Send request
         │ Block and wait...
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server processes
         │
         ▼
    Server (takes 2 seconds)
         │
         │ Generate response
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ Client receives (after 2 seconds)
         │
         ▼
    Client continues
    
Blocking: Client thread blocked until response
Timeout: If no response, throw exception
```

### Asynchronous Request-Reply
```
┌─────────────────────────────────────────────────────────────┐
│              Asynchronous Request-Reply                     │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Send request (non-blocking)
         │ Continue processing...
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server processes
         │
         ▼
    Server
         │
         │ Generate response
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ Callback invoked when response arrives
         │
         ▼
    Client callback receives response
    
Non-Blocking: Client continues immediately
Callback: Response handled asynchronously
Future/Promise: Can check status or wait
```

---

## 5. Timeout and Error Handling

### Timeout Mechanism
```
┌─────────────────────────────────────────────────────────────┐
│              Request Timeout                                │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Send request
         │ Start timeout timer (5 seconds)
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server processing...
         │ (takes longer than 5 seconds)
         │
         ▼
    Timeout occurs (5 seconds)
         │
         │ Client cancels wait
         │
         ▼
    Client throws TimeoutException
    
    Server still processing...
    (Response will be discarded or ignored)
```

### Error Response Handling
```
┌─────────────────────────────────────────────────────────────┐
│              Error Response                                  │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Request: getUser(999)
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Server processes
         │
         ▼
    Server
         │
         │ Error: User not found
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │  [Error response]
    └────┬─────┘
         │
         │ Response: {
         │   correlationId: "req-1",
         │   success: false,
         │   error: "User not found"
         │ }
         │
         ▼
    Client receives error response
         │
         │ Handle error appropriately
         │
         ▼
    Client throws exception or handles error
```

---

## 6. Request-Reply with Multiple Servers

### Load Balanced Request-Reply
```
┌─────────────────────────────────────────────────────────────┐
│              Multiple Servers (Load Balanced)                │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Request
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ Load balanced
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   S1   S2   S3
   
Server 1 processes request
    │
    │ Response
    │
    ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ Client receives response
         │
         ▼
    Client
    
Note: Any server can process any request
Response goes to same client that sent request
```

### Request Routing
```
┌─────────────────────────────────────────────────────────────┐
│              Request Routing                                 │
└─────────────────────────────────────────────────────────────┘

    Client
         │
         │ Request: {operation: "getUser", id: 123}
         │
         ▼
    ┌──────────┐
    │ Request  │
    │ Router   │
    └────┬─────┘
         │
         │ Route based on operation
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   (getUser) (updateUser) (deleteUser)
    │    │    │
    ▼    ▼    ▼
   S1   S2   S3
   
Specialized servers:
- Server 1: Handles getUser requests
- Server 2: Handles updateUser requests
- Server 3: Handles deleteUser requests
```

---

## 7. Real-World Examples

### RPC over Messaging
```
┌─────────────────────────────────────────────────────────────┐
│              RPC Pattern                                     │
└─────────────────────────────────────────────────────────────┘

    Client Application
         │
         │ RPC Call: userService.getUser(123)
         │
         ▼
    RPC Client
         │
         │ Serialize request
         │ Send to message queue
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ RPC Server consumes
         │
         ▼
    RPC Server
         │
         │ Deserialize request
         │ Invoke method: getUser(123)
         │ Serialize response
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ RPC Client receives
         │
         ▼
    RPC Client
         │
         │ Deserialize response
         │ Return to application
         │
         ▼
    Client Application receives result
```

### API Gateway Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              API Gateway with Request-Reply                  │
└─────────────────────────────────────────────────────────────┘

    External Client
         │
         │ HTTP Request: GET /api/users/123
         │
         ▼
    API Gateway
         │
         │ Convert to message
         │ Request: {service: "user", method: "get", id: 123}
         │
         ▼
    ┌──────────┐
    │ Request  │
    │  Queue   │
    └────┬─────┘
         │
         │ User Service consumes
         │
         ▼
    User Service
         │
         │ Process: getUser(123)
         │ Response: {id: 123, name: "John"}
         │
         ▼
    ┌──────────┐
    │ Response │
    │  Queue   │
    └────┬─────┘
         │
         │ API Gateway receives
         │
         ▼
    API Gateway
         │
         │ Convert to HTTP response
         │
         ▼
    External Client receives HTTP response
```

---

## 8. Implementation Examples

### Java (JMS Request-Reply)
```java
// Client (Requestor)
ConnectionFactory factory = new ActiveMQConnectionFactory();
Connection connection = factory.createConnection();
connection.start();
Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

// Create temporary queue for response
TemporaryQueue replyQueue = session.createTemporaryQueue();
MessageConsumer consumer = session.createConsumer(replyQueue);

// Send request
Queue requestQueue = session.createQueue("REQUEST_QUEUE");
MessageProducer producer = session.createProducer(requestQueue);

TextMessage request = session.createTextMessage("getUser:123");
request.setJMSReplyTo(replyQueue);
String correlationId = UUID.randomUUID().toString();
request.setJMSCorrelationID(correlationId);
producer.send(request);

// Wait for response
Message response = consumer.receive(5000); // 5 second timeout
if (response != null && response.getJMSCorrelationID().equals(correlationId)) {
    if (response instanceof TextMessage) {
        TextMessage textMessage = (TextMessage) response;
        System.out.println("Response: " + textMessage.getText());
    }
}

// Server (Replier)
MessageConsumer requestConsumer = session.createConsumer(requestQueue);
requestConsumer.setMessageListener(new MessageListener() {
    @Override
    public void onMessage(Message request) {
        try {
            // Process request
            String requestText = ((TextMessage) request).getText();
            String responseText = processRequest(requestText);
            
            // Send response
            MessageProducer responseProducer = session.createProducer(request.getJMSReplyTo());
            TextMessage response = session.createTextMessage(responseText);
            response.setJMSCorrelationID(request.getJMSCorrelationID());
            responseProducer.send(response);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
});
```

### Python (RabbitMQ RPC)
```python
# Client (Requestor)
import pika
import uuid

class RPCClient:
    def __init__(self):
        self.connection = pika.BlockingConnection(
            pika.ConnectionParameters(host='localhost'))
        self.channel = self.connection.channel()
        result = self.channel.queue_declare(queue='', exclusive=True)
        self.callback_queue = result.method.queue
        self.channel.basic_consume(
            queue=self.callback_queue,
            on_message_callback=self.on_response,
            auto_ack=True)
        self.response = None
        self.corr_id = None

    def on_response(self, ch, method, props, body):
        if self.corr_id == props.correlation_id:
            self.response = body

    def call(self, n):
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(
            exchange='',
            routing_key='rpc_queue',
            properties=pika.BasicProperties(
                reply_to=self.callback_queue,
                correlation_id=self.corr_id,
            ),
            body=str(n))
        while self.response is None:
            self.connection.process_data_events(time_limit=5)
        return self.response

# Server (Replier)
def on_request(ch, method, props, body):
    n = int(body)
    response = str(fib(n))
    ch.basic_publish(
        exchange='',
        routing_key=props.reply_to,
        properties=pika.BasicProperties(
            correlation_id=props.correlation_id),
        body=str(response))
    ch.basic_ack(delivery_tag=method.delivery_tag)

channel.basic_qos(prefetch_count=1)
channel.basic_consume(queue='rpc_queue', on_message_callback=on_request)
channel.start_consuming()
```

---

## Key Characteristics Summary

### Request-Reply Messaging
```
✅ Synchronous-like: Request and response paired
✅ Correlation: Correlation ID matches request/response
✅ One-to-One: One request → One response
✅ Blocking: Client waits for response (synchronous variant)
✅ Non-Blocking: Callback-based (asynchronous variant)
✅ Timeout: Handle missing responses
✅ Error Handling: Error responses possible
```

### When to Use
```
✅ RPC over Messaging: Remote procedure calls
✅ API Gateway: Convert HTTP to messaging
✅ Service Calls: Inter-service communication
✅ Query Operations: Request data from services
✅ Synchronous Operations: Need immediate response
✅ Command-Query Separation: CQRS pattern
```

### When NOT to Use
```
❌ Fire-and-Forget: Don't need response
❌ Event Broadcasting: One-to-many communication
❌ High Throughput: Overhead of request-reply
❌ Long-Running Operations: Timeout issues
❌ Asynchronous Processing: Better with events
```

---

**Next: Part 4 will cover Message Routing (Content-based, header-based routing).**

