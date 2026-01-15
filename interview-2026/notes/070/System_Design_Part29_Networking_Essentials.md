# Networking Essentials for System Design Interviews w/ Ex Meta Senior Manager

## Overview

Understanding networking fundamentals is crucial for system design. This guide covers TCP/IP, HTTP, DNS, load balancing, CDN, and network protocols essential for system design interviews.

## Network Layers

```
┌─────────────────────────────────────────────────────────┐
│              OSI Model                                 │
└─────────────────────────────────────────────────────────┘

7. Application Layer (HTTP, HTTPS, DNS)
6. Presentation Layer (SSL/TLS)
5. Session Layer
4. Transport Layer (TCP, UDP)
3. Network Layer (IP)
2. Data Link Layer
1. Physical Layer
```

## 1. TCP vs UDP

```
┌─────────────────────────────────────────────────────────┐
│         TCP vs UDP Comparison                          │
└─────────────────────────────────────────────────────────┘

TCP (Transmission Control Protocol):
├─ Connection-oriented
├─ Reliable delivery
├─ Ordered delivery
├─ Flow control
└─ Use: Web, email, file transfer

UDP (User Datagram Protocol):
├─ Connectionless
├─ Unreliable delivery
├─ No ordering
├─ Lower latency
└─ Use: Video streaming, gaming, DNS
```

## 2. HTTP/HTTPS

```
┌─────────────────────────────────────────────────────────┐
│         HTTP Methods                                   │
└─────────────────────────────────────────────────────────┘

GET: Retrieve resource
POST: Create resource
PUT: Update resource
DELETE: Delete resource
PATCH: Partial update

HTTP Status Codes:
├─ 2xx: Success (200, 201, 204)
├─ 3xx: Redirection (301, 302, 304)
├─ 4xx: Client Error (400, 401, 404)
└─ 5xx: Server Error (500, 502, 503)
```

## 3. DNS

```
┌─────────────────────────────────────────────────────────┐
│         DNS Resolution Flow                            │
└─────────────────────────────────────────────────────────┘

1. Query local DNS cache
   │
   ▼
2. Query recursive DNS server
   │
   ▼
3. Query root DNS server
   │
   ▼
4. Query TLD DNS server (.com)
   │
   ▼
5. Query authoritative DNS server
   │
   ▼
6. Return IP address
```

## 4. Load Balancing

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Algorithms                      │
└─────────────────────────────────────────────────────────┘

Round Robin:
├─ Distribute requests sequentially
└─ Simple, equal distribution

Least Connections:
├─ Route to server with fewest connections
└─ Better for long-lived connections

IP Hash:
├─ Hash client IP to server
└─ Session persistence

Weighted:
├─ Assign weights to servers
└─ Route based on capacity
```

## 5. CDN

```
┌─────────────────────────────────────────────────────────┐
│         CDN Architecture                               │
└─────────────────────────────────────────────────────────┘

Origin Server
    │
    ├─► Edge Server 1 (US East)
    ├─► Edge Server 2 (US West)
    ├─► Edge Server 3 (Europe)
    └─► Edge Server 4 (Asia)

Benefits:
- Reduced latency
- Lower bandwidth costs
- Better availability
```

## Summary

Networking Essentials:
- **TCP/UDP**: Transport protocols
- **HTTP/HTTPS**: Application protocols
- **DNS**: Domain resolution
- **Load Balancing**: Request distribution
- **CDN**: Content delivery
