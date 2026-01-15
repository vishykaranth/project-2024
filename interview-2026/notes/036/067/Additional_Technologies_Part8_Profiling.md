# Profiling: JProfiler, VisualVM, async-profiler

## Overview

Profiling is the process of analyzing application performance to identify bottlenecks, memory leaks, and optimization opportunities. This guide covers three popular Java profiling tools: JProfiler, VisualVM, and async-profiler.

## Profiling Overview

```
┌─────────────────────────────────────────────────────────┐
│              Profiling Process                          │
└─────────────────────────────────────────────────────────┘

1. Attach Profiler to Application
    │
    ▼
2. Run Application
    │
    ▼
3. Collect Performance Data
    ├─ CPU usage
    ├─ Memory usage
    ├─ Thread activity
    └─ Method execution times
    │
    ▼
4. Analyze Data
    │
    ▼
5. Identify Bottlenecks
    │
    ▼
6. Optimize Code
```

## Profiling Types

### 1. CPU Profiling
- Method execution times
- Hot spots identification
- Call tree analysis
- CPU usage per thread

### 2. Memory Profiling
- Memory allocation
- Object creation
- Memory leaks detection
- Garbage collection analysis

### 3. Thread Profiling
- Thread states
- Deadlock detection
- Thread contention
- Thread activity

## 1. JProfiler

### Overview

JProfiler is a commercial Java profiler with comprehensive features for CPU, memory, and thread profiling.

### Key Features

#### 1. CPU Profiling
```
┌─────────────────────────────────────────────────────────┐
│         JProfiler CPU Profiling                        │
└─────────────────────────────────────────────────────────┘

Call Tree:
├─ Method execution times
├─ Hot spots
├─ Call frequencies
└─ Method call hierarchy

Hot Spots:
├─ Most time-consuming methods
├─ CPU usage percentage
└─ Optimization candidates
```

#### 2. Memory Profiling
- **Allocations**: Track object creation
- **Heap Walker**: Inspect heap contents
- **Memory Leaks**: Detect memory leaks
- **GC Activity**: Garbage collection analysis

#### 3. Thread Profiling
- **Thread States**: Running, waiting, blocked
- **Deadlock Detection**: Identify deadlocks
- **Thread Dump**: Capture thread states

### JProfiler Setup

#### 1. Install JProfiler
- Download from ej-technologies.com
- Install on development machine
- Configure license

#### 2. Attach to Application

**Option A: Agent Mode**
```bash
java -agentpath:/path/to/jprofiler/bin/linux-x64/libjprofilerti.so=port=8849 \
     -jar myapp.jar
```

**Option B: Offline Mode**
```bash
java -agentpath:/path/to/jprofiler/bin/linux-x64/libjprofilerti.so=offline,id=123 \
     -jar myapp.jar
```

#### 3. Connect from JProfiler
- Start JProfiler
- New Session → Attach to JVM
- Select process or enter host:port
- Start profiling

### JProfiler Views

#### CPU Views
- **Call Tree**: Method call hierarchy
- **Hot Spots**: Most time-consuming methods
- **Method List**: All methods with timings
- **Call Graph**: Visual call relationships

#### Memory Views
- **Allocations**: Object creation tracking
- **Heap Walker**: Heap contents inspection
- **GC Activity**: Garbage collection analysis
- **Memory Leaks**: Leak detection

#### Thread Views
- **Thread States**: Current thread states
- **Thread History**: Thread state over time
- **Monitor Usage**: Lock contention
- **Deadlock Detection**: Deadlock identification

### JProfiler Use Cases

1. **Performance Optimization**
   - Identify slow methods
   - Find CPU bottlenecks
   - Optimize hot paths

2. **Memory Leak Detection**
   - Track object allocations
   - Identify memory leaks
   - Analyze heap usage

3. **Thread Analysis**
   - Detect deadlocks
   - Analyze thread contention
   - Optimize concurrency

## 2. VisualVM

### Overview

VisualVM is a free, open-source profiling tool bundled with JDK. It provides basic profiling capabilities.

### Key Features

#### 1. Application Monitoring
- **Overview**: CPU, memory, threads
- **Monitor**: Real-time metrics
- **Threads**: Thread states and stack traces
- **Sampler**: CPU and memory sampling

#### 2. Profiling
- **CPU Profiling**: Method-level CPU usage
- **Memory Profiling**: Object allocations
- **Thread Profiling**: Thread activity

#### 3. Plugins
- **MBeans Browser**: JMX monitoring
- **Visual GC**: Garbage collection visualization
- **Thread Dump**: Thread analysis

### VisualVM Setup

#### 1. Launch VisualVM
```bash
# VisualVM is included with JDK
jvisualvm

# Or download standalone
# Download from visualvm.github.io
```

#### 2. Connect to Application
- Local applications appear automatically
- Remote: Add remote host
- JMX connection for remote apps

#### 3. Start Profiling
- Select application
- Go to Profiler tab
- Click CPU or Memory
- Start profiling

### VisualVM Views

#### Monitor Tab
```
┌─────────────────────────────────────────────────────────┐
│         VisualVM Monitor View                           │
└─────────────────────────────────────────────────────────┘

CPU Usage:
├─ Real-time CPU graph
└─ CPU usage percentage

Memory:
├─ Heap usage
├─ Metaspace usage
└─ GC activity

Threads:
├─ Live threads count
└─ Peak threads count
```

#### Profiler Tab
- **CPU Profiling**: Method execution times
- **Memory Profiling**: Object allocations
- **Results**: Profiling results table

#### Threads Tab
- **Thread List**: All threads
- **Thread States**: Running, waiting, blocked
- **Thread Dump**: Capture thread states

### VisualVM Use Cases

1. **Quick Performance Check**
   - Monitor CPU and memory
   - Check thread count
   - Identify obvious issues

2. **Basic Profiling**
   - CPU profiling
   - Memory profiling
   - Thread analysis

3. **GC Analysis**
   - Visual GC plugin
   - GC activity monitoring
   - Heap usage analysis

## 3. async-profiler

### Overview

async-profiler is a low-overhead sampling profiler for Java. It uses async sampling to minimize performance impact.

### Key Features

#### 1. Low Overhead
- Async sampling
- Minimal performance impact
- Production-safe

#### 2. Multiple Formats
- **Flame Graphs**: Visual performance data
- **HTML Reports**: Interactive reports
- **JFR Integration**: Java Flight Recorder

#### 3. Multiple Profiling Modes
- **CPU**: CPU profiling
- **Alloc**: Memory allocation profiling
- **Lock**: Lock contention profiling

### async-profiler Setup

#### 1. Download async-profiler
```bash
# Download from GitHub
wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/libasyncProfiler.so
```

#### 2. Attach to Application
```bash
# CPU profiling
java -agentpath:/path/to/libasyncProfiler.so=start,event=cpu,file=profile.html \
     -jar myapp.jar

# Memory allocation profiling
java -agentpath:/path/to/libasyncProfiler.so=start,event=alloc,file=alloc.html \
     -jar myapp.jar

# Lock profiling
java -agentpath:/path/to/libasyncProfiler.so=start,event=lock,file=lock.html \
     -jar myapp.jar
```

#### 3. Generate Flame Graph
```bash
# Generate flame graph
java -agentpath:/path/to/libasyncProfiler.so=start,event=cpu,flamegraph \
     -jar myapp.jar

# Output: flamegraph.html
```

### async-profiler Commands

```bash
# Start profiling
jattach <pid> load /path/to/libasyncProfiler.so true start,event=cpu,file=profile.html

# Stop profiling
jattach <pid> load /path/to/libasyncProfiler.so true stop

# Generate flame graph
java -jar converter.jar profile.jfr flamegraph.html
```

### Flame Graphs

```
┌─────────────────────────────────────────────────────────┐
│              Flame Graph Structure                      │
└─────────────────────────────────────────────────────────┘

Flame Graph:
├─ Width: Time spent
├─ Height: Call stack depth
├─ Colors: Different methods
└─ Interactive: Click to zoom

Interpretation:
├─ Wide bars: Hot methods
├─ Tall stacks: Deep call chains
└─ Colors: Different libraries
```

## 4. Profiling Comparison

### Tool Comparison

| Feature | JProfiler | VisualVM | async-profiler |
|---------|-----------|----------|----------------|
| **Cost** | Commercial | Free | Free |
| **Overhead** | Medium | Medium | Low |
| **CPU Profiling** | Excellent | Good | Excellent |
| **Memory Profiling** | Excellent | Good | Good |
| **Thread Profiling** | Excellent | Good | Limited |
| **Ease of Use** | Excellent | Good | Medium |
| **Production Use** | Yes | Limited | Yes |

### When to Use Which Tool

#### JProfiler
- Comprehensive analysis needed
- Memory leak detection
- Thread analysis
- Commercial support required

#### VisualVM
- Quick performance check
- Basic profiling
- Free solution
- Development environment

#### async-profiler
- Production profiling
- Low overhead required
- Flame graphs needed
- Continuous profiling

## 5. Profiling Best Practices

### 1. Profile Realistic Workloads
- Use production-like data
- Realistic user scenarios
- Representative load

### 2. Profile in Production (Carefully)
- Use low-overhead profilers
- Sample-based profiling
- Monitor overhead

### 3. Focus on Hot Spots
- Identify most time-consuming methods
- Optimize 20% that takes 80% of time
- Don't optimize prematurely

### 4. Profile Multiple Scenarios
- Different use cases
- Various load levels
- Edge cases

### 5. Compare Before/After
- Profile before optimization
- Profile after optimization
- Measure improvement

## 6. Common Profiling Scenarios

### Scenario 1: Slow Application

```java
// Profiling reveals:
public void processOrders(List<Order> orders) {
    for (Order order : orders) {
        processOrder(order);  // ← Takes 80% of time
    }
}

// Optimization:
// - Optimize processOrder method
// - Add caching
// - Parallel processing
```

### Scenario 2: Memory Leak

```java
// Profiling reveals:
private List<Data> cache = new ArrayList<>();  // ← Growing indefinitely

// Fix:
// - Implement cache eviction
// - Use bounded cache
// - Clear cache periodically
```

### Scenario 3: High CPU Usage

```java
// Profiling reveals:
while (true) {  // ← Infinite loop consuming CPU
    checkStatus();
}

// Fix:
// - Add sleep/delay
// - Use event-driven approach
// - Optimize checkStatus()
```

## Summary

Profiling Tools:
- **JProfiler**: Commercial, comprehensive profiling
- **VisualVM**: Free, basic profiling (JDK bundled)
- **async-profiler**: Low-overhead, production-safe

**Key Features:**
- CPU profiling (method execution times)
- Memory profiling (allocations, leaks)
- Thread profiling (states, deadlocks)
- Flame graphs (visual performance data)

**Best Practices:**
- Profile realistic workloads
- Focus on hot spots
- Compare before/after
- Use appropriate tool for scenario
- Monitor profiling overhead

**Remember**: Profiling helps identify bottlenecks, but optimization should be based on actual performance requirements!
