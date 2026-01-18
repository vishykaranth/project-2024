# Questions Answered by Senior Software Architects at Software Architects Meetup Bangalore

## Overview

This document covers common questions and answers from senior software architects, providing insights into real-world system design challenges and best practices.

## Common Questions

### 1. How do you handle scale?

```
Answer: Start simple, scale when needed
├─ Begin with monolith
├─ Identify bottlenecks
├─ Scale horizontally
└─ Use caching and CDN
```

### 2. How do you ensure availability?

```
Answer: Multiple strategies
├─ Redundancy (multiple instances)
├─ Health checks and auto-recovery
├─ Circuit breakers
└─ Graceful degradation
```

### 3. How do you handle data consistency?

```
Answer: Choose based on requirements
├─ Strong consistency when needed (financial)
├─ Eventual consistency when acceptable (social media)
└─ CAP theorem trade-offs
```

### 4. How do you design for failure?

```
Answer: Assume everything fails
├─ Design for partial failures
├─ Implement retries
├─ Use timeouts
└─ Fail fast
```

### 5. How do you monitor systems?

```
Answer: Comprehensive observability
├─ Metrics (Prometheus)
├─ Logging (ELK)
├─ Tracing (distributed)
└─ Alerting
```

## Key Takeaways

- Start simple, optimize later
- Design for failure
- Monitor everything
- Choose right trade-offs
- Learn from production

## Summary

Architect Insights:
- **Scale**: Start simple, scale horizontally
- **Availability**: Redundancy and health checks
- **Consistency**: Choose based on requirements
- **Failure**: Design for it
- **Monitoring**: Comprehensive observability
