# Technical Architecture Answers - Part 16-21: Technology Stack (Spring, Kafka, Cloud, Databases)

This consolidated part covers Questions 76-110 focusing on technology-specific implementation details.

---

## Questions 76-90: Kafka Operations & Spring Framework

### Q76-80: Kafka Advanced Operations

**Q76: How do you ensure exactly-once processing with Kafka?**

```java
// Kafka exactly-once configuration
@Configuration
public class KafkaExactlyOnceConfig {
    @Bean
    public ProducerFactory<String, Event> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Exactly-once configuration
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        
        return new DefaultKafkaProducerFactory<>(props);
    }
    
    @Bean
    public ConsumerFactory<String, Event> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Exactly-once configuration
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}

// Transactional producer
@Service
public class TransactionalKafkaService {
    @Autowired
    private KafkaTemplate<String, Event> kafkaTemplate;
    
    @Transactional
    public void processAndPublish(Event event) {
        // Process event
        processEvent(event);
        
        // Publish to Kafka (transactional)
        kafkaTemplate.send("output-topic", event.getId(), event);
        
        // Either both succeed or both fail
    }
}
```

**Q77-80: Schema Evolution, Monitoring, and Recovery**

```java
// Schema Registry integration
@Configuration
public class SchemaRegistryConfig {
    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(
            "http://schema-registry:8081",
            100
        );
    }
}

// Kafka monitoring
@Component
public class KafkaMonitor {
    @Scheduled(fixedRate = 60000)
    public void monitorConsumerLag() {
        AdminClient adminClient = AdminClient.create(adminProps);
        
        Map<TopicPartition, OffsetAndMetadata> offsets = 
            adminClient.listConsumerGroupOffsets("my-group")
                .partitionsToOffsetAndMetadata()
                .get();
        
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
            long lag = calculateLag(entry.getKey(), entry.getValue());
            meterRegistry.gauge("kafka.consumer.lag", lag);
            
            if (lag > 10000) {
                alertService.sendAlert("High consumer lag: " + lag);
            }
        }
    }
}

// Kafka failure recovery
@Service
public class KafkaRecoveryService {
    public void recoverFromFailure(String topic, int partition, long offset) {
        KafkaConsumer<String, Event> consumer = createConsumer();
        
        // Seek to failed offset
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        consumer.assign(Collections.singletonList(topicPartition));
        consumer.seek(topicPartition, offset);
        
        // Replay events
        while (true) {
            ConsumerRecords<String, Event> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) break;
            
            for (ConsumerRecord<String, Event> record : records) {
                try {
                    processEvent(record.value());
                } catch (Exception e) {
                    log.error("Recovery failed for offset: {}", record.offset(), e);
                }
            }
        }
    }
}
```

### Q81-90: Spring Framework

**Q81: You've extensively used Spring Boot. What are the key features you leverage?**

```java
// 1. Auto-configuration
@SpringBootApplication
public class Application {
    // Auto-configures DataSource, JPA, Web, etc.
}

// 2. Dependency Injection
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    
    @Autowired // Constructor injection (recommended)
    public OrderService(OrderRepository orderRepository,
                       PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}

// 3. Configuration Management
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String apiKey;
    private int maxRetries;
    // Getters and setters
}

// 4. Actuator for monitoring
@RestController
public class HealthController {
    @GetMapping("/actuator/health")
    public Health health() {
        return Health.up()
            .withDetail("database", "available")
            .withDetail("kafka", "available")
            .build();
    }
}

// 5. Spring Data JPA
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomerId(String customerId);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt > :date")
    List<Order> findRecentOrders(@Param("status") OrderStatus status,
                                 @Param("date") LocalDateTime date);
}

// 6. Transaction Management
@Service
public class OrderService {
    @Transactional
    public Order createOrder(OrderRequest request) {
        // All operations in single transaction
        Order order = saveOrder(request);
        updateInventory(request);
        return order;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditLog(String action) {
        // New transaction (independent of parent)
    }
}

// 7. Spring Security
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/api/**").authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt();
    }
}

// 8. Spring Cloud
@Configuration
@EnableDiscoveryClient
public class CloudConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// 9. Caching
@Service
public class ProductService {
    @Cacheable(value = "products", key = "#productId")
    public Product getProduct(String productId) {
        return productRepository.findById(productId);
    }
    
    @CacheEvict(value = "products", key = "#product.id")
    public void updateProduct(Product product) {
        productRepository.save(product);
    }
}

// 10. Async Processing
@Service
public class AsyncService {
    @Async
    public CompletableFuture<Result> asyncOperation() {
        // Runs in separate thread
        Result result = performLongOperation();
        return CompletableFuture.completedFuture(result);
    }
}
```

**Q82: How do you structure Spring Boot applications?**

```
┌─────────────────────────────────────────────────────────┐
│         Spring Boot Application Structure              │
└─────────────────────────────────────────────────────────┘

src/main/java/com/example/app/
├── Application.java (Main class)
├── config/
│   ├── DatabaseConfig.java
│   ├── KafkaConfig.java
│   └── SecurityConfig.java
├── controller/ (API layer)
│   ├── OrderController.java
│   └── ProductController.java
├── service/ (Business logic)
│   ├── OrderService.java
│   └── ProductService.java
├── repository/ (Data access)
│   ├── OrderRepository.java
│   └── ProductRepository.java
├── domain/ (Domain models)
│   ├── Order.java
│   └── Product.java
├── dto/ (Data transfer objects)
│   ├── OrderRequest.java
│   └── OrderResponse.java
├── exception/
│   ├── OrderNotFoundException.java
│   └── GlobalExceptionHandler.java
└── util/
    └── DateUtils.java
```

---

## Questions 91-100: Cloud & Kubernetes

### Q91-95: AWS, GCP, and Cloud Design

**Q91: You've worked with AWS and GCP. What services have you used?**

```
┌─────────────────────────────────────────────────────────┐
│         AWS Services Used                              │
└─────────────────────────────────────────────────────────┘

Compute:
├─ EC2 (Virtual machines)
├─ ECS (Container orchestration)
├─ EKS (Kubernetes)
└─ Lambda (Serverless functions)

Storage:
├─ S3 (Object storage)
├─ EBS (Block storage)
└─ EFS (File storage)

Database:
├─ RDS (PostgreSQL, MySQL)
├─ DynamoDB (NoSQL)
└─ ElastiCache (Redis, Memcached)

Networking:
├─ VPC (Virtual network)
├─ ELB (Load balancer)
├─ Route 53 (DNS)
└─ CloudFront (CDN)

Monitoring:
├─ CloudWatch (Metrics, logs)
├─ X-Ray (Distributed tracing)
└─ CloudTrail (Audit logs)

┌─────────────────────────────────────────────────────────┐
│         GCP Services Used                              │
└─────────────────────────────────────────────────────────┘

Compute:
├─ GKE (Kubernetes)
├─ Cloud Functions (Serverless)
└─ Compute Engine (VMs)

Storage:
├─ Cloud Storage (Object storage)
└─ Persistent Disk

Database:
├─ Cloud SQL (PostgreSQL)
└─ Cloud Spanner (Globally distributed)

Monitoring:
└─ Cloud Monitoring (Stackdriver)
```

**Q93: You mention Kubernetes. How do you design for Kubernetes?**

```yaml
# Deployment configuration
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  labels:
    app: order-service
spec:
  replicas: 10
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: order-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5

---
# Service
apiVersion: v1
kind: Service
metadata:
  name: order-service
spec:
  selector:
    app: order-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

---
# Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 5
  maxReplicas: 50
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80

---
# ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: order-service-config
data:
  application.properties: |
    server.port=8080
    spring.datasource.url=jdbc:postgresql://postgres:5432/orders
    kafka.bootstrap-servers=kafka:9092

---
# Secret (Sealed Secret in production)
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
data:
  url: <base64-encoded-url>
  username: <base64-encoded-username>
  password: <base64-encoded-password>
```

**Q95-99: Secrets Management, Multi-Region, IaC**

```hcl
# Terraform for Infrastructure as Code
# AWS Infrastructure
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  region = "us-west-2"
}

# EKS Cluster
resource "aws_eks_cluster" "main" {
  name     = "order-processing-cluster"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.27"

  vpc_config {
    subnet_ids = aws_subnet.private[*].id
  }
}

# RDS Database
resource "aws_db_instance" "orders" {
  identifier           = "orders-db"
  engine              = "postgres"
  engine_version      = "14.7"
  instance_class      = "db.r5.xlarge"
  allocated_storage   = 100
  storage_encrypted   = true
  
  db_name  = "orders"
  username = var.db_username
  password = var.db_password
  
  multi_az = true
  
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
}

# ElastiCache Redis
resource "aws_elasticache_cluster" "redis" {
  cluster_id           = "order-cache"
  engine              = "redis"
  node_type           = "cache.r5.large"
  num_cache_nodes     = 3
  parameter_group_name = "default.redis7"
}

# S3 Bucket
resource "aws_s3_bucket" "documents" {
  bucket = "order-documents"
  
  versioning {
    enabled = true
  }
  
  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }
}
```

---

## Questions 101-110: Databases

### Q101: You've worked with PostgreSQL, MongoDB, Redis, Cassandra. When do you use each?

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection Guide                       │
└─────────────────────────────────────────────────────────┘

PostgreSQL:
├─ Use Case: Transactional data, complex queries
├─ Example: Orders, Customers, Financial transactions
├─ Pros: ACID, relationships, complex queries
└─ Cons: Scaling writes

MongoDB:
├─ Use Case: Document storage, flexible schema
├─ Example: Product catalog, user profiles
├─ Pros: Flexible schema, horizontal scaling
└─ Cons: No transactions (older versions)

Redis:
├─ Use Case: Caching, session storage, real-time
├─ Example: Session cache, leaderboards, rate limiting
├─ Pros: Very fast, in-memory
└─ Cons: Limited data size, no durability (cache mode)

Cassandra:
├─ Use Case: Time-series, high write throughput
├─ Example: Event logs, metrics, IoT data
├─ Pros: Write scalability, multi-datacenter
└─ Cons: No joins, eventual consistency

Elasticsearch:
├─ Use Case: Full-text search, analytics
├─ Example: Product search, log analysis
├─ Pros: Search capabilities, analytics
└─ Cons: Not for primary storage
```

**Q102-110: Database Design & Optimization**

```java
// Database schema design for microservices
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;
    
    @Column(name = "customer_id", nullable = false)
    @Index
    private String customerId;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "created_at", nullable = false)
    @Index
    private Instant createdAt;
    
    // Avoid FK to other services
    // Store denormalized data if needed
}

// Database migration with Flyway
-- V1__Create_orders_table.sql
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- V2__Add_order_items_table.sql
CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

// Database connection pooling
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        
        // Connection pool settings
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}

// Query optimization
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    // Efficient query with join fetch
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.customerId = :customerId")
    List<Order> findOrdersWithItems(@Param("customerId") String customerId);
    
    // Pagination for large datasets
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    // Native query for complex operations
    @Query(value = "SELECT * FROM orders WHERE created_at > :date AND status = :status",
           nativeQuery = true)
    List<Order> findRecentOrders(@Param("date") Instant date, 
                                 @Param("status") String status);
}
```

---

## Summary

Parts 16-21 covered Questions 76-110:
- **Kafka Operations**: Exactly-once semantics, schema evolution, monitoring, recovery
- **Spring Framework**: Key features, application structure, configuration
- **Cloud & Kubernetes**: AWS/GCP services, K8s design, auto-scaling
- **Infrastructure as Code**: Terraform, Helm
- **Databases**: Selection criteria, schema design, optimization, connection pooling

Key technologies demonstrated:
- Spring Boot ecosystem
- Kafka advanced features
- Kubernetes orchestration
- Multi-cloud (AWS, GCP)
- Database optimization
