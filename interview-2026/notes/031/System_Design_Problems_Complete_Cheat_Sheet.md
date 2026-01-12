# System Design Problems - Complete Cheat Sheet
## All 100 Problems with Quick Reference Solutions

---

## 📋 Quick Navigation

- [Problems 1-20: Social, E-Commerce, Content, Search, Storage](#problems-1-20)
- [Problems 21-40: Storage, Analytics, Real-Time, Payment](#problems-21-40)
- [Problems 41-60: Infrastructure, Specialized, Additional Systems](#problems-41-60)
- [Problems 61-80: Content, Search, Storage, Analytics](#problems-61-80)
- [Problems 81-100: Real-Time, Infrastructure, Advanced](#problems-81-100)
- [Database Selection Quick Reference](#database-selection-quick-reference)
- [Architecture Patterns Quick Reference](#architecture-patterns-quick-reference)

---

## Problems 1-20

### 1. Social Media Platform
**Scale**: 1B+ users, 100M+ posts/day  
**Primary DB**: Neo4j (Graph) + Cassandra + Redis  
**Key Features**: User relationships, posts, feeds, recommendations  
**Architecture**: Graph DB for relationships, Cassandra for posts, Redis for feeds  
**Key Pattern**: Fan-out on write for feeds

### 2. Chat Application
**Scale**: 100M+ users, 1B+ messages/day  
**Primary DB**: Cassandra + Redis + PostgreSQL  
**Key Features**: Real-time messaging, group chats, presence  
**Architecture**: Cassandra for messages, Redis for presence, PostgreSQL for metadata  
**Key Pattern**: WebSocket for real-time, message queuing

### 3. Video Conferencing
**Scale**: 10M+ concurrent users  
**Primary DB**: Redis + PostgreSQL + S3  
**Key Features**: Video streaming, screen sharing, recording  
**Architecture**: Redis for sessions, PostgreSQL for metadata, S3 for recordings  
**Key Pattern**: WebRTC, media servers, CDN

### 4. Notification System
**Scale**: 1B+ notifications/day  
**Primary DB**: Redis + Cassandra + RabbitMQ  
**Key Features**: Push notifications, email, SMS, in-app  
**Architecture**: Redis for real-time, Cassandra for history, RabbitMQ for queuing  
**Key Pattern**: Pub-sub, message routing

### 5. Friend Recommendation
**Scale**: 1B+ users, complex graph  
**Primary DB**: Neo4j + Elasticsearch + Redis  
**Key Features**: Graph traversal, similarity scoring  
**Architecture**: Neo4j for graph, Elasticsearch for search, Redis for cache  
**Key Pattern**: Graph algorithms, collaborative filtering

### 6. E-Commerce Platform
**Scale**: 100M+ products, 10M+ orders/day  
**Primary DB**: PostgreSQL + Elasticsearch + Redis  
**Key Features**: Products, orders, payments, inventory  
**Architecture**: PostgreSQL for transactions, Elasticsearch for search, Redis for cache  
**Key Pattern**: ACID transactions, inventory management

### 7. Food Delivery
**Scale**: 10M+ orders/day, real-time tracking  
**Primary DB**: PostgreSQL (PostGIS) + Redis + MongoDB  
**Key Features**: Order management, real-time tracking, delivery routing  
**Architecture**: PostGIS for location, Redis for real-time, MongoDB for orders  
**Key Pattern**: Geospatial queries, real-time updates

### 8. Ride-Sharing
**Scale**: 50M+ users, real-time matching  
**Primary DB**: Redis + PostgreSQL + TimescaleDB  
**Key Features**: Driver-rider matching, real-time location, pricing  
**Architecture**: Redis GeoHash for matching, PostgreSQL for data, TimescaleDB for analytics  
**Key Pattern**: Spatial indexing, surge pricing

### 9. Hotel Booking
**Scale**: 1M+ hotels, 10M+ bookings/day  
**Primary DB**: PostgreSQL + Elasticsearch + Redis  
**Key Features**: Search, booking, availability, pricing  
**Architecture**: PostgreSQL for bookings, Elasticsearch for search, Redis for availability  
**Key Pattern**: Overbooking management, search optimization

### 10. Stock Trading
**Scale**: 1M+ trades/sec, low latency  
**Primary DB**: PostgreSQL + Redis + TimescaleDB  
**Key Features**: Order matching, real-time quotes, historical data  
**Architecture**: PostgreSQL for orders, Redis for quotes, TimescaleDB for history  
**Key Pattern**: Order book, matching engine

### 11. Video Streaming
**Scale**: 100M+ users, 1B+ views/day  
**Primary DB**: Cassandra + S3 + Elasticsearch  
**Key Features**: Video storage, streaming, recommendations  
**Architecture**: S3 for videos, Cassandra for metadata, Elasticsearch for search  
**Key Pattern**: CDN, adaptive bitrate, transcoding

### 12. Music Streaming
**Scale**: 50M+ songs, 100M+ users  
**Primary DB**: PostgreSQL + S3 + Redis  
**Key Features**: Music storage, streaming, playlists, recommendations  
**Architecture**: S3 for audio, PostgreSQL for metadata, Redis for cache  
**Key Pattern**: Streaming, playlist management

### 13. News Feed
**Scale**: 500M+ users, 1B+ posts/day  
**Primary DB**: Redis + Cassandra + Neo4j  
**Key Features**: Feed generation, ranking, personalization  
**Architecture**: Redis for feeds, Cassandra for posts, Neo4j for relationships  
**Key Pattern**: Fan-out on write, ranking algorithms

### 14. Blogging Platform
**Scale**: 10M+ blogs, 100M+ posts  
**Primary DB**: PostgreSQL + Elasticsearch + Redis  
**Key Features**: Blog creation, search, comments, tags  
**Architecture**: PostgreSQL for blogs, Elasticsearch for search, Redis for cache  
**Key Pattern**: Full-text search, tagging

### 15. Photo Sharing
**Scale**: 1B+ photos, 100M+ users  
**Primary DB**: Cassandra + S3 + Redis  
**Key Features**: Photo storage, sharing, albums, search  
**Architecture**: S3 for photos, Cassandra for metadata, Redis for cache  
**Key Pattern**: Image processing, CDN

### 16. Web Search Engine
**Scale**: 1T+ pages, 1B+ queries/day  
**Primary DB**: Elasticsearch + BigTable + Cassandra  
**Key Features**: Web crawling, indexing, ranking  
**Architecture**: BigTable for storage, Elasticsearch for search, Cassandra for index  
**Key Pattern**: Inverted index, PageRank, distributed crawling

### 17. Distributed Search
**Scale**: 1B+ documents, distributed  
**Primary DB**: Elasticsearch + PostgreSQL + Redis  
**Key Features**: Distributed indexing, search, ranking  
**Architecture**: Elasticsearch cluster, PostgreSQL for metadata, Redis for cache  
**Key Pattern**: Sharding, replication, distributed queries

### 18. Typeahead/Autocomplete
**Scale**: 1B+ queries/day, <10ms latency  
**Primary DB**: Redis + Elasticsearch  
**Key Features**: Real-time suggestions, prefix matching  
**Architecture**: Redis Trie for fast lookup, Elasticsearch for complex queries  
**Key Pattern**: Trie data structure, caching

### 19. Product Search
**Scale**: 100M+ products, complex filters  
**Primary DB**: Elasticsearch + PostgreSQL + Redis  
**Key Features**: Product search, filtering, faceting, sorting  
**Architecture**: Elasticsearch for search, PostgreSQL for data, Redis for cache  
**Key Pattern**: Faceted search, aggregations

### 20. Location-Based Search
**Scale**: 10M+ locations, geospatial queries  
**Primary DB**: PostgreSQL (PostGIS) + Elasticsearch  
**Key Features**: Location search, proximity, geofencing  
**Architecture**: PostGIS for spatial queries, Elasticsearch for text search  
**Key Pattern**: Geospatial indexing, distance calculations

---

## Problems 21-40

### 21. Distributed File System
**Scale**: 1PB+ storage, distributed  
**Primary DB**: PostgreSQL + HDFS + Redis  
**Key Features**: File storage, replication, metadata  
**Architecture**: HDFS for storage, PostgreSQL for metadata, Redis for cache  
**Key Pattern**: Distributed storage, replication

### 22. Cloud Storage
**Scale**: 1EB+ storage, 1B+ files  
**Primary DB**: PostgreSQL + S3 + Redis  
**Key Features**: File upload, download, versioning, sharing  
**Architecture**: S3 for storage, PostgreSQL for metadata, Redis for cache  
**Key Pattern**: Object storage, versioning, deduplication

### 23. Distributed Cache
**Scale**: 1B+ keys, sub-millisecond latency  
**Primary DB**: Redis Cluster + PostgreSQL  
**Key Features**: Caching, TTL, eviction, replication  
**Architecture**: Redis cluster, PostgreSQL for persistence  
**Key Pattern**: Cache-aside, write-through, sharding

### 24. Key-Value Store
**Scale**: 1B+ keys, high throughput  
**Primary DB**: DynamoDB + Redis  
**Key Features**: Key-value operations, scaling, consistency  
**Architecture**: DynamoDB for managed, Redis for in-memory  
**Key Pattern**: Consistent hashing, replication

### 25. Distributed Database
**Scale**: 1PB+ data, distributed  
**Primary DB**: Cassandra + PostgreSQL + Redis  
**Key Features**: Sharding, replication, consistency  
**Architecture**: Cassandra for scale, PostgreSQL for transactions, Redis for cache  
**Key Pattern**: Sharding, replication, consistency models

### 26. Distributed Logging
**Scale**: 1TB+ logs/day, real-time  
**Primary DB**: Elasticsearch + Kafka + S3  
**Key Features**: Log aggregation, search, retention  
**Architecture**: Kafka for streaming, Elasticsearch for search, S3 for archive  
**Key Pattern**: Log aggregation, indexing, retention

### 27. Metrics Collection
**Scale**: 1M+ metrics/sec, time-series  
**Primary DB**: Prometheus + TimescaleDB  
**Key Features**: Metrics collection, querying, alerting  
**Architecture**: Prometheus for collection, TimescaleDB for long-term  
**Key Pattern**: Time-series storage, aggregation

### 28. Real-Time Analytics
**Scale**: 1B+ events/day, real-time  
**Primary DB**: Kafka + TimescaleDB + Redis  
**Key Features**: Event processing, aggregations, dashboards  
**Architecture**: Kafka for streaming, TimescaleDB for storage, Redis for cache  
**Key Pattern**: Stream processing, windowing

### 29. URL Shortener
**Scale**: 1B+ URLs, 100M+ redirects/day  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: URL shortening, redirect, analytics  
**Architecture**: Redis for cache, PostgreSQL for storage  
**Key Pattern**: Base62 encoding, caching

### 30. Rate Limiter
**Scale**: 1B+ requests/day, distributed  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Rate limiting, sliding window, distributed  
**Architecture**: Redis for counters, PostgreSQL for persistence  
**Key Pattern**: Token bucket, sliding window

### 31. Real-Time Multiplayer Game
**Scale**: 1M+ concurrent players  
**Primary DB**: Redis + PostgreSQL + Cassandra  
**Key Features**: Game state, real-time updates, leaderboards  
**Architecture**: Redis for state, PostgreSQL for data, Cassandra for logs  
**Key Pattern**: State synchronization, event sourcing

### 32. Live Streaming Platform
**Scale**: 10M+ concurrent viewers  
**Primary DB**: Redis + PostgreSQL + S3  
**Key Features**: Live streaming, chat, recording  
**Architecture**: Redis for real-time, PostgreSQL for data, S3 for recordings  
**Key Pattern**: WebRTC, CDN, transcoding

### 33. Collaborative Editor
**Scale**: 1M+ documents, real-time  
**Primary DB**: PostgreSQL + Redis + Operational Transform  
**Key Features**: Real-time editing, conflict resolution  
**Architecture**: PostgreSQL for storage, Redis for real-time, OT for conflicts  
**Key Pattern**: Operational Transform, CRDTs

### 34. Real-Time Dashboard
**Scale**: 1M+ metrics, real-time  
**Primary DB**: Redis + TimescaleDB + Elasticsearch  
**Key Features**: Real-time metrics, visualizations, alerts  
**Architecture**: Redis for real-time, TimescaleDB for history, Elasticsearch for search  
**Key Pattern**: Stream processing, aggregation

### 35. Leaderboard System
**Scale**: 100M+ users, real-time rankings  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Rankings, updates, queries  
**Architecture**: Redis Sorted Sets for rankings, PostgreSQL for persistence  
**Key Pattern**: Sorted sets, periodic sync

### 36. Payment Processing
**Scale**: 10M+ transactions/day, ACID  
**Primary DB**: PostgreSQL + Redis + Message Queue  
**Key Features**: Payments, refunds, fraud detection  
**Architecture**: PostgreSQL for transactions, Redis for cache, MQ for async  
**Key Pattern**: ACID transactions, idempotency

### 37. Wallet System
**Scale**: 100M+ wallets, transactions  
**Primary DB**: PostgreSQL + Redis  
**Key Features**: Balance, transactions, transfers  
**Architecture**: PostgreSQL for transactions, Redis for balance cache  
**Key Pattern**: Double-entry bookkeeping, transactions

### 38. Cryptocurrency Exchange
**Scale**: 1M+ trades/sec, low latency  
**Primary DB**: PostgreSQL + Redis + TimescaleDB  
**Key Features**: Trading, order book, history  
**Architecture**: PostgreSQL for orders, Redis for order book, TimescaleDB for history  
**Key Pattern**: Order matching, order book

### 39. Billing System
**Scale**: 10M+ invoices/month  
**Primary DB**: PostgreSQL + TimescaleDB  
**Key Features**: Invoicing, payments, reporting  
**Architecture**: PostgreSQL for invoices, TimescaleDB for metrics  
**Key Pattern**: Invoice generation, payment tracking

### 40. Fraud Detection
**Scale**: 1B+ transactions, real-time  
**Primary DB**: Redis + PostgreSQL + ML Store  
**Key Features**: Fraud detection, ML models, rules  
**Architecture**: Redis for real-time, PostgreSQL for data, ML for detection  
**Key Pattern**: Real-time scoring, ML inference

---

## Problems 41-60

### 41. Load Balancer
**Scale**: 1B+ requests/day  
**Primary DB**: Redis (session) + Configuration DB  
**Key Features**: Request routing, health checks, session persistence  
**Architecture**: Load balancer, Redis for sessions, config DB  
**Key Pattern**: Round-robin, least connections, health checks

### 42. Service Discovery
**Scale**: 1000+ services  
**Primary DB**: etcd/Consul + PostgreSQL  
**Key Features**: Service registration, discovery, health checks  
**Architecture**: etcd/Consul for service registry, PostgreSQL for metadata  
**Key Pattern**: Service registry, health monitoring

### 43. Distributed Configuration
**Scale**: 1000+ services, configurations  
**Primary DB**: etcd/Consul + Git  
**Key Features**: Configuration management, versioning, updates  
**Architecture**: etcd/Consul for config, Git for versioning  
**Key Pattern**: Configuration service, versioning

### 44. Distributed Lock Service
**Scale**: 1M+ locks/sec  
**Primary DB**: Redis/ZooKeeper  
**Key Features**: Distributed locks, timeouts, deadlock prevention  
**Architecture**: Redis or ZooKeeper for locks  
**Key Pattern**: Redlock, lease-based locks

### 45. Distributed Task Scheduler
**Scale**: 1M+ tasks/day  
**Primary DB**: PostgreSQL + Redis + Message Queue  
**Key Features**: Task scheduling, execution, retries  
**Architecture**: PostgreSQL for tasks, Redis for scheduling, MQ for execution  
**Key Pattern**: Cron scheduling, distributed execution

### 46. Distributed Counter
**Scale**: 1B+ counters, 1M+ increments/sec  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Atomic increments, distributed, aggregation  
**Architecture**: Redis for counters, PostgreSQL for persistence  
**Key Pattern**: Atomic operations, aggregation

### 47. Distributed Pub-Sub
**Scale**: 1B+ messages/day, 1M+ messages/sec  
**Primary DB**: Kafka + Redis  
**Key Features**: Publishing, subscription, partitioning  
**Architecture**: Kafka for pub-sub, Redis for cache  
**Key Pattern**: Topic partitioning, consumer groups

### 48. Distributed Queue
**Scale**: 1B+ messages/day  
**Primary DB**: RabbitMQ/Kafka + Redis  
**Key Features**: Message queuing, delivery guarantees  
**Architecture**: RabbitMQ/Kafka for queue, Redis for cache  
**Key Pattern**: Message queuing, delivery guarantees

### 49. Distributed Tracing
**Scale**: 1B+ traces/day  
**Primary DB**: Elasticsearch + Kafka  
**Key Features**: Request tracing, span collection, analysis  
**Architecture**: Kafka for collection, Elasticsearch for storage  
**Key Pattern**: Distributed tracing, span correlation

### 50. Distributed ID Generator
**Scale**: 1M+ IDs/sec, unique  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Unique ID generation, distributed  
**Architecture**: Redis for counters, PostgreSQL for sequences  
**Key Pattern**: Snowflake, UUID, sequential IDs

### 51. Social Network Graph
**Scale**: 1B+ users, complex graph  
**Primary DB**: Neo4j + Cassandra  
**Key Features**: Graph relationships, traversal, queries  
**Architecture**: Neo4j for graph, Cassandra for data  
**Key Pattern**: Graph algorithms, traversal

### 52. Messaging Queue System
**Scale**: 1B+ messages/day  
**Primary DB**: Kafka + Redis  
**Key Features**: Message queuing, routing, delivery  
**Architecture**: Kafka for queue, Redis for cache  
**Key Pattern**: Message routing, delivery guarantees

### 53. Activity Feed
**Scale**: 1B+ activities/day  
**Primary DB**: Redis + Cassandra  
**Key Features**: Activity streams, aggregation, personalization  
**Architecture**: Redis for feeds, Cassandra for storage  
**Key Pattern**: Fan-out, aggregation

### 54. User Profile System
**Scale**: 1B+ users, profiles  
**Primary DB**: PostgreSQL + Redis  
**Key Features**: User profiles, updates, search  
**Architecture**: PostgreSQL for profiles, Redis for cache  
**Key Pattern**: Profile management, caching

### 55. Content Moderation
**Scale**: 1B+ content items/day  
**Primary DB**: PostgreSQL + Elasticsearch + ML  
**Key Features**: Content moderation, ML models, rules  
**Architecture**: PostgreSQL for data, Elasticsearch for search, ML for detection  
**Key Pattern**: ML inference, rule-based filtering

### 56. Shopping Cart
**Scale**: 100M+ carts, sessions  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Cart management, persistence, expiration  
**Architecture**: Redis for sessions, PostgreSQL for persistence  
**Key Pattern**: Session management, cart persistence

### 57. Inventory Management
**Scale**: 100M+ products, real-time  
**Primary DB**: PostgreSQL + Redis  
**Key Features**: Inventory tracking, updates, reservations  
**Architecture**: PostgreSQL for inventory, Redis for cache  
**Key Pattern**: Inventory locking, reservations

### 58. Recommendation Engine
**Scale**: 1B+ recommendations/day  
**Primary DB**: Redis + Feature Store + ML  
**Key Features**: Recommendations, ML models, personalization  
**Architecture**: Redis for cache, Feature Store for features, ML for models  
**Key Pattern**: Collaborative filtering, ML inference

### 59. Review System
**Scale**: 100M+ reviews, search  
**Primary DB**: PostgreSQL + Elasticsearch  
**Key Features**: Reviews, ratings, search, moderation  
**Architecture**: PostgreSQL for reviews, Elasticsearch for search  
**Key Pattern**: Review management, search

### 60. Payment Gateway
**Scale**: 10M+ transactions/day  
**Primary DB**: PostgreSQL + Redis + External APIs  
**Key Features**: Payment processing, gateway integration, reconciliation  
**Architecture**: PostgreSQL for transactions, Redis for cache, APIs for gateways  
**Key Pattern**: Payment processing, idempotency

---

## Problems 61-80

### 61. Content Delivery Network
**Scale**: 1PB+ content, global  
**Primary DB**: S3/CDN + Redis  
**Key Features**: Content delivery, caching, edge locations  
**Architecture**: CDN for delivery, S3 for origin, Redis for cache  
**Key Pattern**: Edge caching, origin pull

### 62. Image Processing Service
**Scale**: 1B+ images, processing  
**Primary DB**: S3 + Queue + Processing  
**Key Features**: Image upload, processing, storage  
**Architecture**: S3 for storage, Queue for processing, Workers for processing  
**Key Pattern**: Async processing, image transformation

### 63. Video Transcoding
**Scale**: 1M+ videos, transcoding  
**Primary DB**: S3 + Queue + Workers  
**Key Features**: Video upload, transcoding, delivery  
**Architecture**: S3 for storage, Queue for jobs, Workers for transcoding  
**Key Pattern**: Job queuing, parallel processing

### 64. Document Management
**Scale**: 100M+ documents, search  
**Primary DB**: PostgreSQL + S3 + Elasticsearch  
**Key Features**: Document storage, search, versioning  
**Architecture**: S3 for storage, PostgreSQL for metadata, Elasticsearch for search  
**Key Pattern**: Document storage, full-text search

### 65. Media Library
**Scale**: 1B+ media files  
**Primary DB**: PostgreSQL + S3 + Elasticsearch  
**Key Features**: Media storage, search, metadata  
**Architecture**: S3 for media, PostgreSQL for metadata, Elasticsearch for search  
**Key Pattern**: Media management, search

### 66. Enterprise Search
**Scale**: 1B+ documents, search  
**Primary DB**: Elasticsearch + PostgreSQL  
**Key Features**: Enterprise search, indexing, security  
**Architecture**: Elasticsearch for search, PostgreSQL for data  
**Key Pattern**: Full-text search, security

### 67. Semantic Search
**Scale**: 1B+ documents, vector search  
**Primary DB**: Vector DB + Elasticsearch  
**Key Features**: Semantic search, embeddings, similarity  
**Architecture**: Vector DB for embeddings, Elasticsearch for hybrid  
**Key Pattern**: Vector search, embeddings

### 68. Image Search
**Scale**: 1B+ images, similarity search  
**Primary DB**: Vector DB + Elasticsearch + S3  
**Key Features**: Image search, similarity, reverse image search  
**Architecture**: Vector DB for embeddings, S3 for images, Elasticsearch for search  
**Key Pattern**: Image embeddings, similarity search

### 69. Voice Search
**Scale**: 1M+ queries/day  
**Primary DB**: Elasticsearch + Speech Processing  
**Key Features**: Voice search, speech recognition, search  
**Architecture**: Speech processing, Elasticsearch for search  
**Key Pattern**: Speech-to-text, search

### 70. Multi-Language Search
**Scale**: 100+ languages, search  
**Primary DB**: Elasticsearch + Translation  
**Key Features**: Multi-language search, translation, indexing  
**Architecture**: Elasticsearch for search, Translation for queries  
**Key Pattern**: Language detection, translation

### 71. Object Storage Service
**Scale**: 1EB+ storage, objects  
**Primary DB**: S3/Blob Storage  
**Key Features**: Object storage, versioning, lifecycle  
**Architecture**: Object storage, metadata management  
**Key Pattern**: Object storage, versioning

### 72. Block Storage Service
**Scale**: 1PB+ storage, blocks  
**Primary DB**: Block Storage + Metadata DB  
**Key Features**: Block storage, volumes, snapshots  
**Architecture**: Block storage, metadata DB  
**Key Pattern**: Block storage, snapshots

### 73. Backup System
**Scale**: 1PB+ backups, scheduling  
**Primary DB**: S3 + PostgreSQL + Scheduling  
**Key Features**: Backup, restore, scheduling, retention  
**Architecture**: S3 for storage, PostgreSQL for metadata, Scheduling  
**Key Pattern**: Backup scheduling, retention

### 74. Archive System
**Scale**: 1EB+ archives, long-term  
**Primary DB**: S3/Glacier + PostgreSQL  
**Key Features**: Archiving, retrieval, lifecycle  
**Architecture**: Glacier for archive, PostgreSQL for metadata  
**Key Pattern**: Cold storage, lifecycle management

### 75. Data Lake
**Scale**: 1EB+ data, analytics  
**Primary DB**: S3/HDFS + Metadata Catalog  
**Key Features**: Data storage, catalog, analytics  
**Architecture**: S3/HDFS for storage, Catalog for metadata  
**Key Pattern**: Data lake, schema-on-read

### 76. Business Intelligence
**Scale**: 1TB+ data, analytics  
**Primary DB**: Data Warehouse + OLAP  
**Key Features**: BI, reporting, analytics, dashboards  
**Architecture**: Data warehouse, OLAP cubes  
**Key Pattern**: OLAP, dimensional modeling

### 77. Data Pipeline
**Scale**: 1TB+ data/day, processing  
**Primary DB**: Kafka + Processing + Storage  
**Key Features**: Data ingestion, processing, storage  
**Architecture**: Kafka for streaming, Processing, Storage  
**Key Pattern**: ETL, stream processing

### 78. Event Sourcing
**Scale**: 1B+ events, replay  
**Primary DB**: Event Store + Snapshots  
**Key Features**: Event storage, replay, snapshots  
**Architecture**: Event store, snapshot store  
**Key Pattern**: Event sourcing, CQRS

### 79. CQRS System
**Scale**: 1B+ reads, 100M+ writes  
**Primary DB**: Read DB + Write DB  
**Key Features**: Command-query separation, optimization  
**Architecture**: Separate read/write stores  
**Key Pattern**: CQRS, eventual consistency

### 80. Data Warehouse
**Scale**: 1PB+ data, analytics  
**Primary DB**: Columnar DB + ETL  
**Key Features**: Data warehouse, analytics, reporting  
**Architecture**: Columnar DB, ETL pipelines  
**Key Pattern**: Star schema, fact tables

---

## Problems 81-100

### 81. Real-Time Collaboration
**Scale**: 1M+ documents, real-time  
**Primary DB**: Redis + Operational Transform  
**Key Features**: Real-time editing, conflict resolution  
**Architecture**: Redis for real-time, OT for conflicts  
**Key Pattern**: Operational Transform, CRDTs

### 82. Live Chat Support
**Scale**: 10M+ chats/day  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Live chat, routing, history  
**Architecture**: Redis for real-time, PostgreSQL for history  
**Key Pattern**: Real-time messaging, routing

### 83. Real-Time Notifications
**Scale**: 1B+ notifications/day  
**Primary DB**: Redis + WebSocket  
**Key Features**: Real-time notifications, delivery  
**Architecture**: Redis for pub-sub, WebSocket for delivery  
**Key Pattern**: Pub-sub, push notifications

### 84. Live Analytics Dashboard
**Scale**: 1M+ metrics, real-time  
**Primary DB**: Redis + TimescaleDB  
**Key Features**: Real-time metrics, dashboards  
**Architecture**: Redis for real-time, TimescaleDB for history  
**Key Pattern**: Stream processing, aggregation

### 85. Real-Time Monitoring
**Scale**: 1M+ metrics, monitoring  
**Primary DB**: Prometheus + Grafana  
**Key Features**: Metrics, alerting, dashboards  
**Architecture**: Prometheus for metrics, Grafana for visualization  
**Key Pattern**: Time-series, alerting

### 86. API Gateway
**Scale**: 1B+ requests/day  
**Primary DB**: Redis + Configuration DB  
**Key Features**: Request routing, authentication, rate limiting  
**Architecture**: API Gateway, Redis for cache, Config DB  
**Key Pattern**: Gateway pattern, routing

### 87. Service Mesh
**Scale**: 1000+ services  
**Primary DB**: Configuration + Observability  
**Key Features**: Service communication, observability, security  
**Architecture**: Service mesh, sidecar proxies  
**Key Pattern**: Service mesh, mTLS

### 88. Container Registry
**Scale**: 1M+ images, storage  
**Primary DB**: Object Storage + Metadata DB  
**Key Features**: Image storage, metadata, security  
**Architecture**: Object storage, metadata DB  
**Key Pattern**: Image storage, versioning

### 89. CI/CD Pipeline
**Scale**: 10K+ builds/day  
**Primary DB**: Git + Artifact Storage  
**Key Features**: Build, test, deploy  
**Architecture**: Git for source, Artifact storage  
**Key Pattern**: CI/CD, pipeline

### 90. Infrastructure Monitoring
**Scale**: 1M+ metrics, monitoring  
**Primary DB**: Prometheus + TimescaleDB  
**Key Features**: Infrastructure metrics, alerting  
**Architecture**: Prometheus for metrics, TimescaleDB for storage  
**Key Pattern**: Time-series, monitoring

### 91. Feature Flag System
**Scale**: 1000+ flags, real-time  
**Primary DB**: Redis + PostgreSQL  
**Key Features**: Feature flags, targeting, analytics  
**Architecture**: Redis for real-time, PostgreSQL for storage  
**Key Pattern**: Feature flags, targeting

### 92. A/B Testing Platform
**Scale**: 1B+ experiments/day  
**Primary DB**: PostgreSQL + Redis + Analytics  
**Key Features**: Experiments, targeting, analytics  
**Architecture**: PostgreSQL for data, Redis for cache, Analytics  
**Key Pattern**: Experimentation, statistical analysis

### 93. Experimentation Platform
**Scale**: 1B+ experiments/day  
**Primary DB**: PostgreSQL + Analytics  
**Key Features**: Experiments, analysis, reporting  
**Architecture**: PostgreSQL for data, Analytics  
**Key Pattern**: Experimentation, analysis

### 94. Configuration Service
**Scale**: 1000+ services, configs  
**Primary DB**: etcd/Consul + Git  
**Key Features**: Configuration management, versioning  
**Architecture**: etcd/Consul for config, Git for versioning  
**Key Pattern**: Configuration service, versioning

### 95. Secrets Management
**Scale**: 1M+ secrets, security  
**Primary DB**: Vault + Encryption  
**Key Features**: Secret storage, rotation, access control  
**Architecture**: Vault for secrets, Encryption  
**Key Pattern**: Secret management, rotation

### 96. Multi-Tenant SaaS
**Scale**: 10K+ tenants, 100M+ users  
**Primary DB**: PostgreSQL + Redis + Isolation  
**Key Features**: Tenant isolation, data segregation, billing  
**Architecture**: PostgreSQL with tenant_id, Redis for cache, Isolation  
**Key Pattern**: Tenant isolation, data segregation

### 97. Microservices Architecture
**Scale**: 1000+ services, distributed  
**Primary DB**: Multiple DBs + Service Mesh  
**Key Features**: Service independence, communication, data  
**Architecture**: Database per service, Service mesh  
**Key Pattern**: Microservices, database per service

### 98. Event-Driven Architecture
**Scale**: 1B+ events/day  
**Primary DB**: Kafka + Event Stores  
**Key Features**: Event streaming, processing, storage  
**Architecture**: Kafka for streaming, Event stores  
**Key Pattern**: Event-driven, event sourcing

### 99. Serverless Platform
**Scale**: 1B+ invocations/day  
**Primary DB**: Managed Services + Functions  
**Key Features**: Function execution, scaling, storage  
**Architecture**: Managed services, Functions  
**Key Pattern**: Serverless, auto-scaling

### 100. Edge Computing Platform
**Scale**: Distributed, edge nodes  
**Primary DB**: Distributed DBs + Edge Nodes  
**Key Features**: Edge computing, data processing, latency  
**Architecture**: Edge nodes, Distributed DBs  
**Key Pattern**: Edge computing, distributed processing

---

## Database Selection Quick Reference

### By Data Model
```
Graph Data → Neo4j, ArangoDB
Document Data → MongoDB, CouchDB
Key-Value Data → Redis, DynamoDB
Wide Column → Cassandra, HBase
Time-Series → TimescaleDB, InfluxDB, Prometheus
Relational → PostgreSQL, MySQL
Search → Elasticsearch, Solr
Vector → Pinecone, Weaviate
```

### By Consistency Requirement
```
Strong Consistency → PostgreSQL, MySQL, MongoDB (transactions)
Eventual Consistency → Cassandra, DynamoDB, MongoDB (default)
Configurable → MongoDB, Cassandra
```

### By Scale
```
< 1M users → PostgreSQL, MySQL
1M-100M users → PostgreSQL (replicas), MongoDB
> 100M users → Cassandra, DynamoDB, MongoDB (sharded)
```

### By Read/Write Pattern
```
Read-Heavy → Redis (cache), PostgreSQL (replicas)
Write-Heavy → Cassandra, DynamoDB
Balanced → PostgreSQL, MongoDB
```

### By Latency Requirement
```
< 1ms → Redis (in-memory)
< 10ms → PostgreSQL, MongoDB (with cache)
< 100ms → Most databases
```

### By Query Complexity
```
Simple Queries → Redis, DynamoDB
Moderate Queries → MongoDB, Cassandra
Complex Queries → PostgreSQL, MySQL
```

---

## Architecture Patterns Quick Reference

### Caching Patterns
```
Cache-Aside: App checks cache → DB if miss → Update cache
Write-Through: Write to cache + DB simultaneously
Write-Back: Write to cache → Async write to DB
Refresh-Ahead: Proactive cache refresh
```

### Load Balancing
```
Round-Robin: Distribute sequentially
Least Connections: Route to least busy
IP Hash: Route by client IP
Weighted: Route by server capacity
```

### Database Scaling
```
Vertical Scaling: Add more CPU/RAM
Horizontal Scaling: Add more servers
Sharding: Partition data across servers
Read Replicas: Scale reads independently
```

### Consistency Models
```
Strong Consistency: All nodes see same data
Eventual Consistency: Eventually consistent
Weak Consistency: No guarantees
Causal Consistency: Causally related events ordered
```

### Message Patterns
```
Point-to-Point: One consumer per message
Pub-Sub: Multiple consumers per message
Request-Reply: Synchronous communication
Fan-out: Broadcast to all subscribers
```

### Microservices Patterns
```
API Gateway: Single entry point
Service Discovery: Find services dynamically
Circuit Breaker: Prevent cascading failures
Database Per Service: Independent databases
Event Sourcing: Store events, rebuild state
CQRS: Separate read/write models
```

---

## Quick Decision Matrix

### When to Use What Database?

| Use Case | Primary DB | Why |
|----------|-----------|-----|
| Social Network | Neo4j | Graph relationships |
| High-Volume Writes | Cassandra | Horizontal scaling |
| Real-Time Cache | Redis | Sub-millisecond latency |
| ACID Transactions | PostgreSQL | Strong consistency |
| Full-Text Search | Elasticsearch | Relevance ranking |
| Time-Series | TimescaleDB | Time-optimized |
| Document Storage | MongoDB | Flexible schema |
| Managed Service | DynamoDB | Auto-scaling |
| Media Storage | S3 | Cost-effective, scalable |
| Message Queue | Kafka | High throughput |

---

## Common System Design Patterns

### Fan-Out Patterns
```
Fan-Out on Write: Push to all followers (Twitter)
Fan-Out on Read: Fetch on demand (Facebook)
Hybrid: Fan-out for celebrities, pull for regular users
```

### Storage Patterns
```
Object Storage: S3 for media, files
Block Storage: EBS for volumes
File Storage: EFS for shared files
```

### Processing Patterns
```
Synchronous: Immediate processing
Asynchronous: Queue-based processing
Batch Processing: Scheduled processing
Stream Processing: Real-time processing
```

---

## Interview Quick Tips

### System Design Process
1. **Clarify** (2 min): Requirements, scale, constraints
2. **Estimate** (3 min): Storage, bandwidth, requests
3. **Design** (10 min): High-level architecture
4. **Deep Dive** (10 min): Components, APIs, DB schema
5. **Optimize** (5 min): Bottlenecks, scalability
6. **Trade-offs** (5 min): Alternatives, decisions

### Key Questions to Ask
- What's the scale? (users, requests, data)
- What's the read/write ratio?
- What's the latency requirement?
- What's the consistency requirement?
- What are the constraints?

### Common Components
- Load Balancer
- API Gateway
- Application Servers
- Database (Primary + Secondary)
- Cache (Redis, Memcached)
- Message Queue (Kafka, RabbitMQ)
- CDN (for media)
- Search (Elasticsearch)

---

**Use this cheat sheet for quick reference during interview preparation!** 🚀

**Remember: Understanding the patterns and trade-offs is more important than memorizing solutions.**

