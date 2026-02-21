Here’s a **🎯 Principal-level Kafka topic design for the Netflix control plane (Java/Spring ecosystem)** —
focused on **event contracts, partitioning strategy, ordering, scalability, and failure isolation**.

This is the depth expected in **Staff+ interviews**.

---

# 🧭 1. WHY KAFKA HERE?

We use Kafka for:

* High-write user activity
* Async processing
* Loose coupling between microservices
* Real-time + batch pipelines

Critical rule:

> “Kafka carries facts (events), never commands.”

---

# 🧱 2. CORE EVENT DOMAINS

We design topics around **business domains**, not services.

---

## ▶️ Playback Domain

### Topic: `playback.session-events`

**Produced by:** Playback Service
**Consumed by:**

* Viewing History
* Recommendation pipeline
* Analytics
* Billing (for watch-time plans)

**Event types**

```json
PLAYBACK_STARTED
PLAYBACK_HEARTBEAT
PLAYBACK_COMPLETED
PLAYBACK_STOPPED
```

### Partition key

```
profileId
```

✅ Per-user ordering
✅ Resume playback consistency

---

## 👀 Viewing History

### Topic: `viewing.history-updates`

Produced after aggregation.

Used by:

* Continue Watching service
* Personalization cache updater

---

## 🧠 Recommendation Domain

### Topic: `recommendation.user-signals`

Carries:

* watch_time
* pause
* skip
* search_click

Partition key:

```
profileId
```

Used for:

* Real-time feature updates

---

### Topic: `recommendation.batch-input`

Offline pipeline input (Spark/Flink).

---

## 🔍 Search Domain

### Topic: `catalog.index-updates`

Produced by:

* Catalog service

Consumed by:

* Search indexer

Key:

```
titleId
```

Ensures correct ordering for updates.

---

## 👤 User Domain

### Topic: `user.profile-events`

Events:

* PROFILE_CREATED
* PROFILE_UPDATED
* PLAN_CHANGED
* ENTITLEMENT_UPDATED

Consumers:

* Playback service cache
* Recommendation filters
* Billing

---

## 💳 Billing Domain

### Topic: `billing.payment-events`

Events:

* PAYMENT_SUCCESS
* PAYMENT_FAILED
* SUBSCRIPTION_RENEWED

---

# 🧮 3. PARTITIONING STRATEGY

This is where senior candidates stand out.

---

## Playback events

High volume.

Assume:

* 45M concurrent streams
* heartbeat every 30 sec

```
1.5M events/sec peak
```

### Partition count

Target:

```
~50K events/sec per partition
```

So:

```
1.5M / 50K ≈ 30 partitions minimum
```

In practice:

➡️ **200–500 partitions**

Why?

* Future growth
* Consumer parallelism

---

# 🔑 4. KEY SELECTION RULES

| Domain   | Key       | Why                   |
| -------- | --------- | --------------------- |
| Playback | profileId | Ordering per user     |
| User     | profileId | Cache correctness     |
| Catalog  | titleId   | Last-write-wins       |
| Billing  | accountId | Financial correctness |

---

# 📦 5. EVENT SCHEMA DESIGN

Use:

* Avro / Protobuf
* Schema Registry

Example:

```json
{
  "eventId": "uuid",
  "eventType": "PLAYBACK_STARTED",
  "profileId": "p123",
  "titleId": "t456",
  "timestamp": 1234567890,
  "deviceType": "TV",
  "positionMs": 0
}
```

---

# ⏱ 6. RETENTION STRATEGY

| Topic               | Retention                |
| ------------------- | ------------------------ |
| Playback raw events | 24–72 hours              |
| Aggregated history  | Compact + long retention |
| User profile events | Compacted                |
| Catalog updates     | Compacted                |

---

# 🧊 7. COMPACTION USE CASES

Enable for:

```
user.profile-events
catalog.index-updates
```

So latest state is always available.

---

# 🚀 8. SPRING BOOT IMPLEMENTATION

### Producer

```java
KafkaTemplate<String, PlaybackEvent>
```

### Consumer

```java
@KafkaListener(
  topics = "playback.session-events",
  concurrency = "20"
)
```

---

# ⚠️ 9. FAILURE & RETRY STRATEGY

* Retry topic
* DLQ topic

Example:

```
playback.session-events.retry
playback.session-events.dlq
```

---

# 🏆 10. PRINCIPAL-LEVEL CALLOUTS (SAY THESE)

### 1️⃣ Backpressure isolation

> “Heavy playback traffic cannot impact billing or user domains because topics are isolated.”

### 2️⃣ Reprocessing capability

> “Raw events are retained so we can rebuild derived state.”

### 3️⃣ Exactly-once not needed

> “We use idempotent consumers with event IDs.”

### 4️⃣ Event versioning

> “Schema evolution via backward compatibility.”

---

# 🎯 11. END-TO-END FLOW EXAMPLE

User presses play:

```
Playback Service
 → produces PLAYBACK_STARTED
   → Viewing History updates progress
   → Recommendation updates features
   → Analytics counts concurrency
```

No synchronous coupling.

---

# ⭐ MIC-DROP INTERVIEW LINE

> “Partitioning by profileId guarantees per-user ordering while allowing horizontal consumer scaling, and log compaction lets downstream services rebuild state without tight coupling.”

---

# 🚀 IF YOU WANT NEXT

We can go even deeper into:

* Exact partition sizing with real numbers
* Consumer group scaling math
* Kafka cluster sizing (brokers, disks, throughput)
* Transactional producers
* Outbox pattern with Spring

Just say:

> **“Kafka cluster sizing for this design.”**
