# Part 16: JVM Memory Model - Quick Revision

## JVM Memory Structure

- **Heap**: Object storage; Young Generation (Eden, Survivor S0/S1) and Old Generation
- **Stack**: Method calls, local variables, per-thread; stores method frames
- **Metaspace**: Class metadata, method metadata, constant pool (replaced PermGen in Java 8)
- **Native Memory**: JVM itself, direct memory (NIO), native libraries

## Heap Regions

- **Young Generation**: New objects allocated here
  - Eden: New object allocation
  - Survivor S0/S1: Objects that survive GC
- **Old Generation**: Long-lived objects promoted from Young Generation
- **Object Lifecycle**: Eden → Survivor → Old Generation (based on age)

## Memory Issues

- **OutOfMemoryError: Java heap space**: Heap exhausted
- **OutOfMemoryError: Metaspace**: Class metadata exhausted
- **OutOfMemoryError: Unable to create native thread**: Too many threads
- **StackOverflowError**: Recursive calls, stack exhausted

## Key Concepts

- **Memory Visibility**: How changes in one thread are visible to others
- **Happens-Before**: Memory ordering guarantees in Java Memory Model
- **Thread-Local**: Each thread has its own copy of thread-local variables
