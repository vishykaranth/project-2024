# Part 3: Caching Strategies - Quick Revision

## Caching Layers

```
Browser Cache → CDN → Reverse Proxy → Application Cache → Database Cache
```

## Cache Patterns

- **Cache-Aside (Lazy Loading)**: App checks cache, loads from DB on miss, updates cache
- **Write-Through**: Write to both cache and database simultaneously; ensures consistency
- **Write-Back (Write-Behind)**: Write to cache first, async write to DB; better performance
- **Refresh-Ahead**: Proactively refresh cache before expiration; reduces cache misses

## Eviction Policies

- **LRU (Least Recently Used)**: Evict least recently accessed; good for temporal locality
- **LFU (Least Frequently Used)**: Evict least frequently accessed; good for frequency patterns
- **FIFO (First In First Out)**: Evict oldest items; simple implementation
- **TTL (Time To Live)**: Evict after expiration time; good for time-sensitive data

## Cache Invalidation

- **Time-based**: Expire after TTL
- **Event-based**: Invalidate on data changes
- **Manual**: Explicit invalidation
- **Cache Stampede**: Multiple requests hit DB when cache expires; use locks or probabilistic expiration

## CDN (Content Delivery Network)

- **Purpose**: Serve static content from edge servers closer to users
- **Push CDN**: Content pushed to edge servers proactively
- **Pull CDN**: Content pulled from origin on first request
- **Use Cases**: Images, videos, static assets, API responses

## Distributed Caching

- **Redis**: In-memory data store, supports complex data structures
- **Memcached**: Simple key-value store, high performance
- **Consistent Hashing**: Distribute cache keys across servers, handle node failures
