# Scalability Patterns - Complete Diagrams Guide (Part 2: Vertical Scaling)

## ⬆️ Vertical Scaling: Resource Upgrades & Capacity Planning

---

## 1. Vertical Scaling Overview

### Concept Diagram
```
┌─────────────────────────────────────────────────────────────┐
│              Vertical Scaling Concept                       │
└─────────────────────────────────────────────────────────────┘

Before Scaling:
    ┌──────────────┐
    │   Server     │
    │  CPU: 2 cores│
    │  RAM: 4 GB   │
    │  Disk: 100GB │
    └──────────────┘

After Vertical Scaling:
    ┌──────────────┐
    │   Server     │
    │  CPU: 8 cores│  ← Upgraded
    │  RAM: 32 GB  │  ← Upgraded
    │  Disk: 1 TB  │  ← Upgraded
    └──────────────┘

Key Principle:
- Upgrade existing server resources
- Add more CPU, RAM, Storage
- Scale up, not out
- Single server handles more load
```

### Vertical vs Horizontal Scaling
```
┌─────────────────────────────────────────────────────────────┐
│              Scaling Comparison                            │
└─────────────────────────────────────────────────────────────┘

Vertical Scaling (Scale Up):
    ┌──────────────┐
    │  Bigger      │
    │  Server      │
    │              │
    │  More CPU    │
    │  More RAM    │
    │  More Disk   │
    └──────────────┘
    
Pros:                    Cons:
✓ Simple                 ✗ Single point of failure
✓ No code changes        ✗ Limited by hardware
✓ Lower complexity       ✗ Downtime for upgrades
✓ Better for single      ✗ Expensive at scale
  threaded apps          ✗ Can't scale beyond max

Horizontal Scaling (Scale Out):
    ┌────┐  ┌────┐  ┌────┐  ┌────┐
    │ S1 │  │ S2 │  │ S3 │  │ S4 │
    └────┘  └────┘  └────┘  └────┘
    
Pros:                    Cons:
✓ High availability      ✗ More complex
✓ No downtime            ✗ Requires load balancer
✓ Cost-effective         ✗ Stateless services needed
✓ Unlimited scale        ✗ Network overhead
```

---

## 2. Resource Upgrade Strategies

### CPU Upgrading
```
┌─────────────────────────────────────────────────────────────┐
│              CPU Upgrade Strategy                           │
└─────────────────────────────────────────────────────────────┘

CPU Upgrade Path:
    Current:              Upgraded:
    ┌──────────┐         ┌──────────┐
    │ 2 cores  │  ────►  │ 8 cores  │
    │ 2.0 GHz  │         │ 3.5 GHz  │
    └──────────┘         └──────────┘

Impact Analysis:
    ┌─────────────────────────────────┐
    │  Before:                        │
    │  - Request handling: 100 req/s │
    │  - CPU usage: 95%              │
    │  - Response time: 500ms         │
    └─────────────────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │  After:                          │
    │  - Request handling: 400 req/s  │
    │  - CPU usage: 60%               │
    │  - Response time: 150ms          │
    └─────────────────────────────────┘

Considerations:
- CPU-bound applications benefit most
- Multi-threaded applications scale better
- Check application thread limits
- Monitor CPU utilization patterns
```

### Memory (RAM) Upgrading
```
┌─────────────────────────────────────────────────────────────┐
│              Memory Upgrade Strategy                        │
└─────────────────────────────────────────────────────────────┘

Memory Upgrade Path:
    Current:              Upgraded:
    ┌──────────┐         ┌──────────┐
    │  4 GB    │  ────►  │  32 GB   │
    │  RAM     │         │  RAM     │
    └──────────┘         └──────────┘

Memory Usage Patterns:
    ┌─────────────────────────────────┐
    │  Application Memory:            │
    │  ┌──────────────────────────┐ │
    │  │ Heap: 2 GB                │ │
    │  │ Stack: 500 MB             │ │
    │  │ Cache: 1 GB               │ │
    │  │ OS: 500 MB                │ │
    │  └──────────────────────────┘ │
    │  Total: 4 GB (100% used)      │
    └─────────────────────────────────┘
                │
                ▼ Upgrade to 32 GB
    ┌─────────────────────────────────┐
    │  Application Memory:            │
    │  ┌──────────────────────────┐ │
    │  │ Heap: 8 GB                │ │
    │  │ Stack: 2 GB               │ │
    │  │ Cache: 16 GB              │ │ ← More cache
    │  │ OS: 1 GB                 │ │
    │  └──────────────────────────┘ │
    │  Total: 27 GB (84% used)      │
    └─────────────────────────────────┘

Benefits:
- Larger in-memory caches
- More concurrent users
- Better garbage collection
- Reduced disk I/O
```

### Storage Upgrading
```
┌─────────────────────────────────────────────────────────────┐
│              Storage Upgrade Strategy                       │
└─────────────────────────────────────────────────────────────┘

Storage Upgrade Path:
    Current:              Upgraded:
    ┌──────────┐         ┌──────────┐
    │ 100 GB   │  ────►  │  2 TB     │
    │  HDD     │         │  SSD      │
    │ 5400 RPM │         │  NVMe     │
    └──────────┘         └──────────┘

Performance Comparison:
    ┌─────────────────────────────────┐
    │  HDD (5400 RPM):                │
    │  - Read: 100 MB/s               │
    │  - Write: 80 MB/s               │
    │  - IOPS: 100                    │
    │  - Latency: 10-15 ms             │
    └─────────────────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │  SSD (SATA):                     │
    │  - Read: 550 MB/s                │
    │  - Write: 520 MB/s               │
    │  - IOPS: 50,000                  │
    │  - Latency: 0.1 ms               │
    └─────────────────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │  NVMe SSD:                       │
    │  - Read: 3,500 MB/s               │
    │  - Write: 3,000 MB/s             │
    │  - IOPS: 500,000                 │
    │  - Latency: 0.05 ms              │
    └─────────────────────────────────┘

Impact:
- Database queries: 10x faster
- Application startup: 5x faster
- Log writes: Near-instant
- File operations: Dramatically improved
```

---

## 3. Capacity Planning

### Capacity Planning Process
```
┌─────────────────────────────────────────────────────────────┐
│              Capacity Planning Workflow                     │
└─────────────────────────────────────────────────────────────┘

Step 1: Current State Analysis
    ┌──────────────────────┐
    │  Current Metrics:     │
    │  - CPU: 85% avg       │
    │  - RAM: 90% used      │
    │  - Disk I/O: 80%      │
    │  - Network: 60%        │
    │  - Requests: 1000/s   │
    └──────────────────────┘
            │
            ▼
Step 2: Growth Projection
    ┌──────────────────────┐
    │  Projected Growth:   │
    │  - 6 months: +50%     │
    │  - 12 months: +200%  │
    │  - 24 months: +500%  │
    └──────────────────────┘
            │
            ▼
Step 3: Resource Calculation
    ┌──────────────────────┐
    │  Required Resources:  │
    │  - CPU: 16 cores      │
    │  - RAM: 64 GB         │
    │  - Storage: 2 TB SSD   │
    │  - Network: 10 Gbps    │
    └──────────────────────┘
            │
            ▼
Step 4: Cost-Benefit Analysis
    ┌──────────────────────┐
    │  Upgrade Cost:       │
    │  - Hardware: $X      │
    │  - Downtime: Y hrs  │
    │  - Migration: Z hrs │
    │                     │
    │  Benefits:           │
    │  - Performance: +X%  │
    │  - Capacity: +Y%      │
    │  - ROI: Z months     │
    └──────────────────────┘
```

### Resource Utilization Monitoring
```
┌─────────────────────────────────────────────────────────────┐
│              Resource Utilization Dashboard                  │
└─────────────────────────────────────────────────────────────┘

Time-based Monitoring:
    CPU Usage (%)
    100│
       │     ╱╲
       │    ╱  ╲
     75│   ╱    ╲
       │  ╱      ╲
     50│ ╱        ╲
       │╱          ╲
     25│            ╲
       │              ╲
      0└──────────────────► Time
        0h  6h  12h  18h  24h
    
    Memory Usage (%)
    100│
       │  ────────────────
     75│  │
       │  │
     50│  │
       │  │
     25│  │
       │  │
      0└──────────────────► Time
    
    Disk I/O (MB/s)
    100│
       │     ╱╲
       │    ╱  ╲
     75│   ╱    ╲
       │  ╱      ╲
     50│ ╱        ╲
       │╱          ╲
     25│            ╲
       │              ╲
      0└──────────────────► Time

Thresholds:
- Warning: > 70%
- Critical: > 85%
- Upgrade needed: > 90% sustained
```

### Capacity Planning Formulas
```
┌─────────────────────────────────────────────────────────────┐
│              Capacity Planning Calculations                 │
└─────────────────────────────────────────────────────────────┘

1. CPU Capacity:
   Required CPU = (Current CPU × Growth Factor) / Utilization
   
   Example:
   Current: 4 cores at 80% = 3.2 effective cores
   Growth: 2x
   Required: (4 × 2) / 0.7 = 11.4 cores → 12 cores

2. Memory Capacity:
   Required RAM = (Current RAM × Growth Factor) + Buffer
   
   Example:
   Current: 16 GB at 90% = 14.4 GB used
   Growth: 2x
   Buffer: 20%
   Required: (16 × 2) × 1.2 = 38.4 GB → 40 GB

3. Storage Capacity:
   Required Storage = Current + (Daily Growth × Retention)
   
   Example:
   Current: 500 GB
   Daily Growth: 10 GB
   Retention: 90 days
   Required: 500 + (10 × 90) = 1,400 GB → 2 TB

4. Network Capacity:
   Required Bandwidth = Peak Traffic × Safety Factor
   
   Example:
   Peak: 1 Gbps
   Safety: 1.5x
   Required: 1 × 1.5 = 1.5 Gbps → 10 Gbps (next tier)
```

---

## 4. Vertical Scaling Scenarios

### Database Server Scaling
```
┌─────────────────────────────────────────────────────────────┐
│              Database Vertical Scaling                      │
└─────────────────────────────────────────────────────────────┘

Before:
    ┌──────────────────────┐
    │  Database Server     │
    │  CPU: 4 cores       │
    │  RAM: 16 GB          │
    │  Storage: 500 GB HDD │
    │                      │
    │  Performance:        │
    │  - Queries: 1000/s   │
    │  - Avg latency: 50ms │
    │  - Cache hit: 60%   │
    └──────────────────────┘
            │
            ▼ Upgrade
After:
    ┌──────────────────────┐
    │  Database Server     │
    │  CPU: 16 cores      │  ← 4x more cores
    │  RAM: 128 GB         │  ← 8x more RAM
    │  Storage: 2 TB SSD   │  ← 4x capacity, 50x faster
    │                      │
    │  Performance:        │
    │  - Queries: 5000/s   │  ← 5x improvement
    │  - Avg latency: 10ms  │  ← 5x faster
    │  - Cache hit: 95%   │  ← More cache
    └──────────────────────┘

Key Upgrades:
1. More CPU cores → Parallel query execution
2. More RAM → Larger buffer pool, more cache
3. SSD → Faster I/O, lower latency
4. Faster network → Better replication
```

### Application Server Scaling
```
┌─────────────────────────────────────────────────────────────┐
│              Application Server Scaling                     │
└─────────────────────────────────────────────────────────────┘

Before:
    ┌──────────────────────┐
    │  App Server          │
    │  CPU: 2 cores       │
    │  RAM: 4 GB          │
    │  JVM Heap: 2 GB     │
    │                      │
    │  Capacity:           │
    │  - Concurrent users: 500│
    │  - Requests/sec: 200 │
    │  - Response time: 300ms│
    └──────────────────────┘
            │
            ▼ Upgrade
After:
    ┌──────────────────────┐
    │  App Server          │
    │  CPU: 8 cores       │  ← 4x more cores
    │  RAM: 32 GB         │  ← 8x more RAM
    │  JVM Heap: 16 GB    │  ← 8x larger heap
    │                      │
    │  Capacity:           │
    │  - Concurrent users: 4000│ ← 8x improvement
    │  - Requests/sec: 1600│ ← 8x improvement
    │  - Response time: 100ms│ ← 3x faster
    └──────────────────────┘

JVM Tuning:
- -Xmx16g: Max heap size
- -Xms16g: Initial heap size
- -XX:ParallelGCThreads=8: GC threads
- -XX:+UseG1GC: G1 garbage collector
```

---

## 5. Upgrade Procedures

### Zero-Downtime Upgrade Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Zero-Downtime Upgrade Process                   │
└─────────────────────────────────────────────────────────────┘

Method 1: Blue-Green Deployment
    ┌──────────────┐
    │  Production  │
    │  (Blue)      │  ← Current server
    │  CPU: 4      │
    │  RAM: 16GB   │
    └──────────────┘
            │
            │ (Traffic continues)
            │
            ▼
    ┌──────────────┐
    │  Staging     │
    │  (Green)     │  ← New upgraded server
    │  CPU: 16     │
    │  RAM: 128GB  │
    └──────────────┘
            │
            │ (Test & validate)
            │
            ▼
    ┌──────────────┐
    │  Switch      │  ← DNS/LB switch
    │  Traffic     │
    └──────────────┘
            │
            ▼
    ┌──────────────┐
    │  Production  │
    │  (Green)     │  ← Now serving traffic
    └──────────────┘

Steps:
1. Provision new server with upgrades
2. Deploy application to new server
3. Run smoke tests
4. Switch traffic (DNS/Load Balancer)
5. Monitor new server
6. Decommission old server
```

### Rolling Upgrade Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Rolling Upgrade Process                         │
└─────────────────────────────────────────────────────────────┘

Cluster of 3 Servers:
    ┌────┐  ┌────┐  ┌────┐
    │ S1 │  │ S2 │  │ S3 │
    │ 4GB │  │ 4GB │  │ 4GB │
    └────┘  └────┘  └────┘
      │       │       │
      └───────┴───────┘
            │
            ▼
    Step 1: Upgrade S1
    ┌────┐  ┌────┐  ┌────┐
    │ S1 │  │ S2 │  │ S3 │
    │32GB│  │ 4GB │  │ 4GB │  ← S1 upgraded
    └────┘  └────┘  └────┘
      │       │       │
      └───────┴───────┘
            │
            ▼
    Step 2: Upgrade S2
    ┌────┐  ┌────┐  ┌────┐
    │ S1 │  │ S2 │  │ S3 │
    │32GB│  │32GB│  │ 4GB │  ← S2 upgraded
    └────┘  └────┘  └────┘
      │       │       │
      └───────┴───────┘
            │
            ▼
    Step 3: Upgrade S3
    ┌────┐  ┌────┐  ┌────┐
    │ S1 │  │ S2 │  │ S3 │
    │32GB│  │32GB│  │32GB│  ← All upgraded
    └────┘  └────┘  └────┘

Benefits:
- No downtime
- Gradual migration
- Easy rollback
- Test each upgrade
```

---

## 6. Cost Analysis

### Vertical Scaling Cost Model
```
┌─────────────────────────────────────────────────────────────┐
│              Cost Comparison                                │
└─────────────────────────────────────────────────────────────┘

Cost per Request Analysis:
    ┌─────────────────────────────────┐
    │  Small Server (4 CPU, 16GB):     │
    │  - Cost: $100/month              │
    │  - Capacity: 1,000 req/s         │
    │  - Cost/req: $0.0001             │
    └─────────────────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │  Medium Server (8 CPU, 32GB):   │
    │  - Cost: $300/month             │
    │  - Capacity: 3,000 req/s        │
    │  - Cost/req: $0.0001            │
    └─────────────────────────────────┘
                │
                ▼
    ┌─────────────────────────────────┐
    │  Large Server (16 CPU, 64GB):   │
    │  - Cost: $800/month             │
    │  - Capacity: 8,000 req/s        │
    │  - Cost/req: $0.0001            │
    └─────────────────────────────────┘

Break-even Analysis:
- Small: Good for < 1,000 req/s
- Medium: Good for 1,000-3,000 req/s
- Large: Good for > 3,000 req/s

Diminishing Returns:
- Very large servers: Higher cost/performance ratio
- Consider horizontal scaling beyond certain point
```

---

## Key Takeaways

### When to Use Vertical Scaling
```
┌─────────────────────────────────────────────────────────────┐
│              Vertical Scaling Decision Matrix                │
└─────────────────────────────────────────────────────────────┘

Use Vertical Scaling When:
✓ Single-threaded applications
✓ Database servers (often)
✓ Applications with stateful connections
✓ Limited budget for infrastructure changes
✓ Quick capacity boost needed
✓ Applications that benefit from more RAM/CPU
✓ Development/staging environments

Avoid Vertical Scaling When:
✗ Need high availability
✗ Application is stateless
✗ Need to scale beyond hardware limits
✗ Cost-effective horizontal scaling available
✗ Need geographic distribution
```

---

**Next: Part 3 will cover Auto-Scaling with metrics-based and predictive scaling.**

