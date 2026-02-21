Here’s a **Principal-level, interview-ready Netflix back-of-the-envelope estimation table** — focused on the real bottleneck: **video streaming bandwidth**, not metadata.

---

# 🎬 Netflix System Design — Estimation Table

## 📌 Assumptions

| Parameter             | Value      | Notes                              |
| --------------------- | ---------- | ---------------------------------- |
| Monthly active users  | 300 M      | Rounded global scale               |
| Concurrent viewers    | 15% of MAU | Peak hour                          |
| Concurrent streams    | **45 M**   | 300M × 15%                         |
| Avg streaming bitrate | 5 Mbps     | HD blended avg (mobile → 4K mix)   |
| Peak factor           | 1.5×       | Evening surge                      |
| Catalog size          | 1 M videos | Including regional content         |
| Avg video size        | 5 GB       | After encoding (multiple bitrates) |
| Replication factor    | 3×         | Durability                         |

---

# 🧮 Streaming Bandwidth (THE CORE METRIC)

| Metric                   | Calculation  | Result           |
| ------------------------ | ------------ | ---------------- |
| Total outbound bandwidth | 45M × 5 Mbps | **225 Tbps**     |
| In GB/sec                | 225 / 8      | **~28.1 TB/sec** |
| Peak bandwidth           | 28.1 × 1.5   | **~42 TB/sec**   |

🚨 This is why Netflix relies on **CDN + Open Connect edge nodes**.

---

# 👀 Streaming Requests

Assume:

* 1 stream → 1 segment request every 4 seconds

| Metric               | Calculation | Result               |
| -------------------- | ----------- | -------------------- |
| Segment requests/sec | 45M / 4     | **~11.25 M req/sec** |

---

# 💾 Storage Estimation

## Raw content storage

| Metric             | Calculation | Result     |
| ------------------ | ----------- | ---------- |
| Total catalog size | 1M × 5 GB   | **~5 PB**  |
| With replication   | 5 PB × 3    | **~15 PB** |

---

## Encoded multi-bitrate storage

Assume:

* 6 bitrate variants per video

```
5 PB × 6 = 30 PB
With replication → 90 PB
```

---

# 📥 Upload / Ingest Traffic

Assume:

* 1,000 new titles/month
* 5 GB mezzanine file

| Metric           | Calculation | Result         |
| ---------------- | ----------- | -------------- |
| Ingest per month | 1000 × 5 GB | **5 TB/month** |

(Small compared to streaming)

---

# 🧊 Metadata & User Data

| Component                   | Estimate             |
| --------------------------- | -------------------- |
| User profiles (300M × 2 KB) | ~600 GB              |
| Viewing history             | ~PB scale over years |
| Search index                | ~TBs                 |

👉 Tiny compared to video.

---

# ⚡ Cache (CDN Edge)

Assume:

* 20% catalog = 80% traffic (Zipf)

```
Hot data ≈ 1 PB globally distributed at edge
```

---

# 🌍 Multi-Region Strategy

| Layer                | Placement               |
| -------------------- | ----------------------- |
| Control plane        | AWS regions             |
| Video delivery       | ISP edge (Open Connect) |
| User personalization | Regional                |

---

# 🎯 Principal-Level Insight (Say this in interview)

> “Netflix is fundamentally a **global bandwidth distribution system**, not a database scaling problem.
> The primary challenge is serving **tens of terabits per second** via edge caching while keeping the control plane lightweight.”

---

# 🏆 Bottleneck Summary

| Area               | Challenge            | Solution           |
| ------------------ | -------------------- | ------------------ |
| Bandwidth          | Massive              | CDN / Open Connect |
| Startup latency    | First segment        | Edge pre-warm      |
| Adaptive streaming | Network variability  | ABR profiles       |
| Hot content        | Skewed access        | Edge caching       |
| Global scale       | Cross-region latency | Local delivery     |

---

# ✅ 60-Second Verbal Version (Interview Gold)

> “With ~45M concurrent viewers at ~5 Mbps, we’re serving ~225 Tbps (~28 TB/sec).
> That immediately tells us the system is CDN-driven.
> Storage is PB-scale but manageable; the real problem is edge distribution, adaptive bitrate streaming, and hot-content caching.”

---

# 🚀 If you want next

I can give you:

* Full **45-minute Netflix answer flow**
* **Component sizing** (CDN nodes, cache per region)
* **Cost-aware estimation**
* Comparison with **YouTube vs Netflix architecture**

Just say: **“Give the 45-minute Netflix design flow.”**
