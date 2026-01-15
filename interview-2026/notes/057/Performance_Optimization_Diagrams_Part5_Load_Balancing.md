# Performance Optimization - Complete Diagrams Guide (Part 5: Load Balancing)

## ⚖️ Load Balancing: Round-Robin, Least Connections, Sticky Sessions

---

## 1. Load Balancer Architecture

### Basic Load Balancing
```
┌─────────────────────────────────────────────────────────────┐
│              Load Balancer Architecture                     │
└─────────────────────────────────────────────────────────────┘

Clients
    │
    │ Requests
    ▼
┌─────────────────────────────────────────────────────────┐
│  Load Balancer (LB)                                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Algorithm: Distribute requests                  │  │
│  │  Health Checks: Monitor backend health            │  │
│  │  SSL Termination: Handle TLS                       │  │
│  └──────────────────────────────────────────────────┘  │
└──────────────┬──────────────────────────────────────────┘
               │
               │ Distribute
               ▼
┌─────────────────────────────────────────────────────────┐
│  Backend Servers                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │ Server 1 │  │ Server 2 │  │ Server 3 │             │
│  │ (Healthy)│  │ (Healthy)│  │ (Healthy)│             │
│  └──────────┘  └──────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────┘

Benefits:
• High availability (if one server fails, others serve)
• Scalability (add/remove servers)
• Performance (distribute load)
• SSL termination (offload from servers)
```

### Load Balancer Types
```
┌─────────────────────────────────────────────────────────────┐
│              Load Balancer Types                            │
└─────────────────────────────────────────────────────────────┘

1. Layer 4 (L4) - Transport Layer
─────────────────────────────────────────────────────────────
    Works with: IP addresses and ports
    Decisions based on: TCP/UDP headers
    
    ┌──────────┐
    │  Client  │
    └────┬─────┘
         │ TCP: 10.0.1.1:443
         ▼
    ┌──────────┐
    │   L4 LB  │  ← Routes based on IP:Port
    └────┬─────┘
         │
         ├───► Server 1 (10.0.2.1:8080)
         ├───► Server 2 (10.0.2.2:8080)
         └───► Server 3 (10.0.2.3:8080)
    
    Pros: Fast, simple, low overhead
    Cons: No application awareness

2. Layer 7 (L7) - Application Layer
─────────────────────────────────────────────────────────────
    Works with: HTTP/HTTPS content
    Decisions based on: URL, headers, cookies
    
    ┌──────────┐
    │  Client  │
    └────┬─────┘
         │ HTTP: GET /api/users
         ▼
    ┌──────────┐
    │   L7 LB  │  ← Routes based on URL/headers
    └────┬─────┘
         │
         ├───► /api/* → API Servers
         ├───► /static/* → Static Servers
         └───► /admin/* → Admin Servers
    
    Pros: Content-aware routing, SSL termination
    Cons: Higher overhead, more complex
```

---

## 2. Load Balancing Algorithms

### Round-Robin
```
┌─────────────────────────────────────────────────────────────┐
│              Round-Robin Algorithm                          │
└─────────────────────────────────────────────────────────────┘

Request Distribution:
─────────────────────────────────────────────────────────────
Request 1 ──► Server 1
Request 2 ──► Server 2
Request 3 ──► Server 3
Request 4 ──► Server 1  (cycle repeats)
Request 5 ──► Server 2
Request 6 ──► Server 3
...

Visualization:
─────────────────────────────────────────────────────────────
    ┌──────────┐
    │   LB     │
    └────┬─────┘
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
┌────┐┌────┐┌────┐
│ S1 ││ S2 ││ S3 │
└────┘└────┘└────┘
  ↑     ↑     ↑
  │     │     │
  └─────┴─────┘
    Round-Robin
    (equal distribution)

Characteristics:
• Simple and fair distribution
• Each server gets equal share
• No consideration of server load
• Works well when servers are similar

Use Case:
• Servers have similar capacity
• Requests are similar in processing time
• Stateless applications
```

### Weighted Round-Robin
```
┌─────────────────────────────────────────────────────────────┐
│              Weighted Round-Robin                           │
└─────────────────────────────────────────────────────────────┘

Server Weights:
─────────────────────────────────────────────────────────────
Server 1: Weight = 3 (most powerful)
Server 2: Weight = 2
Server 3: Weight = 1 (least powerful)

Request Distribution:
─────────────────────────────────────────────────────────────
Request 1 ──► Server 1
Request 2 ──► Server 1
Request 3 ──► Server 1
Request 4 ──► Server 2
Request 5 ──► Server 2
Request 6 ──► Server 3
Request 7 ──► Server 1  (cycle repeats)
...

Visualization:
─────────────────────────────────────────────────────────────
    ┌──────────┐
    │   LB     │
    └────┬─────┘
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
┌────┐┌────┐┌────┐
│ S1 ││ S2 ││ S3 │
│(3x)││(2x)││(1x)│
└────┘└────┘└────┘

Characteristics:
• Distributes based on server capacity
• More powerful servers get more requests
• Better utilization of resources
• Requires capacity assessment

Use Case:
• Servers have different capacities
• Mixed hardware (new + old servers)
• Gradual server upgrades
```

### Least Connections
```
┌─────────────────────────────────────────────────────────────┐
│              Least Connections Algorithm                     │
└─────────────────────────────────────────────────────────────┘

Connection Tracking:
─────────────────────────────────────────────────────────────
    ┌──────────┐
    │   LB     │
    │          │
    │ Active Connections:
    │ S1: 5 connections
    │ S2: 2 connections  ← Least
    │ S3: 8 connections
    └────┬─────┘
         │
         │ New request
         │ → Route to S2 (least connections)
         ▼
    ┌──────────┐
    │ Server 2 │  ← Now has 3 connections
    └──────────┘

Dynamic Distribution:
─────────────────────────────────────────────────────────────
Time T1:
    S1: 10 connections
    S2: 5 connections  ← Route here
    S3: 15 connections

Time T2 (after some requests complete):
    S1: 8 connections
    S2: 3 connections  ← Route here
    S3: 12 connections

Characteristics:
• Routes to server with fewest active connections
• Adapts to changing load
• Good for long-lived connections
• Better load distribution

Use Case:
• Long-running requests (file uploads, streaming)
• Variable request processing times
• WebSocket connections
• Real-time applications
```

### Least Response Time
```
┌─────────────────────────────────────────────────────────────┐
│              Least Response Time                            │
└─────────────────────────────────────────────────────────────┘

Response Time Monitoring:
─────────────────────────────────────────────────────────────
    ┌──────────┐
    │   LB     │
    │          │
    │ Avg Response Time:
    │ S1: 50ms   ← Fastest
    │ S2: 120ms
    │ S3: 200ms
    └────┬─────┘
         │
         │ New request
         │ → Route to S1 (fastest)
         ▼
    ┌──────────┐
    │ Server 1 │
    └──────────┘

How It Works:
─────────────────────────────────────────────────────────────
1. LB sends health check requests to all servers
2. Measures response time
3. Routes new requests to fastest server
4. Continuously monitors and updates

Characteristics:
• Routes to fastest responding server
• Adapts to server performance
• Considers network latency
• More overhead (health checks)

Use Case:
• Servers with varying performance
• Geographic distribution
• Performance-critical applications
• Real-time response requirements
```

### IP Hash (Consistent Hashing)
```
┌─────────────────────────────────────────────────────────────┐
│              IP Hash Algorithm                              │
└─────────────────────────────────────────────────────────────┘

Hash Function:
─────────────────────────────────────────────────────────────
    Client IP: 192.168.1.100
        │
        │ Hash function
        ▼
    Hash = hash(192.168.1.100) = 42
        │
        │ Modulo number of servers
        ▼
    Server = 42 % 3 = 0 → Server 1

Visualization:
─────────────────────────────────────────────────────────────
    Client A (IP: 1.1.1.1)
        │
        │ hash(1.1.1.1) % 3 = 1
        ▼
    ┌──────────┐
    │ Server 2 │  ← Always routes here
    └──────────┘

    Client B (IP: 2.2.2.2)
        │
        │ hash(2.2.2.2) % 3 = 0
        ▼
    ┌──────────┐
    │ Server 1 │  ← Always routes here
    └──────────┘

Characteristics:
• Same client always routes to same server
• Enables session persistence
• Predictable routing
• Problem: Rehashing when servers added/removed

Consistent Hashing (Better):
─────────────────────────────────────────────────────────────
    Hash Ring:
    
    0 ────────────► 100 ────────────► 200 ────────────► 300
    │                │                │                │
    │            Server 1         Server 2         Server 3
    │                │                │                │
    Client A ────────┘                │                │
    (hash=150)                        │                │
                                      │                │
    Client B ─────────────────────────┘                │
    (hash=250)                                          │
                                                        │
    Client C ──────────────────────────────────────────┘
    (hash=350 → wraps to 50)

Benefits:
• Minimal rehashing when servers added/removed
• Only affects ~1/N of connections
• Better for distributed systems
```

---

## 3. Sticky Sessions (Session Affinity)

### Session Affinity
```
┌─────────────────────────────────────────────────────────────┐
│              Sticky Sessions                                │
└─────────────────────────────────────────────────────────────┘

Problem Without Sticky Sessions:
─────────────────────────────────────────────────────────────
Request 1: Login ──► Server 1 (creates session)
Request 2: Get Data ──► Server 2 (no session!) ❌
Request 3: Logout ──► Server 3 (no session!) ❌

Solution: Sticky Sessions
─────────────────────────────────────────────────────────────
Request 1: Login ──► Server 1 (creates session)
    │
    │ LB remembers: Client → Server 1
    │ (via cookie or IP hash)
    │
Request 2: Get Data ──► Server 1 (session exists) ✅
Request 3: Logout ──► Server 1 (session exists) ✅

Implementation Methods:
─────────────────────────────────────────────────────────────
1. Cookie-Based
   ┌──────────┐
   │  Client  │
   └────┬─────┘
        │ Request
        ▼
   ┌──────────┐
   │   LB     │
   │          │ Sets cookie: SERVERID=server1
   └────┬─────┘
        │
        ▼
   ┌──────────┐
   │ Server 1 │
   └──────────┘
        │
        │ Response with cookie
        ▼
   ┌──────────┐
   │  Client  │  ← Stores cookie
   └──────────┘
        │
        │ Next request includes cookie
        ▼
   ┌──────────┐
   │   LB     │  ← Reads cookie, routes to Server 1
   └────┬─────┘

2. IP Hash
   • Hash client IP
   • Always route to same server
   • No cookie needed
   • Problem: Multiple users behind NAT

3. Application Cookie
   • Application sets session cookie
   • LB reads session ID
   • Routes based on session → server mapping
```

### Session Replication vs Sticky Sessions
```
┌─────────────────────────────────────────────────────────────┐
│              Session Management Strategies                   │
└─────────────────────────────────────────────────────────────┘

Option 1: Sticky Sessions
─────────────────────────────────────────────────────────────
    Client ──► LB ──► Server 1 (session stored locally)
    Client ──► LB ──► Server 1 (same server)
    
    Pros:
    • Simple
    • No replication overhead
    • Fast access
    
    Cons:
    • Server failure = lost sessions
    • Uneven load if sessions vary in duration
    • Can't scale down gracefully

Option 2: Session Replication
─────────────────────────────────────────────────────────────
    Client ──► LB ──► Server 1 (creates session)
        │
        │ Replicate to all servers
        ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │ Server 1 │  │ Server 2 │  │ Server 3 │
    │ (session)│  │ (session)│  │ (session)│
    └──────────┘  └──────────┘  └──────────┘
    
    Client ──► LB ──► Any server (session available)
    
    Pros:
    • Any server can handle request
    • Better load distribution
    • Server failure doesn't lose sessions
    
    Cons:
    • Replication overhead
    • Memory usage (N copies)
    • Network bandwidth

Option 3: External Session Store (Recommended)
─────────────────────────────────────────────────────────────
    Client ──► LB ──► Server 1
        │
        │ Store session
        ▼
    ┌──────────┐
    │  Redis   │  ← Centralized session store
    │ /Memcached│
    └────┬─────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌──────┐  ┌──────┐
│Server│  │Server│  ← Any server can read session
│  1   │  │  2   │
└──────┘  └──────┘
    
    Pros:
    • No sticky sessions needed
    • Scalable
    • Centralized management
    • Can survive server failures
    
    Cons:
    • Network latency to session store
    • Session store becomes critical component
```

---

## 4. Health Checks

### Health Check Mechanism
```
┌─────────────────────────────────────────────────────────────┐
│              Load Balancer Health Checks                     │
└─────────────────────────────────────────────────────────────┘

Health Check Flow:
─────────────────────────────────────────────────────────────
    ┌──────────┐
    │   LB     │
    └────┬─────┘
         │
         │ Periodic health checks
         │ (every 5-30 seconds)
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
┌────┐┌────┐┌────┐
│ S1 ││ S2 ││ S3 │
│ ✅ ││ ✅ ││ ❌ │  ← Unhealthy
└────┘└────┘└────┘
    │    │    │
    │    │    │ Response
    └────┴────┴────┘
         │
         ▼
    ┌──────────┐
    │   LB     │  ← Updates server status
    │          │
    │ Active: S1, S2
    │ Down: S3 (removed from pool)
    └──────────┘

Health Check Types:
─────────────────────────────────────────────────────────────
1. TCP Check
   • Connect to port
   • If connection succeeds → healthy
   • Fast, but doesn't check application

2. HTTP Check
   • GET /health endpoint
   • Check response code (200 = healthy)
   • More thorough, application-aware

3. Custom Check
   • Application-specific logic
   • Check database connectivity
   • Check dependencies

Health Check Configuration:
─────────────────────────────────────────────────────────────
Interval: 10s        (check every 10 seconds)
Timeout: 5s          (wait 5s for response)
Healthy Threshold: 2 (2 successes = healthy)
Unhealthy Threshold: 3 (3 failures = unhealthy)
```

---

## 5. Load Balancer Features

### SSL/TLS Termination
```
┌─────────────────────────────────────────────────────────────┐
│              SSL Termination at LB                          │
└─────────────────────────────────────────────────────────────┘

Without SSL Termination:
─────────────────────────────────────────────────────────────
    Client
        │ HTTPS
        ▼
    ┌──────────┐
    │   LB     │  ← Decrypts
    └────┬─────┘
         │ HTTP (internal)
         ▼
    ┌──────────┐
    │ Server   │  ← Must handle SSL
    └──────────┘

With SSL Termination:
─────────────────────────────────────────────────────────────
    Client
        │ HTTPS
        ▼
    ┌──────────┐
    │   LB     │  ← Handles SSL certificate
    │          │     Decrypts here
    └────┬─────┘
         │ HTTP (internal, unencrypted)
         ▼
    ┌──────────┐
    │ Server   │  ← No SSL needed
    └──────────┘

Benefits:
• Offload SSL processing from servers
• Centralized certificate management
• Easier certificate updates
• Better performance (servers don't encrypt/decrypt)
```

### Path-Based Routing
```
┌─────────────────────────────────────────────────────────────┐
│              Path-Based Routing (L7)                        │
└─────────────────────────────────────────────────────────────┘

    Client
        │
        │ GET /api/users
        ▼
    ┌──────────┐
    │   L7 LB  │
    │          │
    │ Rules:
    │ /api/* → API Servers
    │ /static/* → Static Servers
    │ /admin/* → Admin Servers
    └────┬─────┘
         │
         │ /api/users
         ▼
    ┌──────────┐
    │ API      │
    │ Servers  │
    └──────────┘

    Client
        │
        │ GET /static/logo.png
        ▼
    ┌──────────┐
    │   L7 LB  │
    └────┬─────┘
         │
         │ /static/logo.png
         ▼
    ┌──────────┐
    │ Static   │
    │ Servers  │
    └──────────┘
```

---

## 6. Load Balancing Best Practices

### Best Practices Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Load Balancing Best Practices                   │
└─────────────────────────────────────────────────────────────┘

Algorithm Selection:
☐ Use Round-Robin for similar servers
☐ Use Weighted Round-Robin for mixed capacity
☐ Use Least Connections for long-lived connections
☐ Use Least Response Time for performance-critical apps
☐ Use IP Hash only when sticky sessions needed

Health Checks:
☐ Configure appropriate interval (10-30s)
☐ Set reasonable timeout (5s)
☐ Use HTTP health checks for application awareness
☐ Implement /health endpoint in application
☐ Monitor health check failures

Session Management:
☐ Prefer external session store (Redis) over sticky sessions
☐ If sticky sessions needed, use cookie-based
☐ Set appropriate session timeout
☐ Handle server failures gracefully

SSL/TLS:
☐ Terminate SSL at load balancer
☐ Use strong cipher suites
☐ Keep certificates updated
☐ Enable HTTP/2 or HTTP/3

Monitoring:
☐ Track request distribution
☐ Monitor server health
☐ Alert on server failures
☐ Track response times
☐ Monitor connection counts

High Availability:
☐ Use multiple load balancers (active-passive or active-active)
☐ Configure automatic failover
☐ Test failover scenarios
☐ Monitor load balancer health
```

---

## Key Takeaways

### Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Load Balancing Summary                         │
└─────────────────────────────────────────────────────────────┘

1. Algorithms
   • Round-Robin: Simple, equal distribution
   • Weighted: Different server capacities
   • Least Connections: Long-lived connections
   • IP Hash: Sticky sessions

2. Session Management
   • External store (Redis) preferred
   • Sticky sessions when needed
   • Handle failures gracefully

3. Health Checks
   • Regular monitoring
   • Automatic removal of unhealthy servers
   • Application-aware checks

4. Features
   • SSL termination
   • Path-based routing (L7)
   • High availability

Remember:
• Choose algorithm based on use case
• Monitor and adjust
• Design for failures
• Use health checks
• Prefer stateless applications
```

---

**Next: Part 6 will cover Connection Pooling (Database and HTTP).**

