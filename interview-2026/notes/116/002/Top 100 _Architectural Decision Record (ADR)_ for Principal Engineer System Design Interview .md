To pass a Principal Engineer (PE) interview, you don't just need to know the "right" answer; you need to demonstrate **Trade-off Analysis**. A PE knows that every architectural choice is a "loan" with an interest rate.

Below are the top categories of ADRs often discussed in high-level interviews, categorized by the "Pressure Points" they address.

---

## 1. Data Consistency & Storage (The "Foundation" ADRs)

These decisions dictate how your system handles the "Source of Truth."

1. **Relational vs. NoSQL:** Choosing Postgres for ACID compliance vs. Cassandra for high-volume writes.
2. **Consistency Level:** Opting for **Eventual Consistency** (to maintain high availability) vs. **Strong Consistency** (using Paxos/Raft).
3. **Database Sharding Key:** Sharding by `user_id` (localization) vs. `geography` (compliance) vs. `time` (log data).
4. **Read-Through vs. Write-Through Caching:** Deciding when to update the cache (latency vs. freshness).
5. **Storage Engine:** Choosing **LSM-Trees** (write-heavy, e.g., LevelDB) vs. **B-Trees** (read-heavy, e.g., Postgres).
6. **Data Archival:** TTL (Time-to-Live) policies for "Cold Data" vs. keeping everything in "Hot Storage."

---

## 2. Communication & Integration (The "Wiring" ADRs)

How services talk to each other defines the system's fragility.

7. **Synchronous vs. Asynchronous:** Using **gRPC/REST** for immediate feedback vs. **Kafka/RabbitMQ** for decoupling.
8. **Message Delivery Guarantees:** Deciding if the system needs **Exactly-Once** (expensive) vs. **At-Least-Once** (requires idempotency).
9. **API Versioning:** Header-based versioning vs. URL pathing (`/v1/`) for backward compatibility.
10. **Schema Evolution:** Using **Protocol Buffers** or **Avro** (strict schemas) vs. **JSON** (flexibility).
11. **Service Discovery:** Client-side discovery (Netflix Eureka) vs. Server-side (AWS ALB).

---

## 3. Resilience & Stability (The "Insurance" ADRs)

A PE is judged by how the system behaves when things break.

12. **Circuit Breaking:** When to fail fast to prevent a "cascading failure" across the microservices.
13. **Rate Limiting:** Implementing **Token Bucket** vs. **Fixed Window** at the API Gateway level.
14. **Bulkheading:** Isolating resources (e.g., separate thread pools) so one slow service doesn't starve the whole system.
15. **Backpressure:** How a downstream service signals an upstream service to slow down.
16. **Deployment Strategy:** **Blue-Green** (fast rollback) vs. **Canary** (low risk).

---

## 4. Performance & Scalability (The "Speed" ADRs)

17. **Global Load Balancing:** **Anycast IP** vs. **Latency-based DNS** (Route53).
18. **CDN Strategy:** Edge-computing (Cloudflare Workers) vs. simple static asset caching.
19. **Connection Pooling:** Managing DB connections at the application level vs. using a proxy (like PgBouncer).
20. **State Management:** Choosing **Stateless Services** (easier to scale) vs. **Sticky Sessions** (better for specific real-time apps).

---

## The "PE Interview" ADR Template

In an interview, don't just say "I'd use Kafka." Use this verbal ADR structure:

* **The Context:** "We have a 10:1 Read/Write ratio and 100M daily active users."
* **The Problem:** "The current monolithic DB is locking during peak traffic."
* **The Options:** "We considered vertical scaling, but it's a dead end. We looked at Vitess (sharded MySQL) and Cassandra."
* **The Decision:** "I chose **Cassandra**."
* **The Trade-off (The "Principal" part):** "We gain massive write scalability, but we lose the ability to perform complex JOINs. We will handle the JOINs at the application layer or via Denormalization."

---

### How to use these in a 45-minute interview:

Pick **three** of these ADRs to "anchor" your design. For a Twitter design, your anchors are:

1. **Hybrid Fan-out** (Push vs. Pull).
2. **Sharding by UserID** (Locality of data).
3. **Eventual Consistency** (Prioritizing Availability).

**Would you like me to expand on any specific category—for example, the top 10 ADRs for "Security & Compliance" in system design?**