# Performance Optimization - Complete Diagrams Guide (Part 1: JVM Tuning)

## ☕ JVM Tuning: Heap Sizing, GC Tuning, JIT Optimization

---

## 1. JVM Memory Structure

### Heap Memory Layout
```
┌─────────────────────────────────────────────────────────────┐
│              JVM Heap Memory Structure                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    JVM Heap (Xmx)                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Young Generation (Eden + Survivor)          │   │
│  │                                                      │   │
│  │  ┌──────────────┐  ┌──────┐  ┌──────┐              │   │
│  │  │    Eden      │  │ S0   │  │ S1   │              │   │
│  │  │  (New Objects)│  │(Survivor)│(Survivor)│              │   │
│  │  └──────────────┘  └──────┘  └──────┘              │   │
│  │                                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Old Generation (Tenured)                     │   │
│  │                                                      │   │
│  │  ┌──────────────────────────────────────────────┐  │   │
│  │  │  Long-lived objects, promoted from Young Gen  │  │   │
│  │  └──────────────────────────────────────────────┘  │   │
│  │                                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Metaspace (Class Metadata)                  │   │
│  │  (Replaces PermGen in Java 8+)                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Key Parameters:
-Xms: Initial heap size
-Xmx: Maximum heap size
-XX:NewRatio: Ratio of Old/Young generation
-XX:SurvivorRatio: Ratio of Eden/Survivor spaces
```

### Memory Allocation Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Object Lifecycle in Heap                       │
└─────────────────────────────────────────────────────────────┘

New Object Created
    │
    ▼
┌──────────────┐
│    Eden      │  ← All new objects allocated here
│  (Young Gen) │
└──────┬───────┘
       │
       │ Minor GC (frequent, fast)
       │
       ├───► Dead? ──► Yes ──► Collected immediately
       │
       │ No (Survives)
       │
       ▼
┌──────────────┐
│  Survivor    │  ← Surviving objects move here
│  (S0 or S1)  │     (Age counter incremented)
└──────┬───────┘
       │
       │ After multiple Minor GCs
       │ (Age threshold reached)
       │
       ▼
┌──────────────┐
│  Old Gen     │  ← Long-lived objects promoted here
│  (Tenured)   │
└──────┬───────┘
       │
       │ Major GC (less frequent, slower)
       │
       └───► Dead? ──► Yes ──► Collected
            │
            │ No
            │
            └───► Remains in Old Gen
```

---

## 2. Heap Sizing

### Heap Size Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Heap Sizing Strategy                            │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Initial Heap Size (-Xms)                                   │
│  ────────────────────────────────────────────────────────  │
│  • Set equal to -Xmx to avoid heap resizing                │
│  • Prevents allocation pauses during startup                │
│  • Example: -Xms4g -Xmx4g                                  │
│                                                             │
│  Maximum Heap Size (-Xmx)                                   │
│  ────────────────────────────────────────────────────────  │
│  • Should be 50-75% of available RAM                       │
│  • Leave room for OS, other processes                      │
│  • Example: 16GB RAM → -Xmx8g to -Xmx12g                  │
│                                                             │
│  Young Generation Size                                      │
│  ────────────────────────────────────────────────────────  │
│  • -XX:NewRatio=2 (Old:Young = 2:1)                       │
│  • -XX:NewSize=1g (Explicit young gen size)                │
│  • -XX:MaxNewSize=2g (Max young gen size)                  │
│                                                             │
│  Survivor Spaces                                            │
│  ────────────────────────────────────────────────────────  │
│  • -XX:SurvivorRatio=8 (Eden:Survivor = 8:1)               │
│  • Each survivor = 10% of young gen                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Example Configuration:
java -Xms4g -Xmx4g \
     -XX:NewRatio=2 \
     -XX:SurvivorRatio=8 \
     -XX:+UseG1GC \
     MyApplication
```

### Heap Size Monitoring
```
┌─────────────────────────────────────────────────────────────┐
│              Heap Usage Over Time                            │
└─────────────────────────────────────────────────────────────┘

Heap Usage (%)
    │
100%│─────────────────────────────────────────────── Xmx
    │
 75%│     ╱╲       ╱╲       ╱╲       ╱╲
    │    ╱  ╲     ╱  ╲     ╱  ╲     ╱  ╲
    │   ╱    ╲   ╱    ╲   ╱    ╲   ╱    ╲
 50%│  ╱      ╲ ╱      ╲ ╱      ╲ ╱      ╲
    │ ╱        ╲        ╲        ╲        ╲
    │╱          ╲        ╲        ╲        ╲
 25%│            ╲        ╲        ╲        ╲
    │             ╲        ╲        ╲        ╲
    │              ╲        ╲        ╲        ╲
  0%│               ╲        ╲        ╲        ╲
    └──────────────────────────────────────────────► Time
        GC    GC    GC    GC    GC    GC    GC
    
Red Flags:
- Frequent spikes near 100% → Increase -Xmx
- Constant high usage → Memory leak investigation
- Frequent GC → Tune GC parameters or increase heap
```

---

## 3. Garbage Collection Tuning

### GC Algorithms Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              GC Algorithms                                  │
└─────────────────────────────────────────────────────────────┘

Serial GC (-XX:+UseSerialGC)
─────────────────────────────────────────────────────────────
    Application Threads
    │
    │  ────► GC Thread (Single Thread)
    │         │
    │         │ Stop-the-World (STW)
    │         │
    │         └───► Application Resumes
    │
    └─────────────────────────────────────────────────────
    
Use Case: Small applications, single-core systems
Pros: Simple, low overhead
Cons: Long pause times

Parallel GC (-XX:+UseParallelGC)
─────────────────────────────────────────────────────────────
    Application Threads
    │
    │  ────► GC Threads (Multiple Threads)
    │         │ │ │ │
    │         │ │ │ │ Parallel collection
    │         │ │ │ │
    │         └─┴─┴─┘
    │
    └─────────────────────────────────────────────────────
    
Use Case: Throughput-focused applications
Pros: Faster collection, uses multiple cores
Cons: Still has STW pauses

G1 GC (-XX:+UseG1GC)
─────────────────────────────────────────────────────────────
    Heap divided into regions (1-32MB each)
    ┌───┐┌───┐┌───┐┌───┐┌───┐┌───┐┌───┐┌───┐
    │ E ││ E ││ O ││ O ││ E ││ O ││ E ││ O │
    └───┘└───┘└───┘└───┘└───┘└───┘└───┘└───┘
    
    Concurrent Marking + Incremental Collection
    Predictable pause times
    
Use Case: Large heaps (>4GB), low-latency requirements
Pros: Predictable pauses, concurrent marking
Cons: Slightly higher CPU usage

ZGC (-XX:+UseZGC)
─────────────────────────────────────────────────────────────
    Concurrent collection (no STW for most operations)
    ┌─────────────────────────────────────────┐
    │  Application Threads                   │
    │  │                                      │
    │  │  ────► GC Threads (Concurrent)     │
    │  │         │                            │
    │  │         │ No application pause      │
    │  │         │                            │
    │  └─────────┴──────────────────────────┘
    └─────────────────────────────────────────┘
    
Use Case: Very large heaps, ultra-low latency
Pros: Sub-10ms pauses, scales to TB heaps
Cons: Requires Java 11+, higher CPU usage
```

### G1 GC Tuning Parameters
```
┌─────────────────────────────────────────────────────────────┐
│              G1 GC Configuration                            │
└─────────────────────────────────────────────────────────────┘

Key Parameters:
─────────────────────────────────────────────────────────────
-XX:+UseG1GC
    Enable G1 garbage collector

-XX:MaxGCPauseMillis=200
    Target maximum GC pause time (milliseconds)
    G1 will try to keep pauses below this value

-XX:G1HeapRegionSize=16m
    Size of each G1 region (1MB to 32MB)
    Automatically calculated if not specified

-XX:InitiatingHeapOccupancyPercent=45
    Start concurrent marking when heap is 45% full
    Lower = earlier marking, more overhead
    Higher = later marking, risk of full GC

-XX:ConcGCThreads=4
    Number of threads for concurrent GC
    Usually 1/4 of total CPU cores

-XX:ParallelGCThreads=8
    Number of threads for parallel GC phases
    Usually equal to CPU cores

-XX:G1ReservePercent=10
    Reserve 10% of heap to avoid to-space exhaustion

Example:
java -Xms8g -Xmx8g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:InitiatingHeapOccupancyPercent=45 \
     -XX:G1HeapRegionSize=16m \
     MyApplication
```

### GC Logging and Analysis
```
┌─────────────────────────────────────────────────────────────┐
│              GC Log Configuration                           │
└─────────────────────────────────────────────────────────────┘

Enable GC Logging:
─────────────────────────────────────────────────────────────
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-XX:+PrintGCTimeStamps
-Xloggc:/path/to/gc.log

Java 9+ Unified Logging:
─────────────────────────────────────────────────────────────
-Xlog:gc*:file=/path/to/gc.log:time,level,tags

GC Log Format:
─────────────────────────────────────────────────────────────
2024-01-15T10:30:45.123+0000: 123.456: [GC (Allocation Failure)
  [PSYoungGen: 1024K->512K(2048K)] 2048K->1536K(8192K), 
  0.0012345 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]

Analysis:
- Allocation Failure: Triggered when Eden is full
- PSYoungGen: Parallel Scavenge Young Generation
- 1024K->512K: Before -> After (survivors)
- 0.0012345 secs: Pause time
- real=0.00: Wall clock time

GC Analysis Tools:
─────────────────────────────────────────────────────────────
- GCViewer: Visual GC log analysis
- GCPlot: Online GC log analyzer
- jstat: Real-time GC statistics
  jstat -gc <pid> 1000 (every 1 second)
```

---

## 4. JIT Compilation Optimization

### JIT Compilation Process
```
┌─────────────────────────────────────────────────────────────┐
│              JIT Compilation Pipeline                       │
└─────────────────────────────────────────────────────────────┘

Java Source Code (.java)
    │
    ▼
┌──────────────────┐
│  javac Compiler  │  ← Compile-time
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Bytecode (.class)│  ← Platform-independent
└────────┬─────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│              Runtime (JVM)                               │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Interpreter (Initial Execution)                  │  │  │
│  │  • Executes bytecode line by line                 │  │  │
│  │  • Slow but starts immediately                   │  │  │
│  └───────────────────────────────────────────────────┘  │
│                    │                                       │
│                    │ Method called multiple times          │
│                    │ (Hot Spot Detection)                 │
│                    ▼                                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  C1 Compiler (Client Compiler)                    │  │  │
│  │  • Fast compilation                               │  │  │
│  │  • Basic optimizations                            │  │  │
│  │  • -XX:CompileThreshold=1500 (default)            │  │  │
│  └───────────────────────────────────────────────────┘  │
│                    │                                       │
│                    │ Method called many more times         │
│                    │ (Tiered Compilation)                │
│                    ▼                                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  C2 Compiler (Server Compiler)                    │  │  │
│  │  • Slow compilation, highly optimized code        │  │  │
│  │  • Advanced optimizations:                        │  │  │
│  │    - Inlining                                      │  │  │
│  │    - Loop unrolling                                │  │  │
│  │    - Dead code elimination                         │  │  │
│  │    - Escape analysis                               │  │  │
│  └───────────────────────────────────────────────────┘  │
│                    │                                       │
│                    ▼                                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Native Machine Code                              │  │  │
│  │  • Executes directly on CPU                       │  │  │
│  │  • Much faster than interpreted code              │  │  │
│  └───────────────────────────────────────────────────┘  │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

### JIT Optimization Techniques
```
┌─────────────────────────────────────────────────────────────┐
│              JIT Optimizations                              │
└─────────────────────────────────────────────────────────────┘

1. Method Inlining
─────────────────────────────────────────────────────────────
Before:
    public int add(int a, int b) {
        return a + b;
    }
    int result = add(5, 3);  // Method call overhead

After (Inlined):
    int result = 5 + 3;  // Direct calculation, no call

Benefits:
- Eliminates method call overhead
- Enables further optimizations
- Controlled by: -XX:MaxInlineSize=35

2. Loop Unrolling
─────────────────────────────────────────────────────────────
Before:
    for (int i = 0; i < 4; i++) {
        sum += array[i];
    }

After (Unrolled):
    sum += array[0];
    sum += array[1];
    sum += array[2];
    sum += array[3];

Benefits:
- Reduces loop overhead
- Better CPU pipeline utilization

3. Escape Analysis
─────────────────────────────────────────────────────────────
    public void method() {
        Point p = new Point(1, 2);  // Object created
        int x = p.getX();            // Used only locally
        // p never escapes method scope
    }

Optimized:
    // Object allocated on stack instead of heap
    // No GC pressure, faster allocation

4. Dead Code Elimination
─────────────────────────────────────────────────────────────
    if (false) {  // Always false
        expensiveOperation();
    }

Optimized:
    // Entire block removed

5. Constant Folding
─────────────────────────────────────────────────────────────
    int result = 5 + 3 * 2;

Optimized:
    int result = 11;  // Calculated at compile time
```

### JIT Tuning Parameters
```
┌─────────────────────────────────────────────────────────────┐
│              JIT Compiler Tuning                            │
└─────────────────────────────────────────────────────────────┘

Compilation Thresholds:
─────────────────────────────────────────────────────────────
-XX:CompileThreshold=10000
    Number of method invocations before compilation
    Lower = compile earlier (faster warm-up, more compilation)
    Higher = compile later (slower warm-up, less compilation)

-XX:TieredCompilation
    Enable tiered compilation (C1 + C2)
    Default: enabled on server JVM

-XX:TieredStopAtLevel=4
    Stop at compilation level (1-4)
    1 = C1 only, 4 = C1 + C2 (full optimization)

Code Cache:
─────────────────────────────────────────────────────────────
-XX:InitialCodeCacheSize=32m
    Initial size of code cache

-XX:ReservedCodeCacheSize=240m
    Maximum size of code cache
    Increase if: "CodeCache is full" warnings

-XX:+UseCodeCacheFlushing
    Flush old compiled code to make room

Inlining:
─────────────────────────────────────────────────────────────
-XX:MaxInlineSize=35
    Maximum bytecode size for inlining

-XX:InlineSmallCode=1000
    Maximum code size for inlining small methods

-XX:+PrintCompilation
    Print JIT compilation events (debugging)

Example:
java -XX:+TieredCompilation \
     -XX:CompileThreshold=10000 \
     -XX:ReservedCodeCacheSize=240m \
     -XX:MaxInlineSize=35 \
     MyApplication
```

---

## 5. JVM Monitoring and Profiling

### JVM Monitoring Tools
```
┌─────────────────────────────────────────────────────────────┐
│              JVM Monitoring Stack                          │
└─────────────────────────────────────────────────────────────┘

Application Layer
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  Application Metrics (JMX, Micrometer, Prometheus)      │
│  • Heap usage, GC metrics, thread counts                │
└─────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  JVM Built-in Tools                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │    jstat     │  │    jmap     │  │    jstack    │  │
│  │  GC stats    │  │  Heap dump  │  │ Thread dump │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │    jinfo     │  │    jcmd     │  │    jconsole  │  │
│  │  JVM config  │  │  Command    │  │  GUI Monitor │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  Profiling Tools                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  VisualVM    │  │  JProfiler   │  │  YourKit     │  │
│  │  (Free)      │  │  (Commercial)│  │  (Commercial)│  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │  Async Profiler│  │  Flight Recorder│                  │
│  │  (Low overhead)│  │  (JFR)        │                  │
│  └──────────────┘  └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
```

### jstat Usage Examples
```
┌─────────────────────────────────────────────────────────────┐
│              jstat Command Examples                         │
└─────────────────────────────────────────────────────────────┘

GC Statistics (every 1 second):
─────────────────────────────────────────────────────────────
jstat -gc <pid> 1000

Output:
 S0C    S1C    S0U    S1U      EC       EU        OC         OU
1024.0 1024.0  0.0   512.0   8192.0   4096.0   16384.0    8192.0

 MC     MU      CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
2048.0 1024.0  256.0  128.0     10     0.123     2     0.456  0.579

Columns:
- S0C/S1C: Survivor 0/1 capacity (KB)
- EC/EU: Eden capacity/used (KB)
- OC/OU: Old generation capacity/used (KB)
- YGC/YGCT: Young GC count/time
- FGC/FGCT: Full GC count/time
- GCT: Total GC time

GC Capacity:
─────────────────────────────────────────────────────────────
jstat -gccapacity <pid>

Compilation Statistics:
─────────────────────────────────────────────────────────────
jstat -compiler <pid>

Class Loading:
─────────────────────────────────────────────────────────────
jstat -class <pid>
```

---

## 6. Best Practices and Checklist

### JVM Tuning Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              JVM Tuning Best Practices                      │
└─────────────────────────────────────────────────────────────┘

Heap Sizing:
☐ Set -Xms = -Xmx to avoid heap resizing
☐ Allocate 50-75% of available RAM to heap
☐ Leave room for OS and other processes
☐ Monitor heap usage patterns

GC Selection:
☐ Use G1 GC for heaps >4GB and low-latency needs
☐ Use Parallel GC for throughput-focused applications
☐ Consider ZGC for very large heaps (>32GB) and ultra-low latency
☐ Test GC under production-like load

GC Tuning:
☐ Set realistic MaxGCPauseMillis targets
☐ Monitor GC logs regularly
☐ Adjust InitiatingHeapOccupancyPercent based on patterns
☐ Use GC analysis tools to identify issues

JIT Optimization:
☐ Enable tiered compilation (default on server JVM)
☐ Monitor code cache usage
☐ Increase code cache if "CodeCache is full" warnings
☐ Use -XX:+PrintCompilation for debugging

Monitoring:
☐ Enable GC logging in production
☐ Set up JMX monitoring
☐ Use profiling tools for performance analysis
☐ Create dashboards for key metrics

Common Pitfalls:
☐ Setting -Xmx too high (causes long GC pauses)
☐ Ignoring GC logs (miss optimization opportunities)
☐ Not testing under production load
☐ Changing multiple parameters at once
☐ Not monitoring after changes
```

### Performance Tuning Workflow
```
┌─────────────────────────────────────────────────────────────┐
│              Tuning Workflow                                │
└─────────────────────────────────────────────────────────────┘

1. Baseline Measurement
   ──────────────────────────────────────────────────────────
   • Measure current performance
   • Identify bottlenecks
   • Document metrics (throughput, latency, GC pauses)

2. Hypothesis Formation
   ──────────────────────────────────────────────────────────
   • Analyze GC logs
   • Review heap usage patterns
   • Identify optimization opportunities

3. Make Changes
   ──────────────────────────────────────────────────────────
   • Change ONE parameter at a time
   • Start with heap sizing
   • Then GC algorithm/parameters
   • Finally JIT settings

4. Test and Measure
   ──────────────────────────────────────────────────────────
   • Run under production-like load
   • Measure same metrics as baseline
   • Compare results

5. Iterate
   ──────────────────────────────────────────────────────────
   • If improvement: Keep change, try next optimization
   • If regression: Revert, try different approach
   • Document learnings

6. Production Deployment
   ──────────────────────────────────────────────────────────
   • Deploy with monitoring
   • Watch metrics closely
   • Be ready to rollback
```

---

## Key Takeaways

### Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Key JVM Tuning Principles                     │
└─────────────────────────────────────────────────────────────┘

1. Heap Sizing
   • -Xms = -Xmx (avoid resizing)
   • 50-75% of available RAM
   • Monitor and adjust based on usage

2. GC Selection
   • G1 for large heaps and low latency
   • Parallel for throughput
   • ZGC for ultra-low latency

3. GC Tuning
   • Set realistic pause time targets
   • Monitor GC logs
   • Adjust based on patterns

4. JIT Optimization
   • Tiered compilation enabled
   • Monitor code cache
   • Let JIT do its job (usually)

5. Monitoring
   • GC logging essential
   • JMX for real-time metrics
   • Profiling for deep analysis

Remember:
• Measure before optimizing
• Change one thing at a time
• Test under production load
• Monitor continuously
```

---

**Next: Part 2 will cover Database Optimization.**

