# 🐦 Twitter System Design - Complete Capacity Estimates

*Detailed back-of-the-envelope calculations for Principal Engineer interviews*

---

## Table of Contents

1. [Traffic Estimates](#traffic-estimates)
2. [Storage Estimates](#storage-estimates)
3. [Bandwidth Estimates](#bandwidth-estimates)
4. [Memory/Cache Estimates](#memory-cache-estimates)
5. [Database Capacity](#database-capacity)
6. [Server Count Estimates](#server-count-estimates)
7. [Cost Estimates](#cost-estimates)
8. [Interview Calculation Framework](#interview-calculation-framework)

---

## Traffic Estimates

### User Base Assumptions

```
Total registered users: 500 million
Daily Active Users (DAU): 200 million (40% of total)
Monthly Active Users (MAU): 350 million (70% of total)

User distribution:
  Heavy users (10%): 20M users, 50 tweets/day
  Regular users (30%): 60M users, 5 tweets/day
  Light users (60%): 120M users, 0.5 tweets/day

Geographic distribution:
  North America: 30% (60M DAU)
  Europe: 25% (50M DAU)
  Asia: 35% (70M DAU)
  Rest of World: 10% (20M DAU)
```

### Tweet Volume Calculations

```
DAILY TWEET VOLUME:

Heavy users:  20M × 50 tweets  = 1,000M tweets/day
Regular users: 60M × 5 tweets  =   300M tweets/day
Light users:  120M × 0.5 tweets =    60M tweets/day
────────────────────────────────────────────────────
TOTAL:                            1,360M tweets/day

Conservative estimate: 500M tweets/day
(Accounting for user behavior variance)

TWEETS PER SECOND (Average):
500,000,000 tweets/day ÷ 86,400 seconds = 5,787 tweets/sec

Peak traffic multiplier: 2x (during events, breaking news)
Peak tweets/sec = 5,787 × 2 = 11,574 tweets/sec

Round to: ~6,000 tweets/sec avg, ~12,000 tweets/sec peak
```

### Read Traffic Calculations

```
TIMELINE READS:

Assumptions:
  - Average user opens Twitter: 10 times/day
  - Each session: User scrolls through ~3 timeline loads
  - Each timeline load: 50 tweets fetched

Daily timeline reads per user:
  10 sessions × 3 loads = 30 timeline fetches/day

Total daily timeline reads:
  200M DAU × 30 = 6,000M timeline reads/day

Timeline reads per second (average):
  6,000,000,000 ÷ 86,400 = 69,444 reads/sec

Peak traffic multiplier: 3x
  69,444 × 3 = 208,333 reads/sec (peak)

Round to: ~70,000 reads/sec avg, ~200,000 reads/sec peak

READ-TO-WRITE RATIO:
  70,000 reads/sec ÷ 6,000 writes/sec = 11.7:1
  
Round to: 12:1 read-to-write ratio (or 100:1 if counting individual tweet views)
```

### User Profile & Search Queries

```
USER PROFILE VIEWS:
  Assumptions: 20% of DAU view 5 profiles/day
  200M × 0.20 × 5 = 200M profile views/day
  200M ÷ 86,400 = 2,314 profile views/sec

SEARCH QUERIES:
  Assumptions: 30% of DAU search 3 times/day
  200M × 0.30 × 3 = 180M searches/day
  180M ÷ 86,400 = 2,083 searches/sec

TRENDING TOPICS:
  Assumptions: 50% of DAU check trends 2 times/day
  200M × 0.50 × 2 = 200M trend requests/day
  200M ÷ 86,400 = 2,314 trend requests/sec
```

### Total API Traffic Summary

```
╔══════════════════════════════════════════════════════════════╗
║               TOTAL API REQUESTS PER SECOND                   ║
╠══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Operation          Average      Peak        Percentage      ║
║  ─────────────────────────────────────────────────────────   ║
║  Timeline reads     70,000      200,000         87%          ║
║  Tweet writes        6,000       12,000          8%          ║
║  Profile views       2,314        7,000          3%          ║
║  Search queries      2,083        6,000          3%          ║
║  ─────────────────────────────────────────────────────────   ║
║  TOTAL:            ~80,000      ~225,000        100%         ║
║                                                               ║
╚══════════════════════════════════════════════════════════════╝

Note: Read-heavy workload (90%+ reads)
```

---

## Storage Estimates

### Tweet Storage Calculations

```
SINGLE TWEET SIZE:

Text content: 280 chars × 2 bytes (UTF-8) = 560 bytes
Metadata:
  - tweet_id (BIGINT): 8 bytes
  - user_id (BIGINT): 8 bytes
  - created_at (TIMESTAMP): 8 bytes
  - likes_count (INT): 4 bytes
  - retweets_count (INT): 4 bytes
  - reply_count (INT): 4 bytes
  - media_ids (JSON, avg): 50 bytes
  - hashtags (JSON, avg): 30 bytes
  - mentions (JSON, avg): 30 bytes
  - location (VARCHAR, optional): 20 bytes
  - is_retweet (BOOLEAN): 1 byte
  - reply_to_id (BIGINT, nullable): 8 bytes
  ──────────────────────────────────
Metadata total: ~175 bytes

Total per tweet: 560 + 175 = 735 bytes
Round to: ~1 KB per tweet (accounting for DB overhead, indexes)

DAILY STORAGE:
  500M tweets/day × 1 KB = 500 GB/day

YEARLY STORAGE:
  500 GB/day × 365 = 182.5 TB/year

5-YEAR STORAGE:
  182.5 TB/year × 5 = 912.5 TB ≈ 1 PB

With replication (3x for safety):
  1 PB × 3 = 3 PB total storage needed for 5 years of tweets
```

### Media Storage Calculations

```
MEDIA ASSUMPTIONS:

Tweet with media: 30% of all tweets
  500M tweets/day × 0.30 = 150M media tweets/day

Media type breakdown:
  Images: 80% → 120M images/day
  Videos: 15% → 22.5M videos/day
  GIFs: 5% → 7.5M GIFs/day

STORAGE PER MEDIA TYPE:

Images:
  Original (high-res): 2 MB average
  Thumbnail (150x150): 20 KB
  Small (400x400): 100 KB
  Medium (800x800): 300 KB
  Large (1200x1200): 800 KB
  Total per image: 2 + 0.02 + 0.1 + 0.3 + 0.8 = 3.22 MB
  
  Daily: 120M images × 3.22 MB = 386 TB/day

Videos:
  Average video: 30 seconds
  Raw upload: 10 MB
  Transcoded (360p): 3 MB
  Transcoded (720p): 8 MB
  Transcoded (1080p): 15 MB
  Total per video: 10 + 3 + 8 + 15 = 36 MB
  
  Daily: 22.5M videos × 36 MB = 810 TB/day

GIFs:
  Average GIF: 2 MB
  Daily: 7.5M GIFs × 2 MB = 15 TB/day

TOTAL MEDIA STORAGE:
  Daily: 386 + 810 + 15 = 1,211 TB/day ≈ 1.2 PB/day
  Yearly: 1.2 PB/day × 365 = 438 PB/year
  
With CDN caching (reduce origin storage by 70%):
  Origin: 438 PB/year × 0.30 = 131 PB/year
  CDN: Cached dynamically, ~10 PB total across edge locations
```

### User Data Storage

```
USER PROFILE SIZE:

Per user:
  - user_id (BIGINT): 8 bytes
  - username (VARCHAR 15): 15 bytes
  - display_name (VARCHAR 50): 50 bytes
  - bio (VARCHAR 160): 160 bytes
  - location (VARCHAR 30): 30 bytes
  - website (VARCHAR 100): 100 bytes
  - profile_image_url (VARCHAR 200): 200 bytes
  - banner_image_url (VARCHAR 200): 200 bytes
  - created_at (TIMESTAMP): 8 bytes
  - followers_count (INT): 4 bytes
  - following_count (INT): 4 bytes
  - tweets_count (INT): 4 bytes
  - verified (BOOLEAN): 1 byte
  - protected (BOOLEAN): 1 bytes
  ────────────────────────────────────
  Total: ~785 bytes

Round to: 1 KB per user

Total user data:
  500M users × 1 KB = 500 GB

With indexes and overhead: ~1.5 TB
```

### Social Graph Storage

```
FOLLOW RELATIONSHIPS:

Average followers per user: 200
Average following per user: 200

Total relationships: 500M users × 200 = 100 billion relationships

Per relationship:
  - follower_id (BIGINT): 8 bytes
  - followee_id (BIGINT): 8 bytes
  - created_at (TIMESTAMP): 8 bytes
  ────────────────────────────────────
  Total: 24 bytes per relationship

Total storage:
  100 billion × 24 bytes = 2.4 TB

With indexes (2x overhead):
  2.4 TB × 2 = 4.8 TB ≈ 5 TB
```

### Total Storage Summary

```
╔══════════════════════════════════════════════════════════════╗
║                   TOTAL STORAGE REQUIREMENTS                  ║
╠══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Data Type              1 Year       5 Years     Growth       ║
║  ───────────────────────────────────────────────────────────  ║
║  Tweets (text/metadata)  183 TB        915 TB    183 TB/yr   ║
║  Media (images/video)    131 PB        655 PB    131 PB/yr   ║
║  User profiles             1 TB          2 TB    Minimal     ║
║  Social graph              5 TB         10 TB    Minimal     ║
║  Indexes & overhead       40 TB        200 TB    40 TB/yr    ║
║  ───────────────────────────────────────────────────────────  ║
║  TOTAL:                  131 PB        655 PB    131 PB/yr   ║
║                                                               ║
║  Note: 99% of storage is media (images/videos)               ║
║        Text data is negligible in comparison                 ║
║                                                               ║
╚══════════════════════════════════════════════════════════════╝
```

---

## Bandwidth Estimates

### Ingress (Upload) Bandwidth

```
TWEET WRITES (Text):
  6,000 tweets/sec × 1 KB = 6 MB/sec = 48 Mbps
  Peak: 12,000 tweets/sec × 1 KB = 12 MB/sec = 96 Mbps

MEDIA UPLOADS:
  150M media items/day total
  
  Distributed over 24 hours:
    150M ÷ 86,400 = 1,736 media uploads/sec
  
  Average media size: 5 MB (mix of images and videos)
    1,736 uploads/sec × 5 MB = 8,680 MB/sec = 69 Gbps
  
  Peak (3x): 69 Gbps × 3 = 207 Gbps

TOTAL INGRESS:
  Average: 48 Mbps + 69 Gbps ≈ 70 Gbps
  Peak: 96 Mbps + 207 Gbps ≈ 210 Gbps
```

### Egress (Download) Bandwidth

```
TIMELINE READS:
  70,000 timeline reads/sec
  Each timeline: 50 tweets × 1 KB = 50 KB
  70,000 × 50 KB = 3,500 MB/sec = 28 Gbps
  
  Peak: 200,000 reads/sec × 50 KB = 10,000 MB/sec = 80 Gbps

MEDIA DOWNLOADS (CDN):
  Assumptions:
    - 60% of timeline tweets have media
    - Users view 30% of media in their timeline
    - Average media download: 500 KB (thumbnail/small image)
  
  Media views per second:
    70,000 reads/sec × 50 tweets × 0.60 × 0.30 = 630,000 media views/sec
  
  Bandwidth:
    630,000 views/sec × 500 KB = 315,000 MB/sec = 2,520 Gbps
  
  Peak: 2,520 Gbps × 3 = 7,560 Gbps

PROFILE IMAGE LOADS:
  2,314 profile views/sec × 100 KB (profile image) = 231 MB/sec = 1.8 Gbps

TOTAL EGRESS:
  Average: 28 Gbps + 2,520 Gbps + 1.8 Gbps ≈ 2,550 Gbps (2.5 Tbps)
  Peak: 80 Gbps + 7,560 Gbps + 5.4 Gbps ≈ 7,645 Gbps (7.6 Tbps)
```

### Daily Data Transfer

```
DAILY INGRESS:
  70 Gbps × 86,400 seconds = 6,048,000 Gb = 756 TB/day

DAILY EGRESS:
  2,550 Gbps × 86,400 seconds = 220,320,000 Gb = 27,540 TB/day ≈ 27.5 PB/day

MONTHLY DATA TRANSFER:
  Ingress: 756 TB/day × 30 = 22.7 PB/month
  Egress: 27.5 PB/day × 30 = 825 PB/month

Note: 97% of bandwidth is media delivery via CDN
```

---

## Memory/Cache Estimates

### Timeline Cache (Redis)

```
TIMELINE CACHE REQUIREMENTS:

Per user timeline:
  - Store 800 most recent tweet IDs
  - Each tweet ID: 8 bytes (BIGINT)
  - Per timeline: 800 × 8 bytes = 6.4 KB

Total for all active users:
  200M DAU × 6.4 KB = 1.28 TB

Add 20% overhead (Redis metadata):
  1.28 TB × 1.20 = 1.54 TB

Cache all users (not just DAU) for instant access:
  500M users × 6.4 KB = 3.2 TB
  With overhead: 3.2 TB × 1.20 = 3.84 TB

Round to: 4 TB for timeline cache
```

### Tweet Object Cache (Redis)

```
HOT TWEET CACHE:

Assumptions:
  - Cache last 24 hours of tweets: 500M tweets
  - Each tweet object: 1 KB
  - Total: 500M × 1 KB = 500 GB

Cache only frequently accessed (Pareto principle):
  - 20% of tweets get 80% of views
  - Cache: 500M × 0.20 × 1 KB = 100 GB

Add user objects cache:
  - Cache top 10M active users: 10M × 1 KB = 10 GB

Add trending/viral tweets (extra hot cache):
  - Top 10,000 viral tweets: 10,000 × 1 KB = 10 MB

TOTAL TWEET CACHE:
  100 GB + 10 GB + 0.01 GB = 110 GB
  Round to: 150 GB (with overhead)
```

### Session Cache

```
ACTIVE SESSIONS:

Concurrent users (10% of DAU):
  200M × 0.10 = 20M concurrent users

Per session:
  - session_id: 32 bytes
  - user_id: 8 bytes
  - auth_token: 256 bytes
  - user_metadata: 200 bytes
  ────────────────────────────────
  Total: ~500 bytes per session

Total session cache:
  20M × 500 bytes = 10 GB

Round to: 15 GB (with overhead)
```

### Application Server Memory

```
PER SERVER MEMORY USAGE:

Application heap:
  - JVM heap: 4 GB
  - Connection pools: 500 MB
  - Local caches: 1 GB
  - Thread stacks: 500 MB
  ────────────────────────────────
  Total per server: ~6 GB

OS and overhead: 2 GB

Total per server: 8 GB

Recommended instance: 16 GB RAM (50% headroom)
```

### Total Cache Summary

```
╔══════════════════════════════════════════════════════════════╗
║                    TOTAL CACHE REQUIREMENTS                   ║
╠══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Cache Type              Size        Nodes (64GB each)       ║
║  ───────────────────────────────────────────────────────────  ║
║  Timeline cache (Redis)   4 TB             64 nodes          ║
║  Tweet objects (Redis)   150 GB             3 nodes          ║
║  Sessions (Redis)         15 GB             1 node           ║
║  Trending (Redis)          5 GB             1 node           ║
║  ───────────────────────────────────────────────────────────  ║
║  TOTAL:                 ~4.2 TB            69 nodes          ║
║                                                               ║
║  With replication (3x for HA):                               ║
║  Total nodes: 69 × 3 = 207 Redis nodes                       ║
║                                                               ║
╚══════════════════════════════════════════════════════════════╝
```

---

## Database Capacity

### PostgreSQL Sizing

```
SHARDING STRATEGY:

Total users: 500M
Shards: 4,096 (power of 2 for easy routing)

Users per shard:
  500M ÷ 4,096 = 122,070 users/shard

Tweets per shard per day:
  500M tweets/day ÷ 4,096 = 122,070 tweets/day/shard
  = 1.4 tweets/second/shard (very manageable)

Storage per shard (1 year):
  183 TB/year ÷ 4,096 = 44.7 GB/year/shard
  
Storage per shard (5 years):
  915 TB ÷ 4,096 = 223 GB/shard
  
With indexes (2x): 223 GB × 2 = 446 GB/shard

Recommended instance size per shard:
  db.r5.xlarge: 4 vCPU, 32 GB RAM, 500 GB storage
  
Number of database instances:
  Primary shards: 4,096
  Read replicas (2 per shard): 8,192
  Total: 12,288 PostgreSQL instances

Note: This is theoretical maximum. In practice:
  - Start with 256 shards (virtual shards map to physical)
  - Scale to 1,024 shards at 1B users
  - Scale to 4,096 shards at 2B users
```

### Database IOPS Requirements

```
WRITE IOPS PER SHARD:

Tweets: 1.4 writes/sec
Social graph updates: ~0.5 writes/sec
User updates: ~0.1 writes/sec
Engagement (likes/retweets): ~5 writes/sec
────────────────────────────────────────
Total: ~7 writes/sec per shard

Peak (2x): 14 writes/sec

READ IOPS PER SHARD:

Most reads served from cache (95% cache hit rate)
Only 5% hit database

Timeline queries (cache miss): ~3 reads/sec
User profile queries: ~1 read/sec
Social graph queries: ~2 reads/sec
────────────────────────────────────────
Total: ~6 reads/sec per shard

Peak: 18 reads/sec

TOTAL IOPS PER SHARD:
  Average: 13 IOPS
  Peak: 32 IOPS

PostgreSQL can handle 10,000+ IOPS easily
Conclusion: IOPS not a bottleneck
```

### Database Connection Pooling

```
CONNECTIONS PER SHARD:

Application servers: 200 servers
Connections per server to each shard: 2
Total: 200 × 2 = 400 connections per shard

Background workers: 50 servers
Connections: 50 × 1 = 50 connections

Total connections per shard: 450

PostgreSQL max connections: 1,000 (safe limit: 500)
Conclusion: Well within limits
```

---

## Server Count Estimates

### Application Servers

```
CAPACITY PER SERVER:

Assumption: Each server can handle 500 req/sec

CPU: 8 vCPUs @ 70% utilization
Memory: 16 GB (JVM heap: 8 GB)
Network: 1 Gbps
Instance type: c5.2xlarge

SERVERS NEEDED:

For average load (80,000 req/sec):
  80,000 ÷ 500 = 160 servers

For peak load (225,000 req/sec):
  225,000 ÷ 500 = 450 servers

With 50% safety margin:
  450 × 1.50 = 675 servers

With geographic distribution:
  US-East: 250 servers (37%)
  US-West: 150 servers (22%)
  Europe: 150 servers (22%)
  Asia: 125 servers (19%)
  Total: 675 servers

For autoscaling:
  Minimum: 200 servers (baseline)
  Maximum: 800 servers (Black Friday, Super Bowl)
  Average: 400 servers
```

### Background Worker Servers

```
TIMELINE FANOUT WORKERS:

Tweets per second: 6,000
Fanout per tweet: ~500 followers (average)
Redis writes per second: 6,000 × 500 = 3,000,000 writes/sec

Redis write capacity: 100,000 writes/sec per node
Nodes needed: 3,000,000 ÷ 100,000 = 30 Redis nodes

Worker servers (to drive Redis writes):
  Assuming 10,000 writes/sec per worker: 300 workers

KAFKA CONSUMERS:

Topics: tweets, likes, retweets, follows, etc.
Partitions per topic: 256
Consumers per partition: 1
Total consumers: ~1,000

Worker servers: 50 (each runs 20 consumers)

TOTAL BACKGROUND WORKERS: 350 servers
```

### Load Balancer Servers

```
LOAD BALANCER CAPACITY:

Using AWS Application Load Balancer (ALB):
  Capacity: ~500,000 req/sec per ALB
  
ALBs needed:
  225,000 req/sec ÷ 500,000 = 0.45 ALBs
  
Use 2 ALBs per region for HA: 8 ALBs total
```

### Total Server Summary

```
╔══════════════════════════════════════════════════════════════╗
║                     TOTAL SERVER COUNT                        ║
╠══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Server Type              Count       Instance Type          ║
║  ───────────────────────────────────────────────────────────  ║
║  Application servers        400       c5.2xlarge             ║
║  Background workers         350       c5.xlarge              ║
║  PostgreSQL (primary)       256       db.r5.xlarge           ║
║  PostgreSQL (replicas)      512       db.r5.xlarge           ║
║  Redis cache nodes          207       cache.r5.2xlarge       ║
║  Kafka brokers               24       r5.2xlarge             ║
║  Elasticsearch nodes         32       r5.2xlarge.search      ║
║  Load balancers               8       AWS ALB                ║
║  ───────────────────────────────────────────────────────────  ║
║  TOTAL:                   1,789 servers                      ║
║                                                               ║
╚══════════════════════════════════════════════════════════════╝
```

---

## Cost Estimates

### Compute Costs (EC2)

```
APPLICATION SERVERS:
  400 × c5.2xlarge × $0.34/hr × 730 hrs = $99,280/month

BACKGROUND WORKERS:
  350 × c5.xlarge × $0.17/hr × 730 hrs = $43,435/month

KAFKA BROKERS:
  24 × r5.2xlarge × $0.504/hr × 730 hrs = $8,830/month

Total compute: $151,545/month
```

### Database Costs (RDS PostgreSQL)

```
PRIMARY SHARDS:
  256 × db.r5.xlarge × $0.34/hr × 730 hrs = $63,590/month

READ REPLICAS:
  512 × db.r5.xlarge × $0.34/hr × 730 hrs = $127,180/month

Total database: $190,770/month
```

### Cache Costs (ElastiCache Redis)

```
REDIS NODES:
  207 × cache.r5.2xlarge × $0.568/hr × 730 hrs = $85,895/month
```

### Search Costs (Elasticsearch)

```
ELASTICSEARCH NODES:
  32 × r5.2xlarge.search × $0.568/hr × 730 hrs = $13,270/month
```

### Storage Costs

```
S3 STORAGE (Media):
  131 PB/year = 10.9 PB/month average
  
  S3 Standard (hot): 1 PB × $0.023/GB = $23,000/month
  S3 IA (warm): 3 PB × $0.0125/GB = $37,500/month
  S3 Glacier (cold): 6.9 PB × $0.004/GB = $27,600/month
  
  Total S3: $88,100/month

EBS STORAGE (Databases):
  768 DB instances × 500 GB × $0.10/GB = $38,400/month
```

### CDN Costs (CloudFront)

```
DATA TRANSFER (Egress):
  825 PB/month egress

CloudFront pricing tiers:
  First 10 TB: $0.085/GB = $850
  Next 40 TB: $0.080/GB = $3,200
  Next 100 TB: $0.060/GB = $6,000
  Next 350 TB: $0.040/GB = $14,000
  Next 524 TB: $0.030/GB = $15,720
  Remaining ~825,000 TB: $0.020/GB = $16,500,000

Total CDN: ~$16,539,770/month
```

### Monthly Cost Summary

```
╔══════════════════════════════════════════════════════════════╗
║                    MONTHLY COST BREAKDOWN                     ║
╠══════════════════════════════════════════════════════════════╣
║                                                               ║
║  Service                     Cost          Percentage        ║
║  ───────────────────────────────────────────────────────────  ║
║  CDN (CloudFront)      $16,539,770            94.6%          ║
║  Database (RDS)        $   190,770             1.1%          ║
║  Compute (EC2)         $   151,545             0.9%          ║
║  Storage (S3)          $    88,100             0.5%          ║
║  Cache (Redis)         $    85,895             0.5%          ║
║  Database storage      $    38,400             0.2%          ║
║  Search (ES)           $    13,270             0.1%          ║
║  Other (monitoring)    $    50,000             0.3%          ║
║  ───────────────────────────────────────────────────────────  ║
║  TOTAL:               $17,157,750/month       100%           ║
║                                                               ║
║  Per user (500M):      $0.034/user/month                     ║
║  Per DAU (200M):       $0.086/DAU/month                      ║
║                                                               ║
╚══════════════════════════════════════════════════════════════╝

NOTE: 95% of cost is CDN (media delivery)
      Text-only Twitter would cost ~$850K/month
```

### Cost Optimization Strategies

```
REDUCE CDN COSTS (Biggest opportunity):
  1. Implement aggressive caching (30-day TTL on media)
     Savings: 30% reduction = $5M/month
  
  2. Serve lower quality by default (compress images)
     Savings: 20% reduction = $3.3M/month
  
  3. P2P video delivery (WebRTC)
     Savings: 10% reduction on videos = $1.6M/month

REDUCE DATABASE COSTS:
  1. Use Aurora Serverless for read replicas
     Savings: 40% = $50K/month
  
  2. Archive old data to S3
     Savings: Storage reduction = $15K/month

TOTAL POTENTIAL SAVINGS: ~$10M/month (58% reduction)
Optimized cost: ~$7.2M/month
```

---

## Interview Calculation Framework

### Step-by-Step Approach

```
STEP 1: CLARIFY REQUIREMENTS (2 minutes)
────────────────────────────────────────────
Q: "Let me confirm the scale. Are we talking about Twitter's current scale?"
A: 500M users, 200M DAU, 500M tweets/day

Q: "Should I focus on core features like tweet, timeline, follow?"
A: Yes, no need for ads, analytics, or premium features

Q: "Any specific geographic requirements?"
A: Global, multi-region

STEP 2: ESTIMATE TRAFFIC (3 minutes)
────────────────────────────────────────────
Tweets per day: 500M
Seconds per day: 86,400
Tweets/sec: 500M ÷ 86,400 ≈ 6,000/sec avg
Peak (2x): 12,000/sec

Timeline reads: 200M users × 30 loads/day = 6B/day
Reads/sec: 6B ÷ 86,400 ≈ 70,000/sec avg
Peak (3x): 210,000/sec

Total API: ~80K req/sec avg, ~225K req/sec peak

STEP 3: ESTIMATE STORAGE (3 minutes)
────────────────────────────────────────────
Tweet size: ~1 KB (text + metadata)
Daily: 500M × 1 KB = 500 GB/day
Yearly: 500 GB × 365 = 183 TB/year
5 years: 915 TB ≈ 1 PB

Media: 30% have media, avg 5 MB
Daily: 150M × 5 MB = 750 TB/day
Yearly: 274 PB/year

Total: ~275 PB/year

STEP 4: ESTIMATE BANDWIDTH (2 minutes)
────────────────────────────────────────────
Ingress: 6K tweets/sec × 1 KB + media ≈ 70 Gbps
Egress: 70K reads/sec × 50 KB + media ≈ 2.5 Tbps

Daily transfer: ~28 PB/day egress

STEP 5: ESTIMATE CACHE (2 minutes)
────────────────────────────────────────────
Timeline cache: 500M users × 800 tweet IDs × 8 bytes = 3.2 TB
Tweet cache: Hot tweets (24 hrs) = 100 GB
Total: ~4 TB Redis cache

STEP 6: ESTIMATE SERVERS (3 minutes)
────────────────────────────────────────────
App servers: 225K req/sec ÷ 500 req/sec = 450 servers
Database: 256 shards (primary) + 512 replicas = 768 servers
Redis: 4 TB ÷ 64 GB = 64 nodes × 3 (replication) = 192 nodes

Total: ~1,500 servers

STEP 7: ESTIMATE COST (2 minutes)
────────────────────────────────────────────
Compute: 400 servers × $250/month = $100K
Database: 768 instances × $250/month = $192K
Cache: 192 nodes × $400/month = $77K
Storage: 275 PB/year ÷ 12 ÷ 1024 × $20/TB = $45K
CDN: 28 PB/day × 30 × $0.02/GB ≈ $17M

Total: ~$17.4M/month
```

### Quick Reference Formulas

```java
/**
 * INTERVIEW QUICK CALCULATIONS
 */
public class TwitterEstimates {
    
    // Basic conversions
    int SECONDS_PER_DAY = 86_400;
    int HOURS_PER_MONTH = 730;
    
    // Traffic estimates
    long tweetsPerSecond(long tweetsPerDay) {
        return tweetsPerDay / SECONDS_PER_DAY;
        // 500M / 86,400 = 5,787 ≈ 6,000
    }
    
    long peakTraffic(long avgTraffic, double multiplier) {
        return (long)(avgTraffic * multiplier);
        // 6,000 × 2 = 12,000
    }
    
    // Storage estimates
    long dailyStorage(long itemsPerDay, int sizePerItemKB) {
        return itemsPerDay * sizePerItemKB / 1_000_000; // GB
        // 500M × 1 KB / 1M = 500 GB/day
    }
    
    long yearlyStorage(long dailyStorageGB) {
        return dailyStorageGB * 365; // GB
        // 500 GB × 365 = 182,500 GB = 183 TB
    }
    
    // Server estimates
    int serversNeeded(long requestsPerSecond, int capacityPerServer) {
        return (int)Math.ceil((double)requestsPerSecond / capacityPerServer);
        // 80,000 / 500 = 160 servers
    }
    
    int serversWithSafety(int baseServers, double safetyMargin) {
        return (int)(baseServers * (1 + safetyMargin));
        // 160 × 1.5 = 240 servers
    }
    
    // Cost estimates
    long monthlyCost(int servers, double costPerHour) {
        return (long)(servers * costPerHour * HOURS_PER_MONTH);
        // 400 × $0.34 × 730 = $99,280
    }
    
    // Cache estimates
    long cacheSize(long users, int itemsPerUser, int bytesPerItem) {
        return users * itemsPerUser * bytesPerItem / 1_000_000_000; // GB
        // 500M × 800 × 8 / 1B = 3,200 GB = 3.2 TB
    }
}
```

### Common Interview Mistakes to Avoid

```
❌ MISTAKE 1: Forgetting peak traffic
   Wrong: "6,000 tweets/sec, so we need X servers"
   Right: "Peak is 2-3x average, so 12,000 tweets/sec"

❌ MISTAKE 2: Ignoring media in storage
   Wrong: "500M tweets × 1 KB = 500 GB/day"
   Right: "Text is 500 GB, but media is 1.2 PB/day (99% of storage)"

❌ MISTAKE 3: Not accounting for replication
   Wrong: "Need 256 database servers"
   Right: "256 primary + 512 replicas = 768 total"

❌ MISTAKE 4: Underestimating CDN costs
   Wrong: "Infrastructure costs ~$500K/month"
   Right: "CDN alone is $16.5M/month (95% of total cost)"

❌ MISTAKE 5: Forgetting safety margins
   Wrong: "160 servers for avg load"
   Right: "450 servers for peak + 50% margin = 675 servers"

✅ PRO TIP: Always state your assumptions clearly!
   "Assuming 200M DAU, 30 timeline loads/day..."
```

---

## Summary: Key Numbers to Remember

```
╔══════════════════════════════════════════════════════════════╗
║           TWITTER ESTIMATES - MEMORIZE THESE                  ║
╠══════════════════════════════════════════════════════════════╣
║                                                               ║
║  SCALE:                                                       ║
║    500M users, 200M DAU (40%)                                ║
║    500M tweets/day = 6K/sec avg, 12K/sec peak                ║
║    6B timeline reads/day = 70K/sec avg, 210K/sec peak        ║
║    Read-heavy: 12:1 read-to-write ratio                      ║
║                                                               ║
║  STORAGE:                                                     ║
║    Text: 183 TB/year (1 KB per tweet)                        ║
║    Media: 438 PB/year (images/videos)                        ║
║    Total: ~440 PB/year (99% is media)                        ║
║                                                               ║
║  BANDWIDTH:                                                   ║
║    Ingress: 70 Gbps avg                                      ║
║    Egress: 2.5 Tbps avg (95% is CDN)                         ║
║    Daily: 28 PB/day egress                                   ║
║                                                               ║
║  CACHE:                                                       ║
║    Timelines: 4 TB (500M × 800 IDs × 8 bytes)                ║
║    Tweets: 150 GB (hot 24-hour cache)                        ║
║    Total: ~4.2 TB Redis                                      ║
║                                                               ║
║  SERVERS:                                                     ║
║    App servers: 400 (c5.2xlarge)                             ║
║    Databases: 768 PostgreSQL (256 primary + 512 replicas)    ║
║    Cache: 207 Redis nodes                                    ║
║    Total: ~1,500 servers                                     ║
║                                                               ║
║  COST:                                                        ║
║    $17.2M/month total                                        ║
║    $0.034/user/month or $0.086/DAU/month                     ║
║    95% is CDN (media delivery)                               ║
║                                                               ║
╚══════════════════════════════════════════════════════════════╝
```

---

**END OF TWITTER SYSTEM DESIGN ESTIMATES**

*Complete capacity planning guide for Principal Engineer interviews*
