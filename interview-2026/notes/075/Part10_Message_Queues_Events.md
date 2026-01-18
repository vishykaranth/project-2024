# Part 10: Message Queues & Event-Driven Architecture - Quick Revision

## Message Queue Patterns

- **Point-to-Point**: One producer, one consumer; task distribution
- **Publish-Subscribe**: One producer, multiple consumers; event broadcasting
- **Request-Reply**: Request message, reply message; RPC-like
- **Message Routing**: Route messages based on content or headers

## Message Queue Systems

- **Apache Kafka**: Distributed streaming, high throughput, event sourcing
- **RabbitMQ**: Message broker, multiple protocols, flexible routing
- **Amazon SQS**: Managed queue service, at-least-once delivery
- **Redis Pub/Sub**: Simple pub/sub, fast, in-memory

## Event-Driven Architecture

- **Event Sourcing**: Store events, rebuild state from events
- **CQRS**: Separate read and write models, optimize each independently
- **Saga Pattern**: Distributed transactions using compensating transactions
- **Event Streaming**: Continuous stream of events, real-time processing

## Message Guarantees

- **At-Least-Once**: Message delivered at least once; may have duplicates
- **At-Most-Once**: Message delivered at most once; may be lost
- **Exactly-Once**: Message delivered exactly once; hardest to achieve
- **Ordering**: Maintain message order within partition/topic

## Dead Letter Queue (DLQ)

- **Purpose**: Store messages that can't be processed
- **Use Cases**: Invalid messages, processing failures, retry exhaustion
- **Monitoring**: Alert on DLQ growth, investigate failures

## Benefits

- **Decoupling**: Services don't need to know about each other
- **Scalability**: Handle bursts, scale consumers independently
- **Reliability**: Messages persisted, retry on failure
- **Asynchronous**: Non-blocking, better performance
