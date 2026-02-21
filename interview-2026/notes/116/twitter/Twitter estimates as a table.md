Here’s a **clean, interview-ready estimation table for Twitter/X system design** — the way a **Principal engineer presents numbers quickly and confidently**.

---

# 🐦 Twitter System Design — Back-of-the-Envelope Estimation

## 📌 Assumptions

| Parameter              | Value   | Notes                   |
| ---------------------- | ------- | ----------------------- |
| DAU                    | 300 M   | Daily active users      |
| Tweeting users         | 20%     | Users who post daily    |
| Tweets per active user | 2 / day | Avg                     |
| Avg tweet size         | 1 KB    | Text + metadata pointer |
| Read:Write ratio       | 100 : 1 | Feed heavy              |
| Peak factor            | 5×      | Traffic spikes          |

---

## 🧮 Write Traffic

| Metric                  | Calculation    | Result          |
| ----------------------- | -------------- | --------------- |
| Tweets per day          | 300M × 20% × 2 | **120M/day**    |
| Tweets per second (avg) | 120M / 86400   | **~1.4K/sec**   |
| Peak write TPS          | 1.4K × 5       | **~7K/sec**     |
| Write bandwidth (avg)   | 1.4K × 1 KB    | **~1.4 MB/sec** |
| Write bandwidth (peak)  | 7K × 1 KB      | **~7 MB/sec**   |

---

## 👀 Read Traffic (Timeline Loads)

Assume:

* Each DAU opens app **5 times/day**
* Each load fetches **50 tweets**

| Metric                 | Calculation  | Result                |
| ---------------------- | ------------ | --------------------- |
| Timeline requests/day  | 300M × 5     | **1.5B/day**          |
| Reads per second (avg) | 1.5B / 86400 | **~17K/sec**          |
| Peak read QPS          | 17K × 5      | **~85K/sec**          |
| Tweets served/sec      | 85K × 50     | **~4.25M tweets/sec** |
| Read bandwidth         | 4.25M × 1 KB | **~4.25 GB/sec**      |

🚨 **This is the real scaling problem — not writes.**

---

## 💾 Storage Estimation

### Tweets per year

```
120M × 365 ≈ 43.8B tweets/year
```

### Raw storage per year

```
43.8B × 1 KB ≈ 43.8 TB/year
```

### With replication (×3)

```
≈ 131 TB/year
```

---

## 🧊 Cache Requirement

Hot timeline cache:

Assume:

* 50M highly active users
* 50 tweets cached per user
* 1 KB per tweet reference

```
50M × 50 × 1 KB = 2.5 TB
```

---

## 🌟 Celebrity Fanout Impact

Assume:

* 1 celebrity with 100M followers

| Metric         | Calculation           | Result                |
| -------------- | --------------------- | --------------------- |
| Fanout writes  | 100M timeline inserts | **100M operations**   |
| Data generated | 100M × 1 KB           | **~100 GB per tweet** |

➡️ Must be:

* Async
* Queue-based
* Rate-controlled

---

# 🎯 One-Line Principal-Level Insight

> “Writes are cheap (~7K TPS). Timeline reads dominate (~85K QPS, multi-GB/sec).
> The system is fundamentally a **fanout + cache invalidation problem**, not a storage problem.”

---

# ✅ How this scores in an interview

This table shows:

* Structured thinking
* Correct bottleneck identification
* Capacity planning mindset
* Realistic peak handling

---

# 🚀 If you want next

I can give you:

* Twitter **component sizing** (Redis nodes, Kafka partitions, DB shards)
* Multi-region numbers
* Cost-aware estimation
* “Say this verbally in 90 seconds” version

Just say: **“Turn this into a verbal answer.”**
