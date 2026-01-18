# Part 42: Performance Optimization - Quick Revision

## JVM Tuning

- **Heap Size**: -Xms, -Xmx, balance between too small (GC overhead) and too large (long pauses)
- **GC Algorithm**: Choose based on latency vs throughput requirements
- **GC Tuning**: -XX:MaxGCPauseMillis, -XX:NewRatio, monitor GC logs
- **JIT Compilation**: -XX:+TieredCompilation, warm-up period

## Code Optimization

- **Reduce Allocations**: Reuse objects, object pooling, avoid unnecessary object creation
- **String Optimization**: Use StringBuilder for concatenation, avoid string operations in loops
- **Collection Optimization**: Choose right collection, pre-size collections, avoid unnecessary iterations
- **Algorithm Optimization**: Choose efficient algorithms, reduce time complexity

## Database Optimization

- **Query Optimization**: Use indexes, avoid N+1 queries, optimize joins
- **Connection Pooling**: Reuse connections, configure pool size appropriately
- **Caching**: Cache frequently accessed data, reduce database load
- **Batch Operations**: Batch inserts/updates, reduce round trips

## Profiling

- **CPU Profiling**: Identify CPU bottlenecks, optimize hot paths
- **Memory Profiling**: Identify memory leaks, optimize memory usage
- **Tools**: JProfiler, VisualVM, async-profiler, Java Flight Recorder

## Monitoring

- **APM Tools**: Application Performance Monitoring, identify bottlenecks
- **Metrics**: Track latency, throughput, error rates
- **Profiling**: Regular profiling in production (sampling mode)
