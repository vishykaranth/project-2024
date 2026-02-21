Here is your **⏱️ 45-minute Twitter (X) System Design interview flow** — structured exactly the way strong **Senior / Staff candidates** drive the conversation.

You can **literally follow this script on a whiteboard**.

---

# 🧭 0 – 3 min → Clarify Requirements

**Start by framing the problem (this shows seniority).**

**Functional**

* Users post tweets (text + media)
* Follow/unfollow users
* Home timeline
* User profile timeline
* Like/retweet

**Non-Functional**

* Low latency timeline load (<200 ms)
* High availability (multi-region later)
* Massive scale
* Eventually consistent is acceptable

**Ask for scale assumptions**

> “Let’s assume 300M DAU, 500M tweets/day, read-heavy system (100:1). OK?”

---

# 🔢 3 – 8 min → Back-of-the-Envelope Estimation

### Traffic

* 500M tweets/day → ~6K writes/sec
* Timeline reads → 100× writes → **600K reads/sec**

### Storage

* 1 tweet ≈ 1 KB
  → 500M/day → **500 GB/day**
  → ~180 TB/year

### Fanout impact

* Avg followers = 200
  → 500M × 200 = **100B timeline inserts/day**

Then say:

> “This immediately tells us fanout strategy is the core design decision.”

---

# 🏗️ 8 – 12 min → High-Level Architecture

Draw:

```
Client
  ↓
API Gateway
  ↓
Stateless App Servers
  ↓
Core Services:
  - Tweet Service
  - User Service
  - Timeline Service
  - Social Graph Service
  - Fanout Service
```

Backends:

* Cache (Redis)
* Tweet DB (NoSQL)
* Graph DB / KV
* Blob store + CDN

---

# 🧵 12 – 20 min → Data Modeling

### Tweet

* tweet_id (Snowflake)
* user_id
* text
* media_id
* timestamp

### Social Graph

Two tables:

* following(user → list)
* followers(user → list)

Explain:

> “We store both for fast reads.”

### Home Timeline

Precomputed:

```
user_id → [tweet_id, tweet_id, tweet_id]
```

---

# ⚡ 20 – 30 min → Timeline Generation (Core Deep Dive)

## Option 1: Fanout-on-write

When user tweets:

* Push to all followers’ home timeline

✅ Fast reads
❌ Massive write amplification

---

## Option 2: Fanout-on-read

At request time:

* Pull tweets from all followees
* Merge + rank

✅ Cheap writes
❌ Slow reads

---

## 🎯 Hybrid Strategy (Correct Answer)

Normal users → **fanout-on-write**
Celebrities → **fanout-on-read**

Define celebrity threshold:

> “Users with >1M followers.”

Also:

* Only precompute for **active followers**

---

# 🧠 30 – 34 min → Caching Strategy

Cache:

* Home timeline (per user)
* Tweet objects
* User profiles

Mention:

* Write-through or async cache update
* TTL + background refresh

---

# 🗃️ 34 – 37 min → Storage & Scaling

### Tweet storage

* Wide-column / NoSQL (high write throughput)

### Social graph

* KV store / graph partitioned by user_id

### Media

* Object storage + CDN

### ID generation

* Snowflake for time-ordered IDs

---

# 🚀 37 – 41 min → Feed Ranking & Pagination

Ranking signals:

* Recency
* Engagement
* ML ranking

Pagination:

* Use **tweet_id as cursor**
* `since_id`, `max_id` (no offset pagination)

---

# 🧯 41 – 43 min → Reliability & Hotspot Handling

* Rate limiting for celebrities
* Queue-based fanout
* Backpressure handling
* Idempotent writes
* Retry with dedupe

Hot user protection:

> “Shard by user_id to avoid hot partitions.”

---

# 🌍 43 – 45 min → Multi-Region & Trade-offs

* Active-active timelines
* Geo-partition users
* Eventual consistency acceptable

Call out trade-offs:

* Write vs read latency
* Consistency vs availability
* Cost vs performance

---

# 🏁 Strong Closing Statement

End with:

> “The core of the system is the hybrid fanout model, supported by heavy caching, precomputation for active users, and cursor-based pagination. From here, we can evolve into ML-ranked feeds and multi-region active-active deployment.”

---

# ⭐ Want to level this up to **Staff / Principal signal**?

Next I can:

* Turn this into a **mock interview where I interrupt, challenge, and push back**
* Give you **expected follow-up questions + winning answers**
* Run a **live whiteboard drill with time pressure** (your previous prep style).
