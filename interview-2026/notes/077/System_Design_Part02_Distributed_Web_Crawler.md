# System Design: Distributed Web Crawler to Crawl Billions of Web Pages

## Overview

A distributed web crawler system designed to efficiently crawl and index billions of web pages across the internet. This system must handle massive scale, be fault-tolerant, and avoid overloading target websites.

## System Requirements

### Functional Requirements
- Crawl web pages from URLs
- Extract and store content
- Handle different content types (HTML, PDF, images)
- Respect robots.txt
- Avoid duplicate crawling
- Handle dynamic content (JavaScript)

### Non-Functional Requirements
- Scale to billions of pages
- High throughput (millions of pages/day)
- Fault tolerance
- Politeness (don't overload servers)
- Low latency for URL processing

## System Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Web Crawler Architecture            │
└─────────────────────────────────────────────────────────┘

URL Frontier          Crawler Workers          Content Store
    │                        │                        │
    │───URLs─────────────────>│                        │
    │                        │                        │
    │                        │───Fetch Page───────────>│
    │                        │    (Internet)            │
    │                        │                        │
    │                        │<──HTML Content──────────│
    │                        │                        │
    │                        │───Store───────────────>│
    │                        │                        │
    │<──New URLs─────────────│                        │
    │    (Extracted)          │                        │
    │                        │                        │

URL Deduplicator      URL Filter              Robots.txt Cache
    │                        │                        │
    │───Check────────────────>│                        │
    │    Duplicate            │                        │
    │                        │                        │
    │<──Not Seen──────────────│                        │
    │                        │                        │
    │                        │───Check───────────────>│
    │                        │    robots.txt          │
    │                        │                        │
```

## Core Components

### 1. URL Frontier (URL Queue)

```
┌─────────────────────────────────────────────────────────┐
│         URL Frontier Structure                          │
└─────────────────────────────────────────────────────────┘

Priority Queue (High Priority URLs)
    │
    ├─► News sites
    ├─► Popular domains
    └─► Recently updated

Standard Queue (Regular URLs)
    │
    ├─► General web pages
    └─► Lower priority

Back Queue (Retry Failed URLs)
    │
    └─► Failed crawls
        └─► Exponential backoff
```

**Implementation:**
- Priority-based queues
- Per-domain queues (politeness)
- Rate limiting per domain
- Distributed queues (Kafka/RabbitMQ)

### 2. Crawler Workers

```
┌─────────────────────────────────────────────────────────┐
│         Crawler Worker Architecture                     │
└─────────────────────────────────────────────────────────┘

Worker Pool
    │
    ├─► Worker 1
    │   ├─► Fetch URL
    │   ├─► Parse HTML
    │   ├─► Extract links
    │   └─► Store content
    │
    ├─► Worker 2
    │   └─► (Same process)
    │
    └─► Worker N
        └─► (Scalable)
```

**Worker Responsibilities:**
1. Fetch URL from frontier
2. Download page content
3. Parse HTML
4. Extract links
5. Check robots.txt
6. Store content
7. Submit new URLs to frontier

### 3. URL Deduplicator

```
┌─────────────────────────────────────────────────────────┐
│         URL Deduplication System                        │
└─────────────────────────────────────────────────────────┘

URL                    Bloom Filter              Database
    │                        │                        │
    │───Check───────────────>│                        │
    │                        │                        │
    │                        │───Not in Bloom────────>│
    │                        │    (Check DB)          │
    │                        │                        │
    │                        │<──Not Found────────────│
    │<──New URL───────────────│                        │
    │                        │                        │
    │                        │───Add to Bloom────────>│
    │                        │───Add to DB───────────>│
```

**Techniques:**
- **Bloom Filter**: Fast probabilistic check
- **Distributed Hash Table**: Exact duplicate detection
- **Normalization**: URL canonicalization

### 4. Content Store

```
┌─────────────────────────────────────────────────────────┐
│         Content Storage Architecture                    │
└─────────────────────────────────────────────────────────┘

Crawler                Content Store            Index
    │                        │                        │
    │───Store───────────────>│                        │
    │    HTML Content        │                        │
    │                        │                        │
    │                        │───Index───────────────>│
    │                        │    Content             │
    │                        │                        │
    │                        │───Store Metadata─────>│
    │                        │                        │
```

**Storage Strategy:**
- **Distributed File System**: HDFS, S3
- **Database**: Metadata storage
- **CDN**: Cached content delivery

## Politeness and Rate Limiting

```
┌─────────────────────────────────────────────────────────┐
│         Politeness Mechanism                            │
└─────────────────────────────────────────────────────────┘

Per-Domain Rate Limiter
    │
    ├─► example.com: 1 request/second
    ├─► news.com: 2 requests/second
    └─► blog.com: 0.5 requests/second

Robots.txt Compliance
    │
    ├─► Check robots.txt
    ├─► Respect crawl-delay
    └─► Honor disallow rules
```

**Implementation:**
- Per-domain queues
- Rate limiting per domain
- Respect robots.txt
- Exponential backoff on errors

## Distributed Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Crawler System                     │
└─────────────────────────────────────────────────────────┘

Master Node
    │
    ├─► URL Frontier Manager
    ├─► Worker Coordinator
    └─► Monitoring

Worker Nodes (N instances)
    │
    ├─► Worker 1
    │   ├─► Crawl URLs
    │   └─► Extract content
    │
    ├─► Worker 2
    │   └─► (Same)
    │
    └─► Worker N
        └─► (Scalable)
```

## Data Flow

```
┌─────────────────────────────────────────────────────────┐
│         Crawling Data Flow                              │
└─────────────────────────────────────────────────────────┘

1. Seed URLs → URL Frontier
    │
    ▼
2. URL Frontier → Crawler Worker
    │
    ▼
3. Crawler Worker → Fetch Page
    │
    ▼
4. Parse HTML → Extract Links
    │
    ▼
5. Check Deduplicator → New URLs?
    │
    ▼
6. Store Content → Content Store
    │
    ▼
7. New URLs → URL Frontier (Repeat)
```

## Scaling Strategies

### Horizontal Scaling
- Add more worker nodes
- Distribute URL queues
- Shard content storage

### Vertical Scaling
- Increase worker capacity
- Larger queues
- More storage

## Fault Tolerance

```
┌─────────────────────────────────────────────────────────┐
│         Fault Tolerance Mechanisms                     │
└─────────────────────────────────────────────────────────┘

1. URL Retry Queue
   ├─► Failed URLs
   └─► Exponential backoff

2. Worker Health Checks
   ├─► Monitor workers
   └─► Replace failed workers

3. Checkpointing
   ├─► Save progress
   └─► Resume from checkpoint
```

## Performance Optimization

### 1. Parallel Crawling
- Multiple workers per domain
- Concurrent requests
- Connection pooling

### 2. Caching
- Robots.txt cache
- DNS cache
- Content cache

### 3. Compression
- Compress stored content
- Reduce storage costs
- Faster transfers

## Storage Schema

```
┌─────────────────────────────────────────────────────────┐
│         Data Storage Schema                             │
└─────────────────────────────────────────────────────────┘

URLs Table:
├─ url_id (PK)
├─ url (unique)
├─ domain
├─ priority
├─ status (pending/crawled/failed)
└─ last_crawled

Content Table:
├─ content_id (PK)
├─ url_id (FK)
├─ html_content
├─ metadata
├─ crawled_at
└─ content_hash

Links Table:
├─ link_id (PK)
├─ from_url_id (FK)
├─ to_url_id (FK)
└─ link_text
```

## Summary

Distributed Web Crawler System:
- **URL Frontier**: Priority-based URL queue
- **Crawler Workers**: Distributed crawling workers
- **Deduplication**: Bloom filter + database
- **Content Store**: Distributed storage
- **Politeness**: Rate limiting and robots.txt

**Key Features:**
- Scalable to billions of pages
- Fault-tolerant architecture
- Politeness mechanisms
- Efficient deduplication
- Distributed processing

**Technologies:**
- Message Queues (Kafka)
- Distributed Storage (HDFS, S3)
- Bloom Filters
- Load Balancers
- Monitoring Systems
