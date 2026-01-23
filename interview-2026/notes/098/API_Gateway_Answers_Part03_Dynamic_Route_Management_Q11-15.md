# API Gateway Answers - Part 3: Dynamic Route Management (Questions 11-15)

## Question 11: You "stored dynamic route management in PostgreSQL, enabling real-time route configuration updates." How did you implement this?

### Answer

### Dynamic Route Management with PostgreSQL

#### 1. **Architecture Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Dynamic Route Management Architecture          │
└─────────────────────────────────────────────────────────┘

┌──────────────┐         ┌──────────────┐
│  Admin API   │────────▶│  PostgreSQL  │
│  (CRUD)      │         │  (Routes DB) │
└──────────────┘         └──────┬───────┘
                                │
                                │ Change Event
                                │
                    ┌───────────▼───────────┐
                    │  Route Change Service │
                    │  (Event Publisher)    │
                    └───────────┬───────────┘
                                │
                                │ WebSocket Notification
                                │
                    ┌───────────▼───────────┐
                    │  API Gateway         │
                    │  (Route Locator)     │
                    └───────────┬───────────┘
                                │
                                │ Cache Update
                                │
                    ┌───────────▼───────────┐
                    │  Route Cache         │
                    │  (In-Memory)         │
                    └──────────────────────┘
```

#### 2. **Database Schema Design**

```sql
-- Routes table
CREATE TABLE routes (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(500) NOT NULL,
    uri VARCHAR(500) NOT NULL,
    predicates JSONB NOT NULL,
    filters JSONB,
    order_number INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT true,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Route predicates JSON structure
-- {
--   "path": "/api/v1/**",
--   "headers": {"X-Tenant": "tenant1"},
--   "methods": ["GET", "POST"],
--   "query": {"version": "v1"}
-- }

-- Route filters JSON structure
-- {
--   "rewritePath": {
--     "regexp": "/api/v1/(?<segment>.*)",
--     "replacement": "/${segment}"
--   },
--   "addRequestHeader": {
--     "name": "X-Gateway",
--     "value": "spring-cloud"
--   },
--   "retry": {
--     "retries": 3,
--     "statuses": [500, 502, 503]
--   }
-- }

-- Indexes for performance
CREATE INDEX idx_routes_enabled ON routes(enabled);
CREATE INDEX idx_routes_tenant ON routes(tenant_id);
CREATE INDEX idx_routes_path ON routes USING gin(path gin_trgm_ops);
```

#### 3. **Route Entity and Repository**

```java
@Entity
@Table(name = "routes")
public class RouteEntity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String path;
    
    @Column(nullable = false, length = 500)
    private String uri;
    
    @Column(columnDefinition = "jsonb", nullable = false)
    @Type(JsonType.class)
    private Map<String, Object> predicates;
    
    @Column(columnDefinition = "jsonb")
    @Type(JsonType.class)
    private Map<String, Object> filters;
    
    @Column(name = "order_number")
    private Integer orderNumber = 0;
    
    private Boolean enabled = true;
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and setters
}

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, String> {
    @Query("SELECT r FROM RouteEntity r WHERE r.enabled = true " +
           "ORDER BY r.orderNumber ASC")
    List<RouteEntity> findActiveRoutes();
    
    @Query("SELECT r FROM RouteEntity r WHERE r.enabled = true " +
           "AND (r.tenantId = :tenantId OR r.tenantId IS NULL) " +
           "ORDER BY r.orderNumber ASC")
    List<RouteEntity> findActiveRoutesForTenant(@Param("tenantId") String tenantId);
    
    @Modifying
    @Query("UPDATE RouteEntity r SET r.enabled = :enabled WHERE r.id = :id")
    void updateRouteStatus(@Param("id") String id, @Param("enabled") Boolean enabled);
}
```

#### 4. **Route Service Implementation**

```java
@Service
@Transactional
public class RouteService {
    private final RouteRepository routeRepository;
    private final RouteChangeEventPublisher eventPublisher;
    
    public RouteEntity createRoute(RouteCreateRequest request) {
        RouteEntity route = new RouteEntity();
        route.setId(UUID.randomUUID().toString());
        route.setName(request.getName());
        route.setPath(request.getPath());
        route.setUri(request.getUri());
        route.setPredicates(request.getPredicates());
        route.setFilters(request.getFilters());
        route.setOrderNumber(request.getOrderNumber());
        route.setTenantId(request.getTenantId());
        route.setCreatedAt(LocalDateTime.now());
        
        RouteEntity saved = routeRepository.save(route);
        
        // Publish change event
        eventPublisher.publishRouteCreated(saved);
        
        return saved;
    }
    
    public RouteEntity updateRoute(String id, RouteUpdateRequest request) {
        RouteEntity route = routeRepository.findById(id)
            .orElseThrow(() -> new RouteNotFoundException(id));
        
        route.setPath(request.getPath());
        route.setUri(request.getUri());
        route.setPredicates(request.getPredicates());
        route.setFilters(request.getFilters());
        route.setOrderNumber(request.getOrderNumber());
        route.setUpdatedAt(LocalDateTime.now());
        
        RouteEntity updated = routeRepository.save(route);
        
        // Publish change event
        eventPublisher.publishRouteUpdated(updated);
        
        return updated;
    }
    
    public void deleteRoute(String id) {
        RouteEntity route = routeRepository.findById(id)
            .orElseThrow(() -> new RouteNotFoundException(id));
        
        routeRepository.delete(route);
        
        // Publish change event
        eventPublisher.publishRouteDeleted(id);
    }
    
    public void enableRoute(String id) {
        routeRepository.updateRouteStatus(id, true);
        eventPublisher.publishRouteStatusChanged(id, true);
    }
    
    public void disableRoute(String id) {
        routeRepository.updateRouteStatus(id, false);
        eventPublisher.publishRouteStatusChanged(id, false);
    }
}
```

---

## Question 12: How did you achieve real-time route updates without service restarts?

### Answer

### Real-Time Route Updates

#### 1. **Real-Time Update Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Route Update Flow                    │
└─────────────────────────────────────────────────────────┘

1. Route Change
   ├─ Admin updates route in database
   └─ RouteService saves change

2. Event Publication
   ├─ RouteChangeEvent published
   └─ Event sent to WebSocket channel

3. WebSocket Notification
   ├─ Gateway instances subscribed to channel
   └─ Notification received

4. Cache Invalidation
   ├─ Route cache invalidated
   └─ Routes reloaded from database

5. Active Routes Updated
   ├─ New routes active immediately
   └─ No service restart required
```

#### 2. **WebSocket-Based Notification**

```java
@Component
public class RouteChangeEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;
    
    public void publishRouteCreated(RouteEntity route) {
        RouteChangeEvent event = new RouteChangeEvent(
            "CREATED", route.getId(), route);
        messagingTemplate.convertAndSend("/topic/routes/changes", event);
    }
    
    public void publishRouteUpdated(RouteEntity route) {
        RouteChangeEvent event = new RouteChangeEvent(
            "UPDATED", route.getId(), route);
        messagingTemplate.convertAndSend("/topic/routes/changes", event);
    }
    
    public void publishRouteDeleted(String routeId) {
        RouteChangeEvent event = new RouteChangeEvent(
            "DELETED", routeId, null);
        messagingTemplate.convertAndSend("/topic/routes/changes", event);
    }
    
    public void publishRouteStatusChanged(String routeId, boolean enabled) {
        RouteChangeEvent event = new RouteChangeEvent(
            enabled ? "ENABLED" : "DISABLED", routeId, null);
        messagingTemplate.convertAndSend("/topic/routes/changes", event);
    }
}
```

#### 3. **Route Change Listener**

```java
@Component
public class RouteChangeListener {
    private final DynamicRouteLocator routeLocator;
    
    @EventListener
    public void handleRouteChange(RouteChangeEvent event) {
        log.info("Route change event received: {}", event.getAction());
        
        switch (event.getAction()) {
            case "CREATED":
            case "UPDATED":
                routeLocator.refreshRoute(event.getRoute());
                break;
            case "DELETED":
            case "DISABLED":
                routeLocator.removeRoute(event.getRouteId());
                break;
            case "ENABLED":
                routeLocator.enableRoute(event.getRouteId());
                break;
        }
    }
    
    @RabbitListener(queues = "route-changes")
    public void handleRouteChangeMessage(RouteChangeMessage message) {
        // Handle route change from message queue
        handleRouteChange(message.toEvent());
    }
}
```

#### 4. **Dynamic Route Locator with Cache**

```java
@Component
public class DynamicRouteLocator implements RouteLocator {
    private final RouteRepository routeRepository;
    private final Cache<String, Route> routeCache;
    private final AtomicReference<Flux<Route>> routesRef = new AtomicReference<>();
    
    @PostConstruct
    public void initialize() {
        loadRoutes();
        
        // Subscribe to route changes
        subscribeToRouteChanges();
    }
    
    @Override
    public Flux<Route> getRoutes() {
        return routesRef.get()
            .switchIfEmpty(loadRoutes())
            .cache(Duration.ofMinutes(5));
    }
    
    private Flux<Route> loadRoutes() {
        return Flux.fromIterable(routeRepository.findActiveRoutes())
            .map(this::convertToRoute)
            .doOnNext(route -> routeCache.put(route.getId(), route))
            .doOnComplete(() -> {
                routesRef.set(Flux.fromIterable(routeCache.asMap().values()));
            });
    }
    
    public void refreshRoute(RouteEntity entity) {
        Route route = convertToRoute(entity);
        routeCache.put(route.getId(), route);
        
        // Update routes reference
        routesRef.set(Flux.fromIterable(routeCache.asMap().values()));
        
        log.info("Route refreshed: {}", route.getId());
    }
    
    public void removeRoute(String routeId) {
        routeCache.invalidate(routeId);
        routesRef.set(Flux.fromIterable(routeCache.asMap().values()));
        log.info("Route removed: {}", routeId);
    }
    
    private void subscribeToRouteChanges() {
        // WebSocket subscription
        messagingTemplate.getClientInboundChannel()
            .subscribe(message -> {
                if (message.getHeaders().get("destination")
                    .equals("/topic/routes/changes")) {
                    RouteChangeEvent event = (RouteChangeEvent) 
                        message.getPayload();
                    handleRouteChange(event);
                }
            });
    }
}
```

---

## Question 13: What was your database schema for route management?

### Answer

### Database Schema Design

#### 1. **Complete Schema**

```sql
-- Routes table
CREATE TABLE routes (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    path VARCHAR(500) NOT NULL,
    uri VARCHAR(500) NOT NULL,
    predicates JSONB NOT NULL,
    filters JSONB,
    order_number INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT true,
    tenant_id VARCHAR(50),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 1
);

-- Route predicates structure
-- {
--   "path": "/api/v1/**",
--   "headers": {
--     "X-Tenant": "tenant1",
--     "X-Version": "v1"
--   },
--   "methods": ["GET", "POST"],
--   "query": {
--     "version": "v1"
--   },
--   "host": "api.example.com",
--   "remoteAddr": "192.168.1.0/24"
-- }

-- Route filters structure
-- {
--   "rewritePath": {
--     "regexp": "/api/v1/(?<segment>.*)",
--     "replacement": "/${segment}"
--   },
--   "addRequestHeader": [
--     {"name": "X-Gateway", "value": "spring-cloud"},
--     {"name": "X-Tenant", "value": "${tenantId}"}
--   ],
--   "addResponseHeader": [
--     {"name": "X-Response-Time", "value": "${responseTime}"}
--   ],
--   "retry": {
--     "retries": 3,
--     "statuses": [500, 502, 503],
--     "methods": ["GET"],
--     "backoff": {
--       "firstBackoff": "50ms",
--       "maxBackoff": "500ms",
--       "factor": 2
--     }
--   },
--   "circuitBreaker": {
--     "name": "service-circuit-breaker",
--     "fallbackUri": "forward:/fallback"
--   },
--   "rateLimiter": {
--     "replenishRate": 10,
--     "burstCapacity": 20,
--     "requestedTokens": 1
--   }
-- }

-- Route metadata (optional)
-- {
--   "tags": ["production", "high-priority"],
--   "owner": "team-a",
--   "documentation": "https://docs.example.com/routes/service-a"
-- }

-- Indexes
CREATE INDEX idx_routes_enabled ON routes(enabled) WHERE enabled = true;
CREATE INDEX idx_routes_tenant ON routes(tenant_id);
CREATE INDEX idx_routes_path_pattern ON routes USING gin(path gin_trgm_ops);
CREATE INDEX idx_routes_created_at ON routes(created_at DESC);
CREATE INDEX idx_routes_order ON routes(order_number);

-- Route history table (for audit)
CREATE TABLE route_history (
    id BIGSERIAL PRIMARY KEY,
    route_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL, -- CREATED, UPDATED, DELETED
    old_data JSONB,
    new_data JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT NOW()
);

-- Route statistics table
CREATE TABLE route_statistics (
    route_id VARCHAR(50) PRIMARY KEY,
    request_count BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    avg_response_time DECIMAL(10, 2),
    last_request_at TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES routes(id)
);
```

#### 2. **Schema Relationships**

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema Relationships                  │
└─────────────────────────────────────────────────────────┘

routes (1) ──┐
             │
             ├── (1:N) route_history
             │
             └── (1:1) route_statistics
```

#### 3. **Entity Relationships**

```java
@Entity
@Table(name = "routes")
public class RouteEntity {
    @Id
    private String id;
    
    // ... other fields
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<RouteHistoryEntity> history;
    
    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL)
    private RouteStatisticsEntity statistics;
}

@Entity
@Table(name = "route_history")
public class RouteHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "route_id")
    private RouteEntity route;
    
    private String action;
    
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> oldData;
    
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> newData;
}
```

---

## Question 14: How did you handle route configuration changes?

### Answer

### Route Configuration Change Handling

#### 1. **Change Handling Process**

```
┌─────────────────────────────────────────────────────────┐
│         Route Configuration Change Process            │
└─────────────────────────────────────────────────────────┘

1. Change Request
   ├─ Admin API receives change
   ├─ Validate change
   └─ Check permissions

2. Database Update
   ├─ Begin transaction
   ├─ Update route in database
   ├─ Create history record
   └─ Commit transaction

3. Event Publication
   ├─ Publish route change event
   ├─ Send WebSocket notification
   └─ Publish to message queue

4. Gateway Update
   ├─ Gateway receives notification
   ├─ Invalidate cache
   ├─ Reload route from database
   └─ Update active routes

5. Validation
   ├─ Verify route is active
   ├─ Test route matching
   └─ Monitor for errors
```

#### 2. **Change Validation**

```java
@Service
public class RouteChangeValidator {
    public ValidationResult validateRouteChange(
            RouteEntity route, RouteChangeType changeType) {
        ValidationResult result = new ValidationResult();
        
        // Validate path format
        if (!isValidPath(route.getPath())) {
            result.addError("Invalid path format: " + route.getPath());
        }
        
        // Validate URI format
        if (!isValidUri(route.getUri())) {
            result.addError("Invalid URI format: " + route.getUri());
        }
        
        // Validate predicates
        if (!isValidPredicates(route.getPredicates())) {
            result.addError("Invalid predicates structure");
        }
        
        // Validate filters
        if (route.getFilters() != null && 
            !isValidFilters(route.getFilters())) {
            result.addError("Invalid filters structure");
        }
        
        // Check for duplicate paths
        if (changeType == RouteChangeType.CREATE && 
            isDuplicatePath(route.getPath(), route.getTenantId())) {
            result.addError("Route with same path already exists");
        }
        
        return result;
    }
    
    private boolean isValidPath(String path) {
        // Validate path pattern
        try {
            Pattern.compile(path.replace("**", ".*"));
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
```

#### 3. **Atomic Change Processing**

```java
@Service
@Transactional
public class RouteChangeService {
    private final RouteRepository routeRepository;
    private final RouteHistoryRepository historyRepository;
    private final RouteChangeEventPublisher eventPublisher;
    
    public RouteEntity updateRoute(String id, RouteUpdateRequest request) {
        // Load current route
        RouteEntity currentRoute = routeRepository.findById(id)
            .orElseThrow(() -> new RouteNotFoundException(id));
        
        // Create history snapshot
        RouteHistoryEntity history = new RouteHistoryEntity();
        history.setRoute(currentRoute);
        history.setAction("UPDATED");
        history.setOldData(convertToMap(currentRoute));
        
        // Update route
        applyUpdate(currentRoute, request);
        currentRoute.setUpdatedAt(LocalDateTime.now());
        
        // Save changes
        RouteEntity updated = routeRepository.save(currentRoute);
        history.setNewData(convertToMap(updated));
        historyRepository.save(history);
        
        // Publish event (after transaction commit)
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publishRouteUpdated(updated);
                }
            });
        
        return updated;
    }
}
```

#### 4. **Change Conflict Resolution**

```java
@Service
public class RouteConflictResolver {
    public RouteEntity resolveConflict(RouteEntity existing, 
                                      RouteEntity incoming) {
        // Check for conflicts
        if (hasPathConflict(existing, incoming)) {
            // Resolve based on priority
            if (incoming.getOrderNumber() < existing.getOrderNumber()) {
                // Incoming route has higher priority
                return incoming;
            } else {
                // Keep existing route
                throw new RouteConflictException(
                    "Route conflict: path already exists");
            }
        }
        
        return incoming;
    }
    
    private boolean hasPathConflict(RouteEntity existing, 
                                   RouteEntity incoming) {
        // Check if paths overlap
        return pathsOverlap(existing.getPath(), incoming.getPath()) &&
               sameTenant(existing.getTenantId(), incoming.getTenantId());
    }
}
```

---

## Question 15: What mechanisms did you use to notify the gateway of route changes?

### Answer

### Route Change Notification Mechanisms

#### 1. **Multi-Channel Notification**

```
┌─────────────────────────────────────────────────────────┐
│         Route Change Notification Architecture         │
└─────────────────────────────────────────────────────────┘

Route Change Event
    │
    ├──▶ WebSocket (Real-time)
    │    └── Immediate notification
    │
    ├──▶ Message Queue (Reliable)
    │    └── Guaranteed delivery
    │
    └──▶ Database Polling (Fallback)
         └── Periodic check
```

#### 2. **WebSocket Notification**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for route changes
        config.enableSimpleBroker("/topic/routes");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/routes")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}

@Component
public class RouteChangeWebSocketPublisher {
    private final SimpMessagingTemplate messagingTemplate;
    
    public void notifyRouteChange(RouteChangeEvent event) {
        // Broadcast to all gateway instances
        messagingTemplate.convertAndSend(
            "/topic/routes/changes", 
            event);
        
        // Notify specific tenant
        if (event.getTenantId() != null) {
            messagingTemplate.convertAndSend(
                "/topic/routes/changes/" + event.getTenantId(),
                event);
        }
    }
}

// Gateway side subscription
@Component
public class RouteChangeWebSocketListener {
    
    @EventListener
    public void handleRouteChange(RouteChangeEvent event) {
        log.info("Route change received via WebSocket: {}", event);
        
        switch (event.getAction()) {
            case "CREATED":
            case "UPDATED":
                routeLocator.refreshRoute(event.getRoute());
                break;
            case "DELETED":
                routeLocator.removeRoute(event.getRouteId());
                break;
        }
    }
}
```

#### 3. **Message Queue Notification**

```java
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue routeChangesQueue() {
        return QueueBuilder.durable("route-changes")
            .withArgument("x-message-ttl", 60000) // 1 minute TTL
            .build();
    }
    
    @Bean
    public TopicExchange routeChangesExchange() {
        return new TopicExchange("route-changes-exchange");
    }
    
    @Bean
    public Binding routeChangesBinding() {
        return BindingBuilder
            .bind(routeChangesQueue())
            .to(routeChangesExchange())
            .with("route.changes.#");
    }
}

@Component
public class RouteChangeMessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    
    public void publishRouteChange(RouteChangeEvent event) {
        rabbitTemplate.convertAndSend(
            "route-changes-exchange",
            "route.changes." + event.getAction().toLowerCase(),
            event);
    }
}

@Component
public class RouteChangeMessageListener {
    private final DynamicRouteLocator routeLocator;
    
    @RabbitListener(queues = "route-changes")
    public void handleRouteChangeMessage(RouteChangeMessage message) {
        log.info("Route change received via RabbitMQ: {}", message);
        
        RouteChangeEvent event = message.toEvent();
        routeLocator.handleRouteChange(event);
    }
}
```

#### 4. **Database Polling (Fallback)**

```java
@Component
public class RouteChangePoller {
    private final RouteRepository routeRepository;
    private final DynamicRouteLocator routeLocator;
    private final AtomicReference<LocalDateTime> lastCheck = 
        new AtomicReference<>(LocalDateTime.now());
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void pollForRouteChanges() {
        LocalDateTime lastCheckTime = lastCheck.get();
        
        // Find routes updated since last check
        List<RouteEntity> changedRoutes = routeRepository
            .findByUpdatedAtAfter(lastCheckTime);
        
        if (!changedRoutes.isEmpty()) {
            log.info("Found {} route changes via polling", 
                changedRoutes.size());
            
            for (RouteEntity route : changedRoutes) {
                routeLocator.refreshRoute(route);
            }
            
            lastCheck.set(LocalDateTime.now());
        }
    }
}
```

#### 5. **Hybrid Notification Strategy**

```java
@Component
public class RouteChangeNotifier {
    private final RouteChangeWebSocketPublisher webSocketPublisher;
    private final RouteChangeMessagePublisher messagePublisher;
    private final RouteChangePoller poller;
    
    public void notifyRouteChange(RouteChangeEvent event) {
        // Primary: WebSocket (fastest)
        try {
            webSocketPublisher.notifyRouteChange(event);
        } catch (Exception e) {
            log.warn("WebSocket notification failed, using fallback", e);
            
            // Fallback: Message queue (reliable)
            try {
                messagePublisher.publishRouteChange(event);
            } catch (Exception e2) {
                log.error("Message queue notification failed", e2);
                // Polling will pick up changes eventually
            }
        }
    }
}
```

---

## Summary

Part 3 covers questions 11-15 on Dynamic Route Management:

11. **PostgreSQL Storage**: Database schema, route repository, route service
12. **Real-Time Updates**: WebSocket notifications, cache invalidation, no restarts
13. **Database Schema**: Complete schema design, relationships, indexes
14. **Change Handling**: Validation, atomic updates, conflict resolution
15. **Notification Mechanisms**: WebSocket, message queue, polling, hybrid strategy

Key techniques:
- PostgreSQL for route persistence with JSONB for flexibility
- WebSocket for real-time notifications
- Message queue for reliable delivery
- Database polling as fallback
- Multi-channel notification strategy
