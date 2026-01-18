# Part 43: Memory Leak Detection - Quick Revision

## Common Causes

- **Unclosed Resources**: Files, database connections, network connections
- **Static Collections**: Growing collections that are never cleared
- **Listeners Not Removed**: Event listeners, observers not unregistered
- **Inner Classes**: Hold references to outer class, prevent GC
- **ThreadLocal Not Cleaned**: ThreadLocal variables not removed

## Detection Techniques

- **Heap Dump Analysis**: Generate heap dump, analyze with Eclipse MAT, VisualVM
- **Memory Profilers**: JProfiler, YourKit, identify objects consuming memory
- **GC Log Analysis**: Increasing old generation usage, frequent Full GC
- **jmap, jstat**: Command-line tools for memory analysis

## Tools

- **Eclipse MAT**: Memory Analyzer Tool, identify leak suspects
- **VisualVM**: Heap dump analysis, memory profiling
- **JProfiler**: Commercial profiler, memory leak detection
- **jhat**: Built-in heap dump browser (deprecated, use MAT)

## Prevention

- **Resource Management**: Use try-with-resources, close resources
- **Weak References**: Use WeakHashMap, WeakReference for caches
- **Monitor Collections**: Track collection sizes, set limits
- **Code Reviews**: Review for common leak patterns

## Symptoms

- **OutOfMemoryError**: Heap exhausted, identify what's consuming memory
- **Increasing Memory**: Memory usage grows over time
- **Frequent Full GC**: Old generation filling up, long pause times
