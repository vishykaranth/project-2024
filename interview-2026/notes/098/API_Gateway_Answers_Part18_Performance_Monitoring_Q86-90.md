# API Gateway Answers - Part 18: Performance & Monitoring (Questions 86-90)

## Question 86: What performance metrics did you track for the API gateway?

### Answer

### Performance Metrics

- Request count
- Request duration (latency)
- Throughput (req/sec)
- Error rate
- Active connections

---

## Question 87: How did you monitor gateway latency and throughput?

### Answer

### Latency and Throughput Monitoring

```java
@Component
public class PerformanceMonitor {
    private final Timer requestTimer;
    private final Counter requestCounter;
    
    public void recordRequest(Duration duration) {
        requestTimer.record(duration);
        requestCounter.increment();
    }
}
```

---

## Question 88: What alerting did you set up for the gateway?

### Answer

### Alerting

- High latency alerts
- High error rate alerts
- Low throughput alerts
- Health check failures

---

## Question 89: How did you identify and resolve performance issues?

### Answer

### Performance Issue Resolution

- Metrics analysis
- Profiling
- Log analysis
- Load testing

---

## Question 90: What load testing did you perform on the gateway?

### Answer

### Load Testing

- JMeter tests
- Gatling tests
- Stress testing
- Capacity planning

---

## Summary

Part 18 covers questions 86-90 on Performance & Monitoring:
- Performance metrics
- Latency and throughput monitoring
- Alerting
- Performance issue resolution
- Load testing
