# JVM Internals & Advanced Java Concepts - Complete Diagrams Guide (Part 4: JIT Compilation)

## ⚡ JIT Compilation: HotSpot JIT, Optimization Techniques

---

## 1. JIT Compilation Overview

### Interpreter vs JIT
```
┌─────────────────────────────────────────────────────────────┐
│              Interpreter vs JIT Compiler                    │
└─────────────────────────────────────────────────────────────┘

Interpreter:
┌──────────┐      ┌──────────┐      ┌──────────┐
│ Bytecode │ ───► │Interpreter│ ───► │Native Code│ ───► CPU
└──────────┘      └──────────┘      └──────────┘
(line by line)    (immediate)       (slow execution)

JIT Compiler:
┌──────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
│ Bytecode │ ───► │ Profiler │ ───► │   JIT    │ ───► │Native Code│
└──────────┘      └──────────┘      └──────────┘      └──────────┘
                  (count calls)      (optimize)        (fast execution)
                                                              │
                                                              ▼
                                                         ┌──────────┐
                                                         │   CPU    │
                                                         └──────────┘

HotSpot Principle:
- Methods called frequently are "hot"
- Hot methods compiled to native code
- Compiled code cached for reuse
- Cold methods remain interpreted
```

---

## 2. HotSpot JIT Architecture

### HotSpot JIT Components
```
┌─────────────────────────────────────────────────────────────┐
│              HotSpot JIT Architecture                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Execution Flow                           │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Bytecode Execution                                    │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐            │  │
│  │  │ Method  │  │ Method   │  │ Method   │            │  │
│  │  │   A     │  │    B     │  │    C     │            │  │
│  │  └──────────┘  └──────────┘  └──────────┘            │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│                        ▼ (Method call counter)                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Profiler                                              │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method Call Count                               │  │  │
│  │  │  - Method A: 1500 calls (HOT!)                  │  │  │
│  │  │  - Method B: 50 calls (warm)                     │  │  │
│  │  │  - Method C: 2 calls (cold)                      │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│                        ▼ (Threshold reached)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Compilation Queue                                     │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method A (priority: HIGH)                      │  │  │
│  │  │  Method B (priority: MEDIUM)                   │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│        ┌───────────────┴───────────────┐                     │
│        │                               │                     │
│        ▼                               ▼                     │
│  ┌──────────────┐              ┌──────────────┐              │
│  │  C1 Compiler │              │  C2 Compiler │              │
│  │  (Client)    │              │  (Server)    │              │
│  │  - Fast      │              │  - Slow      │              │
│  │  - Less opt  │              │  - More opt  │              │
│  └──────┬───────┘              └──────┬───────┘              │
│         │                              │                      │
│         └──────────────┬───────────────┘                      │
│                        │                                      │
│                        ▼                                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Compiled Code Cache                                  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method A: Native code (optimized)              │  │  │
│  │  │  Method B: Native code (optimized)              │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                      │
│                        ▼                                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Execution                                            │  │
│  │  - Use compiled code if available                     │  │
│  │  - Fall back to interpreter if not compiled          │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Tiered Compilation

### Tiered Compilation Levels
```
┌─────────────────────────────────────────────────────────────┐
│              Tiered Compilation                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Compilation Tiers                         │
│                                                              │
│  Tier 0: Interpreter                                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  - No compilation                                      │  │
│  │  - Fast startup                                        │  │
│  │  - Slow execution                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│                        ▼ (call count > threshold)            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Tier 1: C1 (Simple)                                  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  - Fast compilation (~10ms)                    │  │  │
│  │  │  - Basic optimizations                          │  │  │
│  │  │  - No profiling                                 │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│                        ▼ (more calls)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Tier 2: C1 (Profiled)                                │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  - Fast compilation (~20ms)                    │  │  │
│  │  │  - Basic optimizations                          │  │  │
│  │  │  - Light profiling (branch, call counts)        │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│                        ▼ (even more calls)                   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Tier 3: C1 (Full Profiling)                          │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  - Fast compilation (~30ms)                    │  │  │
│  │  │  - Basic optimizations                          │  │  │
│  │  │  - Full profiling                               │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        │                                     │
│                        ▼ (hot method)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Tier 4: C2 (Fully Optimized)                         │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  - Slow compilation (~100-500ms)                │  │  │
│  │  │  - Aggressive optimizations                      │  │  │
│  │  │  - Uses profiling data                           │  │  │
│  │  │  - Best performance                              │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘

Compilation Thresholds:
-XX:CompileThreshold=10000 (default)
-XX:TieredCompilation (enabled by default)
-XX:TieredStopAtLevel=1 (stop at tier 1)
```

---

## 4. JIT Optimizations

### Common Optimizations
```
┌─────────────────────────────────────────────────────────────┐
│              JIT Optimizations                              │
└─────────────────────────────────────────────────────────────┘

1. Inlining
   Before:
   ┌──────────┐
   │ method1()│ ───► call method2() ───► method2()
   └──────────┘
   
   After:
   ┌──────────┐
   │ method1()│ ───► [method2 code inlined]
   └──────────┘
   
   Benefits:
   - Eliminates call overhead
   - Enables further optimizations
   - Threshold: -XX:MaxInlineSize=35 bytes

2. Loop Optimizations
   - Loop unrolling
   - Loop invariant code motion
   - Bounds check elimination
   
   Example:
   for (int i = 0; i < array.length; i++) {
       sum += array[i];  // Bounds check eliminated
   }

3. Dead Code Elimination
   if (false) {
       // This code is removed
   }

4. Constant Folding
   int x = 2 + 3;  // Compiled as: int x = 5;

5. Method Devirtualization
   // If actual type known at compile time
   Animal a = new Dog();  // Compiler knows it's Dog
   a.makeSound();  // Direct call, not virtual

6. Escape Analysis
   // Object doesn't escape method
   // Allocate on stack instead of heap
   void method() {
       Point p = new Point(1, 2);  // Stack allocated
       // Use p
   }  // No GC needed
```

---

## 5. Code Cache

### Code Cache Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Code Cache                                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Code Cache Memory                         │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Non-Profiled Code (C1)                               │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method A (compiled by C1)                     │  │  │
│  │  │  Method B (compiled by C1)                     │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Profiled Code (C1)                                  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method C (compiled by C1 with profiling)     │  │  │
│  │  │  Method D (compiled by C1 with profiling)     │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Non-Profiled Code (C2)                               │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method E (compiled by C2)                     │  │  │
│  │  │  Method F (compiled by C2)                     │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Profiled Code (C2)                                   │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Method G (fully optimized by C2)              │  │  │
│  │  │  Method H (fully optimized by C2)              │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Code Cache Size:                                            │
│  -XX:ReservedCodeCacheSize=240m (default)                   │
│  -XX:InitialCodeCacheSize=10m                                 │
│  -XX:CodeCacheExpansionSize=32k                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Concepts Summary

### JIT Compilation
```
1. HotSpot Detection
   - Method call counting
   - Compilation thresholds
   - Hot method identification

2. Tiered Compilation
   - Tier 0: Interpreter
   - Tier 1-3: C1 (Client compiler)
   - Tier 4: C2 (Server compiler)

3. Optimizations
   - Inlining
   - Loop optimizations
   - Dead code elimination
   - Constant folding
   - Escape analysis

4. Code Cache
   - Stores compiled native code
   - Separate segments for C1/C2
   - Size tuning important
```

---

**Next: Part 5 will cover Performance Tuning in detail.**

