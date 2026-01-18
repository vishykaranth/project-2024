# URL Shortener System Design | TinyURL System Design | Bitly System Design

## Overview

URL shorteners convert long URLs into short, shareable links. Systems like TinyURL and Bitly must handle billions of URLs, provide fast redirects, and scale globally.

## System Requirements

- Shorten long URLs
- Redirect short URLs to original
- Handle billions of URLs
- Custom aliases (optional)
- Analytics
- Expiration (optional)

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│         URL Shortener Architecture                      │
└─────────────────────────────────────────────────────────┘

Client                    API Service          Database
    │                            │                        │
    │───Shorten───────────────>│                        │
    │    long_url               │                        │
    │                            │                        │
    │                            ├───Generate───────────>│
    │                            │    Short Code          │
    │                            │                        │
    │                            ├───Store──────────────>│
    │                            │                        │
    │<──Short URL────────────────│                        │
    │                            │                        │
    │───Redirect─────────────────>│                        │
    │    short_url               │                        │
    │                            │                        │
    │                            ├───Lookup─────────────>│
    │                            │                        │
    │<──Redirect─────────────────│                        │
    │    long_url                │                        │
    │                            │                        │
```

## URL Encoding

### Base62 Encoding

```
┌─────────────────────────────────────────────────────────┐
│         Base62 Encoding                                 │
└─────────────────────────────────────────────────────────┘

Characters: 0-9, a-z, A-Z (62 characters)

Example:
├─ URL ID: 1234567890
├─ Base62: aBc123
└─ Short URL: tinyurl.com/aBc123

Encoding Process:
├─ Convert decimal to base 62
├─ Map to characters
└─ Generate short code
```

### Hash-Based Approach

```
┌─────────────────────────────────────────────────────────┐
│         Hash-Based Generation                           │
└─────────────────────────────────────────────────────────┘

1. Hash long URL (MD5/SHA256)
    │
    ▼
2. Take first 6-8 characters
    │
    ▼
3. Check for collision
    │
    ├─► If collision: Append counter
    └─► If no collision: Use it
```

## Database Design

```sql
URL_Mappings:
- id (PK, auto-increment)
- short_code (unique index)
- long_url
- created_at
- expires_at (nullable)
- click_count
- user_id (FK, nullable)

Indexes:
├─ short_code (unique)
├─ long_url (for duplicate detection)
└─ created_at
```

## Scaling Strategies

### 1. Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Sharding Strategy                               │
└─────────────────────────────────────────────────────────┘

Shard by short_code hash:
├─ Shard 1: Codes starting with 0-9
├─ Shard 2: Codes starting with a-z
└─ Shard 3: Codes starting with A-Z
```

### 2. Caching

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                                
└─────────────────────────────────────────────────────────┘

Cache hot URLs:
├─ Popular URLs in Redis
├─ Fast redirects
└─ Reduce database load
```

## Summary

URL Shortener System:
- **Encoding**: Base62 or hash-based
- **Storage**: Database with indexes
- **Redirect**: Fast lookup and redirect
- **Scale**: Sharding and caching

**Key Features:**
- Short code generation
- Fast redirects
- Analytics
- Custom aliases
- Expiration
