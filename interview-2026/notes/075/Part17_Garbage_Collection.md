# Part 17: Garbage Collection - Quick Revision

## GC Algorithms

- **Serial GC**: Single-threaded, stop-the-world; small applications
- **Parallel GC**: Multi-threaded, throughput-focused; batch processing
- **G1 GC**: Low-latency, divides heap into regions, concurrent marking
- **ZGC**: Ultra-low latency, <10ms pauses, multi-terabyte heaps
- **Shenandoah**: Concurrent evacuation, pause times independent of heap size

## GC Phases (G1)

- **Young Collection**: Collect Eden and Survivor spaces
- **Mixed Collection**: Collect Young + Old regions
- **Full GC**: Collect entire heap (avoid if possible)

## GC Tuning Parameters

- **-Xms / -Xmx**: Initial and maximum heap size
- **-XX:NewRatio**: Ratio of old to young generation
- **-XX:MaxGCPauseMillis**: Target pause time (G1)
- **-XX:+UseG1GC**: Enable G1 garbage collector

## GC Analysis

- **GC Logs**: Use -Xlog:gc or -XX:+PrintGCDetails
- **Tools**: GCViewer, GCPlot, jstat
- **Key Metrics**: Pause times, throughput, heap usage, allocation rate

## When to Use Each GC

- **Parallel GC**: Throughput-focused applications
- **G1 GC**: Balanced latency and throughput
- **ZGC/Shenandoah**: Low-latency requirements, large heaps
