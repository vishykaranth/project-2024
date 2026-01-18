# Part 7: Capacity Estimation - Quick Revision

## Estimation Process

- **Storage**: Data per user × Number of users × Retention period
- **Bandwidth**: Requests/sec × Average request size × Peak multiplier
- **Database**: Read QPS, Write QPS, Data growth rate
- **Cache**: Cache size, Hit ratio, Popular data percentage
- **Servers**: CPU, Memory, I/O based on request patterns

## Back-of-Envelope Calculations

- **Use Powers of 2**: 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024
- **Round Numbers**: Make reasonable assumptions, round up
- **Example**: 1M users × 1KB data = 1GB; 10M users × 1KB = 10GB

## Common Estimates

- **User Data**: 1KB-10KB per user profile
- **Media Files**: 1MB-10MB per image, 100MB-1GB per video
- **Database**: 1M users ≈ 10GB-100GB (depending on data)
- **Cache**: 20-30% of database size for hot data

## Latency Budget

- **Total Latency**: Network (10-50ms) + Processing (10-100ms) + Database (5-50ms) + Cache (1-5ms)
- **Target**: < 200ms for web applications, < 50ms for APIs

## Growth Planning

- **1x Current**: Baseline capacity
- **10x Growth**: 10× capacity, may need architecture changes
- **100x Growth**: Major redesign, distributed systems required
