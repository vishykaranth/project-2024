To present these as a Principal Engineer would, we must move beyond the "how" and document the **rationalized "why."** Here are the three formal Architecture Decision Records (ADRs) for the Twitter Home Timeline anchors.

---

## ADR 001: Hybrid Fan-out Strategy for Timeline Delivery

**Status:** Proposed / Accepted

**Context:** Twitter has a highly skewed power-law distribution of followers. A "push-only" model (writing a tweet to every follower's inbox) creates a "write explosion" for celebrities (e.g., 100M followers). A "pull-only" model (gathering tweets from everyone you follow at read-time) is too slow for users following thousands of people.

### Decision

We will implement a **Hybrid Fan-out** model based on user "Influence" scores.

* **Push (Fan-out on Write):** For users with  followers (e.g., ), their tweets are pushed into the Redis Timeline Caches of all followers immediately.
* **Pull (Fan-out on Load):** For "Celebrity" users ( followers), tweets are stored in a specific "Celebrity Tweet Store." We do **not** push these to followers.

### Consequences

* **Pros:** Sub-100ms latency for the vast majority of users; prevents system-wide "In-memory" cache exhaustion during viral events.
* **Cons:** Increased complexity in the Timeline Service. It must now perform a "Merge-Sort" of the cached timeline (Push) and the celebrity tweets (Pull) at read-time.

---

## ADR 002: Sharding Strategy via UserID (Data Locality)

**Status:** Accepted

**Context:** To achieve  latency, we must minimize "network hops." If a user's timeline data is scattered across 50 different database nodes, the "Scatter-Gather" overhead and tail latency () will exceed our budget.

### Decision

We will use **Consistent Hashing** to shard the Timeline Cache and Social Graph by `user_id`.

* All data required to construct `User A`'s view of the world (their list of followers and their precomputed timeline) will reside on the same logical shard.
* We will use **Virtual Nodes** (e.g., 256 per physical machine) to ensure that if one physical node fails, its load is distributed evenly across the remaining cluster rather than overwhelming a single neighbor.

### Consequences

* **Pros:** Highly predictable read performance. Most timeline requests become a single-node "point lookup."
* **Cons:** **Hot Sharding.** If a specific user is extremely active or famous, the shard housing their ID may experience higher CPU/Memory load. We mitigate this by using a separate "Hot Item Cache" for global trending data.

---

## ADR 003: Eventual Consistency (Prioritizing Availability)

**Status:** Accepted (Architectural Trade-off)

**Context:** According to the **CAP Theorem**, in the event of a network partition (common in global distributed systems), we must choose between Consistency and Availability. For a social media feed, "Strong Consistency" (ensuring every user sees a tweet at the exact same microsecond) is less important than the site being "up."

### Decision

We will adopt an **AP (Available and Partition-Tolerant)** model with **Eventual Consistency**.

* We use asynchronous replication for our Tweet Store and Timeline Caches.
* The "Like" count and "Retweet" counts will be updated via a "Buffered Counter" pattern—incremental updates that may lag by a few seconds but ensure the system remains responsive under heavy load.

### Consequences

* **Pros:** The system can survive data center outages. Users can still scroll their timelines even if the "Follower Service" is momentarily lagging.
* **Cons:** The "Ghost Tweet" or "Out-of-order" phenomenon. A user might see a reply before the original tweet if they refresh at the exact moment of a partition. We accept this as a UX trade-off for high speed.

---

### Summary Table for Interview Quick-Reference

| Decision | Primary Driver | Alternative Considered | Why Rejected? |
| --- | --- | --- | --- |
| **Hybrid Fan-out** | Scalability | Pull-only | Too slow for high "Follow" counts (). |
| **UserID Sharding** | Low Latency | TweetID Sharding | High "Scatter-Gather" latency for reads. |
| **Eventual Consistency** | Availability | Strong Consistency | Would cause site-wide timeouts during partitions. |

**Would you like me to create a "Failure Mode Analysis" for these ADRs—specifically what happens if the Redis Shard for a specific UserID goes down?**