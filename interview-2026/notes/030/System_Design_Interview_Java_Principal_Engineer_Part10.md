# System Design Interview Questions for Java Principal Engineers - Part 10

## Real-World System Design Problems (Part 2) & Best Practices

This final part covers additional system design problems and interview best practices.

---

## Interview Question 45: Design Uber/Lyft

### Requirements

- Match riders with drivers
- Real-time location tracking
- Ride booking and tracking
- Payment processing
- Surge pricing

### Architecture

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  API Gateway    │
└──────┬──────────┘
       │
   ┌───┴───┬────────┬────────┐
   ▼       ▼        ▼        ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│Trip │ │Match│ │Pay  │ │Notif│
│Svc  │ │Svc  │ │Svc  │ │Svc  │
└──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
   │       │       │       │
   ▼       ▼       ▼       ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│MySQL│ │Redis│ │Kafka│ │S3   │
│     │ │Geo  │ │     │ │     │
└─────┘ └─────┘ └─────┘ └─────┘
```

### Implementation

```java
@Service
public class DriverMatchingService {
    @Autowired
    private RedisGeoOperations<String, String> geoOps;
    
    @Autowired
    private DriverRepository driverRepository;
    
    public List<Driver> findNearbyDrivers(double latitude, double longitude, 
                                          double radiusKm) {
        // Use Redis GeoHash for spatial queries
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);
        
        GeoResults<GeoLocation<String>> results = geoOps.radius(
            "drivers:available",
            location,
            distance
        );
        
        List<String> driverIds = results.getContent().stream()
            .map(geoLocation -> geoLocation.getContent().getName())
            .collect(Collectors.toList());
        
        return driverRepository.findByIds(driverIds);
    }
    
    public Driver matchDriver(String riderId, double latitude, double longitude) {
        // Find nearby drivers
        List<Driver> nearbyDrivers = findNearbyDrivers(latitude, longitude, 5.0);
        
        if (nearbyDrivers.isEmpty()) {
            throw new NoDriversAvailableException();
        }
        
        // Select best driver (closest, highest rating)
        Driver bestDriver = nearbyDrivers.stream()
            .max(Comparator
                .comparing(Driver::getRating)
                .thenComparing(d -> calculateDistance(
                    latitude, longitude, d.getLatitude(), d.getLongitude()
                )))
            .orElseThrow();
        
        // Update driver status
        bestDriver.setStatus(DriverStatus.ASSIGNED);
        driverRepository.save(bestDriver);
        
        return bestDriver;
    }
}

@Service
public class LocationTrackingService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void updateDriverLocation(String driverId, double lat, double lon) {
        String key = "location:driver:" + driverId;
        redis.opsForValue().set(key, lat + "," + lon, Duration.ofMinutes(5));
        
        // Update GeoHash index
        geoOps.add("drivers:available", 
            new Point(lon, lat), driverId);
    }
    
    public Location getDriverLocation(String driverId) {
        String locationStr = redis.opsForValue().get("location:driver:" + driverId);
        if (locationStr != null) {
            String[] parts = locationStr.split(",");
            return new Location(Double.parseDouble(parts[0]), 
                              Double.parseDouble(parts[1]));
        }
        return null;
    }
}
```

---

## Interview Question 46: Design a Distributed File Storage System (like Dropbox)

### Requirements

- Upload/download files
- File versioning
- Sync across devices
- File sharing
- Storage optimization (deduplication)

### Implementation

```java
@Service
public class FileStorageService {
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private FileMetadataRepository fileRepository;
    
    @Autowired
    private DeduplicationService deduplicationService;
    
    public FileMetadata uploadFile(String userId, MultipartFile file) {
        // 1. Calculate file hash for deduplication
        String fileHash = calculateSHA256(file);
        
        // 2. Check if file already exists
        FileMetadata existing = deduplicationService.findByHash(fileHash);
        String s3Key;
        
        if (existing != null) {
            // File exists - reuse storage
            s3Key = existing.getS3Key();
        } else {
            // Upload new file
            s3Key = "files/" + fileHash;
            s3Service.uploadFile(s3Key, file);
        }
        
        // 3. Create metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setId(generateId());
        metadata.setUserId(userId);
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileSize(file.getSize());
        metadata.setS3Key(s3Key);
        metadata.setFileHash(fileHash);
        metadata.setVersion(1);
        metadata.setCreatedAt(Instant.now());
        
        fileRepository.save(metadata);
        
        return metadata;
    }
    
    public void syncFile(String userId, String fileId) {
        FileMetadata file = fileRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException());
        
        // Check if file was modified
        String currentHash = s3Service.getFileHash(file.getS3Key());
        
        if (!currentHash.equals(file.getFileHash())) {
            // File changed - create new version
            createNewVersion(file);
        }
    }
}

@Service
public class DeduplicationService {
    @Autowired
    private FileHashRepository hashRepository;
    
    public FileMetadata findByHash(String hash) {
        FileHash fileHash = hashRepository.findByHash(hash);
        if (fileHash != null) {
            return fileRepository.findById(fileHash.getFileId());
        }
        return null;
    }
}
```

---

## Interview Question 47: Design a Distributed Task Scheduler

### Requirements

- Schedule tasks
- Distributed execution
- Fault tolerance
- Priority queues
- Cron-like scheduling

### Implementation

```java
@Entity
public class ScheduledTask {
    @Id
    private String id;
    private String name;
    private String cronExpression;
    private String handlerClass;
    private Map<String, Object> parameters;
    private TaskStatus status;
    private Instant nextExecution;
    private Instant lastExecution;
}

@Service
public class TaskSchedulerService {
    @Autowired
    private ScheduledTaskRepository taskRepository;
    
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Scheduled(fixedRate = 1000) // Check every second
    public void scheduleTasks() {
        Instant now = Instant.now();
        
        // Find tasks ready for execution
        List<ScheduledTask> readyTasks = taskRepository
            .findByStatusAndNextExecutionBefore(TaskStatus.ACTIVE, now);
        
        for (ScheduledTask task : readyTasks) {
            // Try to acquire lock
            if (acquireExecutionLock(task.getId())) {
                // Submit for execution
                executeTask(task);
            }
        }
    }
    
    private boolean acquireExecutionLock(String taskId) {
        String lockKey = "task:lock:" + taskId;
        Boolean acquired = redis.opsForValue().setIfAbsent(
            lockKey, 
            "locked",
            Duration.ofMinutes(10)
        );
        return Boolean.TRUE.equals(acquired);
    }
    
    @Async
    public void executeTask(ScheduledTask task) {
        try {
            // Load handler
            TaskHandler handler = (TaskHandler) Class.forName(task.getHandlerClass())
                .getDeclaredConstructor().newInstance();
            
            // Execute
            handler.execute(task.getParameters());
            
            // Update task
            task.setLastExecution(Instant.now());
            task.setNextExecution(calculateNextExecution(task.getCronExpression()));
            taskRepository.save(task);
            
        } catch (Exception e) {
            // Handle error
            handleTaskError(task, e);
        } finally {
            // Release lock
            releaseExecutionLock(task.getId());
        }
    }
}
```

---

## Interview Question 48: Design a Real-Time Analytics System

### Requirements

- Real-time event processing
- Aggregations (count, sum, avg)
- Time windows
- High throughput

### Implementation

```java
@Service
public class RealTimeAnalyticsService {
    @Autowired
    private KafkaTemplate<String, AnalyticsEvent> kafkaTemplate;
    
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public void recordEvent(AnalyticsEvent event) {
        // Publish to Kafka
        kafkaTemplate.send("analytics-events", event.getUserId(), event);
        
        // Update real-time counters
        updateRealTimeCounters(event);
    }
    
    private void updateRealTimeCounters(AnalyticsEvent event) {
        String minute = Instant.now().truncatedTo(ChronoUnit.MINUTES).toString();
        
        // Increment counters
        redis.opsForValue().increment("analytics:minute:" + minute + ":total");
        redis.opsForValue().increment("analytics:minute:" + minute + ":event:" + event.getType());
        
        // Update time-series
        redis.opsForZSet().add("analytics:timeseries", 
            event.getType() + ":" + minute, 
            Instant.now().toEpochMilli());
    }
    
    @KafkaListener(topics = "analytics-events", groupId = "analytics-processor")
    public void processEvent(AnalyticsEvent event) {
        // Aggregate by time windows
        aggregateByWindow(event, Duration.ofMinutes(1));
        aggregateByWindow(event, Duration.ofHours(1));
        aggregateByWindow(event, Duration.ofDays(1));
    }
    
    private void aggregateByWindow(AnalyticsEvent event, Duration window) {
        String windowKey = getWindowKey(event.getTimestamp(), window);
        
        // Update aggregations
        redis.opsForHash().increment(
            "analytics:agg:" + window + ":" + windowKey,
            event.getType(),
            1
        );
    }
}
```

---

## System Design Interview Best Practices

### 1. Requirements Clarification

**Always Ask:**
- What's the scale? (users, requests, data)
- What are the constraints? (latency, consistency)
- What are the priorities? (availability vs consistency)
- What's the use case? (read-heavy vs write-heavy)

### 2. Communication Strategy

**Structure Your Answer:**
1. **Clarify**: Ask questions first
2. **Estimate**: Capacity and scale
3. **Design**: High-level architecture
4. **Detail**: Components and APIs
5. **Optimize**: Identify bottlenecks
6. **Scale**: Handle growth

### 3. Common Mistakes to Avoid

**❌ Don't:**
- Jump to solutions without understanding
- Ignore non-functional requirements
- Design for perfect scenarios
- Over-engineer simple problems
- Forget about failure scenarios

**✅ Do:**
- Ask clarifying questions
- Start with high-level design
- Discuss trade-offs
- Consider failure cases
- Iterate and improve

### 4. Java-Specific Considerations

**For Principal Engineers:**
- **JVM Tuning**: Heap, GC, thread pools
- **Frameworks**: Spring Boot, Spring Cloud
- **Libraries**: Resilience4j, Micrometer, Hystrix
- **Monitoring**: APM, metrics, tracing
- **Performance**: Async processing, caching

### 5. Evaluation Criteria

**Interviewers Look For:**
- **Problem-Solving**: Systematic approach
- **Trade-offs**: Understanding pros/cons
- **Scalability**: Handling growth
- **Reliability**: Fault tolerance
- **Communication**: Clear explanations

---

## Complete System Design Checklist

### Functional Requirements
- [ ] Core features identified
- [ ] Use cases defined
- [ ] API endpoints designed
- [ ] Data models defined

### Non-Functional Requirements
- [ ] Capacity estimated
- [ ] Performance targets set
- [ ] Availability requirements
- [ ] Consistency model chosen

### Architecture
- [ ] High-level design complete
- [ ] Components identified
- [ ] Data flow defined
- [ ] Technology stack selected

### Scalability
- [ ] Horizontal scaling strategy
- [ ] Database sharding plan
- [ ] Caching strategy
- [ ] CDN usage

### Reliability
- [ ] Failure scenarios considered
- [ ] Redundancy planned
- [ ] Disaster recovery
- [ ] Monitoring and alerting

### Security
- [ ] Authentication mechanism
- [ ] Authorization strategy
- [ ] Data encryption
- [ ] API security

---

## Common System Design Patterns Summary

### 1. **Load Balancing**
- Round-robin, least connections, IP hash
- Health checking
- Session persistence

### 2. **Caching**
- Multi-level (L1, L2, L3)
- Cache-aside, write-through, write-back
- Eviction policies (LRU, LFU, TTL)

### 3. **Database**
- Master-slave replication
- Sharding strategies
- Read replicas

### 4. **Messaging**
- Pub-sub, point-to-point
- Message queues (Kafka, RabbitMQ)
- Event-driven architecture

### 5. **Microservices**
- Service discovery
- API Gateway
- Circuit breaker
- Distributed tracing

---

## Real-World System Design Problems Summary

### Part 9 & 10 Covered:
1. ✅ Twitter/X (tweets, timelines)
2. ✅ Chat/Messaging (WebSocket, real-time)
3. ✅ Video Streaming (upload, CDN, processing)
4. ✅ News Feed (pull vs push)
5. ✅ Uber/Lyft (matching, location tracking)
6. ✅ File Storage (Dropbox-like, deduplication)
7. ✅ Task Scheduler (distributed, cron)
8. ✅ Real-Time Analytics (streaming, aggregations)

---

## Key Takeaways for Principal Engineers

### 1. **Think in Trade-offs**
- Every design decision has pros and cons
- Discuss trade-offs explicitly
- Justify your choices

### 2. **Start Simple, Scale Later**
- Begin with basic design
- Add complexity as needed
- Optimize bottlenecks

### 3. **Consider Failure**
- What if a component fails?
- How to handle partial failures?
- Disaster recovery plan?

### 4. **Java Ecosystem**
- Leverage Spring Boot/Cloud
- Use proven libraries
- Consider JVM characteristics

### 5. **Communication Matters**
- Explain your thinking
- Draw diagrams
- Be open to feedback

---

## Final Interview Tips

### Before the Interview
- Review common system design problems
- Practice drawing diagrams
- Study scalability patterns
- Understand Java ecosystem

### During the Interview
- **Clarify first**: Ask questions
- **Think aloud**: Show your process
- **Start high-level**: Then dive deep
- **Discuss trade-offs**: Show understanding
- **Iterate**: Improve your design

### After the Interview
- Reflect on what went well
- Identify areas for improvement
- Study topics you struggled with

---

## Summary: Complete Series (Parts 1-10)

### Part 1: Fundamentals
- Scalability, URL Shortener, Cache, Rate Limiter

### Part 2: Infrastructure
- Load Balancing, Multi-level Caching, CDN, Sessions

### Part 3: Database
- Sharding, Replication, Time-series, Distributed Locks

### Part 4: Microservices
- Service Discovery, API Gateway, Circuit Breaker, Tracing

### Part 5: Messaging
- Message Queues, Event Sourcing, Saga, DLQ

### Part 6: Distributed Systems
- CAP Theorem, Consensus, Eventual Consistency, Quorum

### Part 7: Security
- Authentication, Authorization, Encryption, Secrets

### Part 8: Observability
- Logging, Metrics, Tracing, Alerting, APM

### Part 9: Real-World Problems (Part 1)
- Twitter, Chat, Video Streaming, News Feed

### Part 10: Real-World Problems (Part 2) & Best Practices
- Uber, File Storage, Task Scheduler, Analytics

---

**Total Coverage: 48+ System Design Interview Questions**

Each with:
- ✅ Complete requirements analysis
- ✅ Capacity estimation
- ✅ Architecture design
- ✅ Java implementation examples
- ✅ Trade-offs discussion
- ✅ Scalability considerations

---

**Master these concepts to excel in Principal Engineer System Design interviews!** 🚀

**Remember**: As a Principal Engineer, you're expected to show deep understanding, make informed trade-offs, and design systems that scale. Focus on real-world constraints and practical solutions.

