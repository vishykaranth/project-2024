# Beginner System Design Interview: Design Bitly

## Overview

Designing a URL shortener like Bitly is a classic system design interview question. This guide walks through the complete design process, covering requirements, capacity estimation, system design, and scaling considerations.

## Requirements

### Functional Requirements

```
┌─────────────────────────────────────────────────────────┐
│         Functional Requirements                         │
└─────────────────────────────────────────────────────────┘

1. Shorten URL
   ├─ Input: Long URL
   └─ Output: Short URL (e.g., bit.ly/abc123)

2. Redirect
   ├─ Input: Short URL
   └─ Output: Redirect to original URL

3. Optional Features:
   ├─ Custom short URLs
   ├─ Expiration dates
   ├─ Analytics (click count)
   └─ User accounts
```

### Non-Functional Requirements

- **Availability**: 99.9% uptime
- **Scalability**: Handle 100M URLs/day
- **Performance**: Low latency (< 100ms)
- **Durability**: URLs should not be lost

## Capacity Estimation

### Traffic Estimates

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimation                            │
└─────────────────────────────────────────────────────────┘

Assumptions:
├─ 100M URLs shortened per day
├─ Read:Write ratio = 100:1
└─ 10B redirects per day

Calculations:
├─ Writes: 100M/day = ~1,160 writes/sec
├─ Reads: 10B/day = ~115,740 reads/sec
└─ Peak traffic: 2x average = ~231,480 reads/sec
```

### Storage Estimates

```
┌─────────────────────────────────────────────────────────┐
│         Storage Requirements                           │
└─────────────────────────────────────────────────────────┘

Per URL:
├─ Short URL: 7 chars = 7 bytes
├─ Long URL: 500 chars avg = 500 bytes
├─ Created date: 8 bytes
├─ Expiration: 8 bytes
└─ Total: ~523 bytes per URL

Total Storage:
├─ 100M URLs × 523 bytes = 52.3 GB
├─ With 5 years data: 52.3 GB × 5 = 261.5 GB
└─ With replication (3x): ~785 GB
```

## System Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         High-Level Architecture                        │
└─────────────────────────────────────────────────────────┘

[Client]
    │
    │ HTTP Request
    ▼
[Load Balancer]
    │
    ├─► [API Server 1]
    ├─► [API Server 2]
    └─► [API Server N]
    │
    ├─► [Database] (URL mappings)
    └─► [Cache] (Hot URLs)
```

### Core Components

#### 1. URL Shortening Service

```
┌─────────────────────────────────────────────────────────┐
│         URL Shortening Flow                            │
└─────────────────────────────────────────────────────────┘

1. Client sends long URL
   POST /api/v1/shorten
   { "url": "https://example.com/very/long/url" }

2. Generate unique short code
   ├─ Base62 encoding (a-z, A-Z, 0-9)
   └─ 7 characters = 62^7 = 3.5 trillion possibilities

3. Store mapping
   Database: short_code → long_url

4. Return short URL
   Response: { "shortUrl": "bit.ly/abc123" }
```

#### 2. URL Redirection Service

```
┌─────────────────────────────────────────────────────────┐
│         URL Redirection Flow                           │
└─────────────────────────────────────────────────────────┘

1. Client requests short URL
   GET /abc123

2. Lookup in cache
   ├─ Cache hit? → Return long URL (301 redirect)
   └─ Cache miss? → Check database

3. Database lookup
   ├─ Found? → Cache result → Return long URL
   └─ Not found? → Return 404

4. Redirect
   HTTP 301 Moved Permanently
   Location: https://example.com/very/long/url
```

### Database Design

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                │
└─────────────────────────────────────────────────────────┘

urls table:
├─ id (BIGINT, Primary Key)
├─ short_code (VARCHAR(7), Unique Index)
├─ long_url (TEXT)
├─ created_at (TIMESTAMP)
├─ expires_at (TIMESTAMP, nullable)
└─ user_id (BIGINT, nullable)

Indexes:
├─ PRIMARY KEY (id)
├─ UNIQUE INDEX (short_code)
└─ INDEX (user_id)
```

### Short Code Generation

#### Option 1: Base62 Encoding

```
┌─────────────────────────────────────────────────────────┐
│         Base62 Encoding                                │
└─────────────────────────────────────────────────────────┘

Algorithm:
1. Generate unique ID (auto-increment or UUID)
2. Convert to Base62
   Characters: a-z, A-Z, 0-9 (62 characters)
3. Take first 7 characters

Example:
ID: 123456789
Base62: abc123
Short URL: bit.ly/abc123
```

#### Option 2: Hash-based

```
┌─────────────────────────────────────────────────────────┐
│         Hash-based Generation                          │
└─────────────────────────────────────────────────────────┘

Algorithm:
1. Hash long URL (MD5 or SHA256)
2. Take first 7 characters
3. Check for collisions
4. If collision, append sequence number

Example:
URL: https://example.com/very/long/url
Hash: a1b2c3d4e5f6...
Short Code: a1b2c3d
```

## Detailed Design

### API Design

```
┌─────────────────────────────────────────────────────────┐
│         API Endpoints                                  │
└─────────────────────────────────────────────────────────┘

POST /api/v1/shorten
Request:
{
  "url": "https://example.com/very/long/url",
  "customCode": "my-link" (optional),
  "expiresAt": "2024-12-31" (optional)
}
Response:
{
  "shortUrl": "bit.ly/abc123",
  "longUrl": "https://example.com/very/long/url",
  "expiresAt": "2024-12-31"
}

GET /{shortCode}
Response:
HTTP 301 Moved Permanently
Location: https://example.com/very/long/url

GET /api/v1/{shortCode}/stats
Response:
{
  "shortCode": "abc123",
  "clicks": 1234,
  "createdAt": "2024-01-01",
  "lastAccessed": "2024-01-15"
}
```

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                               │
└─────────────────────────────────────────────────────────┘

Cache Popular URLs:
├─ Cache 20% of URLs (80/20 rule)
├─ Use Redis or Memcached
├─ TTL: 24 hours
└─ LRU eviction policy

Cache Structure:
Key: short_code
Value: long_url
TTL: 24 hours
```

### Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Sharding Strategy:
├─ Shard by short_code hash
├─ 10 shards (can scale)
└─ Consistent hashing for distribution

Shard Distribution:
├─ Shard 0: codes starting with 0-9
├─ Shard 1: codes starting with a-g
├─ Shard 2: codes starting with h-n
└─ ... (10 shards total)
```

## Scaling Considerations

### 1. Read Scaling

```
┌─────────────────────────────────────────────────────────┐
│         Read Scaling                                   │
└─────────────────────────────────────────────────────────┘

Strategies:
├─ Read replicas (database)
├─ CDN for popular URLs
├─ Caching layer (Redis)
└─ Load balancing
```

### 2. Write Scaling

```
┌─────────────────────────────────────────────────────────┐
│         Write Scaling                                  │
└─────────────────────────────────────────────────────────┘

Strategies:
├─ Database sharding
├─ Pre-generate short codes
├─ Use NoSQL for high write throughput
└─ Async processing for analytics
```

### 3. High Availability

```
┌─────────────────────────────────────────────────────────┐
│         High Availability                              │
└─────────────────────────────────────────────────────────┘

Components:
├─ Multiple API servers
├─ Database replication
├─ Cache replication
└─ Load balancer health checks
```

## Advanced Features

### 1. Analytics

```
┌─────────────────────────────────────────────────────────┐
│         Analytics System                               │
└─────────────────────────────────────────────────────────┘

Track:
├─ Click count
├─ Geographic location
├─ Referrer
├─ Device type
└─ Timestamp

Implementation:
├─ Log clicks to message queue
├─ Async processing
├─ Store in analytics DB
└─ Aggregate for reporting
```

### 2. Custom Short URLs

```
┌─────────────────────────────────────────────────────────┐
│         Custom Short URLs                              │
└─────────────────────────────────────────────────────────┘

Validation:
├─ Check availability
├─ Validate format (alphanumeric, 4-20 chars)
├─ Reserved words check
└─ User authentication required
```

### 3. URL Expiration

```
┌─────────────────────────────────────────────────────────┐
│         URL Expiration                                 │
└─────────────────────────────────────────────────────────┘

Implementation:
├─ Store expiration date
├─ Background job to delete expired URLs
├─ Check expiration on redirect
└─ Return 410 Gone for expired URLs
```

## Technology Stack

```
┌─────────────────────────────────────────────────────────┐
│         Technology Choices                             │
└─────────────────────────────────────────────────────────┘

Application:
├─ Language: Java, Python, or Go
├─ Framework: Spring Boot, Django, or Gin
└─ API: RESTful

Database:
├─ Primary: PostgreSQL or MySQL
├─ Cache: Redis
└─ Analytics: ClickHouse or BigQuery

Infrastructure:
├─ Load Balancer: NGINX or AWS ELB
├─ CDN: CloudFlare or AWS CloudFront
└─ Message Queue: RabbitMQ or Kafka
```

## Interview Discussion Points

### Q1: How do you ensure uniqueness of short codes?
**Answer:**
- Use database unique constraint
- Check before insertion
- Retry with different code on collision
- Pre-generate codes in batches

### Q2: How do you handle 301 vs 302 redirects?
**Answer:**
- **301 (Permanent)**: Better for SEO, cached by browsers
- **302 (Temporary)**: Better for analytics, not cached
- Default to 301, allow users to choose

### Q3: How do you prevent abuse?
**Answer:**
- Rate limiting per IP/user
- CAPTCHA for suspicious activity
- URL validation (check if accessible)
- Block malicious URLs
- User authentication for high volume

### Q4: How do you scale to billions of URLs?
**Answer:**
- Database sharding
- Read replicas
- Aggressive caching
- CDN for popular URLs
- Pre-generate short codes
- Use NoSQL for high write throughput

## Summary

Designing Bitly requires:
- **URL Shortening**: Generate unique short codes
- **Redirection**: Fast lookup and redirect
- **Storage**: Efficient database design
- **Caching**: Cache popular URLs
- **Scaling**: Sharding and replication
- **Analytics**: Track clicks and usage

**Key Components:**
- API servers for shortening and redirection
- Database for URL mappings
- Cache for hot URLs
- Load balancer for distribution
- Analytics system for tracking

**Scaling Strategies:**
- Database sharding
- Read replicas
- Caching layer
- CDN for popular URLs
- Pre-generate short codes
