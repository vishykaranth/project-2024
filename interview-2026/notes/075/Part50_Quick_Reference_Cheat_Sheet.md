# Part 50: Quick Reference Cheat Sheet - Quick Revision

## System Design Checklist

- [ ] Clarify requirements (functional, non-functional, scale)
- [ ] Estimate capacity (storage, bandwidth, servers)
- [ ] Design high-level architecture
- [ ] Choose database (SQL vs NoSQL)
- [ ] Design caching strategy
- [ ] Plan load balancing
- [ ] Consider scalability (horizontal vs vertical)
- [ ] Plan for failures (redundancy, failover)
- [ ] Design security (auth, encryption)
- [ ] Plan monitoring and observability

## CAP Theorem Quick Reference

- **CP**: Consistency + Partition Tolerance (PostgreSQL, MongoDB)
- **AP**: Availability + Partition Tolerance (Cassandra, DynamoDB)
- **CA**: Consistency + Availability (single-node only)

## GC Algorithms Quick Reference

- **Parallel GC**: Throughput-focused, batch processing
- **G1 GC**: Balanced latency/throughput, low-latency apps
- **ZGC**: Ultra-low latency (<10ms), large heaps
- **Shenandoah**: Concurrent evacuation, large heaps

## Common Time Complexities

- **O(1)**: Hash lookup, array access
- **O(log n)**: Binary search, balanced tree
- **O(n)**: Linear search, iterate array
- **O(n log n)**: Merge sort, heap sort
- **O(n²)**: Nested loops, bubble sort
- **O(2ⁿ)**: Recursive Fibonacci, subsets

## Key JVM Parameters

- **-Xms / -Xmx**: Heap size
- **-XX:+UseG1GC**: Enable G1 collector
- **-XX:MaxGCPauseMillis**: Target pause time
- **-Xlog:gc**: GC logging

## Spring Annotations

- **@Component, @Service, @Repository**: Bean definitions
- **@Autowired**: Dependency injection
- **@Transactional**: Transaction management
- **@RestController**: REST controller
- **@RequestMapping**: URL mapping

## Database Scaling

- **Read Replicas**: Scale reads, eventual consistency
- **Sharding**: Partition data, horizontal scaling
- **Caching**: Reduce database load, faster reads

## Load Balancing Algorithms

- **Round Robin**: Sequential distribution
- **Least Connections**: Fewest active connections
- **IP Hash**: Sticky sessions
- **Weighted**: Based on server capacity

## Cache Patterns

- **Cache-Aside**: App manages cache
- **Write-Through**: Write to cache and DB
- **Write-Back**: Write to cache, async to DB
- **Eviction**: LRU, LFU, FIFO, TTL

## Interview Tips

- **Think out loud**: Show your process
- **Ask questions**: Clarify requirements
- **Start high-level**: Then dive deep
- **Discuss trade-offs**: Show understanding
- **Iterate**: Improve your design

---

**Remember**: These are quick revision notes. Refer to detailed materials for in-depth understanding. Good luck with your Principal Engineer interview preparation! 🚀
