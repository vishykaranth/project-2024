# Monitoring & Alerting - Part 2: Analysis & Observability

## Question 306: How do you perform root cause analysis?

### Answer

### Root Cause Analysis Process

#### 1. **RCA Methodology**

```
┌─────────────────────────────────────────────────────────┐
│         Root Cause Analysis Process                    │
└─────────────────────────────────────────────────────────┘

Step 1: Define the Problem
├─ What happened?
├─ When did it happen?
├─ What was the impact?
└─ Who was affected?

Step 2: Collect Data
├─ Logs
├─ Metrics
├─ Traces
└─ Events

Step 3: Identify Possible Causes
├─ Brainstorm causes
├─ Analyze data
├─ Test hypotheses
└─ Narrow down

Step 4: Determine Root Cause
├─ Use 5 Whys
├─ Use fishbone diagram
├─ Verify root cause
└─ Document findings

Step 5: Develop Solutions
├─ Immediate fix
├─ Long-term prevention
├─ Process improvements
└─ Documentation updates
```

#### 2. **5 Whys Technique**

```java
@Service
public class RootCauseAnalysisService {
    public RootCause perform5Whys(Incident incident) {
        RootCause rootCause = new RootCause();
        
        // Why 1: Why did the service fail?
        String why1 = "Service failed due to high memory usage";
        rootCause.addWhy(1, why1);
        
        // Why 2: Why was memory usage high?
        String why2 = "Memory leak in event processing";
        rootCause.addWhy(2, why2);
        
        // Why 3: Why was there a memory leak?
        String why3 = "Event listeners not being removed";
        rootCause.addWhy(3, why3);
        
        // Why 4: Why weren't listeners removed?
        String why4 = "No cleanup mechanism in place";
        rootCause.addWhy(4, why4);
        
        // Why 5: Why was there no cleanup mechanism?
        String why5 = "Missing code review process for resource management";
        rootCause.addWhy(5, why5);
        
        // Root cause identified
        rootCause.setRootCause(why5);
        
        return rootCause;
    }
}
```

#### 3. **Data Collection for RCA**

```java
@Service
public class RCADataCollectionService {
    public RCAData collectData(Incident incident) {
        RCAData data = new RCAData();
        
        // Collect logs
        data.setLogs(collectLogs(incident));
        
        // Collect metrics
        data.setMetrics(collectMetrics(incident));
        
        // Collect traces
        data.setTraces(collectTraces(incident));
        
        // Collect events
        data.setEvents(collectEvents(incident));
        
        // Collect configuration
        data.setConfiguration(collectConfiguration(incident));
        
        // Collect recent changes
        data.setRecentChanges(collectRecentChanges(incident));
        
        return data;
    }
    
    private List<LogEntry> collectLogs(Incident incident) {
        // Get logs around incident time
        Instant startTime = incident.getStartTime().minus(Duration.ofMinutes(10));
        Instant endTime = incident.getEndTime().plus(Duration.ofMinutes(10));
        
        return logRepository.findByTimeRangeAndService(
            startTime, 
            endTime, 
            incident.getService()
        );
    }
}
```

#### 4. **Timeline Reconstruction**

```java
@Service
public class TimelineReconstructionService {
    public Timeline reconstructTimeline(Incident incident) {
        Timeline timeline = new Timeline();
        
        // Get all events
        List<Event> events = getAllEvents(incident);
        
        // Sort by timestamp
        events.sort(Comparator.comparing(Event::getTimestamp));
        
        // Build timeline
        for (Event event : events) {
            TimelineEntry entry = TimelineEntry.builder()
                .timestamp(event.getTimestamp())
                .event(event.getType())
                .description(event.getDescription())
                .service(event.getService())
                .build();
            
            timeline.addEntry(entry);
        }
        
        return timeline;
    }
}
```

---

## Question 307: What's the post-mortem process?

### Answer

### Post-Mortem Process

#### 1. **Post-Mortem Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Post-Mortem Structure                          │
└─────────────────────────────────────────────────────────┘

1. Executive Summary
   ├─ What happened
   ├─ Impact
   └─ Resolution

2. Timeline
   ├─ Incident timeline
   ├─ Key events
   └─ Response actions

3. Root Cause Analysis
   ├─ Immediate cause
   ├─ Contributing factors
   └─ Root cause

4. Impact Assessment
   ├─ User impact
   ├─ Business impact
   └─ Technical impact

5. What Went Well
   ├─ Successful responses
   ├─ Good practices
   └─ Team coordination

6. What Went Wrong
   ├─ Failures
   ├─ Gaps
   └─ Mistakes

7. Action Items
   ├─ Immediate fixes
   ├─ Long-term improvements
   └─ Process changes

8. Lessons Learned
   ├─ Key takeaways
   ├─ Best practices
   └─ Prevention measures
```

#### 2. **Post-Mortem Template**

```java
@Service
public class PostMortemService {
    public PostMortem createPostMortem(Incident incident) {
        PostMortem postMortem = new PostMortem();
        
        // Executive Summary
        postMortem.setExecutiveSummary(createExecutiveSummary(incident));
        
        // Timeline
        postMortem.setTimeline(reconstructTimeline(incident));
        
        // Root Cause Analysis
        postMortem.setRootCauseAnalysis(performRCA(incident));
        
        // Impact Assessment
        postMortem.setImpactAssessment(assessImpact(incident));
        
        // What Went Well
        postMortem.setWhatWentWell(identifySuccesses(incident));
        
        // What Went Wrong
        postMortem.setWhatWentWrong(identifyFailures(incident));
        
        // Action Items
        postMortem.setActionItems(createActionItems(incident));
        
        // Lessons Learned
        postMortem.setLessonsLearned(extractLessons(incident));
        
        return postMortem;
    }
    
    private List<ActionItem> createActionItems(Incident incident) {
        List<ActionItem> actionItems = new ArrayList<>();
        
        // Immediate fixes
        actionItems.add(ActionItem.builder()
            .priority(ActionPriority.HIGH)
            .description("Fix memory leak in event processing")
            .owner("Engineering Team")
            .dueDate(Instant.now().plus(Duration.ofDays(7)))
            .build());
        
        // Long-term improvements
        actionItems.add(ActionItem.builder()
            .priority(ActionPriority.MEDIUM)
            .description("Implement automated memory leak detection")
            .owner("Platform Team")
            .dueDate(Instant.now().plus(Duration.ofDays(30)))
            .build());
        
        return actionItems;
    }
}
```

#### 3. **Post-Mortem Meeting**

```java
@Service
public class PostMortemMeetingService {
    public void schedulePostMortem(Incident incident) {
        // Schedule within 48 hours
        Instant meetingTime = incident.getResolvedTime()
            .plus(Duration.ofHours(48));
        
        // Invite participants
        List<Participant> participants = getParticipants(incident);
        
        // Create meeting
        Meeting meeting = Meeting.builder()
            .title("Post-Mortem: " + incident.getTitle())
            .time(meetingTime)
            .participants(participants)
            .agenda(createAgenda(incident))
            .build();
        
        scheduleMeeting(meeting);
    }
    
    private List<String> createAgenda(Incident incident) {
        return Arrays.asList(
            "Incident overview",
            "Timeline review",
            "Root cause analysis",
            "Impact assessment",
            "What went well",
            "What went wrong",
            "Action items",
            "Lessons learned"
        );
    }
}
```

---

## Question 308: How do you track system reliability (SLA, SLO, SLI)?

### Answer

### Reliability Tracking

#### 1. **SLA, SLO, SLI Definitions**

```
┌─────────────────────────────────────────────────────────┐
│         SLA, SLO, SLI                                  │
└─────────────────────────────────────────────────────────┘

SLA (Service Level Agreement):
├─ Contract with customers
├─ 99.9% uptime
├─ < 200ms P95 latency
└─ < 0.1% error rate

SLO (Service Level Objective):
├─ Internal target
├─ 99.95% uptime
├─ < 100ms P95 latency
└─ < 0.05% error rate

SLI (Service Level Indicator):
├─ Measured metric
├─ Actual uptime: 99.92%
├─ Actual P95 latency: 95ms
└─ Actual error rate: 0.03%
```

#### 2. **SLI Implementation**

```java
@Component
public class SLICalculator {
    private final MeterRegistry meterRegistry;
    
    public SLI calculateAvailability(String service) {
        // Calculate uptime
        double uptime = calculateUptime(service);
        
        // Calculate availability SLI
        SLI availability = SLI.builder()
            .name("availability")
            .value(uptime)
            .target(0.9995) // 99.95%
            .build();
        
        return availability;
    }
    
    public SLI calculateLatency(String service) {
        // Get P95 latency
        Timer timer = meterRegistry.find("http.server.requests")
            .tag("service", service)
            .timer();
        
        double p95Latency = timer.percentile(0.95, TimeUnit.MILLISECONDS);
        
        // Calculate latency SLI (requests under 200ms)
        double requestsUnderThreshold = calculateRequestsUnderThreshold(
            service, 
            200
        );
        
        SLI latency = SLI.builder()
            .name("latency")
            .value(requestsUnderThreshold)
            .target(0.95) // 95% of requests under 200ms
            .build();
        
        return latency;
    }
    
    public SLI calculateErrorRate(String service) {
        // Calculate error rate
        double errorRate = calculateErrorRate(service);
        
        // Error rate SLI (1 - error rate)
        double successRate = 1 - errorRate;
        
        SLI errorRateSLI = SLI.builder()
            .name("error_rate")
            .value(successRate)
            .target(0.9995) // 99.95% success rate
            .build();
        
        return errorRateSLI;
    }
}
```

#### 3. **SLO Tracking**

```java
@Service
public class SLOTrackingService {
    @Scheduled(fixedRate = 3600000) // Every hour
    public void trackSLOs() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Calculate SLIs
            SLI availability = calculateAvailability(service);
            SLI latency = calculateLatency(service);
            SLI errorRate = calculateErrorRate(service);
            
            // Check SLO compliance
            checkSLOCompliance(service, availability, latency, errorRate);
        }
    }
    
    private void checkSLOCompliance(Service service, SLI... slis) {
        for (SLI sli : slis) {
            if (sli.getValue() < sli.getTarget()) {
                log.warn("SLO violation: {} - {} < {}", 
                    sli.getName(), sli.getValue(), sli.getTarget());
                
                alertService.sloViolation(service, sli);
            }
        }
    }
}
```

#### 4. **SLA Reporting**

```java
@Service
public class SLAReportingService {
    public SLAReport generateSLAReport(Service service, Duration period) {
        // Calculate metrics
        double uptime = calculateUptime(service, period);
        double avgLatency = calculateAverageLatency(service, period);
        double errorRate = calculateErrorRate(service, period);
        
        // Check SLA compliance
        boolean uptimeCompliant = uptime >= 0.999; // 99.9%
        boolean latencyCompliant = avgLatency < 200; // < 200ms
        boolean errorRateCompliant = errorRate < 0.001; // < 0.1%
        
        SLAReport report = SLAReport.builder()
            .service(service.getName())
            .period(period)
            .uptime(uptime)
            .uptimeCompliant(uptimeCompliant)
            .avgLatency(avgLatency)
            .latencyCompliant(latencyCompliant)
            .errorRate(errorRate)
            .errorRateCompliant(errorRateCompliant)
            .overallCompliant(uptimeCompliant && latencyCompliant && errorRateCompliant)
            .build();
        
        return report;
    }
}
```

---

## Question 309: What's the observability stack (logging, metrics, tracing)?

### Answer

### Observability Stack

#### 1. **Three Pillars of Observability**

```
┌─────────────────────────────────────────────────────────┐
│         Observability Stack                            │
└─────────────────────────────────────────────────────────┘

Metrics (Prometheus + Grafana):
├─ System metrics
├─ Application metrics
├─ Business metrics
└─ Custom metrics

Logging (ELK Stack):
├─ Application logs
├─ Access logs
├─ Error logs
└─ Audit logs

Tracing (Jaeger/Zipkin):
├─ Distributed tracing
├─ Request flow
├─ Performance analysis
└─ Dependency mapping
```

#### 2. **Metrics Stack**

```yaml
# Prometheus configuration
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'agent-match-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['agent-match-service:8080']
        
  - job_name: 'nlu-facade-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['nlu-facade-service:8080']
```

```java
@Configuration
public class MetricsConfiguration {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCustomizer() {
        return registry -> {
            registry.config().commonTags("application", "conversational-ai");
        };
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

#### 3. **Logging Stack**

```yaml
# Logback configuration
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

```java
@RestController
public class LoggingController {
    private static final Logger logger = LoggerFactory.getLogger(LoggingController.class);
    
    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(@RequestBody ConversationRequest request) {
        // Structured logging
        MDC.put("requestId", request.getId());
        MDC.put("tenantId", request.getTenantId());
        
        logger.info("Creating conversation: {}", request);
        
        try {
            Conversation conversation = conversationService.create(request);
            logger.info("Conversation created: {}", conversation.getId());
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            logger.error("Failed to create conversation", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
```

#### 4. **Tracing Stack**

```java
@Configuration
public class TracingConfiguration {
    @Bean
    public Tracer tracer() {
        return new BraveTracer(
            Tracing.newBuilder()
                .localServiceName("conversational-ai")
                .sampler(Sampler.create(0.1)) // Sample 10% of requests
                .build()
                .tracer()
        );
    }
}

@RestController
public class TracedController {
    private final Tracer tracer;
    
    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable String id) {
        Span span = tracer.nextSpan()
            .name("get-conversation")
            .tag("conversation.id", id)
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            Conversation conversation = conversationService.findById(id);
            return ResponseEntity.ok(conversation);
        } finally {
            span.end();
        }
    }
}
```

#### 5. **Observability Dashboard**

```
┌─────────────────────────────────────────────────────────┐
│         Observability Dashboard                        │
└─────────────────────────────────────────────────────────┘

Metrics Dashboard (Grafana):
├─ System health
├─ Application performance
├─ Business metrics
└─ SLO compliance

Logs Dashboard (Kibana):
├─ Error logs
├─ Access logs
├─ Application logs
└─ Search and filter

Traces Dashboard (Jaeger):
├─ Request traces
├─ Service dependencies
├─ Performance analysis
└─ Error traces
```

---

## Question 310: How do you handle distributed tracing?

### Answer

### Distributed Tracing Implementation

#### 1. **Tracing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Tracing Flow                       │
└─────────────────────────────────────────────────────────┘

Request Flow:
1. API Gateway
   ├─ Creates trace
   ├─ Generates trace ID
   └─ Propagates to services

2. Service A
   ├─ Receives trace ID
   ├─ Creates span
   ├─ Calls Service B
   └─ Passes trace ID

3. Service B
   ├─ Receives trace ID
   ├─ Creates child span
   └─ Completes operation

4. Trace Collection
   ├─ All spans collected
   ├─ Reconstructed into trace
   └─ Visualized in UI
```

#### 2. **Trace Context Propagation**

```java
@Component
public class TraceContextPropagator {
    private final Tracer tracer;
    
    public <T> T propagateTrace(Supplier<T> operation) {
        Span currentSpan = tracer.currentSpan();
        
        if (currentSpan != null) {
            // Extract trace context
            TraceContext context = extractTraceContext(currentSpan);
            
            // Propagate to next service
            return propagateAndExecute(context, operation);
        }
        
        // Create new trace
        return createNewTrace(operation);
    }
    
    private TraceContext extractTraceContext(Span span) {
        return TraceContext.builder()
            .traceId(span.context().traceId())
            .spanId(span.context().spanId())
            .parentSpanId(span.context().parentId())
            .build();
    }
}
```

#### 3. **HTTP Trace Propagation**

```java
@Configuration
public class TracingConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add tracing interceptor
        restTemplate.getInterceptors().add(new TracingInterceptor(tracer));
        
        return restTemplate;
    }
}

@Component
public class TracingInterceptor implements ClientHttpRequestInterceptor {
    private final Tracer tracer;
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        
        Span span = tracer.nextSpan()
            .name("http-request")
            .tag("http.method", request.getMethod().name())
            .tag("http.url", request.getURI().toString())
            .start();
        
        // Inject trace headers
        injectTraceHeaders(request, span);
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            ClientHttpResponse response = execution.execute(request, body);
            span.tag("http.status_code", String.valueOf(response.getStatusCode().value()));
            return response;
        } catch (Exception e) {
            span.tag("error", true);
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
    
    private void injectTraceHeaders(HttpRequest request, Span span) {
        // Inject B3 headers
        request.getHeaders().add("X-B3-TraceId", span.context().traceId());
        request.getHeaders().add("X-B3-SpanId", span.context().spanId());
        request.getHeaders().add("X-B3-ParentSpanId", span.context().parentId());
    }
}
```

#### 4. **Kafka Trace Propagation**

```java
@Component
public class KafkaTracingInterceptor {
    private final Tracer tracer;
    
    public void addTracingHeaders(ProducerRecord<String, Event> record) {
        Span span = tracer.currentSpan();
        
        if (span != null) {
            // Inject trace headers
            record.headers().add("X-B3-TraceId", span.context().traceId().getBytes());
            record.headers().add("X-B3-SpanId", span.context().spanId().getBytes());
        }
    }
    
    @KafkaListener(topics = "events")
    public void handleEvent(ConsumerRecord<String, Event> record) {
        // Extract trace context
        TraceContext context = extractTraceContext(record.headers());
        
        // Create span
        Span span = tracer.nextSpan()
            .name("kafka-consume")
            .tag("kafka.topic", record.topic())
            .tag("kafka.partition", String.valueOf(record.partition()))
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Process event
            processEvent(record.value());
        } finally {
            span.end();
        }
    }
}
```

#### 5. **Trace Analysis**

```java
@Service
public class TraceAnalysisService {
    public void analyzeTrace(String traceId) {
        // Get trace
        Trace trace = getTrace(traceId);
        
        // Analyze spans
        List<Span> spans = trace.getSpans();
        
        // Find slowest spans
        spans.stream()
            .sorted(Comparator.comparing(Span::getDuration).reversed())
            .limit(5)
            .forEach(span -> 
                log.info("Slow span: {} took {}ms", 
                    span.getName(), span.getDuration().toMillis())
            );
        
        // Find errors
        spans.stream()
            .filter(Span::hasError)
            .forEach(span -> 
                log.error("Error span: {} - {}", 
                    span.getName(), span.getError())
            );
        
        // Analyze service dependencies
        Map<String, Integer> dependencies = analyzeDependencies(spans);
        log.info("Service dependencies: {}", dependencies);
    }
}
```

---

## Summary

Part 2 covers monitoring and alerting for:

1. **Root Cause Analysis**: Methodology, 5 Whys, data collection, timeline reconstruction
2. **Post-Mortem Process**: Structure, template, meeting, action items
3. **SLA/SLO/SLI Tracking**: Definitions, implementation, tracking, reporting
4. **Observability Stack**: Metrics, logging, tracing, dashboards
5. **Distributed Tracing**: Architecture, propagation, HTTP/Kafka tracing, analysis

Key principles:
- Use systematic RCA methodology
- Conduct thorough post-mortems
- Track SLA/SLO/SLI continuously
- Implement comprehensive observability
- Use distributed tracing for debugging
