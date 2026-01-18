# Part 26: Database Sharding - Quick Revision

## Sharding Strategies

- **Range-Based**: Partition by value ranges (user_id 1-1000 → Shard 1)
- **Hash-Based**: Partition by hash function (hash(user_id) % num_shards)
- **Directory-Based**: Lookup table maps key to shard
- **Composite**: Combine multiple strategies

## Sharding Challenges

- **Cross-Shard Queries**: Difficult to join data across shards
- **Rebalancing**: Moving data when adding/removing shards
- **Hot Spots**: Uneven distribution, some shards overloaded
- **Referential Integrity**: Foreign keys across shards are complex

## Shard Key Selection

- **Criteria**: Even distribution, minimize cross-shard queries, support common access patterns
- **Examples**: User ID, geographic location, time-based
- **Avoid**: Frequently changing values, low cardinality

## Rebalancing Strategies

- **Horizontal Split**: Split shard into multiple shards
- **Vertical Split**: Move some columns to different shard
- **Migration**: Move data from one shard to another

## Best Practices

- **Start Without Sharding**: Shard only when necessary
- **Plan for Growth**: Design for future sharding needs
- **Monitor Distribution**: Track shard sizes, query patterns
