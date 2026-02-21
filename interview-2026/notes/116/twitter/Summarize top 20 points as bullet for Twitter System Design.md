Here are the **top 20 crisp, interview-ready bullets** for **Twitter (X) System Design** — optimized for whiteboard recall and discussion flow.

---

### 🧠 Core Requirements

1. Define **functional vs non-functional requirements** (post tweet, timeline, follow, like, high availability, low latency).
2. Clarify **read-heavy system** → timelines dominate traffic.
3. Estimate **DAU, tweets/day, read/write ratio**.

---

### 📦 Capacity & Back-of-the-Envelope

4. Calculate **tweet write QPS** and **timeline read QPS**.
5. Do **storage estimation** (tweet text, media, indexes).
6. Perform **bandwidth estimation** for home timeline delivery.

---

### 🗂️ High-Level Architecture

7. API Gateway → stateless app servers → services (Tweet, User, Timeline, Fanout).
8. Use **load balancers + horizontal scaling** everywhere.

---

### 🧵 Data Modeling

9. **Tweet table**: tweet_id (snowflake), user_id, text, media_ref, timestamp.
10. **Follow graph** stored as adjacency list (followers / following).

---

### 🚀 Timeline Generation (The Heart of the System)

11. **Fanout-on-write** for normal users → push to followers’ home timeline.
12. **Fanout-on-read** for celebrities → pull at request time.
13. Hybrid strategy based on **follower count threshold**.

---

### 🌟 Celebrity Problem

14. Avoid millions of writes per tweet → use **lazy loading + caching**.
15. Precompute only for **active followers**.

---

### 🧰 Caching Strategy

16. Cache **home timeline**, **user timeline**, and **tweet objects** (Redis/Memcache).
17. Use **cache invalidation via write-through or async update**.

---

### 🗃️ Storage Choices

18. **Distributed SQL/NoSQL** for tweets (high write throughput).
19. **Graph / KV store** for follow relationships.
20. **Blob storage + CDN** for media.

---

## ⚡ If interviewer pushes deeper, talk about:

* Snowflake ID generation
* Hot key mitigation
* Ranking & ML timeline
* Pagination (since_id / max_id)
* Eventual consistency
* Rate limiting
* Observability

---

✅ If you want, next I’ll:

* Turn this into a **perfect 45-minute interview answer flow**
* Or start **grilling you with follow-up questions like a real interviewer** (Principal/Staff level).


