# System Design Interview Questions for Java Principal Engineers - Part 8

## Monitoring, Logging, and Observability

This part covers monitoring strategies, logging architectures, metrics collection, and observability patterns.

---

## Interview Question 36: Design a Distributed Logging System

### Requirements

- Centralized log aggregation
- Real-time log search
- Log retention policies
- High throughput

### ELK Stack Integration

```java
@Configuration
public class LoggingConfig {
    
    @Bean
    public LoggerContext loggerContext() {
        LoggerContext context = new LoggerContext();
        
        // Logstash encoder for JSON logs
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setIncludeContext(true);
        encoder.setIncludeMdc(true);
        
        // TCP appender for Logstash
        TcpSocketAppender appender = new TcpSocketAppender();
        appender.setContext(context);
        appender.setName("LOGSTASH");
        appender.setRemoteHost("logstash-host");
        appender.setPort(5000);
        appender.setEncoder(encoder);
        appender.start();
        
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
        
        return context;
    }
}

@Aspect
@Component
public class RequestLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingAspect.class);
    
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) 
            RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        
        MDC.put("traceId", traceId);
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", getCurrentUserId());
        MDC.put("endpoint", request.getRequestURI());
        MDC.put("method", request.getMethod());
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Request completed", 
                kv("duration", duration),
                kv("status", "success"));
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            logger.error("Request failed", 
                kv("duration", duration),
                kv("status", "error"),
                kv("error", e.getMessage()),
                e);
            
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
```

### Custom Log Aggregation

```java
@Service
public class LogAggregationService {
    @Autowired
    private KafkaTemplate<String, LogEvent> kafkaTemplate;
    
    public void sendLog(LogLevel level, String message, Map<String, Object> context) {
        LogEvent logEvent = new LogEvent();
        logEvent.setLevel(level);
        logEvent.setMessage(message);
        logEvent.setTimestamp(Instant.now());
        logEvent.setServiceName(getServiceName());
        logEvent.setTraceId(MDC.get("traceId"));
        logEvent.setContext(context);
        
        kafkaTemplate.send("logs", logEvent);
    }
}

@Component
public class LogConsumer {
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    @KafkaListener(topics = "logs", groupId = "log-aggregator")
    public void consumeLog(LogEvent logEvent) {
        // Index in Elasticsearch
        IndexRequest request = IndexRequest.of(i -> i
            .index("logs-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            .document(logEvent)
        );
        
        elasticsearchClient.index(request);
    }
}
```

---

## Interview Question 37: Design a Metrics Collection System

### Requirements

- Collect application metrics
- Time-series storage
- Real-time dashboards
- Alerting

### Micrometer Integration

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

@Service
public class OrderService {
    @Autowired
    private MeterRegistry meterRegistry;
    
    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;
    private final Gauge activeOrdersGauge;
    
    public OrderService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderCreatedCounter = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(meterRegistry);
        
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("Order processing time")
            .register(meterRegistry);
        
        this.activeOrdersGauge = Gauge.builder("orders.active", 
            () -> getActiveOrderCount())
            .description("Active orders count")
            .register(meterRegistry);
    }
    
    @Timed(value = "orders.create", description = "Time to create order")
    public Order createOrder(OrderRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Order order = processOrder(request);
            orderCreatedCounter.increment(
                Tags.of("status", "success", "type", request.getType())
            );
            return order;
        } catch (Exception e) {
            orderCreatedCounter.increment(
                Tags.of("status", "error", "type", request.getType())
            );
            throw e;
        } finally {
            sample.stop(orderProcessingTimer);
        }
    }
}
```

### Custom Metrics Collection

```java
@Component
public class CustomMetricsCollector {
    @Autowired
    private MeterRegistry meterRegistry;
    
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> operationTimers = new ConcurrentHashMap<>();
    
    public void recordError(String operation, String errorType) {
        Counter counter = errorCounters.computeIfAbsent(
            operation + ":" + errorType,
            key -> Counter.builder("errors")
                .tag("operation", operation)
                .tag("type", errorType)
                .register(meterRegistry)
        );
        counter.increment();
    }
    
    public Timer.Sample startOperation(String operation) {
        Timer timer = operationTimers.computeIfAbsent(
            operation,
            key -> Timer.builder("operations")
                .tag("operation", operation)
                .register(meterRegistry)
        );
        return Timer.start(meterRegistry);
    }
}
```

---

## Interview Question 38: Design a Distributed Tracing System

### Requirements

- Trace requests across services
- Performance analysis
- Dependency mapping
- Error tracking

### OpenTelemetry Integration

```java
@Configuration
public class TracingConfig {
    
    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(
                        OtlpGrpcSpanExporter.builder()
                            .setEndpoint("http://jaeger:4317")
                            .build()
                    ).build())
                    .setResource(Resource.getDefault())
                    .build()
            )
            .build();
    }
    
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("order-service");
    }
}

@Service
public class OrderService {
    @Autowired
    private Tracer tracer;
    
    public Order createOrder(OrderRequest request) {
        Span span = tracer.spanBuilder("create-order")
            .setAttribute("order.type", request.getType())
            .setAttribute("order.amount", request.getAmount())
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Business logic
            Order order = processOrder(request);
            
            span.setAttribute("order.id", order.getId());
            span.setStatus(StatusCode.OK);
            
            return order;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### Custom Tracing Implementation

```java
@Component
public class TracingInterceptor implements HandlerInterceptor {
    @Autowired
    private Tracer tracer;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        
        Span span = tracer.nextSpan()
            .name(request.getRequestURI())
            .tag("http.method", request.getMethod())
            .tag("http.url", request.getRequestURI())
            .start();
        
        request.setAttribute("span", span);
        request.setAttribute("traceId", traceId);
        
        MDC.put("traceId", traceId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        Span span = (Span) request.getAttribute("span");
        if (span != null) {
            span.tag("http.status_code", String.valueOf(response.getStatus()));
            if (ex != null) {
                span.error(ex);
            }
            span.end();
        }
        MDC.clear();
    }
}
```

---

## Interview Question 39: Design an Alerting System

### Requirements

- Real-time alerts
- Multiple notification channels
- Alert aggregation
- Alert routing

### Alerting Service

```java
@Service
public class AlertingService {
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AlertRuleEngine ruleEngine;
    
    public void evaluateMetrics(MetricData metric) {
        List<AlertRule> rules = ruleEngine.getApplicableRules(metric);
        
        for (AlertRule rule : rules) {
            if (rule.matches(metric)) {
                createAlert(rule, metric);
            }
        }
    }
    
    private void createAlert(AlertRule rule, MetricData metric) {
        // Check if alert already exists
        Alert existingAlert = alertRepository.findActiveAlert(rule.getId(), metric.getService());
        
        if (existingAlert != null) {
            // Update existing alert
            existingAlert.incrementCount();
            existingAlert.setLastTriggered(Instant.now());
            alertRepository.save(existingAlert);
        } else {
            // Create new alert
            Alert alert = new Alert();
            alert.setRuleId(rule.getId());
            alert.setService(metric.getService());
            alert.setSeverity(rule.getSeverity());
            alert.setMessage(rule.getMessage(metric));
            alert.setTriggeredAt(Instant.now());
            alert.setStatus(AlertStatus.ACTIVE);
            
            alertRepository.save(alert);
            
            // Send notification
            notificationService.sendAlert(alert);
        }
    }
}

@Service
public class NotificationService {
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private SlackService slackService;
    
    @Autowired
    private PagerDutyService pagerDutyService;
    
    public void sendAlert(Alert alert) {
        List<NotificationChannel> channels = getNotificationChannels(alert.getSeverity());
        
        for (NotificationChannel channel : channels) {
            switch (channel) {
                case EMAIL:
                    emailService.sendAlert(alert);
                    break;
                case SMS:
                    if (alert.getSeverity() == AlertSeverity.CRITICAL) {
                        smsService.sendAlert(alert);
                    }
                    break;
                case SLACK:
                    slackService.sendAlert(alert);
                    break;
                case PAGERDUTY:
                    if (alert.getSeverity() == AlertSeverity.CRITICAL) {
                        pagerDutyService.triggerIncident(alert);
                    }
                    break;
            }
        }
    }
}
```

---

## Interview Question 40: Design a Performance Monitoring System

### Requirements

- Application performance monitoring (APM)
- Database query monitoring
- JVM metrics
- Response time tracking

### APM Implementation

```java
@Component
public class PerformanceMonitor {
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private Tracer tracer;
    
    public <T> T monitor(String operation, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Span span = tracer.nextSpan().name(operation).start();
        
        try (Scope scope = tracer.withSpanInScope(span)) {
            T result = supplier.get();
            sample.stop(Timer.builder(operation + ".duration")
                .register(meterRegistry));
            span.end();
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder(operation + ".duration")
                .tag("status", "error")
                .register(meterRegistry));
            span.error(e);
            span.end();
            throw e;
        }
    }
}

@Aspect
@Component
public class DatabaseMonitoringAspect {
    @Autowired
    private PerformanceMonitor performanceMonitor;
    
    @Around("execution(* org.springframework.data.repository.*.*(..))")
    public Object monitorDatabase(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        
        return performanceMonitor.monitor("database." + method, () -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}

@Component
public class JVMMetricsCollector {
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 5000)
    public void collectJVMMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        Gauge.builder("jvm.memory.heap.used", heapUsage::getUsed)
            .register(meterRegistry);
        Gauge.builder("jvm.memory.heap.max", heapUsage::getMax)
            .register(meterRegistry);
        
        // GC metrics
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory
            .getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            Counter.builder("jvm.gc.collections")
                .tag("gc", gcBean.getName())
                .register(meterRegistry)
                .increment(gcBean.getCollectionCount());
        }
    }
}
```

---

## Summary: Part 8

### Key Topics Covered:
1. ✅ Distributed logging (ELK, Kafka)
2. ✅ Metrics collection (Micrometer, Prometheus)
3. ✅ Distributed tracing (OpenTelemetry, Zipkin)
4. ✅ Alerting systems
5. ✅ Performance monitoring (APM)

### Observability Pillars:
- **Logs**: What happened
- **Metrics**: How much, how often
- **Traces**: Request flow across services

---

**Next**: Part 9 will cover Real-World System Design Problems (Part 1).

