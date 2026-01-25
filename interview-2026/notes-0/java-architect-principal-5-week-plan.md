# 5-Week Interview Prep Plan (Java Principal Engineer / Architect)

## Week 1: Core Java, JVM, Concurrency
- **Java/JVM:** Memory model (happens-before, volatile, atomics), GC (G1/ZGC basics), classloading. Perf: escape analysis, allocation profiling (JFR/async-profiler), lock contention.
- **Concurrency:** Executors, CompletableFuture, parallelism vs concurrency, thread safety (immutability, confinement, CAS). Blocking vs non-blocking IO; backpressure basics.
- **Coding drills:** Rate limiter (token bucket), bounded queue with backpressure, memoizer with eviction; add timeouts/retries/circuit-breakers.
- **Reading:** Effective Java (equals/hashCode, immutability, generics, concurrency).
- **Mock:** Core Java + concurrency deep-dive; profile a small app and explain findings.

## Week 2: Data Modeling, Persistence, Transactions
- **Data modeling:** Relational vs document vs key-value vs graph; indexing; denormalization trade-offs; schema evolution.
- **Persistence:** JPA pitfalls (N+1, batching, caching), isolation levels, locking; transaction anomalies and remedies; 2PC vs sagas.
- **Distributed data:** Replication modes, quorum, RYW/causal; partitioning/sharding, secondary indexes across shards, hotspots.
- **Coding drills:** Ledger/payments schema; idempotent write path with outbox; DAO layer with pagination, optimistic locking, batching.
- **Mock:** Persistence/design scenario; consistency vs availability trade-offs.

## Week 3: Distributed Systems & Architecture Patterns
- **Fundamentals:** CAP/FLP, consensus basics (Raft/Paxos), failure detection, fencing tokens. Messaging semantics: at-most/at-least/effectively-once; idempotent consumers/producers.
- **Patterns:** Microservices vs modular monolith; bounded contexts; async vs sync; CQRS/event sourcing/outbox/CDC. Caching (aside/write-through/write-behind, stampede protection). Resilience (retries/backoff, circuit breakers, bulkheads, rate limiting).
- **Cloud/infra:** Kubernetes basics (deployment/service/HPA, readiness/liveness), service mesh concepts. Observability (RED/USE, tracing, structured logging, SLO/error budgets).
- **Coding drills:** Design a high-scale service (payments, dispatch, or feed) with SLAs, partitioning, caching, queues, and failure modes. Implement idempotent Kafka consumer with retries + DLQ + tracing.
- **Mock:** System design interview; defend trade-offs.

## Week 4: APIs, Security, Ops Excellence
- **APIs:** REST fundamentals (versioning, pagination, filtering, partial responses), GraphQL vs REST; contracts (OpenAPI), backward compatibility, consumer-driven contracts.
- **Security:** OAuth2/OIDC, JWT validation, mTLS; multi-tenant isolation; secrets/KMS, PII handling, logging hygiene.
- **Ops/DevEx:** CI/CD strategies (blue/green, canary, feature flags), rollback plans. Cost/perf levers (caching, pooling, batching, right-sizing, autoscaling signals).
- **Coding drills:** Secure a service with JWT verification, RBAC, audit logging. Build a canary/feature-flag rollout plan; add observability to track impact.
- **Mock:** Production readiness + security deep-dive.

## Week 5: Synthesis, Mocks, and Gaps
- **Consolidation:** Fill weak areas; create one-pagers (consistency models, GC tuning, idempotency, backpressure).
- **Full mocks:** 2–3 loops covering coding (LLD), system design, and leadership/behavioral on architectural decision-making and influence.
- **Leadership/Behavioral:** Stories on migrations, incidents, deprecations, build-vs-buy, mentoring, platform standards.
- **Final drills:** Design variants under constraints (low-latency vs high-throughput; strict consistency vs AP; multi-region failover). Implement a resilience wrapper (retry/backoff/circuit breaker) and an outbox processor with metrics.
- **Interview kit:** Cheat sheets (APIs, data stores, messaging, resilience patterns, SLOs) and 3–5 polished impact stories with metrics.

### Tips
- Keep daily hands-on coding; pair tests with refactors.
- After each mock, write a short retro and fix one issue next day.
- Anchor answers in trade-offs and SLAs; cover correctness, scalability, operability, and security. 

