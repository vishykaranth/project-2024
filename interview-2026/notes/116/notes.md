- I optimize for P95 latency, not average, because it represents real user experience and exposes contention, saturation, and coordination delays in distributed systems.
- P95 latency is the performance experienced by almost all users and the first indicator that your distributed system is running out of headroom.
- I optimize for P95 for cost-efficient performance, but I track P99 to detect saturation, coordinated contention, and fan-out amplification.
  P99 is a resilience metric, not just a latency metric.
- At scale you don’t eliminate tail latency — you design for tail tolerance using hedged requests, adaptive concurrency limits, bulkheads, and load shedding so a single slow dependency cannot amplify across a fan-out graph.

## Twitter 
~~~textmate
┌─────────────────────────────────────────────────────┐
│              TWITTER SYSTEM DESIGN                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Users:          500M total, 200M DAU               │
│  Tweets:         500M per day (6K/sec avg)          │
│  Reads:          1B timeline per day (12K/sec avg)  │
│  Write Path:     PostgreSQL → Kafka → Redis fanout  │
│  Read Path:      Redis timeline → Hydrate from cache│
│  Sharding:       4096 shards by user_id             │
│  Cache:          Redis (timelines + tweets)         │
│  Search:         Elasticsearch (near real-time)     │
│  Media:          S3 + CloudFront CDN                │
│  Cost:           $134K/month ($0.00027/user)        │
│  Latency:        p95 read: 50ms, write: 200ms       │
│  Availability:   99.9% (multi-region)               │
│                                                     │
└─────────────────────────────────────────────────────┘
~~~