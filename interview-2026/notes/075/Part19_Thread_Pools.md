# Part 19: Thread Pools - Quick Revision

## ExecutorService Types

- **newFixedThreadPool**: Fixed number of threads, unbounded queue
- **newCachedThreadPool**: Creates threads as needed, reuses idle threads
- **newSingleThreadExecutor**: Single thread, sequential execution
- **newScheduledThreadPool**: Scheduled and periodic execution

## ThreadPoolExecutor Parameters

- **corePoolSize**: Minimum threads to keep alive
- **maximumPoolSize**: Maximum threads allowed
- **keepAliveTime**: Time idle threads wait before termination
- **workQueue**: Queue for holding tasks before execution
- **rejectionPolicy**: What to do when queue is full

## Rejection Policies

- **AbortPolicy**: Throws RejectedExecutionException (default)
- **CallerRunsPolicy**: Executes task in caller thread
- **DiscardPolicy**: Silently discards task
- **DiscardOldestPolicy**: Discards oldest task, adds new one

## Thread Pool Sizing

- **CPU-bound**: Number of threads ≈ Number of CPU cores
- **I/O-bound**: Number of threads > CPU cores (waiting for I/O)
- **Formula**: Threads = CPU cores × (1 + Wait time / Service time)

## ForkJoinPool

- **Work-Stealing**: Idle threads steal work from busy threads
- **Recursive Tasks**: Split work recursively, process in parallel
- **Parallel Streams**: Uses ForkJoinPool internally
