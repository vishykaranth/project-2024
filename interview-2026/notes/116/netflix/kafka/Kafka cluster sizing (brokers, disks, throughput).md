Here’s your **🏗️ Kafka CLUSTER SIZING for Netflix-scale control-plane events** —
with **real numbers, formulas, and interview talk-track**.

This is exactly how a **Principal engineer** explains capacity planning.

---

# 🧭 0. WHAT WE ARE SIZING

Cluster for:

* Playback session events
* Viewing history
* Recommendation signals
* User/profile changes
* Catalog updates

👉 Not video streaming.

---

# 📌 1. INPUT ASSUMPTIONS

From earlier:

### Concurrent streams

```
45M
```

### Heartbeat interval

```
30 seconds
```

### Events per stream

```
START + HEARTBEATS + STOP
≈ 1 event / 30 sec
```

---

## 🔢 Peak event rate

```
45M / 30 ≈ 1.5M events/sec
```

Add:

* user events
* search
* catalog

➡️ **Round to 2M events/sec peak**

---

## 📦 Average event size

Playback event (Avro/Protobuf):

```
~500 bytes
```

---

# 🚀 2. TOTAL WRITE THROUGHPUT

```
2M × 500 bytes = 1 GB/sec ingest
```

With replication factor = 3:

```
1 GB/sec × 3 = 3 GB/sec broker network write
```

---

# 💾 3. RETENTION REQUIREMENT

### Raw events retained for 48 hours

```
1 GB/sec × 86400 × 2
≈ 172 TB
```

With replication:

```
≈ 516 TB total disk
```

---

# 🧮 4. PARTITION COUNT

Target per partition:

```
50 MB/sec max
```

So:

```
1 GB/sec / 50 MB/sec = 20 partitions minimum
```

But for parallelism & growth:

✅ **300–500 partitions**

---

# 🖥️ 5. BROKER SIZING

### Disk per broker target

Keep below:

```
8–10 TB usable per broker
```

So:

```
516 TB / 10 TB ≈ 52 brokers
```

✅ Round → **60 brokers**

---

# ⚙️ 6. PER-BROKER LOAD

### Network

Ingress:

```
1 GB/sec ÷ 60 ≈ 17 MB/sec
```

Replication traffic:

```
≈ 50 MB/sec
```

Total:

```
~70 MB/sec per broker
```

✔ Very safe for 10–25 Gb NICs

---

### Disk write per broker

```
~17 MB/sec
```

Easy for modern SSDs.

---

# 🧠 7. CONSUMER SCALING

If:

```
400 partitions
```

Max parallel consumers per group:

```
400
```

So you can run:

* 200 Viewing History consumers
* 200 Recommendation consumers

independently.

---

# 🧊 8. STORAGE LAYOUT

Per broker:

```
~9 TB usable SSD
```

Disk type:

✅ NVMe / SSD
❌ HDD (too slow for this ingest)

---

# 🌐 9. RACK / AZ AWARENESS

Replication factor = 3

Distribute across:

```
3 availability zones
```

So:

* Each partition has replicas in different AZs
* Survive full AZ loss

---

# 🔥 10. PRODUCER CONFIG (SPRING)

```properties
acks=all
enable.idempotence=true
compression.type=zstd
batch.size=128KB
linger.ms=5
```

Compression reduces:

* Disk
* Network
  by **60–80%**

---

# 📊 11. WITH COMPRESSION (REALISTIC NUMBERS)

If 70% reduction:

### Effective ingest

```
1 GB/sec → 300 MB/sec
```

Disk:

```
516 TB → ~155 TB
```

Brokers needed:

✅ **~20–25 brokers**

This is what strong candidates call out.

---

# 🏆 12. PRINCIPAL-LEVEL CALLOUTS

Say these in interview:

### 1️⃣ Network, not disk, is the real limit

> “Kafka is typically network-bound at scale.”

---

### 2️⃣ Compression changes everything

> “Cluster size drops by 3× with zstd.”

---

### 3️⃣ Retention drives cost

> “Storage is a business decision, not a technical one.”

---

### 4️⃣ Partition count is a concurrency lever

Not just throughput.

---

# 🧾 13. FINAL SIZING SUMMARY

### Without compression

| Component | Value    |
| --------- | -------- |
| Ingress   | 1 GB/sec |
| Disk      | 516 TB   |
| Brokers   | ~60      |

### With compression (realistic)

| Component | Value      |
| --------- | ---------- |
| Ingress   | 300 MB/sec |
| Disk      | 155 TB     |
| Brokers   | **20–25**  |

---

# 🎯 MIC-DROP INTERVIEW LINE

> “With zstd compression and RF=3, a 25-broker Kafka cluster with ~400 partitions easily sustains 2M events/sec while allowing independent horizontal scaling of consumer groups.”

---

# 🚀 IF YOU WANT NEXT

We can go deeper into:

* Kubernetes deployment for Kafka
* Tiered storage sizing
* Consumer lag capacity planning
* MirrorMaker for multi-region
* Cost estimation per day

Just say:

> **“Multi-region Kafka strategy for this design.”**
