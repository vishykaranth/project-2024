# Part 8: Common System Design Problems - Quick Revision

## URL Shortener (bit.ly)

- **Generate Short Code**: Hash-based (MD5, SHA-256) or counter-based
- **Storage**: Short URL → Long URL mapping, analytics tracking
- **Caching**: Cache popular URLs in Redis
- **Scale**: 100:1 read-to-write ratio, handle 100M+ URLs
- **Redirect**: 301 (permanent) or 302 (temporary) redirects

## Distributed Cache

- **Consistent Hashing**: Distribute keys across cache servers
- **Eviction**: LRU, LFU, TTL-based policies
- **Replication**: Cache redundancy, failover
- **Invalidation**: Write-through, write-behind, cache-aside

## Rate Limiter

- **Token Bucket**: Tokens added at fixed rate, requests consume tokens
- **Sliding Window**: Track requests in time windows, more accurate
- **Redis**: Use for distributed rate limiting, atomic operations
- **Strategies**: Per user, per IP, per API key, global limits

## News Feed (Twitter/Facebook)

- **Feed Generation**: Pull (user fetches), Push (pre-computed), Hybrid
- **Ranking**: Relevance, time decay, user engagement, personalization
- **Scale**: Millions of users, billions of posts, real-time updates
- **Caching**: Cache user feeds, popular posts, social graph

## Chat Application (WhatsApp)

- **Real-time**: WebSocket connections, message queuing
- **Presence**: Online/offline status, typing indicators
- **Group Chats**: Message broadcasting, member management
- **Scale**: Millions of concurrent users, messages per second
- **Ordering**: Ensure messages arrive in correct order
